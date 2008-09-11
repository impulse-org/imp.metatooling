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

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.imp.WizardPlugin;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.utils.ExtensionPointUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundleModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.osgi.framework.Bundle;

/**
 * This wizard supports the generation of one or more implementation
 * classes for language or IDE services that are not extensions
 */
public abstract class GeneratedComponentWizard extends IMPWizard implements INewWizard {
//    private static final String START_HERE= "// START_HERE";

    // SMS 14 Nov 2007:  	now declared in IMPWizard
    //protected int currentPage;
    
//    protected GeneratedComponentWizardPage pages[];
    protected GeneratedComponentAttribute[] fWizardAttributes;

    public GeneratedComponentWizard() {
		super();
		setNeedsProgressMonitor(true);
    }

    
    
    /* *********************************************************
     * The following several methods address the handling
     * of pages for the wizard
     */
    

//    public void init(IWorkbench workbench, IStructuredSelection selection) {}

    
//    public void setPage(int page) {
//    	currentPage= page;
//        }
//
//    
//    public int getPageCount() {
//	return NPAGES;
//    }

//    protected void addPages(GeneratedComponentWizardPage[] pages) {
//		this.pages= pages;
//		NPAGES= pages.length;
//		for(int n= 0; n < pages.length; n++) {
//		    addPage(pages[n]);
//		}
//		List/*<String>*/extenRequires= getPluginDependencies();
//		for(Iterator/*<String>*/iter= extenRequires.iterator(); iter.hasNext();) {
//		    String requiredPlugin= (String) iter.next();
//		    for(int n= 0; n < pages.length; n++) {
//			List/*<String>*/pageRequires= pages[n].getRequires();
//			pageRequires.add(requiredPlugin);
//		    }
//		}
//    }
//
//    public IWizardPage getPreviousPage(IWizardPage page) {
//	if (currentPage == 0)
//	    return null;
//	return pages[currentPage];
//    }
//
//    public IWizardPage getNextPage(IWizardPage page) {
//	if (currentPage == pages.length - 1)
//	    return null;
//	return pages[++currentPage];
//    }


    // SMS 6 Aug 2008:  Used in several generated-component wizards
    // but not really needed anymore as the wizard page (which uses
    // the attributes) can now tolerate a null value for the attribute array.
    // Should probably remove where still called.
    public GeneratedComponentAttribute[] setupAttributes()
    {
    	// Warning:  Returning an array with empty elements may cause problems,
    	// so be sure to only allocate as many elements as there are actual	attributes
    	GeneratedComponentAttribute[] attributes = new GeneratedComponentAttribute[0];
    	return attributes;
    }	
    
    
    /* *********************************************************
     * The following several methods address the "Finish" of the wizard
     * and the generation of code stubs for a service implementation
     */

    
//    public boolean canFinish() {
//    	return super.canFinish();// pages[currentPage].isPageComplete() && (currentPage >= pages.length - 1);
//    }


//    /**
//     * @return the list of plugin dependencies for this language service.
//     */
//    protected abstract List getPluginDependencies();
        

    
//    /**
//     * Generate any necessary code for this extension from template files in the
//     * templates directory.<br>
//     * Implementations can use <code>getTemplateFile(String)</code> to access the
//     * necessary template files.<br>
//     * Implementations must be careful not to access the fields of the wizard page,
//     * as this code will probably be called from a thread other than the UI thread.
//     * I.e., don't write something like:<br>
//     * <code>pages[0].languageText.getText()</code><br>
//     * Instead, in the wizard class, override <code>collectCodeParams()</code>,
//     * which gets called earlier from the UI thread, and save any necessary data
//     * in fields in the wizard class.
//     * @param monitor
//     * @throws CoreException
//     */
//    protected abstract void generateCodeStubs(IProgressMonitor mon) throws CoreException;

   
//    /**
//     * Collects basic information from wizard-page fields and computes
//     * additional common values for use by wizards in generating code.
//     * 
//     * Can be extended by subclasses for specific wizards in order to
//     * gather wizard-specific values.
//     */
//    protected void collectCodeParms() {
//    	fProject = pages[0].getProjectOfRecord();
//    	fProjectName = pages[0].fProjectText.getText();
//        fLanguageName= pages[0].fLanguageText.getText();
//        
//        if (pages[0].fTemplateText != null)
//        	fTemplateName = pages[0].fTemplateText.getText();
//        
//        fClassNamePrefix= Character.toUpperCase(fLanguageName.charAt(0)) + fLanguageName.substring(1);
//        
//		String qualifiedClassName= pages[0].getField("class").fValue;
//		fFullClassName = qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.') + 1);
//		fPackageName= qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
//		fPackageFolder= fPackageName.replace('.', File.separatorChar);
//
//		fParserPackage = CodeServiceWizard.discoverParserPackage(fProject);
//    }
    
    
    /**
     * This method is called when 'Finish' button is pressed in the wizard.
     * We will create an operation and run it using wizard as execution context.
     * 
     * This method is quite a bit simpler than the corresponding method for
     * ExtensionPointWizard since no extensions have to be created here.
     */
    public boolean performFinish()
    {
    	collectCodeParms(); // Do this in the UI thread while the wizard fields are still accessible
		// NOTE:  Invoke after collectCodeParms() so that collectCodeParms()
		// collect collect the names of files from the wizard
    	if (!okToClobberFiles(getFilesThatCouldBeClobbered()))
    		return false;
    	// Do we need to do just this in a runnable?
    	try {
    		generateCodeStubs(new NullProgressMonitor());
    	} catch (Exception e){
		    ErrorHandler.reportError("GeneratedComponentWizard.performFinish:  Could not generate code stubs", e);
		    return false;
    	}
    			
		return true;
    }

    
    
