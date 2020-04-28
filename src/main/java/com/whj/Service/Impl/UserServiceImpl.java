package com.whj.Service.Impl;

import com.whj.Config.ShiroEncryption;
import com.whj.Mapper.UserMapper;
import com.whj.Pojo.User;
import com.whj.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User findUser(String tel) {
        return userMapper.findUser(tel);
    }

    @Override
    public User findUserByRoleByPer(String tel) {
        return userMapper.findUserByRoleByPer(tel);
    }

    @Transactional
    @Override
    public Boolean registerUser(User user) {
        User user1 = userMapper.findUser(user.getTel());
        if(user1 != null )
            return false;
        //存入数据库格式化时间
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        user.setCreate_time(dateFormat.format(date));
        //加密
        user.setPassword(ShiroEncryption.shiroencryption(user.getPassword(),user.getCreate_time()));
        //账号正常
        user.setLocked(1);
        userMapper.registerUser(user);
        user1 = userMapper.findUser(user.getTel());
        //插入角色，普通用户id为2
        userMapper.insertRole(user1.getId(),2);
        return true;
    }

    @Override
    public Integer updateUserLock(User user) {
        return userMapper.updateUserLock(user);
    }

    @Override
    public Integer alterPWD(User user) {
        User u = userMapper.findUser(user.getTel());
        if(u == null)
            return -2;
        user.setPassword(ShiroEncryption.shiroencryption(user.getPassword(),u.getCreate_time()));
        int result = userMapper.alterPWD(user);
        return result;
    }

    @Override
    public Integer updateUser(User user) {
        return userMapper.updateUser(user);
    }

    @Override
    public User user(int id) {
        return userMapper.user(id);
    }

    @Override
    public int ModifyADS(User user) {
        return userMapper.ModifyADS(user);
    }
}
