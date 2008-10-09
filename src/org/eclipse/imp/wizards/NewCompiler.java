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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.extensionsmodel.ImpWorkspaceExtensionsModel;
import org.eclipse.imp.utils.ExtensionPointUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.plugin.PluginElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class NewCompiler extends GeneratedComponentWizard {
    // Need a variant of CodeServiceWizard that doesn't actually create an extension, just generates code...
	// SMS 27 Jul 2006:  This is it ...
	
    protected static final String thisWizardName = "New Compiler Wizard";
    protected static final String thisWizardDescription = "Wizard for creating a simple compiler";
    
    protected static final String componentID = "compiler";
    
    protected String fProblemMarkerID = null;
    
	
    public void addPages() {
    	fWizardAttributes = setupAttributes();
        addPages(new GeneratedComponentWizardPage[] { new NewCompilerPage(this) });
    }

    
    class NewCompilerPage extends GeneratedComponentWizardPage {
		public NewCompilerPage(IMPWizard owner) {
		    super(owner, componentID, false, fWizardAttributes, thisWizardName, thisWizardDescription);
		}

		protected void createFirstControls(Composite parent) {
		    super.createFirstControls(parent, componentID);
		    createClassField(parent, "ClassBrowse");
		}
		
	    protected void createAdditionalControls(Composite parent)
	    {
	    	createTextField(parent, "PoorMansCompiler", "class",
	    		"The qualified name of the compiler class to be generated", 
	    		"", "ClassBrowse", true);
	        
	        createTemplateBrowseField(parent, "PoorMansCompiler");

	        fillColumns(parent, 3);	// skip line on page
	        
	        final Button cb = createBooleanField(parent, "Customize problem\nmarker id?",
	        	"Enable or disable the field to set a custom problem marker id",
	        	"Click to enable or disable the field to set a custom problem marker id",
	        	false, 0);
	        fillColumns(parent, 1);
	        
	    	final WizardPageField problemID = createTextField(parent, "PoorMansCompiler", "Problem\nmarker id",
		    		"The internal identifier used for markers for problems reported by the compiler", 
		    		"", "NoBrowse", true);
	        
	        String problemMarkerID = getProblemMarkerID(fProject);
	        if (problemMarkerID != null)
	        	problemID.setText(problemMarkerID);
	        else
	        	problemID.setText("org.eclipse.core.resources.problemmarker");
	    	problemID.setEnabled(false);
	        fillColumns(parent, 1);

	        // To enable the enabled state of the problem marker id field
	        // to follow the settings of the (enabling) checkbox
			cb.addSelectionListener(new SelectionListener() {
			    public void widgetSelected(SelectionEvent e) {
			    	problemID.setEnabled(cb.getSelection());
			    }
			    public void widgetDefaultSelected(SelectionEvent e) {}
			});
			
	    }
	    
	    
	    private void fillColumns(Composite parent, int numCols) {
	    	for (int i = 0; i < numCols; i++) {
		        Label label= new Label(parent, SWT.NULL);
		        label.setText("");
		        label.setBackground(parent.getBackground());
	    	}
	    	
	    	
	    }
	    
	    public void createControl(Composite parent) {
			super.createControl(parent);
			
            // SMS 28 Jul 2008
            createTemplateBrowseField(parent, fComponentID);
			
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
			    ErrorHandler.reportError("NewCompilerWizardPage.createControl(..):  Internal error", e);
			}
	    }

    }


    
    protected List<String> getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
                "org.eclipse.imp.runtime" });
    }
    
    
    protected void collectCodeParms() {
    	super.collectCodeParms();
    	
    	fProblemMarkerID = pages[0].getField("Problem\nmarker id").getText();
    }
    

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException
    {	
        Map<String,String> subs= getStandardSubstitutions(fProject);

        subs.remove("$COMPILER_CLASS_NAME$");
        subs.put("$COMPILER_CLASS_NAME$", fFullClassName);

        subs.remove("$ParseControllerClassName$");
        String parseControllerClassName = getParseControllerClassName(fProject);
        subs.put("$ParseControllerClassName$", parseControllerClassName);
        
        subs.remove("$PROBLEM_MARKER_ID$");
        subs.put("$PROBLEM_MARKER_ID$", fProblemMarkerID);

        IFile compilerSrc= createFileFromTemplate(fFullClassName + ".java", fTemplateName, fPackageFolder, subs, fProject, mon);

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
    
    
    public String getProblemMarkerID(IProject project)
    {
    	ImpWorkspaceExtensionsModel iwem = null;
    	try {
    		iwem = ExtensionPointEnabler.loadImpExtensionsModel(
	    			(IPluginModel)pages[0].getPluginModel(), project);
    	} catch (CoreException e) {
		    ErrorHandler.reportError("CoreException getting problem marker id; returning null", e);
    	}

	    IPluginExtension[] extensions = iwem.getExtensions().getExtensions();
        List<IPluginExtension> markerExtensions =  new ArrayList<IPluginExtension> ();
        
        for (int i = 0; i < extensions.length; i++) {
        	if(extensions[i].getPoint().equals("org.eclipse.core.resources.markers")) {
        		markerExtensions.add(extensions[i]);
        	}
        }
        
        for (IPluginExtension ext:  markerExtensions) {
        	IPluginObject[] children = ext.getChildren();
        	
		    for(int j= 0; j < children.length; j++) {
				if (children[j].getName().equals("super")) {
					String typeValue = ((IPluginElement) children[j]).getAttribute("type").getValue();
					if (typeValue.equals("org.eclipse.core.resources.problemmarker")) {
						return ext.getId().toString();
					}
				}
		    }
        }
        return null;
    }

    
}
