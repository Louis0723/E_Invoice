package lursun.einvoice;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.TintTypedArray;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2016/9/25.
 */
public class ReportActivity extends Activity {
    public ArrayList<Integer> MMa=new ArrayList<Integer>();
    public int[] daya={0,31,28,31,30,31,30,31,31,30,31,30,31};
    public LinearLayout tableScroll;
    int yyyy=0;
    public static SimpleDateFormat sdfyyyy=new SimpleDateFormat("yyyy");
    public static SimpleDateFormat sdfMM=new SimpleDateFormat("MM");
    public static SimpleDateFormat sdfdd=new SimpleDateFormat("dd");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_layout);

        Date date=new Date();
        ((EditText)findViewById(R.id.month)).setText(sdfMM.format(date));
        ((EditText)findViewById(R.id.day)).setText(sdfdd.format(date));

        int MM=Integer.parseInt( sdfMM.format(date));

        if(MM%2==0)MMa.add(MM-3>0?MM-3:MM+9);
        MMa.add(MM-2>0?MM-2:MM+10);
        MMa.add(MM-1>0?MM-1:MM+11);
        MMa.add(MM);
        yyyy=Integer.parseInt( sdfyyyy.format(date));
        daya[2]=yyyy%4==0&&yyyy%100!=0||yyyy%400==0?29:28;

        tableScroll=(LinearLayout) findViewById(R.id.tableScroll);

        //---------------------------------------------------------------------------------------------
        makeReport();
        showEarnings();
    }
    public void showEarnings(){
        String MM= ((EditText)findViewById(R.id.month)).getText().toString();
        String dd= ((EditText)findViewById(R.id.day)).getText().toString();
        SQLite sqLite = new SQLite(this);
        final SQLiteDatabase db =sqLite.getReadableDatabase();
        String sql="SELECT SUM( cast( Amount as Integer))  as total  FROM 'history' WHERE  MakeTime LIKE '"+yyyy+"-"+MM+"-"+dd+"%' AND CANCEL_REASON IS NULL  Order By _id DESC ";
        Cursor c= db.rawQuery(sql,null);
        c.moveToFirst();
        ((TextView)findViewById(R.id.dayAmount)).setText(""+c.getInt(0));
        sql="SELECT SUM( cast( Amount as Integer))  as total  FROM 'history'  WHERE MakeTime  LIKE '"+yyyy+"-"+MM+"%' AND CANCEL_REASON IS NULL";
        c= db.rawQuery(sql,null);
        c.moveToFirst();
        ((TextView)findViewById(R.id.monthAmount)).setText(""+c.getInt(0));
    }
    public void dayClick(View view){
        int dd=Integer.parseInt(((EditText)findViewById(R.id.day)).getText().toString());
        int MM=Integer.parseInt(((EditText)findViewById(R.id.month)).getText().toString());
        SimpleDateFormat sdfddnow=new SimpleDateFormat("dd");
        SimpleDateFormat sdfMMnow=new SimpleDateFormat("MM");
        Date date=new Date();
        int ddnow=Integer.parseInt(sdfdd.format(date));
        int MMnow=Integer.parseInt(sdfMM.format(date));
        if(((TextView)view).getText().toString().equals("▶")){
            dd++;
            if(MMnow==MM){
                if(dd>ddnow)dd=1;
            }
            else{
                if(dd>daya[MM])dd=1;
            }
        }else {
            dd--;
            if(dd<=0){
                if (MMnow == MM) {
                    dd = ddnow;
                } else {
                    dd = daya[MM];
                }
            }
        }
        ((EditText)findViewById(R.id.day)).setText(String.format("%02d",dd));
        makeReport();
        getCurrentFocus().clearFocus();
        showEarnings();
    }
    public void monthClick(View view){

        int MM=Integer.parseInt(((EditText)findViewById(R.id.month)).getText().toString());
        if(((TextView)view).getText().toString().equals("▶")){
            if(MMa.indexOf(++MM)>=0){
                ((EditText)findViewById(R.id.month)).setText(String.format("%02d",MM));
            }
        }else {
            if(MMa.indexOf(--MM)>=0){
                ((EditText)findViewById(R.id.month)).setText(String.format("%02d",MM));
            }
        }

        if(sdfMM.format(new Date()).equals(String.format("%02d",MM))){
            if(Integer.parseInt(((EditText)findViewById(R.id.day)).getText().toString())> Integer.parseInt(sdfdd.format(new Date()))){
                ((EditText)findViewById(R.id.day)).setText(sdfdd.format(new Date()));
            }
        }
        makeReport();
        showEarnings();
    }
    public void makeReport(){
        ((LinearLayout) findViewById(R.id.tableScroll)).removeAllViews();
        String MM= ((EditText)findViewById(R.id.month)).getText().toString();
        String dd= ((EditText)findViewById(R.id.day)).getText().toString();
        SQLite sqLite = new SQLite(this);
        final SQLiteDatabase db =sqLite.getReadableDatabase();
        String sql="SELECT INVOICENUMBER , Serial , AMOUNT  , CANCEL_REASON , Submit1 , Submit2,MakeTime   FROM 'history' WHERE  MakeTime  LIKE '"+yyyy+"-"+MM+"-"+dd+"%' Order By _id DESC ";
        Cursor c= db.rawQuery(sql,null);
        final View.OnLongClickListener OLC=new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                final String str[]=(String[]) v.getTag();
                TextView title=(new TextView(ReportActivity.this));
                title.setText("動作");title.setTextSize(40);
                title.setGravity(Gravity.CENTER|Gravity.LEFT);
                title.setPadding(20,20,20,0);
                title.setTextColor(Color.rgb(255,64,64));

                final AlertDialog action=new AlertDialog.Builder(ReportActivity.this).setCustomTitle(title).setNeutralButton("作廢",null).setNegativeButton("補印",null).setPositiveButton("印明細",null).create();

                Cursor msgC=db.rawQuery("Select * From history Where INVOICENUMBER='"+str[0]+"' AND MakeTime='"+str[1]+"'",null);
                if(msgC.moveToFirst()) {
                    action.setMessage( String.format("From:%s\n%s 金額:%s\n時間:%s\n序號:%s No:%s\n%s %s",msgC.getString(msgC.getColumnIndex("Type")),msgC.getString(msgC.getColumnIndex("INVOICENUMBER")),msgC.getString(msgC.getColumnIndex("AMOUNT")),msgC.getString(msgC.getColumnIndex("MakeTime")),msgC.getString(msgC.getColumnIndex("Serial")),msgC.getString(msgC.getColumnIndex("No")),msgC.getString(msgC.getColumnIndex("MESSAGE1")),msgC.getString(msgC.getColumnIndex("MESSAGE2"))==null?"":msgC.getString(msgC.getColumnIndex("MESSAGE2"))) );

                }

                action.show();

                ((TextView)action.findViewById(android.R.id.message)).setTextSize(30);

                action.setButton(AlertDialog.BUTTON_NEGATIVE, "補印", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        action.dismiss();
                        Module.printIvoice(ReportActivity.this,"補印",str[0]);
                        Module.printIvoice(ReportActivity.this,"明細",str[0]);
                    }
                });

                Button negative=action.getButton(AlertDialog.BUTTON_NEGATIVE);
                negative.setTextSize(30);
                action.setButton(AlertDialog.BUTTON_NEUTRAL, "作廢", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        final EditText ET=new EditText(ReportActivity.this);
                        ET.setEms(4);
                        ET.setMaxLines(4);
                        ET.setTextSize(30);
                        ET.setInputType(InputType.TYPE_CLASS_NUMBER);

                        DialogInterface.OnClickListener olc=new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String Random=ET.getText().toString();

                                String sql="SELECT * FROM 'history' WHERE (  Random='"+Random+"'  OR PrintYN='N'    ) AND INVOICENUMBER='"+str[0]+"' AND MakeTime='"+str[1]+"'  AND CANCEL_REASON IS NULL  Order By _id DESC  ";
                                Cursor c= db.rawQuery(sql,null);
                                if(c.getCount()>=1){
                                    SimpleDateFormat sdfdate=new SimpleDateFormat("yyyy-MM-dd");
                                    SimpleDateFormat sdftime=new SimpleDateFormat("HH:mm:ss");
                                    String CANCEL_DATE=sdfdate.format(new Date());
                                    String CANCEL_TIME=sdftime.format(new Date());
                                    String CANCEL_REASON="退貨";
                                    db.execSQL("Update history SET CANCEL_DATE='"+CANCEL_DATE+"', CANCEL_TIME='"+CANCEL_TIME+"',CANCEL_REASON='"+CANCEL_REASON+"' Where  INVOICENUMBER='"+str[0]+"' AND MakeTime='"+str[1]+"' ");
                                    makeReport();
                                }else {
                                    new AlertDialog.Builder(ReportActivity.this).setPositiveButton("確認", null).setTitle("作廢失敗，請確定隨機碼").show();


                                }
                            }
                        };
                        TextView title=new TextView(ReportActivity.this);
                        title.setText("請輸入發票隨機碼\n(載具、愛心碼免填)");
                        title.setTextSize(35);
                        title.setTextColor(Color.rgb(255,64,64));
                        title.setPadding(20,20,20,20);

                        AlertDialog ad=new AlertDialog.Builder(ReportActivity.this).setCustomTitle(title).setIcon(android.R.drawable.ic_dialog_info).setView(ET).setPositiveButton("確定", olc).setNeutralButton("取消", null).show();
                        ad.getButton(AlertDialog.BUTTON_NEUTRAL).setTextSize(30);
                        ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(30);
                    }
                });
                Button neutral=action.getButton(AlertDialog.BUTTON_NEUTRAL);
                neutral.setTextSize(30);
                action.setButton(AlertDialog.BUTTON_POSITIVE, "印明細", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        action.dismiss();
                        Module.printIvoice(ReportActivity.this,"明細",str[0]);
                    }
                });
                Button positive=action.getButton(AlertDialog.BUTTON_POSITIVE);
                positive.setTextSize(30);
                return false;
            }
        };
        LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
        if(c.moveToFirst())
            do {
                LinearLayout LL=(LinearLayout)inflater.inflate(R.layout.table_template,null);
                ((TextView)LL.findViewById(R.id.INVOICENUMBER)).setText(c.getString(c.getColumnIndex("INVOICENUMBER")));
                Pattern p=Pattern.compile("\\w{0,10}$");
                Matcher m=p.matcher( c.getString(c.getColumnIndex("Serial")));
                m.find();
                ((TextView)LL.findViewById(R.id.Serial)).setText(m.group());
                ((TextView)LL.findViewById(R.id.Amount)).setText(c.getString(c.getColumnIndex("AMOUNT")));
                ((TextView)LL.findViewById(R.id.Time)).setText(c.getString(c.getColumnIndex("MakeTime")).replaceFirst(":\\d\\d$","").replaceFirst(".+?(?=\\d\\d:\\d\\d)",""));
                ((TextView)LL.findViewById(R.id.CANCEL_REASON)).setText(c.getString(c.getColumnIndex("CANCEL_REASON"))!=null?"✘":"✔");
                ((TextView)LL.findViewById(R.id.CANCEL_REASON)).setTextColor(Color.rgb(c.getString(c.getColumnIndex("CANCEL_REASON"))!=null?255:0,c.getString(c.getColumnIndex("CANCEL_REASON"))!=null?0:255,0));

                if(c.getString(c.getColumnIndex("Submit1"))!=null) {
                    if(c.getString(c.getColumnIndex("CANCEL_REASON"))!=null ^ c.getString(c.getColumnIndex("Submit2"))!=null){
                        ((TextView)LL.findViewById(R.id.upload)).setText("✘");
                        ((TextView)LL.findViewById(R.id.upload)).setTextColor(Color.rgb(255,0,0));
                    }else {
                        ((TextView)LL.findViewById(R.id.upload)).setText("✔");
                        ((TextView)LL.findViewById(R.id.upload)).setTextColor(Color.rgb(0,255,0));
                    }
                }else {
                    ((TextView)LL.findViewById(R.id.upload)).setText("✘");
                    ((TextView)LL.findViewById(R.id.upload)).setTextColor(Color.rgb(255,0,0));
                }


                LL.setOnLongClickListener(OLC);

                String[]  str={c.getString(c.getColumnIndex("INVOICENUMBER")),c.getString(c.getColumnIndex("MakeTime"))};
                LL.setTag(str);
                (tableScroll).addView(LL);
            }while (c.moveToNext());

    }
    public void errorCheck(View view){
        SQLite sqLite = new SQLite(this);
        SQLiteDatabase db =sqLite.getReadableDatabase();
        Cursor c=db.rawQuery("select MESSAGE1 ,MESSAGE2 From history Where (Submit1 IS NOT NULL and  not (MESSAGE1  like \"%已上傳%\" or MESSAGE1  like \"%發票資料已轉入加值中心%\")) or (Submit2 IS NOT NULL and MESSAGE2 not like \"%發票作廢成功%\")",null);
        if(c.moveToFirst())
            new AlertDialog.Builder(ReportActivity.this).setTitle(c.getString(0)+"  "+c.getString(1)).setIcon(android.R.drawable.ic_dialog_info).setPositiveButton("确定", null).show();
        else
            new AlertDialog.Builder(ReportActivity.this).setTitle("恩...你很健康").setIcon(android.R.drawable.ic_dialog_info).setPositiveButton("确定", null).show();

    }
}
