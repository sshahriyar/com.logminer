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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.logminer.dbms.IDBCursor;
import com.logminer.dbms.IDBRecord;
import com.logminer.dbms.IDataAccessObject;
import com.logminer.exception.TotalADSDBMSException;
import com.logminer.outstream.ILogOutStream;

/**
 * This class implements the tree transformation methods for the sequence matching
 * algorithm. It converts a trace of events into a tree of events and serializes
 * to database when needed.
 *
 * @author Syed Shariyar Murtaza
 *
 *
 */
class SequenceMatchingTree {
    /**
     * Constructor
     */
    public SequenceMatchingTree() {

    }

    /**
     * Searches and adds a sequence in the tree of events forming sequences
     *
     * @param newSequence
     *            the sequence to search and add
     * @param existinTreesofSeqs
     *            Existing trees of sequences
     * @param out
     *            Output stream to display processing messages
     */
    public void searchAndAddSequence(Integer[] newSequence, HashMap<Integer, Event[]> existinTreesofSeqs, ILogOutStream out) {

        Integer seqSize = newSequence.length;

        // Find the first event in the tree of existing sequence
        Event[] eventSequence = existinTreesofSeqs.get(newSequence[0]);

        // load from database
        // Event[] eventSequence= loadTreeFromDatabase(newSequence[0], database,
        // dataAccessObject) ;

        if (eventSequence == null) { // if there is no such starting event then
                                     // add sequence to such an event

            eventSequence = new Event[seqSize + 1];
            for (int j = 0; j < seqSize; j++) {
                eventSequence[j] = new Event();
                eventSequence[j].setEvent(newSequence[j]);
                // out.addOutputEvent(newSequence[j].toString()+ " ");
            }
            eventSequence[seqSize] = new Event();
            eventSequence[seqSize].setEvent(1);
            // out.addNewLine();

        } else {

            Boolean isFound = searchAndAppendSequence(eventSequence, newSequence);// search
                                                                                  // in
                                                                                  // tree
                                                                                  // from
                                                                                  // this
                                                                                  // node/sequence

            if (isFound == false) {// if no match is found then add on 0 branch
                                   // because only top event matches
                eventSequence[0].setBranches(new ArrayList<Event[]>());
                Event[] newBranchSeq = new Event[seqSize + 1];
                for (int j = 0; j < seqSize - 1; j++) {
                    newBranchSeq[j] = new Event();
                    newBranchSeq[j].setEvent(newSequence[j]);
                    // out.addOutputEvent(newSequence[j].toString()+ " ");
                }
                newBranchSeq[seqSize] = new Event();
                newBranchSeq[seqSize].setEvent(1);
                eventSequence[0].getBranches().add(newBranchSeq);
                // out.addNewLine();

            }
        }
        // putting the sequence (tree actually) to the starting event (node) as
        // a name
        existinTreesofSeqs.put(newSequence[0], eventSequence);

        // add to database
        // saveTreeInDatabase( database, dataAccessObject, eventSequence,
        // HmmModelCollection.COLLECTION_NAME.toString());
    }

