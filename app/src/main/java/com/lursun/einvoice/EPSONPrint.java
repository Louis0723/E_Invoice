package com.lursun.einvoice;


import java.nio.ByteBuffer;


public class EPSONPrint
{
    byte[] ESC_at={(byte) 0x1B,'@'};
    byte[] ESCL={(byte) 0x1B,'L'};
    byte[] GSP={(byte) 0x1D,'P',0x00,(byte)0xc8};
    public byte[] ESCW={(byte) 0x1B,'W',0x00,0x00,0x00,0x00,(byte) 0xC8,(byte)0x01,(byte) 0xCB,(byte) 0x02};
    byte[] ESCT={(byte)0x1B,'T',(byte)0x00};
    byte[] FF={(byte)0x0C};
    byte[] GSb={(byte)0x1D,'b',0x01};
    byte[] GSB={(byte)0x1D,'B',0x00};
    byte[] GS_e0={(byte)0x1D,'!',0x00};
    byte[] GS_e17={(byte)0x1D,'!',0x11};
    byte[] GSH={0x1D,'H',0x00};
    byte[] GSh={0x1D,'h',0x40};
    byte[] GSw={0x1D,'w',0x01};
    byte[] GSk={0x1D,'k',0x45,0x13};
    byte[] GS_k65={0x1D,'(','k',0x04,0x00,0x31,0x41,0x32,0x00};//qrcode model
    public byte[] GS_k67={0x1D,'(','k',0x03,0x00,0x31,0x43,0x03};//qrcoe size
    byte[] GS_k69={0x1D,'(','k',0x03,0x00,0x31,0x45,0x31};//qrcode level
    byte[] GS_k80_dataHeader={0x1D,'(','k'};
    byte[] GS_k80_datafoot={0x31,0x50,0x30};
    byte[] GS_k81={0x1D,'(','k',0x03,0x00,0x31,0x51,0x30};
    byte[] GS_Dollar={0x1D,'$'};
    byte[] ESC_Dollar={(byte)0x1B,'$'};
    byte[] GSV={0x1D,'V',(byte)0x01,0x00};
    byte[] DLEDC4={0x10,0x14,0x01,0x00,0x01};
    byte[] US={0x1F,0x1B,0x1F,0x13,0x14,0x01};
    public byte[] paper={};
    public EPSONPrint() {
    	
    }
    public void initPrint(){

        paper=link(ESC_at,ESCL,GSP,ESCW,ESCT);
    }
    public void Big(String s){
        try{
            s=s+'\n';
            paper=link(paper,GS_e17,s.getBytes("BIG5"),GS_e0);
        }catch (Exception e){}
    }
    public void Nomal(String s){
        try{
            s=s+'\n';
            paper=link(paper,GS_e0,s.getBytes("BIG5"),GS_e0);
        }catch (Exception e){}
    }
    public void Byte_arr(byte[] bytes){
        paper=link(paper,bytes);
    }
    public void Barcode(String s){
        paper=link(paper,GSH,GSh,GSw,GSk,s.getBytes(),GS_k81);
    }
    public void QRcode(String s){
        byte[] data=fillup(s);
        byte[] pL={(byte) ((data.length+3) % 256)};
        byte[] pH={(byte) ((data.length+3) / 256)};
        paper=link(paper,GS_k65,GS_k67,GS_k69,GS_k80_dataHeader,pL,pH,GS_k80_datafoot,fillup(s),GS_k81);
    }
    public void QRcode2(String s){
        byte[] data=fillup2(s);
        byte[] pL={(byte) ((data.length+3) % 256)};
        byte[] pH={(byte) ((data.length+3) / 256)};
        paper=link(paper,GS_k65,GS_k67,GS_k69,GS_k80_dataHeader,pL,pH,GS_k80_datafoot,fillup2(s),GS_k81);
    }
    public void SetXY(int x,int y){
        x*=8;y*=8;
        byte[] y_byte1={(byte)(y/256)};
        byte[] y_byte2={(byte)(y%256)};
        byte[] x_byte1={(byte)(x/256)};
        byte[] x_byte2={(byte)(x%256)};
        paper=link(paper,GS_Dollar,y_byte2,y_byte1 ,ESC_Dollar ,x_byte2,x_byte1 );
    }
    public void Cut(){
        paper=link(paper,GSV,ESC_at);
    }
    public void Print(){
        paper=link(paper,FF);
    }
    public void Open(){
        paper=link(paper,DLEDC4);
    }
    public byte[] fillup(String s){
        int SIZE=170;
        byte[] temp=new byte[SIZE];
        byte[] bytes= s.getBytes();
        for(int i = 0 ;i<SIZE;i++){
            temp[i]=0x20;
        }
        if(bytes.length<SIZE){
            for(int i=0;i<bytes.length;i++){
                temp[i]=bytes[i];
            }
        }else temp=bytes;
        return temp;
    };
    public byte[] fillup2(String s){
        int SIZE=150;
        byte[] temp=new byte[SIZE];
        byte[] bytes= s.getBytes();
        for(int i = 0 ;i<SIZE;i++){
            temp[i]=0x20;
        }
        if(bytes.length<SIZE){
            for(int i=0;i<bytes.length;i++){
                temp[i]=bytes[i];
            }
        }else temp=bytes;
        return temp;
    };
    public byte[] getPaper(){
        return paper;
    };
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
