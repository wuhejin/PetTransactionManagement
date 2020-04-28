package com.whj.Pojo;

import lombok.Data;

import java.io.Serializable;


@Data
public class Cart implements Serializable {
    private static final long serialVersionUID = 5175082362119580768L;
    private int user_id;
    private Pet pet;
    private int num;

}
