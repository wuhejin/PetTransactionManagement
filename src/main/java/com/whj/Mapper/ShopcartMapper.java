package com.whj.Mapper;

import com.whj.Pojo.Cart;
import com.whj.Pojo.Pet;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.util.List;

public interface ShopcartMapper {

    //查看购物车是否已添加
    @Select("select count(*) from shopcart where user_id = #{user_id} and pet_id = #{pet_id}")
    public Integer select(@Param("user_id")int user_id,@Param("pet_id") int pet_id);

    //添加购物车
    @Insert("insert into shopcart values(#{user_id}, #{pet_id},1)")
    public Integer insertCart(@Param("user_id")int user_id,@Param("pet_id") int pet_id);

    //查看购物车件数
    @Select("select count(*) from shopcart where user_id = #{user_id}")
    public Integer selectShopcart(int user_id);

    //查看当前用户的购物车
    @Select("select * from shopcart where user_id = #{user_id}")
    @Results({
            @Result(id=true,column = "user_id",property = "user_id"),
            @Result(column = "pet_id",property = "pet",
                one = @One(select = "selectPet",fetchType = FetchType.EAGER)
            ),
            @Result(column = "num",property = "num")
    })
    public List<Cart> selectCart(int user_id);

    @Select("select * from pet where pet_id = #{pet_id}")
    public Pet selectPet(int pet_id);

    //清空购物车
    @Delete("delete from shopcart where user_id = #{user_id}")
    public Integer clear(int user_id);

    //删除购物车商品
    @Delete("delete from shopcart where user_id = #{user_id} and pet_id = #{pet_id}")
    public Integer deleteShop(@Param("user_id")int user_id,@Param("pet_id") int pet_id);

    //更新宠物数量
    @Update("update shopcart set num = #{num} where user_id = #{user_id} and pet_id = #{pet.pet_id}")
    public int updatePetNum(Cart cart);
}
