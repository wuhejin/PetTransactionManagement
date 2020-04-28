package com.whj.Service;

import com.whj.Pojo.Pet;
import com.whj.Pojo.Pet_type;
import com.whj.Pojo.Rotation;

import java.util.List;

public interface IndexService {
    //查看所有宠物类型
    public List<Pet_type> selectType();
    //查看热门宠物
    public List<Pet> selectPet();
    //查询宠物
    public List<Pet> selectPetAll(Pet pet);
    //查看单个宠物
    public Pet Pet(int id);
    //查看所有轮播图
    List<Rotation> selectRotation();
}
