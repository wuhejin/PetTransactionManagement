package com.whj.Service.Impl;

import com.whj.Mapper.ShopcartMapper;
import com.whj.Pojo.Cart;
import com.whj.Pojo.Pet;
import com.whj.Service.ShopcartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShopcartServiceImpl implements ShopcartService {

    @Autowired
    private ShopcartMapper shopcartMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Transactional
    @Override
    public Integer insertCart(int user_id, int pet_id) {
        int result = shopcartMapper.select(user_id,pet_id);
        if(result > 0) {
            redisTemplate.delete("shopcart"+user_id);
            return 0;
        }
        return shopcartMapper.insertCart(user_id,pet_id);
    }

    @Override
    public Integer selectShopcart(int user_id) {
        return shopcartMapper.selectShopcart(user_id);
    }

    @Override
    public List<Cart> selectCart(int user_id) {
        List<Cart> carts = (List<Cart>)redisTemplate.opsForValue().get("shopcart");
        if(carts == null){
            carts = shopcartMapper.selectCart(user_id);
            redisTemplate.opsForValue().set("shopcart"+user_id,carts);
        }
        return carts;
    }

    @Override
    public Integer clear(int user_id) {
        redisTemplate.delete("shopcart"+user_id);
        return shopcartMapper.clear(user_id);
    }

    @Override
    public Integer deleteShop(int user_id, int pet_id) {
        redisTemplate.delete("shopcart"+user_id);
        return shopcartMapper.deleteShop(user_id,pet_id);
    }

    @Override
    public String updatePetNum(Cart cart) {
        int result = shopcartMapper.updatePetNum(cart);
        if(result > 0){
            redisTemplate.delete("shopcart"+cart.getUser_id());
            return "success";
        }
        else
            return "false";
    }
}
