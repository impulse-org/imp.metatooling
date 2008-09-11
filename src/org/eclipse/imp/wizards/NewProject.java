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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class NewProject extends CodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new NewProjectWizardPage(this) });
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] {
                "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.imp.runtime", "org.eclipse.ui", "org.eclipse.jdt.core", "org.eclipse.jdt.ui" });
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
        ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
        IProject project= page.getProjectOfRecord();
        Map subs= getStandardSubstitutions();

        subs.put("$NATURE_ID$", fLanguageName + ".nature");

        String projectTemplateName = "newProjectWizard.java";
        createFileFromTemplate(
        	fClassNamePrefix + "ProjectWizard.java", projectTemplateName, fPackageFolder, subs, project, mon);
        IFile projectFirstPageSrc= createFileFromTemplate(
        	fClassNamePrefix + "ProjectWizardFirstPage.java", "newProjectWizardFirstPage.java", fPackageFolder, subs, project, mon);
        IFile projectSecondPageSrc= createFileFromTemplate(
        	fClassNamePrefix + "ProjectWizardSecondPage.java", "newProjectWizardSecondPage.java", fPackageFolder, subs, project, mon);

        editFile(mon, projectFirstPageSrc);
    }
}