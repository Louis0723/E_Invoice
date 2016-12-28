package com.lursun.einvoice;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Administrator on 2016/12/4.
 */
public class ConstructionInfo {
    public static Handler alert, reserial ;
    public static Cipher cipher=null;
    public static void alert_init(final Context context){
        alert=new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(((String)msg.obj).equals("start")){
                    AlertDialog ad=new AlertDialog.Builder(context).setTitle("提示").setMessage("電子發票程式已啟動").setPositiveButton("確認",null).create();
                    ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    ad.show();
                }else {
                    super.handleMessage(msg);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("已上傳交易");
                    builder.setMessage((String) msg.obj);
                    builder.setPositiveButton("確認", null);
                    AlertDialog ad = builder.create();
                    ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    ad.show();
                }
            }
        };

    }
     public static void cipher(){
        try {
            String TOKEN_KEY = "5CDEF115B53F67F23D0414E771A1A2F8";
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(TOKEN_KEY.getBytes("utf-8"), "AES"), new IvParameterSpec(iv));
        } catch (Exception e) {
            e = e;
        }
    }
}
