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

import org.eclipse.osgi.util.NLS;

import com.logminer.reader.adlogs.Messages;
/**
 * Message Bundle
 *
 * @author <p>
 *         Syed Shariyar Murtaza justsshary@hotmail.com
 *         </p>
 *
 */
@SuppressWarnings("javadoc")

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.logminer.reader.adlogs..messages"; //$NON-NLS-1$
    public static String TextLineTraceReader_NoNull;
    public static String TextLineTraceReader_TextReaderName;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
