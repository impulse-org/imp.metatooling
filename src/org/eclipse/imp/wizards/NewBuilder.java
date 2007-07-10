/**
 * 
 */
package org.eclipse.uide.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class NewBuilder extends CodeServiceWizard
{
    private boolean fAddSMAPSupport;
    
    // SMS 20 Mar 2007:  for holding the entered value of the builder id
    private String fBuilderExtensionId = null;
    
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new BuilderWizardPage(this) });
    }

    protected List getPluginDependencies() {
		return Arrays.asList(new String[] {
			"org.eclipse.core.runtime", "org.eclipse.core.resources",
			"org.eclipse.uide.runtime", "com.ibm.watson.smapifier" });
    }

    @Override
    protected void collectCodeParms() {
        super.collectCodeParms();
        fAddSMAPSupport= ((BuilderWizardPage) pages[0]).fAddSMAPSupport;
        // SMS 20 Mar 2007:  for getting the entered value of the builder id:
        fBuilderExtensionId = ((BuilderWizardPage) pages[0]).getExtensionID();
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

        Map<String,String> subs= getStandardSubstitutions(fProject);

        subs.put("$PARSER_PKG$", fParserPackage);	
        
        subs.put("$BUILDER_ID$", fBuilderExtensionId);
        // SMS 28 Mar 2007:  to get builder-specific id for problems markers
        // (to better accommodate multiple builders per language)
        subs.put("$PROBLEM_ID$", fBuilderExtensionId + ".problem");
        
        
        ExtensionPointEnabler.enable(fProject, "org.eclipse.core.resources", "natures", new String[][] {
                // RMF 10/18/2006: The nature ID should NOT have the plugin ID as a prefix (it's implicit)
                { "extension:id", "safari.nature" },
                { "extension:name", fLanguageName + " Nature" },
                // SMS 28 Mar 2007:  updated builder id to be a combination of 
                // plugin id plus value obtained from wizard (like it says)
                { "builder:id", subs.get("$PLUGIN_ID$") + "." + fBuilderExtensionId },
                { "runtime:", "" },
    	    	// SMS 9 May 2006:
                { "runtime.run:class", fLanguageName + ".safari.builders." + fClassNamePrefix + "Nature" },
        		},     
        		false,
        		getPluginDependencies(),
        		mon);
        ExtensionPointEnabler.enable(fProject, "org.eclipse.core.resources", "markers",
    	    new String[][] {
        			// SMS 28 Mar 2007:  id based on parameter
                    { "extension:id",   (String) subs.get("$PROBLEM_ID$")},
                    { "extension:name", fLanguageName + " Error" },
    	    	{ "super:type", "org.eclipse.core.resources.problemmarker" },
        	    },
        		false,
        		getPluginDependencies(),
        		mon);

        subs.remove("$BUILDER_CLASS_NAME$");
        subs.put("$BUILDER_CLASS_NAME$", fFullClassName);
        
        subs.remove("$PACKAGE_NAME$");
        subs.put("$PACKAGE_NAME$", fPackageName);

        subs.put("$IPROJECT_IMPORT$", fAddSMAPSupport ? k_IProject_import : "");
        subs.put("$SMAP_SUPPORT$", fAddSMAPSupport ? k_SMAP_enabler.replaceAll("\\$LANG_EXTEN\\$", fLanguageName) : "");
        subs.put("$SMAPI_IMPORT$", fAddSMAPSupport ? k_SMAP_import : "");

        String builderTemplateName = "builder.java";
        IFile builderSrc= createFileFromTemplate(fFullClassName + ".java", builderTemplateName, fPackageFolder, subs, fProject, mon);
        // SMS 18 May 2006:
        // Note that we generate the Nature class and extension regardless of whether
        // the user has indicated in the wizard that the builder has a nature.
        String natureTemplateName = "nature.java";
        createFileFromTemplate(fClassNamePrefix + "Nature.java", natureTemplateName, fPackageFolder, subs, fProject, mon);

        editFile(mon, builderSrc);
    }
    
    
    /**
     * Return the names of any existing files that would be clobbered by the
     * new files to be generated.
     * 
     * @return	An array of names of existing files that would be clobbered by
     * 			the new files to be generated
     */
    protected String[] getFilesThatCouldBeClobbered() {
    	String prefix = fProject.getLocation().toString() + '/' + getProjectSourceLocation() + fPackageName.replace('.', '/') + '/';
		return new String[] {prefix + fFullClassName + ".java" , prefix + fClassNamePrefix + "Nature.java" };
    }
    
}	