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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.search.JavaWorkspaceScope;
import org.eclipse.jdt.internal.ui.dialogs.PackageSelectionDialog;
import org.eclipse.jdt.internal.ui.dialogs.TypeSelectionDialog2;
import org.eclipse.jdt.internal.ui.wizards.NewClassCreationWizard;
import org.eclipse.jdt.ui.JavaUI;
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
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.ischema.IMetaAttribute;
import org.eclipse.pde.internal.core.ischema.ISchemaAttribute;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.internal.Workbench;

/**
 * The "New" wizard page allows setting the container for the new file as well as the file name. The page will only
 * accept file name without the extension OR with the extension that matches the expected one.
 */
public class GeneratedComponentWizardPage extends WizardPage
{
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


    
    protected String fComponentID;

    // SMS 26 Jul 2006:  no schema if no extension	
    //protected Schema fSchema;
    //protected String fExtPluginID;
    //protected String fExtPoinID;

    //protected ExtensionPointWizard fOwningWizard;
    protected GeneratedComponentWizard fOwningWizard;	
    
    protected int fThisPageNumber;

    protected int fTotalPages;

    protected boolean fSkip= false;

    protected boolean fIsOptional;

    protected List/*<WizardPageField>*/ fFields= new ArrayList();
    
    protected IProject fProject;

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
    
    protected String fWizardName = "";
    
    protected String fWizardDescription = "";

    protected List fAttributes = new ArrayList();
    
    
    public boolean canFlipToNextPage() {
        return super.canFlipToNextPage();
    }

    // SMS 4 Aug 2006:  Doesn't appear to be called from anywhere
//    public GeneratedComponentWizardPage(GeneratedComponentWizard owner, String pluginID, String pointID,
//    		GeneratedComponentAttribute[] attributes, String wizardName, String wizardDescription)
//    {
//    	this(owner, pluginID, pointID, true, attributes, wizardName, wizardDescription);
//    }

    public GeneratedComponentWizardPage(
    	GeneratedComponentWizard owner, /*String pluginID,*/ String componentID, boolean omitIDName,
    	GeneratedComponentAttribute[] attributes, String wizardName, String wizardDescription)
    {
        this(owner, 0, 1, /*pluginID,*/ componentID, false, attributes, wizardName, wizardDescription);
        fOmitExtensionIDName= omitIDName;
    }

    public GeneratedComponentWizardPage(
    	GeneratedComponentWizard owner, int pageNumber, int totalPages, /*String pluginID,*/ String componentID, boolean isOptional,
    	GeneratedComponentAttribute[] attributes, String wizardName, String wizardDescription)
    {
        super("wizardPage");
        //this.fExtPluginID= pluginID;
        this.fComponentID= componentID;
        this.fIsOptional= isOptional;
        this.fThisPageNumber= pageNumber;
        this.fTotalPages= totalPages;
        this.fOwningWizard= owner;
        for (int i = 0; i < attributes.length; i++) {
        	this.fAttributes.add(attributes[i]);
        }
        this.fWizardName = wizardName;
        this.fWizardDescription = wizardDescription;

    }

    // SMS 4 Aug 2006:  Not an extension, no schema
/*    
    private URL locateSchema(IExtensionPoint ep, String srcBundle) {
	Bundle platSrcPlugin= Platform.getBundle(srcBundle);
	Bundle extProviderPlugin= Platform.getBundle(ep.getNamespace());
	String extPluginVersion= (String) extProviderPlugin.getHeaders().get("Bundle-Version");
	Path schemaPath= new Path("src/" + ep.getNamespace() + "_" + extPluginVersion + "/" + ep.getSchemaReference());
	URL schemaURL= Platform.find(platSrcPlugin, schemaPath);

	return schemaURL;
    }
*/
    