    /**
     * Searches and appends a sequence(set of events) to an already existing
     * tree of events. If a sequence already exists, it updates the counter Use
     * the following tree example to understand the code
     *
     * 108-106-5-55-45-90 | -3-9-6 | |-3-10 | |-3-6 | 106 | 5 i.e., the
     * sequences are 108-106-5-55-45-90,108-106-5-3-9-6, 108-106-5-3-3-10,
     * 108-106-5-3-3-6, 108-106-5-3-3-106, and 108-106-5-3-3-5
     *
     * @param existingTree
     *            an already existing tree of events
     * @param newSeq
     *            a sequence of events that requires to be appended
     * @return
     */
    private Boolean searchAndAppendSequence(Event[] existingTree, Integer[] newSeq) {

        Integer seqSize = newSeq.length;
        Integer j;
        for (j = 0; j < seqSize; j++) {

            if ((j>=existingTree.length-1) || !existingTree[j].getEvent().equals(newSeq[j])) {// length-1 because we want to avoid the matching of count at the end
                break;
            }
        }

        Integer matchIdx = j - 1;

        if (matchIdx >= seqSize - 1) {
            Integer counter = existingTree[seqSize].getEvent() + 1;
            existingTree[seqSize].setEvent(counter);
            return true;
        }
        else if (matchIdx < 0) {
            return false; // return false if mismatch on the first idx
        }

        else {
            Event[] newEventSeq = new Event[seqSize - matchIdx];// +1 for the
                                                                // count
            Integer[] newTmpSeq = new Integer[seqSize - matchIdx - 1];
            Boolean isFound = false;
            Integer i;
            for (i = 0; i < newEventSeq.length - 1; i++) {
                newEventSeq[i] = new Event();
                newEventSeq[i].setEvent(newSeq[matchIdx + i + 1]);// that is
                                                                  // copy from
                                                                  // the next
                                                                  // index of
                                                                  // matched
                                                                  // index
                newTmpSeq[i] = newSeq[matchIdx + i + 1];
            }
            newEventSeq[i] = new Event();
            newEventSeq[i].setEvent(1);// add 1 as a counter at the leaf, for
                                       // the first sequence on a branch

            ArrayList<Event[]> branches = existingTree[matchIdx].getBranches();
            // When there are no more branches then we shall automatically
            // add a new branch by skipping the if block
            if (branches != null) {
                // if the branches exist then we need to recursively go through
                // the remaining branches to find
                // a possible location to append the new sequence

                for (int bCount = 0; bCount < branches.size(); bCount++) {
                    Event[] branchEventSeq = branches.get(bCount);
                    // / ****** recursive call
                    isFound = searchAndAppendSequence(branchEventSeq, newTmpSeq);
                    // / ****** recursive call
                    if (isFound == true) {// {// there is no need to iterate
                                          // more branches, we have found a
                                          // match
                        // branches.set(bCount, branchEventSeq);
                        // branches.add(returnSeq);
                        break;
                    }
                }
            } else {
                branches = new ArrayList<>();
            }
            // We have just found out where to append a branch in the tree
            // add a new branch to the event and return the eventSeq.

            if (isFound == false) {
                branches.add(newEventSeq);
            }

            existingTree[matchIdx].setBranches(branches);
            return true;

        }
        // End of function searchAndAddSequence
    }

    /**
     * This function saves the model in the database by converting HashMap to
     * JSON and serializing to a NoSQL database
     *
     * @param outStream
     *            An object to display output
     * @param database
     *            Database name
     * @param dataAccessObject
     *            IDataAccessObject object
     * @param sysCallSequences
     *            A map containing one tree of events for every name
     * @throws TotalADSDBMSException
     *             DBMS related exception
     */
    public void saveinDatabase(ILogOutStream outStream, String database, IDataAccessObject dataAccessObject, HashMap<Integer,
            Event[]> sysCallSequences) throws TotalADSDBMSException {

        outStream.addOutputEvent(Messages.SlidingWindowTree_SaveinDB);
        outStream.addNewLine();

        for (Map.Entry<Integer, Event[]> nodes : sysCallSequences.entrySet()) {

            Event[] events = nodes.getValue();

            JsonObject jsonEvents=convertToJson(events, nodes.getKey());

            JsonObject jsonKey = new JsonObject();
            jsonKey.addProperty(TraceCollection.KEY.toString(), nodes.getKey());
            dataAccessObject.insertOrUpdateUsingJSON(database, jsonKey, jsonEvents, TraceCollection.COLLECTION_NAME.toString());

        }

    }

    /**
     * Saves a tree in the database
     *
     * @param database
     *            Database name
     * @param dataAccessObject
     *            Data access object
     * @param tree
     *            Tree
     * @param collectionName
     *            Collection name
     * @throws TotalADSDBMSException
     *             DBMS Exception
     */
    public void saveTreeInDatabase(String database, IDataAccessObject dataAccessObject,
            Event[] tree, String collectionName) throws TotalADSDBMSException {

        Integer key = tree[0].getEvent(); // Top node is the name

        JsonObject jsonEvents=convertToJson(tree, key);

        JsonObject jsonKey = new JsonObject();
        jsonKey.addProperty(TraceCollection.TREE.toString(), key);

        dataAccessObject.insertOrUpdateUsingJSON(database, jsonKey, jsonEvents, collectionName);

    }
    
    /**
     * Converts Event array to JSON
     * @param tree
     * @param key
     * @return
     */
    public JsonObject convertToJson(Event[] tree, Integer key){
    	Gson gson = new Gson();

        JsonElement jsonArray = gson.toJsonTree(tree);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(TraceCollection.KEY.toString(), key);
        jsonObject.add(TraceCollection.TREE.toString(), jsonArray);
		return jsonObject;
    }
    /**
     * Converts Event array of strings to JSON
     * @param tree
     * @param key
     * @return
     */
    public JsonObject convertToJson(EventString[] tree, String key){
    	Gson gson = new Gson();

        JsonElement jsonArray = gson.toJsonTree(tree);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(TraceCollection.KEY.toString(), key);
        jsonObject.add(TraceCollection.TREE.toString(), jsonArray);
		return jsonObject;
    }

