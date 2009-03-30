package org.eclipse.imp.sanityChecker;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.builder.BuilderBase;
import org.eclipse.imp.runtime.PluginBase;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.imp.wizards.ExtensionPointEnabler;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.bundle.BundlePluginModel;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundleModel;
import org.eclipse.pde.internal.core.ibundle.IBundleModel;
import org.eclipse.pde.internal.core.plugin.ImpPluginElement;

/**
 * A plugin "sanity checker" that checks for necessary conditions on executable extensions
 * that the PDE sadly doesn't check for, e.g., that there's a default constructor, that it's
 * public, etc.
 * @author rfuhrer@watson.ibm.com
 */
public class SanityChecker extends BuilderBase {
    public static final String BUILDER_ID= "org.eclipse.imp.metatooling.sanityChecker";

    private final Set<String> EXTENSION_POINT_IDS= new HashSet<String>();

    {
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.autoEditStrategy");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.annotationHover");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.astAdapter");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.contentProposer");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.documentationProvider");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.editorActionContributions");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.foldingUpdater");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.formatter");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.hoverHelper");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.hyperLink");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.imageDecorator");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.indexContributor");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.labelProvider");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.modelListener");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.modelTreeBuilder");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.markOccurrences");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.outlineContentProvider");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.outliner");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.parser");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.preferencesDialog");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.preferencesSpecification");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.refactoringContributions");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.referenceResolvers");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.syntaxProps");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.tokenColorer");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.viewerFilter");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.editorService");
        EXTENSION_POINT_IDS.add("org.eclipse.imp.runtime.contextHelper");
    };

    private IJavaProject fJavaProject;

    private IFile fPluginManifest;

    private IPluginExtension[] fPluginExtensions;

    public SanityChecker() { }

    @Override
    public String getBuilderID() {
        return BUILDER_ID;
    }

    @Override
    protected void collectDependencies(IFile file) { }

    @Override
    protected PluginBase getPlugin() {
        return RuntimePlugin.getInstance();
    }

    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) {
        IProject project= getProject();
        IFile pluginManifest= project.getFile("plugin.xml");
        try {
            project.deleteMarkers(this.getErrorMarkerID(), true, IResource.DEPTH_INFINITE);
        } catch (CoreException e) {
        }
        compile(pluginManifest, monitor);
        return new IProject[0];
    }

    @Override
    protected void compile(IFile file, IProgressMonitor monitor) {
        fJavaProject= JavaCore.create(getProject());
        fPluginManifest= file;

        if (!readPluginManifest()) {
            return;
        }

        for(int i= 0; i < fPluginExtensions.length; i++) {
            IPluginExtension extension= fPluginExtensions[i];

            if (EXTENSION_POINT_IDS.contains(extension.getPoint())) {
                validateServiceImplClass(extension);
            }
        }
    }

    private boolean readPluginManifest() {
        IPluginModel pluginModel = ExtensionPointEnabler.getPluginModel(getProject());

        if (pluginModel instanceof BundlePluginModel) {
            BundlePluginModel bpm = (BundlePluginModel) pluginModel;
            IBundleModel bm = bpm.getBundleModel();
            if (bm instanceof WorkspaceBundleModel) {
                ((WorkspaceBundleModel)bm).setEditable(true);
            }
        }

        try {
            ExtensionPointEnabler.loadImpExtensionsModel(pluginModel, getProject());
            IExtensions pmExtensions = pluginModel.getExtensions();
            fPluginExtensions= pmExtensions.getExtensions();
        } catch (CoreException e) {
            createMarker(fPluginManifest, 0, 0, 0, e.getLocalizedMessage(), IMarker.SEVERITY_ERROR);
            return false;
        }
        return true;
    }

    private void validateServiceImplClass(IPluginExtension extension) {
        // Check that:
        // 1) the given impl class exists
        // 2) the class is public
        // 3) the class is not abstract
        // 4) the class has a default constructor
        // 5) the default ctor is public
        String pointID= extension.getPoint();
//      String extenID= extension.getId();
        String fullyQualifiedClassName= findImplementationClass(extension);

        if (fullyQualifiedClassName == null) {
            createMarker(fPluginManifest, 1, 0, 0, "Extension for " + pointID + " has no 'class' attribute", IMarker.SEVERITY_ERROR);
            return;
        }

        String simpleClassName= fullyQualifiedClassName.substring(fullyQualifiedClassName.lastIndexOf('.') + 1);

        try {
            IJavaElement javaElt= fJavaProject.findType(fullyQualifiedClassName);

            if (javaElt == null || !javaElt.exists() || !(javaElt instanceof IType)) {
                createMarker(fPluginManifest, 1, 0, 0, "Extension for " + pointID + " refers to non-existent implementation class: " + fullyQualifiedClassName, IMarker.SEVERITY_ERROR);
                return;
            }

            IType javaType= (IType) javaElt;
            IResource javaRes= javaType.getResource();
            ISourceRange typeNameRange= javaType.getNameRange();

            if (!Flags.isPublic(javaType.getFlags())) {
                createMarker(javaRes, 1, typeNameRange.getOffset(), typeNameRange.getOffset() + typeNameRange.getLength(), "Implementation class " + fullyQualifiedClassName + " for extension point " + pointID + " is not public!", IMarker.SEVERITY_ERROR);
            } else if (Flags.isAbstract(javaType.getFlags())) {
                createMarker(javaRes, 1, typeNameRange.getOffset(), typeNameRange.getOffset() + typeNameRange.getLength(), "Implementation class " + fullyQualifiedClassName + " for extension point " + pointID + " is abstract!", IMarker.SEVERITY_ERROR);
            } else if (javaType.isMember() && !Flags.isStatic(javaType.getFlags())) {
                createMarker(javaRes, 1, typeNameRange.getOffset(), typeNameRange.getOffset() + typeNameRange.getLength(), "Implementation class " + fullyQualifiedClassName + " for extension point " + pointID + " is a member type!", IMarker.SEVERITY_ERROR);
            }

            IMethod[] methods= javaType.getMethods();

            boolean defCtorFound= false;
            boolean foundAnyCtors= false;
            for(int i= 0; i < methods.length && !defCtorFound; i++) {
                IMethod method= methods[i];
                if (method.getElementName().equals(simpleClassName)) {
                    foundAnyCtors= true;
                    if (method.getParameterTypes().length == 0) {
                        defCtorFound= true;
                        if (!Flags.isPublic(method.getFlags())) {
                            createMarker(fPluginManifest, 1, 0, 0, "Default constructor for implementation class " + fullyQualifiedClassName + " of extension point " + pointID + " is not public!", IMarker.SEVERITY_ERROR);
                        }
                    }
                }
            }
            if (!defCtorFound && foundAnyCtors) {
                createMarker(fPluginManifest, 1, 0, 0, "Implementation class " + fullyQualifiedClassName + " for extension point " + pointID + " has no default constructor!", IMarker.SEVERITY_ERROR);
            }
        } catch (JavaModelException e) {
            createMarker(fPluginManifest, 1, 0, 0, "Error encountered while examining Java code on behalf of extension point " + pointID + ": " + e.getLocalizedMessage(), IMarker.SEVERITY_ERROR);
        }
    }

    private String findImplementationClass(IPluginExtension extension) {
        IPluginObject[] children= extension.getChildren();
        for(int i= 0; i < children.length; i++) {
            IPluginObject child= children[i];
            if (child instanceof ImpPluginElement) {
                ImpPluginElement ipe= (ImpPluginElement) child;
                for(IPluginAttribute attr: ipe.getAttributes()) {
                    if (attr.getName().equals("class")) {
                        return attr.getValue();
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected String getErrorMarkerID() {
        return "org.eclipse.imp.metatooling.sanity.problem";
    }

    @Override
    protected String getInfoMarkerID() {
        return "org.eclipse.imp.metatooling.sanity.problem";
    }

    @Override
    protected String getWarningMarkerID() {
        return "org.eclipse.imp.metatooling.sanity.problem";
    }

    @Override
    protected boolean isNonRootSourceFile(IFile file) {
        return false;
    }

    @Override
    protected boolean isOutputFolder(IResource resource) {
        return false;
    }

    @Override
    protected boolean isSourceFile(IFile file) {
        return file.getName().equals("plugin.xml");
    }
}
