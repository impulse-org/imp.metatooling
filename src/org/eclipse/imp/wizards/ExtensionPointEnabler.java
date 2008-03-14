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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginModelFactory;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.IPluginParent;
import org.eclipse.pde.core.plugin.ISharedExtensionsModel;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModel;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.core.plugin.PluginElement;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.extensionsmodel.ImpWorkspaceExtensionsModel;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.imp.utils.StreamUtils;


/**
 * @author Claffra
 * @author rfuhrer@watson.ibm.com
 * @author suttons@us.ibm.com
 */
public class ExtensionPointEnabler {
    public static String findServiceImplClass(String servicePointID, String lang, String defaultImplClass) {
        // Following assumes that the impl class is given by the "class" attribute of
        // a child element whose name is the last component of the extension point ID.
        return findServiceAttribute(servicePointID, lang, servicePointID.substring(servicePointID.lastIndexOf('.')+1), "class", defaultImplClass);
    }

    public static String findServiceAttribute(String servicePointID, String lang, String childElementName, String attributeName, String defaultValue) {
        if (lang != null && lang.length() > 0) {
            // TODO Handle case where base-language plugin exists but not as workspace project
            IProject project= findProjectForLanguage(lang);
            IPluginExtension extension= getServiceExtension(servicePointID, project);
            IPluginElement element= findChildElement(childElementName, extension);
    
            return (element != null) ? element.getAttribute(attributeName).getValue() : defaultValue;
        }
        return defaultValue;
    }

    public static String determineLanguage(IProject project) {
        IPluginExtension extension= getServiceExtension(RuntimePlugin.IMP_RUNTIME + ".languageDescription", project);
        IPluginElement element= findChildElement("language", extension);
    
        if (element != null)
            return element.getAttribute("language").getValue();
        return "";
    }

    public static IPluginElement findChildElement(String elementName, IPluginExtension extension) {
        if (extension == null) return null;
    
        IPluginObject[] children= extension.getChildren();
    
        for(int k= 0; k < children.length; k++) {
            IPluginObject object= children[k];
    
            if (object.getName().equals(elementName)) {
        	return (IPluginElement) object;
            }
        }
        return null;
    }

