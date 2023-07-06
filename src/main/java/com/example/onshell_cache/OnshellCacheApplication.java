package com.example.onshell_cache;

import com.example.onshell_cache.db.service.impl.init_database_table;
import com.onshell.email.Drivers.CacheUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.example.onshell_cache.db.mapper")
public class OnshellCacheApplication {
	private static ConfigurableApplicationContext ctx;
	public static ConfigurableApplicationContext getCtx(){
		return ctx;
	}
	public static void main(String[] args) {
		ctx = SpringApplication.run(OnshellCacheApplication.class, args);
		CacheUtil.init();
	}

}
