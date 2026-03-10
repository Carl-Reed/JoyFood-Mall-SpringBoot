package com.lpw.joyfoodmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lpw.joyfoodmall.entity.OmsMemberReceiveAddress;
import com.lpw.joyfoodmall.mapper.OmsMemberReceiveAddressMapper;
import com.lpw.joyfoodmall.service.OmsMemberReceiveAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OmsMemberReceiveAddressServiceImpl extends ServiceImpl<OmsMemberReceiveAddressMapper, OmsMemberReceiveAddress> implements OmsMemberReceiveAddressService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveAddress(OmsMemberReceiveAddress address) {
        // 如果当前设置为默认地址，先清空之前的默认设置
        if (address.getDefaultStatus() != null && address.getDefaultStatus() == 1) {
            this.clearDefaultStatus(address.getMemberId());
        }
        return this.saveOrUpdate(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long addressId) {
        // 1. 全部设为非默认
        this.clearDefaultStatus(userId);

        // 2. 设指定ID为默认
        OmsMemberReceiveAddress address = new OmsMemberReceiveAddress();
        address.setId(addressId);
        address.setDefaultStatus(1);
        this.updateById(address);
    }

    private void clearDefaultStatus(Long userId) {
        OmsMemberReceiveAddress update = new OmsMemberReceiveAddress();
        update.setDefaultStatus(0);
        this.update(update, new LambdaQueryWrapper<OmsMemberReceiveAddress>()
                .eq(OmsMemberReceiveAddress::getMemberId, userId));
    }
}
