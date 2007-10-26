/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.plugin.PluginElement;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.imp.utils.ExtensionPointUtils;

public class NewCompiler extends GeneratedComponentWizard {
    // Need a variant of CodeServiceWizard that doesn't actually create an extension, just generates code...
	// SMS 27 Jul 2006:  This is it ...
	
    protected static final String thisWizardName = "New Compiler Wizard";
    protected static final String thisWizardDescription = "Wizard for creating a simple compiler";
    
    protected static final String componentID = "compiler";
    
	
    public void addPages() {
    	fWizardAttributes = setupAttributes();
        addPages(new GeneratedComponentWizardPage[] { new NewCompilerPage(this) });
    }

    
    class NewCompilerPage extends GeneratedComponentWizardPage {
		public NewCompilerPage(GeneratedComponentWizard owner) {
		    super(owner, componentID, false, fWizardAttributes, thisWizardName, thisWizardDescription);
		}

		protected void createFirstControls(Composite parent) {
		    super.createFirstControls(parent, componentID);
		    createClassField(parent, "ClassBrowse");
		}
		
	    protected void createAdditionalControls(Composite parent) {
	    	createTextField(parent, "PoorMansCompiler", "class",
	    		"The qualified name of the compiler class to be generated", 
	    		"", "ClassBrowse", true);    	       	
	    }
	    
	    public void createControl(Composite parent) {
			super.createControl(parent);
			// SMS 9 Oct 2007 
			//discoverProjectLanguage();
			try {
				// SMS 10 May 2007
				// setEnabled was called with "false"; I don't know why,
				// but I want an enabled field (and enabling it here
				// hasn't seemed to cause any problems)	
				fQualClassText.setEnabled(true);
				
				// Set the class name based on the language name
				fLanguageText.addModifyListener(new ModifyListener() {
	                public void modifyText(ModifyEvent e) {	
	                    setClassByLanguage();
	                }
	            });
			} catch (Exception e) {
			    ErrorHandler.reportError("NewParserWrapperWizardPage.createControl(..):  Internal error, extension point schema may have changed", e);
			}
	    }

    }


    
    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
                "org.eclipse.imp.runtime" });
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException
    {	
        Map<String,String> subs= getStandardSubstitutions(fProject);

        subs.remove("$COMPILER_CLASS_NAME$");
        subs.put("$COMPILER_CLASS_NAME$", fFullClassName);

        String parseControllerClassName = getParseControllerClassName(fProject);
        subs.put("$ParseControllerClassName$", parseControllerClassName);
        
        // SMS 21 Jul 2006
        // NOTE:  The template also makes reference to "$CLASS_NAME_PREFIX$ParseController();"
        // which may *not* be the name of the par	se controller if the user has change that
        // from the default.  So some provision should be made to identify the correct name
        // for the parse controller (e.g., adding a field to the new compiler wizard).
        // Otherwise, the generated code will not compile, but that can be easily repaired
        // by the user.
        
        String compilerTemplateName = "compiler.java";
        IFile compilerSrc= createFileFromTemplate(fFullClassName + ".java", compilerTemplateName, fPackageFolder, subs, fProject, mon);

        editFile(mon, compilerSrc);
    }
 

    // SMS 25 Sep 2007
    // Now provided in GeneratedComponentWizard; this override is left here
    // for purposes of testing
    public GeneratedComponentAttribute[] setupAttributes()
    {
    	// Warning:  Returning an array with empty elements may cause problems,
    	// so be sure to only allocate as many elements as there are actual	attributes
    	GeneratedComponentAttribute[] attributes = new GeneratedComponentAttribute[0];

    	// SMS 10 Aug 2006
    	// Ignore these--they're for testing the attribute mechanism.  The compiler
    	// doesn't have any attributes that are specific to it, but it's the only
    	// generated component we have that isn't an extension, so for testing I've
    	// given it some made-up attributes just so see how things work
    	// 
    	/*
		GeneratedComponentAttribute languageAttr = new GeneratedComponentAttribute();
		languageAttr.setName("name");
		languageAttr.setBasedOn("");
		languageAttr.setDescription("What is the compiler's name?");
		languageAttr.setValue(null);
		languageAttr.setUse(ISchemaAttribute.REQUIRED);
		attributes[0] = languageAttr;

		GeneratedComponentAttribute classAttr = new GeneratedComponentAttribute();
		classAttr.setName("quest");
		classAttr.setBasedOn("");
		classAttr.setDescription("What is the compiler's quest?");
		classAttr.setValue(null);
		classAttr.setUse(ISchemaAttribute.REQUIRED);
		attributes[1] = classAttr;

		GeneratedComponentAttribute colorAttr = new GeneratedComponentAttribute();
		colorAttr.setName("color");
		colorAttr.setBasedOn("");
		colorAttr.setDescription("What is the compiler's favourite color?");
		colorAttr.setValue(null);
		colorAttr.setUse(ISchemaAttribute.OPTIONAL);
		attributes[2] = colorAttr;
		*/
    	return attributes;
    }
    
    
    public String getParseControllerClassName(IProject project)
    {
    	IPluginModelBase pluginModelBase = pages[0].getPluginModel();

        // Get the extension that represents the parser
        IPluginExtension parserExtension= ExtensionPointUtils.findExtensionByName("org.eclipse.imp.runtime.parser", pluginModelBase);

        if (parserExtension == null) return null;

        // Get the plugin element that represents the class of the parser
        PluginElement parserPluginElement = ExtensionPointUtils.findElementByName(pluginModelBase, project, "parser", parserExtension);
        if (parserPluginElement == null) {
            parserPluginElement = ExtensionPointUtils.findElementByName(pluginModelBase, project, "parserWrapper", parserExtension);
        }

        if (parserPluginElement == null) return null;
 
        // Get the name of the parser package
        String parserName = parserPluginElement.getAttribute("class").getValue();
        String parserPackageName = parserName.substring(0, parserName.lastIndexOf('.'));

        // The ParseController class should be in that package, so look for it there

        // Get the package (fragment) that contains the parser
    	IPackageFragment parserPackage = ExtensionPointUtils.findPackageByName(project, parserPackageName);

    	if (parserPackage == null) return null;

        // Check the classes in the parser package for one that represents an IParseController
        // (assume there's just one)
        try {
            ICompilationUnit[] compilationUnits = parserPackage.getCompilationUnits();
            for (int i = 0; i < compilationUnits.length; i++) {
            	ICompilationUnit unit = compilationUnits[i];
            	// Get the type(s) declared by this compilation unit
            	IType[] unitTypes = unit.getTypes();
            	for (int j = 0; j < unitTypes.length; j++) {
            		IType type = unitTypes[j];
            		String[] superInterfaceNames = type.getSuperInterfaceNames();
            		for (int k = 0; k < superInterfaceNames.length; k++) {
            			if (superInterfaceNames[k].contains("IParseController")) {
            				// Found it :-)
            				return type.getElementName();
            			}
            		}
            	}
            }
        } catch (JavaModelException e) {
        	System.err.println("NewCompiler.getParseControllerClassName(IProject):  JavaModelException checking for IParseController:  " +
        			"\n\t" + e.getMessage() +
        			"\n\tReturning null");
        }
        
        // Didn't find it :-(
        return null;
    }
}
