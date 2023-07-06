package com.example.onshell_cache.db.mapper;

import com.example.onshell_cache.db.bean.Key_Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface initMapper {
    //创建数据库并为key创建索引
    public boolean CreateInitTable();
    //获取key，用来检测是不是已经存在了
    public String getkey(String key_name);
    public List<Key_Value> getkeyall();
    //用来获取value
    public String getvalue(String key_uuid);
    public boolean updateValue(String key_uuid,String value);
    public boolean addValue(String key_uuid,String value);
    public boolean addkey(String uuid,String key_name, long expire_time);
    public boolean updatekey( String uuid, long expire_time);
    public boolean deleteKey(String key_name);

    public int TimeoutDelete(long TimeMillis);
}
