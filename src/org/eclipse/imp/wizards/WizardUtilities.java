package org.eclipse.imp.wizards;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.osgi.framework.Bundle;

public class WizardUtilities {

	// Probably need to think about how to treat this in a more
	// principled manner (but the value is needed in one or more
	// of these methods)
    private static final String START_HERE= "// START_HERE";


    /**
     * Returns the bundle id for the bundle that contains this
     * utility class along with (presumably) most of the rest of
     * the IMP wizards.  Presently that is the bundle id for the
     * plugin org.eclipse.imp.metatooling.  Making it available here
     * means that the wizards in this plugin won't have to provide
     * it as a parameter in calls to several of the methods.
     */
    protected static String getTemplateBundleID() {
    	return WizardPlugin.kPluginID;
    }


	/**
	 * Provide a means to get some presumed, default value for the
	 * source location in an unspecified project.  This is really a
	 * stopgap measure until or unless better approaches are available.	
	 * 
	 * @return 	A default value for the source location in an
	 * 			unspecified project
	 */
    public static String getProjectSourceLocation() {
    		return "src/";
    }
	
    
    /**
     * Creates a file of the given name from the named template in the given folder in the
     * given project. Subjects the template's contents to meta-variable substitution.
     * 
     * SMS 5 Sep 2007:  When working on NewPreferencesSpecificationWizard, I had occasion
     * to override ExstensionPointWizard.createFileFromTemplate(..) because that rendition
     * of the method had some elements specific to the creation of Java files and I wasn't
     * creating a Java file (or package).  Later I changed NewPreferencesSpecificationWizard
     * so that it did create a Java package, and I tried relying on createFileFromTemplate(..)
     * from ExtensionPointWizard.  Strangely (it seemed to me) I found that that version of
     * the method was creating a plain folder rather than a package folder.  I can't tell
     * why that was happening, and it evidently doesn't happen when we invoke the method to
     * generate Java files (which get generated into package folders).  In contrast, the
     * version of createFileFromTemplate(..) that I'd adpated for NewPreferencesSpecificationWizard
     * did create a package folder, even though I'd done nothing in particular to achieve that
     * result(not suspecting that there was anything that needed to, or could, be done).
     * On the assumption that we probably want to use ExtensionPointWizard to create Java
     * packages even when we're not immediately creating Java classes, I've substituted the
     * alternative version of createFileFromTemplate(..) here.
     * 
     * @param fileName		Unqualified name of the new file being created
     * @param templateName	Short (unqualified) name of the template file to be used
     * 						in creating the new file
     * @param folder		Name of the folder in which the new file will be created
     * 						(presumably a package folder)
     * @param replacements  A Map of meta-variable substitutions to apply to the template
     * @param project		The project in which the new file will be created
     * @param monitor		A monitor
     * @return 				A handle to the file created
     * @throws CoreException
     */
	public static IFile createFileFromTemplate(
		String fileName, String templateName, String folder, String projectSourceLocation,
		Map replacements, IProject project, IProgressMonitor monitor)
	throws CoreException
	{
		monitor.setTaskName("ExtensionPointWizard.createFileFromTemplate:  Creating " + fileName);
	
		String packagePath = projectSourceLocation + folder.replace('.', '/');
		IPath specFilePath = new Path(packagePath + "/" + fileName);
		
		final IFile file= project.getFile(specFilePath);
		String templateContents= new String(getTemplateFileContents(templateName));
		String contents= performSubstitutions(templateContents, replacements);
	
		if (fileName.endsWith(".java")) {
			contents= formatJavaCode(contents);
		}
		
		if (file.exists()) {
		    file.setContents(new ByteArrayInputStream(contents.getBytes()), true, true, monitor);
		} else {
		    createSubFolders(packagePath, project, monitor);
		    file.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
		}
	//	monitor.worked(1);
		return file;
	}
	
	
	
