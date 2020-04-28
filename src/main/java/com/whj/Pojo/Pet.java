package com.whj.Pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Pet implements Serializable {
    private static final long serialVersionUID = 5175082362119580768L;
    private int pet_id;
    private String pet_name;
    private String pet_type;
    private String pet_image;
    private String pet_details;
    private Double purchase_price;
    private Double selling_price;
    private String upshelf_time;
    private int pet_num;
}
