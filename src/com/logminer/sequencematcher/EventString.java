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

//import org.eclipse.tracecompass.internal.totalads.algorithms.sequencematching.Event;

/**
 * This class represents an event of a trace in a tree and the corresponding
 * branches of events
 *
 * @author <p>
 *         Syed Shariyar Murtaza justsshary@hotmail.com
 *         </p>
 *
 */
class EventString {

    // Data variables
    private String fEvent="";
    private ArrayList<EventString[]> fBranches;

    /** constructor */
    public EventString() {
    }

    /**
     * Returns an event
     *
     * @return event
     */
    public String getEvent() {
        return fEvent;
    }

    /**
     * Sets an event
     *
     * @param event
     *            string
     */
    public void setEvent(String event) {
        this.fEvent = event;
    }

    /**
     * Returns the branch of events
     *
     * @return ArrayList of events
     */
    public ArrayList<EventString[]> getBranches() {
        return fBranches;
    }

    /**
     * Sets the branch of events
     *
     * @param branchesAtEvent
     *            Arraylist of events
     */
    public void setBranches(ArrayList<EventString[]> branchesAtEvent) {
        this.fBranches = branchesAtEvent;
    }

}
