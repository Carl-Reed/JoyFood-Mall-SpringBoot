package com.lpw.joyfoodmall.controller.mall;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lpw.joyfoodmall.common.Result;
import com.lpw.joyfoodmall.entity.OmsMemberReceiveAddress;
import com.lpw.joyfoodmall.service.OmsMemberReceiveAddressService;
import com.lpw.joyfoodmall.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mall/address")
@RequiredArgsConstructor
public class OmsAddressController {

    private final OmsMemberReceiveAddressService addressService;

    @GetMapping("/list")
    public Result<List<OmsMemberReceiveAddress>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(addressService.list(new LambdaQueryWrapper<OmsMemberReceiveAddress>()
                .eq(OmsMemberReceiveAddress::getMemberId, userId)));
    }

    @PostMapping("/save")
    public Result<?> save(@RequestBody OmsMemberReceiveAddress address) {
        address.setMemberId(SecurityUtils.getCurrentUserId());
        addressService.saveAddress(address);
        return Result.success("保存成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        addressService.removeById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/setDefault/{id}")
    public Result<?> setDefault(@PathVariable Long id) {
        addressService.setDefault(SecurityUtils.getCurrentUserId(), id);
        return Result.success("设置成功");
    }
}
