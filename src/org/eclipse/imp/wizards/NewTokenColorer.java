/**
 * 
 */
package org.eclipse.imp.wizards;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.runtime.RuntimePlugin;

public class NewTokenColorer extends CodeServiceWizard {
	
	
    public void addPages() {
	addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.IMP_RUNTIME, "tokenColorer"), });
    }

    protected List getPluginDependencies() {
		return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
			"org.eclipse.uide.runtime", "org.eclipse.ui", "org.eclipse.jface.text", "lpg.runtime"});
    }
    
    
    public void generateCodeStubs(IProgressMonitor mon) throws CoreException
    {
		Map subs= getStandardSubstitutions();
		
		subs.put("$PARSER_PKG$", fParserPackage);
		
		subs.remove("$COLORER_CLASS_NAME$");
		subs.put("$COLORER_CLASS_NAME$", fFullClassName);
	
		subs.remove("$PACKAGE_NAME$");
		subs.put("$PACKAGE_NAME$", fPackageName);
	
		String colorerTemplateName = "colorer_simple.java";
		IFile colorerSrc= createFileFromTemplate(fFullClassName + ".java", colorerTemplateName, fPackageFolder, subs, fProject, mon);
	
		editFile(mon, colorerSrc);
    }
    
    
}
