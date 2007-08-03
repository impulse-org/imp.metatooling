/**
 * 
 */
package org.eclipse.imp.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;


/*
 * SMS 27 Mar 2007:
 * 	- Introduced use of $PLUGIN_CLASS$ parameter
 *  - eliminated generation of PreferencesConstants class
 * SMS 23 Apr 2007:
 * 	- Made fProject and fSubs top-level fields so that
 * 	  they can be shared by gatherCodeParms and performFinish.
 * 	- Added code to check for duplicate plugin ids and confirm
 *    whether the user wants that
 * SMS 19 Jun 2007:  Added $LANG_NAME$ substitution parameter
 * SMS 02 Jul 2007:  Realized that fProject is already defined
 *  in ExtensionPointWizard, so eliminating local declaration
 */

public class NewLanguage extends CodeServiceWizard {
	
	// For sharing between methods here, to avoid recomputation
    Map<String, String> fSubs = null;
	
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.IMP_RUNTIME, "languageDescription") });
    }


    protected List getPluginDependencies() {
        return Arrays.asList(new String[] {
            "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.uide.runtime", "org.eclipse.ui" });
    }

    
    /*
     * Overrides the method in CodeServiceWizard because fewer parameters
     * are needed and available at this point in the creation of an IDE
     * 
     * @see org.eclipse.uide.wizards.ExtensionPointWizard#collectCodeParms()
     */
    protected void collectCodeParms() {
    	fProject = pages[0].getProject();
    	fProjectName = pages[0].fProjectText.getText();
        fLanguageName= pages[0].fLanguageText.getText();
        
        fClassNamePrefix= Character.toUpperCase(fLanguageName.charAt(0)) + fLanguageName.substring(1);
    }

    
    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
        fSubs = getStandardSubstitutions(fProject);
    	if (fProject == null) {
    		ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
    		fProject = page.getProject();
    	}
        
        String pluginPackage = getPluginPackageName(fProject, null);
        
        String pluginClassFolder= pluginPackage.replace('.', File.separatorChar);
        
        // SMS 23 Mar 2007:  Need to update this part of approach to preferences?
        String prefsPackage= pluginPackage + ".preferences";
        String prefsFolder= prefsPackage.replace('.', File.separatorChar);
        fSubs.put("$PREFS_PACKAGE_NAME$", prefsPackage);
    	fSubs.put("$LANG_NAME$", fLanguageName);

        String pluginTemplateName = "plugin.java";
        createFileFromTemplate((String)fSubs.get("$PLUGIN_CLASS$") + ".java", pluginTemplateName, pluginClassFolder, fSubs, fProject, mon);	
    }
    
    
    // SMS 23 Apr 2007
    // Here we're concerned with whether the plugin id for the project
    // is a duplicate--Eclipse allows that, but the presence of duplicate
    // plugin ids can cause the Universal Editor to dispatch the wrong
    // editor on a file, so we want to check whether the user really wants
    // to define a language in this plugin.
    public boolean performFinish()
    {
    	if (fProject == null || fSubs == null) {
    		ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
    		fProject = page.getProject();
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
    	return super.performFinish();
    }
    
    
    public List<String> getPluginIds()
    {
    	List<String> result = new ArrayList();
    	
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
    
    
    protected boolean okToDuplicatePluginIds(String pluginId)
    {
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