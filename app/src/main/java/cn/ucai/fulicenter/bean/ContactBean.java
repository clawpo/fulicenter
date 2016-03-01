package cn.ucai.fulicenter.bean;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by clawpo on 16/2/10.
 */
public class ContactBean implements Serializable {

    /**
     * result : ok
     * myuid : 1001
     * cuid : 1002
     */

    private String result;
    private int myuid;
    private int cuid;

    public void setResult(String result) {
        this.result = result;
    }

    public void setMyuid(int myuid) {
        this.myuid = myuid;
    }

    public void setCuid(int cuid) {
        this.cuid = cuid;
    }

    public String getResult() {
        return result;
    }

    public int getMyuid() {
        return myuid;
    }

    public int getCuid() {
        return cuid;
    }

    public ContactBean() {
    }

    public ContactBean(String result, int myuid, int cuid) {
        this.result = result;
        this.myuid = myuid;
        this.cuid = cuid;
    }

    @Override
    public String toString() {
        return "ContactBean{" +
                "result='" + result + '\'' +
                ", myuid=" + myuid +
                ", cuid=" + cuid +
                '}';
    }
}
