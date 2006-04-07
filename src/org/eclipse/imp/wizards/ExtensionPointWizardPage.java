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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.ischema.ISchema;
import org.eclipse.pde.internal.core.ischema.ISchemaAttribute;
import org.eclipse.pde.internal.core.ischema.ISchemaComplexType;
import org.eclipse.pde.internal.core.ischema.ISchemaCompositor;
import org.eclipse.pde.internal.core.ischema.ISchemaElement;
import org.eclipse.pde.internal.core.ischema.ISchemaObject;
import org.eclipse.pde.internal.core.ischema.ISchemaType;
import org.eclipse.pde.internal.core.schema.Schema;
import org.eclipse.pde.internal.core.schema.SchemaComplexType;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
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
    private final class FocusDescriptionListener extends FocusAdapter {
	public void focusGained(FocusEvent e) {
	    Text text= (Text) e.widget;
	    WizardPageField field= (WizardPageField) text.getData();
	    fDescriptionText.setText(field.fDescription);
	}
    }

    private final class ProjectTextModifyListener implements ModifyListener {
	public void modifyText(ModifyEvent e) {
	    Text text= (Text) e.widget;

	    setProjectName(text.getText());
	    discoverProjectLanguage();
	    // RMF Don't add imports yet; wait for user to press "Finish"
	    // ExtensionPointEnabler.addImports(ExtensionPointWizardPage.this);
	    dialogChanged();
	}
    }

    private final class ProjectBrowseSelectionListener extends SelectionAdapter {
	private final IProject project;

	private ProjectBrowseSelectionListener(IProject project) {
	    super();
	    this.project= project;
	}

	public void widgetSelected(SelectionEvent e) {
	    ContainerSelectionDialog dialog= new ContainerSelectionDialog(getShell(), project, false,
	            "Select a plug-in Project");
	    // RMF Would have thought the following would set the initial selection,
	    // but passing project as the initialRoot arg above seems to work...
	    if (project != null)
	        dialog.setInitialSelections(new Object[] { project.getFullPath() });
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
	            // fProjectText.setText(((Path) result[0]).toOSString());
	            fProjectText.setText(selectedProject.getName());
	            sProjectName= selectedProject.getName();
	        }
	    }
	}
    }

    protected String fExtPluginID;

    protected String fExtPointID;

    protected Schema fSchema;

    protected ExtensionPointWizard fOwningWizard;

    protected int fThisPageNumber;

    protected int fTotalPages;

    protected boolean fSkip= false;

    protected boolean fIsOptional;

    protected List/*<WizardPageField>*/ fFields= new ArrayList();

    protected Text fProjectText;

    protected Text fDescriptionText;

    protected Text fLanguageText;

    protected Text fQualClassText;

    protected Button fAddThisExtensionPointButton;

    protected boolean fOmitExtensionIDName= true;

    protected String fPackageName;

    protected List fRequiredPlugins= new ArrayList();

    protected boolean fDone= true;

    // shared between wizard pages
    protected static String sLanguage= "";

    protected static String sProjectName= "";

    public boolean canFlipToNextPage() {
        return super.canFlipToNextPage();
    }

    public ExtensionPointWizardPage(ExtensionPointWizard owner, String pluginID, String pointID) {
	this(owner, pluginID, pointID, true);
    }

    public ExtensionPointWizardPage(ExtensionPointWizard owner, String pluginID, String pointID, boolean omitIDName) {
        this(owner, 0, 1, pluginID, pointID, false);
	fOmitExtensionIDName= omitIDName;
    }

    public ExtensionPointWizardPage(ExtensionPointWizard owner, int pageNumber, int totalPages, String pluginID, String pointID, boolean isOptional) {
        super("wizardPage");
        this.fExtPluginID= pluginID;
        this.fExtPointID= pointID;
        this.fIsOptional= isOptional;
        this.fThisPageNumber= pageNumber;
        this.fTotalPages= totalPages;
        this.fOwningWizard= owner;
        try {
            IExtensionPoint ep= (IExtensionPoint) Platform.getExtensionRegistry().getExtensionPoint(pluginID, pointID);
            String schemaLoc;
            URL localSchemaURL;

            if (ep == null)
        	throw new IllegalArgumentException("Unknown extension point: " + pluginID + "." + pointID);

            if (ep.getUniqueIdentifier().startsWith("org.eclipse.") && !ep.getUniqueIdentifier().startsWith("org.eclipse.uide")) {
                // RMF 1/5/2006 - Hack to get schema for extension points defined by Eclipse
                // platform plugins: attempts to find them in org.eclipse.platform.source,
        	// or, failing that, org.eclipse.rcp.source
                URL schemaURL= locateSchema(ep, "org.eclipse.platform.source");

                if (schemaURL == null)
                    schemaURL= locateSchema(ep, "org.eclipse.rcp.source");
                if (schemaURL == null)
                    throw new Exception("Cannot find schema source for " + ep.getSchemaReference());

                localSchemaURL= Platform.asLocalURL(schemaURL);
                schemaLoc= localSchemaURL.getPath();
            } else {
                Bundle core= Platform.getBundle(pluginID);

                localSchemaURL= Platform.asLocalURL(Platform.find(core, new Path("schema/" + ep.getSimpleIdentifier() + ".exsd")));
                schemaLoc= localSchemaURL.getPath();
            }
            fSchema= new Schema(pluginID, pointID, "", false);
            fSchema.load(new FileInputStream(schemaLoc));
            setDescription(fSchema.getDescription());
        } catch (Exception e) {
            ErrorHandler.reportError("Cannot create wizard page for " + pluginID + "." + pointID, e);
            setTitle("Extension point: " + pluginID + "." + pointID);
            setDescription("Cannot create wizard page: " + e);
        }
    }

    private URL locateSchema(IExtensionPoint ep, String srcBundle) {
	Bundle platSrcPlugin= Platform.getBundle(srcBundle);
	Bundle extProviderPlugin= Platform.getBundle(ep.getNamespace());
	String extPluginVersion= (String) extProviderPlugin.getHeaders().get("Bundle-Version");
	Path schemaPath= new Path("src/" + ep.getNamespace() + "_" + extPluginVersion + "/" + ep.getSchemaReference());
	URL schemaURL= Platform.find(platSrcPlugin, schemaPath);

	return schemaURL;
    }

    /**
     * Creates controls that are to appear above the schema attributes
     * on the wizard page. Derived classes may override.
     * @param parent
     */
    protected void createFirstControls(Composite parent) {
	// Attempt to locate the "language" element attribute, so that we can
	// create that field first, since it's got a listener that populates
	// several other fields with reasonable values based on the language
	// name. As a result, it's nicer to have the language field near the
	// top, so that it's easier to get at than if it were at the bottom.
	ISchemaElement elt= fSchema.findElement(fExtPointID);

	if (elt == null) {
	    ISchemaElement[] elements= fSchema.getElements();
	    for(int i= 0; i < elements.length; i++) {
		if (elements[i].getAttribute("language") != null) {
		    elt= elements[i];
		    break;
		}
	    }
	}
	if (elt != null)
	    createElementAttributeTextField(parent, "extension." + fExtPointID, elt.getAttribute("language"));
    }

    /**
     * Creates additional controls that are to appear below the schema
     * attributes on the wizard page. Derived classes may override.
     * @param parent
     */
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
            if (fTotalPages > 1 && fIsOptional) {
                addServiceEnablerCheckbox(container);
            }
            createProjectLabelText(container);
            try {
        	createFirstControls(container);
        	createControlsForSchema(fSchema, container);
                createAdditionalControls(container);
                createDescriptionText(container);
                discoverProjectLanguage();
                addLanguageListener();
            } catch (Exception e) {
                new Label(container, SWT.NULL).setText("Could not create wizard page");
                ErrorHandler.reportError("Could not create wizard page", e);
            }
            dialogChanged();
            setControl(container);
            fProjectText.setFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createControlsForSchema(ISchema schema, Composite container) {
	createControlsForSchemaElement(schema.findElement("extension"), schema, "extension", container);
//	for(Iterator iter= fFields.iterator(); iter.hasNext(); ) {
//	    WizardPageField field= (WizardPageField) iter.next();
//	    System.out.println(field);
//	}
    }

    private void createControlsForSchemaElement(ISchemaElement element, ISchema schema, String prefix, Composite container) {
	ISchemaType eltType= element.getType();

	createControlsForElementAttributes(element, prefix, container);

	if (eltType instanceof ISchemaComplexType) {
	    SchemaComplexType complexType= (SchemaComplexType) eltType;
	    ISchemaCompositor comp= complexType.getCompositor();

	    if (comp != null) {
		ISchemaObject[] children= comp.getChildren();

		for(int i= 0; i < children.length; i++) {
		    ISchemaElement child= (ISchemaElement) children[i];
		    String subPrefix= ((prefix.length() > 0) ? (prefix + ".") : prefix) + child.getName();

		    createControlsForSchemaElement(schema.findElement(child.getName()), schema, subPrefix, container);
		}
	    }
	}
    }

    private void createControlsForElementAttributes(ISchemaElement element, String prefix, Composite container) {
	ISchemaAttribute[] attributes= element.getAttributes();

	for(int k= 0; k < attributes.length; k++) {
	    ISchemaAttribute attribute= attributes[k];

	    if (attribute.getName().equals("point"))
		continue;

	    if (fOmitExtensionIDName && element.getName().equals("extension") &&
		    (attribute.getName().equals("id") || attribute.getName().equals("name")))
		continue;

	    createElementAttributeTextField(container, prefix, attribute);
	}
    }

    private void createElementAttributeTextField(Composite container, String schemaElementPrefix, ISchemaAttribute attribute) {
        String name= attribute.getName();

        // We manually create the language field first, for the user's
        // convenience, so check to see whether we already created it.
        if (name.equals("language") && fLanguageText != null)
            return;

        String basedOn= attribute.getBasedOn();
        String description= stripHTML(attribute.getDescription());
        Object value= attribute.getValue();
        String valueStr= (value == null) ? "" : value.toString();
        boolean isRequired= (attribute.getUse() == ISchemaAttribute.REQUIRED);
        String upName= upperCaseFirst(name);

        WizardPageField field= new WizardPageField(schemaElementPrefix, name, upName, valueStr, attribute.getKind(), isRequired, description);
        Text text= createLabelTextBrowse(container, field, basedOn);

        if (name.equals("language"))
            fLanguageText= text;
        else if (name.equals("class"))
            fQualClassText= text;

        text.setData(field);
        fFields.add(field);
    }

    protected Text createLabelTextBrowse(Composite container, WizardPageField field, final String basedOn) {
        Widget labelWidget= null;
        String name= field.fAttributeName;
        String description= field.fDescription;
        String value= field.fValue;
	// BUG Prevents clicking "Finish" if an element is optional but one of its attributes isn't
        boolean required= field.fRequired;

        if (required)
            name+= "*";
        name+= ":";
        if (basedOn != null) {
            labelWidget= createNewClassHyperlink(field, name, basedOn, container);
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
            field.fText= text;

        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                Text text= (Text) e.widget;
                WizardPageField field= (WizardPageField) text.getData();
                field.fValue= text.getText();
                if (field.fAttributeName.equals("language")) {
                    sLanguage= field.fValue;
                }
                dialogChanged();
            }
        });
        text.addFocusListener(new FocusDescriptionListener());

        return text;
    }

    private Widget createNewClassHyperlink(WizardPageField field, String name, final String basedOn, Composite container) {
        Widget labelWidget;
        FormToolkit toolkit= new FormToolkit(Display.getDefault());
        Hyperlink link= toolkit.createHyperlink(container, name, SWT.NULL);

        link.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent e) {
                Text text= (Text) e.widget.getData();
                try {
                    if (getProject() == null)
                        MessageDialog.openError(null, "SAFARI Wizard", "Please select a project first");
                    else {
                	// BUG Should pick up info from wizard page, rather than using defaults.
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
        link.setToolTipText(field.fDescription);
        labelWidget= link;
        if (field != null)
            field.fLink= link;
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
            field.fButton= button;
    }

    protected void openClassDialog(String interfaceQualName, String superClassName, Text text) {
        try {
            String intfName= interfaceQualName.substring(interfaceQualName.lastIndexOf('.') + 1);
            IJavaProject javaProject= JavaCore.create(getProject());
            // RMF 7/5/2005 - If the project doesn't yet have the necessary plug-in
            // dependency for this reference to be satisfiable, an error ensues.
            IType basedOnClass= javaProject.findType(interfaceQualName);

            if (basedOnClass == null) {
                ErrorHandler.reportError("Base interface '" + interfaceQualName
                        + "' does not exist in project's build path; be sure to add the appropriate plugin to the dependencies.", true);
            }

            NewClassCreationWizard wizard= new NewClassCreationWizard();

            wizard.init(Workbench.getInstance(), null);

            WizardDialog dialog= new WizardDialog(null, wizard);

            dialog.create();

            NewClassWizardPage page= (NewClassWizardPage) wizard.getPages()[0];
            String langName= fLanguageText.getText();
            // TODO RMF Should either fix and use fPackageName (sometimes null at this point) or get rid of it altogether.
            String langPkg= fQualClassText.getText().substring(0, fQualClassText.getText().indexOf('.'));// fPackageName; // Character.toLowerCase(langName.charAt(0)) + langName.substring(1);

            page.setSuperClass(superClassName, true);

            ArrayList interfaces= new ArrayList();

            interfaces.add(interfaceQualName);
            page.setSuperInterfaces(interfaces, true);

            IFolder srcFolder= getProject().getFolder("src/");
            String servicePackage= langPkg + ".safari." + fExtPointID.substring(fExtPointID.lastIndexOf('.')+1); // pkg the service belongs in

            fOwningWizard.createSubFolders(servicePackage.replace('.', '\\'), getProject(), new NullProgressMonitor());

            IPackageFragmentRoot pkgFragRoot= javaProject.getPackageFragmentRoot(srcFolder);
            IPackageFragment pkgFrag= pkgFragRoot.getPackageFragment(servicePackage);

            page.setPackageFragmentRoot(pkgFragRoot, true);
            page.setPackageFragment(pkgFrag, true);

            String langClass= upperCaseFirst(langName);
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
            ErrorHandler.reportError("Could not create class implementing " + interfaceQualName, true, e);
        }
    }

    private void createProjectLabelText(Composite container) {
        Label label= new Label(container, SWT.NULL);

        label.setText("Project*:");
        label.setBackground(container.getBackground());
        label.setToolTipText("Select the plug-in project");
        fProjectText= new Text(container, SWT.BORDER | SWT.SINGLE);
        fProjectText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final IProject project= getProject();

        if (project != null)
            fProjectText.setText(project.getName());

        Button browseButton= new Button(container, SWT.PUSH);

        browseButton.setText("Browse...");
        browseButton.addSelectionListener(new ProjectBrowseSelectionListener(project));
        fProjectText.addModifyListener(new ProjectTextModifyListener());
        fProjectText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                // Text text = (Text)e.widget;
                fDescriptionText.setText("Select the plug-in project to add this extension point to");
            }
        });
    }

    private void addLanguageListener() {
        fLanguageText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setIDIfEmpty();
                setClassIfEmpty();
                setNameIfEmpty();
            }
        });
    }

    private void createDescriptionText(Composite container) {
        fDescriptionText= new Text(container, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        fDescriptionText.setBackground(container.getBackground());
        GridData gd= new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan= 3;
        gd.widthHint= 450;
        fDescriptionText.setLayoutData(gd);
        fDescriptionText.setEditable(false);
        fDescriptionText.setText(fSchema.getDescription());
    }

    private void addServiceEnablerCheckbox(Composite container) {
        Label label= new Label(container, SWT.NONE);
        label.setText("Add this service:");
        // label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        fAddThisExtensionPointButton= new Button(container, SWT.CHECK);
        fAddThisExtensionPointButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                fSkip= !fAddThisExtensionPointButton.getSelection();
                dialogChanged();
            }
        });
        // fAddThisExtensionPointButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        fAddThisExtensionPointButton.setSelection(true);
        GridData gd= new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan= 2;
        fAddThisExtensionPointButton.setLayoutData(gd);
    }

    private IProject discoverSelectedProject() {
        ISelectionService service= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
        ISelection selection= service.getSelection();
        IProject project= getProject(selection);

        if (project != null) {
            sProjectName= project.getName();
            fProjectText.setText(sProjectName);
        }
        return project;
    }

    /**
     * Attempt to find the languageDescription extension for the specified project
     * (if any), and use the language name from that extension to populate the
     * language name field in the dialog.
     */
    private void discoverProjectLanguage() {
	if (fProjectText.getText().length() == 0)
	    return;

	IPluginModelBase pluginModel= getPluginModel();

	if (pluginModel != null) {
	    IPluginExtension[] extensions= pluginModel.getExtensions().getExtensions();

	    for(int i= 0; i < extensions.length; i++) {
		if (extensions[i].getPoint().endsWith(".languageDescription")) {
		    IPluginObject[] children= extensions[i].getChildren();

		    for(int j= 0; j < children.length; j++) {
			if (children[j].getName().equals("language")) {
			    fLanguageText.setText(((IPluginElement) children[j]).getAttribute("language").getValue());
			    return;
			}
		    }
		}
	    }
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
        if (selection instanceof ITextSelection || selection == null) {
            IEditorPart editorPart= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            IEditorInput editorInput= editorPart.getEditorInput();

            if (editorInput instanceof IFileEditorInput) {
        	IFileEditorInput fileInput= (IFileEditorInput) editorInput;

        	return fileInput.getFile().getProject();
            }
        }
        return null;
    }

    private void setProjectName(String newProjectName) {
        if (newProjectName.startsWith("P\\"))
            sProjectName= newProjectName.substring(1);
        else if (newProjectName.startsWith("\\"))
            sProjectName= newProjectName;
        else
            sProjectName= "\\" + newProjectName;
    }

    public IProject getProject() {
        try {
            IProject project= null;

            if (sProjectName != null && sProjectName.length() > 0)
        	project= ResourcesPlugin.getWorkspace().getRoot().getProject(sProjectName);

            if (project == null)
        	project= discoverSelectedProject();

            if (project != null && project.exists())
                return project;
        } catch (Exception e) {
        }
        return null;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            setTitle(fSchema.getName() + " (Step " + (fThisPageNumber + 1) + " of " + fOwningWizard.getPageCount() + ")");
            fOwningWizard.setPage(fThisPageNumber);
            if (fLanguageText != null) {
                fLanguageText.setText(sLanguage);
            }
            if (fThisPageNumber > 0 && fProjectText.getCharCount() == 0) {
                fProjectText.setText(sProjectName);
            }
        }
        super.setVisible(visible);
        dialogChanged();
    }

    public boolean hasBeenSkipped() {
        return fSkip;
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

    protected void setNameIfEmpty() {
        try {
            WizardPageField langField= getField("language");
            String language= langField.getText();

            if (language.length() == 0)
                return;

            WizardPageField nameField= getField("name");

            if (nameField != null)
                nameField.setText(language + " " + fSchema.getPointId());
        } catch (Exception e) {
            ErrorHandler.reportError("Cannot set name", e);
        }
    }

    protected void setIDIfEmpty() {
        try {
            WizardPageField langField= getField("language");
            String language= langField.getText();

            if (language.length() == 0)
                return;

            String langID= lowerCaseFirst(language);
            WizardPageField idField= getField("id");

            if (idField != null)
                idField.setText(langID + ".safari." + lowerCaseFirst(fSchema.getPointId()));
        } catch (Exception e) {
            ErrorHandler.reportError("Cannot set ID", e);
        }
    }

    protected void setClassIfEmpty() {
        try {
            WizardPageField langField= getField("language");
            String language= langField.getText();

            if (language.length() == 0)
                return;
            String langPkg= lowerCaseFirst(language);
            String langClass= upperCaseFirst(language);
            WizardPageField classField= getField("class");

            fPackageName= langPkg + ".safari." + lowerCaseFirst(fSchema.getPointId());
            if (classField != null)
                classField.setText(fPackageName + "." + langClass + upperCaseFirst(fSchema.getPointId()));
        } catch (Exception e) {
            ErrorHandler.reportError("Cannot set class", e);
        }
    }

    protected String upperCaseFirst(String language) {
	return Character.toUpperCase(language.charAt(0)) + language.substring(1);
    }

    protected String lowerCaseFirst(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    IPluginModelBase getPluginModel() {
        try {
            PluginModelManager pmm= PDECore.getDefault().getModelManager();
            IPluginModelBase[] plugins= pmm.getAllPlugins();
            IProject project= getProject();

            if (project == null)
        	return null;
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
        if (fSkip)
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
                setErrorMessage("\"" + sProjectName + "\" is not a plug-in project. Please select a plug-in project to add this extension point to");
                setPageComplete(false);
                return;
            }
            WizardPageField field= getUncompletedField();
            if (field != null) {
                setErrorMessage("Please provide a value for the required attribute \"" + field.fLabel + "\"");
                setPageComplete(false);
                return;
            }
            setPageComplete(true);
        }
    }

    WizardPageField getUncompletedField() {
	// BUG Prevents clicking "Finish" if an element is optional but one of its attributes isn't
        for(int n= 0; n < fFields.size(); n++) {
            WizardPageField field= (WizardPageField) fFields.get(n);
            if (field.fRequired && field.fValue.length() == 0) {
                return field;
            }
        }
        return null;
    }

    public List getFields() {
        return fFields;
    }

    public String getValue(String name) {
        for(int n= 0; n < fFields.size(); n++) {
            WizardPageField field= (WizardPageField) fFields.get(n);
            if (field.fAttributeName.toLowerCase().equals(name)) {
                return field.fValue;
            }
        }
        return "No such field: " + name;
    }

    public WizardPageField getField(String name) {
        for(int n= 0; n < fFields.size(); n++) {
            WizardPageField field= (WizardPageField) fFields.get(n);
            if (field.fAttributeName.toLowerCase().equals(name)) {
                return field;
            }
        }
        return null;
    }

    public List getRequires() {
        return fRequiredPlugins;
    }

    protected void createLanguageFieldForPlatformSchema(Composite parent) {
        WizardPageField languageField= new WizardPageField(fExtPointID, "language", "Language", "", 0, true, "Language for which to create a " + fExtPointID);
    
        fLanguageText= createLabelTextBrowse(parent, languageField, null);

        fFields.add(languageField);
    
        fLanguageText.setData(languageField);
        fLanguageText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                Text text= (Text) e.widget;
                WizardPageField field= (WizardPageField) text.getData();
                field.fValue= text.getText();
                sLanguage= field.fValue;
                dialogChanged();
            }
        });
    }
}
