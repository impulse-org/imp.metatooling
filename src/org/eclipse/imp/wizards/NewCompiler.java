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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.uide.runtime.RuntimePlugin;

public class NewCompiler extends CodeServiceWizard {
    // Need a variant of CodeServiceWizard that doesn't actually create an extension, just generates code...
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new NewCompilerPage(this) });
    }

    class NewCompilerPage extends ExtensionPointWizardPage {
	public NewCompilerPage(ExtensionPointWizard owner) {
	    super(owner, RuntimePlugin.UIDE_RUNTIME, "compiler", false);
	}

	protected void createFirstControls(Composite parent) {
	    createLanguageFieldForPlatformSchema(parent);
	    WizardPageField field= new WizardPageField(null, "class", "Class:", "Compiler", 0, true, "Name of the compiler implementation class");
	    Text classText= createLabelTextBrowse(parent, field, "");

	    classText.setData(field);
	    fFields.add(field);
	}
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
                "org.eclipse.uide.runtime" });
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
        ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
        IProject project= page.getProject();
        Map subs= getStandardSubstitutions();

        subs.put("$PARSER_PACKAGE$", fParserPackage); // These should be in the standard substitutions...
        subs.put("$AST_PACKAGE$", fParserPackage);

        IFile compilerSrc= createFileFromTemplate(fClassName + "Compiler.java", "compiler.tmpl", fPackageFolder, subs, project, mon);

        editFile(mon, compilerSrc);
    }
}