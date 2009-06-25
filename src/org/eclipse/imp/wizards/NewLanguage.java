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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.WizardPlugin;
import org.eclipse.imp.perspective.IMPPerspectiveFactory;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.imp.sanityChecker.SanityNature;
import org.eclipse.imp.ui.dialogs.validators.SelectionValidatorForPluginProjects;
import org.eclipse.imp.utils.StreamUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


/*
 * SMS 27 Mar 2007:
 * 	- Introduced use of $PLUGIN_CLASS$ parameter
 *  - eliminated generation of PreferencesConstants class
 * SMS 23 Apr 2007:
 * 	- Made fProject and fSubs top-level fields so that they can be shared
 *    by gatherCodeParms and performFinish.
 * 	- Added code to check for duplicate plugin ids and confirm whether the
 *    user wants that
 * SMS 19 Jun 2007:  Added $LANG_NAME$ substitution parameter
 * SMS 02 Jul 2007:  Realized that fProject is already defined in
 *  ExtensionPointWizard, so eliminating local declaration
 * SMS 13 Nov 2007:  Change type of wizard page to newly created
 *  type NewLanguageWizardPage
 * SMS 24 Nov 2007:  Modified creation of page to add appropriate selection
 *  validator (new feature of IMPWizardPage)
 */

public class NewLanguage extends CodeServiceWizard {
	// For sharing between methods here, to avoid recomputation
    Map<String, String> fSubs = null;

    public void addPages() {
    	NewLanguageWizardPage page = new NewLanguageWizardPage(this, RuntimePlugin.IMP_RUNTIME, "languageDescription");
    	page.setSelectionValidatorForProjects(new SelectionValidatorForPluginProjects());
        addPages(new ExtensionPointWizardPage[] { page });
    }

    protected List<String> getPluginDependencies() {
        return Arrays.asList(new String[] {
            "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.imp.runtime", "org.eclipse.ui" });
    }

    /*
     * Overrides the method in CodeServiceWizard because fewer parameters
     * are needed and available at this point in the creation of an IDE
     * 
     * @see org.eclipse.imp.wizards.ExtensionPointWizard#collectCodeParms()
     */
    protected void collectCodeParms() {
    	fProject = pages[0].getProjectOfRecord();
    	fProjectName = pages[0].fProjectText.getText();
        fLanguageName= pages[0].fLanguageText.getText();
        
        fClassNamePrefix= Character.toUpperCase(fLanguageName.charAt(0)) + fLanguageName.substring(1);
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
        fSubs = getStandardSubstitutions(fProject);
    	if (fProject == null) {
    		ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
    		fProject = page.getProjectOfRecord();
    	}
        
        String pluginPackage = getPluginPackageName(fProject, null);
        
        String pluginClassFolder= pluginPackage.replace('.', File.separatorChar);
        
        // SMS 23 Mar 2007:  Need to update this part of approach to preferences?
        String prefsPackage= pluginPackage + ".preferences";
        // SMS 9 Oct 2007:  apparently never read
        //String prefsFolder= prefsPackage.replace('.', File.separatorChar);
        fSubs.put("$PREFS_PACKAGE_NAME$", prefsPackage);
    	fSubs.put("$LANG_NAME$", fLanguageName);

        String pluginTemplateName = "plugin.java";
        String pluginClassName = (String)fSubs.get("$PLUGIN_CLASS$");
//        createFileFromTemplate(pluginClassName + ".java", pluginTemplateName, pluginClassFolder, getProjectSourceLocation(fProject), fSubs, fProject, mon);	
        createFileFromTemplate(pluginClassName + ".java", pluginTemplateName, pluginClassFolder, fSubs, fProject, mon);	
        
        // SMS 6 Aug 2007
        // Assure that the bundle activator is recorded in the plugin manifest
    	IFile manifestFile = fProject.getFile("META-INF/MANIFEST.MF");
    	String manifestContents = null;
    	if (manifestFile.exists()) {
    		manifestContents = StreamUtils.readStreamContents(manifestFile.getContents(), manifestFile.getCharset());
    		if (manifestContents.indexOf("Bundle-Activator") < 0) {
    			if (!manifestContents.endsWith("\n"))
    				manifestContents = manifestContents + "\n";
    			manifestContents = manifestContents + 
    				"Bundle-Activator: " + pluginPackage + "." + pluginClassName + "\n";
    		}
    		
    		if (manifestContents.indexOf("Eclipse-LazyStart: true") < 0) {
    			manifestContents = manifestContents + "Eclipse-LazyStart: true\n";
    		}
    	   	// Put the text back into the file
    		manifestFile.setContents(new ByteArrayInputStream(manifestContents.getBytes()), true, true, null);
    	}
    	new SanityNature().addToProject(fProject);
    }

