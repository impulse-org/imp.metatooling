/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

/**
 * 
 */
package org.eclipse.imp.wizards;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class NoCodeServiceWizard extends ExtensionPointWizard {
    public void generateCodeStubs(IProgressMonitor m) {}

    protected Map getStandardSubstitutions() {
        return Collections.EMPTY_MAP; // noone should be calling this: generateCodeStubs() is empty...
    }
}