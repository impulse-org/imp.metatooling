package org.eclipse.uide.wizards;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginModelFactory;
import org.eclipse.pde.core.plugin.ISharedExtensionsModel;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.WorkspaceModelManager;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModel;
import org.eclipse.pde.internal.core.plugin.PluginElement;
import org.eclipse.uide.core.ErrorHandler;

/**
 * @author Claffra
 */
public class ExtensionPointEnabler {
    public static void enable(ExtensionPointWizardPage page, IProgressMonitor monitor) {
	try {
	    IPluginModel plugin= getPlugin(page);
	    if (plugin != null) {
		addExtensionPoint(plugin, page);
	    }
	} catch (Exception e) {
	    ErrorHandler.reportError("Could not enable extension point for " + page, e);
	}
    }

    private static final String pluginXMLSkeleton= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<?eclipse version=\"3.0\"?>\n" +
        "<plugin>\n" +
        "</plugin>";

    public static IPluginModel getPlugin(final ExtensionPointWizardPage page) {
	try {
	    final IProject project= page.getProject();
	    if (project == null) return null;
            IFile pluginXML= project.getFile("plugin.xml"); 
            if (!pluginXML.exists())
                pluginXML.create(new StringBufferInputStream(pluginXMLSkeleton), false, new NullProgressMonitor());
//            WorkspaceModelManager wmm = PDECore.getDefault().getWorkspaceModelManager();
//            IPluginModelBase[] wsPlugins= wmm.getFeatureModel(project).getWorkspaceModels();
            PluginModelManager pmm = PDECore.getDefault().getModelManager();
            IPluginModelBase[] wsPlugins= pmm.getState().getWorkspaceModels();

            if (wsPlugins.length == 0) {
                ErrorHandler.reportError("Project " + page.getProject().getName() + " is not a plugin project?", true);
                return null;
            }
            for(int i= 0; i < wsPlugins.length; i++) {
                IPluginModelBase wsPlugin= wsPlugins[i];
                if (wsPlugin.getBundleDescription().getName().equals(project.getName())) {
                    return (IPluginModel) wsPlugin;
                }
            }
//	    IPluginModelBase thePluginModel= pmm.findModel(project);
//
//	    return (IPluginModel) thePluginModel;
            ErrorHandler.reportError("Could not find plugin for project " + page.getProject().getName(), true);
	} catch (Exception e) {
	    ErrorHandler.reportError("Could not find plugin for project " + page.getProject().getName(), true, e);
	}
	return null;
    }

    static void addExtensionPoint(IPluginModel pluginModel, ExtensionPointWizardPage page) throws CoreException, IOException {
	IPluginExtension extension= pluginModel.getPluginFactory().createExtension();

        if (extension == null) {
            ErrorHandler.reportError("Unable to create extension " + page.fExtPointID + " in plugin " + pluginModel.getBundleDescription().getName(), true);
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

    private static void setElementAttributes(IPluginModel plugin, ExtensionPointWizardPage page, IPluginExtension extension) throws CoreException {
	PluginElement schema= null;
	List fields= page.getFields();

	for(int n= 0; n < fields.size(); n++) {
	    WizardPageField field= (WizardPageField) fields.get(n);

	    if (schema == null || !field.schemaName.equals(schema.getName())) {
		schema= new PluginElement();
		schema.setModel(plugin);
		schema.setName(field.schemaName);
		extension.add(schema);
		schema.setParent(extension);
	    }
	    schema.setAttribute(field.name, field.value);
	}
    }

    private static void addRequiredPluginImports(IPluginModel pluginModel, ExtensionPointWizardPage page) throws CoreException {
	IPluginBase base= pluginModel.getPluginBase();
	List requires= page.getRequires();
	// RMF Ask the model's associated bundle description for the list
	// of required bundles; I've seen this list be out of sync wrt the
	// list of imports in the IPluginBase (e.g. the latter is empty).
//	IPluginImport[] imports= base.getImports();
	BundleDescription[] reqBundles= pluginModel.getBundleDescription().getResolvedRequires();
	IPluginModelFactory pluginFactory= pluginModel.getPluginFactory();

	for(int n= 0; n < requires.size(); n++) {
	    String pluginID= (String) requires.get(n);

	    if (!containsImport(reqBundles /* imports */, pluginID)) {
		IPluginImport importNode= pluginFactory.createImport();
		importNode.setId(pluginID);
		base.add(importNode);
	    }
	}
    }

    private static boolean containsImport(BundleDescription/*IPluginImport*/[] imports, String pluginID) {
	boolean found= false;
	for(int i= 0; i < imports.length; i++) {
	    if (imports[i].getSymbolicName().equals(pluginID)) {
		found= true;
		break;
	    }
	}
	return found;
    }

    static public void addImports(ExtensionPointWizardPage page) {
	try {
	    IPluginModel plugin= getPlugin(page);

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