    /**
     * Creates controls that are to appear by default above controls that
     * are created by wizard-specific attributes.  By default we want to
     * put the language field at (or near) the top, since it's generally
     * important and since other fields may have default values set based
     * on language.
     * 	
     * Derived classes may override.
     * 
     * @param parent
     */
    protected void createFirstControls(Composite parent) {
	    createLanguageFieldForPlatformSchema(parent);	
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
	        	//createControlsForSchema(fSchema, container);				// No schema for generated components
	        	createControlsForAttributes(fAttributes, null, container);	// SMS 26 Jul 2006:  new
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

    private void createControlsForAttributes(List attributes, String prefix, Composite container)
    {
    	for(int k= 0; k < attributes.size(); k++) {
    	    ISchemaAttribute attribute = (ISchemaAttribute) attributes.get(k);
      
    	    createElementAttributeTextField(container, prefix, attribute);
    	}
    }
    
    
    /**
     * Directly create a text field for a wizard from given values rather than using an extension schema
     * element or attributes.
     * 
     * @param container			The wizard page (or other container) in which the field will be placed
     * @param fieldCategoryName	A name for a larger grouping of fields that might contain this one
     * 							(used in place of the schema element name)
     * @param fieldName			A name for the field (used in place of the schema attribute name)
     * @param description		A description of the field (what it's for, how it should be filled, ...)
     * @param value				A value that may be filled into the field by default (may be null)
     * @param basedOn			For fields that represent a Java type, a value (such as the name of
     * 							a parent type) used to evaluate or process the given value
     * @param isRequired		Whether the field must be given a value before the containing wizard can
     * 							be finished
     */
    public void createTextField(Composite container, String fieldCategoryName, String fieldName, String description, String value, String basedOn, boolean isRequired)
    {
        String valueStr= (value == null) ? "" : value;
        String upName= upperCaseFirst(fieldName);

        WizardPageField field= new WizardPageField(fieldCategoryName, fieldName, upName, valueStr, IMetaAttribute.STRING, isRequired, description);
        Text text= createLabelTextBrowse(container, field, basedOn);

        // SMS 13 Jun 2007:  added test for "Language"
        // SMS 25 Sep 2007:  inherited from the original method in ExtensionPointWizardPage
        // on which this one is based; may still be useful
        if (fieldName.equals("language") || fieldName.equals("Language"))
            fLanguageText= text;
        else if (fieldName.equals("class"))
            fQualClassText= text;

        text.setData(field);
        fFields.add(field);
    }
  
    
    
    /*
     * For creating an element from a schema attribute; "generated components" don't rely on extension schemas
     * but some types of generated component may nevertheless define their fields using schema attributes.
     */
    private void createElementAttributeTextField(Composite container, String schemaElementPrefix, ISchemaAttribute attribute)
    {
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

        // SMS 27 Jul 2006
        // Added to allow for basedOn possibly being empty rather
        // than null when the value is based on nothing
        // (modified relevant conditional tests below)
        boolean basedOnSomething = (basedOn != null) && (basedOn.length() > 0);
        
        if (required)
            name+= "*";
        name+= ":";
        if (basedOnSomething) {
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
        if (!basedOnSomething)
            gd.horizontalSpan= 2;
        else
            gd.horizontalSpan= 1;
        text.setLayoutData(gd);
        text.setText(value);
        // SMS 27 Jul 2006:  added second condition:
        //if (basedOnSomething)
        //	createClassBrowseButton(container, field, text);
        if (basedOn != null) {
        	// SMS 5 May 2007
        	if (basedOn.endsWith("FileBrowse")) {
        		createFileBrowseButton(container, field, text);
        	} else if (basedOn.endsWith("FolderBrowse")) {
            	createFolderBrowseButton(container, field, text);
        	} else if (basedOn.endsWith("ClassBrowse")) {
	            createClassBrowseButton(container, field, text);
        	} else if (basedOn.endsWith("PackageBrowse")) {
	            createPackageBrowseButton(container, field, text);
        	} else {
	        	// This is the original action;
        		// left until a better option can be identified
	            createClassBrowseButton(container, field, text);
        	}
        }
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
                        GeneratedComponentEnabler.addImports(GeneratedComponentWizardPage.this);
                        String basedOnQualName= basedOn;
                        String basedOnTypeName= basedOn.substring(basedOnQualName.lastIndexOf('.') + 1);
                        String superClassName= "";

                        if (basedOnTypeName.charAt(0) == 'I' && Character.isUpperCase(basedOnTypeName.charAt(1))) {
                            superClassName= "org.eclipse.imp.defaults.Default" + basedOnTypeName.substring(1);
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
    
    ///////////////////////
    
    
	
    private void createFileBrowseButton(Composite container, WizardPageField field, Text text) {
        Button button= new Button(container, SWT.PUSH);
        button.setText("Browse...");
        button.setData(text);
        button.addSelectionListener(new FileBrowseSelectionAdapter(/*container,*/ field));
        if (field != null)
            field.fButton= button;
    }
    

    protected class FileBrowseSelectionAdapter extends SelectionAdapter
    {
    	private WizardPageField field;
    	
    	public FileBrowseSelectionAdapter(WizardPageField field) {
    		this.field = field;
    	}

    	public void widgetSelected(SelectionEvent e) {
          String newValue = null;
          File f = new File(field.getText());
          if (!f.exists())
              f = null;
          File d = getFile(f);
          if (d != null)
              newValue = d.getAbsolutePath();
          if (newValue != null) {	
          	field.setText(newValue);
          }
    	}
        
        /**
         * Helper to open the file chooser dialog.
         * @param startingDirectory the directory to open the dialog on.
         * @return File The File the user selected or <code>null</code> if they
         * do not.
         */
        private File getFile(File startingDirectory) {
            FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
            dialog.setText("File Browse");
            if (startingDirectory != null)
                dialog.setFileName(startingDirectory.getPath());
//            if (extensions != null)
//                dialog.setFilterExtensions(extensions);
            String file = dialog.open();
            if (file != null) {
                file = file.trim();
                if (file.length() > 0)
                    return new File(file);
            }	
            return null;
        }	
    }

    
    //
    // SMS 10 May 2007
    // Added following couple of classes to support addition
    // of browse buttons for arbitrary folders.
    //
    	
    private void createFolderBrowseButton(Composite container, WizardPageField field, Text text) {
        Button button= new Button(container, SWT.PUSH);
        button.setText("Browse...");
        button.setData(text);
        button.addSelectionListener(new FolderBrowseSelectionAdapter(/*container,*/ field));
        if (field != null)
            field.fButton= button;
    }
    

    protected class FolderBrowseSelectionAdapter extends SelectionAdapter
    {
    	private WizardPageField field;
    	
    	public FolderBrowseSelectionAdapter(WizardPageField field) {
    		this.field = field;
    	}

    	
    	public void widgetSelected(SelectionEvent e) {
    		
          String newValue = null;
          File f = new File(field.getText());
          if (!f.exists()) {
        	  IFolder srcFolder = getProject().getFolder("src");
        	  if (srcFolder.exists()) {
        		  f = srcFolder.getLocation().toFile();
        	  } else {
        		  f = getProject().getLocation().toFile();
        	  }
          }
          File d = getDirectory(f);
          if (d != null)
              newValue = d.getAbsolutePath();
          if (newValue != null) {	
          	field.setText(newValue);
          }
    	}
        
        /**
         * Helper that opens the folder chooser dialog.
         * @param startingDirectory The directory the dialog will open in.
         * @return File File or <code>null</code>.
         * 
         */
        private File getDirectory(File startingDirectory) {
            DirectoryDialog folderDialog = new DirectoryDialog(getShell(), SWT.OPEN);
            folderDialog.setText("Folder Browse");
            // Dialog should not return with a null value, although it might
            // return with an invalid one
            if (startingDirectory != null)
            	folderDialog.setFilterPath(startingDirectory.getPath());
            String dir = folderDialog.open();
            if (dir != null) {
	        	dir = dir.trim();
	        	if (dir.length() > 0)
	        	    return new File(dir);
            }
            return null;
        }
    }

    
    private void createPackageBrowseButton(Composite container, WizardPageField field, Text text) {
        Button button= new Button(container, SWT.PUSH);

        button.setText("Browse...");
        button.setData(text);
    	final Shell shell = container.getShell();
    	
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    IRunnableContext context= PlatformUI.getWorkbench().getProgressService();
                    IProject project = null;
                    String projectName = fProjectText.getText();

                    if (projectName != null) {
                	project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
                    }
                    if (project ==  null) {
                	project= getProject();
                    }
                	
                    if (project == null) {
                        setErrorMessage("Please select a plug-in project to add this extension to");
                        setPageComplete(false);
                        return;
                    }	
                    
                    JavaProject javaProject = (JavaProject) JavaCore.create(project);
                    if (javaProject == null)
                        // the project is not configured for Java (has no Java nature)
                        throw new Exception("createPackageBrowseButton:  unable to open Java project = '" + project.getName() + "' for search scope");

                    SelectionDialog dialog= JavaUI.createPackageDialog(shell, javaProject, 0);
                    dialog.setTitle("Package Browser");

                    if (dialog.open() == PackageSelectionDialog.OK) {
                        Text text= (Text) e.widget.getData();
                        IPackageFragment pack = (org.eclipse.jdt.internal.core.PackageFragment) dialog.getResult()[0];
                        text.setText(pack.getElementName());
                    }
                } catch (Exception ee) {
                    ErrorHandler.reportError("Could not browse package", ee);
                }
            }
        });
        if (field != null)
            field.fButton= button;
    }
    
    
    /////////////////////////
    
    

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
                    dialog.setTitle("Class Browse");

