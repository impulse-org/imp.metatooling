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

package org.eclipse.imp.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.WizardPlugin;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.language.ServiceFactory;
import org.eclipse.imp.wizards.ExtensionEnabler;
import org.eclipse.imp.wizards.IMPWizardPage;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.plugin.PluginElement;

public class ExtensionPointUtils {
    private ExtensionPointUtils() { }

    /**
     * Return an extension with a given name in a given plug-in model.
     * 
     * Limitation:  Returns only one extension, even if there may be more than
     * one, so this method is most suitable for finding extensions that are
     * expected to be unique in their plug-in.  As a practical matter, this
     * method returns the first extension found, if any, that matches the
     * given name.
     * 
     * @param extName		The name of the extension sought; actually,
     * 						an extension point id
     * @param pluginModel	The model of the plug-in in which the extension is sought
     * @return				An extension from the given plug-in model that extends
     * 						the extension point identified by the given name
     */
    public static IPluginExtension findExtensionByName(String extName, IPluginModelBase pluginModel) {
        IPluginExtension[] extensions = pluginModel.getExtensions().getExtensions();
        IPluginExtension extension = null;
    
        for (int i = 0; i < extensions.length; i++) {
        	if(extensions[i].getPoint().equals(extName)) {
        		extension = extensions[i];
        		break;
        	}
        }
        return extension;
    }

    /**
     * Return an array of extensions with a given name in a given plug-in model.
     * 
     * @param extName		The name of the extensions sought; actually,
     * 						an extension point id
     * @param pluginModel	The model of the plug-in in which the extensions are sought
     * @return				An array of extensions from the given plug-in model that
     * 						extend the extension point identified by the given name
     */
    public static IPluginExtension[] findExtensionsByName(String extName, IPluginModelBase pluginModel) {
        IPluginExtension[] extensions = pluginModel.getExtensions().getExtensions();
        List extensionsList = new ArrayList();

        for (int i = 0; i < extensions.length; i++) {
        	if(extensions[i].getPoint().equals(extName)) {
        		extensionsList.add(extensions[i]);
        	}
        }
        
        IPluginExtension[] namedExtensions = new IPluginExtension[extensionsList.size()];
        for (int i = 0; i < namedExtensions.length; i++)
        	namedExtensions[i] = (IPluginExtension) extensionsList.get(i);
        return namedExtensions;
    }

    public static IPackageFragment findPackageByName(IProject project, String parserPackageName) {
        IWorkspace workspace = project.getWorkspace();
        IJavaModel javaModel = JavaCore.create(workspace.getRoot());
        IJavaProject javaProject = javaModel.getJavaProject(project.getName());
        try {
        	IPackageFragment[] packageFragments = javaProject.getPackageFragments();
        	for (int i = 0; i < packageFragments.length; i++) {
        		if (packageFragments[i].getElementName().equals(parserPackageName)) {
        			return packageFragments[i];
        		}
        	}
        } catch (JavaModelException e) {
            WizardPlugin.getInstance().logException("ExtensionPointUtils.findPackageByName(): JavaModelException getting parser package", e);
        }
        return null;
    }

