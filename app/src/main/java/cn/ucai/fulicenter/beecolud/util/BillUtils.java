/**
 * BillUtils.java
 * <p/>
 * Created by xuanzhui on 2015/10/31.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.ucai.fulicenter.beecolud.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.ucai.fulicenter.I;

public class BillUtils {
    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINA);

    public static String genBillNum() {
        return simpleDateFormat.format(new Date());
    }

    public static String getOrderBeneficiary(String beneficiary){
        return "收款方： "+beneficiary;
    }

    public static String getAmountType(String type){
        if(type==null){
            return I.CURRENCY_TYPE_CNY;
        }else{
            return I.CURRENCY_TYPE_USD;
        }
    }

    public static String getBillTotalFee(String billTotalFee){
        java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");
        double fee = Double.parseDouble(billTotalFee);
        return ""+df.format((fee*0.01));
    }
}
