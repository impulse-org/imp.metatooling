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
                // SMS 9 May 2006:
                // Added sProjectName to the following (makes this reference consistent with the
                // builder id as specified)
                { "builder:id", project.getName() + "." + fLanguageName + ".safari.builder" },
    	    { "runtime:", "" },
    	    	// SMS 9 May 2006:
    	    	// Added "builders" after ".safari." and changed fLanguageName (where it occurred
    	    	// before "Nature") to fClassName (as being more appropriate for a class)
    	    	// Note:  fClassName isn't the whole class name; it's really more of a language-
    	    	// specific prefix for naming various classes relating to the language
                { "runtime.run:class", fLanguageName + ".safari.builders." + fClassName + "Nature" },
        }, mon);
        ExtensionPointEnabler.enable(project, "org.eclipse.core.resources", "markers",
    	    new String[][] {
                    { "extension:id",   "problem" },
                    { "extension:name", fLanguageName + " Error" },
    	    	{ "super:type", "org.eclipse.core.resources.problemmarker" },
        	    },
        	    mon);
        IFile builderSrc= createFileFromTemplate(fClassName + "Builder.java", "builder.tmpl", fPackageFolder, subs, project, mon);
        // SMS 18 May 2006:
        // Note that we generate the Nature class and extension regardless of whether
        // the user has indicated in the wizard that the builder has a nature.
        createFileFromTemplate(fClassName + "Nature.java", "nature.tmpl", fPackageFolder, subs, project, mon);

        editFile(mon, builderSrc);
    }
}