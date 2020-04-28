package com.whj.Service;

import com.alipay.api.AlipayApiException;
import com.whj.Pojo.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminService {
    //查看所有用户
    public List<User> selectUser();
    //修改用户状态
    public String updateUserLock(User user);
    //删除用户
    public String deleteUser(int id);
    //查看单个用户
    public User User(int id);
    //修改用户信息
    public String updateUser(User user);
    //批量删除用户
    public String deleteAll(String checkbox);
    //查看所有宠物信息
    public List<Pet> selectPet();
    //查看所有宠物类型
    public List<Pet_type> selectType();
    //添加宠物
    public String addPet(MultipartFile file,Pet pet);
    //删除宠物
    public String deletePet(int id);
    //查看单个宠物
    public Pet Pet(int id);
    //修改宠物信息
    public String updatePet(MultipartFile file,Pet pet);
    //批量删除宠物
    public String deleteAllPet(String checkbox);
    //删除宠物类型
    public Integer deletePetType(int id);
    //查看单个宠物类型
    public Pet_type pettype(int id);
    //修改宠物类型
    public String updatePetType(Pet_type pet_type);
    //添加宠物类型
    public String addPetType(String type);
    //批量删除宠物类型
    public String deleteAllPetType(String checkbox);
    //查看所有订单
    public List<Ord> orderlist();
    //订单操作
    public String order(String id,Double price_sum, int status_ord) throws AlipayApiException;
    //查看所有轮播图
    public List<Rotation> selectRotation();
    //添加轮播图
    public String addRotation(MultipartFile file);
    //更新轮播图
    public String updateRotation(MultipartFile file,int id);
    //删除轮播图
    public String deleteRotation(int id);
    //批量删除轮播图
    public String deleteAllRotation(String checkbox);
}