    // SMS 26 Jul 2007
    // Adapted to take plugin model base and project parameters so as to be able to call
    // ExtensionPointEnabler.loadImpExtensionsModel so as to assure that the extensions
    // model is loaded in detail, i.e., including children
    public static PluginElement findElementByName(IPluginModelBase pluginModelBase, IProject project, String name, IPluginExtension parserExtension) {
    	try {
    		ExtensionEnabler.loadImpExtensionsModel((IPluginModel) pluginModelBase, project);
    	} catch (CoreException e) {
    	    WizardPlugin.getInstance().logException("ExtensionPointUtils.findElementByName(): Exception loading extensions model", e);
    		return null;
    	} catch (ClassCastException e) {
    	    WizardPlugin.getInstance().logException("ExtensionPointUtils.findElementByName(): exception loading extensions model", e);
    		return null;
    	}
		IPluginObject[] children = parserExtension.getChildren();
        for (int i = 0; i < children.length; i++) {
            if(children[i].getName().equals(name)) {
            	return (PluginElement) children[i];
            }
        }
        return null;
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
     * Updates:  Stan Sutton, 9 Aug 2006
     * 			Changed return from $AST_CLASS$ to $AST_NODE$ since the latter
     * 			is the symbol more commonly used (and the one on which I will
     * 			try to standardize)
     * 
     * @author	Stan Sutton
     * @since	17 May 2006
     */
    public static Map<String,String> getASTInformation(IPluginModel pluginModel, IProject project) {
    	Map<String,String> result = new HashMap<String, String>();

        // TBD: check whether this exists (or put the info somewhere where we can find it here)
        String astClassName = "ASTNode";

        result.put("$PROJ_NAME$", project.getName());
        result.put("$AST_NODE$", astClassName);

    	if (pluginModel == null) {
    		return result;
    	}

        // Get the extension that represents the parser

    	// SMS 26 Jul 2007
        // Load the extensions model in detail, using the adapted IMP representation,
        // to assure that the children of model elements are represented
    	try {
    		ExtensionEnabler.loadImpExtensionsModel((IPluginModel)pluginModel, project);
    	} catch (CoreException e) {
    	    WizardPlugin.getInstance().logException("ExtensionPointUtils.getASTInformation(): exception loading extensions model; may not succeed", e);
    	} catch (ClassCastException e) {
    	    WizardPlugin.getInstance().logException("ExtensionPointUtils.getASTInformation(): exception loading extensions model; may not succeed", e);
    	}

        // RMF 10/14/2008 - If this isn't a plugin project, return now rather than causing an NPE
        if (pluginModel == null) return result;

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

        result.put("$PARSER_PACKAGE$", parserPackageName);
        result.put("$AST_PACKAGE$", astPackageName);

        return result;
    }

    public static IProject findProjectForLanguage(String langName) {
        IProject[] allProjects= ResourcesPlugin.getWorkspace().getRoot().getProjects();
        String lowerLangName= langName.toLowerCase();
    
        for(IProject proj: allProjects) {
            String projLangName= ExtensionPointUtils.discoverLanguageForProject(proj);
            if (projLangName != null && projLangName.toLowerCase().equals(lowerLangName)) {
                return proj;
            }
        }
        return null;
    }

    public static IProject findProjectForLanguageExtension(String langName, String extensionPointID) {
        IProject[] allProjects= ResourcesPlugin.getWorkspace().getRoot().getProjects();
        String lowerLangName= langName.toLowerCase();
    
        for(IProject proj: allProjects) {
            if (ExtensionPointUtils.projectHasLanguageExtension(proj, lowerLangName, extensionPointID)) {
                return proj;
            }
        }
        return null;
    }

    public static String getLanguageID(IPluginExtension extension) {
        IPluginObject[] children= extension.getChildren();
    
        for(int j= 0; j < children.length; j++) {
            IPluginElement childElt= (IPluginElement) children[j];
            IPluginAttribute langAttr= childElt.getAttribute("language");
            if (langAttr != null) { 
                try {
                    return langAttr.getValue();
                } catch (Exception e) {
                    ErrorHandler.reportError("Exception getting language attribute value; returning", e);
                }
                return null;
            }
        }
        return null;
    }

    public static String getLanguageIDFromDescriptor(IPluginExtension extension) {
        IPluginObject[] children= extension.getChildren();
    
        for(int j= 0; j < children.length; j++) {
            if (children[j].getName().equals("language")) {
                try {
                    return ((IPluginElement) children[j]).getAttribute("language").getValue();
                } catch (Exception e) {
                    ErrorHandler.reportError("Exception getting language attribute value; returning", e);
                }
                return null;
            }
        }
        return null;
    }

    public static String discoverLanguageForProject(IProject project) {
        IPluginModelBase pluginModel= IMPWizardPage.getPluginModel(project.getName());
    
        if (pluginModel != null) {
            // SMS 26 Jul 2007
            // Load the extensions model in detail, using the adapted IMP representation,
            // to assure that the children of model elements are represented
            try {
                ExtensionEnabler.loadImpExtensionsModel((IPluginModel) pluginModel, project);
            } catch (CoreException e) {
                // WizardPlugin.getInstance().logException("WizardUtilities.discoverLanguageForProject(): exception loading extensions model", e);
            } catch (ClassCastException e) {
                WizardPlugin.getInstance().logException("exception while loading extensions model", e);
            }
    
            IPluginExtension[] extensions= pluginModel.getExtensions().getExtensions();
    
            // Prefer the language descriptor, if it exists
            for(int i= 0; i < extensions.length; i++) {
                IPluginExtension extension= extensions[i];
                if (extension.getPoint().equals(ServiceFactory.LANGUAGE_DESCRIPTION_QUALIFIED_POINT_ID)) {
                    String langID= getLanguageIDFromDescriptor(extension);
                    if (langID != null) {
                        return langID;
                    }
                }
            }
            // No language descriptor; take the language ID from any of the IMP extensions
            for(int i= 0; i < extensions.length; i++) {
                IPluginExtension extension= extensions[i];
                String pointID= extension.getPoint();
                if (ServiceFactory.ALL_SERVICES.contains(pointID)) {
                    String langID= getLanguageID(extension);
                    if (langID != null) {
                        return langID;
                    }
                }
            }
        }
        return null;
    }

    public static boolean projectHasLanguageExtension(IProject project, String langName, String extensionPointID) {
        IPluginModelBase pluginModel= IMPWizardPage.getPluginModel(project.getName()); // TODO getPluginModel() probably belongs on ExtensionPointUtils
    
        if (pluginModel != null) {
            try {
                ExtensionEnabler.loadImpExtensionsModel((IPluginModel) pluginModel, project);
            } catch (CoreException e) {
                // WizardPlugin.getInstance().logException("WizardUtilities.discoverLanguageForProject(): exception loading extensions model", e);
            } catch (ClassCastException e) {
                WizardPlugin.getInstance().logException("exception while loading extensions model", e);
            }
    
            IPluginExtension[] extensions= pluginModel.getExtensions().getExtensions();
    
            for(int i= 0; i < extensions.length; i++) {
                IPluginExtension extension= extensions[i];
                String pointID= extension.getPoint();
                if (pointID.equals(extensionPointID)) {
                    String langID= getLanguageID(extension);
    
                    if (langID != null && langID.equals(langName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
