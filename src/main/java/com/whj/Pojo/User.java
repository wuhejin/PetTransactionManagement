package com.whj.Pojo;

import lombok.Data;

import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

@Data
public class User implements Serializable {

    private static final long serialVersionUID = 5175082362119580768L;

    //主键
    private Integer id;
    //昵称
    private String username;
    //手机号，唯一确定，1开头，11位手机号
    private String tel;
    //密码，存入数据库要MD5加密
    private String password;
    //性别
    private String gender;
    //出生日期
    private String age;
    //个性签名
    private String per_signature;
    //地址
    private String address;
    //账号状态，0锁定，1正常
    private int locked;
    //账号创建日期
    private String create_time;
    //账号所属角色
    private List<Role> roles;
}
