/**
 * 
 */
package org.eclipse.imp.wizards;

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
    	    "org.eclipse.imp.runtime", "org.eclipse.ui", "org.eclipse.jdt.core", "org.eclipse.jdt.ui" });
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
        ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
        IProject project= page.getProject();
        Map subs= getStandardSubstitutions();

        subs.put("$NATURE_ID$", fLanguageName + ".nature");

        String projectTemplateName = "newProjectWizard.java";
        createFileFromTemplate(fClassNamePrefix + "ProjectWizard.java", projectTemplateName, fPackageFolder, subs, project, mon);
        IFile projectFirstPageSrc= createFileFromTemplate(fClassNamePrefix + "ProjectWizardFirstPage.java", "newProjectWizardFirstPage.java", fPackageFolder, subs, project, mon);
        IFile projectSecondPageSrc= createFileFromTemplate(fClassNamePrefix + "ProjectWizardSecondPage.java", "newProjectWizardSecondPage.java", fPackageFolder, subs, project, mon);

        editFile(mon, projectFirstPageSrc);
    }
}