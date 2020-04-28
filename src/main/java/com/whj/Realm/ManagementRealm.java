package com.whj.Realm;

import com.whj.Config.ShiroEncryption;
import com.whj.Pojo.Permissions;
import com.whj.Pojo.Role;
import com.whj.Pojo.User;
import com.whj.Service.UserService;
import com.whj.Util.MyByteSource;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;


public class ManagementRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;



    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println("权限认证");
        User user = (User) principalCollection.getPrimaryPrincipal();
        user = userService.findUserByRoleByPer(user.getTel());
        //获取角色
        Set<String> roles = new HashSet<>();
        //获取权限
        Set<String> permissions = new HashSet<>();
        for (Role role : user.getRoles()){
            roles.add(role.getRole());
            for(Permissions permission: role.getPermissions()){
                permissions.add(permission.getPermission());
            }
        }
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.setRoles(roles);
        simpleAuthorizationInfo.setStringPermissions(permissions);
        return simpleAuthorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        // 1. 从主体传过来的认证信息中，获得用户名
//        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
//        String tel = usernamePasswordToken.getUsername();
//        String password = new String(usernamePasswordToken.getPassword());
        String tel = (String)authenticationToken.getPrincipal();
        System.out.println("身份认证");
        // 2. 通过用户名到数据库中获取信息
        User user = userService.findUser(tel);
        if (user == null) {
            throw new UnknownAccountException("用户名未找到");
        }
        //密码加密比较
//        password = ShiroEncryption.shiroencryption(password,user.getCreate_time());
//        if(!password.equals(user.getPassword())){
//            throw new IncorrectCredentialsException("密码错误");
//        }
        // 账号锁定
//        if (user.getLocked() == 0) {
//            throw new LockedAccountException("账号已被锁定,请联系管理员");
//        }
        // 设置加密的 盐 为用户创建时间
        SimpleAuthenticationInfo authenticationInfo =
                new SimpleAuthenticationInfo(user, user.getPassword(),
                       new MyByteSource(user.getCreate_time()),
                        "ManagementRealm");

        return authenticationInfo;
    }


    /**
     * 重写方法,清除当前用户的的 授权缓存
     * @param principals
     */
    @Override
    public void clearCachedAuthorizationInfo(PrincipalCollection principals) {
        super.clearCachedAuthorizationInfo(principals);
    }

    /**
     * 重写方法，清除当前用户的 认证缓存
     * @param principals
     */
    @Override
    public void clearCachedAuthenticationInfo(PrincipalCollection principals) {
        super.clearCachedAuthenticationInfo(principals);
    }

    @Override
    public void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
    }

    /**
     * 建议重写此方法，提供唯一的缓存Key
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Object getAuthenticationCacheKey(PrincipalCollection principals) {
        User user = (User) principals.getPrimaryPrincipal();
        return user.getTel();
    }
}
