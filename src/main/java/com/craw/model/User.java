package com.craw.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
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

    public User initWbUserId() {
        if (Objects.nonNull(detailsUrl)) {
            Matcher matcher = WB_USER_ID_REX.matcher(detailsUrl);
            if (matcher.find()) {
                this.wbUserId = matcher.group(1);
            }
        }
        return this;
    }

    public String getImg() {
        return img;
    }

    public User setImg(String img) {
        this.img = img;
        return this;
    }

    public String getImgId() {
        return imgId;
    }

    public User setImgId(String imgId) {
        this.imgId = imgId;
        return this;
    }

    public User initImgId() {
        this.imgId = UUID.randomUUID().toString().replace("-", "");
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getSex() {
        return sex;
    }

    public User setSex(String sex) {
        this.sex = sex;
        return this;
    }

    public String getSite() {
        return site;
    }

    public User setSite(String site) {
        this.site = site;
        return this;
    }

    public String getBirthday() {
        return birthday;
    }

    public User setBirthday(String birthday) {
        this.birthday = birthday;
        return this;
    }

    public String getConstellation() {
        return constellation;
    }

    public User setConstellation(String constellation) {
        this.constellation = constellation;
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public User setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }

    public User setDetailsUrl(String detailsUrl) {
        this.detailsUrl = detailsUrl;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return wbUserId.equals(user.wbUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wbUserId);
    }

    @Override
    public String toString() {
        return "User{" +
                "wbUserId='" + wbUserId + '\'' +
                ", img='" + img + '\'' +
                ", imgId='" + imgId + '\'' +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", site='" + site + '\'' +
                ", birthday='" + birthday + '\'' +
                ", constellation='" + constellation + '\'' +
                ", tags=" + tags +
                ", detailsUrl='" + detailsUrl + '\'' +
                '}';
    }
}