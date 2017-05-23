package com.mycompany.xmlbitmap;

import java.io.FileInputStream;
import java.io.FileWriter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;
import java.util.HashMap;

public class XMLBitmap {
    public static void main(String[] args){
        System.setProperty("entityExpansionLimit", "10000000");
        
        String XMLFileName = "C:/DBLP/dblp-2016-11-02.xml"; //Name of xml file parameter
        String csvFileName = "C:/Users/User/Desktop/dblpBitmap.csv"; //Name of csv file
        double ratio = 1.00; //Ratio parameter
        
        try{
            XMLStreamReader xmlr = XMLInputFactory.newInstance().createXMLStreamReader(XMLFileName, new FileInputStream(XMLFileName));
            
            int nBegins = 0;
            ArrayList<java.lang.Integer> startTags = new ArrayList();
            
            BitSet isLeaf = new BitSet();
            
            while (xmlr.hasNext()){
                xmlr.next();
                
                if (xmlr.isStartElement()){
                    nBegins++;
                    startTags.add(nBegins);
                }
                
                if (xmlr.isCharacters() && xmlr.getText().trim().length()>0){
                    isLeaf.set(startTags.get(startTags.size()-1));
                }
                
                if (xmlr.isEndElement()){
                    startTags.remove(startTags.size()-1);
                }  
            }
            
            
            Map<String, java.lang.Integer> howManyMustBe = new HashMap();
            nBegins = 0;
            int lvl = 0;
            int k;
            int inside_lvl;
            HashSet<String> all_attributes = new HashSet();
            
            xmlr = XMLInputFactory.newInstance().createXMLStreamReader(XMLFileName, new FileInputStream(XMLFileName));
            
            while (xmlr.hasNext()){
                xmlr.next();
                
                if (xmlr.isStartElement()){
                    nBegins++;
                    if (!isLeaf.get(nBegins)){
                        if (lvl>1)
                            all_attributes.add(xmlr.getLocalName()+"_id");
                        for (int i = 0; i < xmlr.getAttributeCount();i++)
                            all_attributes.add(xmlr.getAttributeLocalName(i));
                        if (!howManyMustBe.keySet().contains(xmlr.getLocalName()))
                            howManyMustBe.put(xmlr.getLocalName(), 1);
                        else {
                            k = howManyMustBe.get(xmlr.getLocalName());
                            howManyMustBe.put(xmlr.getLocalName(), k+1);
                        }
                    }
                    else{
                        all_attributes.add(xmlr.getLocalName());
                        inside_lvl = 1;
                        do{
                            xmlr.next();
                            if (xmlr.isStartElement()){
                                inside_lvl++;
                                nBegins++;
                            }
                            if (xmlr.isEndElement())
                                inside_lvl--;
                        }
                        while (!(xmlr.isEndElement() && inside_lvl==0));
                    }
                    lvl++;
                }
                
                if (xmlr.isEndElement()){
                    lvl--;     
                }
                
            }
            
            ArrayList<String> attributes = new ArrayList(all_attributes);
            
            for (String str: howManyMustBe.keySet()){
                k = howManyMustBe.get(str);
                howManyMustBe.put(str, (int)(k*ratio));
            }
            
            Map<String,java.lang.Integer> howManyThereAre = new HashMap();
            
            for (String str: howManyMustBe.keySet())
                howManyThereAre.put(str, 0);
            
            howManyMustBe.put("mastersthesis", 0);
            
            
            int bitmap_length = attributes.size();
            BitSet bitmap = new BitSet(bitmap_length);
            nBegins = 0;
            lvl = 0;
            ArrayList<String> attrs_stack = new ArrayList();
            startTags.clear();
            
            int L;
            int until;
            
            int progress = 0;
            
            FileWriter fw = new FileWriter(csvFileName);
            
            xmlr = XMLInputFactory.newInstance().createXMLStreamReader(XMLFileName, new FileInputStream(XMLFileName));
            
            while (xmlr.hasNext()){
                xmlr.next();
                
                if (xmlr.isStartElement()){
                    nBegins++;
                    startTags.add(nBegins);
                    if (!isLeaf.get(nBegins)){
                        if (lvl>1)
                            attrs_stack.add(xmlr.getLocalName()+"_id");
                        attrs_stack.add("$");
                        for (int i=0;i<xmlr.getAttributeCount();i++)
                            attrs_stack.add(xmlr.getAttributeLocalName(i));
                    }
                    else{
                        attrs_stack.add(xmlr.getLocalName());
                        inside_lvl = 1;
                        do{
                            xmlr.next();
                            if (xmlr.isStartElement()){
                                nBegins++;
                                inside_lvl++;
                            }
                            if (xmlr.isEndElement())
                                inside_lvl--;
                        }
                        while(!(xmlr.isEndElement() && inside_lvl==0));
                    }
                    lvl++;
                }
                
                if (xmlr.isEndElement()){
                    if (!isLeaf.get(startTags.get(startTags.size()-1)) && lvl!=1){
                        bitmap.clear();
                        L = attrs_stack.size();
                        until = attrs_stack.lastIndexOf("$");
                        for (int i = L-1; i>until; i--)
                            bitmap.set(attributes.indexOf(attrs_stack.remove(i)));
                        attrs_stack.remove(until);
                        if (howManyThereAre.get(xmlr.getLocalName()) < howManyMustBe.get(xmlr.getLocalName())){
                            k = howManyThereAre.get(xmlr.getLocalName());
                            howManyThereAre.put(xmlr.getLocalName(), k+1);
                            bitmapToFile(bitmap, bitmap_length, fw);
                            progress++;
                            System.out.println(progress);
                        }
                    }
                    startTags.remove(startTags.size()-1);
                    lvl--;
                }
            }
            
            fw.flush();
            fw.close();  
            
        }
        catch(Exception e){
            e.printStackTrace();
        }
       
    }
    
    public static void bitmapToFile(BitSet bitmap, int bitmap_size, FileWriter file_writer){
        
        String to_be_inserted;
        
        if (bitmap.get(0))
            to_be_inserted = "1";
        else to_be_inserted = "0";
                       
        for (int i=1;i<bitmap_size;i++){
            to_be_inserted = to_be_inserted + ",";
            if (bitmap.get(i))
                to_be_inserted = to_be_inserted + "1";
            else to_be_inserted = to_be_inserted + "0";
        }
        
        try{
            file_writer.append(to_be_inserted);
            file_writer.append("\n");
        } catch(Exception e){
            System.out.println("Error in CsvFileWriter!");
        }
    }
    
}