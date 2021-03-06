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
package com.logminer.sequencematcher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.osgi.util.NLS;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.logminer.dbms.IDBCursor;
import com.logminer.dbms.IDBRecord;
import com.logminer.dbms.IDataAccessObject;
import com.logminer.exception.TotalADSDBMSException;
import com.logminer.exception.TotalADSGeneralException;
import com.logminer.exception.TotalADSReaderException;
import com.logminer.outstream.ILogOutStream;
import com.logminer.reader.ILogIterator;


/**
 * This class implements the sequence matching algorithm for the host-based anomaly
 * detection.
 *
 * @author <p>
 *         Syed Shariyar Murtaza justsshary@hotmail.com
 *         </p>
 *
 */
//public class SequenceMatching implements IDetectionAlgorithm {
public class SequenceMatching {

    private Integer fMaxWin = 5;
    private Integer fMaxHamDis = 0;
    private HashMap<Integer, Event[]> fEventSequences;
    private Boolean fDetailedAnalysis = false;
    private String[] fTrainingOptions = { "Max Win", "5", "Max Hamming Distance", "0", "Detailed Analysis", "false" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    private String[] fTestingOptions = { "Max Hamming Distance", "0", "Detailed Analysis", "false" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    private Integer fValidationTraceCount = 0;
    private Integer fValidationAnomalies = 0;
    private Integer fTestTraceCount = 0;
    private Integer fTestAnomalies = 0;
    private Boolean fIsintialize = false;
    private Boolean fIsTestStarted = false;
    private SequenceMatchingTree fTreeTransformer;
    private int fMaxWinLimit = 25;
    private NameToIDMapper fNameToID;
    private int fTestNameToIDSize;
    private  Boolean isValidationStarted = false;


    /**
     * Constructor
     **/
    public SequenceMatching() {

        fEventSequences = new HashMap<>();
        fTreeTransformer = new SequenceMatchingTree();
        fNameToID = new NameToIDMapper();

    }

    /**
     * Initializes the model, if it is already found in the database
     *
     * @param dataAccessObject
     *            IDataAccessObject object
     * @param database
     *            Database name
     * @throws TotalADSDBMSException DBMS exception
     */
  /*  private void initialize(IDataAccessObject dataAccessObject, String database) throws TotalADSDBMSException {

        try (IDBCursor cursor = dataAccessObject.selectAll(database,
                TraceCollection.COLLECTION_NAME.toString())){
                while (cursor.hasNext()) {
                    IDBRecord record = cursor.next();
                    Gson gson = new Gson();
                    Integer key = (Integer) record.get(TraceCollection.KEY.toString());
                    Object obj = record.get(TraceCollection.TREE.toString());
                    if (obj != null) {
                        Event[] event = gson.fromJson(obj.toString(), Event[].class);
                        fSysCallSequences.put(key, event);
                    }

                }
        }

        // Get the fMaxWin and maxHam
        try (IDBCursor cursor = dataAccessObject.selectAll(database,
                    SettingsCollection.COLLECTION_NAME.toString())){

            while (cursor.hasNext()) {
                IDBRecord record = cursor.next();
                fMaxWin = (Integer) record.get(SettingsCollection.MAX_WIN.toString());
                fMaxHamDis = (Integer) record.get(SettingsCollection.MAX_HAM_DIS.toString());
                fDetailedAnalysis = (Boolean) record.get(SettingsCollection.DETAILED_ANALYSIS.toString());
            }
        }

    }

*/
    /**
     * 
     * @param maxWin
     * @param hammingDistance
     */
    public void adjustSettings(int maxWin, int hammingDistance){
    	this.fMaxWin=maxWin;
    	this.fMaxHamDis=hammingDistance;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.tracecompass.totalads.algorithms.IDetectionAlgorithm#train
     * (org.eclipse.tracecompass.totalads.readers.ITraceIterator,
     * java.lang.Boolean, java.lang.String,
     * org.eclipse.tracecompass.totalads.dbms.IDataAccessObject,
     * org.eclipse.tracecompass.totalads.algorithms.IAlgorithmOutStream)
     */
    //@Override
    public void train(ILogIterator trace, Boolean isLastTrace, String database, IDataAccessObject dataAccessObject, ILogOutStream outStream) throws TotalADSGeneralException, TotalADSDBMSException, TotalADSReaderException {

    	//if (trace==null || isLastTrace==null || database==null|| dataAccessObject==null|| outStream==null) {
        //    throw new TotalADSGeneralException(Messages.SlidingWindow_NoNull);
        //}


      /*  if (!fIsintialize) {
            fValidationTraceCount = 0;
            fValidationAnomalies = 0;
            initialize(dataAccessObject, database);
            fIsintialize = true;
            fNameToID.loadMap(dataAccessObject, database);

        }*/

        outStream.addOutputEvent(Messages.SlidingWindow_StartMsg);
        outStream.addNewLine();

        int winWidth = 0;
        LinkedList<Integer> newSequence = new LinkedList<>();
        String event = null;

        while (trace.advance()) {
            event = trace.getCurrentEvent();

            newSequence.add(fNameToID.getId(event));

            winWidth++;

            if (winWidth >= fMaxWin) {

                winWidth--;
                Integer[] seq = new Integer[fMaxWin];
                seq = newSequence.toArray(seq);

                fTreeTransformer.searchAndAddSequence(seq, fEventSequences, outStream);

                newSequence.remove(0);

            }

        }
        if (isLastTrace) {
            // Saving events tree in database
            outStream.addOutputEvent(Messages.SlidingWindow_UniqueMsg);
            outStream.addNewLine();
            if (fEventSequences.size() > 0) {
                fTreeTransformer.printSequence(outStream, fEventSequences, fNameToID,false);
            } else{
                String err=NLS.bind(Messages.SlidingWindow_NoSeqLength,fMaxWin );
                outStream.addOutputEvent(err);
                outStream.addNewLine();
                throw new TotalADSGeneralException(err);

            }

            //fTreeTransformer.saveinDatabase(outStream, database, dataAccessObject, fSysCallSequences);
            fIsintialize = false;
          //  fNameToID.saveMap(dataAccessObject, database);
        }

    }


 /**
  * Mine Temporal patterns by grouping events by a common event. The Resource should be the second one after ":" sign.
  * @param trace
  * @param isLastTrace
  * @param database
  * @param dataAccessObject
  * @param outStream
  * @param isSorted
  * @param isCommonPatterns  True to mine common events without the resource name
  * @throws TotalADSGeneralException
  * @throws TotalADSDBMSException
  * @throws TotalADSReaderException
  */
    public void minePatternsByResources(ILogIterator trace, Boolean isLastTrace, String database, 
    		IDataAccessObject dataAccessObject, ILogOutStream outStream, Boolean isSorted, Boolean isCommonPatterns)
    				throws TotalADSGeneralException, TotalADSDBMSException, TotalADSReaderException {

    	//if (trace==null || isLastTrace==null || database==null|| dataAccessObject==null|| outStream==null) {
        //    throw new TotalADSGeneralException(Messages.SlidingWindow_NoNull);
        //}


      /*  if (!fIsintialize) {
            fValidationTraceCount = 0;
            fValidationAnomalies = 0;
            initialize(dataAccessObject, database);
            fIsintialize = true;
            fNameToID.loadMap(dataAccessObject, database);

        }*/

        outStream.addOutputEvent(Messages.SlidingWindow_StartMsg);
        outStream.addNewLine();

        int winWidth = 0;
        HashMap<Integer,LinkedList<Integer>> commonEvntMap=new HashMap<>();
        
        String events = null;

        while (trace.advance()) {
            events = trace.getCurrentEvent();
           
            String []event=events.split(":");
            
            
            if (event.length>1){
			            Integer key=fNameToID.getId(event[1]);
			            Integer val=fNameToID.getId(event[0]);
			           
			            LinkedList<Integer> newSequence=commonEvntMap.get(key);
			            if (newSequence!=null){
			            	newSequence.add(val);
			            	commonEvntMap.put(key,newSequence);
			            }else{
			            	newSequence = new LinkedList<>();
			            	newSequence.add(val);
			            	commonEvntMap.put(key,newSequence);
			            }
			            
			          }
	             }
			            
			          //  winWidth=fMaxWin;
			
			          //  if (winWidth >= fMaxWin) {
			
			                
			                for (Map.Entry<Integer, LinkedList<Integer>> seqByGroup:commonEvntMap.entrySet()){
			                	winWidth=0;
			                	LinkedList<Integer> sequence =seqByGroup.getValue();
			                	
			                	LinkedList<Integer> tmp=new LinkedList<>();
			                	if (!isCommonPatterns)
			                	    tmp.addFirst(seqByGroup.getKey());
			                	
			                	int k=0;
			                	for (k=0; k<sequence.size();k++){
			                		winWidth++;
			                		tmp.add(sequence.get(k));
			                		if (winWidth>=fMaxWin){
			                			Integer[] seq =null;
			                			
			                			if (isCommonPatterns)
			                				 seq =new Integer[fMaxWin];
			                			else
			                			     seq = new Integer[fMaxWin+1];// add 1 for the group name;i.e., key
				                    	
			                			seq = tmp.toArray(seq);
				                    					                   
					                    fTreeTransformer.searchAndAddSequence(seq, fEventSequences, outStream);
					                   // fTreeTransformer.printSequence(outStream, fEventSequences, fNameToID,isSorted);
					                    winWidth--;
					                    tmp.remove(1);
			                		}
			                    }
			                	/*if (k<fMaxWin-1 && k>0){
			                		Integer[] seq = new Integer[k];// add 1 for the group name;i.e., key
			                    	seq = tmp.toArray(seq);
			                    	// we need to create a new data structure to store events with less than the window size or igonore them
				                    // storing with existing structure is prone to errors
				                    fTreeTransformer.searchAndAddSequence(seq, fEventSequences, outStream);
			                	}*/
			                		
			                	tmp.clear();
			                	sequence.clear();// clean memory, we don't need this any more
			
			                }
			                commonEvntMap.clear();
			                
			          //  }
        //   }
      // }
        if (isLastTrace) {
            // Saving events tree in database
            outStream.addOutputEvent(Messages.SlidingWindow_UniqueMsg);
            outStream.addNewLine();
            if (fEventSequences.size() > 0) {
                fTreeTransformer.printSequence(outStream, fEventSequences, fNameToID,isSorted);
            } else{
                String err=NLS.bind(Messages.SlidingWindow_NoSeqLength,fMaxWin );
                outStream.addOutputEvent(err);
                outStream.addNewLine();
                throw new TotalADSGeneralException(err);

            }

            //fTreeTransformer.saveinDatabase(outStream, database, dataAccessObject, fSysCallSequences);
            fIsintialize = false;
          //  fNameToID.saveMap(dataAccessObject, database);
        }

    }

    /**
     * Returns the json tree for a key
     * @return
     */
    public JsonObject getPatternTree(String key){
    	Integer id= fNameToID.getId(key);
    	Event [] events=fEventSequences.get(id);
    	if (events==null || events.length <=0)
    		return null;
    	//EventString [] eventsString=new EventString[events.length];
    //	fTreeTransformer.generateStringRepresentation(events, eventsString, fNameToID);
    	//return fTreeTransformer.convertToJson(eventsString, key);
    	//return fTreeTransformer.convertToJson(events, 0);
    	EventJson eventJson=new EventJson();
    	eventJson=fTreeTransformer.generateHierarchicalJsonObjects(events, eventJson, fNameToID);
    	Gson gson = new Gson();
    	return gson.toJsonTree(eventJson).getAsJsonObject();
    }
    
    /**
     * Get keys
     * @return
     */
    public String[] getPatternKeys(){
        Set<Integer> keys=fEventSequences.keySet();
    	String []keyList=new String[keys.size()];
        Iterator<Integer> it=keys.iterator();
        int cnt=0;
    	while (it.hasNext())
    		keyList[cnt++]=fNameToID.getKey(it.next());
    	
    	return keyList;
    	
    }
    
    /**
     * 
     * @param outStream
     * @param isSorted
     */
    public void printPatterns(ILogOutStream outStream, Boolean isSorted){
    	 fTreeTransformer.printSequence(outStream, fEventSequences, fNameToID,isSorted);
    }
    
    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.tracecompass.totalads.algorithms.IDetectionAlgorithm#validate
     * (org.eclipse.tracecompass.totalads.readers.ITraceIterator,
     * java.lang.String,
     * org.eclipse.tracecompass.totalads.dbms.IDataAccessObject,
     * java.lang.Boolean,
     * org.eclipse.tracecompass.totalads.algorithms.IAlgorithmOutStream)
     */
  /*  @Override
    public void validate(ITraceIterator trace, String database, IDataAccessObject dataAccessObject, Boolean isLastTrace, IAlgorithmOutStream outStream)
            throws TotalADSGeneralException, TotalADSDBMSException, TotalADSReaderException {

    	if (trace==null || isLastTrace==null || database==null|| dataAccessObject==null|| outStream==null) {
            throw new TotalADSGeneralException(Messages.SlidingWindow_NoNull);
        }

        if (!isValidationStarted) {
            loadSetings(database, dataAccessObject);
            isValidationStarted = true;
        }
        fValidationTraceCount++;// count the number of traces

        Results result = evaluateTrace(trace, database, dataAccessObject, outStream);

        if (result.getAnomaly()) {
            String details = result.getDetails().toString();
            outStream.addOutputEvent(details);
            outStream.addNewLine();
            fValidationAnomalies++;

        }

        if (isLastTrace) {

            outStream.addOutputEvent(NLS.bind(Messages.SlidingWindow_TotalTraces , fValidationTraceCount));
            outStream.addNewLine();
            Double anomalyPrcentage = (fValidationAnomalies.doubleValue() / fValidationTraceCount.doubleValue()) * 100;

            outStream.addOutputEvent(NLS.bind(Messages.SlidingWindow_TotAnom , fMaxHamDis , anomalyPrcentage));
            outStream.addNewLine();

            Double normalPercentage = (100 - anomalyPrcentage);
            outStream.addOutputEvent(NLS.bind(Messages.SlidingWindow_TotalNorm , fMaxHamDis , normalPercentage));
            outStream.addNewLine();
            // Update the settings collection for maxwin and maxhamm
            saveSettings(database, dataAccessObject);
            outStream.addOutputEvent(Messages.SlidingWindow_DBUpdated);
            outStream.addNewLine();


        }

    }
*/
    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.tracecompass.totalads.algorithms.IDetectionAlgorithm#test
     * (org.eclipse.tracecompass.totalads.readers.ITraceIterator,
     * java.lang.String,
     * org.eclipse.tracecompass.totalads.dbms.IDataAccessObject,
     * org.eclipse.tracecompass.totalads.algorithms.IAlgorithmOutStream)
     */
    /*@Override
    public Results test(ITraceIterator trace, String database, IDataAccessObject dataAccessObject, IAlgorithmOutStream outputStream)
            throws TotalADSGeneralException, TotalADSDBMSException, TotalADSReaderException {

    	if (trace==null || database==null|| dataAccessObject==null|| outputStream==null) {
            throw new TotalADSGeneralException(Messages.SlidingWindow_NoNull);
        }


        if (!fIsTestStarted) {
            fTestTraceCount = 0;
            fTestAnomalies = 0;
            initialize(dataAccessObject, database); // get the trees from db

            fIsTestStarted = true;
            fNameToID.loadMap(dataAccessObject, database);
            fTestNameToIDSize = fNameToID.getSize();
        }

        Results res = evaluateTrace(trace, database, dataAccessObject, outputStream);
        outputStream.addOutputEvent(Messages.SlidingWindow_Finish);
        outputStream.addNewLine();
        return res;

    }
*/
    /**
     * Evaluates a trace
     *
     * @param trace Trace iterator
     * @param database Model name
     * @param dataAccessObject Data Access object
     * @return Results enclosed in an object
     * @throws TotalADSReaderException I/O Exception
     */
  /*  private Results evaluateTrace(ITraceIterator trace, String database, IDataAccessObject dataAccessObject, IAlgorithmOutStream outStream) throws TotalADSReaderException {

        int winWidth = 0, anomalousSequences = 0, maxAnomalousSequencesToReturn;
        int displaySeqCount = 0, totalAnomalousSequences = 0, largestHam = 0;
        Integer[] largestHamSeq = null;
        Results results = new Results();
        if (fDetailedAnalysis == true) {
            maxAnomalousSequencesToReturn = 10;
        } else {
            maxAnomalousSequencesToReturn = 5;
        }

        String headerMsg =  NLS.bind( Messages.SlidingWindow_TopNSeq,maxAnomalousSequencesToReturn, fMaxHamDis);
        results.setAnomalyType(""); //$NON-NLS-1$
        results.setAnomaly(false);
        fTestTraceCount++;

        LinkedList<Integer> newSequence = new LinkedList<>();
        outStream.addOutputEvent(Messages.SlidingWindow_StartMsg);
        outStream.addNewLine();
        outStream.addOutputEvent(Messages.SlidingWindow_SeqEval);
        outStream.addNewLine();
        String event = null;
        int seqCount = 0;
        while (trace.advance()) {

            event = trace.getCurrentEvent();
            newSequence.add(fNameToID.getId(event));

            winWidth++;

            if (winWidth >= fMaxWin) {
                seqCount++;

                winWidth--;

                Integer[] seq = new Integer[fMaxWin];
                seq = newSequence.toArray(seq);

                // Calculate the minimum Hamming distance
                Integer hammDisForSequence = seq.length; // we assign max
                                                         // hamming distance
                for (Map.Entry<Integer, Event[]> tree : fSysCallSequences.entrySet()) {
                    Event[] nodes = tree.getValue();
                    // Just get the Hamming and search with a full sequence
                    Integer hammDisForTree = fTreeTransformer.getHammingAndSearch(nodes, seq);
                    if (hammDisForTree < hammDisForSequence) {
                        hammDisForSequence = hammDisForTree;
                    }
                    if (hammDisForSequence == 0)
                    {
                        break;// if Hamming is zero, we found a match break;
                              // don't continue further, save time
                    }
                }

                // Print every 20,000th sequence because trace parsing could
                // take longer
                if ((seqCount % 100000) == 0) {
                    outStream.addOutputEvent(Messages.SlidingWindow_EvalUptoSeq + seqCount + Messages.SlidingWindow_LargestHam + largestHam);
                    outStream.addNewLine();
                }
                // If Hamming distance is greater than the set threshold then it
                // is an anomaly
                if (hammDisForSequence > fMaxHamDis) {
                    totalAnomalousSequences++;

                    if (headerMsg.length() >= 1) {
                        results.setAnomaly(true);
                        results.setDetails(headerMsg);
                        headerMsg = ""; //$NON-NLS-1$
                    }
                    // Add a new sequence for display, when all of the previous
                    // events are gone
                    if (displaySeqCount <= maxAnomalousSequencesToReturn) {
                        if (anomalousSequences % fMaxWin == 0) {
                            // Convert sequence in integer ids to name
                            StringBuilder seqName = new StringBuilder();
                            for (int i = 0; i < seq.length; i++) {
                                if (i == seq.length - 1) {
                                    seqName.append(fNameToID.getKey(seq[i]));
                                } else {
                                    seqName.append(fNameToID.getKey(seq[i])).append(" "); //$NON-NLS-1$
                                }
                            }
                            seqName.append(":: Ham=").append(hammDisForSequence).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
                            // Add sequence to results
                            results.setDetails(seqName.toString());
                            displaySeqCount++;
                        }
                    }

                    // Get the sequence with the largest Hamming distance
                    if (hammDisForSequence > largestHam) {
                        largestHam = hammDisForSequence;
                        largestHamSeq = seq;

                    }
                    anomalousSequences++;
                    // When fDetailedAnalysis is false, then just break after
                    // ten anomalous sequences
                    if (fDetailedAnalysis == false && displaySeqCount > maxAnomalousSequencesToReturn) {
                        outStream.addOutputEvent(NLS.bind(Messages.SlidingWindow_DistinctSeq, maxAnomalousSequencesToReturn));
                        outStream.addNewLine();
                        break;
                    }
                }// End of Ham comparison

                newSequence.remove(0);// remove the top event and slide a window
            }

        }
        if (seqCount == 0) {
            results.setAnomaly(true);
        }
        additionalInforForResults(largestHam, largestHamSeq, results, totalAnomalousSequences);

        return results;

    }
*/
    /**
     * Adds additional information to the results
     *
     * @param largestHam
     * @param largestHamSeq
     * @param results
     * @param totalAnomalousSequences
     */
  /*  private void additionalInforForResults(int largestHam, Integer[] largestHamSeq, Results results, int totalAnomalousSequences) {

        if (results.getAnomaly()) {
            fTestAnomalies++;
        }

        if (results.getAnomaly() && fDetailedAnalysis) {

            results.setDetails(NLS.bind(Messages.SlidingWindow_LargeHam , largestHam) );
            results.setDetails(Messages.SlidingWindow_LastHamSeq);
            for (int i = 0; i < largestHamSeq.length; i++) {
                results.setDetails(fNameToID.getKey(largestHamSeq[i]) + " "); //$NON-NLS-1$
            }
            results.setDetails(NLS.bind(Messages.SlidingWindow_TotAnomalousSeq ,totalAnomalousSequences));
        }

        // // get unknown events
        if (fNameToID.getSize() > fTestNameToIDSize) {
            Integer diff = fNameToID.getSize() - fTestNameToIDSize;
            int eventCount = 0;
            if (diff > 10) {
                eventCount = fTestNameToIDSize + 10;
            } else {
                eventCount = fTestNameToIDSize + diff;
            }

            results.setDetails(Messages.SlidingWindow_TenEvents);
            int count = 0;
            for (int i = fTestNameToIDSize; i < eventCount; i++) {// All these
                                                                  // events are
                                                                  // unknown
                results.setDetails(fNameToID.getKey(i) + " "); //$NON-NLS-1$
                count++;
                if ((count) % 10 == 0) {
                    results.setDetails("\n"); //$NON-NLS-1$
                }
            }
            fTestNameToIDSize += diff;// don't display this for the second trace
                                      // unless or untill there are additional
                                      // events

        }
    }
*/

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.tracecompass.totalads.algorithms.IDetectionAlgorithm#
     * getTotalAnomalyPercentage()
     */
    /*@Override
    public Double getTotalAnomalyPercentage() {
        Double anomalousPercentage = (fTestAnomalies.doubleValue() / fTestTraceCount.doubleValue()) * 100;
        return anomalousPercentage;
    }
*/
 


    /**
     * Self registration of the model with the modelFactory
     * @throws TotalADSGeneralException Validation exception
     */
   /* public static void registerAlgorithm() throws TotalADSGeneralException {
        AlgorithmFactory modelFactory = AlgorithmFactory.getInstance();
        SequenceMatching sldWin = new SequenceMatching();
        modelFactory.registerModelWithFactory(AlgorithmTypes.ANOMALY, sldWin);
    }
*/

}
