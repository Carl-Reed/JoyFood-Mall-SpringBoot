package com.lpw.joyfoodmall.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpw.joyfoodmall.entity.DTO.ProductSaveDTO;
import com.lpw.joyfoodmall.entity.OmsOrderItem;
import com.lpw.joyfoodmall.entity.Product;
import com.lpw.joyfoodmall.entity.SkuStock;
import com.lpw.joyfoodmall.entity.SysFile;
import com.lpw.joyfoodmall.mapper.ProductMapper;
import com.lpw.joyfoodmall.service.OmsOrderItemService;
import com.lpw.joyfoodmall.service.ProductService;
import com.lpw.joyfoodmall.service.SkuStockService;
import com.lpw.joyfoodmall.service.SysFileService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final SkuStockService skuStockService;
    private final SysFileService sysFileService;
    private final ObjectMapper objectMapper;
    private final OmsOrderItemService orderItemService;

    @Override
    // 查询商品信息，包括SKU信息
    public IPage<Product> getProductListWithStock(Page<Product> page, LambdaQueryWrapper<Product> wrapper) {
        return baseMapper.selectProductPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProduct(ProductSaveDTO saveDTO) {
        // 获取旧的关联路径（如果是更新操作，用于后续清理孤儿图片）
        Set<String> oldPaths = new HashSet<>();
        if (saveDTO.getId() != null) {
            Product oldProduct = this.getById(saveDTO.getId());
            if (oldProduct != null) {
                oldPaths = collectPathsFromEntity(oldProduct);
            }
        }

        // 处理商品主表 (SPU) 基础数据映射
        Product product = new Product();
        BeanUtils.copyProperties(saveDTO, product);
        product.setDetailHtml(saveDTO.getDescription());

        // 保存或更新 SPU，获取最新的 productId (如果是新增则生成 ID)
        this.saveOrUpdate(product);
        Long productId = product.getId();
        String productName = product.getProductName();

        // 构造并转换新的 SKU 列表数据
        List<SkuStock> newSkuList = saveDTO.getSkuList().stream().map(dto -> {
            SkuStock sku = new SkuStock();
            BeanUtils.copyProperties(dto, sku);
            sku.setProductId(productId);

            // 自动生成 skuName：商品标题 + 规格值
            if (StringUtils.hasText(dto.getSpecName())) {
                String specValues = extractSpecValues(dto.getSpecName());
                sku.setSkuName(productName + " " + specValues);
                // 将规格名解析为 JSON (如 [{"key":"颜色","value":"黑色"}])
                sku.setSpData(parseSpecNameToJson(dto.getSpecName()));
            } else {
                sku.setSkuName(productName);
            }
            return sku;
        }).collect(Collectors.toList());

        // 处理 SKU 的差集同步（删除不在当前列表中的旧规格）
        List<SkuStock> existingSkus = skuStockService.list(new LambdaQueryWrapper<SkuStock>()
                .eq(SkuStock::getProductId, productId));

        if (!existingSkus.isEmpty()) {
            // 提取前端传回的列表中所有带有 ID 的 SKU
            Set<Long> currentSkuIds = newSkuList.stream()
                    .map(SkuStock::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // 找出：数据库中有，但前端传来的列表中没有的 ID（即被用户删除的规格项）
            List<Long> idsToRemove = existingSkus.stream()
                    .map(SkuStock::getId)
                    .filter(id -> !currentSkuIds.contains(id))
                    .collect(Collectors.toList());

            if (!idsToRemove.isEmpty()) {
                skuStockService.removeByIds(idsToRemove);
            }
        }

        // 执行 SKU 批量保存或更新
        if (!CollectionUtils.isEmpty(newSkuList)) {
            skuStockService.saveOrUpdateBatch(newSkuList);
        }

        // 计算最低价并回填到 SPU 表
        syncMinPrice(productId);

        // 处理图片引用状态逻辑
        handleImageLifecycle(saveDTO, oldPaths);
    }

    /**
     * 计算并同步该商品的最低价格到 SPU 表
     */
    private void syncMinPrice(Long productId) {
        // 重新查一遍最新的 SKU 列表
        List<SkuStock> latestSkus = skuStockService.list(new LambdaQueryWrapper<SkuStock>()
                .eq(SkuStock::getProductId, productId));

        // 计算所有有效 SKU 中的最小值
        BigDecimal minPrice = latestSkus.stream()
                .map(SkuStock::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 更新到 Product 表的冗余字段 price 中
        this.update(new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, productId)
                .set(Product::getPrice, minPrice));
    }

    /**
     * 处理图片生命周期：标记激活与废弃
     */
    private void handleImageLifecycle(ProductSaveDTO saveDTO, Set<String> oldPaths) {
        Set<String> newPaths = collectAllImagePaths(saveDTO);

        // 计算不再引用的路径：旧路径中有，但新路径中没有的
        Set<String> abandonedPaths = new HashSet<>(oldPaths);
        abandonedPaths.removeAll(newPaths);

        // 批量更新文件表状态
        if (!abandonedPaths.isEmpty()) {
            sysFileService.update(new LambdaUpdateWrapper<SysFile>()
                    .in(SysFile::getFilePath, abandonedPaths)
                    .set(SysFile::getIsUsed, 0));
        }
        if (!newPaths.isEmpty()) {
            sysFileService.update(new LambdaUpdateWrapper<SysFile>()
                    .in(SysFile::getFilePath, newPaths)
                    .set(SysFile::getIsUsed, 1));
        }
    }

    @Override
    public ProductSaveDTO getProductDetail(Long id) {
        // 获取 SPU
        Product product = this.getById(id);
        if (product == null) return null;

        // 获取 SKU 列表
        List<SkuStock> skus = skuStockService.list(new LambdaQueryWrapper<SkuStock>()
                .eq(SkuStock::getProductId, id));

        // 组装 DTO
        ProductSaveDTO dto = new ProductSaveDTO();
        BeanUtils.copyProperties(product, dto);
        dto.setDescription(product.getDetailHtml()); // 映射回前端字段

        // 转换 SKU 列表
        List<ProductSaveDTO.SkuStockDTO> skuDTOs = skus.stream().map(sku -> {
            ProductSaveDTO.SkuStockDTO skuDTO = new ProductSaveDTO.SkuStockDTO();
            BeanUtils.copyProperties(sku, skuDTO);
            // 将 JSON 解析回 "颜色:红;尺寸:M" 供前端显示
            skuDTO.setSpecName(parseJsonToSpecName(sku.getSpData()));
            return skuDTO;
        }).collect(Collectors.toList());

        dto.setSkuList(skuDTOs);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIdWithSKU(Long id){
        Product product = getById(id);
        Set<String> paths = collectPathsFromEntity(product);
        // 将所有关联的图片记录设为未使用
        if (!paths.isEmpty()) {
            sysFileService.update(new LambdaUpdateWrapper<SysFile>()
                    .in(SysFile::getFilePath, paths)
                    .set(SysFile::getIsUsed, 0));
        }
        // 执行逻辑删除
        this.removeById(id);
        LambdaQueryWrapper<SkuStock> skuWrapper = new LambdaQueryWrapper<>();
        skuWrapper.eq(SkuStock::getProductId,id);
        skuStockService.remove(skuWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务
    public void deleteBatchProducts(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) return;

        // 收集所有待删除商品涉及的图片路径
        Set<String> allPathsToAbandon = new HashSet<>();

        // 批量查询商品信息
        List<Product> products = this.listByIds(ids);
        for (Product product : products) {
            // 获取单个商品（含其SKU）的所有路径
            allPathsToAbandon.addAll(collectPathsFromEntity(product));
        }

        // 将所有关联的图片在 sys_file 中标记为未使用
        if (!allPathsToAbandon.isEmpty()) {
            sysFileService.update(new LambdaUpdateWrapper<SysFile>()
                    .in(SysFile::getFilePath, allPathsToAbandon)
                    .set(SysFile::getIsUsed, 0));
        }
        // 删除关联的 SKU 信息
        LambdaQueryWrapper<SkuStock> skuWrapper = new LambdaQueryWrapper<>();
        skuWrapper.in(SkuStock::getProductId, ids);
        skuStockService.remove(skuWrapper);

        this.removeByIds(ids);
    }

    public void increaseProductSales(Long orderId){
        // 获取该订单下的所有商品清单
        List<OmsOrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId)
        );

        // 遍历并累加销量
        for (OmsOrderItem item : items) {
            Long productId = item.getProductId();
            Integer quantity = item.getProductQuantity(); // 用户买了几件，销量就加几

            this.update(new UpdateWrapper<Product>()
                    .setSql("sales_actual = sales_actual + " + quantity)
                    .eq("id", productId));
        }
    }

    // ================== 前端商城方法 ==================
    @Override
    public List<Product> getNewArrivals(int limit){

        return this.lambdaQuery()
                .eq(Product::getIsPublish, 1)          // 必须是上架状态
                .eq(Product::getIsNew, 1)           // 标记为新品的
                .orderByDesc(Product::getCreateTime) // 按创建时间倒序，保证是“新”品
                .last("LIMIT " + limit)
                .list();
    }

    // ================== 私有辅助方法 ==================

    /**
     * 格式转换： "颜色:红;尺寸:L" -> "[{"key":"颜色","value":"红"}, ...]"
     */
    private String parseSpecNameToJson(String specName) {
        if (!StringUtils.hasText(specName)) return "[]";
        List<Map<String, String>> list = new ArrayList<>();
        String[] pairs = specName.split(";");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                Map<String, String> map = new HashMap<>();
                map.put("key", kv[0]);
                map.put("value", kv[1]);
                list.add(map);
            }
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    /**
     * 格式转换：JSON -> "颜色:红;尺寸:L"
     */
    private String parseJsonToSpecName(String spData) {
        if (!StringUtils.hasText(spData)) return "";
        try {
            List<Map<String, String>> list = objectMapper.readValue(spData, new TypeReference<List<Map<String, String>>>() {});
            return list.stream()
                    .map(map -> map.get("key") + ":" + map.get("value"))
                    .collect(Collectors.joining(";"));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 从规格字符串中提取数值部分
     * 输入: "颜色:黑色;尺寸:512G"
     * 输出: "黑色 512G"
     */
    private String extractSpecValues(String specName) {
        if (!StringUtils.hasText(specName)) return "";
        // 按分号拆分规格项
        String[] parts = specName.split(";");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            // 按冒号拆分键值对，取后面的值
            if (part.contains(":")) {
                String val = part.substring(part.indexOf(":") + 1).trim();
                sb.append(val).append(" ");
            } else {
                sb.append(part.trim()).append(" ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * 提取富文本图片并只保留本系统路径
     */
    private Set<String> extractAndFilterImgSrcs(String html) {
        Set<String> srcs = new HashSet<>();
        if (!StringUtils.hasText(html)) return srcs;

        // 正则匹配 src 属性
        Pattern p = Pattern.compile("<img[^>]+?src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*?>");
        Matcher m = p.matcher(html);
        while (m.find()) {
            String path = m.group(1);
            // 【过滤器】：只处理本系统的文件路径，防止把外部链接存入 sys_file
            if (path.startsWith("/api/")) {
                String normalizedPath = path.substring(path.indexOf("/files/"));
                srcs.add(normalizedPath);
            }
        }
        return srcs;
    }

    /**
     * 提取商品涉及的所有图片路径
     */
    private Set<String> collectAllImagePaths(ProductSaveDTO saveDTO) {
        Set<String> paths = new HashSet<>();
        if (saveDTO == null) return paths;

        // 主图
        if (StringUtils.hasText(saveDTO.getPic())) paths.add(saveDTO.getPic());

        // 相册
        if (StringUtils.hasText(saveDTO.getAlbumPics())) {
            List<String> albums = JSON.parseArray(saveDTO.getAlbumPics(), String.class);
            if (albums != null) paths.addAll(albums);
        }

        // SKU图片
        if (!CollectionUtils.isEmpty(saveDTO.getSkuList())) {
            saveDTO.getSkuList().forEach(sku -> {
                if (StringUtils.hasText(sku.getPic())) paths.add(sku.getPic());
            });
        }

        // 富文本图片
        paths.addAll(extractAndFilterImgSrcs(saveDTO.getDescription()));

        return paths;
    }

    /**
     * 从商品实体类中提取所有图片路径
     * 用于在更新或删除前获取旧的图片关联关系
     */
    private Set<String> collectPathsFromEntity(Product product) {
        Set<String> paths = new HashSet<>();
        if (product == null) {
            return paths;
        }

        // 提取主图路径
        if (StringUtils.hasText(product.getPic())) {
            paths.add(product.getPic());
        }

        // 提取相册图片路径 (JSON 字符串解析)
        if (StringUtils.hasText(product.getAlbumPics())) {
            try {
                // 使用 Fastjson 解析相册 JSON 数组
                List<String> albums = JSON.parseArray(product.getAlbumPics(), String.class);
                if (albums != null) {
                    paths.addAll(albums);
                }
            } catch (Exception e) {
                System.out.println("解析商品相册图片失败:");
            }
        }

        // 提取富文本中的图片路径 (HTML 字符串正则提取)
        if (StringUtils.hasText(product.getDetailHtml())) {
            paths.addAll(extractAndFilterImgSrcs(product.getDetailHtml()));
        }

        // 提取该商品下所有 SKU 的图片路径
        // 如果是更新操作，需要额外查询一次该商品关联的所有 SKU
        List<SkuStock> skuList = skuStockService.list(new LambdaQueryWrapper<SkuStock>()
                .eq(SkuStock::getProductId, product.getId()));

        if (skuList != null) {
            for (SkuStock sku : skuList) {
                if (StringUtils.hasText(sku.getPic())) {
                    paths.add(sku.getPic());
                }
            }
        }

        return paths;
    }
}
