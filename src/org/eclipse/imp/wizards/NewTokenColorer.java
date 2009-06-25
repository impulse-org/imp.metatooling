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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.runtime.RuntimePlugin;

public class NewTokenColorer extends CodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.IMP_RUNTIME, "tokenColorer"), });
    }

    protected List<String> getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
                "org.eclipse.imp.runtime", "org.eclipse.ui", "org.eclipse.jface.text", "lpg.runtime"});
    }
    
    
    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
        Map<String, String> subs= getStandardSubstitutions();

        subs.put("$PARSER_PKG$", fParserPackage);

        subs.remove("$COLORER_CLASS_NAME$");
        subs.put("$COLORER_CLASS_NAME$", fFullClassName);

        subs.remove("$PACKAGE_NAME$");
        subs.put("$PACKAGE_NAME$", fPackageName);

        IFile colorerSrc= createFileFromTemplate(fFullClassName + ".java", fTemplateName, fPackageFolder, subs, fProject, mon);

        editFile(mon, colorerSrc);
    }
}
