package org.eclipse.uide.wizards;
/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.plugin.PluginElement;
import org.eclipse.pde.internal.core.plugin.PluginExtension;
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModel;
import org.eclipse.pde.internal.ui.wizards.PluginSelectionDialog;
import org.eclipse.uide.core.ErrorHandler;

/**
 * @author Claffra
 */
public class ExtensionPointEnabler {

	public static void enable(ExtensionPointWizardPage page, IProgressMonitor monitor) {
		try {
            WorkspacePluginModel plugin = getPlugin(page);
			if (plugin != null) {
				addExtensionPoint(plugin, page);
			}
		} catch (Exception e) {
			ErrorHandler.reportError("Could not enable extension point for "+page, e);
		}
	}

	public static WorkspacePluginModel getPlugin(ExtensionPointWizardPage page) {
		try {
			IProject project = page.getProject();
			if (project == null)
				return null;
			PluginModelManager pmm = PDECore.getDefault().getModelManager();
			IPluginModelBase[] plugins = pmm.getAllPlugins();
			for (int n = 0; n < plugins.length; n++) {
				IPluginModelBase plugin = plugins[n];
				IResource resource = plugin.getUnderlyingResource();
				if (resource != null && project.equals(resource.getProject())) {
					if (plugin instanceof WorkspacePluginModel)
						return (WorkspacePluginModel)plugin;
				}
			}
		} catch (Exception e) {
			ErrorHandler.reportError("Could not find plugin for "+page, e);
		}
		return null;
	}

	static void addExtensionPoint(WorkspacePluginModel plugin, ExtensionPointWizardPage page) throws CoreException, IOException {
		PluginExtension extension = new PluginExtension();
		plugin.getPluginFactory().createExtension();
		extension.setModel(plugin);
		extension.setPoint(page.pluginID+"."+page.pointID);
		PluginElement schema = null;
		List values = page.getValues();
		for (int n=0; n<values.size(); n++) {
			ExtensionPointWizardPage.Field field = (ExtensionPointWizardPage.Field)values.get(n);
			if (schema == null || !field.schemaName.equals(schema.getName())) {
				schema = new PluginElement();
				schema.setModel(plugin);
				schema.setName(field.schemaName);
				extension.add(schema);
				schema.setParent(extension);
			}
			schema.setAttribute(field.name, field.value);
		}
		IPluginBase base = plugin.getPluginBase();
		base.add(extension);
		
		List requires = page.getRequires();
		HashSet imports = PluginSelectionDialog.getExistingImports(plugin.getPluginBase());
		for (int n=0; n<requires.size(); n++) {
			String pluginID = (String)requires.get(n);
			if (!imports.contains(pluginID)) {
				IPluginImport importNode = plugin.getPluginFactory().createImport();
				importNode.setId(pluginID);
				base.add(importNode);
			}
		}

		plugin.save();
		plugin.getUnderlyingResource().refreshLocal(1, null);
	}

	static public void addImports(ExtensionPointWizardPage page) {
		try {
			WorkspacePluginModel plugin = getPlugin(page);
			if (plugin != null) {
				IPluginBase base = plugin.getPluginBase();		
				List requires = page.getRequires();
				HashSet imports = PluginSelectionDialog.getExistingImports(plugin.getPluginBase());
				for (int n=0; n<requires.size(); n++) {
					String pluginID = (String)requires.get(n);
					if (!imports.contains(pluginID)) {
						IPluginImport importNode = plugin.getPluginFactory().createImport();
						importNode.setId(pluginID);
						base.add(importNode);
					}
				}
				plugin.save();
				plugin.getUnderlyingResource().refreshLocal(1, null);
			}
		} catch (Exception e) {
			ErrorHandler.reportError("Could not enable extension point for "+page, e);
		}
	}

}