    protected String[] getFilesThatCouldBeClobbered() {
    	try {
        	IFile manifestFile = fProject.getFile("META-INF/MANIFEST.MF");
    	   	if (manifestFile.exists()) {
        		String manifestContents = StreamUtils.readStreamContents(manifestFile.getContents(), manifestFile.getCharset());
        		if (manifestContents.indexOf("Bundle-Activator") >= 0) {
        			return new String[] { "Existing bundle activator class" };
        		}
    		}
    	} catch (CoreException e) {
    	}
    	return new String[0];
    }
    
    /**
     * Check whether it's okay for the files to be generated to clobber
     * any existing files--in this case, a bundle activator class.
     * 
     * The default implementation of this method is overridden here
     * in order to simplify the method of checking for overwriting.
     * 
     * @param files		Nominally the names of files that would be clobbered by
     * 					files to be generated, but here any non-empty array
     * 					is taken as an indication that there exists some bundle
     * 					activator class
     * @return			True if there are no files that would be clobbered
     * 					or if the users presses OK; false if there are
     * 					files and the user presses CANCEL
     */
    protected boolean okToClobberFiles(String[] files) {
    	if (files.length == 0)
    		return true;
    	String message = "This project already has a bundle activator class; do you want to overwrite it?\n";
    	boolean askUser = true;
    	Shell parent = this.getShell();
    	MessageBox messageBox = new MessageBox(parent, (SWT.CANCEL | SWT.OK));
    	messageBox.setMessage(message);
    	int result = messageBox.open();
    	if (result == SWT.CANCEL)
    		return false;
    	return true;
    }

    // SMS 23 Apr 2007
    // Here we're concerned with whether the plugin id for the project
    // is a duplicate--Eclipse allows that, but the presence of duplicate
    // plugin ids can cause the Universal Editor to dispatch the wrong
    // editor on a file, so we want to check whether the user really wants
    // to define a language in this plugin.
    public boolean performFinish() {
    	if (fProject == null || fSubs == null) {
    		ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
    		fProject = page.getProjectOfRecord();
            fSubs = getStandardSubstitutions(fProject);
    	}

        String pluginId = fSubs.get("$PLUGIN_ID$");
        if (pluginId != null) {
        	List<String> pluginIds = getPluginIds();
        	if ((pluginIds.size() > 1) && (pluginIds.indexOf(pluginId) != pluginIds.lastIndexOf(pluginId))) {
        		// This project's plugin id duplicates that of another project
        		if (!okToDuplicatePluginIds(pluginId)) {
        			return false;
        		}
        	}
        }
        maybeSwitchPerspective();
    	return super.performFinish();
    }

    private void maybeSwitchPerspective() {
        final IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        String curPerspective= window.getActivePage().getPerspective().getId();

        if (!curPerspective.equals(IMPPerspectiveFactory.IMP_PERSPECTIVE_ID)) {
            try {
                window.run(false, false, new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        boolean answer= MessageDialog.openConfirm(window.getShell(), "Switch perspective?", "Would you like to switch to the IMP IDE Building perspective now?");

                        if (answer) {
                            IPerspectiveDescriptor perspective= PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(IMPPerspectiveFactory.IMP_PERSPECTIVE_ID);

                            window.getActivePage().setPerspective(perspective);
                        }
                    }
                });
            } catch (InvocationTargetException e) {
                WizardPlugin.getInstance().logException("Error posting switch-perspective dialog", e);
            } catch (InterruptedException e) {
                WizardPlugin.getInstance().logException("Error posting switch-perspective dialog", e);
            }
        }
    }

    public List<String> getPluginIds() {
    	List<String> result = new ArrayList<String>();
    	
		PluginModelManager pmm = PDECore.getDefault().getModelManager();
		IPluginModelBase[] wsPlugins= pmm.getWorkspaceModels();
		
		for(int i= 0; i < wsPlugins.length; i++) {
		    IPluginModelBase wsPlugin= wsPlugins[i];
		    IPluginBase pmBase = wsPlugin.getPluginBase();
		    if (pmBase == null) continue;
		    String id = pmBase.getId();
		    if (id == null) continue;
		    result.add(id);
		}
		return result;
	}

    protected boolean okToDuplicatePluginIds(String pluginId) {
    	String message = "This project has id = '" + pluginId + "', which duplicates that of another project.\n" +
    					 "Duplicate plugin ids can lead to errors in the enabling of new language services;\n" +
    					 "do you wish to continue to define your language in this project?";
    	Shell parent = this.getShell();
    	MessageBox messageBox = new MessageBox(parent, (SWT.OK | SWT.CANCEL));
    	messageBox.setMessage(message);
    	int result = messageBox.open();
    	if (result == SWT.CANCEL)
    		return false;
    	return true;
    }
    
}