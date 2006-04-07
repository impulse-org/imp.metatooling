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
        Map subs= getStandardSubstitutions();
        String pluginPackage= fLanguageName; // + ".safari";
        String pluginClassFolder= pluginPackage.replace('.', File.separatorChar);
        String prefsPackage= pluginPackage + ".preferences";
        String prefsFolder= prefsPackage.replace('.', File.separatorChar);

        subs.put("$PACKAGE_NAME$", pluginPackage);
        subs.put("$PREFS_PACKAGE_NAME$", prefsPackage);

        createFileFromTemplate(fClassName + "Plugin.java", "plugin.tmpl", pluginClassFolder, subs, project, mon);
        createFileFromTemplate(fClassName + "PreferenceCache.java", "prefs_cache.tmpl", prefsFolder, subs, project, mon);
        createFileFromTemplate(fClassName + "PreferenceConstants.java", "prefs_const.tmpl", prefsFolder, subs, project, mon);
    }
}