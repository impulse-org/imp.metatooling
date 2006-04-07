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
import org.eclipse.uide.runtime.RuntimePlugin;

public class NewTokenColorer extends CodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "tokenColorer"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime", "org.eclipse.ui", "org.eclipse.jface.text", "lpg" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
            ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
            IProject project= page.getProject();
            Map subs= getStandardSubstitutions();

//          { "$PKG_NAME$", fPackageName },
            subs.put("$PARSER_PKG$", fParserPackage);

            IFile colorerSrc= createFileFromTemplate(fClassName + "TokenColorer.java", "colorer.tmpl", fPackageFolder, subs, project, mon);

            editFile(mon, colorerSrc);
	}
    }