package lursun.einvoice;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SQLite sqLite = new SQLite(this);
        SQLiteDatabase db =sqLite.getReadableDatabase();
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
