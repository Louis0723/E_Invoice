package com.lursun.einvoice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.Inflater;

/**
 * Created by admin on 2016/12/5.
 */
public class BuyerInfo {
    String type;
    String serial;
    String money;
    String No;
    static boolean Lock=true;
    BuyerInfo(final Context context, LayoutInflater _inflater,String _type,String _No,String _serial,String _money,String ... give_change){
        if(Lock) {
            Lock=!Lock;
            type = _type;
            serial = _serial;
            money = _money;
            No=_No;
            final LinearLayout buyer_info = (LinearLayout) _inflater.inflate(R.layout.buyer_info, null);
            final EditText lovenumberE = (EditText) buyer_info.findViewById(R.id.LOVENUMBER);
            final EditText buyeridE = (EditText) buyer_info.findViewById(R.id.BUYERID);
            final EditText vehicleNumber = (EditText) buyer_info.findViewById(R.id.VehicleNumber);
            final CheckBox detailsBox = (CheckBox) buyer_info.findViewById(R.id.details);
            View.OnFocusChangeListener lOCL = new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    buyeridE.setText("");
                }
            };
            View.OnFocusChangeListener buyeridOCL = new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    lovenumberE.setText("");
                }
            };
            lovenumberE.setOnFocusChangeListener(lOCL);
            buyeridE.setOnFocusChangeListener(buyeridOCL);

            vehicleNumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String str = s.toString();
                    if (!str.matches(".{0}|\\/.{0,7}|[A-Za-z]{0,2}[0-9]{0,14}")) {
                        s.delete(s.length() - 1, s.length());
                    }

                }
            });
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.dialog);
            builder.setView(buyer_info);

            TextView title = new TextView(context);
            title.setText("$:" + money + (give_change.length > 0 ? "  找:" + give_change[0] : ""));
            title.setGravity(Gravity.CENTER);
            title.setTextSize(30);
            title.setBackgroundColor(Color.GRAY);
            title.setTextColor(Color.WHITE);


            builder.setCancelable(false);
            builder.setCustomTitle(title);
            builder.setPositiveButton("結帳", null);
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Lock=!Lock;
                }
            });


            final AlertDialog dialog = builder.create();

            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.gravity = Gravity.CENTER | Gravity.TOP;
            dialog.getWindow().setAttributes(lp);
            dialog.show();
            try {
                Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String BUYERID = buyeridE.getText().toString();
                        String VehicleNumber = vehicleNumber.getText().toString();
                        String LoveNumber = lovenumberE.getText().toString();
                        boolean details = detailsBox.isChecked();
                        detailsBox.setChecked(false);
                        boolean stop = true;
                        if (!BUYERID.equals("") && !checkID(BUYERID)) {
                            stop = false;
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("統編驗證失敗");
                            builder.setPositiveButton("確認", null);
                            AlertDialog ad = builder.create();
                            ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                            ad.show();

                        }
                        if (!VehicleNumber.equals("") && !VehicleNumber.matches("\\/.{7}|[A-Za-z]{2}[0-9]{14}")) {
                            stop = false;
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("載具驗證失敗");
                            builder.setPositiveButton("確認", null);
                            AlertDialog ad = builder.create();
                            ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                            ad.show();


                        }
                        if (!LoveNumber.equals("") && !LoveNumber.matches("\\d{3,7}")) {
                            stop = false;
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("愛心碼驗證失敗");
                            builder.setPositiveButton("確認", null);
                            AlertDialog ad = builder.create();
                            ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                            ad.show();
                        }

                        if (stop) {
                            dialog.dismiss();
                            if(ConstructionInfo.cipher==null)
                            {ConstructionInfo.cipher();}

                            SimpleDateFormat sdf_yyymm1=new SimpleDateFormat("yyyy");
                            SimpleDateFormat sdf_yyymm2=new SimpleDateFormat("MM");
                            String yyy=""+(Integer.parseInt( sdf_yyymm1.format(new Date()) )-1911);
                            int MMi=Integer.parseInt( sdf_yyymm2.format(new Date()) );
                            String MMs=String.format("%02d",MMi%2==0?MMi:MMi+1);
                            SQLite sqLite = new SQLite(context);
                            final SQLiteDatabase db = sqLite.getReadableDatabase();

                            if(type.equals("手動")){
                                Cursor count=db.rawQuery("Select COUNT(Random) From history Where TAXMONTH='"+yyy+MMs+"' ",null);
                                count.moveToFirst();
                                No=(count.getInt(0)+1)+"";
                            }
                            Module.saveTransactionToDB(context,money,BUYERID,VehicleNumber,LoveNumber,serial,No,details,type);

                            Cursor invoicenumC=db.rawQuery("Select INVOICENUMBER From history Where RANDOM IS NOT NULL AND PRINTYN IS NULL Order By _id Desc",null);
                            if(invoicenumC.moveToFirst()) {
                                Module.printIvoice(context, "印發票", invoicenumC.getString(0));
                                if (details || !BUYERID.equals("") || !VehicleNumber.equals("") || !LoveNumber.equals("")) {
                                   Module.printIvoice(context, "明細", invoicenumC.getString(0));
                                }
                                Module.printIvoice(context, "開錢櫃", invoicenumC.getString(0));
                            }
                            Lock=!Lock;


                        }
                    }

                });

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(30);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.rgb(129, 216, 208));
            } catch (Exception e) {
                e = e;
            }

            try {
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Lock=!Lock;
                    }
                });
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(30);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.rgb(129, 216, 208));


            } catch (Exception e) {
                e = e;
            }
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("警告");
            builder.setPositiveButton("多筆請求", null);
            AlertDialog ad = builder.create();
            ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            ad.show();
        }
    }
    public static boolean checkID(String id){
        if (id.length()!=8)
            return false;
        try{
            int v[] = {1,2,1,2,1,2,4,1};
            int temp=0;
            int sum=0;
            for (int i=0;i<v.length;i++){
                temp= Integer.parseInt(String.valueOf(id.charAt(i))) * v[i];
                sum = sum+temp/10+temp%10;
            }
            if (sum % 10 ==0)
                return true;
            else
                return false;
        }catch(Exception e){
            return false;
        }
    }

}
