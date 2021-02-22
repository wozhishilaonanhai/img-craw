package com.craw.model;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User {
    private static final Pattern WB_USER_ID_REX = Pattern.compile("/u/(\\d+)");

    private String wbUserId;
    private String img;
    private String imgId;
    private String name;
    private String sex;
    private String site;
    private String birthday;
    private String constellation;
    private List<String> tags;
    private String detailsUrl;

    public String getWbUserId() {
        return wbUserId;
    }

    public void setWbUserId(String wbUserId) {
        this.wbUserId = wbUserId;
    }

    public void initWbUserId() {
        if (Objects.isNull(detailsUrl)) {
            return;
        }
        Matcher matcher = WB_USER_ID_REX.matcher(detailsUrl);
        if (matcher.find()) {
            this.wbUserId = matcher.group(1);
        }
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getImgId() {
        return imgId;
    }

    public void setImgId(String imgId) {
        this.imgId = imgId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getConstellation() {
        return constellation;
    }

    public void setConstellation(String constellation) {
        this.constellation = constellation;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }

    public void setDetailsUrl(String detailsUrl) {
        this.detailsUrl = detailsUrl;
    }
}