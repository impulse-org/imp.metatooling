/**
 * 
 */
package org.eclipse.uide.wizards;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uide.runtime.RuntimePlugin;

public class NewContentProposer extends CodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "contentProposer"), });
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime",
                                            "org.eclipse.core.resources",
                                            "org.eclipse.uide.runtime" 
                                          });
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
    // TODO Auto-generated method stub
        ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
        IProject project= page.getProject();
        Map subs= getStandardSubstitutions();

        subs.put("$PARSER_PKG$", fParserPackage);
        subs.put("$AST_PKG$", fParserPackage + "." + Wizards.astDirectory);
        subs.put("$AST_NODE$", Wizards.astNode);

        // SMS 19 Jul 2006
        // Added (or modified) following so as to accommodate
        // values provided through wizard by user
        
        WizardPageField field = pages[0].getField("class");
        String qualifiedClassName = field.fValue;
        String className = qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.')+1);
        subs.remove("$CONTENT_PROPOSER_CLASS_NAME$");
        subs.put("$CONTENT_PROPOSER_CLASS_NAME$", className);
        
        subs.remove("$PACKAGE_NAME$");
        String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
        subs.put("$PACKAGE_NAME$", packageName);
        String packageFolder = packageName.replace('.', File.separatorChar);

        IFile outlinerSrc= createFileFromTemplate(className      + ".java", "contentProposer.java", packageFolder, subs, project, mon);
 
        editFile(mon, outlinerSrc);
    }
}