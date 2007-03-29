/**
 * 
 */
package org.eclipse.uide.wizards;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IPlugin;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.WorkspaceModelManager;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundleModel;
import org.eclipse.uide.runtime.RuntimePlugin;

public class NewLanguage extends CodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "languageDescription") });
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] {
            "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.uide.runtime", "org.eclipse.ui" });
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
        ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
        IProject project= page.getProject();
      
        // SMS 23 Mar 2007:  getting substitutions including
        // values related to project plugin
        Map<String,String> subs = getStandardSubstitutions(project);
        String pluginPackage = getPluginPackageName(project, null);
        
        String pluginClassFolder= pluginPackage.replace('.', File.separatorChar);
        
        // SMS 23 Mar 2007:  Need to update this part of approach to preferences?
        String prefsPackage= pluginPackage + ".preferences";
        String prefsFolder= prefsPackage.replace('.', File.separatorChar);
        subs.put("$PREFS_PACKAGE_NAME$", prefsPackage);

        String pluginTemplateName = "plugin.java";
        // SMS 27 Mar 2007:  using parameter for plugin class name
        createFileFromTemplate((String)subs.get("$PLUGIN_CLASS$") + ".java", pluginTemplateName, pluginClassFolder, subs, project, mon);
        
        // SMS 27 Mar 2007 commented out creation of file for preference cache because no longer used
        createFileFromTemplate(fClassName + "PreferenceConstants.java", "prefs_const.tmpl", prefsFolder, subs, project, mon);
    }
}