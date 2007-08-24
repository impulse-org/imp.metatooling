/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
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

public class NewReferenceResolver extends CodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] {
        	new ExtensionPointWizardPage(this, RuntimePlugin.IMP_RUNTIME, "referenceResolvers"), });
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.imp.runtime" });
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
    	
        Map subs= getStandardSubstitutions();

        subs.put("$PARSER_PKG$", fParserPackage);

        subs.remove("$PACKAGE_NAME$");
        subs.put("$PACKAGE_NAME$", fPackageName);
        
        String resolverTemplateName = "reference_resolver.java";
        IFile resolverSrc = createFileFromTemplate(
        	fClassNamePrefix + "ReferenceResolver.java", resolverTemplateName, fPackageFolder, subs, fProject, mon);

        editFile(mon, resolverSrc);
    }
    
   
}