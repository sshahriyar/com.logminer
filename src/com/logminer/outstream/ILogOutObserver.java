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

import com.logminer.outstream.LogOutStream;

/**
 * An interface that is required to be implemented by the class that displays
 * the output of the algorithms
 *
 * @author <p>
 *         Syed Shariyar Murtaza justsshary@hotmail.com
 *         </p>
 *
 */
public interface ILogOutObserver {
    /**
     * This method gets called from the {@link LogOutStream}
     *
     * @param message
     *            The message to be displayed
     */
    public void updateOutput(String message);
}
