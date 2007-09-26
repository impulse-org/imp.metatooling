/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.wizards;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.WizardPlugin;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.utils.StreamUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.osgi.framework.Bundle;


/**
 * This wizard creates a new file resource in the provided container. 
 * The wizard creates one file with the extension "g". 
 */
public abstract class ExtensionPointWizard extends Wizard implements INewWizard
{
    private static final String START_HERE= "// START_HERE";

    protected int currentPage;
    
    protected ExtensionPointWizardPage pages[];

    protected int NPAGES;

    // SMS 13 Apr 2007
    // Can be set by collectCodeParms(), which can get it
    // from the page along with the parameters
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
    	
    	String prefix = fProject.getLocation().toString() + '/' + getProjectSourceLocation();
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

    public void setPage(int page) {
	currentPage= page;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {}

// WizardUtilities
//    public static void addBuilder(IProject project, String id) throws CoreException {
//		IProjectDescription desc= project.getDescription();
//		ICommand[] commands= desc.getBuildSpec();
//		for(int i= 0; i < commands.length; ++i)
//		    if (commands[i].getBuilderName().equals(id))
//			return;
//		//add builder to project
//		ICommand command= desc.newCommand();
//		command.setBuilderName(id);
//		ICommand[] nc= new ICommand[commands.length + 1];
//		// Add it before other builders.
//		System.arraycopy(commands, 0, nc, 1, commands.length);
//		nc[0]= command;
//		desc.setBuildSpec(nc);
//		project.setDescription(desc, null);
//    }
//
//    public static void enableBuilders(IProgressMonitor monitor, final IProject project, final String[] builderIDs) {
//	monitor.setTaskName("Enabling builders...");
//	Job job= new WorkspaceJob("Enabling builders...") {
//	    public IStatus runInWorkspace(IProgressMonitor monitor) {
//		try {
//		    for(int i= 0; i < builderIDs.length; i++) {
//			addBuilder(project, builderIDs[i]);
//		    }
//		} catch (Throwable e) {
//		    e.printStackTrace();
//		}
//		return Status.OK_STATUS;
//	    }
//	};
//	job.schedule();
//    }

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
    
// Wizard utilities
//    public static void editFile(IProgressMonitor monitor, final IFile file, Shell shell) {
//    	monitor.setTaskName("Opening file for editing...");
//    	shell.getDisplay().asyncExec(new Runnable() {
//    	    public void run() {
//    		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//    		try {
//    		    IEditorPart editorPart= IDE.openEditor(page, file, true);
//    		    AbstractTextEditor editor= (AbstractTextEditor) editorPart;
//    		    IFileEditorInput fileInput= (IFileEditorInput) editorPart.getEditorInput();
//    		    String contents= StreamUtils.readStreamContents(file.getContents(), file.getCharset());
//    		    int cursor= contents.indexOf(START_HERE);
//
//    		    if (cursor >= 0) {
//    			TextSelection textSel= new TextSelection(editor.getDocumentProvider().getDocument(fileInput), cursor, START_HERE.length());
//    			editor.getEditorSite().getSelectionProvider().setSelection(textSel);
//    		    }
//    		} catch (PartInitException e) {
//    		} catch (CoreException e) {
//    		    // TODO Auto-generated catch block
//    		    e.printStackTrace();
//    		}
//    	    }
//    	});
//    	monitor.worked(1);
//        }
//    
    
    // SMS 13 Apr 2007
    // A step toward relaxing assumptions about the location
    // of source files within the project
    public static String getProjectSourceLocation() {
    		return "src/";
    }
    
    
 
//    /**
//     * Creates a file of the given name from the named template in the given folder in the
//     * given project. Subjects the template's contents to meta-variable substitution.
//     * 
//     * SMS 5 Sep 2007:  When working on NewPreferencesSpecificationWizard, I had occasion
//     * to override ExstensionPointWizard.createFileFromTemplate(..) because that rendition
//     * of the method had some elements specific to the creation of Java files and I wasn't
//     * creating a Java file (or package).  Later I changed NewPreferencesSpecificationWizard
//     * so that it did create a Java package, and I tried relying on createFileFromTemplate(..)
//     * from ExtensionPointWizard.  Strangely (it seemed to me) I found that that version of
//     * the method was creating a plain folder rather than a package folder.  I can't tell
//     * why that was happening, and it evidently doesn't happen when we invoke the method to
//     * generate Java files (which get generated into package folders).  In contrast, the
//     * version of createFileFromTemplate(..) that I'd adpated for NewPreferencesSpecificationWizard
//     * did create a package folder, even though I'd done nothing in particular to achieve that
//     * result(not suspecting that there was anything that needed to, or could, be done).
//     * On the assumption that we probably want to use ExtensionPointWizard to create Java
//     * packages even when we're not immediately creating Java classes, I've substituted the
//     * alternative version of createFileFromTemplate(..) here.
//     * 
//     * @param fileName		Unqualified name of the new file being created
//     * @param templateName	Short (unqualified) name of the template file to be used
//     * 						in creating the new file
//     * @param folder		Name of the folder in which the new file will be created
//     * 						(presumably a package folder)
//     * @param replacements  A Map of meta-variable substitutions to apply to the template
//     * @param project		The project in which the new file will be created
//     * @param monitor		A monitor
//     * @return 				A handle to the file created
//     * @throws CoreException
//     */
//	public static IFile createFileFromTemplate(
//		String fileName, String templateName, String folder, Map replacements,
//	    IProject project, IProgressMonitor monitor)
//	throws CoreException
//	{
//		monitor.setTaskName("ExtensionPointWizard.createFileFromTemplate:  Creating " + fileName);
//	
//		String packagePath = getProjectSourceLocation() + folder.replace('.', '/');
//		IPath specFilePath = new Path(packagePath + "/" + fileName);
//		
//		final IFile file= project.getFile(specFilePath);
//		String templateContents= new String(getTemplateFileContents(templateName));
//		String contents= performSubstitutions(templateContents, replacements);
//	
//		if (fileName.endsWith(".java")) {
//			contents= formatJavaCode(contents);
//		}
//		
//		if (file.exists()) {
//		    file.setContents(new ByteArrayInputStream(contents.getBytes()), true, true, monitor);
//		} else {
//		    createSubFolders(packagePath, project, monitor);
//		    file.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
//		}
//	//	monitor.worked(1);
//		return file;
//	}
//
//    
//    /**
//     * Creates a file of the given name from the named template in the given folder in the
//     * given project.  The template is sought in the bundle identified by the given template
//     * bundle Id.  Subjects the template's contents to meta-variable substitution.
//	 *
//     * This version of the method allows for relaxation of the assumption that the templates
//     * are found in the bundle in which this class is found (org.eclipse.imp.metatooling).
//     * 
//     * @param fileName		Unqualified name of the new file being created
//     * @param templatePluginId	The id of the plugin that contains the "templates" folder
//     * 						in which the named template file is to be found
//     * @param templateName	Short (unqualified) name of the template file to be used
//     * 						in creating the new file
//     * @param folder		Name of the folder in which the new file will be created
//     * 						(presumably a package folder)
//     * @param replacements  A Map of meta-variable substitutions to apply to the template
//     * @param project		The project in which the new file will be created
//     * @param monitor		A monitor
//     * @return 				A handle to the file created
//     * @throws CoreException
//     * 	
//     */
//	public static IFile createFileFromTemplate(
//			String fileName, String templateBundleId, String templateName, String folder, Map replacements,
//		    IProject project, IProgressMonitor monitor)
//		throws CoreException
//		{
//			monitor.setTaskName("ExtensionPointWizard.createFileFromTemplate:  Creating " + fileName);
//		
//			String packagePath = getProjectSourceLocation() + folder.replace('.', '/');
//			IPath specFilePath = new Path(packagePath + "/" + fileName);
//			final IFile file= project.getFile(specFilePath);
//		
//			String templateContents= new String(WizardUtilities.getTemplateFileContents(templateBundleId, templateName));
//			String contents= WizardUtilities.performSubstitutions(templateContents, replacements);
//		
//			if (fileName.endsWith(".java")) {
//				contents= WizardUtilities.formatJavaCode(contents);
//			}
//			
//			if (file.exists()) {
//			    file.setContents(new ByteArrayInputStream(contents.getBytes()), true, true, monitor);
//			} else {
//			    WizardUtilities.createSubFolders(packagePath, project, monitor);
//			    file.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
//			}
//		//	monitor.worked(1);
//			return file;
//		}
//	
//	
//    
//    /**
//     * Extends a file of the given name from the named template in the given folder in the
//     * given project. Subjects the template's contents to meta-variable substitution.
//     * @param fileName
//     * @param templateName
//     * @param folder
//     * @param replacements a Map of meta-variable substitutions to apply to the template
//     * @param project
//     * @param monitor
//     * @return a handle to the extended file
//     * @throws CoreException
//     * 
//     * SMS 19 Jun 2007:  Added to support extension of existing files with updates by
//     * 					 later introduced language services (e.g., of language plugin file
//     * 					 with elements related to the preference service--which isn't actually
//     * 					 done yet, but which might reasonably be introduced)
//     */
//    protected IFile extendFileFromTemplate(
//    	String fileName, String templateName, String folder, Map replacements,
//	    IProject project, IProgressMonitor monitor)
//    throws CoreException
//	{
//		monitor.setTaskName("ExtensionPointWizard.extendFileFromTemplate:  Extending " + fileName);
//		
//		final IFile file= project.getFile(new Path(getProjectSourceLocation() + folder + "/" + fileName));
//		if (!file.exists()) {
//			throw new IllegalArgumentException();	
//		}
//		String fileContents = file.getContents().toString();
//		fileContents = fileContents.substring(0, fileContents.lastIndexOf("}")) + "\n";
//		
//		String extensionContents= new String(WizardUtilities.getTemplateFileContents(templateName));
//		extensionContents = WizardUtilities.performSubstitutions(extensionContents, replacements);
//	
//		String newFileContents = fileContents + extensionContents + "\n\n}";
//		
//		if (fileName.endsWith(".java")) {
//			newFileContents= WizardUtilities.formatJavaCode(newFileContents);
//		}
//
//	    file.setContents(new ByteArrayInputStream(newFileContents.getBytes()), true, true, monitor);
//
//		return file;
//    }
    
    
// Wizard utilities    
//    private static String formatJavaCode(String contents) {
//	CodeFormatter formatter= org.eclipse.jdt.core.ToolFactory.createCodeFormatter(JavaCore.getOptions());
//	TextEdit te= formatter.format(CodeFormatter.K_COMPILATION_UNIT, contents, 0, contents.length(), 0, "\n");
//
//	IDocument l_doc= new Document(contents);
//	try {
//	    te.apply(l_doc);
//	} catch (MalformedTreeException e) {
//	    e.printStackTrace();
//	} catch (BadLocationException e) {
//	    e.printStackTrace();
//	} catch (NullPointerException e) {
//		// Can happen that te is null
//		e.printStackTrace();
//	}
//	contents= l_doc.get();
//	return contents;
//    }

//    /**
//     * Like createFileFromTemplate, but does not attempt to perform any meta-variable substitutions.
//     * Useful for binary files (e.g. images) that are to be copied as-is to the user's workspace.
//     */
//    protected IFile copyLiteralFile(String fileName, String folder, IProject project, IProgressMonitor monitor) throws CoreException {
//	monitor.setTaskName("Creating " + fileName);
//
//	final IFile file= project.getFile(new Path(folder + "/" + fileName));
//	byte[] fileContents= WizardUtilities.getTemplateFileContents(fileName);
//
//	if (file.exists()) {
//	    file.setContents(new ByteArrayInputStream(fileContents), true, true, monitor);
//	} else {
//			WizardUtilities.createSubFolders(folder, project, monitor);
//	    file.create(new ByteArrayInputStream(fileContents), true, monitor);
//	}
////	monitor.worked(1);
//	return file;
//    }

// Wizard utilities    
//    public static void createSubFolders(String folder, IProject project, IProgressMonitor monitor) throws CoreException {
//        String[] subFolderNames= folder.split("[\\" + File.separator + "\\/]");
//        String subFolderStr= "";
//
//        for(int i= 0; i < subFolderNames.length; i++) {
//            String childPath= subFolderStr + "/" + subFolderNames[i];
//            Path subFolderPath= new Path(childPath);
//            IFolder subFolder= project.getFolder(subFolderPath);
//
//            if (!subFolder.exists())
//                subFolder.create(true, true, monitor);
//            subFolderStr= childPath;
//        }
//    }

// Wizard utilities
//    public static void replace(StringBuffer sb, String target, String substitute) {
//	for(int index= sb.indexOf(target); index != -1; index= sb.indexOf(target))
//	    sb.replace(index, index + target.length(), substitute);
//    }
//
//    public static String performSubstitutions(String contents, Map replacements) {
//	StringBuffer buffer= new StringBuffer(contents);
//
//	for(Iterator iter= replacements.keySet().iterator(); iter.hasNext();) {
//	    String key= (String) iter.next();
//	    String value= (String) replacements.get(key);
//
//	    if (value != null)
//		replace(buffer, key, value);
//	}
//	return buffer.toString();
//    }

// Wizard utilities
//    protected static String getTemplateBundleID() {
//    	return WizardPlugin.kPluginID;
//    }

// Wizard utilities
//    protected static byte[] getTemplateFileContents(String fileName) {
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
//
//    /**
//     * Gets the contents of a named template file from the "templates" folder
//     * of a plugin with a given plugin id.  Created for use with the version of
//     * createFileFromTemplate(..) that also takes a plugin id.
//     * 
//     * @param templateBundleId	The id of the plugin that contains the templates
//     * 							folder in which the template is to be found
//     * @param fileName			The name of the template file for which contents
//     * 							are to be returned
//     * @return					The contents of the named template file
//     */
//    public static byte[] getTemplateFileContents(String templateBundleId, String fileName) {
//    	try {
//    	    Bundle bundle= Platform.getBundle(templateBundleId);
//    	    URL templateURL= Platform.find(bundle, new Path("/templates/" + fileName));
//                if (templateURL == null) {
//                    ErrorHandler.reportError("Unable to find template file: " + fileName, true);
//                    return new byte[0];
//                }
//                URL url= Platform.asLocalURL(templateURL);
//    	    String path= url.getPath();
//    	    FileInputStream fis= new FileInputStream(path);
//    	    DataInputStream is= new DataInputStream(fis);
//    	    byte bytes[]= new byte[fis.available()];
//
//    	    is.readFully(bytes);
//    	    is.close();
//    	    fis.close();
//    	    return bytes;
//    	} catch (Exception e) {
//    	    e.printStackTrace();
//    	    return ("// missing template file: " + fileName).getBytes();
//    	}
//    }
    
    
    
    
    
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
