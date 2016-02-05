package com.logminer.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.google.gson.JsonObject;
import com.logminer.exception.TotalADSDBMSException;
import com.logminer.exception.TotalADSGeneralException;
import com.logminer.exception.TotalADSReaderException;
import com.logminer.outstream.ILogOutObserver;
import com.logminer.outstream.LogOutConsole;
import com.logminer.reader.ILogIterator;
import com.logminer.reader.ILogTypeReader;
import com.logminer.reader.adlogs.AdAuthLogReader;
import com.logminer.reader.adlogs.AdRawLogReader;
import com.logminer.reader.adlogs.TextLineTraceReader;
import com.logminer.sequencematcher.SequenceMatching;

public class LogMiner {
	private HashMap<String,ArrayList<String>> commonPattern;
	private HashMap<String,SequenceMatching> resourcePattern;
	
	
	private String []logTypes={"RawADLogs","AuthADLogs"};
	private ArrayList<String> sortedPatterns;
	// Inner class
	private class OutputClass implements ILogOutObserver {			
		private BufferedWriter outFile;
		/**
		 * Constructor
		 * @throws IOException 
		 */
		public  OutputClass() throws IOException{
		
				outFile=new BufferedWriter(new FileWriter("/home/t909801/logs/output.txt"));
		
		}
		
