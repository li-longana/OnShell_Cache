package com.example.onshell_cache.db.service.impl;

import com.example.onshell_cache.db.bean.Key_Value;
import com.example.onshell_cache.db.mapper.initMapper;
import com.example.onshell_cache.db.service.init_database_cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
@Service
public class init_database_table implements init_database_cache {

    @Autowired
    private initMapper initMapper;
    @Override
    public void Create_index_database() {
        initMapper.CreateInitTable();
    }

    @Override
    public boolean addkey(Key_Value key_value) {
        long TimeMillis = System.currentTimeMillis();

        String uuid = String.valueOf(UUID.randomUUID())+TimeMillis;
        try {
            String key_uuid = initMapper.getkey(key_value.getKey_name());
            if (key_uuid == null || key_uuid.isEmpty()) {
                initMapper.addkey(uuid, key_value.getKey_name(),key_value.getSeconds() * 1000 + TimeMillis);
                initMapper.addValue(uuid, key_value.getKey_value());
            } else {
                initMapper.updatekey(key_uuid, key_value.getSeconds() * 1000 + TimeMillis);
                initMapper.updateValue(key_uuid, key_value.getKey_value());
            }

            return true;
        }catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updatekey(String uuid, String value, String time) {
        return false;
    }

    @Override
    public boolean rmkey(String key) {

        return  initMapper.deleteKey(key);
    }

    @Override
    public String getKey(String key_name) {
        String key_uuid = initMapper.getkey(key_name);
        if(key_uuid == null || key_uuid.isEmpty()) {
            return null;
        }else {
            return initMapper.getvalue(key_uuid);
        }
    }

    @Override
    public List<Key_Value> getKeyall() {
        return initMapper.getkeyall();

    }

    @Override
    @Scheduled(cron = "* * * * * ?")
    public void TimeoutDelete() {
        initMapper.TimeoutDelete(System.currentTimeMillis());
    }

    @Override
    public Key_Value getKey_Value(String key) {
        Key_Value redata = new Key_Value();
        redata.setKey_name(key);
        redata.setKey_value(getKey(key));
        return redata;
    }
}
