package com.example.magnewsrestcontroller.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.magnewsrestcontroller.persistence")
public class MyBatisConfig {
}
