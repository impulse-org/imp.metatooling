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
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.uide.runtime.RuntimePlugin;

import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.ischema.ISchemaAttribute;
import org.eclipse.pde.internal.core.plugin.PluginElement;

public class NewCompiler extends GeneratedComponentServiceWizard {
    // Need a variant of CodeServiceWizard that doesn't actually create an extension, just generates code...
	// SMS 27 Jul 2006:  This is it ...
	
    protected static String thisWizardName = "New Compiler Wizard";
    protected static String thisWizardDescription = "Wizard for creating a simple compiler";
    
    GeneratedComponentAttribute[] compilerAttributes;
	
    public void addPages() {
    	compilerAttributes = setupAttributes();
        addPages(new GeneratedComponentWizardPage[] { new NewCompilerPage(this) });
    }

    
    class NewCompilerPage extends GeneratedComponentWizardPage {
		public NewCompilerPage(GeneratedComponentWizard owner) {
		    super(owner, RuntimePlugin.UIDE_RUNTIME, "compiler", false, compilerAttributes, thisWizardName, thisWizardDescription);
		}

		protected void createFirstControls(Composite parent) {
		    super.createFirstControls(parent);
		    createClassField(parent);
		}
		
    }


    
    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
                "org.eclipse.uide.runtime" });
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
    	GeneratedComponentWizardPage page= (GeneratedComponentWizardPage) pages[0];
        IProject project= page.getProject();
        Map subs= getStandardSubstitutions();

        // SMS 10 Aug 2006
        // The following are all now part of the standard substitution
        //subs.put("$PARSER_PACKAGE$", fParserPackage); // These should be in the standard substitutions...
        //subs.put("$AST_PACKAGE$", fParserPackage + ".Ast");
        //subs.put("$AST_NODE$", "ASTNode");

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
        
        // SMS 10 Aug 2006
        // We also need to know the name of the ParseController class
        // because the user may have changed that from the default
        String parseControllerClassName = getParseControllerClassName(project);
        subs.remove("$ParseControllerClassName$");
        subs.put("$ParseControllerClassName$", parseControllerClassName);
        
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
        // Get the extension that represents the parser
        IPluginModelBase pluginModel = pages[0].getPluginModel();
        IExtensions extensionsThing = pluginModel.getExtensions();
        IPluginExtension[] extensions = extensionsThing.getExtensions();
        IPluginExtension parserExtension = null;
        for (int i = 0; i < extensions.length; i++) {
        	if(extensions[i].getPoint().equals("org.eclipse.uide.runtime.parser")) {
        		parserExtension = extensions[i];
        		break;
        	}
        }
    	
        // Get the plugin element that represents the class of the parser
        PluginElement parserPluginElement = null;
        if (parserExtension != null) {
        	IPluginObject[] children = parserExtension.getChildren();
        	for (int i = 0; i < children.length; i++) {
        		if(children[i].getName().equals("parser")) {
        			parserPluginElement = (PluginElement) children[i];
        				break;
        		}
        	}
        }
        if (parserPluginElement == null) return null;
 
        // Get the name of the parser package
        IPluginAttribute parserClassAttribute = parserPluginElement.getAttribute("class");
        String parserPackageName = parserClassAttribute.getValue();
        parserPackageName = parserPackageName.substring(0, parserPackageName.lastIndexOf('.'));
    	
        // The ParseController class should be in that package, so look for it there

        // Get the package (fragment) that contains the parser
        IWorkspace workspace = project.getWorkspace();
        IJavaModel javaModel = JavaCore.create(workspace.getRoot());
        IJavaProject javaProject = javaModel.getJavaProject(project.getName());
    	IPackageFragment parserPackage = null;
        try {
        	IPackageFragment[] packageFragments = javaProject.getPackageFragments();
        	for (int i = 0; i < packageFragments.length; i++) {
        		if (packageFragments[i].getElementName().equals(parserPackageName)) {
        			parserPackage = packageFragments[i];
        		}
        	}
        } catch (JavaModelException e) {
        	System.err.println("NewCompiler.getParseControllerClassName(IProject):  JavaModelException getting parser package:  " +
        			"\n\t" + e.getMessage() +
        			"\n\tReturning null");
        	return null;
        }
        if (parserPackage == null) return null;
        
        
        // Check the classes in the parser package for one that represents an IParseController
        // (assume there's just one)
        try {
            ICompilationUnit[] compilationUnits = parserPackage.getCompilationUnits();
            typeSearch:  for (int i = 0; i < compilationUnits.length; i++) {
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
        	return null;
        }
        
        // Didn't find it :-(
        return null;
    }
    
    
}