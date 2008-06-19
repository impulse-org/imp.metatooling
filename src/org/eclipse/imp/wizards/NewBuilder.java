/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

/**
 * 
 */
package org.eclipse.imp.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class NewBuilder extends CodeServiceWizard
{
    private boolean fAddSMAPSupport;
    
    // SMS 20 Mar 2007:  for holding the entered value of the builder id
    private String fBuilderExtensionId = null;
    private String fBuilderExtensionName = null;
    private String fBuilderExtensionClass = null;
    
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new BuilderWizardPage(this) });
    }

    protected List getPluginDependencies() {
		return Arrays.asList(new String[] {
			"org.eclipse.core.runtime", "org.eclipse.core.resources",
			"org.eclipse.imp.runtime", "org.eclipse.platform.source", "org.eclipse.imp.smapifier" });
    }

    @Override
    protected void collectCodeParms() {
        super.collectCodeParms();
        fAddSMAPSupport= ((BuilderWizardPage) pages[0]).fAddSMAPSupport;
        fBuilderExtensionId = ((BuilderWizardPage) pages[0]).getExtensionID();
        fBuilderExtensionName = ((BuilderWizardPage) pages[0]).getExtensionName();
        fBuilderExtensionClass = ((BuilderWizardPage) pages[0]).getExtensionClass();
        
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
		"import org.eclipse.imp.smapifier.builder.SmapiProjectNature;\n";

    
    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {

        Map<String,String> subs= getStandardSubstitutions(fProject);

        subs.put("$PARSER_PKG$", fParserPackage);	
        
        subs.put("$BUILDER_ID$", fBuilderExtensionId);
        // SMS 28 Mar 2007:  to get builder-specific id for problems markers
        // (to better accommodate multiple builders per language)
        subs.put("$PROBLEM_ID$", fBuilderExtensionId + ".problem");
        
        
        ExtensionPointEnabler.enable(fProject, "org.eclipse.core.resources", "natures", new String[][] {
                // RMF 10/18/2006: The nature ID should NOT have the plugin ID as a prefix (it's implicit)
                { "extension:id", "imp.nature" },
                { "extension:name", fLanguageName + " Nature" },
                // SMS 28 Mar 2007:  updated builder id to be a combination of 
                // plugin id plus value obtained from wizard (like it says)
                { "builder:id", subs.get("$PLUGIN_ID$") + "." + fBuilderExtensionId },
                { "runtime:", "" },
    	    	// SMS 9 May 2006:
                { "runtime.run:class", fLanguageName + ".imp.builders." + fClassNamePrefix + "Nature" },
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
        IFile builderSrc= WizardUtilities.createFileFromTemplate(	
        	fFullClassName + ".java", builderTemplateName, fPackageFolder, getProjectSourceLocation(fProject), subs, fProject, mon);
        // SMS 18 May 2006:
        // Note that we generate the Nature class and extension regardless of whether
        // the user has indicated in the wizard that the builder has a nature.
        String natureTemplateName = "nature.java";
        WizardUtilities.createFileFromTemplate(
        	fClassNamePrefix + "Nature.java", natureTemplateName, fPackageFolder, getProjectSourceLocation(fProject), subs, fProject, mon);

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
    	String prefix = fProject.getLocation().toString() + '/' + getProjectSourceLocation(fProject) + fPackageName.replace('.', '/') + '/';
		return new String[] {prefix + fFullClassName + ".java" , prefix + fClassNamePrefix + "Nature.java" };
    }
    
    
    
    /**
     * This method is called when 'Finish' button is pressed in the wizard.
     * We will create an operation and run it using wizard as execution context.
     * 
     * This overrides, and is an adaptation of, the corresponding method in
     * ExtensionPointWizard.  We need to override it here because the super
     * method both does the enabling in a thread and uses an enable method that
     * refers back to the wizard page.  That leads to an access of the extension
     * id field on the page from a thread that is not the thread that created the
     * page.  In turn that leads to an SWTException for invalid thread access.
     * So here we have to either not fork a separate thread or use the version
     * of the enable method that doesn't rely on the wizard page.  I've chosen
     * the latter approach since it may
     */
    public boolean performFinish() {
		collectCodeParms();
    	if (!okToClobberFiles(getFilesThatCouldBeClobbered()))
    		return false;
    	
		IRunnableWithProgress op= new IRunnableWithProgress() {
		    public void run(IProgressMonitor monitor) throws InvocationTargetException {
			IWorkspaceRunnable wsop= new IWorkspaceRunnable() {
			    public void run(IProgressMonitor monitor) throws CoreException {
				try {
				    for(int n= 0; n < pages.length; n++) {
					ExtensionPointWizardPage page= pages[n];

					if (!page.hasBeenSkipped() && page.fSchema != null)
//					    ExtensionPointEnabler.enable(page, false, monitor);
						ExtensionPointEnabler.enable(
							fProject, "org.eclipse.core.resources", "builders",
							new String[][] {
			                { "extension:id", fBuilderExtensionId },
			                { "extension:name", fBuilderExtensionName },
			                { "builder:", "" },
			                { "builder.run:class", fBuilderExtensionClass },
			                { "builder.run.parameter:", "" },
			                { "builder.run.parameter:name", "foo" },
			                { "builder.run.parameter:value", "bar" }
			        		},
							true, getPluginDependencies(), monitor);
				    }
				    generateCodeStubs(monitor);
				} catch (Exception e) {
				    ErrorHandler.reportError("Could not add extension points", e);
				} finally {
				    monitor.done();
				}
			    }
			};
			try {
			    ResourcesPlugin.getWorkspace().run(wsop, monitor);
			} catch (Exception e) {
			    ErrorHandler.reportError("Could not add extension points", e);
			}
		    }
		};
		try {
		    getContainer().run(true, false, op);
		} catch (InvocationTargetException e) {
		    Throwable realException= e.getTargetException();
		    ErrorHandler.reportError("Error", realException);
		    return false;
		} catch (InterruptedException e) {
		    return false;
		}
		return true;
    }

    
    
}	