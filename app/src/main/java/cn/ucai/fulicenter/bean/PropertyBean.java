package cn.ucai.fulicenter.bean;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by ucai001 on 2016/3/1.
 */
public class PropertyBean implements Serializable {

    private int id;
    private int goodsId;
    private int colorId;
    private String colorName;
    private String colorCode;
    private String colorImg;
    private String colorUrl;
    /**
     * pid : 7677
     * imgId : 28296
     * imgUrl : 201509/goods_img/7677_P_1442391216432.png
     * thumbUrl : no_picture.gif
     */

    private AlbumBean[] albums;

    public void setId(int id) {
        this.id = id;
    }

    public void setGoodsId(int goodsId) {
        this.goodsId = goodsId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public void setColorImg(String colorImg) {
        this.colorImg = colorImg;
    }

    public void setColorUrl(String colorUrl) {
        this.colorUrl = colorUrl;
    }

    public void setAlbums(AlbumBean[] albums) {
        this.albums = albums;
    }

    public int getId() {
        return id;
    }

    public int getGoodsId() {
        return goodsId;
    }

    public int getColorId() {
        return colorId;
    }

    public String getColorName() {
        return colorName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getColorImg() {
        if(colorImg.indexOf("/")==0){
            return colorImg.substring(1);
        }
        return colorImg;
    }

    public String getColorUrl() {
        return colorUrl;
    }

    public AlbumBean[] getAlbums() {
        return albums;
    }

    @Override
    public String toString() {
        return "PropertyBean{" +
                "id=" + id +
                ", goodsId=" + goodsId +
                ", colorId=" + colorId +
                ", colorName='" + colorName + '\'' +
                ", colorCode='" + colorCode + '\'' +
                ", colorImg='" + colorImg + '\'' +
                ", colorUrl='" + colorUrl + '\'' +
                ", albums=" + Arrays.toString(albums) +
                '}';
    }
}
