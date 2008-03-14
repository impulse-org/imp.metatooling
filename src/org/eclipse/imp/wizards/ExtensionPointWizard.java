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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.utils.StreamUtils;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import sun.misc.FpUtils;


/**
 * This wizard creates a new file resource in the provided container. 
 * The wizard creates one file with the extension "g". 
 */
public abstract class ExtensionPointWizard extends IMPWizard implements INewWizard
{
    private static final String START_HERE= "// START_HERE";
    
    protected ExtensionPointWizardPage pages[];

    protected int NPAGES;
    protected IProject fProject;

	protected String fProjectName;
    protected String fLanguageName;
    protected String fPackageName;
    protected String fPackageFolder;
    protected String fParserPackage;
    // SMS 	13 Apr 2007:  refactored fClassName -> fClassNamePrefix
    // to better reflect actual use
    protected String fClassNamePrefix;
    protected String fFullClassName;

    
    
    
    public ExtensionPointWizard() {
	super();
	setNeedsProgressMonitor(true);
    }

    public int getPageCount() {
	return NPAGES;
    }

    protected void addPages(ExtensionPointWizardPage[] pages) {
		this.pages= pages;
		NPAGES= pages.length;
		for(int n= 0; n < pages.length; n++) {
		    addPage(pages[n]);
		}
		List/*<String>*/extenRequires= getPluginDependencies();
		for(Iterator/*<String>*/iter= extenRequires.iterator(); iter.hasNext();) {
		    String requiredPlugin= (String) iter.next();
		    for(int n= 0; n < pages.length; n++) {	
			List/*<String>*/pageRequires= pages[n].getRequires();
			pageRequires.add(requiredPlugin);
		    }
		}
    }

    public IWizardPage getPreviousPage(IWizardPage page) {
	if (currentPage == 0)
	    return null;
	return pages[currentPage];
    }

    public IWizardPage getNextPage(IWizardPage page) {
	if (currentPage == pages.length - 1)
	    return null;
	return pages[++currentPage];
    }

    public boolean canFinish() {
	return super.canFinish();// pages[currentPage].isPageComplete() && (currentPage >= pages.length - 1);
    }

    /**
     * @return the list of plugin dependencies for this language service.
     */
    protected abstract List getPluginDependencies();

    /**
     * Generate any necessary code for this extension from template files in the
     * templates directory.<br>
     * Implementations can use <code>getTemplateFile(String)</code> to access the
     * necessary template files.<br>
     * Implementations must be careful not to access the fields of the wizard page,
     * as this code will probably be called from a thread other than the UI thread.
     * I.e., don't write something like:<br>
     * <code>pages[0].languageText.getText()</code><br>
     * Instead, in the wizard class, override <code>collectCodeParams()</code>,
     * which gets called earlier from the UI thread, and save any necessary data
     * in fields in the wizard class.
     * @param monitor
     * @throws CoreException
     */
    protected abstract void generateCodeStubs(IProgressMonitor mon) throws CoreException;

    /**
     * Implementers of generateCodeStubs() should override this to collect any
     * necessary information from the fields in the various wizard pages needed
     * to generate code.
     */
    protected void collectCodeParms() {}
    

    // SMS 13 Apr 2007
    // Added methods and calls related to checking for existing
    // files that would be clobbered by generated files
       
    /**
     * Returns (in an array of Strings) the names of files that will be
     * generated by the SAFARI wizard and that thus may clobber existing
     * files.
     * 
     * The basic implementation provided here simply returns an array with
     * the name of the one class that will provide the core implementation
     * of the service.  (It seems that this is all that is necessary for
     * most wizards.)  If the wizard actually generates no implementation
     * class, then an emtpy array is returned.
     * 
     * Subclasses for specific wizards should override this method if the
     * wizard will generate more than one class.
     * 
     * @return	An array of names files that will be generated by the wizard
     */
    protected String[] getFilesThatCouldBeClobbered() {
    	
    	// In case there's not any implementation class ...
    	if (fFullClassName == null) {
    		return new String[0];
    	}
    	
    	// In the usual case that there is ...
    	
    	String prefix = fProject.getLocation().toString() + '/' + getProjectSourceLocation(fProject);
    	// getProjectSourceLocation should return a "/"-terminated string
    	String prefixTail = (fPackageName == null ? "/" : fPackageName.replace('.', '/') + "/");

    	return new String[] {prefix + prefixTail + fFullClassName + ".java" };
    }	
   
 