                    if (dialog.open() == TypeSelectionDialog2.OK) {
                        Text text= (Text) e.widget.getData();
                        //BinaryType type= (BinaryType) dialog.getFirstResult();
                        Object type = dialog.getFirstResult();
                        if (type instanceof BinaryType) {
                        	text.setText(((BinaryType)type).getFullyQualifiedName());
                        } else if (type instanceof SourceType) {
                        	text.setText(((SourceType)type).getFullyQualifiedName());
                        } else {
                        	throw new Exception("Type selected in dialog not of recognized type");
                        }
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
            String servicePackage= langPkg + ".imp." + fComponentID.substring(fComponentID.lastIndexOf('.')+1); // pkg the service belongs in

            //fOwningWizard.createSubFolders(servicePackage.replace('.', '\\'), getProject(), new NullProgressMonitor());
            WizardUtilities.createSubFolders(servicePackage.replace('.', '\\'), getProject(), new NullProgressMonitor());

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
        	if (fDescriptionText != null)
        	    fDescriptionText.setText("Select the plug-in project to add this extension to");
            }
        });
    }

    private void addLanguageListener() {
	if (fLanguageText != null)
        fLanguageText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setClassByLanguage();
                // SMS 26 Jul 2006:  no id or name for something that's not an extension
                //setIDByLanguage();
                //setNameByLanguage();
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
        //if (fSchema != null)
        //    fDescriptionText.setText(fSchema.getDescription());
        fDescriptionText.setText(fWizardDescription);
    }

    protected Text createLabelText(Composite container, WizardPageField field) {
        Widget labelWidget= null;
        String name= field.fAttributeName;
        String description= field.fDescription;
        String value= field.fValue;

        name += ":";

        Label label= new Label(container, SWT.NULL);
        label.setText(name);
        label.setToolTipText(description);
        labelWidget= label;
        label.setBackground(container.getBackground());

        Text text= new Text(container, SWT.BORDER | SWT.SINGLE);
        labelWidget.setData(text);
        GridData gd= new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan= 2;
        text.setLayoutData(gd);
        text.setText(value);

        if (field != null)
            field.fText= text;

        text.addFocusListener(new FocusDescriptionListener());
        text.setData(field);

        return text;
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
        	fProject = project;
            sProjectName= project.getName();
            fProjectText.setText(sProjectName);
        }
        return project;
    }

    /**
     * Attempt to find the languageDescription extension for the specified project
     * (if any), and use the language name from that extension to populate the
     * language name field in the dialog.
     * 
     * SMS 26 Jul 2007:  Adapted to use load the extensions model in detail using
     * the IMP-adapted mechanism	
     * 
     */
    private void discoverProjectLanguage() {
		if (fProjectText.getText().length() == 0)
		    return;
	
		IPluginModelBase pluginModel= getPluginModel();
		
		if (pluginModel != null) {
		   	// SMS 26 Jul 2007
	        // Load the extensions model in detail, using the adapted IMP representation,
	        // to assure that the children of model elements are represented
	    	if (fProject == null) {
	    		discoverSelectedProject();
	    	}
	    	try {
	    		ExtensionPointEnabler.loadImpExtensionsModel((IPluginModel)pluginModel, fProject);
	    	} catch (CoreException e) {
	    		//System.err.println("GeneratedComponentWizardPage.discoverProjectLanguage():  CoreExeption loading extensions model; may not succeed");
	    	} catch (ClassCastException e) {
	    		System.err.println("GeneratedComponentWizardPage.discoverProjectLanguage():  ClassCastExeption loading extensions model; may not succeed");
	    	}
	    	
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
    
    
    public String getProjectNameFromField() {
    	return fProjectText.getText();
    }
    

    public void setVisible(boolean visible) {
        if (visible) {
            //setTitle((fSchema != null ? fSchema.getName() : "") + " (Step " + (fThisPageNumber + 1) + " of " + fOwningWizard.getPageCount() + ")");
            setTitle(fWizardName + " (Step " + (fThisPageNumber + 1) + " of " + fOwningWizard.getPageCount() + ")");
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

    // SMS 4 Aug 2006:  not called but retained for potential convenience
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

 
    
    protected void setClassByLanguage() {
   	try {
            WizardPageField langField= getField("language");
            WizardPageField classField= getField("class");
            String language= langField.getText();

            if (language.length() == 0)
                return;
            String langPkg= lowerCaseFirst(language);
            String langClass= upperCaseFirst(language);
            //String pointID= fSchema != null ? fSchema.getPointId() : fComponentID;

            // SMS 21 Jul 2006
            // Above the field pointID is set in such a way as to accommodate fSchema being
            // null, but fSchema was referenced three times below anyway.  I've substituted
            // pointID for those references

            fPackageName= langPkg + ".imp." + lowerCaseFirst(fComponentID);
            
            if (classField != null) {
                classField.setText(fPackageName + "." + langClass + upperCaseFirst(fComponentID));
            }
            
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
                setErrorMessage("Please select a plug-in project to add this extension to");
                setPageComplete(false);
                return;
            }
            boolean isPlugin= false;
            try {
                isPlugin= project.hasNature("org.eclipse.pde.PluginNature");
            } catch (CoreException e) {
            }
            if (!isPlugin) {
                setErrorMessage("\"" + sProjectName + "\" is not a plug-in project. Please select a plug-in project to add this extension to");
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

    // SMS 4 Aug 2006:  not called but retained for potential convenience
    public List getFields() {
        return fFields;
    }

    // SMS 4 Aug 2006:  not called but retained for potential convenience
    public String getValue(String name) {
        for(int n= 0; n < fFields.size(); n++) {
            WizardPageField field= (WizardPageField) fFields.get(n);
            if (field.fAttributeName.toLowerCase().equals(name.toLowerCase())) {
                return field.fValue;
            }
        }
        return "No such field: " + name;
    }

    public WizardPageField getField(String name) {
        for(int n= 0; n < fFields.size(); n++) {
            WizardPageField field= (WizardPageField) fFields.get(n);
            if (field.fAttributeName.toLowerCase().equals(name.toLowerCase())) {
                return field;
            }
        }
        return null;
    }

    public List getRequires() {
        return fRequiredPlugins;
    }

    protected void createLanguageFieldForPlatformSchema(Composite parent) {
        WizardPageField languageField= new WizardPageField(fComponentID, "language", "Language", "", 0, true, "Language for which to create " + fComponentID);
    
        fLanguageText= createLabelTextBrowse(parent, languageField, null);
        fLanguageText.setData(languageField);
        fFields.add(languageField);
    

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
    
    
    protected void createClassField(Composite parent, String basedOn) {
	    WizardPageField field = new WizardPageField(null, "class", "Class:", "Compiler", 0, true, "Name of the class that implements " + fComponentID);
	    Text classText= createLabelTextBrowse(parent, field, basedOn);

	    classText.setData(field);
	    fFields.add(field);
    }

}
