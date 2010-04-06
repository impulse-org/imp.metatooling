/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
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

/**
 * A wizard class to create a service implementation for the "toggleBreakpointHandler" extension point.
 * @author rmfuhrer
 */
public class NewBreakpointHandler extends CodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] {
            new ExtensionPointWizardPage(this, RuntimePlugin.IMP_RUNTIME, "toggleBreakpointHandler"),
        });
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] {
                "org.eclipse.core.runtime", "org.eclipse.core.resources",
                "org.eclipse.imp.runtime", "org.eclipse.debug.core"
        });
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
        Map<String, String> subs= getStandardSubstitutions();
        subs.remove("$HANDLER_CLASS_NAME$");
        subs.put("$HANDLER_CLASS_NAME$", fFullClassName);
        subs.remove("$PACKAGE_NAME$");
        subs.put("$PACKAGE_NAME$", fPackageName);
        String folderTemplateName= "toggleBreakpointHandler.java";
        IFile folderSrc= createFileFromTemplate(fFullClassName + ".java", folderTemplateName, fPackageFolder, subs, fProject, mon);
        editFile(mon, folderSrc);
    }
}
