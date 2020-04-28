package com.whj.Mapper;

import com.whj.Pojo.Pet;
import com.whj.Pojo.Pet_type;
import com.whj.Pojo.Rotation;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.util.List;

public interface IndexMapper {

    //查看所有宠物类型
    @Select("select * from pet_type")
    public List<Pet_type> selectType();

    //查看热门宠物
    @Select("select * from pet order by upshelf_time asc limit 4")
    public List<Pet> selectPet();

    //查询宠物
    @Select("<script>" +
            "select * from pet where 1=1" +
            "<if test='pet_name != null'> and pet_name like concat('%',#{pet_name},'%') </if>" +
            "<if test='pet_type != null'> and pet_type = #{pet_type} </if>" +
            " order by purchase_price desc"+
            "</script>")
    public List<Pet> selectPetAll(Pet pet);

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

    @Select("select type from pet_type where id = #{pet_type}")
    public String findType(@Param("pet_type") int pet_type);

    @Select("select * from rotation")
    List<Rotation> selectRotation();
}
