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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.utils.ExtensionPointUtils;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundleModel;
import org.eclipse.pde.internal.core.plugin.PluginElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;

public abstract class IMPWizard extends Wizard {
    protected IMPWizardPage pages[];
    protected int currentPage;
	
    // Name of template for generating implementation skeleton;
    // most wizards of various types (extension point and other)
    // use exactly one.  If a wizard needs more than one, it
    // will have to define those itself.
    protected String fTemplateName;

	protected int NPAGES;

	protected IProject fProject;

	protected String fProjectName;

	protected String fLanguageName;

	protected String fPackageName;

	protected String fPackageFolder;

	protected String fParserPackage;

	protected String fClassNamePrefix;

	protected String fFullClassName;
	
    protected static final String START_HERE= "// START_HERE";


    public void init(IWorkbench workbench, IStructuredSelection selection) {}
	
	
	@Override
	public boolean performFinish() {
		return false;
	}

	
    public void setPage(int page) {
    	currentPage= page;
    }
    
    
    public int getPageCount() {
    	return NPAGES;
    }
    
    
    protected void addPages(IMPWizardPage[] pages) {
		this.pages= pages;
		NPAGES= pages.length;
		for(int n= 0; n < pages.length; n++) {
		    addPage(pages[n]);
		}
		List<String> extenRequires= getPluginDependencies();
		for(String requiredPlugin: extenRequires) {
		    for(int n= 0; n < pages.length; n++) {	
		        List<String> pageRequires= pages[n].getRequires();
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

    
    /**
     * @return the list of plugin ID's for the plugin dependencies for this language service.
     */
    protected abstract List<String> getPluginDependencies();
    
    
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
    public static String getPluginPackageName(IProject project, String defaultName)
    {
    	String result = defaultName;
    	if (result == null) {
    		result = discoverProjectLanguage(project);
    		// SMS 26 Nov 2007 re:  bug #296
    		if (result != null)
    			result = result.toLowerCase();
    	}
       	if (project != null) {
            String activator = null;
            IPluginModel pm = ExtensionEnabler.getPluginModelForProject(project);
            if (pm != null) {
            	WorkspaceBundleModel wbm = new WorkspaceBundleModel(project.getFile("META-INF/MANIFEST.MF")); //$NON-NLS-1$
            	activator = wbm.getBundle().getHeader("Bundle-Activator");
            }

            if (activator != null && !activator.equals("")) {
            	if (activator.lastIndexOf(".") >= 0)
            		result = activator.substring(0, activator.lastIndexOf("."));
            }
    	}
       	return result;
    }
    

    
    public static String getProjectSourceLocation(IProject project) {
		try {
			if (project == null)
				return null;
			JavaModel jm = JavaModelManager.getJavaModelManager().getJavaModel();
			IJavaProject jp = jm.getJavaProject(project);
			if (jp == null)
				return null;
			else {
				IPackageFragmentRoot[] roots = jp.getPackageFragmentRoots();
				for (int i = 0; i < roots.length; i++) {
					if (roots[i].getCorrespondingResource() instanceof IFolder) {
						IPath lcnPath = roots[i].getPath();
						lcnPath = lcnPath.removeFirstSegments(1);
						String lcn = lcnPath.toString();
						if (lcn.startsWith("/"))
							lcn = lcn.substring(1);
						if (!lcn.endsWith("/"))
							lcn = lcn + "/";
						return lcn;
					}
				}
			}
		} catch (JavaModelException e) {
			
		}
		return null;
    }
    
    
    
    
    public static String discoverParserPackage(IProject project) {
		if (project == null)
		    return null;
	
		IPluginModelBase pluginModel= getPluginModel(project.getName());
	
		if (pluginModel != null) {
	    	try {
	    		ExtensionEnabler.loadImpExtensionsModel((IPluginModel)pluginModel, project);
	    	} catch (CoreException e) {
	    		System.err.println("CodeServiceWizard.discoverProjectLanguage():  CoreExeption loading extensions model; may not succeed");
	    	} catch (ClassCastException e) {
	    		System.err.println("CodeServiceWizard.discoverProjectLanguage():  ClassCastExeption loading extensions model; may not succeed");
	    	}
	    	
		    IPluginExtension[] extensions= pluginModel.getExtensions().getExtensions();
	
		    for(int i= 0; i < extensions.length; i++) {
				if (extensions[i].getPoint().endsWith(".parser")) {
				    IPluginObject[] children= extensions[i].getChildren();
		
				    for(int j= 0; j < children.length; j++) {
						if (children[j].getName().indexOf("parser") > -1) {
						    //return (((IPluginElement) children[j]).getAttribute("language").getValue());
							IPluginAttribute attr = ((IPluginElement) children[j]).getAttribute("class");
							if (attr == null)
								return null;
							String parserClassName = attr.getValue();
						    if (parserClassName == null)
						    	return null;
						    if (parserClassName.length() == 0)
						    	return null;
						    if (parserClassName.lastIndexOf('.') < 0)
						    	return "";
						    return parserClassName.substring(0,parserClassName.lastIndexOf('.'));
						}
				    }
				}
		    }
		}
		return null;
    }
    
    
    
    public static IPluginModelBase getPluginModel(String projectName) {
        try {
        	if (projectName == null)
        		return null;
            PluginModelManager pmm= PDECore.getDefault().getModelManager();
            // SMS 28 Apr 2008
            IPluginModelBase[] plugins= pmm.getWorkspaceModels();	//getAllPlugins();

            for(int n= 0; n < plugins.length; n++) {
                IPluginModelBase plugin= plugins[n];
                IResource resource= plugin.getUnderlyingResource();
                if (resource != null && projectName.equals(resource.getProject().getName())) {
                    return plugin;
                }
            }
        } catch (Exception e) {
            ErrorHandler.reportError("Could not enable extension point for " + projectName, e);
        }
        return null;
    }


    public static String discoverProjectLanguage(IProject project) {
		if (project == null)
		    return null;
	
		IPluginModelBase pluginModel= getPluginModel(project.getName());
	
		if (pluginModel != null) {
	    	try {
	    		ExtensionEnabler.loadImpExtensionsModel((IPluginModel)pluginModel, project);
	    	} catch (CoreException e) {
	    		System.err.println("CodeServiceWizard.discoverProjectLanguage():  CoreExeption loading extensions model; may not succeed");
	    	} catch (ClassCastException e) {
	    		System.err.println("CodeServiceWizard.discoverProjectLanguage():  ClassCastExeption loading extensions model; may not succeed");
	    	}
	    	
		    IPluginExtension[] extensions= pluginModel.getExtensions().getExtensions();
	
		    for(int i= 0; i < extensions.length; i++) {
				if (extensions[i].getPoint().endsWith(".languageDescription")) {
				    IPluginObject[] children= extensions[i].getChildren();
		
				    for(int j= 0; j < children.length; j++) {
						if (children[j].getName().equals("language")) {
						    //return (((IPluginElement) children[j]).getAttribute("language").getValue());
							IPluginAttribute attr = ((IPluginElement) children[j]).getAttribute("language");
							if (attr == null)
								return null;
						    return (((IPluginElement) children[j]).getAttribute("language").getValue());
						}
				    }
				}
		    }
		}
		return null;
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
            IPluginModel pm = ExtensionEnabler.getPluginModelForProject(project);
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
            result = ExtensionEnabler.getPluginIDForProject(project);
    	}
       	return result;
    }
 
    
    
    public Map<String,String> getStandardSubstitutions(IProject project) {
    	Map<String, String> result = getStandardSubstitutions();
        result.put("$PLUGIN_PACKAGE$", getPluginPackageName(project, null));
        result.put("$PLUGIN_CLASS$", getPluginClassName(project, null));
        result.put("$PLUGIN_ID$", getPluginID(project, null));
        return result;
    }
    
    
    
    public Map<String, String> getStandardSubstitutions() {
        Map<String,String> result = new HashMap<String,String>();
        
        result = ExtensionPointUtils.getASTInformation((IPluginModel)pages[0].getPluginModel(), fProject);

        result.put("$LANG_NAME$", fLanguageName);
        result.put("$CLASS_NAME_PREFIX$", fClassNamePrefix);
        result.put("$PACKAGE_NAME$", fPackageName);
        result.put("$PROJECT_NAME$", fProjectName);

        // NOTE:  These are default values for plug-in substitutions;
        // they should typically be overridden by real values obtained
        // from getStandardSubstitutions(IProject).  That means that
        // that method should be called after this one (or that one
        // should call this one before setting those values).
        result.put("$PLUGIN_PACKAGE$", getPluginPackageName(null, null));
        result.put("$PLUGIN_CLASS$", getPluginClassName(null, null));
        result.put("$PLUGIN_ID$", getPluginID(null, null));

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
     * of the AST package and class in such a way that they can become part
     * of the "standard substitutions."  (ALTERNATIVELY:  the class could just
     * be obtained in wizards where needed, in which case it need not be part
     * of the standard substitutions.)
     * 
     * @return	A Map that contains two values:  the name of the package that
     * 			contains the AST class, and the name of the AST class.
     * 			These values keyed, respectively, by the symbols "$AST_PACKAGE$"
     * 			and "$AST_NODE$".
     * 
     * Updates: Stan Sutton, 9 Aug 2006
     * 			Changed return from $AST_CLASS$ to $AST_NODE$ since the latter
     * 			is the symbol more commonly used (and the one on which I will
     * 			try to standardize)
     * 
     * @author	Stan Sutton
     * @since	17 May 2006
     */
    public static Map<String,String> getASTInformation(IPluginModel pluginModel, IProject project)
    {
    	Map<String,String> result = new HashMap<String,String>();
    	
        // Get the extension that represents the parser
        
	   	// SMS 26 Jul 2007
        // Load the extensions model in detail, using the adapted IMP representation,
        // to assure that the children of model elements are represented
    	try {
    		ExtensionEnabler.loadImpExtensionsModel((IPluginModel)pluginModel, project);
    	} catch (CoreException e) {
    		System.err.println("GeneratedComponentWizardPage.discoverProjectLanguage():  CoreExeption loading extensions model; may not succeed");
    	} catch (ClassCastException e) {
    		System.err.println("GeneratedComponentWizardPage.discoverProjectLanguage():  ClassCastExeption loading extensions model; may not succeed");
    	}
    	
        
        IExtensions extensionsThing = pluginModel.getExtensions();
        IPluginExtension[] extensions = extensionsThing.getExtensions();
        IPluginExtension parserExtension = null;
        for (int i = 0; i < extensions.length; i++) {
        	if(extensions[i].getPoint().equals("org.eclipse.imp.runtime.parser")) {
        		parserExtension = extensions[i];
        		break;
        	}
        }

        // Get the plugin element that represents the class of the parser
        PluginElement parserPluginElement = null;
        if (parserExtension != null) {
        	IPluginObject[] children = parserExtension.getChildren();
        	for (int i = 0; i < children.length; i++) {
        		String name = children[i].getName();
        		if(name.equals("parser") || name.equals("parserWrapper")) {
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
        result.put("$PROJ_NAME$", project.getName());
        result.put("$PARSER_PACKAGE$", parserPackageName);
        result.put("$AST_PACKAGE$", astPackageName);
        result.put("$AST_NODE$", astClassName);
        
        return result;
    }
    
    
    
    /**
     * Collects basic information from wizard-page fields and computes
     * additional common values for use by wizards in generating code.
     * 	
     * Can be extended by subclasses for specific wizards in order to
     * gather wizard-specific values.
     */
    protected void collectCodeParms() {
    	fProject= pages[0].getProjectOfRecord();
    	fProjectName= pages[0].fProjectText.getText();
        fLanguageName= pages[0].fLanguageText.getText();

        if (pages[0].fTemplateText != null)
        	fTemplateName= pages[0].fTemplateText.getText();

        fClassNamePrefix= Character.toUpperCase(fLanguageName.charAt(0)) + fLanguageName.substring(1);

		WizardPageField classField= pages[0].getField("class");

		if (classField != null) {
    		String qualifiedClassName= classField.fValue;
    		fFullClassName= qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.') + 1);
    		fPackageName= qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
		} else {
	        WizardPageField pkgField= pages[0].getField("package");
	        if (pkgField != null) {
	            fPackageName= pkgField.getText();
	        } else {
	            fPackageName= "";
	        }
		}
        fPackageFolder= fPackageName.replace('.', File.separatorChar);
		fParserPackage= discoverParserPackage(fProject);
    }
    
    
    
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
    	prefix = prefix + prefixTail;
    	
    	return new String[] {prefix + fFullClassName + ".java" };
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
     * Opens the given file in the appropriate editor for editing.<br>
     * If the file contains a comment "// START_HERE", the cursor will
     * be positioned just after that.
     * @param monitor
     * @param file
     */
    protected void editFile(IProgressMonitor monitor, final IFile file)
    {
    	WizardUtilities.editFile(monitor, file, getShell());
    }
    
    
    
    public boolean canFinish() {
    	return super.canFinish();// pages[currentPage].isPageComplete() && (currentPage >= pages.length - 1);
    }
    
    
    public IFile createFileFromTemplate(
        	String fileName, String templateName, String folder, Map replacements,
    	    IProject project, IProgressMonitor monitor)
        throws CoreException
    	{
        	return createFileFromTemplate(fileName, "org.eclipse.imp.metatooling", templateName, folder, replacements, project, monitor);
        }
        
        
    protected IFile createFileFromTemplate(
    		String fileName, String templateBundleID, String templateName, String folder,
    		Map replacements, IProject project, IProgressMonitor monitor) throws CoreException
    {
    	return WizardUtilities.createFileFromTemplate(
    			fileName, templateBundleID, templateName, folder, getProjectSourceLocation(project),
    			replacements, project, monitor);
    }
    

    
    protected static IFile createFile(String fileName, String folder, IProject project, IProgressMonitor monitor) throws CoreException {
		monitor.setTaskName("Creating " + fileName);
	
		final IFile file= project.getFile(new Path(getProjectSourceLocation(project) + folder + "/" + fileName));
	
		if (!file.exists()) {
	            WizardUtilities.createSubFolders(getProjectSourceLocation(project) + folder, project, monitor);
		    file.create(new ByteArrayInputStream("".getBytes()), true, monitor);
		}
	//	monitor.worked(1);
		return file;
    }
    
    
    protected IFile getFile(String fileName, String folder, IProject project) throws CoreException {
    	IFile file= project.getFile(new Path(getProjectSourceLocation(fProject) + folder + "/" + fileName));

    	return file;
    }
}
