package org.eclipse.uide.wizards;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.search.JavaWorkspaceScope;
import org.eclipse.jdt.internal.ui.dialogs.TypeSelectionDialog2;
import org.eclipse.jdt.internal.ui.wizards.NewClassCreationWizard;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.ischema.ISchemaAttribute;
import org.eclipse.pde.internal.core.ischema.ISchemaElement;
import org.eclipse.pde.internal.core.schema.Schema;
import org.eclipse.pde.internal.ui.util.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.uide.core.ErrorHandler;
import org.osgi.framework.Bundle;

/**
 * The "New" wizard page allows setting the container for the new file as well as the file name. The page will only
 * accept file name without the extension OR with the extension that matches the expected one (g).
 */
public class ExtensionPointWizardPage extends WizardPage {
    protected Text projectText;

    protected String pluginID;

    protected String pointID;

    protected Schema schema;

    protected boolean done= true;

    protected Text descriptionText;

    protected Text languageText;

    protected String fPackageName;

    protected List fields= new ArrayList(); // a list of Field instances

    protected Button addThisExtensionPointButton;

    protected boolean skip= false;

    protected boolean isOptional;

    protected ExtensionPointWizard owner;

    protected int pageNumber;

    // shared between wizard pages
    protected static String language= "";

    protected static String projectName= "";

    protected List requires= new ArrayList();

    protected int totalPages;

    public boolean canFlipToNextPage() {
        return super.canFlipToNextPage();
    }

    public ExtensionPointWizardPage(ExtensionPointWizard owner, String pluginID, String pointID) {
        this(owner, 0, 1, pluginID, pointID, false);
    }

    public ExtensionPointWizardPage(ExtensionPointWizard owner, int pageNumber, int totalPages, String pluginID, String pointID, boolean isOptional) {
        super("wizardPage");
        this.pluginID= pluginID;
        this.pointID= pointID;
        this.isOptional= isOptional;
        this.pageNumber= pageNumber;
        this.totalPages= totalPages;
        this.owner= owner;
        try {
            IExtensionPoint ep= (IExtensionPoint) Platform.getExtensionRegistry().getExtensionPoint(pluginID, pointID);
            String schemaLoc;

            if (ep.getUniqueIdentifier().startsWith("org.eclipse.") && !ep.getUniqueIdentifier().startsWith("org.eclipse.uide")) {
                // RMF 1/5/2006 - Hack to get schema for extension points defined by Eclipse
                // platform plugins: attempts to find them in org.eclipse.platform.source.
                Bundle platSrcPlugin= Platform.getBundle("org.eclipse.platform.source");
                Bundle extProviderPlugin= Platform.getBundle(ep.getNamespace());
                String extPluginVersion= (String) extProviderPlugin.getHeaders().get("Bundle-Version");
                Path schemaPath= new Path("src/" + ep.getNamespace() + "_" + extPluginVersion + "/" + ep.getSchemaReference());
                URL schemaURL= Platform.find(platSrcPlugin, schemaPath);
                URL localSchemaURL= Platform.asLocalURL(schemaURL);

                schemaLoc= localSchemaURL.getPath();
            } else {
                Bundle core= Platform.getBundle(pluginID);
                String location= core.getLocation();
                schemaLoc= location.substring(location.indexOf('@') + 1) + ep.getSchemaReference();

            }
            schema= new Schema(pluginID, pointID, "", false);
            schema.load(new FileInputStream(schemaLoc));
            setDescription(schema.getDescription());
        } catch (Exception e) {
            ErrorHandler.reportError("Cannot create wizard page for " + pluginID + "." + pointID, e);
            setTitle("Extension point: " + pluginID + "." + pointID);
            setDescription("Cannot create wizard page: " + e);
        }
    }

