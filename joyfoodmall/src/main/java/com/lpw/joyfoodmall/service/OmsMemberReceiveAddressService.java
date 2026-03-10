package com.lpw.joyfoodmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lpw.joyfoodmall.entity.OmsMemberReceiveAddress;

public interface OmsMemberReceiveAddressService extends IService<OmsMemberReceiveAddress> {
    boolean saveAddress(OmsMemberReceiveAddress address);
    void setDefault(Long userId, Long addressId);
}