    /**
     * Creates a file of the given name from the named template in the given folder in the
     * given project.  The template is sought in the bundle identified by the given template
     * bundle Id.  Subjects the template's contents to meta-variable substitution.
	 *
     * This version of the method allows for relaxation of the assumption that the templates
     * are found in the bundle in which this class is found (org.eclipse.imp.metatooling).
     * 
     * @param fileName		Unqualified name of the new file being created
     * @param templatePluginId	The id of the plugin that contains the "templates" folder
     * 						in which the named template file is to be found
     * @param templateName	Short (unqualified) name of the template file to be used
     * 						in creating the new file
     * @param folder		Name of the folder in which the new file will be created
     * 						(presumably a package folder)
     * @param replacements  A Map of meta-variable substitutions to apply to the template
     * @param project		The project in which the new file will be created
     * @param monitor		A monitor
     * @return 				A handle to the file created
     * @throws CoreException
     * 	
     */
	public static IFile createFileFromTemplate(
			String fileName, String templateBundleId, String templateName, String folder, String projectSourceLocation,
			Map replacements, IProject project, IProgressMonitor monitor)
		throws CoreException
	{
		monitor.setTaskName("ExtensionPointWizard.createFileFromTemplate:  Creating " + fileName);
	
		String packagePath = projectSourceLocation + folder.replace('.', '/');
		IPath specFilePath = new Path(packagePath + "/" + fileName);
		final IFile file= project.getFile(specFilePath);
	
		String templateContents= new String(getTemplateFileContents(templateBundleId, templateName));
		String contents= performSubstitutions(templateContents, replacements);
	
		if (fileName.endsWith(".java")) {
			contents= formatJavaCode(contents);
		}
		
		if (file.exists()) {
		    file.setContents(new ByteArrayInputStream(contents.getBytes()), true, true, monitor);
		} else {
		    createSubFolders(packagePath, project, monitor);
		    file.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
		}
	//	monitor.worked(1);
		return file;
	}
	
	
    
    /**
     * Extends a file of the given name from the named template in the given folder in the
     * given project. Subjects the template's contents to meta-variable substitution.
     * @param fileName
     * @param templateName
     * @param folder
     * @param replacements a Map of meta-variable substitutions to apply to the template
     * @param project
     * @param monitor
     * @return a handle to the extended file
     * @throws CoreException
     * 
     * SMS 19 Jun 2007:  Added to support extension of existing files with updates by
     * 					 later introduced language services (e.g., of language plugin file
     * 					 with elements related to the preference service--which isn't actually
     * 					 done yet, but which might reasonably be introduced)
     */
    protected IFile extendFileFromTemplate(
    	String fileName, String templateName, String folder, String projectSourceLocation, Map replacements,
	    IProject project, IProgressMonitor monitor)
    throws CoreException
	{
		monitor.setTaskName("ExtensionPointWizard.extendFileFromTemplate:  Extending " + fileName);
		
		final IFile file= project.getFile(new Path(projectSourceLocation + folder + "/" + fileName));
		if (!file.exists()) {
			throw new IllegalArgumentException();	
		}
		String fileContents = file.getContents().toString();
		fileContents = fileContents.substring(0, fileContents.lastIndexOf("}")) + "\n";
		
		String extensionContents= new String(WizardUtilities.getTemplateFileContents(templateName));
		extensionContents = WizardUtilities.performSubstitutions(extensionContents, replacements);
	
		String newFileContents = fileContents + extensionContents + "\n\n}";
		
		if (fileName.endsWith(".java")) {
			newFileContents= WizardUtilities.formatJavaCode(newFileContents);
		}

	    file.setContents(new ByteArrayInputStream(newFileContents.getBytes()), true, true, monitor);

		return file;
    }
	
	
	
    /**
     * Like createFileFromTemplate, but does not attempt to perform any meta-variable substitutions.
     * Useful for binary files (e.g. images) that are to be copied as-is to the user's workspace.
     */
    protected static IFile copyLiteralFile(
    	String fileName, String folder, IProject project, IProgressMonitor monitor)
    throws CoreException
    {
		monitor.setTaskName("Creating " + fileName);
	
		final IFile file= project.getFile(new Path(folder + "/" + fileName));
		byte[] fileContents= WizardUtilities.getTemplateFileContents(fileName);
	
		if (file.exists()) {
		    file.setContents(new ByteArrayInputStream(fileContents), true, true, monitor);
		} else {
				WizardUtilities.createSubFolders(folder, project, monitor);
		    file.create(new ByteArrayInputStream(fileContents), true, monitor);
		}
	//	monitor.worked(1);
		return file;
    }
	
	
    public static void addBuilder(IProject project, String id) throws CoreException {
		IProjectDescription desc= project.getDescription();
		ICommand[] commands= desc.getBuildSpec();
		for(int i= 0; i < commands.length; ++i)
		    if (commands[i].getBuilderName().equals(id))
			return;
		//add builder to project
		ICommand command= desc.newCommand();
		command.setBuilderName(id);
		ICommand[] nc= new ICommand[commands.length + 1];
		// Add it before other builders.
		System.arraycopy(commands, 0, nc, 1, commands.length);
		nc[0]= command;
		desc.setBuildSpec(nc);
		project.setDescription(desc, null);
    }

