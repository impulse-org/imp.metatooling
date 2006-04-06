package org.eclipse.uide.wizards;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.core.plugin.IPluginBase;
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

/**
 * @author Claffra
 */
public class ExtensionPointEnabler {
    // BUG Should accept an extension ID, or is that already in the page?
    public static void enable(ExtensionPointWizardPage page, IProgressMonitor monitor) {
	try {
	    IPluginModel pluginModel= getPluginModel(page);

	    if (pluginModel != null) {
		addExtensionPoint(pluginModel, page);
	    }
	} catch (Exception e) {
	    ErrorHandler.reportError("Could not enable extension point for " + page, e);
	}
    }

    // BUG Should accept an extension ID, or is that already in attrNamesValues?
    public static void enable(IProject project, String pluginID, String pointID, String[][] attrNamesValues, IProgressMonitor monitor) {
	try {
	    IPluginModel pluginModel= getPluginModelForProject(project);

	    if (pluginModel != null) {
		addExtensionPoint(pluginModel, pluginID, pointID, attrNamesValues);
	    }
	} catch (Exception e) {
	    ErrorHandler.reportError("Could not enable extension point for " + project.getName(), e);
	}
    }

    public static IPluginModel getPluginModel(final ExtensionPointWizardPage page) {
	try {
	    final IProject project= page.getProject();

	    if (project == null) return null;

            maybeCreatePluginXML(project);
            return getPluginModelForProject(project);
	} catch (Exception e) {
	    ErrorHandler.reportError("Could not find plugin for project " + page.getProject().getName(), true, e);
	    return null;
	}
    }

    private static final String pluginXMLSkeleton= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<?eclipse version=\"3.0\"?>\n" +
    "<plugin>\n" +
    "</plugin>";

    private static void maybeCreatePluginXML(final IProject project) throws CoreException {
	IFile pluginXML= project.getFile("plugin.xml"); 
	if (!pluginXML.exists())
	    pluginXML.create(new StringBufferInputStream(pluginXMLSkeleton), false, new NullProgressMonitor());
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

    static void addExtensionPoint(IPluginModel pluginModel, ExtensionPointWizardPage page) throws CoreException, IOException {
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
	setElementAttributes(pluginModel, page, extension);
	// N.B. BundlePluginBase.add(IPluginExtension) has logic to add the "singleton directive" if needed.
	//      As a result, we call getPluginBase().add() below rather than getExtensions().add()...
	IPluginBase pluginBase= pluginModel.getPluginBase();

	if (!extension.isInTheModel())
	    pluginBase.add(extension);

	addRequiredPluginImports(pluginModel, page);
	saveAndRefresh(pluginModel);
    }

    public static void addExtensionPoint(IPluginModel pluginModel, String pluginID, String pointID, String[][] attrNamesValues) throws CoreException, IOException {
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
    }

    private static void setElementAttributes(IPluginModel pluginModel, ExtensionPointWizardPage page, IPluginExtension extension) throws CoreException {
	List fields= page.getFields();
	Map/*<String qualElemName, PluginElement>*/ elementMap= new HashMap(); // so we can find nested/parent elements after they've been created, somewhat regardless of the field ordering

	elementMap.put("extension", extension); // Let nested elements find the extension object itself by name

	for(int n= 0; n < fields.size(); n++) {
	    WizardPageField field= (WizardPageField) fields.get(n);

	    System.out.println(field);
	    if (field.fSchemaElementName.equals("extension")) {
		// Handle the top-level element (the extension that was passed in)
		if (field.fName.equals("id"))
		    extension.setId(field.fValue);
		else if (field.fName.equals("name"))
		    extension.setName(field.fValue);
		else
		    System.err.println("Unknown 'extension' attribute: '" + field.fName + "'.");
	    } else {
		// Handle all other elements - create on first reference
		PluginElement elt= (PluginElement) elementMap.get(field.fSchemaElementName);

		if (elt == null) {
		    int lastDotIdx= field.fSchemaElementName.lastIndexOf('.');
		    String elemName= field.fSchemaElementName.substring(lastDotIdx+1);
		    IPluginParent parent;

		    elt= new PluginElement();
		    elt.setModel(pluginModel);
		    elt.setName(elemName);
		    if (lastDotIdx > 0) {
			String parentElemName= field.fSchemaElementName.substring(0, lastDotIdx);

			parent= (IPluginParent) elementMap.get(parentElemName);
		    } else
			parent= extension;
		    elt.setParent(parent);
		    parent.add(elt);
		    elementMap.put(field.fSchemaElementName, elt); // use "fully-qualified" name here
		}
		// Ok, we've found the right element; set the attribute
		elt.setAttribute(field.fName, field.fValue);
	    }
	}
    }

    public static void setElementAttributes(IPluginModel pluginModel, IPluginExtension extension, String[][] attrNamesValues) throws CoreException {
	PluginElement elt= null;

	for(int i= 0; i < attrNamesValues.length; i++) {
	    String attrName= attrNamesValues[i][0];
	    String attrValue= attrNamesValues[i][1];

	    if (elt == null || !attrName.equals(elt.getName())) {
		elt= new PluginElement();
		elt.setModel(pluginModel);
		elt.setName(attrName);
		extension.add(elt);
		elt.setParent(extension);
	    }
	    elt.setAttribute(attrName, attrValue);
	}
    }

    private static void addRequiredPluginImports(IPluginModel pluginModel, ExtensionPointWizardPage page) throws CoreException {
	IPluginBase base= pluginModel.getPluginBase();
	List requires= page.getRequires();
	// RMF Ask the model's associated bundle description for the list
	// of required bundles; I've seen this list be out of sync wrt the
	// list of imports in the IPluginBase (e.g. the latter is empty).
	IPluginImport[] imports= base.getImports();
//	BundleSpecification[] reqBundles= pluginModel.getBundleDescription().getRequiredBundles();
//	IPluginModelFactory pluginFactory= pluginModel.getPluginFactory();
//	/*IPluginImport[] curImports=*/ base.getImports(); // make sure the 'base.imports' field is non-null; otherwise, subsequent calls to base.add() are a noop!

        for(int n= 0; n < requires.size(); n++) {
	    String pluginID= (String) requires.get(n);

		boolean found= false;
		for(int i= 0; i < imports.length; i++) {
		    if (imports[i].getId().equals(pluginID)) {
			found= true;
			break;
		    }
		}
	    if (!found /*!containsImport(reqBundles, pluginID*/) {
		PluginImport importNode= new PluginImport(); // pluginFactory.createImport();
                importNode.setModel(pluginModel);
                importNode.setId(pluginID);
                importNode.setInTheModel(true);
                importNode.setParent(base);
		base.add(importNode);
	    }
	}
    }

    private static boolean containsImport(BundleSpecification[] imports, String pluginID) {
	boolean found= false;
	for(int i= 0; i < imports.length; i++) {
	    if (imports[i].getBundle().getSymbolicName().equals(pluginID)) {
		found= true;
		break;
	    }
	}
	return found;
    }

    static public void addImports(ExtensionPointWizardPage page) {
	try {
	    IPluginModel plugin= getPluginModel(page);

	    if (plugin == null) return;

	    addRequiredPluginImports(plugin, page);

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
