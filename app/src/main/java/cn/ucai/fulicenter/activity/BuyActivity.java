package cn.ucai.fulicenter.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.pingplusplus.android.PingppLog;
import com.pingplusplus.libone.PayActivity;
import com.pingplusplus.model.Charge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.BillBean;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.bean.UserBean;
import cn.ucai.fulicenter.utils.DateUtils;
import cn.ucai.fulicenter.utils.NetUtil;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by clawpo on 16/3/20.
 */
public class BuyActivity  extends BaseActivity {
    EditText metReceivePersonalName;
    EditText metMobile;
    EditText metAddress;
    Spinner mspinArea;

    BuyActivity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);
        mContext=this;
        initView();
        setListener();
    }

    private void setListener() {
        setReturnClickListener();
        setBuyClickListener();
    }

    private void setBuyClickListener() {
        findViewById(R.id.btnBuy).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UserBean user = FuLiCenterApplication.getInstance().getUserBean();
                if(user==null){
                    Utils.showToast(mContext,"请先登录",Toast.LENGTH_SHORT);
                    return;
                }
                BillBean billBean=null;
                try {
                    billBean=validateEmpty();
                } catch (Exception e) {
                    String message = e.getMessage();
                    if("收货人姓名不能为空".equals(message)){
                        metReceivePersonalName.setError(message);
                        metReceivePersonalName.requestFocus();
                    }else if("手机号码不能为空".equals(message)){
                        metMobile.setError(message);
                        metMobile.requestFocus();
                    }else if("手机号码格式错误".equals(message)){
                        metMobile.setError(message);
                        metMobile.requestFocus();
                    }else if("收货地区不能为空".equals(message)){
                        Utils.showToast(mContext, message, Toast.LENGTH_SHORT);
                    }else if("街道地址不能为空".equals(message)){
                        metAddress.setError(message);
                        metAddress.requestFocus();
                    }
                    return ;
                }
                //设置支付的日期(字符串类型)
                String orderNo = DateUtils.date2String(new Date(), "yyyyMMddhhmmss");
                /** bills：代表账单的json数组*/
                JSONArray bills=new JSONArray();
                ArrayList<CartBean> cartList = FuLiCenterApplication.getInstance().getCartList();
                double amount=0;
                StringBuilder body=new StringBuilder();
                for (CartBean cart : cartList) {
                    if(cart.isChecked()){
                        String strPrice = cart.getGoods().getCurrencyPrice();
                        strPrice=strPrice.substring(1);
                        double price=Double.parseDouble(strPrice);
                        amount+=cart.getCount()*price*100;
                        GoodDetailsBean goods = cart.getGoods();
                        bills.put(goods.getGoodsName()+"x"+cart.getCount());
                        body.append(goods.getGoodsName()).append(",");
                    }
                }
                body.deleteCharAt(body.length()-1);

                //存放订单中的附加信息，包括订单的标题，订单中商品清单
                JSONObject extras=new JSONObject();

                //将附加信息、显示、支付日期、支付金额存放在订单bill中
                JSONObject bill=new JSONObject();
                String nick = FuLiCenterApplication.getInstance().getUserBean().getNick();
                try {
                    extras.put("subject", nick+"的订单");
                    extras.put("body", body.toString());
                    extras.put("receive_name", billBean.getReceiverName());
                    extras.put("mobile", billBean.getMobile());
                    extras.put("provoice", billBean.getProvince());
                    extras.put("address", billBean.getAddress());

                    //订单中用于显示的内容
                    JSONObject displayItem=new JSONObject();
                    displayItem.put("name", "商品");
                    displayItem.put("contents", bills);
                    //将显示内容存放在json数组中
                    JSONArray display=new JSONArray();
                    display.put(displayItem);

                    bill.put("display", display);
                    bill.put("extras", extras);
                    bill.put("order_no",orderNo);
//                    bill.put("amount", amount);
                    bill.put("amount", 10000);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //设置支付方式
                PayActivity.SHOW_CHANNEL_ALIPAY=true;
                PayActivity.SHOW_CHANNEL_BFB=true;
                PayActivity.SHOW_CHANNEL_UPMP=true;
                PayActivity.SHOW_CHANNEL_WECHAT=true;

                //设置支付用的日志开关
                PingppLog.DEBUG=true;
                //调用壹收款进行支付
                PayActivity.CallPayActivity(BuyActivity.this, bill.toString(), I.PAY_URL);

            }

            /**
             * view非空验证
             * @throws Exception
             */
            private BillBean validateEmpty() throws Exception{
                String receiveName=metReceivePersonalName.getText().toString();
                if(TextUtils.isEmpty(receiveName)){
                    throw new Exception("收货人姓名不能为空");
                }
                String mobile=metMobile.getText().toString();
                if(TextUtils.isEmpty(mobile)){
                    throw new Exception("手机号码不能为空");
                }
                if(!mobile.matches("[\\d]{11}")){
                    throw new Exception("手机号码格式错误");
                }
                String area=mspinArea.getSelectedItem().toString();
                if(TextUtils.isEmpty(area)){
                    throw new Exception("收货地区不能为空");
                }
                String address=metAddress.getText().toString();
                if(TextUtils.isEmpty(address)){
                    throw new Exception("街道地址不能为空");
                }
                BillBean bill=new BillBean(receiveName, mobile, area, address);
                return bill;
            }
        });
    }

    private void setReturnClickListener() {
        findViewById(R.id.ivReturn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        metAddress=getViewById(R.id.etAddress);
        mspinArea=getViewById(R.id.spinArea);
        metMobile=getViewById(R.id.etMobile);
        metReceivePersonalName=getViewById(R.id.etReceiverName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayActivity.PAYACTIVITY_REQUEST_CODE) {
            if (resultCode == PayActivity.PAYACTIVITY_RESULT_CODE) {
/*                Toast.makeText(
                        this,
                        data.getExtras().getString("result") + "  "
                                + data.getExtras().getInt("code"),
                        Toast.LENGTH_LONG).show(); */
                Log.i("main", data.getExtras().getString("result") + "  "
                        + data.getExtras().getInt("code"));
                //获取应用服务器返回的Charge对象
                new GetChargeResultTask().execute();
            }
        }
    }

    /**
     * 获取应用服务器返回的Charge
     * @author yao
     *
     */
    class GetChargeResultTask extends AsyncTask<Void, Void, Charge> {
        @Override
        protected Charge doInBackground(Void... paramList) {
            HashMap<String,Object> charege = NetUtil.findCharege();

            return null;
        }
    }
}