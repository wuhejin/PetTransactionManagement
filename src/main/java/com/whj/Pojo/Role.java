package com.whj.Pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Role implements Serializable {

    private static final long serialVersionUID = 5175082362119580768L;

    private Integer id;
    //角色
    private String role;
    //描述
    private String description;
    //拥有权限
    private List<Permissions> permissions;
}
