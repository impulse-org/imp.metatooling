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
        subs.put("$AST_PACKAGE$", fParserPackage + ".Ast");
        subs.put("$AST_NODE$", "ASTNode");
        // SMS 21 Jul 2006
        // Regarding the above, the parser package is set in the standard
        // substitutions (at least insofar as being set by CodeServiceWiazrd.
        // getStandardSubstitutions()) but the others are not--not sure
        // whether they'll be set anywhere else so as to be in effect here

        // SMS 21 Jul 2006
        // Added (or modified) following to accommodate
        // values provided through wizard by user
        
        WizardPageField field = pages[0].getField("class");
        String qualifiedClassName = field.fValue;
        String className = qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.')+1);
        subs.remove("$COMPILER_CLASS_NAME$");
        subs.put("$COMPILER_CLASS_NAME$", className);
        
        subs.remove("$PACKAGE_NAME$");
        String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
        subs.put("$PACKAGE_NAME$", packageName);
        
        String packageFolder = packageName.replace('.', File.separatorChar);
        
        // SMS 21 Jul 2006
        // NOTE:  The template also makes reference to "$CLASS_NAME_PREFIX$ParseController();"
        // which may *not* be the name of the parese controller if the user has change that
        // from the default.  So some provision should be made to identify the correct name
        // for the parse controller (e.g., adding a field to the new compiler wizard).
        // Otherwise, the generated code will not compile, but that can be easily repaired
        // by the user.
        
        //IFile compilerSrc= createFileFromTemplate(fClassName + "Compiler.java", "compiler.tmpl", fPackageFolder, subs, project, mon);
        IFile compilerSrc= createFileFromTemplate(className + ".java", "compiler.tmpl", packageFolder, subs, project, mon);

        editFile(mon, compilerSrc);
    }
}