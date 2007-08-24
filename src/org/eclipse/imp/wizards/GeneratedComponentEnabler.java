/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.wizards;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

//import java.io.StringBufferInputStream;
import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModel;
import org.eclipse.pde.internal.core.plugin.PluginImport;
import org.eclipse.imp.core.ErrorHandler;

/**
 * @author ssutton, adapted from ExtensionPointEnabler by claffra
 */
public class GeneratedComponentEnabler
{
	
    public static IPluginModel getPluginModel(final GeneratedComponentWizardPage page) {
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

    // SMS 3 Aug 2006:  ???
    private static final String pluginXMLSkeleton= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<?eclipse version=\"3.0\"?>\n" +
    "<plugin>\n" +
    "</plugin>";

    // SMS 3 Aug 2006:  ???
    private static void maybeCreatePluginXML(final IProject project) throws CoreException {
    	IFile pluginXML= project.getFile("plugin.xml");
    	if (!pluginXML.exists()) {
    		// pluginXML.create(new StringBufferInputStream(pluginXMLSkeleton), false, new NullProgressMonitor());
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
	    // It seems that at least sometimes one of these models
	    // might be a workspace plugin model (as opposed to a project
	    // plugin model), and at least some of those may have no ID
	    // (perhaps for a runtime workbench?).
	    // Anyway, it seems reasonable to skip over any model where
	    // any element of interest is null, since that won't be what
	    // we're looking for in any case
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
    
   

    private static void addRequiredPluginImports(IPluginModel pluginModel, GeneratedComponentWizardPage page) throws CoreException {
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


    static public void addImports(GeneratedComponentWizardPage page) {
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

}
