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

// SMS 7 Aug 2008
// This class has a very minor role to play, mainly providing a little
// computational efficiency and helping to categorized a subset of
// our wizards.  Is it worth keeping for that reason?  (Do we imagine
// any other purpose it might serve?)

public abstract class NoCodeServiceWizard extends ExtensionPointWizard {
    public void generateCodeStubs(IProgressMonitor m) {}

    // SMS 7 Aug 2008
    // This represents a minor efficiency.  It avoids the computation of the standard
    // standard substitutions whenever one of these wizards is finished.  But the wizards
    // will run properly even if the standard standard substitutions are computed.
    public Map<String, String> getStandardSubstitutions() {
        return  Collections.emptyMap(); // no one should be calling this: generateCodeStubs() is empty...
    }
}