package com.whj.Controller;

import com.whj.Pojo.Cart;
import com.whj.Pojo.Pet;
import com.whj.Pojo.User;
import com.whj.Service.ShopcartService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/shopcart")
public class ShopcartController {

    @Autowired
    private ShopcartService shopcartService;

    //添加购物车
    @PostMapping("/insertCart")
    @ResponseBody
    public String insertCart(@RequestBody int pet_id, HttpSession session){
        User user = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
        int result = shopcartService.insertCart(user.getId(),pet_id);
        if(result > 0){
            session.setAttribute("shopcart",shopcartService.selectShopcart(user.getId()));
            return "success";
        }
        else
            return "false";

    }

    //返回购物车网页
    @GetMapping("/selectCart")
    public String ShopCart(){
        return "/front/cart";
    }

    //查看当前用户的购物车
    @PostMapping("/selectCart")
    @ResponseBody
    public List<Cart> selectCart(){
        User user = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
        List<Cart> carts = shopcartService.selectCart(user.getId());
        return carts;
    }

    //清空购物车
    @PostMapping("/clear")
    @ResponseBody
    public String clear(HttpSession session){
        User user = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
        int result = shopcartService.clear(user.getId());
        if(result > 0){
            session.setAttribute("shopcart",shopcartService.selectShopcart(user.getId()));
            return "success";
        }
        else
            return "false";
    }

    @PostMapping("/deleteShop")
    @ResponseBody
    public String deleteShop(@RequestBody int id,HttpSession session){
        User user = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
        int result = shopcartService.deleteShop(user.getId(),id);
        if(result > 0) {
            session.setAttribute("shopcart",shopcartService.selectShopcart(user.getId()));
            return "success";
        }
        else
            return "false";
    }

    //更新宠物数量
    @PostMapping("/updatePetNum/{pet_id}")
    @ResponseBody
    public String updatePetNum(@PathVariable("pet_id")int pet_id,@RequestBody int num){
        Cart cart = new Cart();
        cart.setNum(num);
        Pet pet = new Pet();
        pet.setPet_id(pet_id);
        cart.setPet(pet);
        User user = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
        cart.setUser_id(user.getId());
        return shopcartService.updatePetNum(cart);
    }

}
