/**
 * 
 */
package org.eclipse.uide.wizards;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class NewBuilder extends CodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new BuilderWizardPage(this) });
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] {
                "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.uide.runtime" });
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
        ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
        IProject project= page.getProject();
        Map subs= getStandardSubstitutions();

        // TODO Should pull the source file-name extension from the language description
        subs.put("$FILE_EXTEN$", fLanguageName);

        ExtensionPointEnabler.enable(project, "org.eclipse.core.resources", "natures", new String[][] {
                { "extension:id",   fLanguageName + ".safari.nature" },
                { "extension:name", fLanguageName + " Nature" },
                { "builder:id", fLanguageName + ".safari.builder" },
    	    { "runtime:", "" },
                { "runtime.run:class", fLanguageName + ".safari." + fLanguageName + "Nature" },
        }, mon);
        ExtensionPointEnabler.enable(project, "org.eclipse.core.resources", "markers",
    	    new String[][] {
                    { "extension:id",   "problem" },
                    { "extension:name", fLanguageName + " Error" },
    	    	{ "super:type", "org.eclipse.core.resources.problemmarker" },
        	    },
        	    mon);
        IFile builderSrc= createFileFromTemplate(fClassName + "Builder.java", "builder.tmpl", fPackageFolder, subs, project, mon);
        createFileFromTemplate(fClassName + "Nature.java", "nature.tmpl", fPackageFolder, subs, project, mon);

        editFile(mon, builderSrc);
    }
}