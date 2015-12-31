/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logminer.reader.adlogs;

/**
 *
 * @author shary
 */

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

public class NVDParser {

    
  public void executeParser(String file) throws ParserConfigurationException, SAXException, IOException{
      
    SAXParserFactory parserFactor = SAXParserFactory.newInstance();
    SAXParser parser = parserFactor.newSAXParser();
    SAXHandler handler = new SAXHandler();
  
    parser.parse(new File(file), handler);

  }
  
  public static void main(String[] args)  {
      //give dbname and tables
     String file="/home/t909801/logs/ad-auth-xml.xml";
     
     try {
         NVDParser nvd= new NVDParser();
       
         nvd.executeParser(file);
     } catch (Exception ex) {
         Logger.getLogger(NVDParser.class.getName()).log(Level.SEVERE, null, ex);
         ex.printStackTrace();
     }
      
   
  }
  
}
