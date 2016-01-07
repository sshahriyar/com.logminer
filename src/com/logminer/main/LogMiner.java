package com.logminer.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.logminer.exception.TotalADSDBMSException;
import com.logminer.exception.TotalADSGeneralException;
import com.logminer.exception.TotalADSReaderException;
import com.logminer.outstream.ILogOutObserver;
import com.logminer.outstream.LogOutStream;
import com.logminer.reader.ILogIterator;
import com.logminer.reader.ILogTypeReader;
import com.logminer.reader.adlogs.AdAuthLogReader;
import com.logminer.reader.adlogs.AdRawLogReader;
import com.logminer.reader.adlogs.TextLineTraceReader;
import com.logminer.sequencematcher.SequenceMatching;

public class LogMiner {
	
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
				//System.out.print(message);
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
	public  void minePatterns(){
		
		
		
	//	ILogTypeReader logFile= new AdAuthLogReader();
		//String file="/home/t909801/logs/ad-auth-log-dec4-1month.xml";
		ILogTypeReader logFile= new AdRawLogReader();
		String file="/mnt/hgfs/Host/ad-raw.xml";
		
		try(ILogIterator logIterator= logFile.getLogIterator( new File(file))){
		
			OutputClass out=new OutputClass();
			LogOutStream outPut= new LogOutStream();
		 	outPut.addObserver(out);
		
		 	//try(ILogIterator logIterator= logFile.getLogIterator( new File("/home/t909801/logs/temp.txt"))){ 
			        Boolean isLastTrace=true;
					SequenceMatching seq= new SequenceMatching();
					seq.adjustSettings(5, 0);
				//	seq.train(logIterator, isLastTrace, "", null, outPut);
					seq.minePatternsByCommonEvent(logIterator, isLastTrace, "", null, outPut,true);
					out.dispose();
		
		} catch (TotalADSReaderException | TotalADSGeneralException | TotalADSDBMSException | IOException ex){
			System.out.println(ex.getMessage());
			
		}
		
	}
	
	
	public static void main(String []args){
		LogMiner log=new LogMiner();
		log.minePatterns();
	}
	
}
