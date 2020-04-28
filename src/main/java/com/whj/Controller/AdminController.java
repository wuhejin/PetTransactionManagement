package com.whj.Controller;

import com.alipay.api.AlipayApiException;
import com.whj.Pojo.*;
import com.whj.Service.AdminService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    //查看管理员信息
    @RequestMapping("/personal")
    public String personal(){
        return "/admin/personal";
    }

    //管理员页面
    @RequestMapping("/adminindex")
    @RequiresPermissions("admin:*")
    public String AdminIndex(){
        return "/admin/index";
    }

    //查看所有用户
    @PostMapping("/userlist")
    @RequiresPermissions("admin:*")
    @ResponseBody
    public List<User> userlist(){
        return adminService.selectUser();
    }

    //修改用户状态
    @RequiresPermissions("admin:*")
    @PostMapping("/updateUserLock")
    @ResponseBody
    public String updateUserLock(@RequestBody User user){
        return adminService.updateUserLock(user);
    }

    //删除用户
    @RequiresPermissions("admin:*")
    @PostMapping("/deleteUser")
    @ResponseBody
    public String deleteUser(@RequestBody int id){
        return adminService.deleteUser(id);
    }

    //查看单个用户
    @RequiresPermissions("admin:*")
    @RequestMapping("/user")
    public String user(int id, Model model){
        model.addAttribute("user",adminService.User(id));
        return "/admin/user-edit";
    }

    //修改用户信息
    @RequiresPermissions("admin:*")
    @PostMapping("/updateUser")
    @ResponseBody
    public String updateUser(@RequestBody User user){
        return adminService.updateUser(user);
    }

    //批量删除用户
    @RequiresPermissions("admin:*")
    @PostMapping("/deleteAll")
    @ResponseBody
    public String deleteAll(@RequestBody String checkbox){
        return adminService.deleteAll(checkbox);

    }

    //查看所有宠物信息
    @PostMapping("/petlist")
    @RequiresPermissions("admin:*")
    @ResponseBody
    public List<Pet> petlist(){
        return adminService.selectPet();
    }


    //查看所有宠物类型
    @PostMapping("/selectType")
    @RequiresPermissions("admin:*")
    @ResponseBody
    public List<Pet_type> selectType(){
        return adminService.selectType();
    }

    //添加宠物
    @PostMapping("/addPet")
    @RequiresPermissions("admin:*")
    @ResponseBody
    public String addPet(@RequestParam("pet_image_file") MultipartFile file, Pet pet){
        return adminService.addPet(file,pet);
    }

    //删除宠物
    @RequiresPermissions("admin:*")
    @PostMapping("/deletePet")
    @ResponseBody
    public String deletePet(@RequestBody int id){
        return adminService.deletePet(id);
    }

    //查看单个宠物
    @RequiresPermissions("admin:*")
    @RequestMapping("/pet")
    public String pet(int id, Model model){
        model.addAttribute("pet",adminService.Pet(id));
        return "/admin/pet-edit";
    }

    //修改宠物信息
    @RequestMapping("/updatePet")
    @ResponseBody
    public String updatePet(@RequestParam("pet_image_file") MultipartFile file, Pet pet){
        return adminService.updatePet(file,pet);
    }

    //批量删除宠物
    @RequiresPermissions("admin:*")
    @PostMapping("/deleteAllPet")
    @ResponseBody
    public String deleteAllPet(@RequestBody String checkbox){
        return adminService.deleteAllPet(checkbox);
    }
    //删除宠物类型
    @RequiresPermissions("admin:*")
    @PostMapping("/deletePetType")
    @ResponseBody
    public String deletePetType(@RequestBody int id){
        int result = adminService.deletePetType(id);
        if(result > 0)
            return "success";
        else if (result == -2)
            return "typefalse";
        else
            return "false";
    }

    //查看单个宠物类型
    @RequiresPermissions("admin:*")
    @RequestMapping("/pettype")
    public String pettype(int id, Model model){
        Pet_type pet_type = adminService.pettype(id);
        model.addAttribute("pet_type",pet_type);
        return "/admin/pettype-edit";
    }

    //修改宠物类型信息
    @RequiresPermissions("admin:*")
    @PostMapping("/updatePetType")
    @ResponseBody
    public String updatePetType(@RequestBody Pet_type pet_type){
        return adminService.updatePetType(pet_type);
    }

    //添加宠物类型
    @RequiresPermissions("admin:*")
    @PostMapping("/addPetType")
    @ResponseBody
    public String addPetType(@RequestBody String type){
        return adminService.addPetType(type);
    }
    //批量删除宠物类型
    @RequiresPermissions("admin:*")
    @PostMapping("/deleteAllPetType")
    @ResponseBody
    public String deleteAllPetType(@RequestBody String checkbox){
        return adminService.deleteAllPetType(checkbox);
    }

    //查看所有订单
    @PostMapping("/orderlist")
    @RequiresPermissions("admin:*")
    @ResponseBody
    public List<Ord> orderlist() {
        return adminService.orderlist();
    }

    //订单操作
    @PostMapping("/order")
    @RequiresPermissions("admin:*")
    @ResponseBody
    public String order(@RequestBody Ord ord) throws AlipayApiException {
        return adminService.order(ord.getOrd_id(),ord.getPrice_sum(),ord.getStatus_ord());
    }

    //查看所有轮播图
    @PostMapping("/selectRotation")
    @RequiresPermissions("admin:*")
    @ResponseBody
    public List<Rotation> selectRotation(){
        return adminService.selectRotation();
    }

    //添加轮播图
    @PostMapping("/addRotation")
    @ResponseBody
    public String addRotation(@RequestParam("images") MultipartFile file){
        return adminService.addRotation(file);
    }

    //查看单个轮播图
    @RequiresPermissions("admin:*")
    @RequestMapping("/rotation")
    public String rotation(int id, Model model){
        model.addAttribute("id",id);
        return "/admin/rotation-edit";
    }

    //修改轮播图
    @PostMapping("/updateRotation")
    @ResponseBody
    public String updateRotation(@RequestParam("images") MultipartFile file,int id){
        System.out.println(file.toString());
        return adminService.updateRotation(file,id);
    }

    //删除宠物类型
    @RequiresPermissions("admin:*")
    @PostMapping("/deleteRotation")
    @ResponseBody
    public String deleteRotation(@RequestBody int id){
        return adminService.deleteRotation(id);
    }

    //批量删除轮播图
    @RequiresPermissions("admin:*")
    @PostMapping("/deleteAllRotation")
    @ResponseBody
    public String deleteAllRotation(@RequestBody String checkbox){
        return adminService.deleteAllRotation(checkbox);
    }
}
