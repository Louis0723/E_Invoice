package lursun.einvoice;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2016/9/24.
 */
public class SendData {
    public HashMap<String,String> sendValue;
    public HashMap<String,String> getValue;
    public MakeXml xml;
    static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public String getValue(String s, String key){
        key=key.toUpperCase();
        Pattern p= Pattern.compile("(?<=<"+key+">)[\\w\\W]+?(?=</"+key+">)");
        Matcher m=p.matcher(s);
        return m.find()?m.group():null;
    }
    private String send(String data) throws Exception
    {

            Socket socket = new Socket("xml.551.com.tw", 80);
            OutputStream os = socket.getOutputStream();
            InputStreamReader isr = new InputStreamReader(socket.getInputStream());

            data="POST / HTTP/1.1\n" +
                    "Host: xml.551.com.tw\n" +
                    "User-Agent: curl/7.50.2\n" +
                    "Accept: */*\n" +
                    "Content-Length: "+data.getBytes().length+"\n" +
                    "Content-Type: application/x-www-form-urlencoded\n\n"+data;
            os.write(data.getBytes());
            char[] cs = new char[2000];
            isr.read(cs);
            os.close();
            String s = new String(cs);
            socket.close();
            return s;


    }
    public HashMap<String,String> getIssue()throws Exception
    {
        HashMap<String,String> hm=new HashMap<String,String>();
        xml=new MakeXml("INDEX").
                setChild("FUNCTIONCODE","A01").
                nextBrother("SELLERID",sendValue.get("SELLERID"))./*從資料庫取*/
                nextBrother("POSID",sendValue.get("POSID"))./*從資料庫取*/
                nextBrother("POSSN",sendValue.get("POSSN"))./*從資料庫取*/
                nextBrother("SYSTIME", sdf.format(new Date())).
                getTop();
        String data=xml.getXmlData();
        String request=send(data);
        String[] key_array={
                "REPLY","MESSAGE","SYSTIME","TAXMONTH","TYPE","INVOICEHEADER","INVOICESTART","INVOICEEND"
        };
        for(String key:key_array){
            hm.put(key,getValue(request,key));
        }
        return hm;
    }

