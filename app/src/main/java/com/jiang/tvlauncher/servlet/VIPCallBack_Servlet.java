package com.jiang.tvlauncher.servlet;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.jiang.tvlauncher.entity.BaseEntity;
import com.jiang.tvlauncher.entity.Const;
import com.jiang.tvlauncher.utils.HttpUtil;
import com.jiang.tvlauncher.utils.LogUtil;
import com.jiang.tvlauncher.utils.ToolUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: jiangyao
 * @date: 2018/5/18
 * @Email: www.fangmu@qq.com
 * @Phone: 186 6120 1018
 * TODO: 会员登录返回
 */
public class VIPCallBack_Servlet extends AsyncTask<VIPCallBack_Servlet.TencentVip, Integer, BaseEntity> {
    private static final String TAG = "VIPCallBack_Servlet";

    String eveintId = "";

    @Override
    protected BaseEntity doInBackground(TencentVip... tencentVips) {

        TencentVip vip = tencentVips[0];
        Map map = new HashMap();
        map.put("vuid", Const.ktcp_vuid);               //腾讯视频VIP账户
        map.put("vtoken", Const.ktcp_vtoken);           //腾讯视频播放凭证
        map.put("eventId", vip.getEventId());           //事件ID（腾讯视频返回值）
        map.put("terminalId", ToolUtils.getMacAddress());   //终端ID
        map.put("code", vip.getCode());                 //腾讯视频登录返回的code码
        map.put("msg", vip.getMsg());                   //登录结果描述

        eveintId = vip.getEventId();

        String res = "";

        try {

            res = HttpUtil.doPost(Const.URL + "tencent/tencentVideoController/tencentNoticeCallBackByAgent.do", map);
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
        }

        BaseEntity entity;
        if (TextUtils.isEmpty(res)) {
            entity = new BaseEntity();
            entity.setErrorcode(-1);
            entity.setErrormsg("连接服务器失败");
        } else {
            try {
                entity = new Gson().fromJson(res, BaseEntity.class);
            } catch (Exception e) {
                entity = new BaseEntity();
                entity.setErrormsg("数据解析失败");
                entity.setErrorcode(-2);
                LogUtil.e(TAG, e.getMessage());
            }
        }
        return entity;
    }

    @Override
    protected void onPostExecute(BaseEntity entity) {
        super.onPostExecute(entity);
        if (eveintId != null && eveintId.equals("4")) {
            EventBus.getDefault().post("退出");
        }

        if (entity.getErrorcode() == 1000) {

        }
    }

    public static class TencentVip {
        private String vuid;
        private String vtoken;
        private String serialNum;
        private String code;
        private String msg;
        private String eventId;

        public String getVuid() {
            return vuid;
        }

        public void setVuid(String vuid) {
            this.vuid = vuid;
        }

        public String getVtoken() {
            return vtoken;
        }

        public void setVtoken(String vtoken) {
            this.vtoken = vtoken;
        }

        public String getSerialNum() {
            return serialNum;
        }

        public void setSerialNum(String serialNum) {
            this.serialNum = serialNum;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getEventId() {
            return eventId;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
        }
    }
}
