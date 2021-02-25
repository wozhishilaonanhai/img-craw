package com.craw.model;

import com.craw.common.Common;

import java.util.Objects;

public class BlibliUser {

    private String userId;
    private String name;
    private String sex;
    private String birthday;
    private String constellation;
    private String img;
    private String imgId;
    private int level;

    private String allData;

    private FansInfo fansInfo;

    public FansInfo getFansInfo() {
        return fansInfo;
    }

    public BlibliUser setFansInfo(FansInfo fansInfo) {
        this.fansInfo = fansInfo;
        return this;
    }

    public static class FansInfo {
        private int fansNum;
        private int attentionNum;

        public int getFansNum() {
            return fansNum;
        }

        public FansInfo setFansNum(int fansNum) {
            this.fansNum = fansNum;
            return this;
        }

        public int getAttentionNum() {
            return attentionNum;
        }

        public FansInfo setAttentionNum(int attentionNum) {
            this.attentionNum = attentionNum;
            return this;
        }
    }

    public String getAllData() {
        return allData;
    }

    public BlibliUser setAllData(String allData) {
        this.allData = allData;
        return this;
    }

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

    public BlibliUser initConstellation() {
        if (Objects.isNull(birthday)) {
            return this;
        }
        String[] arr = birthday.split("-");
        if (arr.length < 2) {
            return this;
        }
        this.constellation = Common.ConstellationUtils.getConstellation(Integer.parseInt(arr[arr.length - 2]), Integer.parseInt(arr[arr.length - 1]));
        return this;
    }

    public static void main(String[] args) {
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
}
