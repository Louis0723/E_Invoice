package lursun.einvoice;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.midi.MidiOutputPort;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.WindowManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/12/4.
 */
public class BGService extends Service {
    static Thread loopthread=null;
    static ServerSocket server=null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        if(loopthread==null) {
            ConstructionInfo.alert_init(getApplicationContext());
            Message msg=new Message();
            msg.obj="start";
            ConstructionInfo.alert.sendMessage(msg);
            final SQLite sqLite = new SQLite(this);
            final SQLiteDatabase db =sqLite.getReadableDatabase();

            final Handler handler=new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    final Object[] object= (Object[]) msg.obj;





                    Cursor searchC=db.rawQuery("Select * From history Where Serial= '"+((String)object[4])+"'  Order by _id Desc ",null);
                    if(searchC.getCount()>0){
                        searchC.moveToFirst();
                        final String INVOICENUMBER=searchC.getString(searchC.getColumnIndex("INVOICENUMBER"));;
                        final String MakeTime=searchC.getString(searchC.getColumnIndex("MakeTime"));

                        AlertDialog.Builder builder=new AlertDialog.Builder(getApplicationContext()).setTitle("提示");
                        builder.setMessage("發現重複帳單號\n是否作廢重開");
                        builder.setPositiveButton("作廢重開", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SimpleDateFormat sdfdate=new SimpleDateFormat("yyyy-MM-dd");
                                SimpleDateFormat sdftime=new SimpleDateFormat("HH:mm:ss");
                                String CANCEL_DATE=sdfdate.format(new Date());
                                String CANCEL_TIME=sdftime.format(new Date());
                                String CANCEL_REASON="退貨";

                                ContentValues cv=new ContentValues();
                                cv.put("CANCEL_DATE",CANCEL_DATE);
                                cv.put("CANCEL_TIME",CANCEL_TIME);
                                cv.put("CANCEL_REASON",CANCEL_REASON);

                                db.update("history",cv,"INVOICENUMBER= '"+INVOICENUMBER+"'  AND  MakeTime= '"+MakeTime+"' ",null);
                                if(object.length==6)
                                {
                                    new BuyerInfo((Context) object[0],(LayoutInflater)object[1],(String) object[2],(String) object[3],(String) object[4],(String) object[5]);
                                }
                                if(object.length==7)
                                {
                                    new BuyerInfo((Context) object[0],(LayoutInflater)object[1],(String) object[2],(String) object[3],(String) object[4],(String) object[5],(String) object[6]);
                                }


                            }
                        });
                        builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                        AlertDialog ad=builder.create();
                        ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        try {

                            ad.show();
                        }catch (Exception e){
                            e=e;
                        }
                    }
                    else {
                        if(object.length==6)
                        {
                            new BuyerInfo((Context) object[0],(LayoutInflater)object[1],(String) object[2],(String) object[3],(String) object[4],(String) object[5]);
                        }
                        if(object.length==7)
                        {
                            new BuyerInfo((Context) object[0],(LayoutInflater)object[1],(String) object[2],(String) object[3],(String) object[4],(String) object[5],(String) object[6]);
                        }
                    }


                }
            };


            final LayoutInflater inflater=(LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

            try{
                server=new ServerSocket(9100);
            }catch (Exception e){
                e=e;
            }
            Thread T=new Thread("getCommodity"){
                @Override
                public void run() {

                    while (true){
                        try {
                            final Socket socket=server.accept();
                            new Thread("accept"){
                                @Override
                                public void run() {
                                    try {
                                        byte[] word={} ;
                                        InputStream is = socket.getInputStream();

                                        while (true){
                                            byte[] word2 = {(byte) is.read()};
                                            if(word2[0]!=-1)
                                                word=link(word,word2);
                                            else
                                                break;
                                        }
                                        String s=new String(word,"big5");
                                        SQLiteDatabase db =sqLite.getReadableDatabase();
                                        Cursor sellerInfoC=db.rawQuery("SELECT * FROM sellerInfo",null);
                                        sellerInfoC.moveToFirst();

                                        try{
                                            if(s.indexOf("\"TSS24.BF2\"")>0){

                                                Socket socket = new Socket(sellerInfoC.getString(sellerInfoC.getColumnIndex("LabelIP")), Integer.parseInt(sellerInfoC.getString(sellerInfoC.getColumnIndex("LabelPort"))));
                                                OutputStream os = socket.getOutputStream();
                                                String ss = s.replaceAll("TSS24\\.BF2", "TST24.BF2");
                                                os.write(ss.getBytes("BIG5"));
                                                os.close();
                                                socket.close();
                                            }


                                            if(s.indexOf("配菜聯(標籤")>0){
                                                Pattern pname= Pattern.compile("(?<=\\n\u001B!0\u001C!\\s\u001D!\u0011)[\\w\\W]+?\u001D");
                                                Matcher mname=pname.matcher(s);

                                                Pattern ptable= Pattern.compile("(?<=台號：\u001B!0\u001C!\\s\u001D!\u0011)[\\w\\W]+?\u001D");
                                                Matcher mtable=ptable.matcher(s);

                                                Pattern pno= Pattern.compile("(?<=單號：\u001B!0\u001C!\\s\u001D!\u0011)[\\w\\W]+?\u001D");
                                                Matcher mno=pno.matcher(s);

                                                Pattern ptype= Pattern.compile("(?<=口味:)[\\w\\W]+?\u001D");
                                                Matcher mtype=ptype.matcher(s);

                                                Pattern ptime= Pattern.compile("\\d\\d:\\d\\d");
                                                Matcher mtime=ptime.matcher(s);

                                                Pattern pip= Pattern.compile("(?<=\\^)\\d+?(?=\\^)");
                                                Matcher mip=pip.matcher(s);
                                                mip.find();
                                                String print=String.format( "CASHDRAWER 1, 50, 6\n" +
                                                        "SIZE 40 mm, 30 mm\n" +
                                                        "GAP 2 mm, 0 mm\n" +
                                                        "DIRECTION 0\n" +
                                                        "SHIFT 0\n" +
                                                        "SET PEEL OFF\n" +
                                                        "CLS\n" +
                                                        "TEXT 12,20,\"TST24.BF2\",0,2,2,\"%s\"\n" +
                                                        "TEXT 12,70,\"TST24.BF2\",0,1,2,\"%s\"\n" +
                                                        "TEXT 30,95,\"TST24.BF2\",0,1,1,\"\"\n" +
                                                        "TEXT 30,120,\"3\",0,2,2,\"No.%s\"\n" +
                                                        "TEXT 30,155,\"TST24.BF2\",0,1,1,\"          \"\n" +
                                                        "TEXT 30,165,\"TST24.BF2\",0,2,2,\"        %s\"\n" +
                                                        "TEXT 30,210,\"TST24.BF2\",0,1,1,\"Time:%s %s       \"\n" +
                                                        "PRINT 1\n" +
                                                        "EOF\n",mname.find()?mname.group():"",mtype.find()?mtype.group():"",mno.find()?mno.group():"",mtable.find()?mtable.group().replaceFirst("[\\W\\w]+?-",""):"",mtime.find()?mtime.group():"",sellerInfoC.getString(sellerInfoC.getColumnIndex("Title")));
                                                Socket socket = new Socket("192.168.123."+mip.group(), 9100);
                                                OutputStream os = socket.getOutputStream();
                                                os.write(print.getBytes("BIG5"));
                                                os.close();
                                                socket.close();

                                            }
                                            else if (s.indexOf("結帳單(賬務聯)")>0 && s.indexOf("客戶聯") < 0) {
                                                Pattern getMoneyp = Pattern.compile("(?<=實收金額)[\\W\\w]+?(?>\\d+?\\.00)");
                                                Matcher getMoneym = getMoneyp.matcher(s);
                                                if(getMoneym.find()) {
                                                    Pattern getDiscountp = Pattern.compile("(?<=折讓)[\\W\\w]+?(?>\\d+?\\.0)");
                                                    Matcher getDiscountm = getDiscountp.matcher(s);

                                                    Pattern getCouponp = Pattern.compile("(?<=禮券)[\\W\\w]+?(?>\\d+?\\.0)");
                                                    Matcher getCouponm = getCouponp.matcher(s);
                                                    int cut = 0;

                                                    cut += getDiscountm.find() ? Integer.parseInt(getDiscountm.group().replaceAll("\\.0|\\D", "")) : 0;
                                                    cut += getCouponm.find() ? Integer.parseInt(getCouponm.group().replaceAll("\\.0|\\D", "")) : 0;

                                                    String Money = Integer.parseInt(getMoneym.group().replaceFirst("[\\w\\W]+?(?=(?>\\d+?\\.00+?))", "").replaceFirst("\\.00", "")) - cut + "";
                                                    Pattern givechangep = Pattern.compile("(?<=找零金額)[\\W\\w]+?(?>\\d+?\\.00)");
                                                    Matcher givechangepm = givechangep.matcher(s);
                                                    String givechange=null;
                                                    if (givechangepm.find()) {
                                                        givechange = (givechangepm.group().replaceFirst("[\\w\\W]+?(?=(?>\\d+?\\.00+?))", "").replaceFirst("\\.00", ""));
                                                    }
                                                    Pattern SerialP = Pattern.compile("(?<=帳單號\\:)\\d+");
                                                    Matcher SerialM = SerialP.matcher(s);
                                                    if (SerialM.find()) {
                                                        String serial = SerialM.group();


                                                        Pattern NoP = Pattern.compile("(?<=\\W單 {0,4}號:)[\\W\\w]*?\\d+?(?=\u001D!)");
                                                        Matcher NoM = NoP.matcher(s);
                                                        NoM.find();
                                                        String No = NoM.group().replaceFirst("[\\w\\W]+?(?=\\d+$)", "");
                                                        Message msg=new Message();

                                                        if (givechange != null){
                                                            Object[] objects={getApplicationContext(), inflater, "POS", No, serial, Money ,givechange};
                                                            msg.obj=objects;

                                                        }
                                                        else {
                                                            Object[] objects={getApplicationContext(), inflater, "POS", No, serial, Money };
                                                            msg.obj=objects;

                                                        }
                                                        handler.sendMessage(msg);
                                                        if(sellerInfoC.getString(sellerInfoC.getColumnIndex("AccountsYN")).equals("YES")){
                                                            Socket socket = new Socket(sellerInfoC.getString(sellerInfoC.getColumnIndex("AccountsIP")), Integer.parseInt(sellerInfoC.getString(sellerInfoC.getColumnIndex("AccountsPort"))));
                                                            OutputStream os = socket.getOutputStream();
                                                            os.write(word);
                                                            os.close();
                                                            socket.close();
                                                        }
                                                    }
                                                }
                                            }else {
                                                try{
                                                    Socket socket = new Socket(sellerInfoC.getString(sellerInfoC.getColumnIndex("AccountsIP")), Integer.parseInt(sellerInfoC.getString(sellerInfoC.getColumnIndex("AccountsPort"))));
                                                    OutputStream os = socket.getOutputStream();
                                                    os.write(word);
                                                    os.close();
                                                    socket.close();
                                                }catch (Exception e){
                                                    e=e;
                                                }


                                            }

                                        }catch (Exception e){
                                            e=e;
                                        }



                                    }catch (Exception e){
                                        e=e;
                                    }
                                }
                            }.start();
                        }catch (Exception e){}
                    }

                }
            };
            T.start();

            loopthread = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        Module.sendTransaction(getApplicationContext());
                        Module.getNumberFromMongLi(getApplicationContext());
                        Module.sendInvalid(getApplicationContext());
                        Module.uploadTDC(getApplicationContext());
                        try {
                            Thread.sleep(60000);
                        } catch (Exception e) {
                            e = e;
                        }

                        if (false) break;
                    }
                }
            };
            loopthread.start();
            return super.onStartCommand(intent, flags, startId);
        }else {
            this.stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }




    }
    public byte[] link(byte[] ... B){
        int length=0;
        for (byte[] b :B){
            length+=b.length;
        }
        ByteBuffer bb= ByteBuffer.allocate(length);
        for(byte[] b : B){
            bb.put(b);
        }
        return bb.array();
    }
}
