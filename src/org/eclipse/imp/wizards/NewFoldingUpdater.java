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

public class NewFoldingUpdater extends CodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.IMP_RUNTIME, "foldingUpdater"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.imp.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
            Map subs= getStandardSubstitutions();

            subs.put("$PARSER_PKG$", fParserPackage);

            subs.remove("$FOLDER_CLASS_NAME$");
            subs.put("$FOLDER_CLASS_NAME$", fFullClassName);
            
            subs.remove("$PACKAGE_NAME$");
            subs.put("$PACKAGE_NAME$", fPackageName);
          
            String folderTemplateName = fTemplateName;	//"folder.java";
            IFile folderSrc = createFileFromTemplate(fFullClassName + ".java", folderTemplateName, fPackageFolder, subs, fProject, mon);
            
            editFile(mon, folderSrc);
	}


}