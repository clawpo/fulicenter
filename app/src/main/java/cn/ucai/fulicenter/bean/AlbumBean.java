package cn.ucai.fulicenter.bean;

import java.io.Serializable;

/**
 * Created by ucai001 on 2016/3/1.
 */
public class AlbumBean implements Serializable {

    private int pid;
    private int imgId;
    private String imgUrl;
    private String thumbUrl;

    public void setPid(int pid) {
        this.pid = pid;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public int getPid() {
        return pid;
    }

    public int getImgId() {
        return imgId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    @Override
    public String toString() {
        return "AlbumBean{" +
                "pid=" + pid +
                ", imgId=" + imgId +
                ", imgUrl='" + imgUrl + '\'' +
                ", thumbUrl='" + thumbUrl + '\'' +
                '}';
    }
}
