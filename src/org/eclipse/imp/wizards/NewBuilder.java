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

public class NewBuilder extends CodeServiceWizard {
    private boolean fAddSMAPSupport;

    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new BuilderWizardPage(this) });
    }

    protected List getPluginDependencies() {
	return Arrays.asList(new String[] {
		"org.eclipse.core.runtime", "org.eclipse.core.resources",
		"org.eclipse.uide.runtime" });
    }

    @Override
    protected void collectCodeParms() {
        super.collectCodeParms();
        fAddSMAPSupport= ((BuilderWizardPage) pages[0]).fAddSMAPSupport;
    }

    private static final String k_IProject_import=
	"import org.eclipse.core.resources.IProject;\n";

    private static final String k_SMAP_enabler=
	"\n" +
	"    public void addToProject(IProject project) {\n" +
	"        super.addToProject(project);\n" +
        "        new SmapiProjectNature(\"$LANG_EXTEN$\").addToProject(project);\n" +
        "    };\n";

    private static final String k_SMAP_import=
	"\n" + 
	"import com.ibm.watson.smapifier.builder.SmapiProjectNature;\n";

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
        ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
        IProject project= page.getProject();
        Map subs= getStandardSubstitutions();

        // SMS 19 Jul 2006
        // Commented out subs.put since the TODO is now addressed
        // in the builder template
        // TODO Should pull the source file-name extension from the language description
        //subs.put("$FILE_EXTEN$", fLanguageName);
        
        // SMS 17 Oct 2006
        // Need this now ...
        subs.put("$PARSER_PKG$", fParserPackage);

        ExtensionPointEnabler.enable(project, "org.eclipse.core.resources", "natures", new String[][] {
                // RMF 10/18/2006: The nature ID should NOT have the plugin ID as a prefix (it's implicit)
                { "extension:id",   "safari.nature" },
                { "extension:name", fLanguageName + " Nature" },
                // SMS 9 May 2006:
                // Added sProjectName to the following (makes this reference consistent with the
                // builder id as specified)
                // RMF 10/18/2006: The builder ID really shouldn't have the project name as a prefix.
                // RMF 10/18/2006: On the other hand, the following creates a REFERENCE to the builder
                // ID, and so SHOULD have the plugin ID as a prefix.
                { "builder:id", fLanguageName + ".safari.builder" },
                { "runtime:", "" },
    	    	// SMS 9 May 2006:
    	    	// Added "builders" after ".safari." and changed fLanguageName (where it occurred
    	    	// before "Nature") to fClassName (as being more appropriate for a class)
    	    	// Note:  fClassName isn't the whole class name; it's really more of a language-
    	    	// specific prefix for naming various classes relating to the language
                { "runtime.run:class", fLanguageName + ".safari.builders." + fClassName + "Nature" },
        		}, false, mon);
        ExtensionPointEnabler.enable(project, "org.eclipse.core.resources", "markers",
    	    new String[][] {
                    { "extension:id",   "problem" },
                    { "extension:name", fLanguageName + " Error" },
    	    	{ "super:type", "org.eclipse.core.resources.problemmarker" },
        	    },
        	    false, mon);
        
        // SMS 18 Jul 2006
        // Added (or modified) following to accommodate
        // values provided through wizard by user
        
        WizardPageField field = pages[0].getField("class");
        String qualifiedClassName = field.fValue;
        String className = qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.')+1);
        subs.remove("$BUILDER_CLASS_NAME$");
        subs.put("$BUILDER_CLASS_NAME$", className);
        
        subs.remove("$PACKAGE_NAME$");
        String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
        subs.put("$PACKAGE_NAME$", packageName);
        
        String packageFolder = packageName.replace('.', File.separatorChar);

        // TODO Get file name extension for the following from the Language descriptor
        subs.put("$IPROJECT_IMPORT$", fAddSMAPSupport ? k_IProject_import : "");
        subs.put("$SMAP_SUPPORT$", fAddSMAPSupport ? k_SMAP_enabler.replaceAll("\\$LANG_EXTEN\\$", fLanguageName) : "");
        subs.put("$SMAPI_IMPORT$", fAddSMAPSupport ? k_SMAP_import : "");

        String builderTemplateName = "builder.java";
        IFile builderSrc= createFileFromTemplate(className + ".java", builderTemplateName, packageFolder, subs, project, mon);
        // SMS 18 May 2006:
        // Note that we generate the Nature class and extension regardless of whether
        // the user has indicated in the wizard that the builder has a nature.
        String natureTemplateName = "nature.java";
        createFileFromTemplate(fClassName + "Nature.java", natureTemplateName, packageFolder, subs, project, mon);

        editFile(mon, builderSrc);
    }
}	