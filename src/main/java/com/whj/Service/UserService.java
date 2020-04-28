package com.whj.Service;

import com.whj.Pojo.User;

public interface UserService {
    //查询用户信息
    public User findUser(String tel);
    //查询用户信息，角色，权限
    public User findUserByRoleByPer(String tel);
    //注册用户
    public Boolean registerUser(User user);
    //修改用户状态
    public Integer updateUserLock(User user);
    //重置密码
    public Integer alterPWD(User user);
    //修改用户个人信息
    public Integer updateUser(User user);
    //查看用户信息
    public User user(int id);
    //修改用户地址
    public int ModifyADS(User user);
}
