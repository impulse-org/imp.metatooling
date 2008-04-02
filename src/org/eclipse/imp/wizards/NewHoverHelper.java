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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class NewHoverHelper extends CodeServiceWizard {

    protected String fHoverHelperFileName = null;
    protected boolean fResolveReferences = false;
    protected String fRefResolverQualifiedClassName = null;
    protected String fRefResolverPackageName = null;
    protected String fRefResolverClassName = null;
    protected boolean fCustomizeContent = false;
    protected String fDocProviderQualifiedClassName = null;
    protected String fDocProviderPackageName = null;
    protected String fDocProviderClassName = null;
    protected String fLexerClassName = null;
    
    protected NewHoverHelperWizardPage fPage = null;
	
    public void addPages() {
        //addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.IMP_RUNTIME, "hoverHelper"), });
    	addPages(new ExtensionPointWizardPage[] { new NewHoverHelperWizardPage(this) } );
    	fPage = (NewHoverHelperWizardPage) pages[0];
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.imp.runtime" });
    }

    
    protected void collectCodeParms() {
    	super.collectCodeParms();
    	NewHoverHelperWizardPage page= (NewHoverHelperWizardPage) pages[0];
    	
    	fResolveReferences = page.getUseReferenceResolver();
    	if (!fPage.getUseReferenceResolver()) {
    		fRefResolverQualifiedClassName = page.getReferenceResolver();
    	    fRefResolverPackageName = fRefResolverQualifiedClassName.substring(0, fRefResolverQualifiedClassName.lastIndexOf('.'));
    	    fRefResolverClassName = fRefResolverQualifiedClassName.substring(fRefResolverQualifiedClassName.lastIndexOf('.') + 1);
    	}
    	
    	fCustomizeContent = page.getUseCustomProvider();
    	if (!fPage.getUseCustomProvider()) {
    		fDocProviderQualifiedClassName = page.getDocumentationProvider();
    	    fDocProviderPackageName = fDocProviderQualifiedClassName.substring(0, fDocProviderQualifiedClassName.lastIndexOf('.'));
    	    fDocProviderClassName = fDocProviderQualifiedClassName.substring(fDocProviderQualifiedClassName.lastIndexOf('.') + 1);
    	}
    }

    
    public void generateCodeStubs(IProgressMonitor monitor) throws CoreException
    {	
    	// (needed for update in hover-helper template)
    	Map subs= getStandardSubstitutions(fProject);

		subs.remove("$PACKAGE_NAME$");
		subs.put("$PACKAGE_NAME$", fPackageName);
		subs.put("$LEXER_CLASS_NAME$", fClassNamePrefix + "Lexer");
		
		subs.remove("$HOVER_HELPER_CLASS_NAME$");
		subs.put("$HOVER_HELPER_CLASS_NAME$", fFullClassName);
	
		subs.put("$USE_REFERENCE_RESOLVER$", fResolveReferences ? "true" : "false");
		subs.put("$USE_DOCUMENTATION_PROVIDER$", fCustomizeContent ? "true" : "false");	
		
		
		subs.put("$PARSER_PKG$", fParserPackage);

		String hoverHelperTemplateName = "hoverHelper.java";
		IFile hoverHelperFile = WizardUtilities.createFileFromTemplate(fFullClassName + ".java", hoverHelperTemplateName, fPackageFolder, getProjectSourceLocation(fProject), subs, fProject, monitor);
//		ExtensionPointEnabler.enable(pages[0], false, monitor);
		ExtensionPointEnabler.enable(
				fProject, "org.eclipse.imp.runtime", "hoverHelper",
				 new String[][] {
		                { "extension:id", fProject.getName() + ".hoverHelper" },
		                { "extension:name", fLanguageName + " Hover Helper" },
		                { "hoverHelper:class", fPackageName + "." + fFullClassName },
		                { "hoverHelper:language", fLanguageName} },
		         false, getPluginDependencies(), new NullProgressMonitor());

		
		IFile docProviderFile = null;
		IFile refResolverFile = null;

		if (!fPage.getGenReferenceResolver()) {
			String refResolverTemplateName = "reference_resolver.java";
			String refResolverPackageFolder = fRefResolverPackageName.replace('.', File.separatorChar);
			
			subs.remove("$PACKAGE_NAME$");
			subs.put("$PACKAGE_NAME$", fRefResolverPackageName);
	        subs.remove("$REFERENCE_RESOLVER_CLASS_NAME$");
	        subs.put("$REFERENCE_RESOLVER_CLASS_NAME$", fRefResolverClassName);
			
			refResolverFile = WizardUtilities.createFileFromTemplate(fRefResolverClassName + ".java", refResolverTemplateName, refResolverPackageFolder, getProjectSourceLocation(fProject), subs, fProject, monitor);
			
			// Need to enable documentationProvider extension "manually"
			ExtensionPointEnabler.enable(
				fProject, "org.eclipse.imp.runtime", "referenceResolvers",
				 new String[][] {
		                { "extension:id", fProject.getName() + ".referenceResolver" },
		                { "extension:name", fLanguageName + " Reference Resolver" },
		                { "docProvider:class", fRefResolverPackageName + "." + fRefResolverClassName },
		                { "docProvider:language", fLanguageName} },
		         false, getPluginDependencies(), new NullProgressMonitor());
		}
		
		
		// SMS 28 Mar 2008:  uncommented line and added conditional
		if (!fPage.getGenCustomProvider()) {
			// SMS 28 Mar 2008:  uncommented 2 lines
			String docProviderTemplateName = "documentationProvider.java";
			String docPackageFolder = fDocProviderPackageName.replace('.', File.separatorChar);
			
			subs.put("$DOCUMENTATION_PROVIDER_CLASS_NAME$", fDocProviderClassName);
			subs.put("$DOCUMENTATION_PROVIDER_PACKAGE_NAME$", fDocProviderPackageName);
			
			docProviderFile = WizardUtilities.createFileFromTemplate(fDocProviderClassName + ".java", docProviderTemplateName, docPackageFolder, getProjectSourceLocation(fProject), subs, fProject, monitor);
			
			// Need to enable documentationProvider extension "manually"
			ExtensionPointEnabler.enable(
				fProject, "org.eclipse.imp.runtime", "documentationProvider",
				 new String[][] {
		                { "extension:id", fProject.getName() + ".documentationProvider" },
		                { "extension:name", fLanguageName + " Documentation Provider" },
		                { "docProvider:class", fDocProviderPackageName + "." + fDocProviderClassName },
		                { "docProvider:language", fLanguageName} },
		         false, getPluginDependencies(), new NullProgressMonitor());
		}
		
		
		editFile(monitor, hoverHelperFile);
		if (docProviderFile != null)
			editFile(monitor, docProviderFile);
		if (refResolverFile != null)
			editFile(monitor, refResolverFile);

    }
    
    
    /**
     * Overrides method in ExtensionPointWizard.
     *
     * Provides a basic implementation that returns the name of the
     * one class that will definitely be generated by a specific wizard.	
     * 
     * Subclasses should override if the wizard will generate more than
     * one implementation class.
     * 
     * @return	An array of names of existing files that would be clobbered by
     * 			the new files to be generated
     */
    protected String[] getFilesThatCouldBeClobbered() {
    	
    	String[] superFiles = super.getFilesThatCouldBeClobbered();
    	
    	// Check whether a documentationProvider class will be
    	// created here
    	if (fDocProviderQualifiedClassName == null) {
    		// If not, then super result is fine
    		return superFiles;
    	}
    	
    	// In case there is	
    	
    	String prefix = fProject.getLocation().toString() + '/' + getProjectSourceLocation(fProject);
    	// getProjectSourceLocation should return a "/"-terminated string
    	String prefixTail = (fPackageName == null ? "/" : fPackageName.replace('.', '/') + "/");
    	prefix = prefix + prefixTail; 
    	String fullDocProivderFileName = prefix + fDocProviderQualifiedClassName + ".java";

    	String[] result = new String[superFiles.length+1];
    	for (int i = 0; i < superFiles.length; i++) {
    		result[i] = superFiles[i];
    	}
    	result[superFiles.length] = fullDocProivderFileName;
    	return result;
    }	
    
    
    
    
    public boolean performFinish() {
    	collectCodeParms(); // Do this in the UI thread while the wizard fields are still accessible
    	
    	IRunnableWithProgress op= new IRunnableWithProgress() {
    	    public void run(IProgressMonitor monitor) throws InvocationTargetException {
    		IWorkspaceRunnable wsop= new IWorkspaceRunnable() {
    		    public void run(IProgressMonitor monitor) throws CoreException {
    			try {
    			    generateCodeStubs(monitor);
    			} catch (Exception e) {
    			    ErrorHandler.reportError("Could not add extension points", e);
    			} finally {
    			    monitor.done();
    			}
    		    }
    		};
    		try {
    		    ResourcesPlugin.getWorkspace().run(wsop, monitor);
    		} catch (Exception e) {
    		    ErrorHandler.reportError("Could not add extension points", e);
    		}
    	    }
    	};
    	try {
    	    getContainer().run(true, false, op);
    	} catch (InvocationTargetException e) {
    	    Throwable realException= e.getTargetException();
    	    ErrorHandler.reportError("Error", realException);
    	    return false;
    	} catch (InterruptedException e) {
    	    return false;
    	}
    	return true;
        }
    
    
    
}