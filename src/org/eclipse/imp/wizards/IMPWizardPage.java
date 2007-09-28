package org.eclipse.imp.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionValidator;

public class IMPWizardPage extends WizardPage {

	
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
	
	
	
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub

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
		        }
		    }
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
    public IProject getProject() {
        try {
            IProject project= null;
            boolean haveCurrentSelection = false;
            project = discoverSelectedProjectWithoutUpdating();
            if (project != null) {
            	sProjectName = project.getName();
            	haveCurrentSelection = true;
            }

            if (!haveCurrentSelection && sProjectName != null && sProjectName.length() > 0)
            	// get project based on name set with previous selection
            	project= ResourcesPlugin.getWorkspace().getRoot().getProject(sProjectName);
            if (project == null)
            	project= discoverSelectedProject();

            if (project != null && project.exists())
                return project;
        } catch (Exception e) {
        }
        return null;
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
    protected IProject discoverSelectedProject() {
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
    protected	 IProject discoverSelectedProjectWithoutUpdating() {
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
            	project= getProject();

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
            IProject project = null;
            String projectName = fProjectText.getText();
            if (projectName != null && projectName.length() > 0) {
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
    
}
