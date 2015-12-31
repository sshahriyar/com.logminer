package com.logminer.main;

import java.io.File;

import com.logminer.exception.TotalADSDBMSException;
import com.logminer.exception.TotalADSGeneralException;
import com.logminer.exception.TotalADSReaderException;
import com.logminer.outstream.ILogOutObserver;
import com.logminer.outstream.LogOutStream;
import com.logminer.reader.ILogIterator;
import com.logminer.reader.ILogTypeReader;
import com.logminer.reader.adlogs.AdLogReader;
import com.logminer.reader.adlogs.TextLineTraceReader;
import com.logminer.sequencematcher.SequenceMatching;

public class LogMiner {

	
	public static void minePatterns(){
		
		LogOutStream outPut= new LogOutStream();
	 	outPut.addObserver(new ILogOutObserver() {			
					@Override
					public void updateOutput(String message) {
						System.out.print(message);
						
					}
		});
		
		ILogTypeReader logFile= new AdLogReader();
		try(ILogIterator logIterator= logFile.getLogIterator( new File("/home/t909801/logs/ad-auth-xml.xml"))){
		   
			        Boolean isLastTrace=true;
					SequenceMatching seq= new SequenceMatching();
					seq.adjustSettings(2, 0);
					seq.train(logIterator, isLastTrace, "", null, outPut);
		
		} catch (TotalADSReaderException | TotalADSGeneralException | TotalADSDBMSException ex){
			System.out.println(ex.getMessage());
			
		}
		
	}
	
	
	public static void main(String []args){
		LogMiner.minePatterns();
	}
	
}
