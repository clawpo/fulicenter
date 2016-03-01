package cn.ucai.fulicenter.bean;

import java.io.Serializable;

/**
 * Created by ucai001 on 2016/3/1.
 */
public class CategoryGroupBean implements Serializable {

    /**
     * id : 334
     * name : 配饰
     * imageUrl : muying/Jewelry.png
     */

    private int id;
    private String name;
    private String imageUrl;

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String toString() {
        return "CategoryGroupBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
