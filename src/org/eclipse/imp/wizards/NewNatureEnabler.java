package org.eclipse.uide.wizards;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.uide.WizardPlugin;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.utils.StreamUtils;
import org.osgi.framework.Bundle;

public class NewNatureEnabler extends Wizard implements INewWizard {
    private static final String START_HERE= "// START_HERE";

    private NewNatureEnablerPage fEnablerPage;

    /**
     * Cached from fEnablerPage field by collectCodeParms to avoid invalid SWT thread access when enabling extension
     */
    private IProject fProject;

    /**
     * Cached from fEnablerPage field by collectCodeParms to avoid invalid SWT thread access when enabling extension
     */
    private String fLangName;

    private String fBuilderPkgName;

    public NewNatureEnabler() {
	super();
	setNeedsProgressMonitor(true);
    }

    public int getPageCount() {
	return 1;
    }

    public void addPages() {
	addPage(fEnablerPage= new NewNatureEnablerPage());
    }

    class NewNatureEnablerPage extends WizardPage {
	IProject fProject;

	public NewNatureEnablerPage() {
	    super("New Nature Enabler", "New Nature Enabler", null);
	    setDescription("Create a new nature enabler pop-up action for your language");
	}

	private final class ProjectTextModifyListener implements ModifyListener {
	    public void modifyText(ModifyEvent e) {
		Text text= (Text) e.widget;

		setProjectByName(text.getText());
		discoverProjectLanguage();
		dialogChanged();
	    }
	}

