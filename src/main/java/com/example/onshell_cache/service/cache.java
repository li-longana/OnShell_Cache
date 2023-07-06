package com.example.onshell_cache.service;

import com.example.onshell_cache.db.bean.Key_Value;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface cache {
    public List<String> get();
    public String get(String key);
    public String getValue(String key);
    public boolean set(String key,String value,long seconds);
    public boolean set(String key,String value);
    public boolean del(String key);
}
