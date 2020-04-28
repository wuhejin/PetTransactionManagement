package com.whj.Pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Permissions implements Serializable {

    private static final long serialVersionUID = 5175082362119580768L;

    private Integer id;
    //权限
    private String permission;
    //描述
    private String description;
}