    public static void enableBuilders(IProgressMonitor monitor, final IProject project, final String[] builderIDs) {
	monitor.setTaskName("Enabling builders...");
	Job job= new WorkspaceJob("Enabling builders...") {
	    public IStatus runInWorkspace(IProgressMonitor monitor) {
		try {
		    for(int i= 0; i < builderIDs.length; i++) {
			addBuilder(project, builderIDs[i]);
		    }
		} catch (Throwable e) {
		    e.printStackTrace();
		}
		return Status.OK_STATUS;
	    }
	};
	job.schedule();
    }
    
    
    
    public static void editFile(IProgressMonitor monitor, final IFile file, Shell shell) {
    	monitor.setTaskName("Opening file for editing...");
    	shell.getDisplay().asyncExec(new Runnable() {
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
    
    
    public static void createSubFolders(String folder, IProject project, IProgressMonitor monitor) throws CoreException {
        String[] subFolderNames= folder.split("[\\" + File.separator + "\\/]");
        String subFolderStr= "";

        for(int i= 0; i < subFolderNames.length; i++) {
            String childPath= subFolderStr + "/" + subFolderNames[i];
            Path subFolderPath= new Path(childPath);
            IFolder subFolder= project.getFolder(subFolderPath);

            if (!subFolder.exists())
                subFolder.create(true, true, monitor);
            subFolderStr= childPath;
        }
    }
    
    
    protected static byte[] getTemplateFileContents(String fileName)
    {
    	try {
    	    Bundle bundle= Platform.getBundle(getTemplateBundleID());
    	    URL templateURL= Platform.find(bundle, new Path("/templates/" + fileName));
                if (templateURL == null) {
                    ErrorHandler.reportError("Unable to find template file: " + fileName, true);
                    return new byte[0];
                }
                URL url= Platform.asLocalURL(templateURL);
    	    String path= url.getPath();
    	    FileInputStream fis= new FileInputStream(path);
    	    DataInputStream is= new DataInputStream(fis);
    	    byte bytes[]= new byte[fis.available()];

    	    is.readFully(bytes);
    	    is.close();
    	    fis.close();
    	    return bytes;
    	} catch (Exception e) {
    	    e.printStackTrace();
    	    return ("// missing template file: " + fileName).getBytes();
    	}
    }

    
    /**
     * Gets the contents of a named template file from the "templates" folder
     * of a plugin with a given plugin id.  Created for use with the version of
     * createFileFromTemplate(..) that also takes a plugin id.
     * 
     * @param templateBundleId	The id of the plugin that contains the templates
     * 							folder in which the template is to be found
     * @param fileName			The name of the template file for which contents
     * 							are to be returned
     * @return					The contents of the named template file
     */
    public static byte[] getTemplateFileContents(String templateBundleId, String fileName) {
    	try {
    	    Bundle bundle= Platform.getBundle(templateBundleId);
    	    URL templateURL= Platform.find(bundle, new Path("/templates/" + fileName));
                if (templateURL == null) {
                    ErrorHandler.reportError("Unable to find template file: " + fileName, true);
                    return new byte[0];
                }
                URL url= Platform.asLocalURL(templateURL);
    	    String path= url.getPath();
    	    FileInputStream fis= new FileInputStream(path);
    	    DataInputStream is= new DataInputStream(fis);
    	    byte bytes[]= new byte[fis.available()];

    	    is.readFully(bytes);
    	    is.close();
    	    fis.close();
    	    return bytes;
    	} catch (Exception e) {
    	    e.printStackTrace();
    	    return ("// missing template file: " + fileName).getBytes();
    	}
    }
    
        
    public static void replace(StringBuffer sb, String target, String substitute) {
    	for(int index= sb.indexOf(target); index != -1; index= sb.indexOf(target))
    	    sb.replace(index, index + target.length(), substitute);
    }

    public static String performSubstitutions(String contents, Map replacements) {
		StringBuffer buffer= new StringBuffer(contents);
		
		for(Iterator iter= replacements.keySet().iterator(); iter.hasNext();) {
		    String key= (String) iter.next();
		    String value= (String) replacements.get(key);
		
		    if (value != null)
			replace(buffer, key, value);
		}
		return buffer.toString();
    }
	
    
    public static String formatJavaCode(String contents) {
    	CodeFormatter formatter= org.eclipse.jdt.core.ToolFactory.createCodeFormatter(JavaCore.getOptions());
    	TextEdit te= formatter.format(CodeFormatter.K_COMPILATION_UNIT, contents, 0, contents.length(), 0, "\n");

    	IDocument l_doc= new Document(contents);
    	try {
    	    te.apply(l_doc);
    	} catch (MalformedTreeException e) {
    	    e.printStackTrace();
    	} catch (BadLocationException e) {
    	    e.printStackTrace();
    	} catch (NullPointerException e) {
    		// Can happen that te is null
    		e.printStackTrace();
    	}
    	contents= l_doc.get();
    	return contents;
    }
	
}