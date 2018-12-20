package com.demo.shrek;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.demo.shrek.dao")
public class CodeGenApplication {
    public static void main(String[] args)
    {
        SpringApplication.run(CodeGenApplication.class, args);
    }
}