    /**
     * Loads a tree based on the root node from the database
     *
     * @param rootNode
     *            Root event
     * @param database
     *            Database name
     * @param dataAccessObject
     *            Data access object
     * @return A tree
     * @throws TotalADSDBMSException
     *             DBMS related exception
     */
    public Event[] loadTreeFromDatabase(String rootNode, String database, IDataAccessObject dataAccessObject) throws TotalADSDBMSException {

        Event[] event = null;
        try (IDBCursor cursor = dataAccessObject.select(TraceCollection.KEY.toString(), "", //$NON-NLS-1$
                rootNode, database, TraceCollection.COLLECTION_NAME.toString())) {
            if (cursor.hasNext()) {
                IDBRecord record = cursor.next();
                Gson gson = new Gson();
                event = gson.fromJson(record.get(TraceCollection.TREE.toString()).toString(), Event[].class);
            }
        }
        return event;
    }

    /**
     * Prints all the sequences in a tree; use for testing
     *
     * @param outStream
     *            An object to display information
     * @param sysCallSequences
     *            A map containing one tree of events for every name
     * @param nameToId
     *            NameToIDMapper
     * @param isSorted
     * 	         true: To sort the sequence by count. Note sorting consumes more memory.           
     */
    public void printSequence(ILogOutStream outStream, HashMap<Integer, Event[]> sysCallSequences, NameToIDMapper nameToId, Boolean isSorted) {

    	TreeMap<Integer,String> sortSeqTree=new TreeMap<>();
        for (Map.Entry<Integer, Event[]> nodes : sysCallSequences.entrySet()) {
        	if (!isSorted)
        		printRecursive(nodes.getValue(), "", outStream, nameToId,null); //$NON-NLS-1$
        	else
        		printRecursive(nodes.getValue(), "", outStream, nameToId,sortSeqTree); //$NON-NLS-1$;
        }
        
        // Printing sroted Tree
        if (isSorted=true){
		        int count=1;
		        for (Map.Entry<Integer,String> seq:sortSeqTree.descendingMap().entrySet()){
		        	//outStream.addNewLine();
		        	//outStream.addOutputEvent("Count= " + seq.getKey().toString());
		        	outStream.addNewLine();
		        	outStream.addOutputEvent("pat"+count+ ": " +seq.getValue());
		        	count++;
		        }
		        sortSeqTree.clear();
        }
    }