    public HashMap<String,String> getNextIssue ()throws Exception
    {
        HashMap<String,String> hm=new HashMap<String,String>();

        xml=new MakeXml("INDEX").
                setChild("FUNCTIONCODE","C01").
                nextBrother("SELLERID",sendValue.get("SELLERID"))./*從資料庫取*/
                nextBrother("POSID",sendValue.get("POSID"))./*從資料庫取*/
                nextBrother("POSSN",sendValue.get("POSSN"))./*從資料庫取*/
                nextBrother("SYSTIME", sdf.format(new Date())).
                getTop();
        String data=xml.getXmlData();
        String request=send(data);
        String[] key_array={
                "REPLY","MESSAGE","SYSTIME","TAXMONTH","TYPE","INVOICEHEADER","INVOICESTART","INVOICEEND"
        };
        for(String key:key_array){
            hm.put(key,getValue(request,key));
        }
        return hm;
    }
    public HashMap<String,String> getSystime()throws Exception
    {
        HashMap<String,String> hm=new HashMap<String,String>();
        xml=new MakeXml("INDEX").
                setChild("FUNCTIONCODE","Y01").
                nextBrother("SELLERID",sendValue.get("SELLERID"))./*從資料庫取*/
                nextBrother("POSID",sendValue.get("POSID"))./*從資料庫取*/
                nextBrother("POSSN",sendValue.get("POSSN"))./*從資料庫取*/
                nextBrother("SYSTIME", sdf.format(new Date())).
                getTop();
        String data=xml.getXmlData();
        String request=send(data);
        String[] key_array={
                "REPLY","MESSAGE","SYSTIME"
        };
        for(String key:key_array){
            hm.put(key,getValue(request,key));
        }
        return hm;
    }
    public HashMap<String,String> sendTransaction()throws Exception
    {
        HashMap<String,String> hm=new HashMap<String,String>();
        xml=new MakeXml("Invoice").
                setChild("A1","C0401").
                nextBrother("A2",sendValue.get("INVOICENUMBER"))./*發票號碼*//*從資料庫取*/
                nextBrother("A3",sendValue.get("DAY"))./*開立日期*//*存進資料庫 回傳優先*/
                nextBrother("A4",sendValue.get("TIME"))./*開立時間*//*存進資料庫*/
                nextBrother("A5",sendValue.get("BUYERID"))./*買方統編*//*使用者輸入*//*存進資料庫*/
                nextBrother("A6","0000")./*買方名稱 不知道幹嘛的 沒有就填四個0*//*存進資料庫*/
                nextBrother("A19","2013-02-27")./*規定寫死的*/
                nextBrother("A20","資國")./*規定寫死的*/
                nextBrother("A21","1020001054")./*規定寫死的*/
                nextBrother("A22","07")./*發票類別*/
                nextBrother("A24",""+sendValue.get("LOVEYN"))./*捐贈註記 0/1*/
                nextBrother("A28",sendValue.get("PRINTYN"));/*紙本電子發票已列印註記 Y/N*/
                if(sendValue.containsKey("LOVENUMBER")){
                    xml=xml.nextBrother("A29",sendValue.get("LOVENUMBER"));
                }
                xml=xml.nextBrother("A30",sendValue.get("Random"));/*隨機碼*/

                if(sendValue.containsKey("VehicleNumber")) {
                    if (sendValue.get("VehicleNumber").indexOf('/') != -1){
                        xml=xml.nextBrother("A25", "3J0002");
                    }else {
                        xml=xml.nextBrother("A25","CQ0001");
                    }
                    xml=xml.nextBrother("A26",sendValue.get("VehicleNumber"));
                    xml=xml.nextBrother("A27",sendValue.get("VehicleNumber"));
                }
                int sub=0; int i=0;
                for(String B:sendValue.get("B").split(";;") ){
                    String b[]={"","",""};

                    for( String Bb:B.split(",,")){
                        b[i%3]=Bb;
                        i++;
                    }
                    int money= Integer.parseInt(b[1])* Integer.parseInt(b[2]);
                    sub+=money;
                    xml=xml.nextBrother("B").
                            setChild("B1",""+(i/3))./*商品項目資料 範例填1 不知道有沒有其他值*/
                            nextBrother("B2",b[0])./*品項*/
                            nextBrother("B3",b[1])./*數量*/
                            nextBrother("B5",b[2])./*單價*/
                            nextBrother("B6",""+money)./*金額*/
                            nextBrother("B7",""+(i/3))./*發票商品序號*/
                            getFather();
                }

                xml=xml.nextBrother("C1",""+ Math.round (!sendValue.get("BUYERID").equals("0000000000")?(double)sub/1.05:sub))./*應稅銷售合計*/ /* 總額*20/21 */
                nextBrother("C2","0")./*免稅銷售合計*/
                nextBrother("C3","0")./*零稅率銷售合計*/
                nextBrother("C4","1")./*課稅別*/
                nextBrother("C5","0.05")./*稅率*/
                nextBrother("C6",""+ Math.round (!sendValue.get("BUYERID").equals("0000000000")?(double)sub/21:0))./*營業稅額*/ /*總額/21 */
                nextBrother("C7",""+sub);/*總計 含稅總金額*/ /*總額*/
                if(sendValue.containsKey("PS")&&sendValue.get("PS")!=null) {
                    xml = xml.
                            nextBrother("C12", "" + sendValue.get("PS")).
                            nextBrother("C13", "" + sendValue.get("PS"));
                }
                xml=xml.
                nextBrother("D1",sendValue.get("SELLERID"))./*賣方統編*/ /*從資料庫取*/
                nextBrother("D2",sendValue.get("POSSN"))./*通道金鑰*/ /*從資料庫取*/
                nextBrother("D3",sendValue.get("POSID"))./*pos機*/ /*從資料庫取*/
                nextBrother("D4",sendValue.get("MakeTime"))./*XML 產生時間*//*存進資料庫*/
                getTop();
        String data=xml.getXmlData();
        String request=send(data);
        String[] key_array={
                "REPLY","MESSAGE","SYSTIME"
        };
        for(String key:key_array){
            hm.put(key,getValue(request,key));
        }

        return hm;
    }

    public HashMap<String,String> invalidInvoice()throws Exception
    {
        HashMap<String,String> hm=new HashMap<String,String>();


        hm.put("CANCEL_REASON",sendValue.get("CANCEL_REASON"));
        xml=new MakeXml("Invoice").
                setChild("INVOICE_CODE","C0501").
                nextBrother("POSSN",sendValue.get("POSSN"))./*從資料庫取*/
                nextBrother("POSID",sendValue.get("POSID"))./*從資料庫取*/
                nextBrother("INVOICE_NUMBER",sendValue.get("INVOICENUMBER"))./*從資料庫取*/
                nextBrother("INVOICE_DATE",sendValue.get("DAY"))./*從資料庫取*/
                nextBrother("BUYERID",sendValue.get("BUYERID"))./*從資料庫取*/
                nextBrother("SELLERID",sendValue.get("SELLERID"))./*從資料庫取*/
                nextBrother("CANCEL_DATE",sendValue.get("CDAY")).
                nextBrother("CANCEL_TIME",sendValue.get("CTIME")).
                nextBrother("CANCEL_REASON",sendValue.get("CANCEL_REASON"))./*使用者輸入*/ /*從資料庫取*/
                nextBrother("SYSTIME",sdf.format(new Date())).
                getTop();
        String data=xml.getXmlData();

        String request=send(data);


        String[] key_array={
                "REPLY","MESSAGE","SYSTIME","ERROR_CODE"
        };
        for(String key:key_array){
            hm.put(key,getValue(request,key));
        }

        return hm;
    }
    SendData(HashMap _sendValue){
        sendValue=_sendValue;

    }



}
