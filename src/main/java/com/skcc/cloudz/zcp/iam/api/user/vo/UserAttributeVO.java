package com.skcc.cloudz.zcp.iam.api.user.vo;

import javax.validation.constraints.NotNull;

public class UserAttributeVO {
    @NotNull
    private String key;
    @NotNull
    private String value;
    
    public UserAttributeVO() {}

    public UserAttributeVO(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
}
