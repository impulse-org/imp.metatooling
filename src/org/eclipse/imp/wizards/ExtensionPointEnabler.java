package org.eclipse.uide.wizards;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginModelFactory;
import org.eclipse.pde.core.plugin.ISharedExtensionsModel;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.bundle.BundleFragmentModel;
import org.eclipse.pde.internal.core.bundle.BundlePluginModel;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundleModel;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModel;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.core.plugin.PluginElement;
import org.eclipse.pde.internal.core.plugin.PluginExtension;
import org.eclipse.pde.internal.core.plugin.WorkspaceExtensionsModel;
import org.eclipse.pde.internal.core.plugin.WorkspaceFragmentModel;
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModel;
import org.eclipse.pde.internal.ui.wizards.PluginSelectionDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.uide.core.ErrorHandler;

/**
 * @author Claffra
 */
public class ExtensionPointEnabler {
    private static class MyModelManager {
	public static boolean hasBundleManifest(IProject project) {
	    return project.exists(new Path("META-INF/MANIFEST.MF")); //$NON-NLS-1$
	}

	public static boolean hasPluginManifest(IProject project) {
	    return project.exists(new Path("plugin.xml")); //$NON-NLS-1$
	}

	public static boolean hasFragmentManifest(IProject project) {
	    return project.exists(new Path("fragment.xml")); //$NON-NLS-1$
	}

	public static boolean hasFeatureManifest(IProject project) {
	    return project.exists(new Path("feature.xml")); //$NON-NLS-1$
	}

	private IPluginModelBase createPluginModel(IProject project) {
	    if (hasBundleManifest(project))
		return createWorkspaceBundleModel(project.getFile("META-INF/MANIFEST.MF")); //$NON-NLS-1$

	    if (hasPluginManifest(project))
		return createWorkspacePluginModel(project.getFile("plugin.xml")); //$NON-NLS-1$

	    return createWorkspaceFragmentModel(project.getFile("fragment.xml")); //$NON-NLS-1$
	}

	private IPluginModelBase createWorkspacePluginModel(IFile file) {
	    if (!file.exists())
		return null;

	    WorkspacePluginModel model= new WorkspacePluginModel(file, true);
	    loadModel(model, false);
	    return model;
	}

	private IPluginModelBase createWorkspaceBundleModel(IFile file) {
	    if (!file.exists())
		return null;

	    WorkspaceBundleModel model= new WorkspaceBundleModel(file);
	    loadModel(model, false);

	    IBundlePluginModelBase bmodel= null;
	    boolean fragment= model.isFragmentModel();
	    if (fragment)
		bmodel= new BundleFragmentModel();
	    else
		bmodel= new BundlePluginModel();
	    bmodel.setEnabled(true);
	    bmodel.setBundleModel(model);

	    IFile efile= file.getProject().getFile(fragment ? "fragment.xml" : "plugin.xml"); //$NON-NLS-1$ //$NON-NLS-2$
	    if (efile.exists()) {
		WorkspaceExtensionsModel extModel= new WorkspaceExtensionsModel(efile);
		loadModel(extModel, false);
		bmodel.setExtensionsModel(extModel);
		extModel.setBundleModel(bmodel);
	    }
	    return bmodel;
	}

	private IPluginModelBase createWorkspaceFragmentModel(IFile file) {
	    if (!file.exists())
		return null;

	    WorkspaceFragmentModel model= new WorkspaceFragmentModel(file, true);
	    loadModel(model, false);
	    return model;
	}

	private void loadModel(IModel model, boolean reload) {
	    IFile file= (IFile) model.getUnderlyingResource();
	    try {
		InputStream stream= file.getContents(true);
		if (reload)
		    model.reload(stream, false);
		else
		    model.load(stream, false);
		stream.close();
	    } catch (CoreException e) {
		PDECore.logException(e);
		return;
	    } catch (IOException e) {
	    }
	}
    }

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

    public static IPluginModel getPlugin(final ExtensionPointWizardPage page) {
	try {
	    final IProject project= page.getProject();
	    if (project == null) return null;
	    PluginModelManager pmm = PDECore.getDefault().getModelManager();
	    IPluginModelBase thePluginModel= pmm.findModel(project);

	    return (IPluginModel) thePluginModel;
	} catch (Exception e) {
	    ErrorHandler.reportError("Could not find plugin for " + page, e);
	}
	return null;
    }

    static void addExtensionPoint(IPluginModel plugin, ExtensionPointWizardPage page) throws CoreException, IOException {
	IPluginExtension extension= plugin.getPluginFactory().createExtension();

	extension.setPoint(page.pluginID + "." + page.pointID);
	setElementAttributes(plugin, page, extension);
	plugin.getExtensions(true).add(extension);

	addRequiredPluginImports(plugin, page);
	saveAndRefresh(plugin);
    }

    private static void setElementAttributes(IPluginModel plugin, ExtensionPointWizardPage page, IPluginExtension extension) throws CoreException {
	PluginElement schema= null;
	List values= page.getValues();
	for(int n= 0; n < values.size(); n++) {
	    ExtensionPointWizardPage.Field field= (ExtensionPointWizardPage.Field) values.get(n);
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

    private static void addRequiredPluginImports(IPluginModel plugin, ExtensionPointWizardPage page) throws CoreException {
	IPluginBase base= plugin.getPluginBase();
	List requires= page.getRequires();
	IPluginImport[] imports= base.getImports();
	IPluginModelFactory pluginFactory= plugin.getPluginFactory();

	for(int n= 0; n < requires.size(); n++) {
	    String pluginID= (String) requires.get(n);

	    if (!containsImport(imports, pluginID)) {
		IPluginImport importNode= pluginFactory.createImport();
		importNode.setId(pluginID);
		base.add(importNode);
	    }
	}
    }

    private static boolean containsImport(IPluginImport[] imports, String pluginID) {
	boolean found= false;
	for(int i= 0; i < imports.length; i++) {
	    IPluginImport imprt= imports[i];

	    if (imprt.getId().equals(pluginID)) {
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
	}
	plugin.getUnderlyingResource().refreshLocal(1, null);
    }
}
