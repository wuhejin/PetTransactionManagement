package com.whj.Mapper;

import com.whj.Pojo.Ord;
import com.whj.Pojo.Pet;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.util.List;

public interface OrdMapper {

    //创建订单
    @Insert("insert into ord(ord_id,user_id,price_sum,status_pay,status_ord,shipaddress,create_time)" +
            "values(#{ord_id},#{user_id},#{price_sum}," +
            "#{status_pay},#{status_ord},#{shipaddress},#{create_time})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    public int createOrd(Ord ord);

    //插入订单与宠物的联系
    @Insert("insert into ord_pet values(#{ord_id},#{pet_id},#{pet_num})")
    public int insertOrdPet(@Param("ord_id")int ord_id,@Param("pet_id") int pet_id,@Param("pet_num")int pet_num);

    //查询全部订单
    @Select("select * from ord where user_id = #{user_id} order by id desc")
    @Results({
            @Result(id = true,column = "id",property = "id"),
            @Result(column = "ord_id",property = "ord_id"),
            @Result(column = "user_id",property = "user_id"),
            @Result(column = "price_sum",property = "price_sum"),
            @Result(column = "status_pay",property = "status_pay"),
            @Result(column = "status_ord",property = "status_ord"),
            @Result(column = "shipaddress",property = "shipaddress"),
            @Result(column = "create_time",property = "create_time"),
            @Result(column = "id",property = "pets",
                    many = @Many(select = "findPet",
                            fetchType = FetchType.EAGER))
    })
    public List<Ord> selectAll(int user_id);

    @Select("select * from pet p join ord_pet op on p.pet_id = op.pet_id " +
            "where op.ord_id = #{ord_id}")
    @Results({
            @Result(id=true,column="pet_id",property="pet_id"),
            @Result(column="pet_name",property="pet_name"),
            @Result(column="pet_type",property="pet_type",
                    one=@One(select="findType",
                            fetchType= FetchType.EAGER)),
            @Result(column="pet_image",property="pet_image"),
            @Result(column="pet_details",property="pet_details"),
            @Result(column="purchase_price",property="purchase_price"),
            @Result(column="selling_price",property="selling_price"),
            @Result(column = "upshelf_time",property = "upshelf_time"),
            @Result(column = "pet_num",property = "pet_num")
    })
    public List<Pet> findPet(@Param("ord_id")int ord_id);

    @Select("select type from pet_type where id = #{pet_type}")
    public String findType(@Param("pet_type") int pet_type);
    //查询订单详情
    @Select("select * from ord where user_id = #{user_id} and ord_id = #{ord_id}")
    @Results({
            @Result(id = true,column = "id",property = "id"),
            @Result(column = "ord_id",property = "ord_id"),
            @Result(column = "user_id",property = "user_id"),
            @Result(column = "price_sum",property = "price_sum"),
            @Result(column = "status_pay",property = "status_pay"),
            @Result(column = "status_ord",property = "status_ord"),
            @Result(column = "shipaddress",property = "shipaddress"),
            @Result(column = "create_time",property = "create_time"),
            @Result(column = "id",property = "pets",
                    many = @Many(select = "findPet",
                            fetchType = FetchType.EAGER))
    })
    public Ord ord(@Param("user_id")int user_id,@Param("ord_id")String ord_id);

    //用户手动取消订单 //用户确认收货
    @Update("update ord set status_ord = #{status_ord} where user_id = #{user_id} and ord_id = #{ord_id}")
    public Integer Order(@Param("user_id")int user_id,@Param("ord_id")String ord_id,@Param("status_ord")int status_ord);

    //用户支付订单
    @Update("update ord set status_pay = #{status_pay} where ord_id = #{ord_id}")
    public Integer payOrder(@Param("ord_id")String ord_id,@Param("status_pay")int status_pay);

}
