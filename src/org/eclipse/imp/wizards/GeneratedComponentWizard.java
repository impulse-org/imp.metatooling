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

package org.eclipse.imp.wizards;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.ui.INewWizard;

/**
 * This wizard supports the generation of one or more implementation
 * classes for language or IDE services that are not extensions
 */
public abstract class GeneratedComponentWizard extends IMPWizard implements INewWizard {
    protected GeneratedComponentAttribute[] fWizardAttributes;

    public GeneratedComponentWizard() {
		super();
		setNeedsProgressMonitor(true);
    }
    
    // SMS 6 Aug 2008:  Used in several generated-component wizards
    // but not really needed anymore as the wizard page (which uses
    // the attributes) can now tolerate a null value for the attribute array.
    // Should probably remove where still called.
    public GeneratedComponentAttribute[] setupAttributes() {
    	// Warning:  Returning an array with empty elements may cause problems,
    	// so be sure to only allocate as many elements as there are actual	attributes
    	GeneratedComponentAttribute[] attributes = new GeneratedComponentAttribute[0];
    	return attributes;
    }	

    
    /**
     * This method is called when 'Finish' button is pressed in the wizard.
     * We will create an operation and run it using wizard as execution context.
     * 
     * This method is quite a bit simpler than the corresponding method for
     * ExtensionPointWizard since no extensions have to be created here.
     */
    public boolean performFinish() {
    	collectCodeParms(); // Do this in the UI thread while the wizard fields are still accessible
		// NOTE:  Invoke after collectCodeParms() so that collectCodeParms()
		// collect collect the names of files from the wizard
    	if (!okToClobberFiles(getFilesThatCouldBeClobbered()))
    		return false;
    	// Do we need to do just this in a runnable?
    	try {
    		generateCodeStubs(new NullProgressMonitor());
    	} catch (Exception e){
		    ErrorHandler.reportError("GeneratedComponentWizard.performFinish:  Could not generate code stubs", e);
		    return false;
    	}
    			
		return true;
    }
}
