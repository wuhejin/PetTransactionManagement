package com.whj.Pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Pet_type implements Serializable {

    private static final long serialVersionUID = 5175082362119580768L;
    private int id;
    private String type;
}
