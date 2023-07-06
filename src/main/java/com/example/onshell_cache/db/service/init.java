package com.example.onshell_cache.db.service;

import com.example.onshell_cache.db.bean.Key_Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class init implements CommandLineRunner {
    public static String Password="/password";
    //程序启动加载初始化
    @Autowired
    private init_database_cache init_database_cache;
    @Override
    public void run(String... args) throws Exception {
        //启动时初始化数据库
        init_database_cache.Create_index_database();
        Key_Value key_value = new Key_Value();
        key_value.setKey_name("age");
        key_value.setKey_value("18");
        key_value.setSeconds(60);
        init_database_cache.addkey(key_value);
        init_database_cache.getKey("age");
//        init_database_cache.rmkey("age");
    }
}
