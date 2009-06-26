/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 *******************************************************************************/

package org.eclipse.imp.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.utils.StreamUtils;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class NewNatureEnabler extends GeneratedComponentWizard implements INewWizard {
    private NewNatureEnablerPage fEnablerPage;

    /**
     * Cached from fEnablerPage field by collectCodeParms to avoid invalid SWT
     * thread access when enabling extension
     */
    private String fLangName;
    private String fBuilderPkgName;
    private String fQualClassName;

    public NewNatureEnabler() {
        super();
        setNeedsProgressMonitor(true);
    }

    public int getPageCount() {
        return 1;
    }

    public void addPages() {
        addPages(new GeneratedComponentWizardPage[] { fEnablerPage= new NewNatureEnablerPage() });
    }

    class NewNatureEnablerPage extends GeneratedComponentWizardPage {
        public NewNatureEnablerPage() {
            super(NewNatureEnabler.this, "NatureEnabler", true, null, "New Nature Enabler",
                    "Creates an action to enable a nature on a project");
            setDescription("Create a new nature enabler pop-up action for your language");
        }

        protected void createAdditionalControls(Composite parent) {
            discoverProjectLanguage();
            createTextField(parent, "newNatureEnabler", "class", "The qualified name of the action class to be generated", "",
                    "ClassBrowse", true);
            try {
                // SMS 10 May 2007
                // setEnabled was called with "false"; I don't know why,
                // but I want an enabled field (and enabling it here
                // hasn't seemed to cause any problems)
                fQualClassText.setEnabled(true);

                // Set the class name based on the language name
                fLanguageText.addModifyListener(new ModifyListener() {
                    public void modifyText(ModifyEvent e) {
                        setClassByLanguage();
                    }
                });
            } catch (Exception e) {
                ErrorHandler.reportError(
                        "NewNatureEnablerWizardPage.createControl(..):  Internal error, extension point schema may have changed", e);
            }
            createTemplateBrowseField(parent, "newNatureEnabler");
        }

        // Overridden so as to customize the class and package name
        protected void setClassByLanguage() {
            try {
                WizardPageField langField= getField("language");
                WizardPageField classField= getField("class");
                String language= langField.getText();

                if (language.length() == 0)
                    return;
                // SMS 27 Nov 2007 re: bug #296
                String langPkg= language.toLowerCase(); // lowerCaseFirst(language);
                String langClass= upperCaseFirst(language);

                fPackageName= langPkg + ".imp.actions";

                if (classField != null) {
                    classField.setText(fPackageName + "." + "Enable" + langClass + "Nature"); // langClass
                                                                                                // +
                                                                                                // upperCaseFirst(fComponentID));
                }

            } catch (Exception e) {
                ErrorHandler.reportError("Cannot set class", e);
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

        public IProject getProject() {
            if (fProject == null)
                fProject= discoverSelectedProject();

            return fProject;
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

    public Map getStandardSubstitutions() {
        return new HashMap();
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources", "org.eclipse.imp.runtime",
                "org.eclipse.ui", "org.eclipse.jdt.core" });
    }

    /**
     * Implementers of generateCodeStubs() should override this to collect any
     * necessary information from the fields in the various wizard pages needed
     * to generate code.
     */
    protected void collectCodeParms() {
        fProject= fEnablerPage.fProject;
        fLangName= fEnablerPage.fLanguageText.getText();
        fQualClassName= fEnablerPage.fQualClassText.getText();
        // TODO Should try to find the builder package by looking at the builder
        // extension
        // SMS 27 Nov 2007 added toLowerCase() re: bug #296
        fBuilderPkgName= fLangName.toLowerCase() + ".imp.builders";
        fTemplateName= fEnablerPage.fTemplateText.getText();
    }

    /**
     * This method is called when 'Finish' button is pressed in the wizard. We
     * will create an operation and run it using wizard as execution context.
     */
    public boolean performFinish() {
        collectCodeParms(); // Do this in the UI thread while the wizard fields
                            // are still accessible
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
     * If the file contains a comment "// START_HERE", the cursor will be
     * positioned just after that.
     * 
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
                        TextSelection textSel= new TextSelection(editor.getDocumentProvider().getDocument(fileInput), cursor, START_HERE
                                .length());
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

    protected void generateCodeStubs(IProgressMonitor mon) throws CoreException {
        NewNatureEnablerPage page= (NewNatureEnablerPage) fEnablerPage;
        IProject project= page.getProject();
        Map<String, String> subs= getStandardSubstitutions();

        String language= fLangName;
        String natureClassName= upperCaseFirst(language) + "Nature";

        String actionClassName= fQualClassName.substring(fQualClassName.lastIndexOf('.') + 1); // "EnableNature";
        String actionPkgName= fQualClassName.substring(0, fQualClassName.lastIndexOf('.')); // language + ".imp.actions";

        String actionPkgFolder= actionPkgName.replace('.', '/');

        subs.put("$BUILDER_PKG_NAME$", fBuilderPkgName);
        subs.put("$NATURE_CLASS_NAME$", natureClassName);
        subs.put("$PACKAGE_NAME$", actionPkgName);
        subs.put("$ENABLER_CLASS_NAME$", actionClassName);

        createFileFromTemplate(actionClassName + ".java", fTemplateName, actionPkgFolder, subs, project, mon);
    }

    protected static String upperCaseFirst(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void addEnablerAction(IProgressMonitor mon) {
        // This one makes the action show up for any project
        ExtensionEnabler.enable(fProject, "org.eclipse.ui", "popupMenus", new String[][] {
                { "objectContribution:adaptable", "false" },
                { "objectContribution:nameFilter", "*" },
                { "objectContribution:objectClass", "org.eclipse.core.resources.IProject" },
                { "objectContribution:id", fLangName + ".imp.projectContextMenu" },
                { "objectContribution.action:class", fQualClassName }, // fLangName + ".imp.actions." + actionClassName },
                { "objectContribution.action:id", fLangName + ".imp.actions.enableNatureAction" },
                { "objectContribution.action:label", "Enable " + fLangName + " Builder" },
                { "objectContribution.action:tooltip", "Enable the " + fLangName + " builder for this project" } }, false,
                getPluginDependencies(), mon);
    }
}