    /**
     * /** This function goes through the tree of events and print the sequences
     * in a human readable form.
     *
     * @param nodes
     *            Root event
     * @param prefix
     *            Prefix of the event sequence
     * @param OutStream
     *            An object to display output
     * @param nameToId
     *            Name to id mapper
     * @param sortedSeqTree
     * 		      A tree Map to store sorted sequences           
     */
    private void printRecursive(Event[] nodes, String prefix, ILogOutStream outStream, NameToIDMapper nameToId,  TreeMap<Integer,String> sortSeqTree) {

        // Boolean isPrefixPrinted=false;
        String thePrefix = prefix;
        for (int nodeCount = 0; nodeCount < nodes.length; nodeCount++) {

            ArrayList<Event[]> branches = nodes[nodeCount].getBranches();
            if (nodeCount == nodes.length - 1) {
                thePrefix = thePrefix + Messages.SlidingWindowTree_Count + nodes[nodeCount].getEvent();// the
                // last
                // element
                // is
                // the
                // count
                // of
                // the
                // sequence
            }
            else {
                // Just append the events
               // thePrefix = thePrefix + "\"" + nameToId.getKey(nodes[nodeCount].getEvent().intValue()) + "\" "; //$NON-NLS-1$ //$NON-NLS-2$
                thePrefix = thePrefix  + nameToId.getKey(nodes[nodeCount].getEvent().intValue()) + ", "; //$NON-NLS-1$ //$NON-NLS-2$

            }

            if (branches != null) { // if there are branches on an event then
                                    // keep them

                for (int i = 0; i < branches.size(); i++) {
                	if (sortSeqTree!=null)
                		printRecursive(branches.get(i), thePrefix, outStream, nameToId,sortSeqTree);
                    else
                		printRecursive(branches.get(i), thePrefix, outStream, nameToId,null);
                }
            } else {

                // Print only when we reach a leaf of a branch
                if (nodeCount == nodes.length - 1) {
                   if (sortSeqTree!=null){
	                		String seq=sortSeqTree.get(nodes[nodeCount].getEvent()); // get the count of sequence, which is the last event 
	                		if (seq==null)
	                			sortSeqTree.put(nodes[nodeCount].getEvent(), thePrefix);
	                		else{
	                			seq=seq+"\n"+thePrefix;
	                			sortSeqTree.put(nodes[nodeCount].getEvent(), seq);
	                		}
                	}
                	else{
                		 //if (thePrefix.contains("T906537"))
		                  	//    System.out.println(thePrefix);
                		outStream.addOutputEvent(thePrefix);
                	}
                }

               
            }
        }
        if (sortSeqTree==null)
        	outStream.addNewLine();
    }
    
    
    /**
     * /** This function goes through the tree of events and generates the string representation of events
     * 
     *
     * @param nodes
     *            Root event
     * @param prefix
     *            Prefix of the event sequence
     * @param OutStream
     *            An object to display output
     * @param nameToId
     *            Name to id mapper
     * @param sortedSeqTree
     * 		      A tree Map to store sorted sequences           
     */
    public void generateStringRepresentation(Event[] nodes, EventString[] nodesString,  NameToIDMapper nameToId) {

        // Boolean isPrefixPrinted=false;
        
        for (int nodeCount = 0; nodeCount < nodes.length; nodeCount++) {

            ArrayList<Event[]> branches = nodes[nodeCount].getBranches();
            if (nodeCount == nodes.length - 1) {
            	
            	if(nodesString[nodeCount]==null)
            		nodesString[nodeCount]=new EventString();
                
            	nodesString[nodeCount].setEvent(Messages.SlidingWindowTree_Count + nodes[nodeCount].getEvent().toString());// the
                // last
                // element
                // is
                // the
                // count
                // of
                // the
                // sequence
            }
            else {
                // Just append the events
            	if(nodesString[nodeCount]==null)
            		nodesString[nodeCount]=new EventString();
                
            	nodesString[nodeCount].setEvent(nameToId.getKey(nodes[nodeCount].getEvent().intValue())); //$NON-NLS-1$ //$NON-NLS-2$

            }

            if (branches != null) { // if there are branches on an event then
                                    // keep them
            	ArrayList<EventString[]> strBranches= new ArrayList<>();
                for (int i = 0; i < branches.size(); i++) {
                		Event[] evInt=branches.get(i);
                		EventString [] evStr=new EventString[evInt.length];
                		generateStringRepresentation(evInt,evStr , nameToId);
                		strBranches.add(evStr);
                }
                nodesString[nodeCount].setBranches(strBranches);
            } else {

                // Print only when we reach a leaf of a branch
                if (nodeCount == nodes.length - 1) {
                  
                	
                		 //if (thePrefix.contains("T906537"))
		                  	//    System.out.println(thePrefix);
                	//	outStream.addOutputEvent(thePrefix);
                	
                }

               
            }
        }
       
    }
       
    /**
     * /** This function goes through the tree of events and generates the string representation of events
     * 
     *
     * @param nodes
     *            Root event
     * @param prefix
     *            Prefix of the event sequence
     * @param OutStream
     *            An object to display output
     * @param nameToId
     *            Name to id mapper
     * @param sortedSeqTree
     * 		      A tree Map to store sorted sequences           
     */
    public EventJson generateHierarchicalJsonObjects(Event[] nodes, EventJson nodesString,  NameToIDMapper nameToId) {

        // Boolean isPrefixPrinted=false;
        EventJson root=nodesString;
        for (int nodeCount = 0; nodeCount < nodes.length; nodeCount++) {
        	EventJson next=null;
            ArrayList<Event[]> branches = nodes[nodeCount].getBranches();
            int branchSize=0;
            if (branches!=null)
            	branchSize=branches.size();
            
           
            
            if (nodeCount == nodes.length - 1) {
                
            	nodesString.setEvent(Messages.SlidingWindowTree_Count + nodes[nodeCount].getEvent().toString());// the
                // last
                // element
                // is
                // the
                // count
                // of
                // the
                // sequence
            }
            else {
                // Just append the events
            	
            	EventJson []eventJsonBranches=new EventJson[branchSize+1]; //1 for the current siblings
                nodesString.setBranches(eventJsonBranches);
            	
                nodesString.setEvent(nameToId.getKey(nodes[nodeCount].getEvent().intValue())); //$NON-NLS-1$ //$NON-NLS-2$
            	
                EventJson []branchJsons= nodesString.getBranches(); // get the first branch to record events in the array
            	branchJsons[0]=new EventJson();
            	next=branchJsons[0];
            }

            if (branches != null) { // if there are branches on an event then
            	
            	EventJson []branchJsons= nodesString.getBranches();                   
            	for (int i = 0; i < branches.size(); i++) {
                		Event[] evInt=branches.get(i);
                		branchJsons[i+1]=new EventJson();
                		EventJson evJson=branchJsons[i+1];//first one is used above under else; that is why i+1
                		evJson=generateHierarchicalJsonObjects(evInt,evJson , nameToId);
                	    branchJsons[i+1]=evJson;
                		
                }
                nodesString.setBranches(branchJsons);
            } 
            if (next!=null)
            	nodesString=next;// move pointer ahead
        }
        return root;
       
    }
       
