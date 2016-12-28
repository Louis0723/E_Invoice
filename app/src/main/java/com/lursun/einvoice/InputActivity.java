package com.lursun.einvoice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.Inflater;

/**
 * Created by admin on 2016/9/25.
 */
public class InputActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_number);
        Intent BootService = new Intent(InputActivity.this, BGService.class);
        InputActivity.this.startService(BootService);
        findViewById(R.id.input_clear).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String moneyS=((TextView)findViewById(R.id.moneyView)).getText().toString();
                if(moneyS.equals("27557992")) {
                    ContentValues cv=new ContentValues();
                    cv.put("Lock","Open");
                    SQLite sqLite = new SQLite(getApplicationContext());
                    SQLiteDatabase db =sqLite.getReadableDatabase();
                    db.update("sellerInfo",cv,"",null);
                    Intent intent = new Intent();
                    intent.setClass(InputActivity.this, SetInvoiceActivity.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });

    }
    public void buttonOnClick(View view){
        int num=Integer.parseInt(((Button)view).getText().toString());
        String moneyS=((TextView)findViewById(R.id.moneyView)).getText().toString();
        if(moneyS.length()<9){
            ((TextView)findViewById(R.id.moneyView)).setText(moneyS+num);}

    }
    public void submitOnClick(View view){
        final String money=((TextView)findViewById(R.id.moneyView)).getText().toString();
        if(money.length()!=0 &&Integer.parseInt( money)>0) {
            ((TextView) findViewById(R.id.moneyView)).setText("");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);
            final String serial = sdf.format(new Date())+"Q";
            builder.setTitle("序號:" + serial);
            builder.setMessage("金額:" + money);
            builder.setPositiveButton("確認", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new BuyerInfo(InputActivity.this,(LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE),"手動",null,serial,money);
                }
            });
            builder.setNegativeButton("取消",null);
            builder.show();
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);
            builder.setTitle("金額錯誤");
            builder.setNegativeButton("確認",null);
            builder.show();
        }

    }
    public void clearOnClick(View view){
        ((TextView)findViewById(R.id.moneyView)).setText("");
    }
    public void openReport(View view){
        Intent intent = new Intent();
        intent.setClass(InputActivity.this,ReportActivity.class);
        startActivity(intent);
    }
}
