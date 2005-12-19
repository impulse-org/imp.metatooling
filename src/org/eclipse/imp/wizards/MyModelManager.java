/*
 * Created on Oct 10, 2005
 */
package org.eclipse.uide.wizards;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.bundle.BundleFragmentModel;
import org.eclipse.pde.internal.core.bundle.BundlePluginModel;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundleModel;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.core.plugin.WorkspaceExtensionsModel;
import org.eclipse.pde.internal.core.plugin.WorkspaceFragmentModel;
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModel;

class MyModelManager {
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

    IPluginModelBase createPluginModel(IProject project) {
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