    /**
     * This method is called when 'Finish' button is pressed in the wizard.
     * We will create an operation and run it using wizard as execution context.
     */
    public boolean performFinish() {
    	// Do the following in the UI thread while the wizard fields are
    	// still accessible and dialogs are still possible
		collectCodeParms();
		// NOTE:  Invoke after collectCodeParms() so that collectCodeParms()
		// collect collect the names of files from the wizard
    	if (!okToClobberFiles(getFilesThatCouldBeClobbered()))
    		return false;
    	
		IRunnableWithProgress op= new IRunnableWithProgress() {
		    public void run(IProgressMonitor monitor) throws InvocationTargetException {
			IWorkspaceRunnable wsop= new IWorkspaceRunnable() {
			    public void run(IProgressMonitor monitor) throws CoreException {
				try {
				    for(int n= 0; n < pages.length; n++) {
					ExtensionPointWizardPage page= pages[n];
	
					// BUG Make sure the extension ID is correctly set
					if (!page.hasBeenSkipped() && page.fSchema != null)
					    ExtensionPointEnabler.enable(page, false, monitor);
				    }
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


    public void init(IWorkbench workbench, IStructuredSelection selection) {}


    /**
     * Opens the given file in the appropriate editor for editing.<br>
     * If the file contains a comment "// START_HERE", the cursor will
     * be positioned just after that.
     * @param monitor
     * @param file
     */
    public void editFile(IProgressMonitor monitor, final IFile file) {
	monitor.setTaskName("Opening file for editing...");
	getShell().getDisplay().asyncExec(new Runnable() {
	    public void run() {
		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
		    IEditorPart editorPart= IDE.openEditor(page, file, true);
		    AbstractTextEditor editor= (AbstractTextEditor) editorPart;
		    IFileEditorInput fileInput= (IFileEditorInput) editorPart.getEditorInput();
		    String contents= StreamUtils.readStreamContents(file.getContents(), file.getCharset());
		    int cursor= contents.indexOf(START_HERE);

		    if (cursor >= 0) {
			TextSelection textSel= new TextSelection(editor.getDocumentProvider().getDocument(fileInput), cursor, START_HERE.length());
			editor.getEditorSite().getSelectionProvider().setSelection(textSel);
		    }
		} catch (PartInitException e) {
		} catch (CoreException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	});
	monitor.worked(1);
    }
     
    


    
    protected abstract Map getStandardSubstitutions();
    
 
    /**
     * Check whether it's okay for the files to be generated to clobber
     * any existing files.
     * 
     * Current implementation expects that the file names provided will
     * be the full absolute path names in the file system.
     * 
     * @param files		The names of files that would be clobbered by
     * 					files to be generated
     * @return			True if there are no files that would be clobbered
     * 					or if the users presses OK; false if there are
     * 					files and the user presses CANCEL
     */
    protected boolean okToClobberFiles(String[] files) {
    	if (files.length == 0)
    		return true;
    	String message = "File(s) with the following name(s) already exist; do you want to overwrite?\n";
    	boolean askUser = false;
    	for (int i = 0; i < files.length; i++) {
    		File file = new File(files[i]);
    		if (file.exists()) {
    			askUser = true;
    			message = message + "\n" + files[i];
    		}
    	}
    	if (!askUser)
    		return true;
    	Shell parent = this.getShell();
    	MessageBox messageBox = new MessageBox(parent, (SWT.CANCEL | SWT.OK));
    	messageBox.setMessage(message);
    	int result = messageBox.open();
    	if (result == SWT.CANCEL)
    		return false;
    	return true;
    }

}
