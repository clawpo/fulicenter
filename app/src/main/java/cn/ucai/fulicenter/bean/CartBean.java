package cn.ucai.fulicenter.bean;

import java.io.Serializable;

/**
 * Created by ucai001 on 2016/3/1.
 */
public class CartBean implements Serializable {

    /**
     * id : 7672
     * userName : 7672
     * goodsId : 7672
     * count : 2
     * checked : true
     */

    private int id;
    private String userName;
    private int goodsId;
    private GoodDetailsBean goods;
    private int count;
    private boolean isChecked;

    public void setId(int id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setGoodsId(int goodsId) {
        this.goodsId = goodsId;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public GoodDetailsBean getGoods() {
        return goods;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public int getGoodsId() {
        return goodsId;
    }

    public int getCount() {
        return count;
    }

    public void setGoods(GoodDetailsBean goods) {
        this.goods = goods;
    }

    public boolean isIsChecked() {
        return isChecked;
    }

    public CartBean() {
    }

    public CartBean(int id, String userName, int goodsId, int count, boolean isChecked) {
        this.id = id;
        this.userName = userName;
        this.goodsId = goodsId;
        this.count = count;
        this.isChecked = isChecked;
    }
}