    /* *********************************************************
     * The following several methods address the handling support the
     * generation of implementation code
     */
 
    
//    /**
//     * Opens the given file in the appropriate editor for editing.<br>
//     * If the file contains a comment "// START_HERE", the cursor will
//     * be positioned just after that.
//     * @param monitor
//     * @param file
//     */
//    protected void editFile(IProgressMonitor monitor, final IFile file)
//    {
//    	WizardUtilities.editFile(monitor, file, getShell());
//    }
    
    

//    public IFile createFileFromTemplate(
//    	String fileName, String templateName, String folder, Map replacements,
//	    IProject project, IProgressMonitor monitor)
//    throws CoreException
//	{
//    	return createFileFromTemplate(fileName, templateName, folder, getProjectSourceLocation(project), replacements, project, monitor);
//    }
//    
//    
//    protected IFile createFileFromTemplate(
//    		String fileName, String templateBundleID, String templateName, String folder,
//    		Map replacements, IProject project, IProgressMonitor monitor) throws CoreException
//    {
//    	return createFileFromTemplate(
//    			fileName, templateBundleID, templateName, folder, getProjectSourceLocation(project),
//    			replacements, project, monitor);
//    }
//    
    
    
    



    
    // SMS 6 Aug 2008:  This seems to be unused
//    public static void replace(StringBuffer sb, String target, String substitute) {
//	for(int index= sb.indexOf(target); index != -1; index= sb.indexOf(target))
//	    sb.replace(index, index + target.length(), substitute);
//    }


    // SMS 6 Aug 2008:  Only used in the unused getTemplateFile(..)
//    protected static String getTemplateBundleID() {
//    	return WizardPlugin.kPluginID;
//    }

    
    // SMS 6 Aug 2008:  Seems to be unused
//    protected byte[] getTemplateFile(String fileName) {
//	try {
//	    Bundle bundle= Platform.getBundle(getTemplateBundleID());
//	    URL templateURL= Platform.find(bundle, new Path("/templates/" + fileName));
//            if (templateURL == null) {
//                ErrorHandler.reportError("Unable to find template file: " + fileName, true);
//                return new byte[0];
//            }
//            URL url= Platform.asLocalURL(templateURL);
//	    String path= url.getPath();
//	    FileInputStream fis= new FileInputStream(path);
//	    DataInputStream is= new DataInputStream(fis);
//	    byte bytes[]= new byte[fis.available()];
//
//	    is.readFully(bytes);
//	    is.close();
//	    fis.close();
//	    return bytes;
//	} catch (Exception e) {
//	    e.printStackTrace();
//	    return ("// missing template file: " + fileName).getBytes();
//	}
//    }
    
    
    
