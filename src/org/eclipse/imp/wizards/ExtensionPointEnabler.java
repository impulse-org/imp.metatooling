package org.eclipse.uide.wizards;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
//import java.io.StringBufferInputStream;
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
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.IPluginParent;
import org.eclipse.pde.core.plugin.ISharedExtensionsModel;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModel;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.core.plugin.PluginElement;
import org.eclipse.pde.internal.core.plugin.PluginImport;
import org.eclipse.pde.internal.core.plugin.WorkspaceExtensionsModel;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.runtime.RuntimePlugin;

import org.eclipse.pde.core.plugin.IPluginModelFactory;		// SMS 20 Jul 2006
import org.eclipse.pde.core.plugin.IExtensions;				// SMS 20 Jul 2006


/**
 * @author Claffra
 * @author rfuhrer@watson.ibm.com
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
        IPluginExtension extension= getServiceExtension(RuntimePlugin.UIDE_RUNTIME + ".languageDescription", project);
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

    public static void enable(ExtensionPointWizardPage page, IProgressMonitor monitor) {
	try {
	    IPluginModel pluginModel= getPluginModel(page.getProject());

	    if (pluginModel != null) {
	    	// This call to addExtension takes care of adding
	    	// the appropriate extension id
		addExtension(pluginModel, page);
	    }
	} catch (Exception e) {
	    ErrorHandler.reportError("Could not enable extension point for " + page, e);
	}
    }


    public static void enable(IProject project, String pluginID, String pointID, String[][] attrNamesValues, IProgressMonitor monitor) {
	try {
	    IPluginModel pluginModel= getPluginModelForProject(project);

	    if (pluginModel != null) {
		addExtension(pluginModel, pluginID, pointID, attrNamesValues);
	    }
	} catch (Exception e) {
	    ErrorHandler.reportError("Could not enable extension point for " + project.getName(), e);
	}
    }

    public static IPluginModel getPluginModel(final IProject project) {
	try {
	    if (project == null) return null;

            maybeCreatePluginXML(project);
            return getPluginModelForProject(project);
	} catch (Exception e) {
	    ErrorHandler.reportError("Could not find plugin for project " + project.getName(), true, e);
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

    private static IPluginModel getPluginModelForProject(final IProject project) {
//      WorkspaceModelManager wmm = PDECore.getDefault().getWorkspaceModelManager();
//      IPluginModelBase[] wsPlugins= wmm.getFeatureModel(project).getWorkspaceModels();
	PluginModelManager pmm = PDECore.getDefault().getModelManager();
	IPluginModelBase[] wsPlugins= pmm.getWorkspaceModels();

	if (wsPlugins.length == 0) {
	    ErrorHandler.reportError("Project " + project.getName() + " is not a plugin project (no plugin projects)?", true);
	    return null;
	}
	for(int i= 0; i < wsPlugins.length; i++) {
	    IPluginModelBase wsPlugin= wsPlugins[i];
//	    if (wsPlugin.getBundleDescription().getName().equals(project.getName())) {
	    
	    // SMS 19 Jul 2006
	    // It seems that at least sometimes one of these models might be a workspace plugin model
	    // (as opposed to a project plugin model), and at least some of those may have no ID
	    // (perhaps those for a runtime workbench?).  Anyway, it seems reasonable to skip over any
	    // model where any element of interest is null, since that won't be what we're looking for
	    // in any case
	    IPluginBase pmBase = wsPlugin.getPluginBase();
	    if (pmBase == null) continue;
	    String id = pmBase.getId();
	    if (id == null) continue;
	    String projName = project.getName();
	    if (projName == null) continue;
	    
	    if (wsPlugin.getPluginBase().getId().equals(project.getName())) {
	        return (IPluginModel) wsPlugin;
	    }
	}
//	    IPluginModelBase thePluginModel= pmm.findModel(project);
//
//	    return (IPluginModel) thePluginModel;
	ErrorHandler.reportError("Could not find plugin for project " + project.getName(), true);
	return null;
    }
    
    
    /**
     * Adds an extension to a plugin, where the extension is represented by
     * an ExtensionPointWizardPage.
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
     * 
     * @param pluginModel		Represents the plugin to which the extension is added
     * @param page				Represents the extension added to the plugin
     * @throws CoreException	If there's a problem working with the plugin or other models
     * @throws IOException		If there's a problem working with the plugin file
     */
    static void addExtension(IPluginModel pluginModel, ExtensionPointWizardPage page) throws CoreException, IOException {
    	
    	// SMS 20 Jul 2006
    	// Delete previous extension of this type, which is presumably
    	// being replaced by the one being added here
    	removeExtension(pluginModel, page);
    	
    	IPluginExtension extension= pluginModel.getPluginFactory().createExtension();
	
        if (extension == null) {
            String filename= "plugin.xml";
            IBundlePluginModelBase bundleModel= (IBundlePluginModelBase) pluginModel;
            IFile file= page.getProject().getFile(filename);
            WorkspaceExtensionsModel extensions= new WorkspaceExtensionsModel(file);

            extensions.load(file.getContents(), true);
            extensions.setBundleModel(bundleModel);
            bundleModel.setExtensionsModel(extensions);
            extension= pluginModel.getPluginFactory().createExtension();

            if (extension == null) {
            	ErrorHandler.reportError("Unable to create extension " + page.fExtPointID + " in plugin " + pluginModel.getBundleDescription().getName(), true);
            	return;
            }
        }
		extension.setPoint(page.fExtPluginID + "." + page.fExtPointID);
		// SMS 21 Jul 2006
		// Note:  Above we set the point of the extension but nowhere
		// do we set the name or id.  That has been accommodated by deleting
		// the name and id attributes of the extensions we create (none of
		// which really need those attributes).
		// Logic used in ExtensionPointWizardPage can be adapted if it
		// ever becomes necessary to determine name or id values for
		// an extension.
		
		setElementAttributes(pluginModel, page, extension);
		// N.B. BundlePluginBase.add(IPluginExtension) has logic to add the "singleton directive" if needed.
		//      As a result, we call getPluginBase().add() below rather than getExtensions().add()...
		IPluginBase pluginBase= pluginModel.getPluginBase();
	
		if (!extension.isInTheModel())
		    pluginBase.add(extension);
	
		addRequiredPluginImports(pluginModel, page.getRequires());
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
    public static void addExtension(IPluginModel pluginModel, String pluginID, String pointID, String[][] attrNamesValues) throws CoreException, IOException {

    	// SMS 20 Jul 2006
    	// Delete previous extension of this type, which is presumably
    	// being replaced by the one being added here
	// TODO RMF 10/19/2006 - Should enhance this API to permit multiple extensions per extension point
	// (Add boolean parameter that says whether to permit multiple extensions.)
    	removeExtension(pluginModel, pluginID, pointID, attrNamesValues);
    	
    	IPluginExtension extension= pluginModel.getPluginFactory().createExtension();

        if (extension == null) {
            ErrorHandler.reportError("Unable to create extension " + pointID + " in plugin " + pluginModel.getBundleDescription().getName(), true);
        }
		extension.setPoint(pluginID + "." + pointID);
		setElementAttributes(pluginModel, extension, attrNamesValues);
		// N.B. BundlePluginBase.add(IPluginExtension) has logic to add the "singleton directive" if needed.
		//      As a result, we call getPluginBase().add() below rather than getExtensions().add()...
		IPluginBase pluginBase= pluginModel.getPluginBase();
	
		if (!extension.isInTheModel())
		    pluginBase.add(extension);
        saveAndRefresh(pluginModel);
    }

    
    // SMS 20 Jul 2006
    public static void removeExtension(IPluginModel pluginModel, String pluginID, String pointID, String[][] attrNamesValues)
    	throws CoreException, IOException
    {
    	
    	IPluginModelFactory pmFactory = pluginModel.getPluginFactory();
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
    
    
    private static void setElementAttributes(IPluginModel pluginModel, ExtensionPointWizardPage page, IPluginExtension extension) throws CoreException {
	List fields= page.getFields();
	Map/*<String qualElemName, PluginElement>*/ elementMap= new HashMap(); // so we can find nested/parent elements after they've been created, somewhat regardless of the field ordering

	elementMap.put("extension", extension); // Let nested elements find the extension object itself by name

	for(int n= 0; n < fields.size(); n++) {
	    WizardPageField field= (WizardPageField) fields.get(n);
            String schemaElementName= field.fSchemaElementName;
            String attributeName= field.fAttributeName;
            String attributeValue= field.fValue;

//	    System.out.println(field);

	    // RMF 10/18/2006:
	    // Plugin extension ID's should not include the plugin ID as a prefix; it's
	    // implicit. [Extension refs on the other hand must be "fully qualified".]
	    // The following code removes the plugin ID prefix if this field represents
	    // an "id" attribute of an "extension" element.
	    String pluginID= pluginModel.getPlugin().getId();

	    if (schemaElementName.equals("extension") && attributeName.equals("id") && attributeValue.startsWith(pluginID + "."))
		attributeValue= attributeValue.substring(pluginID.length() + 1);

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
            if (attributeName.length() > 0 /* SMS 11 May 2006 */ && attributeValue.length() > 0) {
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
    public static void setElementAttributes(IPluginModel pluginModel, IPluginExtension extension, String[][] attrNamesValues) throws CoreException {
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

    private static void addRequiredPluginImports(IPluginModel pluginModel, List/*<String>*/ requires) throws CoreException {
	IPluginBase base= pluginModel.getPluginBase();
	// RMF Ask the model's associated bundle description for the list
	// of required bundles; I've seen this list be out of sync wrt the
	// list of imports in the IPluginBase (e.g. the latter is empty).
	IPluginImport[] imports= base.getImports();
//	BundleSpecification[] reqBundles= pluginModel.getBundleDescription().getRequiredBundles();
//	IPluginModelFactory pluginFactory= pluginModel.getPluginFactory();
//	/*IPluginImport[] curImports=*/ base.getImports(); // make sure the 'base.imports' field is non-null; otherwise, subsequent calls to base.add() are a noop!

        for(int n= 0; n < requires.size(); n++) {
	    String pluginID= (String) requires.get(n);
	    boolean found= containsImports(imports, pluginID);

	    if (!found /*!containsImport(reqBundles, pluginID*/) {
		PluginImport importNode= new PluginImport(); // pluginFactory.createImport();
                importNode.setModel(pluginModel);
                importNode.setId(pluginID);
                importNode.setInTheModel(true);
                importNode.setParent(base);
		base.add(importNode);
	    }
	}
        // HACK RMF 10/19/2006 - "diddle" the import list by swapping the last 2
        // entries to try to get PDE/JDT to notice the additions.
        // HAS NO EFFECT!
//        IPluginImport[] newImports= base.getImports();
//
//        if (newImports.length >= 2) {
//            int N= newImports.length;
//            base.swap(newImports[N-2], newImports[N-1]);
//        }
    }

    private static boolean containsImports(IPluginImport[] imports, String pluginID) {
	boolean found= false;
	for(int i= 0; i < imports.length; i++) {
	    if (imports[i].getId().equals(pluginID)) {
		found= true;
		break;
	    }
	}
	return found;
    }

//    private static boolean containsImport(BundleSpecification[] imports, String pluginID) {
//	boolean found= false;
//	for(int i= 0; i < imports.length; i++) {
//	    if (imports[i].getBundle().getSymbolicName().equals(pluginID)) {
//		found= true;
//		break;
//	    }
//	}
//	return found;
//    }

    static public void addImports(ExtensionPointWizardPage page) {
	try {
	    IPluginModel plugin= getPluginModel(page.getProject());

	    if (plugin == null) return;

	    addRequiredPluginImports(plugin, page.fRequiredPlugins);

	    if (plugin instanceof IBundlePluginModel) {
		IBundlePluginModel model= (IBundlePluginModel) plugin;

		if (model.getPlugin() instanceof IEditableModel)
		    ((IEditableModel) model.getPlugin()).save();
		if (model.getBundleModel() instanceof IEditableModel)
		    ((IEditableModel) model.getBundleModel()).save();
	    }
	    plugin.getUnderlyingResource().refreshLocal(1, null);
	} catch (Exception e) {
	    ErrorHandler.reportError("Could not enable extension point for " + page, e);
	}
    }

    static public void addImports(IProject project, List/*<String>*/ requiredPlugins) {
	try {
	    IPluginModel plugin= getPluginModel(project);

	    if (plugin == null) return;

	    addRequiredPluginImports(plugin, requiredPlugins);

	    if (plugin instanceof IBundlePluginModel) {
		IBundlePluginModel model= (IBundlePluginModel) plugin;

		if (model.getPlugin() instanceof IEditableModel)
		    ((IEditableModel) model.getPlugin()).save();
		if (model.getBundleModel() instanceof IEditableModel)
		    ((IEditableModel) model.getBundleModel()).save();
	    }
	    plugin.getUnderlyingResource().refreshLocal(1, null);
	} catch (Exception e) {
	    ErrorHandler.reportError("Could not add plugin imports to " + project.getName(), e);
	}
    }

    private static void saveAndRefresh(IPluginModel plugin) throws CoreException {
	if (plugin instanceof IBundlePluginModel) {
	    IBundlePluginModel bundlePluginModel= (IBundlePluginModel) plugin;
	    ISharedExtensionsModel extModel= bundlePluginModel.getExtensionsModel();

	    if (extModel instanceof IEditableModel)
		((IEditableModel) extModel).save();
	    bundlePluginModel.save(); // This blows away the entire MANIFEST.MF...
	}
	plugin.getUnderlyingResource().refreshLocal(1, null);
    }
}
