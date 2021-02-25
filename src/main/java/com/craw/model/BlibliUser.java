package com.craw.model;

public class BlibliUser {

    private String userId;
    private String name;
    private String sex;
    private String birthday;
    private String constellation;
    private String img;
    private String imgId;
    private int level;
    private int fansNum;
    private int attentionNum;

    public String getUserId() {
        return userId;
    }

    public BlibliUser setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getName() {
        return name;
    }

    public BlibliUser setName(String name) {
        this.name = name;
        return this;
    }

    public String getSex() {
        return sex;
    }

    public BlibliUser setSex(String sex) {
        this.sex = sex;
        return this;
    }

    public String getBirthday() {
        return birthday;
    }

    public BlibliUser setBirthday(String birthday) {
        this.birthday = birthday;
        return this;
    }

    public String getConstellation() {
        return constellation;
    }

    public BlibliUser setConstellation(String constellation) {
        this.constellation = constellation;
        return this;
    }

    public String getImg() {
        return img;
    }

    public BlibliUser setImg(String img) {
        this.img = img;
        return this;
    }

    public String getImgId() {
        return imgId;
    }

    public BlibliUser setImgId(String imgId) {
        this.imgId = imgId;
        return this;
    }

    public int getLevel() {
        return level;
    }

    public BlibliUser setLevel(int level) {
        this.level = level;
        return this;
    }

    public int getFansNum() {
        return fansNum;
    }

    public BlibliUser setFansNum(int fansNum) {
        this.fansNum = fansNum;
        return this;
    }

    public int getAttentionNum() {
        return attentionNum;
    }

    public BlibliUser setAttentionNum(int attentionNum) {
        this.attentionNum = attentionNum;
        return this;
    }
}
