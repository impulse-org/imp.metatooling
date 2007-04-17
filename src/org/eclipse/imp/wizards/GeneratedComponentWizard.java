package org.eclipse.uide.wizards;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundleModel;
import org.eclipse.pde.internal.core.plugin.PluginElement;
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
import org.eclipse.uide.WizardPlugin;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.utils.StreamUtils;
import org.osgi.framework.Bundle;

/**
 * This wizard supports the generation of one or more implementation
 * classes for language or IDE services that are not extensions
 */
public abstract class GeneratedComponentWizard extends Wizard implements INewWizard {
    private static final String START_HERE= "// START_HERE";

    protected int currentPage;

    protected GeneratedComponentWizardPage pages[];

    protected int NPAGES;

    // SMS 17 Apr 2007
    protected IProject fProject;
	protected String fProjectName;
    protected String fLanguageName;
    protected String fPackageName;
    protected String fPackageFolder;
    protected String fParserPackage;
    protected String fClassNamePrefix;
    protected String fFullClassName;	
    
    
    public GeneratedComponentWizard() {
	super();
	setNeedsProgressMonitor(true);
    }

    
    
    /* *********************************************************
     * The following several methods address the handling
     * of pages for the wizard
     */
    

    public void init(IWorkbench workbench, IStructuredSelection selection) {}

    
    public void setPage(int page) {
    	currentPage= page;
        }

    
    public int getPageCount() {
	return NPAGES;
    }

