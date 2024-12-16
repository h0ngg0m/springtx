package com.honggom.springtx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
public class SpringtxApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringtxApplication.class, args);
    }

}
