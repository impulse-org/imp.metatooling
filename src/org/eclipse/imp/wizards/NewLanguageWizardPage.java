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

package org.eclipse.imp.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.ui.dialogs.ListSelectionDialog;
import org.eclipse.imp.ui.dialogs.filters.ViewerFilterForPluginProjects;
import org.eclipse.imp.ui.dialogs.validators.SelectionValidatorForPluginProjects;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ISelectionValidator;

/**
 * A wizard page for the New Language wizard.  Not especially different
 * from other ExtensionPointWizardPages, but needs to override the new
 * version of createLangaugeFieldForComponent(..) so as to enable that
 * field.  (The default state for the language field is now disabled,
 * which is appropriate for all wizards with the field except this one.)
 * 
 * @author sutton (Stan Sutton, suttons@us.ibm.com)
 * @since 20071113
 *
 */
public class NewLanguageWizardPage extends ExtensionPointWizardPage {

   public NewLanguageWizardPage(ExtensionPointWizard owner, String pluginID, String pointID) {
    	super(owner, pluginID, pointID, true);

    }
    
    public NewLanguageWizardPage(ExtensionPointWizard owner, String pluginID, String pointID, boolean omitIDName) {
    	super(owner, 0, 1, pluginID, pointID, false);
    }
    
    public NewLanguageWizardPage(ExtensionPointWizard owner, String pluginID, String pointID, boolean omitIDName, boolean local) {
        super(owner, 0, 1, pluginID, pointID, false, local);
    }

    public NewLanguageWizardPage(ExtensionPointWizard owner, int pageNumber, int totalPages, String pluginID, String pointID, boolean isOptional) {
        super(owner, pageNumber, totalPages, pluginID, pointID, isOptional);
    }
	
    /**
     * Overrides the default version of this method so as to do nothing, i.e.,
     * so as to not create a language field.
     * 
     * This method is called "automatically" for all IMP wizard pages.  By default
     * it creates a language field that shows the langauge associated with the
     * selected project.  This is apporpriate for almost all IMP wizards.  The
     * NewLanguage wizard is an exception, since this is the wizard through which
     * the language is associated with the project in the first place.  The wizard
     * page does need a language field (for the naming of the new language), but the
     * field is defined as an attribute in the extension schema and will be created
     * by the mechanism that creates fields for all extension attributes.
     * 
     * @see org.eclipse.imp.wizards.IMPWizardPage#createLanguageFieldForComponent(org.eclipse.swt.widgets.Composite, java.lang.String)
     */
    protected void createLanguageFieldForComponent(Composite parent, String componentID) {
    	// nichts
    }
    
    /**
     * Overrides the default version of this method so as to add a viewer filter
     * for plug-in projects rather than for IDE projects (as in the default case).
     * The language must be defined in a plug-in project, but that project does
     * not become an IDE project until after this wizard is completed.
     * 
     * @see org.eclipse.imp.wizards.IMPWizardPage#addFilterToDialog(org.eclipse.imp.ui.dialogs.ListSelectionDialog)
     */
    protected void addFilterToDialog(ListSelectionDialog dialog) {
        dialog.addFilter(new ViewerFilterForPluginProjects());
    }
    
    /**
     * Overrides the default version of this method so as to add a selection validator
     * for plug-in projects rather than for IDE projects (as in the default case).
     * The language must be defined in a plug-in project, but that project does
     * not become an IDE project until after this wizard is completed.
     * 
     * @see org.eclipse.imp.wizards.IMPWizardPage#getSelectionValidatorForProjects()
     */
	protected ISelectionValidator getSelectionValidatorForProjects() {
		return new SelectionValidatorForPluginProjects();
	}
    
	
	