    /* *********************************************************
     * The following several methods are used to compute values
     * for substitution parameters in implementation templates
     */
    

//    public Map<String,String> getStandardSubstitutions(IProject project) {
//    	Map<String, String> result = getStandardSubstitutions();
//    	result.remove("$PLUGIN_PACKAGE$");
//        result.put("$PLUGIN_PACKAGE$", getPluginPackageName(project, null));
//    	result.remove("$PLUGIN_CLASS$");
//        result.put("$PLUGIN_CLASS$", getPluginClassName(project, null));
//        result.remove("$PLUGIN_ID$");
//        result.put("$PLUGIN_ID$", getPluginID(project, null));
//        return result;
//    }
//    
//    
//    
//    public Map<String, String> getStandardSubstitutions() {
//        Map<String,String> result = new HashMap<String,String>();
//        
//        result = ExtensionPointUtils.getASTInformation((IPluginModel)pages[0].getPluginModel(), fProject);
//
//        result.put("$LANG_NAME$", fLanguageName);
//        result.put("$CLASS_NAME_PREFIX$", fClassNamePrefix);
//        result.put("$PACKAGE_NAME$", fPackageName);
//        result.put("$PROJECT_NAME$", fProjectName);
//
//        // NOTE:  These are default values for plug-in substitutions;
//        // they should typically be overridden by real values obtained
//        // from getStandardSubstitutions(IProject).  That means that
//        // that method should be called after this one (or that one
//        // should call this one before setting those values).
//        result.put("$PLUGIN_PACKAGE$", getPluginPackageName(null, null));
//        result.put("$PLUGIN_CLASS$", getPluginClassName(null, null));
//        result.put("$PLUGIN_ID$", getPluginID(null, null));
//
//        return result;
//    }
  
 
    
    /* *********************************************************
     * The following two methods are used to check whether it's ok
     * for files that are about to be generated to clobber files
     * that already exist. 
     */
    
//    /**
//     * Returns (in an array of Strings) the names of files that will be
//     * generated by the SAFARI wizard and that thus may clobber existing
//     * files.
//     * 
//     * The basic implementation provided here simply returns an array with
//     * the name of the one class that will provide the core implementation
//     * of the service.  (It seems that this is all that is necessary for
//     * most wizards.)  If the wizard actually generates no implementation
//     * class, then an emtpy array is returned.
//     * 
//     * Subclasses for specific wizards should override this method if the
//     * wizard will generate more than one class.
//     * 
//     * @return	An array of names files that will be generated by the wizard
//     */
//    protected String[] getFilesThatCouldBeClobbered() {
//    	
//    	// In case there's not any implementation class ...
//    	if (fFullClassName == null) {
//    		return new String[0];
//    	}
//    	
//    	// In the usual case that there is ...
//    	
//    	String prefix = fProject.getLocation().toString() + '/' + getProjectSourceLocation(fProject);
//    	// getProjectSourceLocation should return a "/"-terminated string
//    	String prefixTail = (fPackageName == null ? "/" : fPackageName.replace('.', '/') + "/");
//
//    	return new String[] {prefix + prefixTail + fFullClassName + ".java" };
//    }	
   
     
    
//    /**
//     * Check whether it's okay for the files to be generated to clobber
//     * any existing files.
//     * 
//     * Current implementation expects that the file names provided will
//     * be the full absolute path names in the file system.
//     * 
//     * @param files		The names of files that would be clobbered by
//     * 					files to be generated
//     * @return			True if there are no files that would be clobbered
//     * 					or if the users presses OK; false if there are
//     * 					files and the user presses CANCEL
//     */
//    protected boolean okToClobberFiles(String[] files) {
//    	if (files.length == 0)
//    		return true;
//    	String message = "File(s) with the following name(s) already exist; do you want to overwrite?\n";
//    	boolean askUser = false;
//    	for (int i = 0; i < files.length; i++) {
//    		File file = new File(files[i]);
//    		if (file.exists()) {
//    			askUser = true;
//    			message = message + "\n" + files[i];
//    		}
//    	}
//    	if (!askUser)
//    		return true;
//    	Shell parent = this.getShell();
//    	MessageBox messageBox = new MessageBox(parent, (SWT.CANCEL | SWT.OK));
//    	messageBox.setMessage(message);
//    	int result = messageBox.open();
//    	if (result == SWT.CANCEL)
//    		return false;
//    	return true;
//    }
    
    
}
