package com.example.magnewsdequeuermail.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.magnewsdequeuermail.persistence")
public class MyBatisConfig {
}