    protected void addPages(GeneratedComponentWizardPage[] pages) {
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


    
    /* *********************************************************
     * The following several methods address the "Finish" of the wizard
     * and the generation of code stubs for a service implementation
     */

    
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
     * Collects basic information from wizard-page fields and computes
     * additional common values for use by wizards in generating code.
     * 
     * Can be extended by subclasses for specific wizards in order to
     * gather wizard-specific values.
     */
    protected void collectCodeParms() {
    	fProject = pages[0].getProject();
    	fProjectName = pages[0].fProjectText.getText();
        fLanguageName= pages[0].fLanguageText.getText();
        
        fClassNamePrefix= Character.toUpperCase(fLanguageName.charAt(0)) + fLanguageName.substring(1);
        
		String qualifiedClassName= pages[0].getField("class").fValue;
		fFullClassName = qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.') + 1);
		fPackageName= qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
		fPackageFolder= fPackageName.replace('.', File.separatorChar);
		
        String[] subPkgs= fPackageName.split("\\.");
        StringBuffer buff= new StringBuffer();
        for(int i= 0; i < subPkgs.length-1; i++) {
            if (i > 0) buff.append('.');
            buff.append(subPkgs[i]);
        }
        buff.append(".parser");
        fParserPackage= buff.toString();
    }
    
    
    // SMS 17 Apr 2007	
    // A step toward relaxing assumptions about the location
    // of source files within the project
    public String getProjectSourceLocation() {
    		return "src/";
    }
    
    
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
 
    
    /**
     * Opens the given file in the appropriate editor for editing.<br>
     * If the file contains a comment "// START_HERE", the cursor will
     * be positioned just after that.
     * @param monitor
     * @param file
     */
    protected void editFile(IProgressMonitor monitor, final IFile file) {
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

    protected IFile createFileFromTemplate(String fileName, String templateName, String folder, Map replacements,
	    IProject project, IProgressMonitor monitor) throws CoreException {
	monitor.setTaskName("Creating " + fileName);

	final IFile file= project.getFile(new Path(getProjectSourceLocation() + folder + "/" + fileName));
	String templateContents= new String(getTemplateFile(templateName));
	String contents= performSubstitutions(templateContents, replacements);

	if (file.exists()) {
	    file.setContents(new ByteArrayInputStream(contents.getBytes()), true, true, monitor);
	} else {
            createSubFolders(getProjectSourceLocation() + folder, project, monitor);
	    file.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
	}
//	monitor.worked(1);
	return file;
    }
    
    protected IFile createFile(String fileName, String folder, IProject project, IProgressMonitor monitor) throws CoreException {
	monitor.setTaskName("Creating " + fileName);

	final IFile file= project.getFile(new Path(getProjectSourceLocation() + folder + "/" + fileName));

	if (!file.exists()) {
            createSubFolders(getProjectSourceLocation() + folder, project, monitor);
	    file.create(new ByteArrayInputStream("".getBytes()), true, monitor);
	}
//	monitor.worked(1);
	return file;
    }

    protected IFile getFile(String fileName, String folder, IProject project) throws CoreException {
	IFile file= project.getFile(new Path(getProjectSourceLocation() + folder + "/" + fileName));

	return file;
    }
    
    
    protected void createSubFolders(String folder, IProject project, IProgressMonitor monitor) throws CoreException {
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

    public static void replace(StringBuffer sb, String target, String substitute) {
	for(int index= sb.indexOf(target); index != -1; index= sb.indexOf(target))
	    sb.replace(index, index + target.length(), substitute);
    }

    protected String performSubstitutions(String contents, Map replacements) {
	StringBuffer buffer= new StringBuffer(contents);

	for(Iterator iter= replacements.keySet().iterator(); iter.hasNext();) {
	    String key= (String) iter.next();
	    String value= (String) replacements.get(key);

	    if (value != null)
		replace(buffer, key, value);
	}
	return buffer.toString();
    }

    protected String getTemplateBundleID() {
	return WizardPlugin.kPluginID;
    }

    protected byte[] getTemplateFile(String fileName) {
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
    
    
    
    /* *********************************************************
     * The following several methods are used to compute values
     * for substitution parameters in implementation templates
     */
    
    
    // SMS 23 Mar 2007
    // This version takes an IProject and provides mappings
    // related to the project's plugin aspect
    protected Map<String,String> getStandardSubstitutions(IProject project) {
    	Map<String, String> result = getStandardSubstitutions();
    	result.remove("$PLUGIN_PACKAGE$");
        result.put("$PLUGIN_PACKAGE$", getPluginPackageName(project, null));
        // SMS 27 Mar 2007
    	result.remove("$PLUGIN_CLASS$");
        result.put("$PLUGIN_CLASS$", getPluginClassName(project, null));
        result.remove("$PLUGIN_ID$");
        result.put("$PLUGIN_ID$", getPluginID(project, null));
        return result;
    }
    
    
    
    protected Map<String, String> getStandardSubstitutions() {
        Map<String,String> result = new HashMap();
        
        // SMS 17 May 2006
        // Need to get a name for the AST package and AST node type for use in
        // the NewFoldingUpdater wizard
        // Note:  The method used assumes that these are the default values
        // (if that assumption is wrong, then the generated folding service won't
        // compile, but if we don't provide any values then it won't compile in
        // any case--specifically because substitutions for these parameters will
        // not have been made)
        result = getASTInformation();
        
        // continuing with original:
        result.put("$LANG_NAME$", fLanguageName);
        result.put("$CLASS_NAME_PREFIX$", fClassNamePrefix);
        result.put("$PACKAGE_NAME$", fPackageName);
        // SMS 22 Mar 2007
        result.put("$PROJECT_NAME$", fProjectName);
        // SMS 23 Mar 2007
        // Not the greatest solution, but if we don't have the
        // project then we may as well assume that $PLUGIN_PACKAGE$
        // has a default value
        result.put("$PLUGIN_PACKAGE$", getPluginPackageName(null, null));
        // SMS 27 Mar 2007:  ditto
        result.put("$PLUGIN_CLASS$", getPluginClassName(null, null));
        result.put("$PLUGIN_ID$", getPluginID(null, null));

        return result;
    }
  
    
    

    /**
     * Get the name of the package in which a plugin class is defined
     * for this project, or a default value if there is no such package
     * or if the project is null.  If no default name is provided, then
     * the name of the language is used for a default.
     * 
     * The intention here is to return a the name of the plugin package,
     * if the package exists, or a name that could be used as the name
     * of the plugin package, if the package does not exist.  So this
     * method should not return null and should not be used as a test
     * of whether a given project contains a plugin package or class.
     * 
     * 
     * 
     * SMS 23 Mar 2007
     * 
     * @param project		The project for which the plugin package name is sought;
     * 						may be null
     * @param defaultName	A name to return if the given package lacks a plugin class;
     * 						may be null
     * @return				The name of the package that contains the project's plugin
     * 						class, if there is one, or a name that could be used for the
     * 						plugin package, if there is none.
     */
    public String getPluginPackageName(IProject project, String defaultName)
    {
    	String result = defaultName;
    	if (result == null)
    		result = fLanguageName;
       	if (project != null) {
            String activator = null;
            IPluginModel pm = ExtensionPointEnabler.getPluginModelForProject(project);
            if (pm != null) {
            	WorkspaceBundleModel wbm = new WorkspaceBundleModel(project.getFile("META-INF/MANIFEST.MF")); //$NON-NLS-1$
            	activator = wbm.getBundle().getHeader("Bundle-Activator");
            }

            if (activator != null) {
            	result = activator.substring(0, activator.lastIndexOf("."));
            }
    	}
       	return result;
    }
    
    /**
     * Get the name of the plugin class for this project, or a default
     * name if there is no plugin class or if the given project is null.
     * If no default name is provided, then a name based on the name of
     * the language is used for a default.
     * 
     * The intention here is to return a the name of the plugin class,
     * if it exists, or a name that could be used as the name of the
     * plugin class, if it does not exist.  So this method should not
     * return null and should not be used as a test of whether a given
     * project contains a plugin class.
     * 
     * SMS 27 Mar 2007
     * 
     * @param project		The project for which the plugin class name is sought;
     * 						may be null
     * @param defaultName	A name to return if the given package lacks a plugin class;
     * 						may be null
     * @return				The name of the project's plugin class, if there is one,
     * 						or a name that could be used for the plugin class, if there
     * 						is none.
     */
    public String getPluginClassName(IProject project, String defaultName)
    {
    	String result = defaultName;
    	if (result == null)
    		result = fClassNamePrefix + "Plugin";
       	if (project != null) {
            String activator = null;
            IPluginModel pm = ExtensionPointEnabler.getPluginModelForProject(project);
            if (pm != null) {
            	WorkspaceBundleModel wbm = new WorkspaceBundleModel(project.getFile("META-INF/MANIFEST.MF")); //$NON-NLS-1$
            	activator = wbm.getBundle().getHeader("Bundle-Activator");
            }

            if (activator != null) {
            	result = activator.substring(activator.lastIndexOf(".")+1);
            }
    	}
       	return result;
    }
    
    /**
     * Get the plugin id defined for this project, or a default value if
     * there is no plugin id or if the given project is null.   If no default
     * id is provided, then an id based on the name of the project is used
     * for a default.
     * 
     * The intention here is to return a plugin id, if it exists, or a
     * value that could be used as the id of the plugin, if it does not
     * exist.  So this method should not return null and should not be
     * used as a test of whether a given project has a plugin id.
     * 
     * SMS 27 Mar 2007
     * 
     * @param project		The project for which the plugin id name is sought;
     * 						may be null
     * @param defaultID		A value to return if the given package lacks a plugin id;
     * 						may be null
     * @return				The plugin id of the project, if there is one, or a value
     * 						that could be used as the plugin id, if there is none.
     */
    public String getPluginID(IProject project, String defaultID)
    {
    	String result = defaultID;
    	if (result == null)
    		getPluginPackageName(project, null);
       	if (project != null) {
            result = ExtensionPointEnabler.getPluginIDForProject(project);
    	}
       	return result;
    }
 
    
    /**
     * Return a Map containing the the names of the AST package and class
     * bound to "well-known" symbols, "$AST_PACKAGE$" and "$AST_CLASS$", 
     * respectively.
     * 
     * WARNING:  The names returned are currently the DEFAULT names (which
     * should be the most commonly occurring but which may not be appropriate
     * in general).
     * 
     * The actual values for the AST package and class are generated in the
     * NewParser wizard but are not (yet) stored anywhere for reference by
     * other wizards.  There is at least one other wizard, the NewFoldingUpdater
     * wizard, which does need these names to complete a template.  In order
     * to make available some reasonable values for these names, this method
     * recomputes the names using the same assumptions as are used for the
     * default case in the NewParser wizard.
     * 
     * TODO:  Provide a means for (more) persistently maintaining the names
     * of hte AST package and class in such a way that they can become part
     * of the "standard substitutions."  (ALTERNATIVELY:  the class could just
     * be obtained in wizards where needed, in which case it need not be part
     * of the standard substitutions.)
     * 
     * @return	A Map that contains two valuse:  the name of the package that
     * 			contains the AST class, and the name of the AST class.
     * 			These values keyed, respectively, by the symbols "$AST_PACKAGE$"
     * 			and "$AST_NODE$".
     * 
     * Updates:  Stan Sutton, 9 Aug 2006
     * 			Changed return from $AST_CLASS$ to $AST_NODE$ since the latter
     * 			is the symbol more commonly used (and the one on which I will
     * 			try to standardize)
     * 
     * @author	Stan Sutton
     * @since	17 May 2006
     */
    protected Map<String,String> getASTInformation()
    {
    	Map<String,String> result = new HashMap();
    	
        // Get the extension that represents the parser
        IPluginModelBase pluginModel = pages[0].getPluginModel();
        IExtensions extensionsThing = pluginModel.getExtensions();
        IPluginExtension[] extensions = extensionsThing.getExtensions();
        IPluginExtension parserExtension = null;
        for (int i = 0; i < extensions.length; i++) {
        	if(extensions[i].getPoint().equals("org.eclipse.uide.runtime.parser")) {
        		parserExtension = extensions[i];
        		break;
        	}
        }

        // Get the plugin element that represents the class of the parser
        PluginElement parserPluginElement = null;
        if (parserExtension != null) {
        	IPluginObject[] children = parserExtension.getChildren();
        	for (int i = 0; i < children.length; i++) {
        		if(children[i].getName().equals("parser")) {
        			parserPluginElement = (PluginElement) children[i];
        				break;
        		}
        	}
        }
        if (parserPluginElement == null) return result;

        
        // Get the names of the parser package, AST package, and AST (node) class name
        
        IPluginAttribute parserClassAttribute = parserPluginElement.getAttribute("class");
        String parserPackageName = parserClassAttribute.getValue();
        parserPackageName = parserPackageName.substring(0, parserPackageName.lastIndexOf('.'));
        // ASSUME that the AST package name is the parser package name extended 
        // with ".Ast" (this is the default when auto-generated)
        String astPackageName = parserPackageName + ".Ast";
        // Just assume this is true
        // TBD:  check whether this exists (or put the info somewhere from
        // where it can be retrieved here)
        String astClassName = "ASTNode";
        
        // Save these values in the substitutions map
        result.put("$PROJ_NAME$", fProjectName);
        result.put("$PARSER_PACKAGE$", parserPackageName);
        result.put("$AST_PACKAGE$", astPackageName);
        result.put("$AST_NODE$", astClassName);
        
        return result;
    }
 
    
    /* *********************************************************
     * The following two methods are used to check whether it's ok
     * for files that are about to be generated to clobber files
     * that already exist. 
     */
    
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
