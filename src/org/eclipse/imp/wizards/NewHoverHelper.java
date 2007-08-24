/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/**
 * 
 */
package org.eclipse.imp.wizards;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class NewHoverHelper extends CodeServiceWizard {

    protected String fHoverHelperFileName = null;
    protected boolean fResolveReferences = false;
    protected boolean fCustomizeContent = false;
    protected String fDocProviderQualifiedClassName = null;
    protected String fDocProviderPackageName = null;
    protected String fDocProviderClassName = null;
    protected String fLexerClassName = null;
	
    public void addPages() {
        //addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.IMP_RUNTIME, "hoverHelper"), });
    	addPages(new ExtensionPointWizardPage[] { new NewHoverHelperWizardPage(this) } );
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.imp.runtime" });
    }

    
    protected void collectCodeParms() {
    	super.collectCodeParms();
    	NewHoverHelperWizardPage page= (NewHoverHelperWizardPage) pages[0];
        
        //fProject=page.getProject();
//    	IProject project = null;	
//    	String projectName = page.getProjectNameFromField();
//    	if (projectName != null) {
//    		project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
//    	}
//    	if (project ==  null) {
//    		project= page.getProject();
//    	}
//        if (project == null) {
//        	throw new IllegalStateException(
//        		"NewHoverHelperWizardPage.collectCodeParms():  project cannot be identified.");
//        }
//    	fProject = project;
//        fLanguageName = page.getValue("language");
//        String qualifiedClassName = page.getValue("class");
//	    fPackageName= qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
//        fPackageFolder= fPackageName.replace('.', File.separatorChar);
//        fClassNamePrefix = Character.toUpperCase(fLanguageName.charAt(0)) + fLanguageName.substring(1);
//        //fControllerFileName = fClassNamePrefix + "ParseController.java";
//        //fLocatorFileName = fClassNamePrefix + "ASTNodeLocator.java";
//		fHoverHelperFileName = fClassNamePrefix + "HoverHelper.java";
    	
    	fResolveReferences = page.getResolveReferences();
    	
    	fCustomizeContent = page.getCustomizeContent();
//    	if (fCustomizeContent) {
//    		fDocProviderQualifiedClassName = page.getDocumentationProvider();
//    	    fDocProviderPackageName = fDocProviderQualifiedClassName.substring(0, fDocProviderQualifiedClassName.lastIndexOf('.'));
//    	    fDocProviderClassName = fDocProviderQualifiedClassName.substring(fDocProviderQualifiedClassName.lastIndexOf('.') + 1);
//    	}
    }

    
    public void generateCodeStubs(IProgressMonitor monitor) throws CoreException
    {	
//		boolean hasKeywords= fGrammarOptions.getHasKeywords();
//		boolean requiresBacktracking= fGrammarOptions.getRequiresBacktracking();
//		boolean autoGenerateASTs= fGrammarOptions.getAutoGenerateASTs();
//		String templateKind= fGrammarOptions.getTemplateKind();
//
//	    String parseCtlrTemplateName= "SeparateParseController.java";
//		String locatorTemplateName = "SeparateASTNodeLocator.java";

    		Map subs= getStandardSubstitutions();

//		subs.put("$AST_PKG_NODE$", fPackageName + "." + astDirectory + "." + astNode);
//		subs.put("$AST_NODE$", astNode);
//		subs.put("$PARSER_TYPE$", fClassNamePrefix + "Parser");
		
		subs.remove("$PACKAGE_NAME$");
		subs.put("$PACKAGE_NAME$", fPackageName);
		subs.put("$LEXER_CLASS_NAME$", fClassNamePrefix + "Lexer");
		
		subs.remove("$HOVER_HELPER_CLASS_NAME$");
		subs.put("$HOVER_HELPER_CLASS_NAME$", fFullClassName);
	
		subs.put("$USE_REFERENCE_RESOLVER$", fResolveReferences ? "true" : "false");
		subs.put("$USE_DOCUMENTATION_PROVIDER$", fCustomizeContent ? "true" : "false");	
		
//		if (fCustomizeContent) {
//			subs.put("$DOCUMENTATION_PROVIDER_CLASS_NAME$", fDocProviderClassName);
//			subs.put("$DOCUMENTATION_PROVIDER_PACKAGE_NAME$", fDocProviderPackageName);
//		}
		
		subs.put("$PARSER_PKG$", fParserPackage);

		String hoverHelperTemplateName = "hoverHelper.java";
//		String docProviderTemplateName = "documentationProvider.java";
//		String docPackageFolder = fDocProviderPackageName.replace('.', File.separatorChar);;
		
		IFile hoverHelperFile = createFileFromTemplate(fFullClassName + ".java", hoverHelperTemplateName, fPackageFolder, subs, fProject, monitor);
//		IFile docProviderFile = createFileFromTemplate(fDocProviderClassName + ".java", docProviderTemplateName, docPackageFolder, subs, fProject, monitor);
		
		// Need to enable documentationProvider extension "manually"
//		ExtensionPointEnabler.enable(
//			fProject, "org.eclipse.imp.runtime", "documentationProvider",
//			 new String[][] {
//	                { "extension:id", fProject.getName() + ".documentationProvider" },
//	                { "extension:name", fLanguageName + " Documentation Provider" },
//	                { "docProvider:class", fDocProviderPackageName + "." + fDocProviderClassName },
//	                { "docProvider:language", fLanguageName} },
//	         false, getPluginDependencies(), new NullProgressMonitor());
		
		editFile(monitor, hoverHelperFile);
//		editFile(monitor, docProviderFile);
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
    	
    	String prefix = fProject.getLocation().toString() + '/' + getProjectSourceLocation();
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
    
}