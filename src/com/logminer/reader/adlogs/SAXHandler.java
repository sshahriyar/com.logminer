/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logminer.reader.adlogs;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Syed Shariyar Murtaza 
 */
/**
 * The Handler for SAX Events.
 */
class SAXHandler extends DefaultHandler {

 // List<NVDNodes> empList = new ArrayList<>();
  private NVDNodes vuln = null;
  private String content = null;
  Boolean isCorrectVal=false;
  
  public SAXHandler(){
 
  }
  
  @Override
  //Triggered when the start of tag is found.
  public void startElement(String uri, String localName, 
                           String qName, Attributes attributes) 
                           throws SAXException {

    switch(qName){
      //Create a new Employee object when the start tag is found
      case "field":
        //vuln = new NVDNodes();
         String att=attributes.getValue("k");
         if (att!=null && (att.equalsIgnoreCase("OBJECT") || att.equalsIgnoreCase("SUBJECT") ||
        	  att.equalsIgnoreCase("TS_CREATED"))){
        	 		isCorrectVal=true;
        	 		System.out.print(att +": ");
         }
         
        	 
        break;
          
      //case "":
        //  vuln.cwe=attributes.getValue("id");
          //break;
    }
  }

 
  @Override

  public void endElement(String uri, String localName, 
                         String qName) throws SAXException {
   switch(qName){
     //Add the employee to list once end tag is found
     case "text":
       if (isCorrectVal){
    	   isCorrectVal=false;
    	   System.out.println(content);
       }
     break;
     
   }
  }

  
  @Override
  public void characters(char[] ch, int start, int length) 
          throws SAXException {
    content = String.copyValueOf(ch, start, length).trim();
  }




}
