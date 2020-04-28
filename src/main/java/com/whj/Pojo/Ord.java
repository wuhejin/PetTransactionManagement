package com.whj.Pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Ord implements Serializable {
    private static final long serialVersionUID = 5175082362119580768L;
    private int id;
    private String ord_id;
    private int user_id;
    private double price_sum;
    private int status_pay;
    private int status_ord;
    private String shipaddress;
    private String create_time;
    private List<Pet> pets;

}