		@Override
		public void updateOutput(String message) {
			try {
				System.out.print(message);
				outFile.write(message);
				outFile.flush();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		public void dispose() throws IOException{
			
				outFile.close();
			
		}
	}
	
	/**
	 * 
	 */
	public  void mineResourcePatterns(File file, ILogTypeReader logFile, SequenceMatching  seq,
			Boolean isLastTrace, ILogOutObserver out){
		
		
		
		try(ILogIterator logIterator= logFile.getLogIterator( file)){
		
			
			LogOutConsole outPut= new LogOutConsole();
		 	outPut.addObserver(out);
		
		 	
	        
	        //seq= new SequenceMatching();
			seq.adjustSettings(5, 0);
			//seq.train(logIterator, isLastTrace, "", null, outPut);
			seq.minePatternsByResources(logIterator, isLastTrace, "", null, outPut,false,false);
			
				
			
			
			
			
		
		} catch (TotalADSReaderException | TotalADSGeneralException | TotalADSDBMSException  ex){
			System.out.println(ex.getMessage());
			
		}
		
	}
	
	/**
	 * 
	 */
	public void mineResourceCommonPattern(File file, ILogTypeReader logFile, SequenceMatching  seq,
			Boolean isLastTrace, ILogOutObserver obsOut){

		
		
		
		try(ILogIterator logIterator= logFile.getLogIterator( file)){
		
			
			
			
			LogOutConsole outPut= new LogOutConsole();
		 	outPut.addObserver(obsOut);
		
		 	
	     
	   		
			
			//seq= new SequenceMatching();
			seq.adjustSettings(5, 0);
			
			seq.minePatternsByResources(logIterator, isLastTrace, "", null, outPut,true,true);
			
			
			
		
		} catch (TotalADSReaderException | TotalADSGeneralException | TotalADSDBMSException ex){
			System.out.println(ex.getMessage());
			
		}
	}
	
	/**
	 * 
	 * @param logType
	 * @return
	 */
	private Boolean validateLogType(String logType){
		Boolean isFlag=false;
		
		for (int i=0; i<logTypes.length; i++)
			if (logTypes[i].equals(logType)){
			  isFlag=true;    break;
			}
		if (isFlag==false)
			return false;
		else 
			return true;
	}
	/**
	 * 
	 * @param key
	 * @return
	 */
	public JsonObject getPatterns(String key, String logType){
		/**
		 * get the log type and the key
		 * return the value for a log type
		 */
		if (!validateLogType(logType))
			return null;
		
		SequenceMatching seqByResource=resourcePattern.get(logType);
		return seqByResource.getPatternTree(key);
	}
	
	/**
	 * 
	 * @return
	 */
	public String[] getPatternKeys(String logType){
		// get the log type
		// return the keys based on a log type
		System.out.println(logType);
		if (!validateLogType(logType))
			return null;
		SequenceMatching seqByResource=resourcePattern.get(logType);
		return seqByResource.getPatternKeys();
	}
	/**
	 * 
	 * @return
	 */
	public String [] getFrequentCommonPatterns(Integer numOfPat, String logType){
		
		if (!validateLogType(logType))
			return null;
		ArrayList<String> sortedP=commonPattern.get(logType);
		
		int size=0;
		
		if (sortedP.size() < numOfPat)
		   size=sortedP.size();
		else
		   size=numOfPat;
		String [] fps=new String[size];
		
		for (int i=0; i<size;i++)
			fps[i]=sortedP.get(i);
		
		return fps;
	}
	
	/**
	 * Gets the type of Logs
	 * @return
	 */
	public String [] getLogTypes(){
		return logTypes;
	}
	
	
	/**
	 * 
	 */
	
	public void initialize(){
		/*loop through all files
		 *  call common resource
		 *  call resources by group
		 *  we have a complete set of patterns
		 * 
		 * */
    	
		ILogOutObserver observerCommmon=msg->{	 
			
            if (msg.startsWith("pat")){
                msg=msg.replaceAll("pat[0-9]+: ", "");
                msg=msg.replaceAll(", ", "-").replaceAll("- -: Count=", ",");
                System.out.print(msg);
                if (msg.contains("\n")){
                	String []msgs=msg.split("\n");
                	for(String str:msgs)
                	  sortedPatterns.add(str);
                }else
                   sortedPatterns.add(msg);
                
                
            }
            									 
		};
		
		commonPattern=new HashMap<String, ArrayList<String>>();
		resourcePattern=new HashMap<String, SequenceMatching>();
		
		
		
		String authDir="/mnt/hgfs/Host/logs/auths/";
		String rawDir="/mnt/hgfs/Host/logs/raw/";
		
		//String authDir="/mnt/hgfs/Host/logs/tmp/auth/";
		//String rawDir="/mnt/hgfs/Host/logs/tmp/raw/";
		try {
			OutputClass observerResource=new OutputClass();
			ILogTypeReader authReader= new AdAuthLogReader();
			subInitialize(authDir, authReader, observerResource,observerCommmon, logTypes[1]);//auth logs
			
			
			/////////////////////////////////// raw
			
			
			ILogTypeReader rawReader= new AdRawLogReader();
			subInitialize(rawDir, rawReader, observerResource,observerCommmon, logTypes[0]);
			
			observerResource.dispose();
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
		
		 
	}
	
	/**
	 * 
	 * @param dir
	 * @param logType
	 * @param observer
	 */
	private void subInitialize(String dir, ILogTypeReader logTypeReader, 
			ILogOutObserver observerResource, ILogOutObserver observerCommon, String logType){
		
		File folder= new File(dir);
		File []fileList=folder.listFiles();
		Boolean isLastTrace=false;
		SequenceMatching seqResource= new SequenceMatching();
		SequenceMatching seqCommon= new SequenceMatching();
		sortedPatterns=new ArrayList<String>();		
		
		int size=fileList.length;
		
		for (int f=0; f<size;f++){
	
			if (f==size-1)
			  isLastTrace=true;
			System.out.println("processing for resource: "+fileList[f].getName());
			mineResourcePatterns(fileList[f], logTypeReader, seqResource, isLastTrace, observerResource);
			System.out.println("processing for common: "+fileList[f].getName());
			mineResourceCommonPattern(fileList[f], logTypeReader, seqCommon, isLastTrace, observerCommon);
		}
		
		commonPattern.put(logType, sortedPatterns);
		resourcePattern.put(logType,seqResource);
	}
	
	
	public static void main(String []args){
		LogMiner log=new LogMiner();
		log.initialize();
		//log.mineResourceCommonPattern();
	}
	
}
