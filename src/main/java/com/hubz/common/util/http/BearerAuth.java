package com.hubz.common.util.http;

/**
 * @author hubz
 * @date 2022/8/14 22:23
 **/
public class BearerAuth implements Auth {

    private String token;

    public BearerAuth(String token) {
        this.token = token;
    }

    @Override
    public String getAuth() {
        return "Bearer " + this.token;
    }
}