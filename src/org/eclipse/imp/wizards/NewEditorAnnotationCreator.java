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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.extensionsmodel.ImpWorkspaceExtensionsModel;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class NewEditorAnnotationCreator extends GeneratedComponentWizard {
	
    protected static final String thisWizardName = "New Editor Annotation-Creator Wizard";
    protected static final String thisWizardDescription = "Wizard for creating an annotation creator for use by the editor";
    
    protected static final String componentID = "editorAnnotationCreator";
    protected String fServiceExtensionId = null;
    protected String fServiceExtensionName = null;
    
    protected String fProblemMarkerID = null;
    protected String fMarkerIDFieldName = "Problem\nmarker id";
    
	
    public void addPages() {
    	fWizardAttributes = setupAttributes();
        addPages(new GeneratedComponentWizardPage[] { new NewEditorAnnotationCreatorPage(this) });
    }


    // TODO:  Revisit the dependencies
    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
                "org.eclipse.imp.runtime", "org.eclipse.ui.editors", "org.eclipse.ui.workbench.texteditor" });
    }
    
    
    protected void collectCodeParms() {
    	super.collectCodeParms();
    	
    	fProblemMarkerID = pages[0].getField(fMarkerIDFieldName).getText();
    	
    	fServiceExtensionId = pages[0].getField("language").getText() + "EditorAnnotationCreator";
    	fServiceExtensionName = pages[0].getField("language").getText() + " Editor Annotation Creator";    	
    }
    

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException
    {	
        Map<String,String> subs= getStandardSubstitutions(fProject);

        subs.remove("$EDITOR_ANOTATION_CREATOR_CLASS_NAME$");
        subs.put("$EDITOR_ANOTATION_CREATOR_CLASS_NAME$", fFullClassName);
        
        subs.remove("$PROBLEM_MARKER_ID$");
        subs.put("$PROBLEM_MARKER_ID$", fProblemMarkerID);


        
        
        IFile compilerSrc= createFileFromTemplate(fFullClassName + ".java", fTemplateName, fPackageFolder, subs, fProject, mon);

		ExtensionPointEnabler.enable(
				fProject, "org.eclipse.imp.runtime", "editorAnnotationCreator",
				new String[][] {
						{ "extension:id", fServiceExtensionId },
			            { "extension:name", fServiceExtensionName},
			            { "editorAnnotationCreator:class", fPackageName + "." + fFullClassName },
			            { "editorAnnotationCreator:language", fLanguageName} },
			    false, getPluginDependencies(), new NullProgressMonitor());
        
        editFile(mon, compilerSrc);
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
        List<IPluginExtension> markerExtensions =  new ArrayList<IPluginExtension>();
        
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
    
    
    
    
    class NewEditorAnnotationCreatorPage extends GeneratedComponentWizardPage {
    	
    	public static final String fieldCategoryName = "EditorAnnotationCreator";
    	
		public NewEditorAnnotationCreatorPage(IMPWizard owner) {
		    super(owner, componentID, false, fWizardAttributes, thisWizardName, thisWizardDescription);
		}

		protected void createFirstControls(Composite parent) {
		    super.createFirstControls(parent, componentID);
		    createClassField(parent, "ClassBrowse");
		}
		
	    protected void createAdditionalControls(Composite parent)
	    {
	    	createTextField(parent, fieldCategoryName, "class",
	    		"The qualified name of the annotation-createor class to be generated", 
	    		"", "ClassBrowse", true);
	        
	        createTemplateBrowseField(parent, fieldCategoryName);

	        fillColumns(parent, 3);	// skip line on page
	        
	        final Button cb = createBooleanField(parent, "Customize problem\nmarker id?",
	        	"Enable or disable the field to set a custom problem marker id",
	        	"Click to enable or disable the field to set a custom problem marker id",
	        	false, 0);
	        fillColumns(parent, 1);
	        
	    	final WizardPageField problemID = createTextField(parent, fieldCategoryName, fMarkerIDFieldName,
		    		"A default value for the internal identifier used for created annotations", 
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
    
}
