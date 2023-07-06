package com.example.onshell_cache.db.service;

import com.example.onshell_cache.db.bean.Key_Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface init_database_cache {
    //创建索引数据库，用于存储所有的键，只有id，键，过期时间
    public void Create_index_database();
    //用于向索引数据库添加新的索引，以及过期时间
    public boolean addkey(Key_Value key_Value);
    public boolean updatekey(String uuid,String value,String time);
    //用于删除某个键
    public boolean rmkey(String key);
    //用于获取某个键,应允许嵌套key
    public String getKey(String key_name);
    public List<Key_Value> getKeyall();

    public void TimeoutDelete();
    public Key_Value getKey_Value(String key);
}
