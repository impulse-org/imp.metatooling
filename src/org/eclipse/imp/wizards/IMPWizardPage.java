package org.eclipse.imp.wizards;

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
import org.eclipse.jdt.core.IJavaElement;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.internal.Workbench;

public class IMPWizardPage extends WizardPage {

	// A simple, extension-point-like name for the component;
	// intended to serve as an internal and reasonably-user-intelligible
	// identifier; expected to be the same as the extension-point
	// id for those components that extend extension points
    protected String fComponentID = "Unspecified";
	
	protected IMPWizard fOwningWizard;

    protected int fThisPageNumber;

    protected int fTotalPages;

    protected boolean fSkip= false;

    protected boolean fIsOptional;

    protected List<WizardPageField> fFields= new ArrayList<WizardPageField>();
    
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

    protected static String sLanguage= "";

    protected static String sProjectName= "";

    
    
	public IMPWizardPage(String pageName) {
		super(pageName);
	}


    public IMPWizardPage(String wizardName, IMPWizard owner, int pageNumber, int totalPages, boolean isOptional)
    {
    	super(wizardName);
        this.fIsOptional= isOptional;
        this.fThisPageNumber= pageNumber;
        this.fTotalPages= totalPages;
        this.fOwningWizard= owner;
    }
	
	
    public boolean canFlipToNextPage() {
        return super.canFlipToNextPage();
    }
    
    
    public List getFields() {
        return fFields;
    }
    
	public List getRequires() {
		return fRequiredPlugins;
	}
	
    public boolean hasBeenSkipped() {
        return fSkip;
    }
    
    public String getProjectNameFromField() {
    	return fProjectText.getText();
    }
    
    public IProject getProjectOfRecord() {
    	return fProject;
    }
    
    
    protected void setProjectName(String newProjectName) {
        if (newProjectName.startsWith("P\\"))
            sProjectName= newProjectName.substring(1);
        else if (newProjectName.startsWith("\\"))
            sProjectName= newProjectName;
        else
            sProjectName= "\\" + newProjectName;
    }
    
	protected final class FocusDescriptionListener extends FocusAdapter {
		public void focusGained(FocusEvent e) {
		    Text text= (Text) e.widget;
		    WizardPageField field= (WizardPageField) text.getData();
		    fDescriptionText.setText(field.fDescription);
		}
	}

    protected final class ProjectTextModifyListener implements ModifyListener {
		public void modifyText(ModifyEvent e) {
		    Text text= (Text) e.widget;

		    String projectName = text.getText();
		    setProjectName(projectName);
		    fProject = getProjectBasedOnNameField();
		    discoverProjectLanguage();
		    
		    // RMF Don't add imports yet; wait for user to press "Finish"
		    // ExtensionPointEnabler.addImports(ExtensionPointWizardPage.this);
		    dialogChanged();
		}
    }

    protected final class ProjectBrowseSelectionListener extends SelectionAdapter {
		private final IProject project;
	