    protected void createAdditionalControls(Composite parent) {
        // Noop here; optionally overridden in derived classes
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        try {
            Composite container= new Composite(parent, SWT.NULL | SWT.BORDER);
            // container.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

            GridLayout layout= new GridLayout();
            container.setLayout(layout);
            layout.numColumns= 3;
            layout.verticalSpacing= 9;
            if (totalPages > 1 && isOptional) {
                Label label= new Label(container, SWT.NONE);
                label.setText("Add this service:");
                // label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
                addThisExtensionPointButton= new Button(container, SWT.CHECK);
                addThisExtensionPointButton.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        skip= !addThisExtensionPointButton.getSelection();
                        dialogChanged();
                    }
                });
                // addThisExtensionPointButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
                addThisExtensionPointButton.setSelection(true);
                GridData gd= new GridData(GridData.FILL_HORIZONTAL);
                gd.horizontalSpan= 2;
                addThisExtensionPointButton.setLayoutData(gd);
            }
            createProjectLabelText(container);
            try {
                ISchemaElement[] elements= schema.getElements();
                for(int n= 0; n < elements.length; n++) {
                    ISchemaElement element= elements[n];
                    if (element.getName().equals("extension"))
                        continue;
                    ISchemaAttribute[] attributes= element.getAttributes();
                    for(int k= 0; k < attributes.length; k++) {
                        ISchemaAttribute attribute= attributes[k];
                        createSchemaAttributeTextField(container, element.getName(), attribute);
                    }
                }
                createAdditionalControls(container);
                descriptionText= new Text(container, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
                descriptionText.setBackground(container.getBackground());
                GridData gd= new GridData(GridData.FILL_BOTH);
                gd.horizontalSpan= 3;
                gd.widthHint= 450;
                descriptionText.setLayoutData(gd);
                descriptionText.setEditable(false);
                descriptionText.setText(schema.getDescription());
            } catch (Exception e) {
                new Label(container, SWT.NULL).setText("Could not create wizard page");
                ErrorHandler.reportError("Could not create wizard page", e);
            }
            dialogChanged();
            setControl(container);
            projectText.setFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSchemaAttributeTextField(Composite container, String schemaName, ISchemaAttribute attribute) {
        String name= attribute.getName();
        int kind= attribute.getKind();
        String basedOn= attribute.getBasedOn();
        String description= stripHTML(attribute.getDescription());
        Object valueObject= attribute.getValue();
        String value= valueObject == null ? "" : valueObject.toString();
        boolean required= attribute.getUse() == ISchemaAttribute.REQUIRED;
        String upName= Character.toUpperCase(name.charAt(0)) + name.substring(1);
        WizardPageField field= new WizardPageField(schemaName, name, upName, value, kind, required, description);
        Text text= createLabelTextBrowse(container, upName, description, basedOn, value, required, field);
        if (name.equals("language"))
            languageText= text;
        text.setData(field);
        fields.add(field);
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                Text text= (Text) e.widget;
                WizardPageField field= (WizardPageField) text.getData();
                field.value= text.getText();
                if (field.name.equals("language")) {
                    language= field.value;
                }
                dialogChanged();
            }
        });
        text.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                Text text= (Text) e.widget;
                WizardPageField field= (WizardPageField) text.getData();
                descriptionText.setText(field.description);
            }
        });
    }

    private void createProjectLabelText(Composite container) {
        Label label= new Label(container, SWT.NULL);
        label.setText("Project*:");
        label.setBackground(container.getBackground());
        label.setToolTipText("Select the plug-in project");
        projectText= new Text(container, SWT.BORDER | SWT.SINGLE);
        projectText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        IProject project= getProject();
        if (project != null)
            projectText.setText(project.getName());
        discoverSelectedProject();
        Button button= new Button(container, SWT.PUSH);
        button.setText("Browse...");
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ContainerSelectionDialog dialog= new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
                        "Select a plug-in Project");
                dialog.setValidator(new ISelectionValidator() {
                    public String isValid(Object selection) {
                        try {
                            IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(selection.toString());
                            if (project.exists() && project.hasNature("org.eclipse.pde.PluginNature")) {
                                return null;
                            }
                        } catch (Exception e) {
                        }
                        return "The selected element \"" + selection + "\" is not a plug-in project";
                    }
                });
                if (dialog.open() == ContainerSelectionDialog.OK) {
                    Object[] result= dialog.getResult();
                    IProject selectedProject= ResourcesPlugin.getWorkspace().getRoot().getProject(result[0].toString());
                    if (result.length == 1) {
                        // projectText.setText(((Path) result[0]).toOSString());
                        projectText.setText(selectedProject.getName());
                    }
                }
            }

        });
        projectText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                Text text= (Text) e.widget;
                setProjectName(text.getText());
                ExtensionPointEnabler.addImports(ExtensionPointWizardPage.this);
                dialogChanged();
            }
        });
        projectText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                // Text text = (Text)e.widget;
                descriptionText.setText("Select the plug-in project to add this extension point to");
            }
        });
    }

    private void discoverSelectedProject() {
        ISelectionService service= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
        ISelection selection= service.getSelection();
        IProject project= getProject(selection);
        if (project != null) {
            projectName= project.getName();
            projectText.setText(projectName);
        }
    }

    private IProject getProject(ISelection selection) {
        if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
            IStructuredSelection ssel= (IStructuredSelection) selection;
            if (ssel.size() > 1)
                return null;
            Object obj= ssel.getFirstElement();
            if (obj instanceof IPackageFragmentRoot)
                obj= ((IPackageFragmentRoot) obj).getResource();
            if (obj instanceof IResource) {
                return ((IResource) obj).getProject();
            }
            if (obj instanceof JavaProject) {
                return ((JavaProject) obj).getProject();
            }
        }
        return null;
    }

    private void setProjectName(String newProjectName) {
        if (newProjectName.startsWith("P\\"))
            projectName= newProjectName.substring(1);
        else if (newProjectName.startsWith("\\"))
            projectName= newProjectName;
        else
            projectName= "\\" + newProjectName;
    }

    public IProject getProject() {
        try {
            IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            if (project.exists())
                return project;
        } catch (Exception e) {
        }
        return null;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            setTitle(schema.getName() + " (Step " + (pageNumber + 1) + " of " + owner.getPageCount() + ")");
            owner.setPage(pageNumber);
            if (languageText != null) {
                languageText.setText(language);
            }
            if (pageNumber > 0 && projectText.getCharCount() == 0) {
                projectText.setText(projectName);
            }
        }
        super.setVisible(visible);
        dialogChanged();
    }

    public boolean hasBeenSkipped() {
        return skip;
    }

    String stripHTML(String description) {
        StringBuffer buffer= new StringBuffer(description);
        replace(buffer, "<p>", "");
        replace(buffer, "<ul>", "");
        replace(buffer, "</ul>", "");
        replace(buffer, "<li>", " *  ");
        return buffer.toString();
    }

    void replace(StringBuffer buffer, String s1, String s2) {
        int index= buffer.indexOf(s1);
        while (index != -1) {
            buffer.replace(index, index + s1.length(), s2);
            index= buffer.indexOf(s1);
        }
    }

    protected Text createLabelTextBrowse(Composite container, String name, String description, final String basedOn, String value, boolean required,
            WizardPageField field) {
        Widget labelWidget= null;
        if (required)
            name+= "*";
        name+= ":";
        if (basedOn != null) {
            labelWidget= createNewClassHyperlink(name, description, basedOn, field, container);
        } else {
            Label label= new Label(container, SWT.NULL);
            label.setText(name);
            label.setToolTipText(description);
            labelWidget= label;
            label.setBackground(container.getBackground());
        }
        Text text= new Text(container, SWT.BORDER | SWT.SINGLE);
        labelWidget.setData(text);
        GridData gd= new GridData(GridData.FILL_HORIZONTAL);
        if (basedOn == null)
            gd.horizontalSpan= 2;
        else
            gd.horizontalSpan= 1;
        text.setLayoutData(gd);
        text.setText(value);
        if (basedOn != null)
            createClassBrowseButton(container, field, text);
        if (field != null)
            field.text= text;
        return text;
    }

    private Widget createNewClassHyperlink(String name, String description, final String basedOn, WizardPageField field, Composite container) {
        Widget labelWidget;
        FormToolkit toolkit= new FormToolkit(Display.getDefault());
        Hyperlink link= toolkit.createHyperlink(container, name, SWT.NULL);
        link.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent e) {
                Text text= (Text) e.widget.getData();
                try {
                    if (getProject() == null)
                        MessageDialog.openError(null, "Universal IDE Wizard", "Please select a project first");
                    else {
                        ExtensionPointEnabler.addImports(ExtensionPointWizardPage.this);
                        String basedOnQualName= basedOn;
                        String basedOnTypeName= basedOn.substring(basedOnQualName.lastIndexOf('.') + 1);
                        String superClassName= "";
                        if (basedOnTypeName.charAt(0) == 'I' && Character.isUpperCase(basedOnTypeName.charAt(1))) {
                            superClassName= "org.eclipse.uide.defaults.Default" + basedOnTypeName.substring(1);
                        }
                        openClassDialog(basedOnQualName, superClassName, text);
                    }
                } catch (Exception ee) {
                    ErrorHandler.reportError("Could not open dialog to find type", true, ee);
                }
            }
        });
        link.setToolTipText(description);
        labelWidget= link;
        if (field != null)
            field.link= link;
        return labelWidget;
    }

    private void createClassBrowseButton(Composite container, WizardPageField field, Text text) {
        Button button= new Button(container, SWT.PUSH);
        button.setText("Browse...");
        button.setData(text);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    IRunnableContext context= PlatformUI.getWorkbench().getProgressService();
                    IJavaSearchScope scope= new JavaWorkspaceScope();
                    TypeSelectionDialog2 dialog= new TypeSelectionDialog2(null, false, context, scope, IJavaSearchConstants.CLASS);
                    if (dialog.open() == TypeSelectionDialog2.OK) {
                        Text text= (Text) e.widget.getData();
                        BinaryType type= (BinaryType) dialog.getFirstResult();
                        text.setText(type.getFullyQualifiedName());
                    }
                } catch (Exception ee) {
                    ErrorHandler.reportError("Could not browse type", ee);
                }
            }
        });
        if (field != null)
            field.button= button;
    }

    protected void openClassDialog(String interfaceQualName, String superClassName, Text text) {
        try {
            String intfName= interfaceQualName.substring(interfaceQualName.lastIndexOf('.') + 1);
            IJavaProject javaProject= JavaCore.create(getProject());
            // RMF 7/5/2005 - If the project doesn't yet have the necessary plug-in dependency
            // on org.eclipse.uide.runtime for this reference to be satisfiable, an error ensues.
            // I don't see any code that would augment the project's build-path appropriately.
            // Even if there were, it would have to be undoable if the user cancelled the wizard.
            IType basedOnClass= javaProject.findType(interfaceQualName);
            if (basedOnClass == null) {
                // IClasspathEntry[] cp= javaProject.getRawClasspath();
                // List/*<IClasspathEntry>*/ newCP= new ArrayList();
                // for(int i= 0; i < cp.length; i++) {
                //    if (cp[i] != null) // sometimes there are null entries in the classpath array???
                //       newCP.add(cp[i]);
                // }
                // newCP.add(JavaCore.newContainerEntry(new Path("org.eclipse.uide")));
                // javaProject.setRawClasspath((IClasspathEntry[])
                //     newCP.toArray(new IClasspathEntry[newCP.size()]), new NullProgressMonitor());
                ErrorHandler.reportError("Base interface '" + interfaceQualName
                        + "' does not exist in project's build path; be sure to add org.eclipse.uide.runtime to the plug-in dependecies.", true);
                // return;
            }
            NewClassCreationWizard wizard= new NewClassCreationWizard();
            wizard.init(Workbench.getInstance(), null);
            WizardDialog dialog= new WizardDialog(null, wizard);
            dialog.create();
            NewClassWizardPage page= (NewClassWizardPage) wizard.getPages()[0];
            String langName= languageText.getText();
            String langPkg= Character.toLowerCase(langName.charAt(0)) + langName.substring(1);
            page.setSuperClass(superClassName, true);
            ArrayList interfaces= new ArrayList();
            interfaces.add(interfaceQualName);
            page.setSuperInterfaces(interfaces, true);
            IFolder srcFolder= getProject().getFolder("src/");
            String servicePackage= langPkg + ".safari." + pointID.substring(pointID.lastIndexOf('.')+1); // pkg the service belongs in
            String[] pkgNames= servicePackage.split("\\.");
            IFolder folder= srcFolder;
            for(int i= 0; i < pkgNames.length; i++) {
                IFolder subFolder= folder.getFolder(pkgNames[i]);
                if (!subFolder.exists()) subFolder.create(true, true, new NullProgressMonitor());
                folder= subFolder;
            }
            IPackageFragmentRoot pkgFragRoot= javaProject.getPackageFragmentRoot(srcFolder);
            IPackageFragment pkgFrag= pkgFragRoot.getPackageFragment(servicePackage);
            page.setPackageFragmentRoot(pkgFragRoot, true);
            page.setPackageFragment(pkgFrag, true);

            String langClass= Character.toUpperCase(langName.charAt(0)) + langName.substring(1);
            if (intfName.charAt(0) == 'I' && Character.isUpperCase(intfName.charAt(1)))
                page.setTypeName(langClass + intfName.substring(1), true);
            else
                page.setTypeName(langClass + intfName, true);
            SWTUtil.setDialogSize(dialog, 400, 500);
            if (dialog.open() == WizardDialog.OK) {
                String name= page.getTypeName();
                String pkg= page.getPackageText();
                if (pkg.length() > 0)
                    name= pkg + '.' + name;
                text.setText(name);
                fPackageName= pkg;
            }
        } catch (Exception e) {
            ErrorHandler.reportError("Could not create type for " + superClassName, true, e);
        }
    }

    IPluginModelBase getPluginModel() {
        try {
            PluginModelManager pmm= PDECore.getDefault().getModelManager();
            IPluginModelBase[] plugins= pmm.getAllPlugins();
            IProject project= getProject();
            for(int n= 0; n < plugins.length; n++) {
                IPluginModelBase plugin= plugins[n];
                IResource resource= plugin.getUnderlyingResource();
                if (resource != null && project.equals(resource.getProject())) {
                    return plugin;
                }
            }
        } catch (Exception e) {
            ErrorHandler.reportError("Could not enable extension point for " + getProject(), e);
        }
        return null;
    }

    void dialogChanged() {
        setErrorMessage(null);
        if (skip)
            setPageComplete(true);
        else {
            IProject project= getProject();
            if (project == null) {
                setErrorMessage("Please select a plug-in project to add this extension point to");
                setPageComplete(false);
                return;
            }
            boolean isPlugin= false;
            try {
                isPlugin= project.hasNature("org.eclipse.pde.PluginNature");
            } catch (CoreException e) {
            }
            if (!isPlugin) {
                setErrorMessage("\"" + projectName + "\" is not a plug-in project. Please select a plug-in project to add this extension point to");
                setPageComplete(false);
                return;
            }
            WizardPageField field= getUncompletedField();
            if (field != null) {
                setErrorMessage("Please provide a value for the required attribute \"" + field.label + "\"");
                setPageComplete(false);
                return;
            }
            setPageComplete(true);
        }
    }

    WizardPageField getUncompletedField() {
        for(int n= 0; n < fields.size(); n++) {
            WizardPageField field= (WizardPageField) fields.get(n);
            if (field.required && field.value.length() == 0) {
                return field;
            }
        }
        return null;
    }

    public List getValues() {
        return fields;
    }

    public String getValue(String name) {
        for(int n= 0; n < fields.size(); n++) {
            WizardPageField field= (WizardPageField) fields.get(n);
            if (field.name.toLowerCase().equals(name)) {
                return field.value;
            }
        }
        return "No such field: " + name;
    }

    public WizardPageField getField(String name) {
        for(int n= 0; n < fields.size(); n++) {
            WizardPageField field= (WizardPageField) fields.get(n);
            if (field.name.toLowerCase().equals(name)) {
                return field;
            }
        }
        return null;
    }

    public List getRequires() {
        return requires;
    }
}
