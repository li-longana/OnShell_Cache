package com.example.onshell_cache.db.bean;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Key_Value {
    private String key_name;
    private String key_value;
    private long seconds;
}
