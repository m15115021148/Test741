package com.meigsmart.test741.model;

/**
 * Created by chenMeng on 2018/2/1.
 */

public class ResultModel {
    private String name;
    private int isPass = 0;
    private int type = 0;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIsPass() {
        return isPass;
    }

    public void setIsPass(int isPass) {
        this.isPass = isPass;
    }
}
