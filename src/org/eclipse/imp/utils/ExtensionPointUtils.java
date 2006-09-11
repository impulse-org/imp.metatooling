package org.eclipse.uide.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.plugin.PluginElement;

public class ExtensionPointUtils {
    private ExtensionPointUtils() { }

    public static IPluginExtension findExtensionByName(String name, IPluginModelBase pluginModel) {
        IExtensions extensionsThing = pluginModel.getExtensions();
        IPluginExtension[] extensions = extensionsThing.getExtensions();
        IPluginExtension parserExtension = null;
    
        for (int i = 0; i < extensions.length; i++) {
        	if(extensions[i].getPoint().equals(name)) {
        		parserExtension = extensions[i];
        		break;
        	}
        }
        return parserExtension;
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
        	System.err.println("NewCompiler.getParseControllerClassName(IProject):  JavaModelException getting parser package:  " +
        			"\n\t" + e.getMessage() +
        			"\n\tReturning null");
        }
        return null;
    }

    public static PluginElement findElementByName(String name, IPluginExtension parserExtension) {
        IPluginObject[] children = parserExtension.getChildren();
        for (int i = 0; i < children.length; i++) {
            if(children[i].getName().equals(name)) {
        	return (PluginElement) children[i];
            }
        }
        return null;
    }
}
