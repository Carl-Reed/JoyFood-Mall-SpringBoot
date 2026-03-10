package com.lpw.joyfoodmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lpw.joyfoodmall.entity.OmsCartItem;

public interface OmsCartService extends IService<OmsCartItem> {
    void addCart(OmsCartItem cartItem);
}
