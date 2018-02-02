package com.meigsmart.test741.db;

/**
 * Created by chenMeng on 2018/1/30.
 */

public class TypeModel {
    private int id;
    private String type;
    private int allTime;
    private String startTime;
    private String filepath;
    private int isRun = 0;
    private int isPass = 0;//是否通过 0未测试  1 pass  2 failure

    public int getIsPass() {
        return isPass;
    }

    public void setIsPass(int isPass) {
        this.isPass = isPass;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAllTime() {
        return allTime;
    }

    public void setAllTime(int allTime) {
        this.allTime = allTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public int getIsRun() {
        return isRun;
    }

    public void setIsRun(int isRun) {
        this.isRun = isRun;
    }
}
