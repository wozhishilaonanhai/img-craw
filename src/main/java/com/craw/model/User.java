package com.craw.model;

import com.craw.common.Common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User implements Cloneable {


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
    private int fansNum;
    private int attentionNum;
    private int wbNum;

    public int getAttentionNum() {
        return attentionNum;
    }

    public User setAttentionNum(int attentionNum) {
        this.attentionNum = attentionNum;
        return this;
    }

    public int getWbNum() {
        return wbNum;
    }

    public User setWbNum(int wbNum) {
        this.wbNum = wbNum;
        return this;
    }

    public int getFansNum() {
        return fansNum;
    }

    public User setFansNum(int fansNum) {
        this.fansNum = fansNum;
        return this;
    }

    public String getWbUserId() {
        return wbUserId;
    }

    public void setWbUserId(String wbUserId) {
        this.wbUserId = wbUserId;
    }

    public User initWbUserId() {
        this.wbUserId = _getWbUserId();
        return this;
    }

    private String _getWbUserId() {
        if (Objects.nonNull(detailsUrl)) {
            Matcher matcher = WB_USER_ID_REX.matcher(detailsUrl);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
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
        this.imgId = this.wbUserId;
        if (Objects.isNull(this.imgId)) {
            this.imgId = _getWbUserId();
        }
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

    public User initConstellation() {
        if (Objects.isNull(birthday)) {
            return this;
        }
        if (birthday.contains("座")) {
            this.constellation = birthday;
            return this;
        }
        LocalDate birthdayDate = LocalDate.parse(birthday, DateTimeFormatter.ofPattern("yyyy年M月d日"));
        this.constellation = Common.ConstellationUtils.getConstellation(birthdayDate.getMonthValue(), birthdayDate.getDayOfMonth());
        return this;
    }

    public List<String> getTags() {
        if (Objects.isNull(tags)) {
            return Collections.emptyList();
        }
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
    public User clone() {
        User res = new User();
        res.birthday = birthday; // 生日
        res.constellation = constellation; // 星座
        res.detailsUrl = detailsUrl; // 详情url
        res.img = img; // 图片地址
        res.imgId = imgId; // 图片id
        res.name = name; // 昵称
        res.sex = sex; // 性别
        res.site = site; // 所在地
        res.wbUserId = wbUserId; // 微博用户id
        res.tags = Objects.nonNull(tags) ? new ArrayList<>(tags) : null; // 标签
        return res;
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