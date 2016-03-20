package cn.ucai.fulicenter.bean;

import java.io.Serializable;

public class BillBean implements Serializable{

    private String receiverName;
    private String mobile;
    private String province;
    private String address;
    public BillBean(String receiverName, String mobile, String province,
            String address) {
        super();
        this.receiverName = receiverName;
        this.mobile = mobile;
        this.province = province;
        this.address = address;
    }
    public String getReceiverName() {
        return receiverName;
    }
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
    public String getMobile() {
        return mobile;
    }
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    public String getProvince() {
        return province;
    }
    public void setProvince(String province) {
        this.province = province;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    @Override
    public String toString() {
        return "BillBean [receiverName=" + receiverName + ", mobile=" + mobile
                + ", province=" + province + ", address=" + address + "]";
    }
    
}
