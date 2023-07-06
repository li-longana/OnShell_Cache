package com.example.onshell_cache.service.impl;

import com.example.onshell_cache.db.bean.Key_Value;
import com.example.onshell_cache.db.service.init_database_cache;
import com.example.onshell_cache.service.cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class cacheImpl implements cache {
    @Autowired
    private init_database_cache init_database_cache;

    @Override
    public List<String> get() {
        List<Key_Value> keyall = init_database_cache.getKeyall();
        List<String> redata = new ArrayList<>();
        for (Key_Value key_Value : keyall) {
            redata.add(key_Value.getKey_name()+":"+key_Value.getKey_value());
        }
        return redata;
    }

    @Override
    public String get(String key) {
        Key_Value key_value = init_database_cache.getKey_Value(key);
        String redata=key_value.getKey_name()+":"+key_value.getKey_value();
        return redata;
    }

    @Override
    public String getValue(String key) {
        return init_database_cache.getKey_Value(key).getKey_value();
    }

    @Override
    public boolean set(String key, String value, long seconds) {
        Key_Value key_value = new Key_Value();
        key_value.setKey_name(key);
        key_value.setKey_value(value);
        key_value.setSeconds(seconds);
        return init_database_cache.addkey(key_value);
    }

    @Override
    public boolean set(String key, String value) {
        long nowMillis = System.currentTimeMillis();
        long fifteenDaysMillis = 15 * 24 * 60 * 60 * 1000L;
        long twentyDaysMillis = 20 * 24 * 60 * 60 * 1000L;
        long randomMillis = fifteenDaysMillis + (long) (Math.random() * (twentyDaysMillis - fifteenDaysMillis));
        long expireTimeMillis = nowMillis + randomMillis;
        return set(key,value,expireTimeMillis);

    }

    @Override
    public boolean del(String key) {
        return init_database_cache.rmkey(key);
    }
}
