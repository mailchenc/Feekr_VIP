package com.jiang.tvlauncher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jiang.tvlauncher.entity.Const;
import com.jiang.tvlauncher.servlet.VIPCallBack_Servlet;
import com.jiang.tvlauncher.utils.LogUtil;
import com.ktcp.video.thirdagent.JsonUtils;
import com.ktcp.video.thirdagent.ThirdPartyAgent;
import com.ktcp.video.thirdagent.inter.IThirdPartyAgentListener;
import com.ktcp.video.thirdparty.IThirdPartyAuthCallback;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * @author: v_shlicheng
 * @date: 2018/4/26.
 * @Email:
 * @Phone:
 * TODO: 云视听动作广播监听
 */

public class ThirdPartyReceiver extends BroadcastReceiver implements IThirdPartyAgentListener {
    private static final String TAG = "ThirdpartyReceiver";
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        LogUtil.e(TAG, "处理登录");

        //处理登录
        if (ThirdPartyAgent.ACTION_SERVER_AGGENT.equals(intent.getAction())) {
            String channel = intent.getStringExtra("channel");
            String data = intent.getStringExtra("data");
            Log.i(TAG, "channel=" + channel + ",data=" + data);

//            Toast.makeText(MyAppliaction.context, "channel=" + channel + ",data=" + data, Toast.LENGTH_LONG).show();

            JSONObject dataObj = JsonUtils.getJsonObj(data);

            //启动APP
            if (dataObj.optInt("type") == ThirdPartyAgent.TYPE_LOGIN) {
                ThirdPartyAgent.getInstance().setOnThirdPartyAgentListener(this);
                ThirdPartyAgent.getInstance().doAuthLogin(channel);

            } else if (dataObj.optInt("type") == ThirdPartyAgent.TYPE_NOTICE) {

                //2=账户登录回调 3=账号退出回调  4=APP退出
                int eveintId = dataObj.optInt("eventId");
                String extraJson = dataObj.optString("extra");
                int code = -1;
                String msg = "";
                if (extraJson != null && extraJson.length() > 0) {
                    JSONObject extraObj = JsonUtils.getJsonObj(extraJson);
                    code = extraObj.optInt("code");
                    msg = extraObj.optString("msg");
                }

                // 2 账户登录回调 3 退出登录 4 APP退出
                LogUtil.e(TAG, "状态码：" + eveintId);
                //Toast.makeText(context, "状态码："+eveintId, Toast.LENGTH_SHORT).show();
                VIPCallBack_Servlet.TencentVip vip = new VIPCallBack_Servlet.TencentVip();

                switch (eveintId) {
                    case 2:         //账户登录回调
                        vip.setCode(String.valueOf(code));
                        vip.setMsg(msg);
                        vip.setEventId(String.valueOf(eveintId));
                        new VIPCallBack_Servlet().execute(vip);
                        break;
                    case 3:         //退出登录
                        vip.setCode(String.valueOf(code));
                        vip.setMsg(msg);
                        vip.setEventId(String.valueOf(eveintId));
                        new VIPCallBack_Servlet().execute(vip);
                        break;
                    case 4:         //APP退出
                        vip.setCode(String.valueOf(code));
                        vip.setMsg(msg);
                        vip.setEventId(String.valueOf(eveintId));
                        new VIPCallBack_Servlet().execute(vip);
                        break;
                }

            }

            //处理支付订单
            else if (dataObj.optInt("type") == ThirdPartyAgent.TYPE_ORDER) {
                long vuid = dataObj.optLong("vuid", -1);
                if (vuid > 0) {
                    String produceId = dataObj.optString("produeceId");
                    String vuSession = dataObj.optString("vuSession");
                    ThirdPartyAgent.getInstance().setOnThirdPartyAgentListener(this);
                    ThirdPartyAgent.getInstance().doOrder(vuid, produceId, vuSession);
                }
            }
        }
    }

    @Override
    public void getAccount(String channel, final IThirdPartyAuthCallback thirdPartyAuthCallback) {
        //fixme 由厂商实现的接口 成功获取到接口vuid,vtoken,accessToken必须通过data回调给视频客户端，需要视频处理的错误定义好提示文案放errTip中
        Toast.makeText(context, "正在为您提供会员服务", Toast.LENGTH_LONG).show();

        if (TextUtils.isEmpty(Const.PARAMS)) {
            Toast.makeText(context, "无法提供会员服务", Toast.LENGTH_LONG).show();
        } else {

            try {
                thirdPartyAuthCallback.authInfo(0, "get vuid error", Const.PARAMS); //data需要返回vuid,vtoken,accesssToken
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e(TAG, e.getMessage());
            }
        }

    }

    @Override
    public void getOrder(long vuid, String produceId, String vusession, final IThirdPartyAuthCallback thirdPartyAuthCallback) {
        LogUtil.e(TAG, "getOrder由厂商实现的接口");
        //fixme 由厂商实现的接口 成功下单回调{"orderId":"","errTip":""}给视频客户端，需要视频处理的错误定义好提示文案放errTip中
        final HashMap<String, Object> params = new HashMap<>();
        params.put("orderId", "");
        params.put("errTip", "");
        int status = 0;// 0 获取数据成功 非0失败
        String msg = "success";
        try {
            thirdPartyAuthCallback.orderResult(status, msg, params.toString());
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, e.getMessage());
        }
    }

}

