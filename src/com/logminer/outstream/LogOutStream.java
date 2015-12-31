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

package com.logminer.outstream;

import java.util.ArrayList;

import com.logminer.outstream.ILogOutObserver;
import com.logminer.outstream.ILogOutStream;

/**
 * This class provides concrete functions to output messages to a class
 * implementing {@link ILogOutObserver} interface. This makes it
 * independent of the display mechanism, which could be a simple console, GUI, a
 * web page, a log file or any thing else.
 *
 * @author <p>
 *         Syed Shariyar Murtaza justsshary@hotmail.com
 *         </p>
 *
 */
public class LogOutStream implements ILogOutStream {

    private ArrayList<ILogOutObserver> fObservers;

    /**
     * Constructor
     */
    public LogOutStream() {
        fObservers = new ArrayList<>();
    }

    /**
     * Adds new event to the output stream
     *
     * @param event
     *            Information to display on the output stream
     */
    @Override
    public void addOutputEvent(String event) {
        notifyObservers(event);

    }

    /**
     * Adds new line to the output Stream
     */
    @Override
    public void addNewLine() {
        notifyObservers("\n"); //$NON-NLS-1$
    }

    /**
     * Adds an observer of type {@link ILogOutObserver}
     *
     * @param observer
     *            Observer
     */
    public void addObserver(ILogOutObserver observer) {
        fObservers.add(observer);

    }

    /**
     * Removes an observer of type {@link ILogOutObserver}
     *
     * @param observer
     *            Observer
     */

    public void removeObserver(ILogOutObserver observer) {
        fObservers.remove(observer);

    }

    /**
     *
     * Notifies all observers of type
     *
     * @param event
     *            Message to observers
     */
    private void notifyObservers(String event) {
        for (ILogOutObserver ob : fObservers) {
            ob.updateOutput(event);
        }
    }
}