	/*
	 * Used to track whether org.eclipse.imp.runtime has been added to
	 * the require-bundle of a particular project (so that it can be
	 * removed later if the wizard isn't completed for that project)
	 */
	protected boolean addedRequiredPluginImport = false;
	protected String requiredPluginName = "org.eclipse.imp.runtime";
	protected IProject projectReceivingRequiredPluginImport = null;
	
	
	/**
	 * Opens a class-creation dialog.  The dialog is actually opened in a call to super.openClassDiallg(..).
	 * Here conditions are prepared for that call to succeed.  That ivolves managing the plug-in ipmorts
	 * for the selected project and assuring that the wizard-page field for the class name has a reasonable value.
	 * Managing the required plug-in import involves checking whether it is present in the selected project,
	 * adding it to the selected project if not, removing it from a project to which it was added if the dialog
	 * does not complete, and tracking changes to the selected project.  Assuring that the wizard has a reasonable
	 * name for the class involves providing a default value for the class-name field if the field has not been
	 * set.
	 * 
	 * ASSUMES that this method will be called for one particular class, i.e., the language validator class.
	 * 
	 * @see org.eclipse.imp.wizards.IMPWizardPage#openClassDialog(java.lang.String, java.lang.String, java.lang.String, org.eclipse.swt.widgets.Text)
	 */
    protected WizardDialog openClassDialog(String componentID, String interfaceQualName, String superClassName, Text text)
    {	
    	// SMS 1 Mar 2008:  It might also be a good idea to check whether the language is
    	// known at this point (since the validator will validate with respect to that language),
    	// but the implementation here is robust with respect to the lack of a language name.
    	
    	if (fProjectText != null) {
    		// Add the listener for changes in the setting of the project field
    		ProjectTextModifyListener ptml = new ProjectTextModifyListener();
            fProjectText.addModifyListener(ptml);
            
            // SMS 1 Mar 2008
            // Add IMP runtime to current projects dependencies before looking
            // for the interface on which to base the class
            String projectName = fProjectText.getText();
		    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		    requiredPluginName = "org.eclipse.imp.runtime";
            if (!ExtensionPointEnabler.hasRequiredPluginImport(project, requiredPluginName)) {
			    List<String> requires = new ArrayList();
			    requires.add(requiredPluginName);
			    try {
			    	ExtensionPointEnabler.addRequiredPluginImports(null, project, requires);
			    	addedRequiredPluginImport = true;
			    	projectReceivingRequiredPluginImport = project;
			    } catch (CoreException ce) {
	    		    ErrorHandler.reportError("Unable to add required plug-in import(s) to project = " + project, ce);
			    }
            }
		    
		    // SMS 1 Mar 2008
		    // In the supertype openClassDialog(..) will attempt to make use of the name set
		    // in the class field.  In case the name has not been set there, fabricate one here
		    // Note:  This will make use of the language name, if that has been set; if the
		    // language name has not been set then it will be ignored in forming the qualified
		    // class name for the class to be generated.
		    if (fQualClassText == null) {
		    	fQualClassText = new Text(this.getControl().getParent(), 0);
		    	String languageName = fLanguageText.getText();
		    	String qualifiedClassName = "org.eclipse.imp." +
		    		((languageName == null || languageName.length() == 0) ? "" : languageName + ".") +
		    		"languageDescription.Validator";
		    	fQualClassText.setText(qualifiedClassName);
		    }
    	}
    	
    	// Everything had been prepared for really opening the class dialog ...
    	WizardDialog dialog = super.openClassDialog(componentID, interfaceQualName, superClassName, text);
    	
    	if (dialog != null) {
	    	if (dialog.getReturnCode() == Window.CANCEL) {
	    		removeRequiredPluginImportIfNecessary();
	    	} else if (dialog.getReturnCode() == Window.OK) {
	    		commitAdditionOfRequiredPluginImport();
	    	}
    	}
    	
    	return dialog;
    }
	

	/**
	 * Listens for changes in the setting of the project in this wizard
	 * and manages the occurrence of the required plugin import accordingly
	 */
    protected final class ProjectTextModifyListener implements ModifyListener {
		public void modifyText(ModifyEvent e) {
		    Text text= (Text) e.widget;
		    String projectName = text.getText();
		    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		    
		    // Remove the plugin import if it was added to the previously selected project
		    removeRequiredPluginImportIfNecessary();

		    // Now add the plugin import to the currently selected project, if it's not already there
            if (!ExtensionPointEnabler.hasRequiredPluginImport(project, requiredPluginName)) {
			    List<String> requires = new ArrayList();
			    requires.add(requiredPluginName);
			    
			    try {
			    	ExtensionPointEnabler.addRequiredPluginImports(null, project, requires);
			    	addedRequiredPluginImport = true;
			    	projectReceivingRequiredPluginImport = project;
			    } catch (CoreException ce) {
			    	System.err.println("NewLanguageWizard.ProjectTextModifyListener.modifyText:  " +
			    			"Something went wrong adding required plug-in org.eclipse.imp.runtime");
			    }
            }
		}
    }
	
    /*
     * Utility routine to undo the addition of the required plugin import to a project's
     * bundle manifest.  Operation is applied to the project to which the import was
     * most recently added, if any, as recorded by fields defined elsewhere in this class.
     */
    private void removeRequiredPluginImportIfNecessary() {
	    if (addedRequiredPluginImport && requiredPluginName != null) {
	    	ExtensionPointEnabler.removeRequiredPluginImport(projectReceivingRequiredPluginImport, requiredPluginName);
	    	addedRequiredPluginImport = false;
	    	projectReceivingRequiredPluginImport = null;
	    }
    }
    
    
    private void commitAdditionOfRequiredPluginImport() {
    	addedRequiredPluginImport = false;
    	projectReceivingRequiredPluginImport = null;
    }
    
    
	
}
