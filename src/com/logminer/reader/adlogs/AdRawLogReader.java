/*********************************************************************************************
 * Copyright (c) 2014-2015  Software Behaviour Analysis Lab, Concordia University, Montreal, Canada
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of Eclipse Public License v1.0 License which
 * accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Syed Shariyar Murtaza -- Initial design and implementation
 **********************************************************************************************/
package com.logminer.reader.adlogs;

/**
 * get data first from this splunk query (change dates for every day):
 * index=argus source=/argus/argus/telus/2016/01/29/ad/abads016/raw.log.gz 
| rex ".*\w{32}\s(?<date>\w{3}\s\d{2}\s\d{2}\:\d{2}\:\d{2}\s\d{4})\s+(?<eid>\d{3,4}).*Account Name:\s+(?<account>[\w-._@$]+)"| table date eid account
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

//import logminer.exception.TotalADSGeneralException;









import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.logminer.exception.TotalADSReaderException;
import com.logminer.reader.ILogIterator;
import com.logminer.reader.ILogTypeReader;
//import logminer.reader.TraceTypeFactory;
/**
 * This class reads a text file and returns each line as an fEvent.
 *
 * @author <p> Syed Shariyar Murtaza justsshary@hotmail.com </p>
 *
 */
public class AdRawLogReader implements ILogTypeReader {
	//---------------------------------------------------------------------------
	//Inner class: Implements the iterator to iterate through the text file
	//---------------------------------------------------------------------------
	private class AdLogIterator implements ILogIterator{
	
		private XMLInputFactory xmlInFactory;
		private XMLStreamReader xmlStrReader;
		private String fEvent=""; //$NON-NLS-1$
		private Boolean fIsClose=false;
		

		/**
		 * Constructor
		 * @param file File object
		 * @throws FileNotFoundException An exception about file
		 * @throws XMLStreamException  An exception about XML
		 */
		public AdLogIterator(File  file) throws FileNotFoundException, XMLStreamException{
			xmlInFactory= XMLInputFactory.newInstance();
			xmlStrReader= xmlInFactory.createXMLStreamReader(new FileReader(file));
			
		}

		/*
		 *Advances the iterator
		 */
		@Override
		public boolean advance() throws TotalADSReaderException  {
		   boolean isAdvance=false;
		   boolean isField=false, isText=false, isResult=false;
		   fEvent="";
		   try {
			   while (xmlStrReader.hasNext()){
				   isAdvance=true;
				   int event=xmlStrReader.next();
				
				   switch (event){
				   		case XMLStreamConstants.START_ELEMENT:
				   			
				   			 if (xmlStrReader.getLocalName().equals("field")){ //look for a  field element
				   				String att=xmlStrReader.getAttributeValue(0);
				   			    if (att!=null){
				   			    	if (att.equalsIgnoreCase("eid") || att.equalsIgnoreCase("account")) {
				   			     		isField=true;
				   	        	 		if (!fEvent.isEmpty())
				   	        	 			fEvent+=":";
				   			    	}
				   			    	
				   			    }
				   	        }
				   			else if (xmlStrReader.getLocalName().equals("text") &&  (isField)){ //look for a  field element
				   				 		isText=true;
				   	        }
				   		break;
				   		case XMLStreamConstants.CHARACTERS:
				   			
				   			 if(isText && isField){
		                    	fEvent+=xmlStrReader.getText();
		                        isText=false;
		                        isField=false;
		                    }
		                break;
				   		case XMLStreamConstants.END_ELEMENT:
				   			if (xmlStrReader.getLocalName().equals("result")){
				   				isResult=true; // end of result tag reached
				   				
				   			}
				   		break;

				   }//end switch case
				   
				   if (isResult)
					   break;
				   
				   
			   }
			   
				


			} catch ( XMLStreamException e) {

				throw new TotalADSReaderException(e.getMessage());
			}
		   return isAdvance;
		}

		/*
		 * Returns the Current fEvent
		 */
		@Override
		public String getCurrentEvent() {
			
			return fEvent;
		}

		/**
		 * Closes the iterator
		 * @throws TotalADSReaderException An exception about reading errors
		 */
		@Override
		public void close() throws TotalADSReaderException {
			try {
				if (!fIsClose) {
                    xmlStrReader.close();
                    fIsClose=true;
                    
                }
			} catch ( XMLStreamException e) {

				throw new TotalADSReaderException(e.getMessage());
			}

		}


	}
	//--------------------------------------------------------------------------------
	// Inner class ends
	//--------------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public AdRawLogReader() {

	}

	@Override
	public ILogTypeReader createInstance(){
		return new AdRawLogReader();
	}


	@Override
	public String getName() {

		return Messages.TextLineTraceReader_TextReaderName;
	}

	/**
    * Returns the acronym of the text reader
    */
    @Override
    public String getAcronym(){

    	return "ADLog"; //$NON-NLS-1$
    }

    /**
     * Returns the trace iterator
     * @throws  
     */
	@Override
	public ILogIterator getLogIterator(File file) throws TotalADSReaderException {

		if (file==null) {
            throw new TotalADSReaderException(Messages.TextLineTraceReader_NoNull);
        }

		try {

			AdLogIterator adIterator=new AdLogIterator(file);
			return adIterator;

		} catch (FileNotFoundException | XMLStreamException e) {
			throw new TotalADSReaderException(e.getMessage());
		}
	}

	/**
	 * Registers Itself with the Trace Type Reader
	 * @throws TotalADSGeneralException A general exception from TotalADS
	 */
	// public static void registerTraceTypeReader() throws TotalADSGeneralException{
	//    	TraceTypeFactory trcTypFactory=TraceTypeFactory.getInstance();
	//    	TextLineTraceReader textFileReader=new TextLineTraceReader();
	 //   	trcTypFactory.registerTraceReaderWithFactory(textFileReader.getName(), textFileReader);
	  //  }
}
