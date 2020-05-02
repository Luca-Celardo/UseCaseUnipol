package com.example.magnewsdequeuermail.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.magnewsdequeurermail.persistence")
public class MyBatisConfig {
}
