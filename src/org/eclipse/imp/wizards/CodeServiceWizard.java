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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.utils.ExtensionPointUtils;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundleModel;
	
/**
 * An ExtensionPointWizard that also generates source code from one or more template files.
 * @author rfuhrer@watson.ibm.com
 */
public abstract class CodeServiceWizard extends ExtensionPointWizard {

	
    /**
     * Collects basic information from wizard-page fields and computes
     * additional common values for use by wizards in generating code.
     * 	
     * Can be extended by subclasses for specific wizards in order to
     * gather wizard-specific values.
     */
    protected void collectCodeParms() {
    	fProject = pages[0].getProjectOfRecord();
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
            IPluginModel pm = ExtensionPointEnabler.getPluginModelForProject(project);
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
    
    
    
    public static String discoverProjectLanguage(IProject project) {
		if (project == null)
		    return null;
	
		IPluginModelBase pluginModel= getPluginModel(project.getName());
	
		if (pluginModel != null) {
	    	try {
	    		ExtensionPointEnabler.loadImpExtensionsModel((IPluginModel)pluginModel, project);
	    	} catch (CoreException e) {
	    		System.err.println("GeneratedComponentWizardPage.discoverProjectLanguage():  CoreExeption loading extensions model; may not succeed");
	    	} catch (ClassCastException e) {
	    		System.err.println("GeneratedComponentWizardPage.discoverProjectLanguage():  ClassCastExeption loading extensions model; may not succeed");
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
    

    public static IPluginModelBase getPluginModel(String projectName) {
        try {
        	if (projectName == null)
        		return null;
            PluginModelManager pmm= PDECore.getDefault().getModelManager();
            IPluginModelBase[] plugins= pmm.getAllPlugins();

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
    
    
    // SMS 23 Mar 2007
    // This version takes an IProject and provides mappings
    // related to the project's plugin aspect
    public Map<String,String> getStandardSubstitutions(IProject project) {
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
    
    
    
    public Map<String, String> getStandardSubstitutions() {
        Map<String,String> result = new HashMap();
        
        // SMS 17 May 2006
        // Need to get a name for the AST package and AST node type for use in
        // the NewFoldingUpdater wizard
        // Note:  The method used assumes that these are the default values
        // (if that assumption is wrong, then the generated folding service won't
        // compile, but if we don't provide any values then it won't compile in
        // any case--specifically because substitutions for these parameters will
        // not have been made)
        result = ExtensionPointUtils.getASTInformation((IPluginModel)pages[0].getPluginModel(), fProject);
        
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
    
}