	private final class FocusDescriptionListener extends FocusAdapter {
	    public void focusGained(FocusEvent e) {
		Text text= (Text) e.widget;
		if (text == NewNatureEnablerPage.this.fProjectText)
		    fDescriptionText.setText("Enter the name of a plug-in project");
		else if (text == NewNatureEnablerPage.this.fLanguageText)
		    fDescriptionText.setText("Enter the name of the language");
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

			    if (project.exists() && project.hasNature("org.eclipse.pde.PluginNature") && project.hasNature("org.eclipse.jdt.core.javanature")) {
				return null;
			    }
			    // TODO Check that this project has a nature and a builder defined
			} catch (Exception e) {
			}
			return "The selected project \"" + selection + "\" is not a plug-in project or is not a Java project.";
		    }
		});
		if (dialog.open() == ContainerSelectionDialog.OK) {
		    Object[] result= dialog.getResult();
		    IProject selectedProject= ResourcesPlugin.getWorkspace().getRoot().getProject(result[0].toString());
		    if (result.length == 1) {
			fProjectText.setText(selectedProject.getName());
		    }
		}
	    }
	}

	protected Text fProjectText;

	protected Text fDescriptionText;

	protected Text fLanguageText;

	public void createControl(Composite parent) {
	    try {
		Composite container= new Composite(parent, SWT.NULL | SWT.BORDER);
		// container.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		GridLayout layout= new GridLayout();
		container.setLayout(layout);
		layout.numColumns= 3;
		layout.verticalSpacing= 9;

		createProjectLabelText(container);
		createLanguageLabelText(container);
		createDescriptionText(container);
		discoverProjectLanguage();
		dialogChanged();
		setControl(container);
		fProjectText.setFocus();
		dialogChanged();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	private void createDescriptionText(Composite container) {
	    fDescriptionText= new Text(container, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
	    fDescriptionText.setBackground(container.getBackground());
	    GridData gd= new GridData(GridData.FILL_BOTH);
	    gd.horizontalSpan= 3;
	    gd.widthHint= 450;
	    fDescriptionText.setLayoutData(gd);
	    fDescriptionText.setEditable(false);
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
			fDescriptionText.setText("Select the plug-in project to add this extension point to");
		}
	    });
	}

	private IProject discoverSelectedProject() {
	    ISelectionService service= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
	    ISelection selection= service.getSelection();
	    IProject project= getProject(selection);

	    if (project != null) {
		fProject= project;
		fProjectText.setText(fProject.getName());
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

	private void setProjectByName(String projName) {
	    fProject= ResourcesPlugin.getWorkspace().getRoot().getProject(projName);
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

	public IProject getProject() {
	    if (fProject == null)
		fProject= discoverSelectedProject();

	    return fProject;
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
		setErrorMessage("\"" + fProject.getName() + "\" is not a plug-in project. Please select a plug-in project to add this extension point to");
		setPageComplete(false);
		return;
	    }
	    if (fLanguageText.getText().length() == 0) {
		setErrorMessage("Please provide a language name.");
		setPageComplete(false);
		return;
	    }
	    setPageComplete(true);
	}

	protected void createLanguageLabelText(Composite parent) {
	    fLanguageText= createLabelText(parent, "language", "The language for which to create a nature enabler");

	    fLanguageText.addModifyListener(new ModifyListener() {
		public void modifyText(ModifyEvent e) {
		    dialogChanged();
		}
	    });
	}

	protected Text createLabelText(Composite container, String name, String description) {
	    Widget labelWidget= null;

	    name+= "*:";

	    Label label= new Label(container, SWT.NULL);
	    label.setText(name);
	    label.setToolTipText(description);
	    labelWidget= label;
	    label.setBackground(container.getBackground());

	    Text text= new Text(container, SWT.BORDER | SWT.SINGLE);
	    labelWidget.setData(text);
	    GridData gd= new GridData(GridData.FILL_HORIZONTAL);
	    gd.horizontalSpan= 1;
	    text.setLayoutData(gd);
	    text.addFocusListener(new FocusDescriptionListener());

	    return text;
	}
    }

    protected Map getStandardSubstitutions() {
	return new HashMap();
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
        "org.eclipse.uide.runtime", "org.eclipse.ui", "org.eclipse.jdt.core" });
    }

    /**
     * Implementers of generateCodeStubs() should override this to collect any
     * necessary information from the fields in the various wizard pages needed
     * to generate code.
     */
    protected void collectCodeParms() {
	fProject= fEnablerPage.fProject;
	fLangName= fEnablerPage.fLanguageText.getText();
	// TODO Should try to find the builder package by looking at the builder extension
	fBuilderPkgName= fLangName + ".safari.builders";
    }

    /**
     * This method is called when 'Finish' button is pressed in the wizard.
     * We will create an operation and run it using wizard as execution context.
     */
    public boolean performFinish() {
	collectCodeParms(); // Do this in the UI thread while the wizard fields are still accessible
	IRunnableWithProgress op= new IRunnableWithProgress() {
	    public void run(IProgressMonitor monitor) throws InvocationTargetException {
		IWorkspaceRunnable wsop= new IWorkspaceRunnable() {
		    public void run(IProgressMonitor monitor) throws CoreException {
			try {
			    addEnablerAction(monitor);
			    generateCodeStubs(monitor);
			} catch (Exception e) {
			    ErrorHandler.reportError("Could not add extension points", e);
			} finally {
			    monitor.done();
			}
		    }
		};
		try {
		    ResourcesPlugin.getWorkspace().run(wsop, monitor);
		} catch (Exception e) {
		    ErrorHandler.reportError("Could not add extension points", e);
		}
	    }
	};
	try {
	    getContainer().run(true, false, op);
	} catch (InvocationTargetException e) {
	    Throwable realException= e.getTargetException();
	    ErrorHandler.reportError("Error", realException);
	    return false;
	} catch (InterruptedException e) {
	    return false;
	}
	return true;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {}

    /**
     * Opens the given file in the appropriate editor for editing.<br>
     * If the file contains a comment "// START_HERE", the cursor will
     * be positioned just after that.
     * @param monitor
     * @param file
     */
    protected void editFile(IProgressMonitor monitor, final IFile file) {
	monitor.setTaskName("Opening file for editing...");
	getShell().getDisplay().asyncExec(new Runnable() {
	    public void run() {
		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
		    IEditorPart editorPart= IDE.openEditor(page, file, true);
		    AbstractTextEditor editor= (AbstractTextEditor) editorPart;
		    IFileEditorInput fileInput= (IFileEditorInput) editorPart.getEditorInput();
		    String contents= StreamUtils.readStreamContents(file.getContents(), file.getCharset());
		    int cursor= contents.indexOf(START_HERE);

		    if (cursor >= 0) {
			TextSelection textSel= new TextSelection(editor.getDocumentProvider().getDocument(fileInput), cursor, START_HERE.length());
			editor.getEditorSite().getSelectionProvider().setSelection(textSel);
		    }
		} catch (PartInitException e) {
		} catch (CoreException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	});
	monitor.worked(1);
    }

    protected IFile createFileFromTemplate(String fileName, String templateName, String folder, Map replacements,
	    IProject project, IProgressMonitor monitor) throws CoreException {
	monitor.setTaskName("Creating " + fileName);

	final IFile file= project.getFile(new Path("src/" + folder + "/" + fileName));
	String templateContents= new String(getTemplateFile(templateName));
	String contents= performSubstitutions(templateContents, replacements);

	if (file.exists()) {
	    file.setContents(new ByteArrayInputStream(contents.getBytes()), true, true, monitor);
	} else {
            createSubFolders("src/" + folder, project, monitor);
	    file.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
	}
//	monitor.worked(1);
	return file;
    }
    
    protected IFile createFile(String fileName, String folder, IProject project, IProgressMonitor monitor) throws CoreException {
	monitor.setTaskName("Creating " + fileName);

	final IFile file= project.getFile(new Path("src/" + folder + "/" + fileName));

	if (!file.exists()) {
            createSubFolders("src/" + folder, project, monitor);
	    file.create(new ByteArrayInputStream("".getBytes()), true, monitor);
	}
//	monitor.worked(1);
	return file;
    }

    protected IFile getFile(String fileName, String folder, IProject project) throws CoreException {
	IFile file= project.getFile(new Path("src/" + folder + "/" + fileName));

	return file;
    }

    protected void createSubFolders(String folder, IProject project, IProgressMonitor monitor) throws CoreException {
        String[] subFolderNames= folder.split("[\\" + File.separator + "\\/]");
        String subFolderStr= "";

        for(int i= 0; i < subFolderNames.length; i++) {
            String childPath= subFolderStr + "/" + subFolderNames[i];
            Path subFolderPath= new Path(childPath);
            IFolder subFolder= project.getFolder(subFolderPath);

            if (!subFolder.exists())
                subFolder.create(true, true, monitor);
            subFolderStr= childPath;
        }
    }

    public static void replace(StringBuffer sb, String target, String substitute) {
	for(int index= sb.indexOf(target); index != -1; index= sb.indexOf(target))
	    sb.replace(index, index + target.length(), substitute);
    }

    protected String performSubstitutions(String contents, Map replacements) {
	StringBuffer buffer= new StringBuffer(contents);

	for(Iterator iter= replacements.keySet().iterator(); iter.hasNext();) {
	    String key= (String) iter.next();
	    String value= (String) replacements.get(key);

	    if (value != null)
		replace(buffer, key, value);
	}
	return buffer.toString();
    }

    protected String getTemplateBundleID() {
	return WizardPlugin.kPluginID;
    }

    protected byte[] getTemplateFile(String fileName) {
	try {
	    Bundle bundle= Platform.getBundle(getTemplateBundleID());
	    URL templateURL= Platform.find(bundle, new Path("/templates/" + fileName));
            if (templateURL == null) {
                ErrorHandler.reportError("Unable to find template file: " + fileName, true);
                return new byte[0];
            }
            URL url= Platform.asLocalURL(templateURL);
	    String path= url.getPath();
	    FileInputStream fis= new FileInputStream(path);
	    DataInputStream is= new DataInputStream(fis);
	    byte bytes[]= new byte[fis.available()];

	    is.readFully(bytes);
	    is.close();
	    fis.close();
	    return bytes;
	} catch (Exception e) {
	    e.printStackTrace();
	    return ("// missing template file: " + fileName).getBytes();
	}
    }

    protected void generateCodeStubs(IProgressMonitor mon) throws CoreException {
      	NewNatureEnablerPage page= (NewNatureEnablerPage) fEnablerPage;
        IProject project= page.getProject();
        Map<String,String> subs= getStandardSubstitutions();

        String language= "leg";
        String natureClassName= upperCaseFirst(language) + "Nature";
        String actionClassName= "EnableNature";

        String actionPkgName= language + ".safari.actions";
        String actionPkgFolder= actionPkgName.replace('.', '/');

        subs.put("$BUILDER_PKG_NAME$", fBuilderPkgName);
        subs.put("$NATURE_CLASS_NAME$", natureClassName);
        subs.put("$PACKAGE_NAME$", actionPkgName);

        createFileFromTemplate(actionClassName + ".java", "natureEnabler.java", actionPkgFolder, subs, project, mon);
    }

    protected static String upperCaseFirst(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }	

    private void addEnablerAction(IProgressMonitor mon) {
	String actionClassName= "EnableNature";

	ExtensionPointEnabler.enable(fProject, "org.eclipse.ui", "popupMenus",
	    new String[][] {
		{ "objectContribution:adaptable", "false" },
		{ "objectContribution:nameFilter", "*" },
		{ "objectContribution:objectClass", "org.eclipse.core.resources.IProject" },
		{ "objectContribution:id", fLangName + ".safari.projectContextMenu" },
		{ "objectContribution.action:class", fLangName + ".safari.actions." + actionClassName },
		{ "objectContribution.action:id", fLangName + ".safari.actions.enableNatureAction" },
		{ "objectContribution.action:label", "Enable " + fLangName + " Builder" },
		{ "objectContribution.action:tooltip", "Enable the " + fLangName + " builder for this project" }
	    },
	mon);
    }
}