		protected ProjectBrowseSelectionListener(IProject project) {
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
                    // SMS 9 Oct 2007
                    //System.out.println("IMPWizardPage:projectBrowseSelectionAdapter:  updating fProject = " + sProjectName);	
                    fProject = getProjectBasedOnNameField();
		        } else {
                    // SMS 9 Oct 2007
                    //System.out.println("IMPWizardPage:projectBrowseSelectionAdapter:  not updating fProject (or other project data)");	
		        }
		    }
		}
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
	    	  IFolder srcFolder = getProjectOfRecord().getFolder("src");
	    	  if (srcFolder.exists()) {
	    		  f = srcFolder.getLocation().toFile();
	    	  } else {
	    		  f = getProjectOfRecord().getLocation().toFile();
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




	protected void addServiceEnablerCheckbox(Composite container) {
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
    
    
    
    /**
     * Attempt to find the languageDescription extension for the specified project
     * (if any), and use the language name from that extension to populate the
     * language name field in the dialog.
     */
    protected void discoverProjectLanguage() {
		if (fProjectText.getText().length() == 0)
		    return;
	
		IPluginModelBase pluginModel= getPluginModel(fProjectText.getText());
	
		if (pluginModel != null) {
		   	// SMS 26 Jul 2007
	        // Load the extensions model in detail, using the adapted IMP representation,
	        // to assure that the children of model elements are represented
			// SMS 9 Oct 2007
			// For some reason, when a wizard is started (it seems), fProject will
			// be null still, so we need to grab the selected project, if any
			// (but how can thiw be when fProjectText has a non-zero length???)
			if (fProject == null) {
				//System.out.println("IMPWIzardPage.discoverProjectLanguage():  fProject == null");
				fProject = discoverSelectedProjectWithoutUpdating();
				//System.out.println("IMPWIzardPage.discoverProjectLanguage():  updated fProject == " + fProject.getName());
			} else {
				//System.out.println("IMPWIzardPage.discoverProjectLanguage():  fProject != null");
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

    
    /*
     * Get the project most recently selected in whatever wizard
     * has been run.  This method may be called when the wizard
     * is still active or after it is disposed.  In the former
     * case it returns the project, if any, that is currently
     * selected.  If the latter case, or if there is no current
     * selection, it returns the project that was most recently
     * selected (if any).
     * 
     * SMS 23 May 2007
     * Updated to use an alternative method to test for the
     * selected project, i.e., one that doesn't trigger an update
     * of the corresponding text field, thus not triggering a
     * callback to this method.  Nonterminating cycles were not a
     * problem with the previous implementation of this method,
     * but the test that prevented cycles caused subsequent project
     * selections to be missed.  This approach seems to both to preclude
     * nonterminating cycles and recognize updates of the selected
     * project.
     * 
     * SMS 24 May 2007
     * Updated to check for null project at first "discover" call,
     * which can happen if called when there is no active workbench
     * window.
     */
//    public IProject getProject() {
//        try {
//            IProject project= null;
//            boolean haveCurrentSelection = false;
//            project = discoverSelectedProjectWithoutUpdating();
//            if (project != null) {
//            	sProjectName = project.getName();
//            	haveCurrentSelection = true;
//            }
//
//            if (!haveCurrentSelection && sProjectName != null && sProjectName.length() > 0)
//            	// get project based on name set with previous selection
//            	project= ResourcesPlugin.getWorkspace().getRoot().getProject(sProjectName);
//            if (project == null)
//            	project= discoverSelectedProject();
//
//            if (project != null && project.exists())
//                return project;
//        } catch (Exception e) {
//        }
//        return null;
//    }
    
    // SMS 8 Oct 2007
    // Replaced the above with the below, which seems to work now
    // May need to distinguish two "get project" methods:  one to get
    // the currently named project, one to get the currently selected
    // project.  To have various methods that "get project" in various
    // ways corresponding to various implicit senses is silly.
    
    
    // SMS 9 Oct 2007
    public IProject discoverSelectedProject() {
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
    
    // SMS 9 Oct 2007 replaced with more specific forms
//    public IProject getProject() {
//        try {
//            IProject project= null;
//
//            if (sProjectName != null && sProjectName.length() > 0)
//        	project= ResourcesPlugin.getWorkspace().getRoot().getProject(sProjectName);
//
//            if (project == null)
//            	// SMS 9 Oct 2007
//            	//project= discoverSelectedProject();
//
//            if (project != null && project.exists())
//                return project;
//        } catch (Exception e) {
//        }
//        return null;
//    }


    public IProject getProjectBasedOnNameField() {
        try {
            IProject project= null;

            if (sProjectName != null && sProjectName.length() > 0)
        	project= ResourcesPlugin.getWorkspace().getRoot().getProject(sProjectName);

//          if (project == null)
//			project= discoverSelectedProject();

            if (project != null && project.exists())
                return project;
        } catch (Exception e) {
        }
        return null;
    }
    
    
    public IProject getSelectedProject() {
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
    
    
    
    
    /*
     * This (original) version of discoverSelectedProject() is called by getProject().
     * It updates fProjectText, which triggers a further call to getProject().
     * The potential cycle has been broken by a test in getProject() for a change
     * to sProjectName.  That test allows the whole thing to work for the first
     * project selected, but it meant that later selections of a different
     * project would go unrecognized.  (Since sProjectName was set, there was no
     * reason to try to discover a newly selected project.)
     * SMS 23 May 2007
     */
//    protected IProject discoverSelectedProject() {
//        ISelectionService service= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
//        ISelection selection= service.getSelection();
//        IProject project= getProject(selection);
//
//        if (project != null) {
//        	fProject = project;
//            sProjectName= project.getName();
//            fProjectText.setText(sProjectName);
//        }
//        return project;
//    }

    
    /*
     * This new version of discoverSelectedProject(WithoutUpdating)() is now also
     * called from getProject(); it differs from the original in not updating fProjectText.
     * That allows getProject() to get a selected project without triggering a recursive
     * call, which means it can be used to test for a newly selected project even after
     * another project has been previously selected.
     * SMS 23 May 2007
     * 
     * Updated to better address the case in which there is no active workbench window.
     * SMS 25 May 2007
     * 
     */
    protected IProject discoverSelectedProjectWithoutUpdating() {
    	try {
    		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		    if (activeWindow == null)
		    	return null;
    		ISelectionService service= activeWindow.getSelectionService();
		    ISelection selection= service.getSelection();
		    IProject project= getProject(selection);
		    return project;
    	} catch (NullPointerException e) {
    		return null;
    	}
    }
    
    
    protected IProject getProject(ISelection selection) {
        if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
            IStructuredSelection ssel= (IStructuredSelection) selection;
            if (ssel.size() > 1)
                return null;
            Object obj= ssel.getFirstElement();
            if (obj instanceof IJavaElement)
                obj= ((IJavaElement) obj).getResource();
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
    
    
    protected IPluginModelBase getPluginModel() {
    	IProject project = null;
        try {
            PluginModelManager pmm= PDECore.getDefault().getModelManager();
            IPluginModelBase[] plugins= pmm.getAllPlugins();
            project= getProjectOfRecord();

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
            ErrorHandler.reportError("Could not enable extension point for " + project.getName(), e);
        }
        return null;
    }

    
    protected IPluginModelBase getPluginModel(String projectName) {
        try {
        	if (projectName == null)
        		return null;
            PluginModelManager pmm= PDECore.getDefault().getModelManager();
            IPluginModelBase[] plugins= pmm.getAllPlugins();

            for(int n= 0; n < plugins.length; n++) {
                IPluginModelBase plugin= plugins[n];
                IResource resource= plugin.getUnderlyingResource();
                if (resource != null && projectName.equals(resource.getProject().getName())) {
                    return plugin;
                }
            }
        } catch (Exception e) {
            ErrorHandler.reportError("Could not enable extension point for " + projectName, e);
        }
        return null;
    }
    
    
    protected void dialogChanged() {
        setErrorMessage(null);
        if (fSkip)
            setPageComplete(true);
        else {
        	// SMS 13 Jun 2007
        	// Seem to need to check for a project that's set in the wizard
        	// before going and getting a project otherwise, which can return
        	// the current selection in the package explorer regardless
        	// of what's set in the wizard
            //IProject project= getProject();
//            IProject project = null;
//            String projectName = fProjectText.getText();
//            if (projectName != null && projectName.length() > 0) {
//        	project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
//            }
//            if (project ==  null) {
//        	project= getProject();
//            }
        	
        	// SMS 9 Oct 2007
            IProject project = null;
            if (fProject != null)
            	project = fProject;
            else
            	project = getProjectBasedOnNameField();
            
            
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
    
    protected WizardPageField getUncompletedField() {
    	// BUG Prevents clicking "Finish" if an element is optional but one of its attributes isn't
        for(int n= 0; n < fFields.size(); n++) {
            WizardPageField field= (WizardPageField) fFields.get(n);
            if (field.fRequired && field.fValue.length() == 0) {
                return field;
            }
        }
        return null;
    }
    
    public WizardPageField getField(String name) {
        for(int n= 0; n < fFields.size(); n++) {
            WizardPageField field= (WizardPageField) fFields.get(n);
            // SMS 13 Jun 2007:  added toLowerCase of name
            if (field.fAttributeName.toLowerCase().equals(name.toLowerCase())) {
                return field;
            }
        }
        return null;
    }
    

    public String getValue(String name) {
        for(int n= 0; n < fFields.size(); n++) {
            WizardPageField field= (WizardPageField) fFields.get(n);
            // SMS 27 Sep 2007:  lower-cased name
            if (field.fAttributeName.toLowerCase().equals(name.toLowerCase())) {
                return field.fValue;
            }
        }
        return "No such field: " + name;
    }

    
    protected String upperCaseFirst(String language) {
    	return Character.toUpperCase(language.charAt(0)) + language.substring(1);
    }

    protected String lowerCaseFirst(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }


	/**
	 * Creates additional controls that are to appear below the schema
	 * attributes on the wizard page. Derived classes may override.
	 * @param parent
	 */
	protected void createAdditionalControls(Composite parent) {
	    // Noop here; optionally overridden in derived classes
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



	/**
	 * @override
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
	
	}
	


	protected void createDescriptionText(Composite container, String text) {
	    fDescriptionText= new Text(container, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
	    fDescriptionText.setBackground(container.getBackground());
	    GridData gd= new GridData(GridData.FILL_BOTH);
	    gd.horizontalSpan= 3;
	    gd.widthHint= 450;
	    fDescriptionText.setLayoutData(gd);
	    fDescriptionText.setEditable(false);
	    //if (fSchema != null)
	    //    fDescriptionText.setText(fSchema.getDescription());
	    fDescriptionText.setText(text);				//fWizardDescription);
	}


	/*
	 * For creating an element from a schema attribute; "generated components" don't rely on extension schemas
	 * but some types of generated component may nevertheless define their fields using schema attributes.
	 */
	protected void createElementAttributeTextField(Composite container, String schemaElementPrefix, ISchemaAttribute attribute)
	{
	    String name= attribute.getName();
	
	    // We manually create the language field first, for the user's
	    // convenience, so check to see whether we have already created it.
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
	
        if (name.equals("language") || name.equals("Language")) {
            fLanguageText= text;
            addProjectListener();
        }
        else if (name.equals("class")) {
            fQualClassText= text;
            // SMS 9 Oct 2007:  why not add language listener here?
            addLanguageListener();
        }
        
	    text.setData(field);
	    fFields.add(field);
	}


    protected void addLanguageListener() {
    	if (fLanguageText != null) {
    		//System.out.println("IMPWizardPage.addLanguageListener:  adding listener");
            fLanguageText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                	//System.out.println("IMPWizardPage:languageModifyListener:  got modify event");
                    setClassByLanguage();
                    setIDByLanguage();
                    setNameByLanguage();
                }
            });
    	} else {
    		System.err.println("IMPWizardPage.addLanguageListener:  unable to add listener");
    	}	
    }

    
    protected void addProjectListener() {
    	if (fProjectText != null) {
    		//System.out.println("IMPWizardPage.addProjectListener:  adding listener");
    		ProjectTextModifyListener ptml = new ProjectTextModifyListener();
            fProjectText.addModifyListener(ptml);
    	} else {
    		System.err.println("IMPWizardPage.addProjectListener:  unable to add listener");
    	}
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
    protected void createFirstControls(Composite parent, String componentID) {
	    createLanguageFieldForComponent(parent, componentID);
	    
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


	protected Text createLabelTextBrowse(Composite container, WizardPageField field, final String basedOn) {
	    Widget labelWidget= null;
	    String name= field.fAttributeName;
	    String description= field.fDescription;
	    String value= field.fValue;
	// BUG Prevents clicking "Finish" if an element is optional but one of its attributes isn't
	    boolean required= field.fRequired;

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

	    if (basedOnSomething) {
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

	
	/**
	 * Create the language field for a wizard page for a component of the
	 * IDE under construction. The component may or may not be an extension
	 * of an extension point and so may or may not have a corresponding
	 * extension point schema id.  The component id is a generalization of
	 * the schema id that can be used with both extension and non-extension
	 * components.
	 * 
	 * SMS 13 Nov 2007:  Field is created in a disabled state, on the theory
	 * that most IMP wizards will be run with reference to a particular project
	 * and that most projects will uniquely determine a language during most
	 * stages of IDE construction.  Thus, there is usually no reason to allow
	 * the IDE developer to set the language field independently.
	 * 
	 * May be overridden by derived types of wizard page.  One particular
	 * reason to override is to have the langauage be enabled, as when the
	 * language is first defined for a project.
	 * 
	 * @param parent		The page in which the field will reside
	 * @param componentID	The id of the component (extension or otherwise)
	 * 						to which the page and field are dedicated
	 */
    protected void createLanguageFieldForComponent(Composite parent, String componentID) {
        WizardPageField languageField= new WizardPageField(
        	componentID, "language", "Language", "", 0, true, "Language for which to create " + componentID);

        fLanguageText= createLabelTextBrowse(parent, languageField, null);
        fLanguageText.setData(languageField);

        fLanguageText.setEnabled(false);
        
        fFields.add(languageField);
    
        // Listen to changes in value of language field in order to
        // reset dependent fields
        fLanguageText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                Text text= (Text) e.widget;
                WizardPageField field= (WizardPageField) text.getData();
                field.fValue= text.getText();
                sLanguage= field.fValue;
                dialogChanged();
            }
        });
        
        // SMS 9 Oct 2007
        // Listen to changes in the value of the project field in order to
        // reset the language field
        if (fProjectText != null) {
	        //System.out.println("IMPWizardPage.createLanguageField...:  fProjectText != null, adding listener");
        	fProjectText.addModifyListener(
        		new ModifyListener() {
		            public void modifyText(ModifyEvent e) {
		            	discoverProjectLanguage();
		            }
        		});
        } else {
	        System.err.println("IMPWizardPage.createLanguageField...:  fProjectText == null, not adding listener");	
        }
        
    }


	private Widget createNewClassHyperlink(WizardPageField field, String name, final String basedOn, Composite container) {
	    Widget labelWidget;
	    FormToolkit toolkit= new FormToolkit(Display.getDefault());
	    Hyperlink link= toolkit.createHyperlink(container, name, SWT.NULL);
	
	    link.addHyperlinkListener(new HyperlinkAdapter() {
	        public void linkActivated(HyperlinkEvent e) {
	            Text text= (Text) e.widget.getData();
	            try {
	                if (getProjectNameFromField() == null)
	                    MessageDialog.openError(null, "IMP Wizard", "Please select a project first");
	                else {
	            	// BUG Should pick up info from wizard page, rather than using defaults.
                    	// SMS 28 Sep 2007:  Why add imports here?  Omitting doesn't seem to cause problems ...
	                    //GeneratedComponentEnabler.addImports(GeneratedComponentWizardPage.this);
	                    String basedOnQualName= basedOn;
	                    String basedOnTypeName= basedOn.substring(basedOnQualName.lastIndexOf('.') + 1);
	                    String superClassName= "";
	
	                    if (basedOnTypeName.charAt(0) == 'I' && Character.isUpperCase(basedOnTypeName.charAt(1))) {
	                        superClassName= "org.eclipse.imp.defaults.Default" + basedOnTypeName.substring(1);
	                    }	
	                    openClassDialog(fComponentID, basedOnQualName, superClassName, text);
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
	
	

    protected void openClassDialog(String componentID, String interfaceQualName, String superClassName, Text text) {
        try	 {
            String intfName= interfaceQualName.substring(interfaceQualName.lastIndexOf('.') + 1);
            IJavaProject javaProject= JavaCore.create(getProjectOfRecord());
            
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

            ArrayList<String> interfaces= new ArrayList<String>();

            interfaces.add(interfaceQualName);
            page.setSuperInterfaces(interfaces, true);

            IFolder srcFolder= getProjectOfRecord().getFolder("src/");
            String servicePackage= langPkg + ".imp." + componentID.substring(componentID.lastIndexOf('.')+1); // pkg the service belongs in

            //fOwningWizard.createSubFolders(servicePackage.replace('.', '\\'), getProject(), new NullProgressMonitor());
            WizardUtilities.createSubFolders(servicePackage.replace('.', '\\'), getProjectOfRecord(), new NullProgressMonitor());
            
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


	private void createPackageBrowseButton(Composite container, WizardPageField field, Text text) {
	    Button button= new Button(container, SWT.PUSH);
	
	    button.setText("Browse...");
	    button.setData(text);
		final Shell shell = container.getShell();
		
	    button.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	            try {
//	                IRunnableContext context= PlatformUI.getWorkbench().getProgressService();
//	                IProject project = null;
//	                String projectName = fProjectText.getText();
//	
//	                if (projectName != null) {
//	            	project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
//	                }
//	                if (project ==  null) {
//	            	project= getProject();
//	                }
	            	IProject project = fProject;
	            	if (project == null)
	            		project = getProjectBasedOnNameField();
	                
	                
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


	protected void createProjectLabelText(Composite container) {
	    Label label= new Label(container, SWT.NULL);
	
	    label.setText("Project*:");
	    label.setBackground(container.getBackground());
	    label.setToolTipText("Select the plug-in project");
	    fProjectText= new Text(container, SWT.BORDER | SWT.SINGLE);
	    fProjectText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	    // SMS 9 Oct 2007
	    // Here we've just created the project text, so we can't
	    // expect that it has any value; to find a value, get the
	    // project that is currently selected
	    final IProject project= discoverSelectedProject(); //getProject();

	    if (project != null) {
	    	// Try setting fProject directly rather than waiting for a listener
	        fProjectText.setText(project.getName());
	        fProject = getProjectBasedOnNameField();
	    }
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

	/**
	 * Set the name of the "extension" based on the language.  "Extension name" is
	 * a concept that applies (as you would expect) to components that implement
	 * extensions of extension points.  We might possibly define an analogous role
	 * for generated components that are not based on extension points.  In any case,
	 * the logic for computing the name can be made independent of the the type of
	 * wizard (in that sense) and so for generality it is included at this level
	 * of the wizard type hierarchy.
	 */
    protected void setNameByLanguage() {
        try {
            WizardPageField langField= getField("language");
            String language= langField.getText();

            if (language.length() == 0)
                return;
            // Give language name an upper-case initial for this purpose
            language = language.substring(0, 1).toUpperCase() + language.substring(1);

            WizardPageField nameField= getField("name");

            if (nameField != null) {
            	// SMS 21 Jul 2006
            	// setClassIfEmpty() below had provision for fSchema == null
            	// but this method did not; I've added it
                //String pointID= fSchema != null ? fSchema.getPointId() : fExtPointID;
                nameField.setText(language + " " + fComponentID);
	            // SMS 10 May 2006:
	            // Struggling with plural and singular for "builder" classes, ids, names, etc.
	            if (fComponentID.endsWith("uilders") || fComponentID.endsWith("olvers")) {
	            	nameField.setText(nameField.getText().substring(0, nameField.getText().length()-1));
	            }
            }
        } catch (Exception e) {
            ErrorHandler.reportError("Cannot set name", e);
        }
    }

    
	/**
	 * Set the id of the "extension" based on the language.  "Extension id" is
	 * a concept that applies (as you would expect) to components that implement
	 * extensions of extension points.  We might possibly define an analogous role
	 * for generated components that are not based on extension points.  In any case,
	 * the logic for computing the id can be made independent of the the type of
	 * wizard (in that sense) and so for generality it is included at this level
	 * of the wizard type hierarchy.
	 */
    protected void setIDByLanguage() {
        try {
             WizardPageField langField= getField("language");
             String language= langField.getText();

             if (language.length() == 0)
                 return;

             String langID= lowerCaseFirst(language);
             WizardPageField idField= getField("id");

             if (idField != null) {
             	// SMS 21 Jul 2006
             	// setClassIfEmpty() below had provision for fSchema == null
             	// but this method did not; I've added it
                 //String pointID= fSchema != null ? fSchema.getPointId() : fExtPointID;
                 
                 idField.setText(langID + ".imp." + lowerCaseFirst(fComponentID));
 	            // SMS 10 May 2006:
 	            // Struggling with plural and singular for "builder" classes, ids, names, etc.
 	            if (fComponentID.endsWith("uilders") || fComponentID.endsWith("olvers")) {
 	            	idField.setText(idField.getText().substring(0, idField.getText().length()-1));
 	            }
             }
         } catch (Exception e) {
             ErrorHandler.reportError("Cannot set ID", e);
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
    	            // SMS 10 May 2006:
    	            // Struggling with plural and singular for "builder" classes, ids, names, etc.
    	            if (fComponentID.endsWith("uilders") || fComponentID.endsWith("olvers")) {
    	            	classField.setText(classField.getText().substring(0, classField.getText().length()-1));
    	            }
                }
                
            } catch (Exception e) {
                ErrorHandler.reportError("Cannot set class", e);
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
}