    public static IPluginExtension getServiceExtension(String pointID, IProject project) {
        try {
            IPluginModel pluginModel= getPluginModel(project);
    
            if (pluginModel != null) {
    		   	// SMS 26 Jul 2007
    	        // Load the extensions model in detail, using the adapted IMP representation,
    	        // to assure that the children of model elements are represented
    	    	try {
    	    		ExtensionPointEnabler.loadImpExtensionsModel((IPluginModel)pluginModel, project);
    	    	} catch (CoreException e) {
    	    		System.err.println("GeneratedComponentWizardPage.discoverProjectLanguage():  CoreExeption loading extensions model; may not succeed");
    	    	} catch (ClassCastException e) {
    	    		System.err.println("GeneratedComponentWizardPage.discoverProjectLanguage():  ClassCastExeption loading extensions model; may not succeed");
    	    	}
    	    	
	        	IPluginExtension[] extensions= pluginModel.getExtensions().getExtensions();
	    
	        	for(int n= 0; n < extensions.length; n++) {
	        	    IPluginExtension extension= extensions[n];
	    
	                    if (extension.getPoint().equals(pointID))
	                        return extension;
	        	}
	        	System.out.println("Unable to find language descriptor extension in plugin '" + pluginModel.getBundleDescription().getName() + "'.");
            } else if (project != null)
            	System.out.println("Not a plugin project: " + project.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static IProject findProjectForLanguage(String langName) {
	IWorkspace ws= ResourcesPlugin.getWorkspace();
	IWorkspaceRoot root= ws.getRoot();
	IProject[] projects= root.getProjects();

	for(int i= 0; i < projects.length; i++) {
	    IProject project= projects[i];

	    try {
		if (project.isNatureEnabled("org.eclipse.pde.PluginNature")) {
		    String projLangName= determineLanguage(project);

		    if (projLangName.equals(langName))
			return project;
		}
	    } catch (CoreException e) {
	    }
	    // Do nothing if this was not a plugin project
	}
	return null;
    }

    public static void enable(
    		ExtensionPointWizardPage page,
    		boolean remove, IProgressMonitor monitor) {
	try {
	    IPluginModel pluginModel= getPluginModel(page.getProjectOfRecord());

	    if (pluginModel != null) {
	    	if (remove) {
	    		//System.out.println("ExtensionPointEnabler.enable(..):  removing previous extension for page = " + page.getName());
	    		removeExtension(pluginModel, page);
	    	} else {
	    		//System.out.println("ExtensionPointEnabler.enable(..):  not removing previous extension for page = " + page.getName());
	    	}
	    	// This call to addExtension takes care of adding
	    	// the appropriate extension id
	    	addExtension(pluginModel, page);
	    }
	} catch (Exception e) {
	    ErrorHandler.reportError("Could not enable extension point for " + page, e);
	}
    }
   
    public static void enable(
    		IProject project, String pluginID, String pointID, String[][] attrNamesValues, boolean replace, 
    		List imports, IProgressMonitor monitor) {
	try {
	    IPluginModel pluginModel= getPluginModelForProject(project);

	    if (pluginModel != null) {
	    	if (replace) {
	    		removeExtension(pluginModel, pluginID, pointID, attrNamesValues);
	    	}
	    	addExtension(project, pluginModel, pluginID, pointID, attrNamesValues, imports);
	    }
	} catch (Exception e) {
	    ErrorHandler.reportError("Could not enable extension point for " + project.getName(), e);
	}
    }

    // SMS 28 Nov 2007
    // Changed showDialog parameter in ErrorHandler.reportError to false on 
    // the assumption that a lot of error management will occur within dialogs
    public static IPluginModel getPluginModel(final IProject project) {
	try {
	    if (project == null) return null;

            maybeCreatePluginXML(project);
            return getPluginModelForProject(project);
	} catch (Exception e) {
	    ErrorHandler.reportError("Could not find plugin for project " + project.getName(), false, e);
	    return null;
	}
    }

    private static final String pluginXMLSkeleton= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<?eclipse version=\"3.0\"?>\n" +
    "<plugin>\n" +
    "</plugin>";

    private static void maybeCreatePluginXML(final IProject project) throws CoreException {
    	IFile pluginXML= project.getFile("plugin.xml"); 
    	if (!pluginXML.exists()) {
    		// SMS 10 Aug 2006:  to remove use of deprecated type StringBufferInputStream
    		//pluginXML.create(new StringBufferInputStream(pluginXMLSkeleton), false, new NullProgressMonitor());
    		byte[] skeletonBytes = pluginXMLSkeleton.getBytes();
    		pluginXML.create(new ByteArrayInputStream(skeletonBytes), false, new NullProgressMonitor());
    	}
    }

    // SMS 23 Mar 2007
    // Changed visibility to public so as to make available to CodeServiceWizard
    // for prospective changes to enable wizards to get the plugin package name
    // SMS 28 Nov 2007
    // Changed showDialog parameter in ErrorHandler.reportError to false on 
    // the assumption that a lot of error management will occur within dialogs
    public static IPluginModel getPluginModelForProject(final IProject project) {
//      WorkspaceModelManager wmm = PDECore.getDefault().getWorkspaceModelManager();
//      IPluginModelBase[] wsPlugins= wmm.getFeatureModel(project).getWorkspaceModels();
		PluginModelManager pmm = PDECore.getDefault().getModelManager();
		IPluginModelBase[] wsPlugins= pmm.getWorkspaceModels();
	
		if (wsPlugins.length == 0) {
		    ErrorHandler.reportError("Project " + project.getName() + " is not a plugin project (no plugin projects)?", false);
		    return null;
		}
		for(int i= 0; i < wsPlugins.length; i++) {
		    IPluginModelBase wsPlugin= wsPlugins[i];
	//	    if (wsPlugin.getBundleDescription().getName().equals(project.getName())) {
		    
		    // SMS 19 Jul 2006
		    // May get both workspace and project plugin models
		    // (although only the latter are of interest)
		    IPluginBase pmBase = wsPlugin.getPluginBase();
		    if (pmBase == null) continue;
		    String id = pmBase.getId();
		    if (id == null) continue;
		    String projName = project.getName();
		    if (projName == null) continue;
		       
		    // SMS 22 Mar 2007:  This depends on the plugin id being equal to the project name
//		    if (wsPlugin.getPluginBase().getId().equals(project.getName())) {
//		        return (IPluginModel) wsPlugin;
//		    }
		    // SMS 22 Mar 2007 Use this, instead:
		    String resourceLocation = pmBase.getModel().getUnderlyingResource().getLocation().toString();
		    if (resourceLocation.endsWith(projName + "/META-INF/MANIFEST.MF")) {
		    	return (IPluginModel) wsPlugin;
		    }
		    
		}
		ErrorHandler.reportError("Could not find plugin for project " + project.getName(), false);
		return null;
    }
    
    
    // SMS 27 Mar 2007
    // New; based on getPluginModelForProject(Iproject)
    // SMS 28 Nov 2007
    // Changed showDialog parameter in ErrorHandler.reportError to false on 
    // the assumption that a lot of error management will occur within dialogs
    public static String getPluginIDForProject(final IProject project) {
		PluginModelManager pmm = PDECore.getDefault().getModelManager();
		IPluginModelBase[] wsPlugins= pmm.getWorkspaceModels();
	
		if (wsPlugins.length == 0) {
		    ErrorHandler.reportError("Project " + project.getName() + " is not a plugin project (no plugin projects)?", false);
		    return null;
		}
		for(int i= 0; i < wsPlugins.length; i++) {
		    IPluginModelBase wsPlugin= wsPlugins[i];

		    // SMS 19 Jul 2006
		    // May get both workspace and project plugin models
		    // (although only the latter are of interest)
		    IPluginBase pmBase = wsPlugin.getPluginBase();
		    if (pmBase == null) continue;
		    String id = pmBase.getId();
		    if (id == null) continue;
		    String projName = project.getName();
		    if (projName == null) continue;

		    String resourceLocation = pmBase.getModel().getUnderlyingResource().getLocation().toString();
		    if (resourceLocation.endsWith(projName + "/META-INF/MANIFEST.MF")) {
		    	return id;
		    }
		}
		ErrorHandler.reportError("Could not find plugin id for project " + project.getName(), false);
		return null;
    }
    
    
    
    
    
    /**
     * Adds an extension to a plugin, where the extension is represented by
     * an ExtensionPointWizardPage.
     * 
     * @param pluginModel		Represents the plugin to which the extension is added
     * @param page				Represents the extension added to the plugin
     * @throws CoreException	If there's a problem working with the plugin or other models
     * @throws IOException		If there's a problem working with the plugin file
     */
    static void addExtension(IPluginModel pluginModel, ExtensionPointWizardPage page)
    	throws CoreException, IOException
    {
    	// SMS 26 Jul 2007
    	// Ideally, that is, if org.eclipse.pde.core supported all of the things that
    	// we'd like to do with the extensions model, we would just create an extension,
    	// fill in the plugin and point ids, add it to the extensions model, and save
    	// the model.  In rare cases that the extension model didn't exist (for example,
    	// before the first extension or extension point was created), the extensions
    	// model would also have to be created.  The model is loaded at startup (I assume),
    	// and listeners keep the loaded version up-to-date with respect to changes in
    	// the plugin.xml file.  (Of course, the file is changed every time the model is
    	// written out.)
    	//
    	// So what's not ideal?
    	// 1.  The extensions model is only read shallowly.  Certain details of the
    	//     model are omitted, and we sometimes need those details when creating our
    	//     extensions.  There seems to be no way in the current implementation of
    	//     org.eclipse.pde.core to access the details of the model
    	// 2.  Repeated cycles of saving the model followed by shallow reading of the
    	//     model lead to the destricution of previous extensions (or of their details).
    	//     When we add a new extension to the model and save the model, that extension
    	//     gets saved in detail, along with the rest of the extensions model, with
    	//     whatever detail it happens to have.  But then a listener reloads the model
    	//     from the file, omitting details, including details for the extension just
    	//     created.  So, when we next add the next extension, we add it to a model
    	//     that lacks the details of all previous extensions.  When we then save the
    	//     model, we overwrite any previously detailed extensions with versions that
    	//     lack details.
    	//     
    	// How can we end the cycle of destruction?
    	// 1.  Use our own subtype of the exstensions model in which we load the extensions
    	//     model in detail.
    	// 2.  Always create a new extensions model (of our own subtype) whenever we want to
    	//     add a new extension, not just when the model does not already exist.
    	// In this way, we can assure that we always have an up-to-date, in-depth extensions
    	// model available to us, regardless of any shallow extensions model that may have
    	// been previously loaded by other mechanisms in org.eclipse.pde.core
    	//


    	loadImpExtensionsModel(pluginModel, page.getProjectOfRecord());
        
        // Create the exstension, fill it out, and add it to the model if necessary
        IPluginExtension extension= pluginModel.getPluginFactory().createExtension();
        if (extension == null) {
        	ErrorHandler.reportError("Unable to create extension " + page.fExtPointID + " in plugin " + pluginModel.getBundleDescription().getName(), true);
        	return;
        }
		extension.setPoint(page.fExtPluginID + "." + page.fExtPointID);
		setElementAttributes(pluginModel, page, extension);
		if (!extension.isInTheModel())	
			pluginModel.getPluginBase().add(extension);
	
		addRequiredPluginImports(pluginModel, page.getProjectOfRecord(), page.getRequires());
		
		saveAndRefresh(pluginModel);
	}


    protected static String lowerCaseFirst(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }	
    
    
    // SMS 20 Jul 2006
    static void removeExtension(IPluginModel pluginModel, ExtensionPointWizardPage page)
    	throws CoreException, IOException
    {      
    	IPluginModelFactory pmFactory = pluginModel.getPluginFactory();
    	IExtensions pmExtensions = pluginModel.getExtensions();
    	IPluginExtension[] pluginExtensions = pmExtensions.getExtensions();
    	for (int i = 0; i < pluginExtensions.length; i++) {
    		IPluginExtension pluginExtension = pluginExtensions[i];
    		if (pluginExtension == null) continue;
    		if (pluginExtension.getPoint() == null) continue;
    		String point = page.fExtPluginID + "." + page.fExtPointID;
    		if (pluginExtension.getPoint().equals(point)) {
				pmExtensions.remove(pluginExtension);
    		}
    	}
    	saveAndRefresh(pluginModel);
     }

 
    /**
     * Adds an extension to a plugin, where the extension is represented by
     * various given values.
     * 
     * NOTE:  As of 21 Jul 2006, the implementation of this method first removes the 
     * extension from the plugin, if it exists there, on the assumption that the
     * extension being added is intended to replace the existing one.  Without this,
     * multiple copies of the same extension can accumulate in the plugin.xml file,
     * and the superfluous ones would have to be removed explicitly by the user.
     * This approach is probably safe for SAFARI so long as the number of languages
     * per plugin is restricted to at most one, which seems to be the current practical
     * limit.  If there may be multiple languages per plugin, then some additional
     * care needs to be taken regarding the removal of existing extensions.	
     * 
     * @param pluginModel		Represents the plugin to which the extension is added
     * @param pluginID			The id of the plugin offering the extension point
     * @param pointID			The id of the specific extension point (without plugin)
     * @param attrNamesValues	Name and values for attributes for the extension
     * @throws CoreException	If there's a problem working with the plugin or other models
     * @throws IOException		If there's a problem working with the plugin file
     */
    public static void addExtension(
    	IProject project, IPluginModel pluginModel, String pluginID, String pointID, String[][] attrNamesValues, List imports)
    throws CoreException, IOException
    {

    	loadImpExtensionsModel(pluginModel, project);
    	
        // Create the exstension, fill it out, and add it to the model if necessary
        IPluginExtension extension= pluginModel.getPluginFactory().createExtension();
        if (extension == null) {
			ErrorHandler.reportError("Unable to create extension " + pointID + " in plugin " + pluginModel.getBundleDescription().getName(), true);
			return;
        }
		extension.setPoint(pluginID + "." + pointID);
		setElementAttributes(pluginModel, extension, attrNamesValues);
        
		if (!extension.isInTheModel())	
			pluginModel.getPluginBase().add(extension);	

		addRequiredPluginImports(pluginModel, project, imports);
		saveAndRefresh(pluginModel);
     }

    
    
    public static ImpWorkspaceExtensionsModel loadImpExtensionsModel(IPluginModel pluginModel, IProject project)
    	throws CoreException
    {
        String filename= "plugin.xml";
        IFile file= project.getFile(filename);
        // Evidently, just creating the extensions model with a given file doesn't
        // actually load the model from that file, which must be done explicitly
        ImpWorkspaceExtensionsModel extensions = new ImpWorkspaceExtensionsModel(file);
        extensions.load(file.getContents(), true);
        
        // Hook the extensions model into the bundle plugin model base
        IBundlePluginModelBase bpmb= (IBundlePluginModelBase) pluginModel;
        extensions.setBundleModel(bpmb);	
        bpmb.setExtensionsModel(extensions);
    	return extensions;
    }
    
    
    
    // SMS 20 Jul 2006
    public static void removeExtension(IPluginModel pluginModel, String pluginID, String pointID, String[][] attrNamesValues)
    	throws CoreException, IOException
    {
    	// SMS 10 Jul 2007:  pmFactory apparently not used
    	//IPluginModelFactory pmFactory = pluginModel.getPluginFactory();
    	IExtensions pmExtensions = pluginModel.getExtensions();
    	IPluginExtension[] pluginExtensions = pmExtensions.getExtensions();
    	for (int i = 0; i < pluginExtensions.length; i++) {
    		IPluginExtension pluginExtension = pluginExtensions[i];
    		if (pluginExtension == null) continue;
    		if (pluginExtension.getPoint() == null) continue;
    		String point = pluginID + "." + pointID;
    		if (pluginExtension.getPoint().equals(point)) {
    				pmExtensions.remove(pluginExtension);
    		}
    	}
    	saveAndRefresh(pluginModel);
    }
    
    
    private static void setElementAttributes(
    	IPluginModel pluginModel, ExtensionPointWizardPage page, IPluginExtension extension)
    	throws CoreException
    {
		List fields= page.getFields();
		Map/*<String qualElemName, PluginElement>*/ elementMap= new HashMap(); // so we can find nested/parent elements after they've been created, somewhat regardless of the field ordering
	
		elementMap.put("extension", extension); // Let nested elements find the extension object itself by name

		for(int n= 0; n < fields.size(); n++) {
		    WizardPageField field= (WizardPageField) fields.get(n);
	            String schemaElementName= field.fSchemaElementName;
	            String attributeName= field.fAttributeName;
	            String attributeValue= field.fValue;
	
	        // SMS 26 Jul 2007
	        // Here we used to have a little bit of code that adjusted the extension id according to
	        // to whether it began with the plugin id.  Now we just trust whoever or whatever set the
	        // extension id and leave it at that.
	            
		    setElementAttribute(schemaElementName, attributeName, attributeValue, extension, elementMap, pluginModel);
		}
    }

    	
    private static void setElementAttribute(String schemaElementName, String attributeName, String attributeValue, IPluginExtension extension, Map elementMap, IPluginModel pluginModel) throws CoreException {
	
    	if (schemaElementName.equals("extension")) {
            // Handle the top-level element (the extension that was passed in)
            if (attributeName.equals("id")) {
                extension.setId(attributeValue);
            }
            else if (attributeName.equals("name")) {
                extension.setName(attributeValue);
            }
            else
                System.err.println("Unknown 'extension' attribute: '" + attributeName + "'.");
        }
        // SMS 10 May 2006
        // Avoid irregular "builders" element
        else if (schemaElementName.equals("builders")) {
        	System.err.println("Irregular schema element name 'builders'; ignoring");
        }
        // End SMS
    	else {
            // Handle all other elements - create on first reference
            PluginElement elt= (PluginElement) elementMap.get(schemaElementName);

            if (elt == null) {
                int lastDotIdx= schemaElementName.lastIndexOf('.');
                String elemName= schemaElementName.substring(lastDotIdx+1);
                IPluginParent parent;

                elt= new PluginElement();
                elt.setModel(pluginModel);
                elt.setName(elemName);
                if (lastDotIdx > 0) {
                    String parentElemName= schemaElementName.substring(0, lastDotIdx);

                    parent= (IPluginParent) elementMap.get(parentElemName);
                } else
                    parent= extension;
                elt.setParent(parent);
                parent.add(elt);
                elementMap.put(schemaElementName, elt); // use "fully-qualified" name here
            }
            // Ok, we've found the right element; set the attribute
        	// attributeName can be empty if a nested element has no attributes;
            // attributeValue can be empty if, well, an attribute has no value
            // (e.g., if it's an optional attribute for which a wizard provides a field
            // but which the user need not set)
            if (attributeName.length() > 0 && attributeValue != null && attributeValue.length() > 0) {
                elt.setAttribute(attributeName, attributeValue);
            }
        }
    }

    /**
     * Like the above flavor of setElementAttributes(), but takes its name/value pairs from
     * an explicit array, rather than the WizardPageFields in an ExtensionPointWizardPage.
     * @param pluginModel
     * @param extension
     * @param attrNamesValues
     * @throws CoreException
     */
    public static void setElementAttributes(
    	IPluginModel pluginModel, IPluginExtension extension, String[][] attrNamesValues) throws CoreException
    {
        Map/*<String qualElemName, PluginElement>*/ elementMap= new HashMap(); // so we can find nested/parent elements after they've been created, somewhat regardless of the field ordering

        elementMap.put("extension", extension); // Let nested elements find the extension object itself by name

		for(int i= 0; i < attrNamesValues.length; i++) {
		    String elementAttrName= attrNamesValues[i][0];
	            String elementName= elementAttrName.substring(0, elementAttrName.indexOf(':'));
	            String attrName= elementAttrName.substring(elementAttrName.indexOf(':') + 1);
	            String attrValue= attrNamesValues[i][1];
	
	            System.out.println("Creating attribute " + elementAttrName + " => " + attrValue);
	
	            setElementAttribute(elementName, attrName, attrValue, extension, elementMap, pluginModel);
		}
    }

    public static void addRequiredPluginImports(
    	IPluginModel pluginModel, IProject project, List/*<String>*/ requires)
    	throws CoreException
   	{
    	if (!project.exists()) {
		    ErrorHandler.reportError("Project does not exist:  " + project);
		    return;
    	}
    	
    	// SMS 24 Jul 2007	
    	IFile manifestFile = project.getFile("META-INF/MANIFEST.MF");
    	String manifestContents = null;
    	if (manifestFile.exists()) {
    		manifestContents = StreamUtils.readStreamContents(manifestFile.getContents(), manifestFile.getCharset());
    	} else {
		    ErrorHandler.reportError("Could read manifest file for project = " + project);
		    return;
    	}
    	
    	List importsList = new ArrayList();	
    	int requireBundleStart = manifestContents.indexOf("Require-Bundle");
    	int requireBundleSemicolon = -1;
    	int nextBundleSemicolon = -1;
    	// This part especially is fragile (assumes semicolon immediately follows bundle name):
    	int nextBundleStart = -1;
    	if (requireBundleStart > -1) {
    		// manifest file has a require bundle
	    	requireBundleSemicolon = manifestContents.indexOf(":", requireBundleStart);
	    	nextBundleSemicolon = manifestContents.indexOf(":", requireBundleSemicolon+1);
	    	if (nextBundleSemicolon > -1) {
	    		// there is a bundle following the require bundle
	    		// (this part especially is fragile (assumes semicolon immediately follows bundle name))
	    		nextBundleStart = manifestContents.lastIndexOf("\n", nextBundleSemicolon) + 1;
	    	} else {
	    		// there is no bundle following the rquire bundle;
	    		// next bundle start will remain as initialized	 to -1
	    	}
	    	String requireBundleText = null;
	    	if (nextBundleStart > requireBundleStart) {
	    		// require bundle is not the last bundle in the file
	    		// so get text from require bundle start up to the next bundle
	    		requireBundleText = manifestContents.substring(requireBundleSemicolon+1, nextBundleStart);
	    	} else {
	    		// require bundle is the last bundle in the file
	    		// so get text from require bundle start up to the end of the file
	    		requireBundleText = manifestContents.substring(requireBundleSemicolon+1);
	    	}
	    	// clean junk out of the string of import ids
	    	requireBundleText = requireBundleText.replace(" ", "");
	    	requireBundleText = requireBundleText.replace("\n", "");
	    	requireBundleText = requireBundleText.replace("\r", "");
	    	requireBundleText = requireBundleText.replace("\t", "");
	    	// put the existing import ids into a list (by way of an array)
	    	String[] importsArray = requireBundleText.split(",");
	    	for (int i = 0; i < importsArray.length; i++) {
	    		importsList.add(importsArray[i]);
	    	}
    	} else {
    		// manifest file lacks a require bundle
    		// so list of existing import ids will be empty
    	}
    	
    	// add any required import ids that are not already in the list
    	for (int i = 0; i < requires.size(); i++) {	
    		if (importsList.contains(requires.get(i)))
    			continue;
    		importsList.add(requires.get(i));
    	}
    	
    	// put the list of import ids back into a string that
    	// defines the require bundle
    	String newRequireBundleText = "Require-Bundle: ";
    	for (int i = 0; i < importsList.size(); i++) {
    		newRequireBundleText += importsList.get(i);
    		if (i < importsList.size()-1)
    			newRequireBundleText += ",\n ";
    		else
    			newRequireBundleText += "\n";
    	}			
    	
    	// put the new require bundle text back into the manifest file
    	// surrounded by whatever it was surrounded by before
    	String newManifestContents = null;
    	if (requireBundleStart > -1) {
    		newManifestContents = manifestContents.substring(0, requireBundleStart);
    	} else {
    		newManifestContents = manifestContents;
    	}
    	if (!newManifestContents.endsWith("\n"))
    		newManifestContents += "\n";
    	newManifestContents += newRequireBundleText;
    	if (nextBundleStart > -1) {
    		newManifestContents += manifestContents.substring(nextBundleStart);
    	}
    	
    	// Now make sure that the singleton directive is set
    	// This is kind of a simplistic approach, but it will work since
    	// multiple declarations of the singleton directive are tolerated
    	String updatedNewManifestContents = newManifestContents;
    	int singletonStart = newManifestContents.indexOf("singleton:=true");
    	if (singletonStart < 0) {
    		// Append the singleton directive to the line containing the 
    		// Bundle-SymbolicName
    		int bundleSymbolicNameStart = newManifestContents.indexOf("Bundle-SymbolicName");
    		if (bundleSymbolicNameStart > -1) {
    			// We have the line on which to operate
    			int endLineBundleSymbolicName = newManifestContents.indexOf("\n", bundleSymbolicNameStart);
    			if (endLineBundleSymbolicName < 0) {
    				// Bundle-SymbolicName is the last line in the file
    				endLineBundleSymbolicName = newManifestContents.length();
    			}
    			// The following will put the singleton directive ahead of the "\n" that
    			// is already at the end of the line that contains the Bundle-SymbolicName
    			// (so don't add another "\n")
    			updatedNewManifestContents = newManifestContents.substring(0,endLineBundleSymbolicName);
    			updatedNewManifestContents += ";singleton:=true";
    			if (endLineBundleSymbolicName != newManifestContents.length()) {
    				updatedNewManifestContents += newManifestContents.substring(endLineBundleSymbolicName, newManifestContents.length());
    			}
    		} else {
    			// We don't have the line, which seems extremely unlikely to me,
    			// and probably represents some sort of error that represents a
    			// bigger problem than the lack of a singleton directive, so
    			// just skip it
    		}
    	}
    	
    	// Put the text back into the file
		manifestFile.setContents(new ByteArrayInputStream(updatedNewManifestContents.getBytes()), true, true, null);

    }
    
    
    public static boolean hasRequiredPluginImport(IProject project, String bundleName)
    {
    	if (!project.exists()) {
		    ErrorHandler.reportError("Project does not exist:  " + project);
			return false;
    	}
    	IFile manifestFile = project.getFile("META-INF/MANIFEST.MF");
    	String manifestContents = null;
    	if (manifestFile.exists()) {
    		try {
    			manifestContents = StreamUtils.readStreamContents(manifestFile.getContents(), manifestFile.getCharset());
    		} catch (CoreException e) {
    		    ErrorHandler.reportError("Could read manifest file for project = " + project, e);
    			return false;
    		}
    	} else {
		    ErrorHandler.reportError("Manifest file does not exist for project = " + project);
    		return false;
    	}
    	
    	int requireBundleOffset = manifestContents.indexOf("Require-Bundle:");
    	if (requireBundleOffset < 0)
    		return false;
    	
    	int followingFieldOffset = manifestContents.indexOf(':', requireBundleOffset+15);
    	if (followingFieldOffset < 0)
    		followingFieldOffset = manifestContents.length();
    	
    	int bundleNameOffset = manifestContents.indexOf(bundleName);
    	if (bundleNameOffset < 0)
    		return false;
    	
    	if (bundleNameOffset > requireBundleOffset && bundleNameOffset < followingFieldOffset)
    		return true;
    	
    	return false;
    }
    
    
    
    public static void removeRequiredPluginImport(IProject project, String bundleName)
    {
    	if (!project.exists()) {
		    ErrorHandler.reportError("Project does not exist:  " + project);
			return;
    	}
    	IFile manifestFile = project.getFile("META-INF/MANIFEST.MF");
    	String manifestContents = null;
    	if (manifestFile.exists()) {
    		try {
    			manifestContents = StreamUtils.readStreamContents(manifestFile.getContents(), manifestFile.getCharset());
    		} catch (CoreException e) {
    		    ErrorHandler.reportError("Could read manifest file for project = " + project, e);
    		    return;
    		}
    	} else {
		    ErrorHandler.reportError("Manifest file does not exist for project = " + project);
		    return;
    	}
    	
    	int requireBundleOffset = manifestContents.indexOf("Require-Bundle:");
    	if (requireBundleOffset < 0)
    		return;
    	
    	int followingFieldOffset = manifestContents.indexOf(':', requireBundleOffset+15);
    	if (followingFieldOffset < 0)
    		followingFieldOffset = manifestContents.length();
    	
    	int bundleNameOffset = manifestContents.indexOf(bundleName);
    	if (bundleNameOffset < 0)
    		return;
    	
    	String newManifestContents = null;
    	if (bundleNameOffset > requireBundleOffset && bundleNameOffset < followingFieldOffset) {
        	int previousNewLineOffset = manifestContents.lastIndexOf('\n', bundleNameOffset);
        	int nextNewLineOffset = manifestContents.indexOf('\n', bundleNameOffset);
        	int nextCommaOffset = manifestContents.indexOf(',', bundleNameOffset);
        	if (nextCommaOffset < 0)
        		nextCommaOffset = manifestContents.length();
        	boolean commaAfterBundleName = nextCommaOffset < nextNewLineOffset;
        	int previousCommaOffset = manifestContents.lastIndexOf(',', bundleNameOffset);

        	int firstTruncationOffset = commaAfterBundleName ? previousNewLineOffset : previousCommaOffset;
        	
        	newManifestContents = manifestContents.substring(0, firstTruncationOffset);
        	if (nextNewLineOffset > -1)
        		newManifestContents+= manifestContents.substring(nextNewLineOffset);
        	else
        		newManifestContents+= "\n";
    	}

    	// Put the text back into the file
    	try {
    		manifestFile.setContents(new ByteArrayInputStream(newManifestContents.getBytes()), true, true, null);
		} catch (CoreException e) {
		    ErrorHandler.reportError("Could not write updated manifest file for project = " + project, e);
		    return;
		}
    }
    
    
    
    
    private static void saveAndRefresh(IPluginModel pluginModel) throws CoreException {
		if (pluginModel instanceof IBundlePluginModel) {
		    IBundlePluginModel bundlePluginModel= (IBundlePluginModel) pluginModel;
		    ISharedExtensionsModel extModel= bundlePluginModel.getExtensionsModel();

		    if (extModel != null) {
		    	// SMS 15 Jul 2007
		    	// If the bundle plugin model is saved below, then you don't need
		    	// to save the 	extensions model separately here, since that model
		    	// is saved as part of the bundle plugin model.  (But if the extensions
		    	// model isn't saved below, then it should be saved here)
			    if (extModel instanceof IEditableModel) {
			    	((IEditableModel) extModel).save();	
			    }

		    	// SMS 15 Jul 2007
			    // Will separately save the bundle model and extensions model
			    // (Note:  It's the bundle model that represents the manifest.mf file)
			    // The Eclipse implementation for each model first checks whether the model
			    // is dirty and then only saves the model if so.  Our operations on the
			    // extensions model have the effect of marking it dirty, but our operations
			    // on the bundle model don't have that effect (for reasons we don't understand).
			    // Consequently, we have adapted the implementation of BundlePluginModelBase
			    // to remove the test for the model being dirty and to save it in any case.
			    //
			    // SMS 24 Jul 2007
			    // BUT don't do this if updating the manifest file directly!
//			    bundlePluginModel.save();
		    }
		}
		pluginModel.getUnderlyingResource().refreshLocal(1, null);
    }

}
