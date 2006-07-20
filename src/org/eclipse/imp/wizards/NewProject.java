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

public class NewProject extends CodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new NewProjectWizardPage(this) });
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

        subs.put("$NATURE_ID$", fLanguageName + ".nature");

        createFileFromTemplate(fClassName + "ProjectWizard.java", "newProjectWizard.tmpl", fPackageFolder, subs, project, mon);
        IFile projectPageSrc= createFileFromTemplate(fClassName + "ProjectPage.java", "newProjectWizardPage.tmpl", fPackageFolder, subs, project, mon);

        editFile(mon, projectPageSrc);
    }
}