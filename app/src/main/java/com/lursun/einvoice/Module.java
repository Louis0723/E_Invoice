package com.lursun.einvoice;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.sunmi.impl.V1Printer;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2016/12/5.
 */
public class Module {

    public synchronized static void saveTransactionToDB(Context context,String Money,String BUYERID,String VehicleNumber,String LoveNumber,String Serial,String No, boolean details,String type){
        SimpleDateFormat sdf_yyymm1=new SimpleDateFormat("yyyy");
        SimpleDateFormat sdf_yyymm2=new SimpleDateFormat("MM");
        String yyy=""+(Integer.parseInt( sdf_yyymm1.format(new Date()) )-1911);
        int MMi=Integer.parseInt( sdf_yyymm2.format(new Date()) );
        String MMs=String.format("%02d",MMi%2==0?MMi:MMi+1);
        Money=Integer.parseInt(Money)+"";
        SQLite sqLite = new SQLite(context);
        SQLiteDatabase db = sqLite.getReadableDatabase();
        Cursor c=db.rawQuery("Select (COUNT(INVOICENUMBER)-COUNT(Random)) From history Where TAXMONTH='"+yyy+MMs+"'  AND AMOUNT IS NULL AND  Serial IS NULL AND LOVENUMBER IS NULL AND VehicleNumber IS NULL AND Type IS NULL AND RANDOM IS NULL  AND AMOUNT  IS NULL AND MakeTime IS NULL Limit 1",null);
        c.moveToFirst();
        String SQL="";
        if(c.getInt(0)>0){
            SQL="Update history SET 'RANDOM'=substr('0000'||Random(),-4, 4),AMOUNT='"+Money+"',BUYERID='"+BUYERID+"',VehicleNumber='"+VehicleNumber+"',LOVENUMBER='"+LoveNumber+"',Serial='"+Serial+"',No='"+No+"',Type='"+type+"',Details='"+(details?"YES":"NO")+"' Where  TAXMONTH='"+yyy+MMs+"'  AND    INVOICENUMBER=(Select INVOICENUMBER From history Where Serial IS NULL AND LOVENUMBER IS NULL AND VehicleNumber IS NULL AND Type IS NULL AND RANDOM IS NULL  AND AMOUNT  IS NULL AND MakeTime IS NULL limit 1 )";
            db.execSQL(SQL);
        }else {

            getNumberFromMongLi(context);
            Cursor c2=db.rawQuery("Select (COUNT(INVOICENUMBER)-COUNT(Random)) From history Where TAXMONTH='"+yyy+MMs+"'  AND  Serial IS NULL AND LOVENUMBER IS NULL AND VehicleNumber IS NULL AND Type IS NULL AND RANDOM IS NULL  AND AMOUNT  IS NULL AND MakeTime IS NULL Limit 1",null);
            c2.moveToFirst();
            if(c2.getInt(0)>0){

                db.execSQL(SQL);
            }else {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("警告");
                builder.setMessage("無發票號");
                builder.setPositiveButton("確認", null);
                AlertDialog ad = builder.create();
                ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                ad.show();
            }
        }
    };
    public synchronized static String getNumberFromMongLi(final Context context){
        Thread getNumberFromMongLi=new Thread("getNumberFromMongLi"){
            public void run() {

                    try{
                        SimpleDateFormat sdf_yyymm1=new SimpleDateFormat("yyyy");
                        SimpleDateFormat sdf_yyymm2=new SimpleDateFormat("MM");
                        String yyy=""+(Integer.parseInt( sdf_yyymm1.format(new Date()) )-1911);
                        int MMi=Integer.parseInt( sdf_yyymm2.format(new Date()) );
                        String MMs=String.format("%02d",MMi%2==0?MMi:MMi+1);
                        SQLite sqLite = new SQLite(context);
                        final SQLiteDatabase db = sqLite.getReadableDatabase();
                        Cursor c= db.rawQuery("SELECT  (COUNT(INVOICENUMBER)-COUNT(Random)) AS surplus  FROM history Where  TAXMONTH='"+yyy+MMs+"'",null);
                        c.moveToFirst();
                        if(c.getInt(0)<101){
                            final HashMap<String,String> sendValue=new HashMap<String, String>();
                            Cursor sellerInfoC = db.rawQuery("SELECT * FROM sellerInfo", null);
                            sellerInfoC.moveToFirst();
                            sendValue.put("SELLERID",sellerInfoC.getString(sellerInfoC.getColumnIndex("SELLERID")));
                            sendValue.put("POSID",sellerInfoC.getString(sellerInfoC.getColumnIndex("POSID")));
                            sendValue.put("POSSN",sellerInfoC.getString(sellerInfoC.getColumnIndex("POSSN")));
                            sendValue.put("WebSite",sellerInfoC.getString(sellerInfoC.getColumnIndex("WebSite")));
                            SendData sd=new SendData(sendValue);

                            HashMap<String ,String> hm= sd.getIssue();
                            Message msg=new Message();
                            msg.obj=hm.get("MESSAGE");
                            if(hm.get("MESSAGE")!=null&&hm.get("MESSAGE").equals("成功")) {
                                ContentValues cv = new ContentValues();
                                cv.put("TAXMONTH", hm.get("TAXMONTH"));
                                cv.put("INVOICEHEADER", hm.get("INVOICEHEADER"));
                                cv.put("INVOICESTART", hm.get("INVOICESTART"));
                                cv.put("INVOICEEND", hm.get("INVOICEEND"));
                                db.insert("numberInfo", null, cv);
                                int start=Integer.parseInt( hm.get("INVOICESTART"));
                                int end=Integer.parseInt( hm.get("INVOICEEND"));
                                for(int i=start;i<=end;i++){
                                    ContentValues cvToHistory = new ContentValues();
                                    String INVOICENUMBER=hm.get("INVOICEHEADER") +String.format("%08d",i);
                                    cvToHistory.put("TAXMONTH",hm.get("TAXMONTH"));
                                    cvToHistory.put("INVOICENUMBER",INVOICENUMBER);
                                    cvToHistory.put("SELLERID",sellerInfoC.getString(sellerInfoC.getColumnIndex("SELLERID")));
                                    db.insert("history", null, cvToHistory);
                                }
                            }
                            else{
                                msg.obj="取號失敗";

                            }

                            Handler showMessage=new Handler(){
                                @Override
                                public void handleMessage(Message msg) {
                                    super.handleMessage(msg);
                                    Toast.makeText(context,(String)msg.obj,Toast.LENGTH_LONG);
                                }
                            };
                            showMessage.sendMessage(msg);


                        }
                    }catch (Exception e){
                        e=e;
                    }

            }
        };
        getNumberFromMongLi.start();
        try{
            getNumberFromMongLi.join();
        }catch (Exception e){
            e=e;
        }
        return "";
    };

