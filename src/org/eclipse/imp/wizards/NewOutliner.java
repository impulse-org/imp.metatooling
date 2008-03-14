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

public class NewOutliner extends CodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.IMP_RUNTIME, "outliner"), });
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] {
                "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.imp.runtime", "org.eclipse.ui", "org.eclipse.jface.text", 
                "org.eclipse.ui.editors", "org.eclipse.ui.workbench.texteditor", "lpg.runtime" });
    }

    
    public void generateCodeStubs(IProgressMonitor mon) throws CoreException
    {	
        // SMS 6 Apr 2007
        // Modifying this call to getStandardSubstitutions to provide project
        // which is needed to get plugin-related substitutions that may be needed
        // in the "images" class that will be generated along with the outliner
        Map subs= getStandardSubstitutions(fProject);

        subs.put("$PARSER_PKG$", fParserPackage);
        subs.put("$AST_PKG$", fParserPackage + "." + Wizards.astDirectory);
        subs.put("$AST_NODE$", Wizards.astNode);
        
        subs.remove("$OUTLINER_CLASS_NAME$");
        subs.put("$OUTLINER_CLASS_NAME$", fFullClassName); //className);
        
        subs.remove("$PACKAGE_NAME$");
        subs.put("$PACKAGE_NAME$", fPackageName);

        String outlinerTemplateName = "outliner.java";
        IFile outlinerSrc= WizardUtilities.createFileFromTemplate(fFullClassName + ".java", outlinerTemplateName, fPackageFolder, getProjectSourceLocation(fProject), subs, fProject, mon);
        
        String imagesTemplateName = "images.java";
        WizardUtilities.createFileFromTemplate(fClassNamePrefix + "Images.java", imagesTemplateName, fPackageFolder, getProjectSourceLocation(fProject), subs, fProject, mon);
        WizardUtilities.copyLiteralFile("../icons/outline_item.gif", "icons", fProject, mon);

        editFile(mon, outlinerSrc);
    }
    
  
}