package org.eclipse.imp.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.ui.dialogs.ListSelectionDialog;
import org.eclipse.imp.ui.dialogs.filters.ViewerFilterForIDEProjects;
import org.eclipse.imp.ui.dialogs.providers.ContentProviderForAllProjects;
import org.eclipse.imp.ui.dialogs.providers.LabelProviderForProjects;
import org.eclipse.imp.ui.dialogs.validators.SelectionValidatorForIDEProjects;
import org.eclipse.jdt.core.IClassFile;
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
import org.eclipse.jface.viewers.ViewerFilter;
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
import org.osgi.framework.Bundle;

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
    
    // SMS 23 Nov 2007
    protected List<ISelectionValidator> projectValidators = new ArrayList();
    
    
    
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
            sProjectName= newProjectName.substring(2);
        else if (newProjectName.startsWith("\\"))
            sProjectName= newProjectName.substring(1);
        else
            sProjectName= newProjectName;
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
		    
	        String errorMessage = validateProjectField();
	        if (errorMessage != null && errorMessage.length() > 0) {
	        	setPageComplete(false);
	        	setErrorMessage(errorMessage);
	        	return;
	        }
		    
		    dialogChanged();
		}
    }

    
    /*
     *  Creates a ContainerSelectionDialog for selecting projects.  Selected projects
     *  are optionally subject to validation as plug-in or IMP IDE projects.
     */
    protected class ProjectBrowseSelectionListener extends SelectionAdapter {
		private IProject project;
		
		protected ProjectBrowseSelectionListener(IProject project) {
			this(project, false, false);
		}
		
		protected ProjectBrowseSelectionListener(
			IProject project, boolean validateForPluginProject, boolean validateForIDEProject)
		{
			super();
			this.project = project;
		}

		public void widgetSelected(SelectionEvent e) {
			ListSelectionDialog dialog = new ListSelectionDialog(
				getShell(), 
				ResourcesPlugin.getWorkspace().getRoot(), //project, 
				new ContentProviderForAllProjects(), new LabelProviderForProjects(),
				"Select a project");

		    if (project != null)
		        dialog.setInitialSelections(new Object[] { project.getFullPath() });
		    
		    // Add a viewer filter and/or selection validator to the dialog.
		    // If the filter is sufficiently specific, then the validator
		    // will be redundant.
		    addFilterToDialog(dialog);
		    addValidatorToDialog(dialog);
		    
		    if (dialog.open() == ContainerSelectionDialog.OK) {
		        Object[] result= dialog.getResult();
		        //IProject selectedProject= ResourcesPlugin.getWorkspace().getRoot().getProject(result[0].toString());
		        
		        if (result.length >= 1 && result[0] instanceof IProject) {
		        	IProject selectedProject = (IProject) result[0];
		            // fProjectText.setText(((Path) result[0]).toOSString());
		            fProjectText.setText(selectedProject.getName());
		            sProjectName= selectedProject.getName();
                    // SMS 9 Oct 2007
                    //System.out.println("IMPWizardPage:projectBrowseSelectionAdapter:  updating fProject = " + sProjectName);	
                    fProject = selectedProject;		//getProjectBasedOnNameField();  // probably wrong!
		        } else {
                    // SMS 9 Oct 2007
                    //System.out.println("IMPWizardPage:projectBrowseSelectionAdapter:  not updating fProject (or other project data)");	
		        }
		    }
		}
    }
    
    
    /**
     * Add a ViewerFilter to a given dialog.
     * 
     * The viewer filter that is added is the one provided by
     * a call to getViewerFilterForProjects(), which returns
     * (in effect) a default viewer filter for IMP wizard pages.
     * Wizard pages that require a different viewer filter can
     * override this method or that one, as seems most appropriate.
     * Wizard pages that wish to omit viewer filtering can override
     * this method so that it has no effect.
     * 
     * @param dialog 	The dialog to which the ViewerFilter
     * 					is to be added
     */
    protected void addFilterToDialog(ListSelectionDialog dialog) {
        dialog.addFilter(getViewerFilterForProjects());
    }
    
    /**
     * Add a SelectionValidator to the given dialog.
     * 
     * This method does nothing, on the assumption that viewer
     * filtering will eliminate any potentially invalid selections.
     * Wizard pages that wish to enable selection validation can
     * override this method so that it has the desired effect.
     * The particular selection validator to be used in that case
     * can be specified in the overriding method.  Alternatively,
     * the overriding mehtod can call getSelectionValidatorForProjects()
     * (and that method can be overridden as needed to return the
     * appropriate validator).
     * 
     * @param dialog	The dialog to which the SelectionValidator
     * 					is to be added.
     */
    protected void addValidatorToDialog(ListSelectionDialog dialog) {
        dialog.addValdator(getSelectionValidatorForProjects());
    }

    

    /**
     * Returns a ViewerFilter for filtering views of projects
     * opened by this wizard page.  In effect this is the default
     * project-viewer filter for IMP wizard pages.  The viewer filter
     * returned passes IDE projects, on the assumption that this
     * will be the most common kind of filtering needed for these
     * pages.  Wizard pages that require other filtering criteria
     * should override this method.
     * 
     * The filter has one anticipated use, that is, in filtering
     * the values that appear in a project selection dialog.  As
     * a result, it is not strictly necessary, as the desired viewer
     * can be constructed in place of a call to this method.  This
     * method is provided mainly to keep the treatment of viewer
     * filters consistent with that of selection validators.  (And
     * maybe it will be of some unanticipated use someday.)
     * 
     * @return	a ViewerFilter for filtering views of projects
     * 			opened by this wizard page
     */
    protected ViewerFilter getViewerFilterForProjects() {
    	return new ViewerFilterForIDEProjects();
    }
    

    
	/**
	 * Return a ISelectionValidator to use in validating projects
	 * selected or specified on this page.  In effect this is the
	 * default project-validator for IMP wizard pages.  The validator
	 * accepts IDE projects, on the assumption that this will be the
	 * most common kind of valid project for these wizard pages.
	 * Wizard pages that require other validation criteria should
	 * override this method.
	 * 
	 * Note that this validator has two potential uses.  One is by
	 * the page in validating values assigned to a project field. 
	 * The other is by project-selection dialogs opened by the page,
	 * in validating values selected in those dialogs.  This method
	 * provides a single source of validators to help assure their
	 * consistency across those roles.
	 * 
	 * Note that, if desired, different validators can be used for
	 * field values on the page and selections in a dialog.  The
	 * validator to be used on pages can be set by overriding this
	 * method.  A different validator to be used in selection dialogs
	 * can be set by overriding addValidatorToDialog().
	 * 
	 * @return	A project-selection validator to use on this page.
	 */
	protected ISelectionValidator getSelectionValidatorForProjects() {
		return new SelectionValidatorForIDEProjects();
	}


    /**
	 * Replaces the current list of validators for projects with
	 * a new one.  If the given validator is not null then it is
	 * added to the new list.
	 * 
	 * Assumes that there is just one project-valued field.
     * 
     * @param validator A selection validator (may be null)
     */
    public void setSelectionValidatorForProjects(ISelectionValidator validator) {
    	projectValidators = new ArrayList();
    	if (validator != null)
    		projectValidators.add(validator);
    }
    
    
    /**
     * Adds a given validator to the current list of validators.
	 * Creates the list if it does not already exist.  If the
	 * given validator is null then no validator is added (but
	 * this is not an error).
     * 
     * @param validator		The validator to be added
     */
    public void addSelectionValdatorForProjects(ISelectionValidator validator) {
    	if (projectValidators == null) {
    		projectValidators = new ArrayList();
    	}
    	projectValidators.add(validator);
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
			// (but how can this be when fProjectText has a non-zero length???)
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
    
    
    // SMS 23 Nov 2007
    // Rewritten to make use of dynamically determined
    // validators for the project field
    protected void dialogChanged() {
        setErrorMessage(null);
        if (fSkip) {
            setPageComplete(true);
            return;
        }
        String errorMessage = validateProjectField();
        if (errorMessage != null && errorMessage.length() > 0) {
        	setPageComplete(false);
        	setErrorMessage(errorMessage);
        	return;
        }
        WizardPageField field= getUncompletedField();
        if (field != null) {
            setErrorMessage("Please provide a value for the required field \"" + field.fLabel + "\"");
            setPageComplete(false);
            return;
        }
        setPageComplete(true);
    }
    
    
    protected String validateProjectField()
    {
      IProject project = getProjectBasedOnNameField();
      if (project == null) {
          setPageComplete(false);
          return "Please select a project";
      }
      String errorMessage = null;
      for (int i = 0; i < projectValidators.size(); i++) {
    	  errorMessage = projectValidators.get(i).isValid(project);
    	  if (errorMessage == null || errorMessage.length() == 0)
    		  continue;
    	  return errorMessage;
      }
      return null;
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
    	return (language == null || language.length() == 0) ?
    		null :
    		Character.toUpperCase(language.charAt(0)) + language.substring(1);
    }

    protected String lowerCaseFirst(String s) {
        return (s == null || s.length() == 0) ?
        	null :
        	Character.toLowerCase(s.charAt(0)) + s.substring(1);
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
	                	// SMS 2 Mar 2008:  we generally do that now, but the wizard page doesn't necessarily
	                	// have relevant values; these links may sometimes amount to a way of filling in default
	                	// values
	                	
	                    String basedOnQualName= basedOn;
	                    String basedOnTypeName= basedOn.substring(basedOnQualName.lastIndexOf('.') + 1);
	                    String superClassName= "";
	                    String interfaceName = "";

	                    IJavaProject javaProject= JavaCore.create(getProjectOfRecord());
	                    IType basedOnType = javaProject.findType(basedOnQualName);
	                    boolean isInterface = false;
	                    if (basedOnType != null) {
	                    	isInterface = basedOnType.isInterface();
	                    }
	                    
	                    if (isInterface) {
	                    	// Assign the interface name
	                    	interfaceName = basedOnQualName;
	                    	
	                    	// Attempt to determine a superclass name
	                    	// First contrive a service name based on the interface name
	                    	String serviceName = null;
	                    	if (basedOnTypeName.startsWith("I") && Character.isUpperCase(basedOnTypeName.charAt(1)))
	                    	{   // Assume that the interface name begins with an "I" that won't be found
	                    		// in the service name
	                    		serviceName = basedOnTypeName.substring(1);
	                    	} else
	                    		// Just use the basedOnTypeName
	                    		serviceName = basedOnTypeName;
	                    	
	                    	// Now look for a base class with a name that includes that service name
	                	    Bundle irb = Platform.getBundle("org.eclipse.imp.runtime");
	                    	if (irb != null) {
		                    	Enumeration entries = irb.getEntryPaths("/src/org/eclipse/imp/services/base/");
	                    		// Look for class names that includes to the service name; might be
		                    	// more than one, so will make a best guess at the correct one
	                    		String entry = null;
	                    		String className = null;
	                    		// Get candidate class names
	                    		List<String> candidateNames = new ArrayList();
	                    		while (entries.hasMoreElements()) {
		                    		entry = (String) entries.nextElement();
		                    		int lastIndexOfSlash = entry.lastIndexOf("/");
		                    		int classNameStart = lastIndexOfSlash > 0 ? lastIndexOfSlash+1 : 0;
		                    		className = entry.substring(classNameStart);

	                    			if (className.indexOf(serviceName) > -1) 
	                    				candidateNames.add(className);
	                    		}
	                    		// Select best candidate class name, based on length
	                    		className = null;
	                    		for (String s:  candidateNames) {
	                    			if (className == null) {
	                    				className = s;
	                    				continue;
	                    			}
	                    			if (s.length() < className.length())
	                    				className = s;
	                    		}
	                    		// Build qualified superclass name
                    			if (className != null) {
                    				String pakageName = "org.eclipse.imp.services.base.";
                    				int indexOfDot = className.indexOf('.');
                    				if (indexOfDot > 0)
                    					superClassName = pakageName + className.substring(0, indexOfDot);
                    				else
                    					superClassName = pakageName + className;
                    			}
	                    	}
	                    } else {
	                    	// The basedOnQualName is the superclass name and there is no interface
	                    	superClassName = basedOnQualName;
	                    	interfaceName = "";
	                    }
	                    openClassDialog(fComponentID, interfaceName, superClassName, text);
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


    protected WizardDialog openClassDialog(String componentID, String interfaceQualName, String superClassName, Text text) {
        try	 {
            String intfName= interfaceQualName.substring(interfaceQualName.lastIndexOf('.') + 1);
            IJavaProject javaProject= JavaCore.create(getProjectOfRecord());
            
            if (javaProject == null) {
        		ErrorHandler.reportError("Java project is null", true);
        		return null;
            }
            
            // RMF 7/5/2005 - If the project doesn't yet have the necessary plug-in
            // dependency for this reference to be satisfiable, an error ensues.
            
            if (interfaceQualName != null && interfaceQualName.length() > 0) {
            	if (javaProject.findType(interfaceQualName) == null) {
            		ErrorHandler.reportError("Base interface '" + interfaceQualName
            				+ "' does not exist in project's build path; be sure to add the appropriate plugin to the dependencies.", true);
            		return null;
            		// TODO:  Do we want to continue from this point, or should we just throw an exception?
            	}
            }

            if (superClassName != null && superClassName.length() > 0) {
            	if (javaProject.findType(superClassName) == null) {
                    ErrorHandler.reportError("Base class '" + superClassName
                            + "' does not exist in project's build path; be sure to add the appropriate plugin to the dependencies.", true);
                    return null;
                    // TODO:  Do we want to continue from this point, or should we just throw an exception?
	            }
            }
            
            NewClassCreationWizard wizard= new NewClassCreationWizard();
            wizard.init(Workbench.getInstance(), null);
            WizardDialog dialog= new WizardDialog(null, wizard);
            dialog.create();
            NewClassWizardPage page= (NewClassWizardPage) wizard.getPages()[0];
            
            // TODO RMF Should either fix and use fPackageName (sometimes null at this point) or get rid of it altogether.
            if (fQualClassText == null)
            	throw new ClassNotFoundException("IMPWizardPage.openClassDialog(..):  qualified class name is null");
            //String langPkg= fQualClassText.getText().substring(0, fQualClassText.getText().indexOf('.'));// fPackageName; // Character.toLowerCase(langName.charAt(0)) + langName.substring(1);
            // SMS 1 Mar 2008:  revised on the assumption that wizards can set fQualClassText automatically if not set by user
            String givenPackageName = fQualClassText.getText().substring(0, fQualClassText.getText().lastIndexOf('.'));
            String givenClassName = fQualClassText.getText().substring(fQualClassText.getText().lastIndexOf('.')+1);
            
            page.setSuperClass(superClassName, true);

            ArrayList<String> interfaces= new ArrayList<String>();

            if (interfaceQualName != null && interfaceQualName.length() > 0) {
            	interfaces.add(interfaceQualName);
            	page.setSuperInterfaces(interfaces, true);
            }


            String langName = fLanguageText.getText();
            String langClassName = upperCaseFirst(langName);
            String langPackageName = lowerCaseFirst(langName);
            
            // Compute name of package for new service class
            String servicePackageName = givenPackageName;
            if (servicePackageName == null || servicePackageName.length() == 0) {
            	servicePackageName = "org.eclipse.imp."  + 
            	((langPackageName == null || langPackageName.length() == 0) ? "" : langPackageName) +
            	componentID.substring(componentID.lastIndexOf('.')+1);
            }
            	
            // Compute unqualified name of new service class
            String serviceClassName = givenClassName;
            if (serviceClassName == null || serviceClassName.length() == 0) {
            	serviceClassName = ((langClassName == null || langClassName.length() == 0) ? "" : langClassName);
                if (intfName.charAt(0) == 'I' && Character.isUpperCase(intfName.charAt(1)))
                	serviceClassName = serviceClassName + intfName.substring(1);
                else
                	serviceClassName = serviceClassName + intfName;
            }

            WizardUtilities.createSubFolders(servicePackageName.replace('.', '\\'), getProjectOfRecord(), new NullProgressMonitor());
            
            // SMS 2 Mar 2008:  Setting of srcFolder could be more sophisticated
            // TODO:  set srcFolder with a properly computed value
            IFolder srcFolder= getProjectOfRecord().getFolder("src/");
            IPackageFragmentRoot pkgFragRoot= javaProject.getPackageFragmentRoot(srcFolder);
            IPackageFragment pkgFrag= pkgFragRoot.getPackageFragment(servicePackageName);

            page.setPackageFragmentRoot(pkgFragRoot, true);
            page.setPackageFragment(pkgFrag, true);

            page.setTypeName(serviceClassName, true);
            
            SWTUtil.setDialogSize(dialog, 400, 500);
            if (dialog.open() == WizardDialog.OK) {
                String name= page.getTypeName();
                String pkg= page.getPackageText();
                if (pkg.length() > 0)
                    name= pkg + '.' + name;
                text.setText(name);
                fPackageName= pkg;
            }
            
            return dialog;
        } catch (Exception e) {
            ErrorHandler.reportError("Could not create class implementing " + interfaceQualName, true, e);
        }
        return null;
    }


	private void createPackageBrowseButton(Composite container, WizardPageField field, Text text) {
	    Button button= new Button(container, SWT.PUSH);
	
	    button.setText("Browse...");
	    button.setData(text);
		final Shell shell = container.getShell();
		
	    button.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	            try {
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


	protected void createProjectField(Composite container) {
	    Label label= new Label(container, SWT.NULL);
	
	    label.setText("Project*:");
	    label.setBackground(container.getBackground());
	    label.setToolTipText("Select the plug-in project");
	    fProjectText= new Text(container, SWT.BORDER | SWT.SINGLE);
	    fProjectText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	    // SMS 23 Nov 2007
	    // Add a default validator for the project field.
	    // Add it here since we've just created the field.
	    // Make the default case the most common one we expect
	    // (pages that don't require an IDE project will have to override).
	    setSelectionValidatorForProjects(getSelectionValidatorForProjects());
	    
	    // SMS 9 Oct 2007
	    // Here we've just created the project text, so we can't
	    // expect that it has any value; to find a value, get the
	    // project that is currently selected.
	    // Don't validate it here; validation will occur as part
	    // of validation of whole page in dialogChanged().
	    final IProject project= discoverSelectedProject(); //getProject();
	    
	    if (project != null) {
	    	// Try setting fProject directly rather than waiting for a listener
	        fProjectText.setText(project.getName());
	        fProject = getProjectBasedOnNameField();
	    }
	    Button browseButton= new Button(container, SWT.PUSH);
	
	    browseButton.setText("Browse...");
	    browseButton.addSelectionListener(new ProjectBrowseSelectionListener(project, true, true));
	    fProjectText.addModifyListener(new ProjectTextModifyListener());
	    fProjectText.addFocusListener(new FocusAdapter() {
	        public void focusGained(FocusEvent e) {
	            // Text text = (Text)e.widget;
	    	if (fDescriptionText != null)
	    	    fDescriptionText.setText("Select the project to use for this wizard");
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
                // SMS 26 Nov 2007  re:  bug #296
                String langPkg= language.toLowerCase(); //lowerCaseFirst(language);
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
	public WizardPageField createTextField(Composite container, String fieldCategoryName, String fieldName, String description, String value, String basedOn, boolean isRequired)
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
	    
	    return field;
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