    public synchronized static void printIvoice(final Context context,final String type,final String _Invoice){

        Thread printIvoice=new Thread("printIvoice") {
            public void run() {
                String Invoice=_Invoice;

                final EPSONPrint epson = new EPSONPrint();
                SQLite sqLite = new SQLite(context);
                final SQLiteDatabase db = sqLite.getReadableDatabase();
                Cursor sellerC = db.rawQuery("Select * From sellerInfo", null);
                sellerC.moveToFirst();
                String title = sellerC.getString(sellerC.getColumnIndex("Title"));
                String machine = sellerC.getString(sellerC.getColumnIndex("Machine"));
                String posid = sellerC.getString(sellerC.getColumnIndex("POSID"));
                String Commodity = sellerC.getString(sellerC.getColumnIndex("Commodity"));
                Cursor invoiceC = db.rawQuery("Select * From history Where INVOICENUMBER='" + Invoice + "' Order by _id desc", null);
                invoiceC.moveToFirst();
                String buyerid = invoiceC.getString(invoiceC.getColumnIndex("BUYERID"));
                String sellerid = invoiceC.getString(invoiceC.getColumnIndex("SELLERID"));
                String vehiclenumber = invoiceC.getString(invoiceC.getColumnIndex("VehicleNumber"));
                String lovenumber = invoiceC.getString(invoiceC.getColumnIndex("LOVENUMBER"));
                String Random = invoiceC.getString(invoiceC.getColumnIndex("RANDOM"));
                String Amount = invoiceC.getString(invoiceC.getColumnIndex("AMOUNT"));
                int No =Integer.parseInt( invoiceC.getString(invoiceC.getColumnIndex("No")));
                String date = invoiceC.getString(invoiceC.getColumnIndex("MakeTime"));

                if (date == null || date.equals("")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    date = sdf.format(new Date());
                }

                Pattern yyyyP = Pattern.compile("\\d\\d\\d\\d");
                Pattern MMP = Pattern.compile("(?<=\\d\\d\\d\\d-)\\d\\d");

                Pattern ddP = Pattern.compile("(?<=\\d\\d\\d\\d-\\d\\d-)\\d\\d");
                Matcher yyyyM = yyyyP.matcher(date);
                yyyyM.find();
                String yyy = Integer.parseInt(yyyyM.group()) - 1911 + "";

                Matcher MMM = MMP.matcher(date);
                MMM.find();
                int MMi = Integer.parseInt(MMM.group());
                int MMi2 = (MMi % 2 == 0 ? MMi : MMi + 1);
                String yyymmmm = yyy + String.format("年%02d-%02d月", MMi2 - 1, MMi2);

                Matcher ddM = ddP.matcher(date);
                ddM.find();
                String dd = ddM.group();
                String day = String.format("%s%02d%s", yyy, MMi, dd);
                String bar = yyy + MMi2 + Invoice + Random;

                String format25 = buyerid.equals("") ? "" : "格式25";
                String buyer = !buyerid.equals("") ? "買方" + buyerid : "";

                String HEXAmount = Integer.toHexString(Integer.parseInt(Amount));
                StringBuffer sb = new StringBuffer();
                for (int i = 8 - HEXAmount.length(); i >= 1; i--) {
                    sb.append("0");
                }
                HEXAmount = sb.append(HEXAmount).toString();

                String qrcode1 = "", qrcode2 = "";
                try {
                    byte[] cipherText = ConstructionInfo.cipher.doFinal((Invoice + Random).getBytes("ASCII"));
                    qrcode1 = Base64.encodeToString(cipherText, Base64.NO_WRAP);
                } catch (Exception e) {
                    e = e;
                }
                qrcode2 = sellerC.getString(sellerC.getColumnIndex("Commodity")) + ":1:" + Amount;


                byte[] printByte={};
                V1Printer v1Printer=null;
                if (!(type == null || type.equals(""))) {

                    if(type.equals("明細")){
                        switch (machine) {
                            case "頂尖":
                                byte[] _ESCW1 = {(byte) 0x1B, 'W', 0x00, 0x00, 0x00, 0x00, (byte) 0xC8, (byte) 0x01, (byte) 0xFF, (byte) 0x01};
                                epson.ESCW = _ESCW1;
                                epson.initPrint();
                                epson.SetXY((int) (24 - (float) title.getBytes().length / 1), 10);
                                epson.Big(String.format("%s", title));
                                epson.SetXY(18 - "交易明細".getBytes().length / 2, 16);
                                epson.Big(String.format("交易明細"));
                                epson.SetXY(6, 22);
                                epson.Nomal(String.format("%s", date));
                                epson.SetXY(6, 26);
                                epson.Nomal(String.format("品名/數量    單價    金額"));
                                epson.SetXY(6, 32);
                                epson.Nomal(String.format("%s/%4s%8s%8s", Commodity, "1", Amount, Amount));
                                epson.SetXY(6, 36);
                                epson.Nomal(String.format("合計共%s項", "1"));
                                epson.SetXY(6, 40);
                                epson.Nomal(String.format("總計 %s元", Amount));
                                epson.SetXY(6, 48);
                                epson.Nomal(String.format("機號:%s 單號:%s", posid, No));
                                epson.Print();
                                epson.Cut();
                                break;

                            case "BPT3":
                                byte[] _ESCW2 = {(byte) 0x1B, 'W', 0x00, 0x00, 0x00, 0x00, (byte) 0xC8, (byte) 0x01, (byte) 0xFF, (byte) 0x01};
                                epson.ESCW = _ESCW2;
                                epson.initPrint();
                                epson.SetXY((int) (25 - (float) title.getBytes().length / 1), 0);
                                epson.Big(String.format("%s", title));
                                epson.SetXY(25 - "交易明細".getBytes().length / 1, 6);
                                epson.Big(String.format("交易明細"));
                                epson.SetXY(6, 13);
                                epson.Nomal(String.format("%s", date));
                                epson.SetXY(6, 17);
                                epson.Nomal(String.format("品名/數量    單價    金額"));
                                epson.SetXY(6, 23);
                                epson.Nomal(String.format("%s/%4s%8s%8s", Commodity, "1", Amount, Amount));
                                epson.SetXY(6, 27);
                                epson.Nomal(String.format("合計共%s項", "1"));
                                epson.SetXY(6, 31);
                                epson.Nomal(String.format("總計 %s元", Amount));
                                epson.SetXY(6, 39);
                                epson.Nomal(String.format("機號:%s 單號:%s", posid, No));


                                epson.Print();
                                epson.Cut();
                                break;
                            case "商米":

                                v1Printer = new V1Printer(context);
                                v1Printer.beginTransaction();
                                v1Printer.printerInit();
                                v1Printer.setAlignment(1);
                                v1Printer.sendRAWData(new byte[]{0x1B, 0x21, 0x08});
                                v1Printer.printTextWithFont(String.format("%s\n", title), "gh", 60);
                                v1Printer.printTextWithFont(String.format("交易明細\n", title), "gh", 60);
                                v1Printer.sendRAWData(new byte[]{0x1B, 0x21, 0x00});
                                //v1Printer.epson.printTextWithFont(String.format("交易明細%s\n",replenish),"gh", 40);
                                v1Printer.sendRAWData(new byte[]{0x1B, 0x21, 0x08});
                                //v1Printer.printTextWithFont(String.format("%s\n", yyymmmm), "gh", 55);
                                //v1Printer.printTextWithFont(String.format("%s\n", INVOICENUMBER), "gh", 50);
                                v1Printer.sendRAWData(new byte[]{0x1B, 0x21, 0x00});
                                v1Printer.setAlignment(0);
                                v1Printer.printTextWithFont(String.format("%s\n", date), "gh", 26);
                                v1Printer.printTextWithFont(String.format("品名/數量    單價    金額\n"), "gh", 26);
                                v1Printer.printTextWithFont(String.format("%s/%4s%8s%8s\n", Commodity, "1", Amount, Amount), "gh", 26);
                                v1Printer.printTextWithFont(String.format("合計共%s項\n", "1"), "gh", 26);
                                v1Printer.printTextWithFont(String.format("總計 %s元\n", Amount), "gh", 26);
                                v1Printer.printTextWithFont(String.format("機號:%s 單號:%s\n", posid, No), "gh", 26);
                                v1Printer.lineWrap(3);
                                v1Printer.commitTransaction();

                        }
                        printByte=epson.getPaper();
                    }
                    else if(type.equals("印發票")||type.equals("補印")){
                        switch (machine) {
                            case "頂尖":
                                epson.initPrint();
                                epson.SetXY((int) (24 - (float) title.getBytes().length / 1), 10);
                                epson.Big(String.format("%s", title));
                                epson.SetXY(type.equals("補印") ?0:0, 16);
                                epson.Big(String.format("電子發票證明聯%s", type.equals("補印") ? type : ""));
                                epson.SetXY(6, 22);
                                epson.Big(String.format("%s", yyymmmm));
                                epson.SetXY(6, 28);
                                epson.Big(String.format("%s", Invoice.replaceFirst("(\\w\\w)","$1-")));
                                epson.SetXY(6, 32);
                                epson.Nomal(String.format("%s  %s", date, format25));
                                epson.SetXY(6, 36);
                                epson.Nomal(String.format("隨機碼:%s   總計:%s", Random, Amount));
                                epson.SetXY(6, 40);
                                epson.Nomal(String.format("賣方%s   %s", sellerid, buyer));
                                epson.Nomal(String.format("\n"));
                                epson.SetXY(4, 48);
                                epson.Barcode(String.format("%s", bar));
                                epson.Nomal(String.format("\n"));
                                epson.SetXY(5, 50);
                                epson.QRcode(String.format("%s%s%s%s%s%s%s%s:**********:1:1:0:", Invoice, day, Random, HEXAmount, HEXAmount, buyerid.equals("")?"00000000":buyerid, sellerid, qrcode1));
                                epson.SetXY(29, 50);
                                epson.QRcode2(String.format("**%s", qrcode2));
                                epson.SetXY(5, 70);
                                epson.Nomal(String.format("機號:%s       單號:%03d", posid, No));
                                epson.SetXY(5, 74);
                                epson.Nomal("退貨憑電字發票證明聯正本辦理");
                                break;
                            case "BPT3":
                                byte[] _ESCW = {(byte) 0x1B, 'W', 0x00, 0x00, 0x00, 0x00, (byte) 0xC8, (byte) 0x01, (byte) 0x9A, (byte) 0x02};
                                epson.ESCW = _ESCW;
                                epson.initPrint();
                                epson.SetXY(20 - title.getBytes().length / 2, 0);
                                epson.Big(String.format("%s", title));
                                epson.SetXY(type.equals("補印") ?0:6, 6);
                                epson.Big(String.format("電子發票證明聯%s", type.equals("補印") ? type : ""));
                                epson.SetXY(9, 12);
                                epson.Big(String.format("%s", yyymmmm));
                                epson.SetXY(9, 18);
                                epson.Big(String.format("%s", Invoice.replaceFirst("(\\w\\w)","$1-")));
                                epson.SetXY(6, 24);
                                epson.Nomal(String.format("%s  %s", date, format25));
                                epson.SetXY(6, 28);
                                epson.Nomal(String.format("隨機碼:%s   總計:%s", Random, Amount));
                                epson.SetXY(6, 32);
                                epson.Nomal(String.format("賣方%s   %s", sellerid, buyer));
                                epson.SetXY(10, 35);
                                epson.Barcode(String.format("%s", bar));
                                epson.Nomal("");
                                epson.SetXY(6, 45);
                                epson.QRcode(String.format("%s%s%s%s%s%s%s%s:**********:1:1:0:", Invoice, day, Random, HEXAmount, HEXAmount, buyerid.equals("")?"00000000":buyerid, sellerid, qrcode1));
                                epson.SetXY(30, 45);
                                epson.QRcode2(String.format("**%s", qrcode2));
                                epson.SetXY(7, 62);
                                epson.Nomal(String.format("機號:%s       單號:%03d", posid, No));
                                epson.SetXY(5, 66);

                                //epson.Nomal(String.format("機號:%s        NO:%03d",c.getString(3),backThread.No));
                                epson.Nomal("退貨憑電字發票證明聯正本辦理");
                                break;
                            case "商米":
                                v1Printer = new V1Printer(context);
                                v1Printer.beginTransaction();
                                v1Printer.printerInit();
                                v1Printer.setAlignment(1);
                                v1Printer.sendRAWData(new byte[]{0x1B, 0x21, 0x08});
                                v1Printer.printTextWithFont(String.format("%s\n", title), "gh", 60);
                                v1Printer.sendRAWData(new byte[]{0x1B, 0x21, 0x00});
                                v1Printer.printTextWithFont(String.format("電子發票證明聯%s\n", type.equals("補印") ? type : ""), "gh", 40);
                                v1Printer.sendRAWData(new byte[]{0x1B, 0x21, 0x08});
                                v1Printer.printTextWithFont(String.format("%s\n", yyymmmm), "gh", 55);
                                v1Printer.printTextWithFont(String.format("%s\n", Invoice.replaceFirst("(\\w\\w)","$1-")), "gh", 50);
                                v1Printer.sendRAWData(new byte[]{0x1B, 0x21, 0x00});
                                v1Printer.setAlignment(0);
                                v1Printer.printTextWithFont(String.format("%s  %s\n", date, format25), "gh", 26);
                                v1Printer.printTextWithFont(String.format("隨機碼:%s   總計:%s \n", Random, Amount), "gh", 26);
                                v1Printer.printTextWithFont(String.format("賣方%s   %s\n", sellerid, buyer), "gh", 26);
                                v1Printer.printBarCode(String.format("%s", bar), BarcodeFormat.CODE_39, 380, 60);
                                v1Printer.printText("\n");
                                v1Printer.printDoubleQRCode(String.format("%s%s%s%s%s%s%s%s:**********:1:1:0:", Invoice, day, Random, HEXAmount, HEXAmount, buyerid.equals("")?"00000000":buyerid, sellerid, qrcode1), String.format("**%s%70s", qrcode2, ""), 185);
                                v1Printer.setAlignment(0);
                                v1Printer.printTextWithFont(String.format("機號:%s   NO:%03d\n", posid, No), "gh", 26);
                                v1Printer.printTextWithFont("退貨憑電字發票證明聯正本辦理\n", "gh", 26);
                                v1Printer.lineWrap(3);
                                v1Printer.commitTransaction();
                                break;

                        }

                        epson.Print();

                        if (buyerid.equals("")) epson.Cut();

                        printByte = epson.getPaper();
                    }
                }
                String printyn= !buyerid.equals("")?"Y":(!lovenumber.equals("")||!vehiclenumber.equals(""))?"N":"Y";

                if(type.equals("明細")||printyn.equals("Y")||type.equals("補印")) {

                    if (!machine.equals("商米")) {
                        try {
                            Socket s = new Socket(sellerC.getString(sellerC.getColumnIndex("InvoiceIP")), Integer.parseInt(sellerC.getString(sellerC.getColumnIndex("InvoicePort"))));
                            OutputStream os = s.getOutputStream();
                            os.write(printByte);
                            os.close();
                            s.close();
                        } catch (Exception e) {
                            e = e;
                        }
                    }
                }

                if(type.equals("開錢櫃")&&!machine.equals("商米")){
                    try {
                        Socket s = new Socket(sellerC.getString(sellerC.getColumnIndex("InvoiceIP")), Integer.parseInt(sellerC.getString(sellerC.getColumnIndex("InvoicePort"))));
                        OutputStream os = s.getOutputStream();
                        os.write(epson.DLEDC4);
                        os.close();
                        s.close();
                    } catch (Exception e) {
                        e = e;
                    }
                }
                ContentValues cv=new ContentValues();
                cv.put("MakeTime",date);
                cv.put("PRINTYN",printyn);
                db.update("history",cv,"INVOICENUMBER='"+Invoice+"'",null);
            }
        };
        printIvoice.start();
        try{
            printIvoice.join();
        }catch (Exception e){
            e=e;
        }
    }
    public synchronized static void sendTransaction(final Context context){
        Thread sendTransaction=new Thread("sendTransaction") {
            @Override
            public void run() {
                SQLite sqLite = new SQLite(context);
                final SQLiteDatabase db = sqLite.getReadableDatabase();

                Cursor sellerC=db.rawQuery("Select * From sellerInfo ",null);
                sellerC.moveToFirst();
                Cursor needUpload=db.rawQuery("Select * From history Where PRINTYN IS NOT NULL AND MakeTime IS NOT NULL AND Submit1 IS NULL AND MESSAGE1 IS NULL",null);
                String ret="";

                if(needUpload.moveToFirst()){
                    do{
                        HashMap<String,String> hm=new HashMap<String,String>();
                        String InvoiceNumber=needUpload.getString(needUpload.getColumnIndex("INVOICENUMBER"));
                        String MakeTime=needUpload.getString(needUpload.getColumnIndex("MakeTime"));
                        Pattern DayP=Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d");
                        Matcher DayM=DayP.matcher(MakeTime);
                        DayM.find();
                        Pattern TimeP=Pattern.compile("\\d\\d:\\d\\d:\\d\\d");
                        Matcher TimeM=TimeP.matcher(MakeTime);
                        TimeM.find();
                        String buyerid=needUpload.getString(needUpload.getColumnIndex("BUYERID"));
                        String printyn=needUpload.getString(needUpload.getColumnIndex("PRINTYN"));
                        String lovenumber=needUpload.getString(needUpload.getColumnIndex("LOVENUMBER"));
                        String Random=needUpload.getString(needUpload.getColumnIndex("RANDOM"));
                        String vehicleNumber=needUpload.getString(needUpload.getColumnIndex("VehicleNumber"));
                        String Amount=needUpload.getString(needUpload.getColumnIndex("AMOUNT"));
                        String Serial=needUpload.getString(needUpload.getColumnIndex("Serial"));
                        String No=needUpload.getString(needUpload.getColumnIndex("No"));
                        String sellerid=sellerC.getString(sellerC.getColumnIndex("SELLERID"));
                        String possn=sellerC.getString(sellerC.getColumnIndex("POSSN"));
                        String posid=sellerC.getString(sellerC.getColumnIndex("POSID"));
                        String Commodity=sellerC.getString(sellerC.getColumnIndex("Commodity"));
                        String Type=needUpload.getString(needUpload.getColumnIndex("Type"));



                        hm.put("INVOICENUMBER",InvoiceNumber);
                        hm.put("DAY",DayM.group());
                        hm.put("TIME",TimeM.group());
                        hm.put("BUYERID",(buyerid.equals("")?"0000000000":buyerid));
                        hm.put("LOVEYN",lovenumber.equals("")?"0":"1");
                        hm.put("PRINTYN",printyn);
                        String tmp=!lovenumber.equals("")?hm.put("LOVENUMBER",lovenumber):"";

                        hm.put("Random",Random);

                        tmp=!vehicleNumber.equals("")?hm.put("VehicleNumber",vehicleNumber):"";

                        hm.put("B",Commodity+",,1,,"+Amount);
                        hm.put("PS",Serial+"||"+No+"||"+Type);
                        hm.put("SELLERID",sellerid);
                        hm.put("POSSN",possn);
                        hm.put("POSID",posid);
                        hm.put("MakeTime",MakeTime);
                        SendData sd=new SendData(hm);
                        HashMap<String,String> hmret=null;
                        try {
                            hmret=sd.sendTransaction();

                            FileWriter fw= new FileWriter(Environment.getDataDirectory()+InvoiceNumber+".xml",false);
                            fw.write(sd.xml.getXmlData());

                            fw.close();

                        }catch (Exception e){
                            e=e;
                        }
                        if(hmret.get("MESSAGE")!=null) {
                            ContentValues cv = new ContentValues();
                            cv.put("Submit1", "YES");
                            cv.put("MESSAGE1", hmret.get("MESSAGE"));
                            db.update("history", cv, "INVOICENUMBER='" + InvoiceNumber + "'", null);
                            ret += InvoiceNumber + "：" + hmret.get("MESSAGE") + "\n";
                        }


                    }while (needUpload.moveToNext());
                    Message msg=new Message();
                    msg.obj=ret;
                    if(!ret.equals("")) {
                        //ConstructionInfo.alert.sendMessage(msg);
                    }
                }



                super.run();
            }
        };
        sendTransaction.start();
        try{
            sendTransaction.join();
        }catch (Exception e){
            e=e;
        }
    }
    public synchronized static void sendInvalid(final Context context){
        Thread t=new Thread("sendInvalid") {
            @Override
            public void run() {
                SQLite sqLite = new SQLite(context);
                final SQLiteDatabase db =sqLite.getReadableDatabase();
                Cursor sellerInfoC=db.rawQuery("SELECT * FROM sellerInfo  ", null);
                sellerInfoC.moveToFirst();
                Cursor historyC = db.rawQuery("SELECT * FROM history Where CANCEL_DATE IS NOT NULL  AND CANCEL_TIME IS NOT NULL AND CANCEL_REASON IS NOT NULL AND Submit2 IS NULL AND MESSAGE2 IS NULL  ", null);

                String ret="";
                if(historyC.moveToFirst()) {
                    do {
                        HashMap<String, String> sendValue = new HashMap<String, String>();
                        String INVOICE_NUMBER = historyC.getString(historyC.getColumnIndex("INVOICENUMBER"));
                        String Maketime = historyC.getString(historyC.getColumnIndex("MakeTime"));

                        sendValue.put("POSID", sellerInfoC.getString(sellerInfoC.getColumnIndex("POSID")));
                        sendValue.put("POSSN", sellerInfoC.getString(sellerInfoC.getColumnIndex("POSSN")));
                        sendValue.put("INVOICENUMBER", INVOICE_NUMBER);
                        sendValue.put("DAY", historyC.getString(historyC.getColumnIndex("MakeTime")).replaceFirst(" \\d\\d:\\d\\d:\\d\\d", ""));
                        sendValue.put("SELLERID", sellerInfoC.getString(sellerInfoC.getColumnIndex("SELLERID")));
                        sendValue.put("BUYERID", historyC.getString(historyC.getColumnIndex("BUYERID")).equals("") ? "0000000000" : historyC.getString(historyC.getColumnIndex("BUYERID")));
                        sendValue.put("CDAY", historyC.getString(historyC.getColumnIndex("CANCEL_DATE")));
                        sendValue.put("CTIME", historyC.getString(historyC.getColumnIndex("CANCEL_TIME")));
                        sendValue.put("CANCEL_REASON", historyC.getString(historyC.getColumnIndex("CANCEL_REASON")));


                        try {
                            HashMap<String, String> hm = new SendData(sendValue).invalidInvoice();
                            ret += INVOICE_NUMBER+"："+hm.get("MESSAGE")+"\n";
                            ContentValues cv;
                            if (hm.get("MESSAGE") != null) {
                                cv = new ContentValues();
                                cv.put("Submit2", "OK!");
                                cv.put("REPLY2", hm.get("REPLY"));
                                cv.put("MESSAGE2", hm.get("MESSAGE"));
                                try {
                                    db.update("history", cv, "INVOICENUMBER='" + INVOICE_NUMBER + "' AND MakeTime='" + Maketime + "'  ", null);
                                } catch (Exception e) {
                                    e = e;
                                }


                            }
                        } catch (Exception e) {
                            e = e;
                        }
                    } while (historyC.moveToNext());
                    Message msg=new Message();
                    msg.obj=ret;
                    if(!ret.equals("")) {
                        //ConstructionInfo.alert.sendMessage(msg);
                    }
                }

            }
        };
        t.start();
        try{
            t.join();
        }catch (Exception e){
            e=e;
        }
    }
    public synchronized static void uploadTDC(final Context context){
        Thread t=new Thread("uploadTDC"){
            @Override
            public void run() {

                    try{
                        SQLite sqLite=new SQLite(context);
                        SQLiteDatabase db=sqLite.getReadableDatabase();
                        Cursor sellerInfoC=db.rawQuery("SELECT * FROM sellerInfo",null);
                        sellerInfoC.moveToFirst();
                        Cursor c= db.rawQuery("SELECT  *   FROM history Where MakeTime IS NOT NULL AND TDC IS NULL",null);
                        if(c.moveToFirst()){
                            do {
                                String item=sellerInfoC.getString(sellerInfoC.getColumnIndex("Commodity"))+","+c.getString(c.getColumnIndex("AMOUNT"))+","+1;
                                String url =String.format("http://admin.joyspots.net/posapi/Invoice.aspx?CompanyTaxID=%s&TDCID=%s&POSNAME=%s&POSID=%s&Operation=AddNew&InvoiceNo=%s&DATE=%s&AIXIN1MA3=%s&ZAI3JU4=%s&TransactionNo=%s&Amount=%s&BuyerCompanyTaxID=%s&Items=%s",
                                        c.getString(c.getColumnIndex("SELLERID")),sellerInfoC.getString(sellerInfoC.getColumnIndex("StoreID")),sellerInfoC.getString(sellerInfoC.getColumnIndex("POSID")),sellerInfoC.getString(sellerInfoC.getColumnIndex("POSSN")),c.getString(c.getColumnIndex("INVOICENUMBER")),c.getString(c.getColumnIndex("MakeTime")).replaceAll(" ","%20"),c.getString(c.getColumnIndex("LOVENUMBER")),c.getString(c.getColumnIndex("VehicleNumber")),c.getString(c.getColumnIndex("Serial")),c.getString(c.getColumnIndex("AMOUNT")),c.getString(c.getColumnIndex("BUYERID")),item);
                                URL obj = new URL(url);
                                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                                if( con.getResponseCode() == HttpURLConnection.HTTP_OK ){
                                    db.execSQL("UPDATE history SET TDC ='1' WHERE  INVOICENUMBER='"+c.getString(c.getColumnIndex("INVOICENUMBER"))+"' AND AMOUNT='"+c.getString(c.getColumnIndex("AMOUNT"))+"'");
                                }

                            }while (c.moveToNext());

                        }

                        c= db.rawQuery("SELECT  *   FROM history Where CANCEL_TIME IS NOT NULL AND  TDC='1'",null);
                        if(c.moveToFirst()){
                            do {

                                String url = String.format("http://admin.joyspots.net/posapi/Invoice.aspx?CompanyTaxID=%s&TDCID=%s&POSID=%s&Operation=Void&InvoiceNo=%s&DATE=%s",
                                        c.getString(c.getColumnIndex("SELLERID")),sellerInfoC.getString(sellerInfoC.getColumnIndex("StoreID")),sellerInfoC.getString(sellerInfoC.getColumnIndex("POSSN")),c.getString(c.getColumnIndex("INVOICENUMBER")),c.getString(c.getColumnIndex("MakeTime")).replaceAll(" ","%20"));
                                URL obj = new URL(url);
                                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                                if( con.getResponseCode() == HttpURLConnection.HTTP_OK ) {
                                    db.execSQL("UPDATE history SET TDC ='2' WHERE  INVOICENUMBER='" + c.getString(c.getColumnIndex("INVOICENUMBER")) + "' AND AMOUNT='" + c.getString(c.getColumnIndex("AMOUNT")) + "'");
                                }
                            }while (c.moveToNext());

                        }
                    }catch (Exception e){
                        e=e;
                    }
                }

        };
        t.start();
        try{
            t.join();
        }catch (Exception e){
            e=e;
        }
    }
}
