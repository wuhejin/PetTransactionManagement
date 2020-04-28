package com.whj.Controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.whj.Pojo.Pet;
import com.whj.Pojo.Pet_type;
import com.whj.Pojo.Rotation;
import com.whj.Service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;


    //设置主页
    @RequestMapping("/")
    public String index(){
        return "/front/index";
    }

    //查找传值用
    @GetMapping("/search")
    public String search(@RequestParam("pet_name")String pet_name, @RequestParam("pet_type")String pet_type , HttpServletRequest request,Model model){
        Pet pet = new Pet();
        if("null".equals(pet_name)){
            pet_name = null;
        }
        request.setAttribute("pet_name",pet_name);
        model.addAttribute("pet_name",pet_name);
        if("null".equals(pet_type)){
            pet_type = null;
        }
        pet.setPet_type(pet_type);
        request.setAttribute("pet_type",pet_type);
        return "/front/search";
    }
    //查询宠物
    @PostMapping("/search")
    @ResponseBody
    public List<Pet> search(@RequestBody Pet pet,Model model){
        model.addAttribute("pet_name",pet.getPet_name());
        return indexService.selectPetAll(pet);
    }


    //查看所有宠物类型
    @PostMapping("/selectType")
    @ResponseBody
    public List<Pet_type> selectType(){
        return indexService.selectType();
    }

    //查看所有轮播图
    @PostMapping("/selectRotation")
    @ResponseBody
    public List<Rotation> selectRotation(){
        return indexService.selectRotation();
    }

    //主页显示前几个宠物
    @PostMapping("/selectPet")
    @ResponseBody
    public List<Pet> selectPet(){
        return indexService.selectPet();
    }

    @RequestMapping("/detail/{pet_id}")
    public String detail(@PathVariable("pet_id") int pet_id,Model model){
        Pet pet = indexService.Pet(pet_id);
        model.addAttribute("pet",pet);
        return "/front/detail";
    }

}
