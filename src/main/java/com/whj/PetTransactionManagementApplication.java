package com.whj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.whj.Mapper")
public class PetTransactionManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetTransactionManagementApplication.class, args);
    }

}
