package com.whj.Mapper;

import com.whj.Pojo.*;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.util.List;

public interface AdminMapper {

    //查看所有用户
    @Select("select * from user where id in (select user_id from user_role where role_id = 2)")
    public List<User> selectUser();

    //修改用户状态
    @Update("update user set locked = #{locked} where id = #{id}")
    public Integer updateUserLock(User user);

    //删除用户
    @Delete("delete from user where id = #{id}")
    public Integer deleteUser(int id);

    //删除用户权限
    @Delete("delete from user_role where user_id = #{id}")
    public Integer deleteUserRole(int id);

    //查看单个用户
    @Select("select * from user where id = #{id}")
    public User User(int id);

    //修改用户信息
    @Update("<script>" +
            "update user set username = #{username} , tel = #{tel} , age = #{age}, gender = #{gender}" +
            "<if test='per_signature != null'> , per_signature = #{per_signature}</if>" +
            "<if test='per_signature == null'> , per_signature = ''</if>" +
            "<if test='address != null'> , address = #{address}</if>" +
            "<if test='address == null'> , address = ''</if>" +
            "where id = #{id}" +
            "</script>")
    public Integer updateUser(User user);

    //批量删除用户
    @Delete("<script>" +
            "delete from user where id in" +
            "<foreach collection='id' open='(' separator=',' close=')' item='id' index='index' >" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    public Integer deleteAll(@Param("id") int[] id);

    //批量删除用户权限
    @Delete("<script>" +
            "delete from user_role where user_id in" +
            "<foreach collection='id' open='(' separator=',' close=')' item='id' index='index' >" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    public Integer deleteAllRole(@Param("id") int[] id);

    //查看所有宠物信息
    @Select("select * from pet")
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
            @Result(column = "upshelf_time",property = "upshelf_time")
    })
    public List<Pet> selectPet();

    @Select("select type from pet_type where id = #{pet_type}")
    public String findType(@Param("pet_type") int pet_type);

    //查看所有宠物类型
    @Select("select * from pet_type")
    public List<Pet_type> selectType();

    //添加宠物
    @Insert("insert into pet(pet_name,pet_type,pet_image," +
            "pet_details,purchase_price,selling_price,upshelf_time)" +
            "values (#{pet_name},#{pet_type},#{pet_image},#{pet_details}," +
            "#{purchase_price},#{selling_price},#{upshelf_time})")
    public Integer AddPet(Pet pet);

    //删除宠物
    @Delete("delete from pet where pet_id = #{id}")
    public Integer deletePet(int id);

    //查看单个宠物
    @Select("select * from pet where pet_id = #{id}")
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
            @Result(column = "upshelf_time",property = "upshelf_time")
    })
    public Pet Pet(int id);

    //修改宠物信息
    @Update("<script>" +
            "update pet set pet_name = #{pet_name} , pet_type = #{pet_type} , pet_details = #{pet_details}," +
            "purchase_price = #{purchase_price},selling_price = #{selling_price}" +
            "<if test='pet_image != null'> , pet_image = #{pet_image}</if>" +
            "where pet_id = #{pet_id}" +
            "</script>")
    public Integer updatePet(Pet pet);

    //批量删除宠物
    @Delete("<script>" +
            "delete from pet where pet_id in" +
            "<foreach collection='id' open='(' separator=',' close=')' item='id' index='index' >" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    public Integer deleteAllPet(@Param("id") int[] id);

    //删除宠物类型
    @Delete("delete from pet_type where id = #{id}")
    public Integer deletePetType(int id);

    //查看将要删除的宠物类型是否有存在
    @Select("select count(*) from pet where pet_type = #{id}")
    public Integer PetType(int id);

    //查看单个宠物类型
    @Select("select * from pet_type where id = #{id}")
    public Pet_type pettype(int id);

    //修改宠物类型
    @Update("update pet_type set type = #{type} where id = #{id}")
    public Integer updatePetType(Pet_type pet_type);

    //添加宠物类型
    @Insert("insert into pet_type(type) value(#{type})")
    public Integer addPetType(String type);

    //批量删除宠物类型
    @Delete("<script>" +
            "delete from pet_type where id in" +
            "<foreach collection='id' open='(' separator=',' close=')' item='id' index='index' >" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    public Integer deleteAllPetType(@Param("id") int[] id);

    //查看将要删除的宠物类型是否有存在
    @Select("<script>" +
            "select count(*) from pet where pet_type in" +
            "<foreach collection='id' open='(' separator=',' close=')' item='id' index='index' >" +
            "#{id}" +
            "</foreach>" +
            "group by pet_type" +
            "</script>")
    public List<Integer> AllPetType(@Param("id") int[] id);

    //查看所有订单
    @Select("select * from ord")
    @Results({
            @Result(id = true,column = "id",property = "id"),
            @Result(column = "ord_id",property = "ord_id"),
            @Result(column = "user_id",property = "user_id"),
            @Result(column = "price_sum",property = "price_sum"),
            @Result(column = "status_pay",property = "status_pay"),
            @Result(column = "status_ord",property = "status_ord"),
            @Result(column = "shipaddress",property = "shipaddress"),
            @Result(column = "id",property = "pets",
                    many = @Many(select = "findPet",
                            fetchType = FetchType.EAGER))
    })
    public List<Ord> orderlist();

    //根据订单查询宠物
    @Select("select * from pet p join ord_pet op on p.pet_id = op.pet_id " +
            "where ord_id = #{ord_id}")
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

    //订单操作
    @Update("update ord set status_ord = #{status_ord} where ord_id = #{ord_id}")
    public Integer order(@Param("ord_id")String ord_id,@Param("status_ord") int status_ord);


    //查看所有轮播图
    @Select("select * from rotation")
    public List<Rotation> selectRotation();

    //添加轮播图
    @Insert("insert into rotation(images) value(#{images})")
    public int addRotation(Rotation rotation);

    //查看单个轮播图
    @Select("select * from rotation where id = #{id}")
    public Rotation selectOneRotataion(int id);

    //更新轮播图
    @Update("update rotation set images = #{images} where id = #{id}")
    public int updateRotation(Rotation rotation);

    //删除轮播图
    @Delete("delete from rotation where id = #{id}")
    public int deleteRotation(int id);

    //批量删除轮播图
    @Delete("<script>" +
            "delete from rotation where id in" +
            "<foreach collection='id' open='(' separator=',' close=')' item='id' index='index' >" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    public int deleteAllRotation(@Param("id") int[] id);
}
