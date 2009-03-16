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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.runtime.RuntimePlugin;

public class NewAutoEditStrategy extends CodeServiceWizard {
    private String fAutoEditClassName;

    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.IMP_RUNTIME, "autoEditStrategy"), });
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.imp.runtime" });
    }

    @Override
    protected void collectCodeParms() {
        super.collectCodeParms();

        fAutoEditClassName= this.fLanguageName + "AutoEditStrategy";
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
        Map<String, String> subs= getStandardSubstitutions(fProject);
            
        subs.put("$AUTO_EDIT_CLASS_NAME$", fAutoEditClassName);

        IFile specFile= createFileFromTemplate(fAutoEditClassName + ".java", "autoEditStrategy.java", fPackageFolder, subs, fProject, mon);

        this.editFile(mon, specFile);
    }
}