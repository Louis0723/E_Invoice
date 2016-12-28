package com.lursun.einvoice;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import android.support.v7.app.AlertDialog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;

import java.net.Socket;
import java.net.URL;
import java.util.HashMap;

public class SetInvoiceActivity extends Activity {
    Handler alert = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AlertDialog.Builder builder  = new AlertDialog.Builder(SetInvoiceActivity.this);
            builder.setTitle("測試").setMessage((String) msg.obj).setPositiveButton("確認",null);
            Dialog dialog = builder.create();

            dialog.show();
        }

    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_pos_layout);

        SQLite sqLite = new SQLite(this);
        SQLiteDatabase db = sqLite.getReadableDatabase();
        Cursor sellerInfoC = db.rawQuery("SELECT * FROM sellerInfo", null);
        sellerInfoC.moveToFirst();
        ((EditText) findViewById(R.id.SELLERID)).setText(sellerInfoC.getString(sellerInfoC.getColumnIndex("SELLERID")));
        ((EditText) findViewById(R.id.InvoiceTitle)).setText(sellerInfoC.getString(sellerInfoC.getColumnIndex("Title")));
        ((EditText) findViewById(R.id.POSID)).setText(sellerInfoC.getString(sellerInfoC.getColumnIndex("POSID")));
        ((EditText) findViewById(R.id.POSSN)).setText(sellerInfoC.getString(sellerInfoC.getColumnIndex("POSSN")));
        ((EditText) findViewById(R.id.StoreID)).setText(sellerInfoC.getString(sellerInfoC.getColumnIndex("StoreID")));
        ((EditText) findViewById(R.id.Commodity)).setText(sellerInfoC.getString(sellerInfoC.getColumnIndex("Commodity")));
        ((EditText) findViewById(R.id.invoiceIP)).setText(sellerInfoC.getString(sellerInfoC.getColumnIndex("InvoiceIP")));
        ((EditText) findViewById(R.id.invoicePort)).setText(sellerInfoC.getString(sellerInfoC.getColumnIndex("InvoicePort")));
        ((EditText) findViewById(R.id.accountsIP)).setText(sellerInfoC.getString(sellerInfoC.getColumnIndex("AccountsIP")));
        ((EditText) findViewById(R.id.accountsPort)).setText(sellerInfoC.getString(sellerInfoC.getColumnIndex("AccountsPort")));
        if (sellerInfoC.getString(sellerInfoC.getColumnIndex("AccountsYN")).equals("YES")) {
            ((CheckBox) findViewById(R.id.accountsYN)).setChecked(true);
        } else {
            ((CheckBox) findViewById(R.id.accountsYN)).setChecked(false);
        }
        switch (sellerInfoC.getString(sellerInfoC.getColumnIndex("Machine"))) {
            case "BPT3":
                ((RadioButton) findViewById(R.id.M1)).setChecked(true);
                break;
            case "頂尖":
                ((RadioButton) findViewById(R.id.M2)).setChecked(true);
                break;
            case "商米":
                ((RadioButton) findViewById(R.id.M3)).setChecked(true);
                break;

        }
        ((EditText) findViewById(R.id.labelIP)).setText(sellerInfoC.getString(sellerInfoC.getColumnIndex("LabelIP")));
        ((EditText) findViewById(R.id.labelPort)).setText(sellerInfoC.getString(sellerInfoC.getColumnIndex("LabelPort")));
        findViewById(R.id.finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveClick(v);
                    Intent intent = new Intent();
                    intent.setClass(SetInvoiceActivity.this, InputActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e = e;
                }
            }
        });


    }

    public void saveClick(View view) {

        ContentValues cv = new ContentValues();
        cv.put("SELLERID", ((EditText) findViewById(R.id.SELLERID)).getText().toString());
        cv.put("Title", ((EditText) findViewById(R.id.InvoiceTitle)).getText().toString());
        cv.put("POSID", ((EditText) findViewById(R.id.POSID)).getText().toString());
        cv.put("POSSN", ((EditText) findViewById(R.id.POSSN)).getText().toString());
        cv.put("StoreID", ((EditText) findViewById(R.id.StoreID)).getText().toString());
        cv.put("Commodity", ((EditText) findViewById(R.id.Commodity)).getText().toString());
        cv.put("InvoiceIP", ((EditText) findViewById(R.id.invoiceIP)).getText().toString());
        cv.put("InvoicePort", ((EditText) findViewById(R.id.invoicePort)).getText().toString());
        cv.put("AccountsIP", ((EditText) findViewById(R.id.accountsIP)).getText().toString());
        cv.put("AccountsPort", ((EditText) findViewById(R.id.accountsPort)).getText().toString());
        cv.put("Lock", "Lock");
        cv.put("Machine", (((RadioButton) findViewById(((RadioGroup) findViewById(R.id.Printer)).getCheckedRadioButtonId())).getText().toString()));
        cv.put("AccountsYN", ((CheckBox) findViewById(R.id.accountsYN)).isChecked() ? "YES" : "NO");
        cv.put("labelIP", ((EditText) findViewById(R.id.labelIP)).getText().toString());
        cv.put("labelPort", ((EditText) findViewById(R.id.labelPort)).getText().toString());
        SQLite sqLite = new SQLite(this);
        SQLiteDatabase db = sqLite.getReadableDatabase();
        db.update("sellerInfo", cv, "", null);
    }
    public void cleardb(View view){
        AlertDialog ad=new AlertDialog.Builder(SetInvoiceActivity.this).setTitle("警告").setMessage("確定要清除資料庫嗎?").setIcon(android.R.drawable.ic_dialog_info).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SQLite sqLite = new SQLite(SetInvoiceActivity.this);
                SQLiteDatabase db = sqLite.getReadableDatabase();
                db.execSQL("Drop Table history");
                db.execSQL("Drop Table numberInfo");
                db.execSQL("Drop Table sellerInfo");
                sqLite.onCreate(db);
                Intent intent = new Intent();
                intent.setClass(SetInvoiceActivity.this, SetInvoiceActivity.class);
                startActivity(intent);
                finish();
            }
        }).setNegativeButton("取消", null).show();
    }
    public void updateClick(View view){
        Uri uri = Uri.parse("http://admin.joyspots.net/Download/einvoice.apk");
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
    public void getSN(View view){
        final String url="http://admin.joyspots.net/posapi/GetKey.aspx?shopid="+((EditText)findViewById(R.id.StoreID)).getText().toString()+"&posid="+((EditText)findViewById(R.id.POSID)).getText().toString();
        final Handler changeSN= new Handler(){
            @Override
            public void handleMessage(Message msg) {
                ((EditText)findViewById(R.id.POSSN)).setText((String)msg.obj);
            }
        };
        new Thread(){
            @Override
            public void run() {
                try{
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStreamReader isr=new InputStreamReader(con.getInputStream());
                        BufferedReader br=new BufferedReader(isr);
                        String s=br.readLine();
                        s=s.replaceAll("\\{\"error\":(0|1),\"msg\":\"|\"\\}","");
                        Message msg=new Message();
                        msg.obj=s;
                        changeSN.sendMessage(msg);
                    }
                }catch (Exception e){
                    e=e;
                }
            }
        }.start();
    }
    public void netTestClick(View view) {

        new Thread() {
            @Override
            public void run() {
                try {
                    InetAddress address;
                    testSystem t;


                    t = new testSystem();
                    t.start();
                    t.join();

                    Message msg = new Message();
                    msg.obj = t.value;
                    alert.sendMessage(msg);

                } catch (Exception e) {
                    Message msg = new Message();
                    msg.obj = e.getMessage();
                    alert.sendMessage(msg);
                }
            }
        }.start();
    }
    public void printClick(View view){
        new Thread(){
            @Override
            public void run() {
                byte[] n = "\n\n\n\n\n".getBytes();
                try {
                    Socket s = new Socket(((EditText) findViewById(R.id.accountsIP)).getText().toString(), Integer.parseInt(((EditText) findViewById(R.id.accountsPort)).getText().toString()));
                    OutputStream os = s.getOutputStream();
                    os.write(n);
                    os.close();
                    s.close();
                }catch (Exception e){
                    e=e;
                }
                try {

                    Socket s = new Socket(((EditText) findViewById(R.id.invoiceIP)).getText().toString(),  Integer.parseInt(((EditText) findViewById(R.id.invoicePort)).getText().toString()));
                    OutputStream os = s.getOutputStream();
                    os.write(n);
                    os.close();
                    s.close();
                }catch (Exception e){
                    e=e;
                }

            }
        }.start();
    }


    public class testSystem extends Thread {
        String value;
        HashMap<String, String> sendValue = new HashMap<String, String>();

        testSystem() {
            SQLite sqLite = new SQLite(getApplicationContext());
            SQLiteDatabase db = sqLite.getReadableDatabase();
            Cursor sellerInfoC = db.rawQuery("SELECT * FROM sellerInfo", null);
            sellerInfoC.moveToFirst();
            sendValue.put("SELLERID", sellerInfoC.getString(sellerInfoC.getColumnIndex("SELLERID")));
            sendValue.put("POSID", sellerInfoC.getString(sellerInfoC.getColumnIndex("POSID")));
            sendValue.put("POSSN", sellerInfoC.getString(sellerInfoC.getColumnIndex("POSSN")));
            sendValue.put("WebSite", "xml.551.com.tw");
        }

        @Override
        public void run() {
            SendData sd = new SendData(sendValue);
            try{
                HashMap<String, String> hm = sd.getSystime();
                value = hm.get("MESSAGE");
            }catch (Exception e)
            {
                e=e;
            }
        }
    }
}
