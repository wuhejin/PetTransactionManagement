package com.whj.Service;


import com.whj.Pojo.Cart;
import com.whj.Pojo.Pet;

import java.util.List;

public interface ShopcartService {
    //插入购物车
    public Integer insertCart(int user_id,int pet_id);
    //查看购物车件数
    public Integer selectShopcart(int user_id);
    //查看当前用户的购物车
    public List<Cart> selectCart(int user_id);
    //清空购物车
    public Integer clear(int user_id);
    //删除购物车商品
    public Integer deleteShop(int user_id,int pet_id);
    //更新宠物数量
    public String updatePetNum(Cart cart);

}
