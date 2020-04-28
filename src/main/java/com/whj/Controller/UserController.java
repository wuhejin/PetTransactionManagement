package com.whj.Controller;

import com.whj.Pojo.User;
import com.whj.Realm.ManagementRealm;
import com.whj.Service.ShopcartService;
import com.whj.Service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private ShopcartService shopcartService;

    //登陆页面和注册页面
    @RequestMapping("/log")
    public String log(){
        return "login";
    }

    //注册
    @PostMapping("/register")
    @ResponseBody
    public Boolean register(@RequestBody User user){
        user.setUsername(user.getTel());
        Boolean aBoolean  = userService.registerUser(user);
        return aBoolean;
    }

//    配置记住我管理器，添加 @Param("rememberMe") boolean rememberMe，从前端页面获取name='rememberMe'的复选框
//      UsernamePasswordToken usernamePasswordToken =
//        new UsernamePasswordToken(user.getTel(),user.getPassword(),rememberMe);
    //登陆
    @PostMapping("/login")
    @ResponseBody
    public String login(@RequestBody User user, HttpSession session){
        //获得主体
        Subject subject = SecurityUtils.getSubject();
        //身份封装
        UsernamePasswordToken usernamePasswordToken =
                new UsernamePasswordToken(user.getTel(),user.getPassword());
        try {
            subject.login(usernamePasswordToken);
            User user1 = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
            System.out.println("登录成功");
            session.setAttribute("shopcart",shopcartService.selectShopcart(user1.getId()));
            if(subject.isPermitted("ordinary:*"))
                return "success";
            else
                return "successadmin";
        }catch (UnknownAccountException e) {
            e.printStackTrace();
            return "null";
        }catch (IncorrectCredentialsException e){
            System.out.println("密码错误");;
            return "pwdfalse";
        }catch (LockedAccountException e){
            System.out.println("账号锁定");;
            return "lock";
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("出现其他异常");
            return "excpetion";
        }

    }
    //重置密码
    @PostMapping("/alterPWD")
    @ResponseBody
    public String alterPWD(@RequestBody User user){
        if(SecurityUtils.getSubject().getPrincipal() != null){
            System.out.println(SecurityUtils.getSubject().getPrincipals());
            /*
             * 清除权限认证缓存
             */
            DefaultWebSecurityManager securityManager = (DefaultWebSecurityManager)SecurityUtils.getSecurityManager();
            ManagementRealm managementRealm = (ManagementRealm) securityManager.getRealms().iterator().next();
            managementRealm.clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
            //清除身份认证缓存
            managementRealm.clearCachedAuthenticationInfo(SecurityUtils.getSubject().getPrincipals());
        }

        int result = userService.alterPWD(user);
        if(result > 0)
            return "success";
        else if(result == -2)
            return "null";
        else
            return "false";
    }

    //返回个人信息页面
    @RequestMapping("/person")
    public String person(Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
        model.addAttribute("user",user);
        return "/front/person";
    }
    //返回修改个人信息页面
    @RequestMapping("/User")
    public String User(Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
        model.addAttribute("user",user);
        return "/front/user-edit";
    }

    //修改个人信息
    @PostMapping("/updateUser")
    @ResponseBody
    public String updateUser(@RequestBody User user){
        int result = userService.updateUser(user);
        if(result > 0){
            PrincipalCollection principalCollection =
                    new SimplePrincipalCollection(user,"ManagementRealm");
            SecurityUtils.getSubject().runAs(principalCollection);
            return "success";
        }
        else {
            return "false";
        }
    }

    @PostMapping("/user")
    @ResponseBody
    public User user(){
        User user = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
        return userService.user(user.getId());
    }

    //修改用户地址
    @PostMapping("/ModifyADS")
    @ResponseBody
    public String ModifyADS(@RequestBody User user) {
        User users = (User) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
        user.setId(users.getId());
        int result = userService.ModifyADS(user);
        if (result > 0) {
            users.setAddress(user.getAddress());
            PrincipalCollection principalCollection =
                    new SimplePrincipalCollection(users, "ManagementRealm");
            SecurityUtils.getSubject().runAs(principalCollection);
            return "success";
        } else {
            return "false";
        }
    }
}
