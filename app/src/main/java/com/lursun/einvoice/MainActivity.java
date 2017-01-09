package com.lursun.einvoice;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SQLite sqLite = new SQLite(this);
        SQLiteDatabase db =sqLite.getReadableDatabase();
        try {
            InputStream in = new FileInputStream(new File(db.getPath()));
            File myFilePath=new File(Environment.getExternalStorageDirectory()+"/EInvoice");
            if (!myFilePath.exists()) {
                myFilePath.mkdir();
            }
            File file=new File(Environment.getExternalStorageDirectory()+"/EInvoice/EInvoice.db");
            file.delete();
            FileOutputStream fw=new FileOutputStream(file);
            byte date[]=new byte[in.available()];
            in.read(date,0,in.available());
            fw.write(date);
            fw.close();
            in.close();

        }catch (IOException e){}
        Cursor sellerInfoC=db.rawQuery("SELECT * FROM sellerInfo",null);
        sellerInfoC.moveToFirst();
        if(sellerInfoC.getString(sellerInfoC.getColumnIndex("Lock")).equals("Lock")){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, InputActivity.class);
            startActivity(intent);
            finish();
        }
        else if(sellerInfoC.getString(sellerInfoC.getColumnIndex("Lock")).equals("Open")){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SetInvoiceActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
