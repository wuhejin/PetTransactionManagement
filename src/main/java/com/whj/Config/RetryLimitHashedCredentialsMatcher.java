package com.whj.Config;

import java.util.concurrent.atomic.AtomicInteger;

import com.whj.Pojo.User;
import com.whj.Service.UserService;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @description: 登陆次数限制
 */
public class RetryLimitHashedCredentialsMatcher extends HashedCredentialsMatcher {

    @Autowired
    private UserService userService;

    private Cache<String, AtomicInteger> passwordRetryCache;

    public RetryLimitHashedCredentialsMatcher(CacheManager cacheManager) {
        passwordRetryCache = cacheManager.getCache("passwordRetryCache");
    }

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {

        //获取用户名
        String tel = (String)token.getPrincipal();
        //获取用户登录次数
        AtomicInteger retryCount = passwordRetryCache.get(tel);
        if (retryCount == null) {
            //如果用户没有登陆过,登陆次数加1 并放入缓存
            retryCount = new AtomicInteger(0);
            passwordRetryCache.put(tel, retryCount);
        }
        if (retryCount.incrementAndGet() > 5) {
            //如果用户登陆失败次数大于5次 抛出锁定用户异常  并修改数据库字段
            User user = userService.findUser(tel);
                //数据库字段 默认为 1  就是正常状态 所以 要改为0
                //修改数据库的状态字段为锁定
            if (user.getLocked() == 1){
                user.setLocked(0);
                userService.updateUserLock(user);
            }
            //抛出用户锁定异常
            throw new LockedAccountException();
        }
        //判断用户账号和密码是否正确
        boolean matches = super.doCredentialsMatch(token, info);
        if (matches) {
            //如果正确,从缓存中将用户登录计数 清除
            passwordRetryCache.remove(tel);
        }
        return matches;
    }

    /**
     * 根据用户名 解锁用户
     * @param tel
     * @return
     */
    public void unlockAccount(String tel){
        User user = userService.findUser(tel);
        if (user != null){
            //修改数据库的状态字段为锁定
            user.setLocked(1);
            userService.updateUser(user);
            passwordRetryCache.remove(tel);
        }
    }

}