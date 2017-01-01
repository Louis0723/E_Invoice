package com.lursun.einvoice;

import java.util.ArrayList;

/**
 * Created by admin on 2016/9/24.
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;


public class MakeXml {
    private MakeXml next=null;
    private String tag=null;
    private String value=null;
    private MakeXml father=null;
    private MakeXml firstchild=null;
    private ArrayList<MakeXml> children=new ArrayList<MakeXml>();
    public MakeXml(String tag){

        this.tag=tag;
    }
    public MakeXml(String tag, String value){

        this.tag=tag;
        this.value=value;
    }
    public void setFather(MakeXml xml){
        father=xml;
    }
    public MakeXml setChild(MakeXml xml){
        firstchild=xml;
        firstchild.setFather(this);
        children.add(firstchild);
        return xml;
    }
    public MakeXml setChild(String tag){
        firstchild=new MakeXml(tag);
        firstchild.setFather(this);
        children.add(firstchild);
        return firstchild;
    }
    public MakeXml setChild(String tag, String value){
        return setChild(new MakeXml(tag,value));
    }
    public MakeXml nextBrother(String tag){
        next=new MakeXml(tag);
        next.setFather(father);
        father.children.add(next);
        return next;
    }
    public MakeXml nextBrother(String tag, String value){
        next=new MakeXml(tag);
        next.setFather(father);
        father.children.add(next);
        next.setValue(value);
        return next;
    }
    public MakeXml setValue(String value){
        this.value=value;
        return this;
    }
    public MakeXml setValue(String tag, String value){
        setChild(new MakeXml(tag,value));
        return this;
    }
    public MakeXml setValue(MakeXml value){
        setChild(value);
        return setChild(value);
    }
    public MakeXml getTop(){
        MakeXml temp=this;
        while(temp.father!=null){
            temp=temp.father;
        }
        return temp;
    };
    public MakeXml getFather()
    {

            return father;

    };
    public String getXmlData(){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+getXmlData(0);
    }
    private String getXmlData(int i){
        MakeXml xml=this;
        String temp="";

        while(xml!=null) {
            temp += i!=0? String.format("%" + (i * 4) + "s", ""):"";
            switch ((xml.value != null ? 1 : 0) + (xml.firstchild != null ? 2 : 0)) {
                case 0:
                    temp += "<" + xml.tag + " />\n";
                    break;
                case 1:
                    temp += "<" + xml.tag + ">";
                    temp += xml.value;
                    temp += "</" + xml.tag + ">\n";
                    break;
                case 2:
                    temp += "<" + xml.tag + ">\n";
                    temp += xml.firstchild.getXmlData(i + 1);
                    temp += i!=0? String.format("%" + (i * 4) + "s", ""):"";
                    temp += "</" + xml.tag + ">\n";
                    break;
            }
            xml=xml.next;

        }
        return temp;
    }

}