    /**
     * Searches a matching sequence in the tree
     *
     * @param eventSeq
     *            Tree
     * @param newSeq
     *            New sequence
     * @return True if a sequence matches, else false
     */
    public Boolean searchMatchingSequenceInTree(Event[] eventSeq, String[] newSeq) {

        Integer seqSize = newSeq.length;
        Integer j;
        for (j = 0; j < seqSize; j++) {
            if (!eventSeq[j].getEvent().equals(newSeq[j])) {
                break;
            }
        }

        Integer matchIdx = j - 1;

        if (matchIdx >= seqSize - 1) {
            return true;
        } else if (matchIdx < 0) {
            return false; // return null if mismatch on the first idx
        } else {

            String[] newTmpSeq = new String[seqSize - matchIdx - 1];
            Boolean isFound = false;
            Integer i;
            for (i = 0; i < newTmpSeq.length - 1; i++) {
                // That is copy from the next index of matched index
                newTmpSeq[i] = newSeq[matchIdx + i + 1];
            }

            ArrayList<Event[]> branches = eventSeq[matchIdx].getBranches();
            // When there are no more branches then we shall automatically
            // add a new branch by skipping the if block
            if (branches != null) {
                // if the branches exist then we need to recursively go through
                // the remaining branches to find
                // a possible location to append the new sequence

                for (int bCount = 0; bCount < branches.size(); bCount++) {
                    Event[] branchEventSeq = branches.get(bCount);
                    // / ****** recursive call
                    isFound = searchMatchingSequenceInTree(branchEventSeq, newTmpSeq);
                    // / ****** recursive call
                    if (isFound == true) {
                        break;
                    }

                }
            }

            if (isFound) {
                return true;
            }
            return false;

        }
        // End of function searchMatchingSequence
    }

    /**
     * Searches a new sequence in the tree and returns the Hamming distance. If
     * Hamming distance is zero then a sequence matched; otherwise, Hamming
     * distance is equal to the number of mismatches
     *
     * @param nodes
     *            Tree of events
     * @param newSeq
     *            Sequence to search
     * @return Hamming distance
     */
    public Integer getHammingAndSearch(Event[] nodes, Integer[] newSeq) {

        Integer hammDis = 0, minHammDis = 100000000;// Initializing minimum
                                                    // Hamming distance with a
                                                    // very large number

        for (int nodeCount = 0; nodeCount < newSeq.length; nodeCount++) {

            ArrayList<Event[]> branches = nodes[nodeCount].getBranches();
            if (!nodes[nodeCount].getEvent().equals(newSeq[nodeCount])) {
                // System.out.print (", x "+nodes[nodeCount].getEvent());
                hammDis++;
            }
            // else
            // System.out.print (", =="+nodes[nodeCount].getEvent());

            if (branches != null) { // if there are branches on an event then
                                    // keep
                Integer[] newTmpSeq = new Integer[newSeq.length - nodeCount - 1];
                for (int i = 0; i < newTmpSeq.length; i++)
                {
                    newTmpSeq[i] = newSeq[nodeCount + i + 1];// that is copy
                                                             // from the next
                                                             // index of matched
                                                             // index
                }

                for (int i = 0; i < branches.size(); i++) {
                    // System.out.println();
                    Integer branchHamming = getHammingAndSearch(branches.get(i), newTmpSeq);
                    branchHamming = branchHamming + hammDis;// add the
                                                            // mismatches that
                                                            // have been found
                                                            // before this
                                                            // branch
                    if (branchHamming == 0) { // there is no need to get further
                                              // branches
                        minHammDis = 0; // we have found a match, as hamming is
                                        // 0
                        break;
                    }
                    else if (branchHamming < minHammDis) {
                        minHammDis = branchHamming;
                    }

                }
            }
        }

        if (hammDis < minHammDis) {
            minHammDis = hammDis;
        }

        return minHammDis;
    }

    // End of class
}
