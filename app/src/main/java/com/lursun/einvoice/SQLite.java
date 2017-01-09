package com.lursun.einvoice;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by admin on 2016/9/5.
 */
public class SQLite extends SQLiteOpenHelper {
    public static String website =null;
    private final static int _DBVersion = 1;
    private final static String _DBName = "Invoice.db";
    public SQLite(Context context) {
        super(context, _DBName, null, _DBVersion);
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery("Select WebSite From sellerInfo",null);
        c.moveToFirst();
        website=c.getString(0);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL = "CREATE TABLE IF NOT EXISTS sellerInfo( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "Title TEXT,"+ //發票抬頭
                "WebSite TEXT,"+ //網址
                "SELLERID TEXT,"+ //統編
                "POSID TEXT," + //pos03
                "POSSN TEXT," + //pos金鑰
                "StoreID TEXT,"+ //店號
                "Commodity TEXT,"+ //品項
                "InvoiceIP TEXT,"+ //發票機IP
                "InvoicePort TEXT,"+ //PORT
                "AccountsIP TEXT,"+ //帳務聯IP
                "AccountsPort TEXT,"+ //PORT
                "LabelIP TEXT,"+
                "LabelPort TEXT,"+
                "Machine TEXT,"+
                "AccountsYN TEXT,"+
                "Lock TEXT,"+ //畫面鎖
                "QueueQRcode TEXT"+
                ");";
        db.execSQL(SQL);
        SQL = "INSERT INTO sellerInfo(Title,WebSite,SELLERID,POSID,POSSN,StoreID,Commodity,InvoiceIP,InvoicePort,AccountsIP,AccountsPort,LabelIP,LabelPort,Machine,AccountsYN,Lock,QueueQRcode)" +
                " VALUES('智慧數碼','xmltest.551.com.tw','','pos03','','le00000115','餐費','192.168.123.100','9100','192.168.123.100','9100','192.168.123.100','9100','BTP3','YES','Open','NO')";
        db.execSQL(SQL);
        SQL = "CREATE TABLE IF NOT EXISTS numberInfo( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TAXMONTH TEXT, " +
                "INVOICEHEADER TEXT," +
                "INVOICESTART TEXT,"+
                "INVOICEEND TEXT,"+
                "REPLY TEXT,"+
                "MESSAGE TEXT"+
                ");";
        db.execSQL(SQL);

        SQL = "CREATE TABLE IF NOT EXISTS history( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "TAXMONTH TEXT ," +
                "INVOICENUMBER TEXT, " +
                "BUYERID TEXT, " +
                "SELLERID TEXT, " +
                "RANDOM TEXT, "+
                "Commodity TEXT, " +
                "Type TEXT, " +
                "No TEXT, " +
                "Serial TEXT ," +
                "Details TEXT ," +
                "LOVEYN TEXT, " +
                "LOVENUMBER TEXT, " +
                "AMOUNT TEXT, " +
                "VehicleNumber TEXT, " +
                "PRINTYN TEXT, " +
                "DAY TEXT, " +
                "TIME TEXT, " +
                "MakeTime TEXT, " +
                "Submit1 TEXT, " +
                "REPLY1 TEXT, " +
                "MESSAGE1 TEXT, " +
                "ERROR_CODE1 TEXT ," +
                "CANCEL_DATE TEXT, " +
                "CANCEL_TIME TEXT, " +
                "CANCEL_REASON TEXT, " +
                "Submit2 TEXT, " +
                "REPLY2 TEXT, " +
                "MESSAGE2 TEXT, " +
                "ERROR_CODE2 TEXT ," +
                "TDC TEXT" +
                ");";
        db.execSQL(SQL);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }


}
