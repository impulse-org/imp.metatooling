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

public class NewDocumentationProvider extends CodeServiceWizard {
	
    protected String fDocProviderQualifiedClassName = null;
    protected String fDocProviderPackageName = null;
    protected String fDocProviderClassName = null;
	
    public void addPages() {
	addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.IMP_RUNTIME, "documentationProvider"), });
    }

    protected List getPluginDependencies() {
		return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
			"org.eclipse.imp.runtime", "org.eclipse.ui", "org.eclipse.jface.text", "lpg.runtime"});
    }
    
    
    protected void collectCodeParms() {
    	super.collectCodeParms();
//    	NewHoverHelperWizardPage page= (NewHoverHelperWizardPage) pages[0];
    	
		fDocProviderQualifiedClassName = pages[0].getField("class").getText();
	    fDocProviderPackageName = fDocProviderQualifiedClassName.substring(0, fDocProviderQualifiedClassName.lastIndexOf('.'));
	    fDocProviderClassName = fDocProviderQualifiedClassName.substring(fDocProviderQualifiedClassName.lastIndexOf('.') + 1);
    }
    
    public void generateCodeStubs(IProgressMonitor mon) throws CoreException
    {
		Map subs= getStandardSubstitutions();
		
		subs.put("$PARSER_PKG$", fParserPackage);
		subs.put("$LEXER_CLASS_NAME$", fClassNamePrefix + "Lexer");
		subs.put("$DOCUMENTATION_PROVIDER_CLASS_NAME$", fDocProviderClassName);
		subs.put("$DOCUMENTATION_PROVIDER_PACKAGE_NAME$", fDocProviderPackageName);
	
		String providerTemplateName = "documentationProvider.java";
		IFile providerSrc= WizardUtilities.createFileFromTemplate(fFullClassName + ".java", providerTemplateName, fPackageFolder, getProjectSourceLocation(fProject), subs, fProject, mon);
	
		editFile(mon, providerSrc);
    }
    
    
}
