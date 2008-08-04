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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewOccurrenceIdentifier extends GeneratedComponentWizard {
	
	// fFullClassName is defined and used in GeneratedComponentWizard
	
    protected static final String thisWizardName = "New Occurrence Identifier";
    protected static final String thisWizardDescription =
    	"This wizard supports the development of a occurrence identifier for occurrence marking";
    protected static final String componentID = "occurrenceIdentifier";
    
    protected NewOccurrenceIdentifierWizardPage thePage = null;
    
    public void addPages() {
    	thePage = new NewOccurrenceIdentifierWizardPage(this);
        addPages(new GeneratedComponentWizardPage[] { thePage } );
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] {
//                "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	        "org.eclipse.imp.runtime",
//    	        "org.eclipse.ui", "org.eclipse.jface.text", 
//                "org.eclipse.ui.editors", "org.eclipse.ui.workbench.texteditor", 
                "lpg.runtime" });
    }
    
    
    protected void collectCodeParms() {
    	fProject = pages[0].getProjectOfRecord();
    	fProjectName = pages[0].fProjectText.getText();
        fLanguageName= pages[0].fLanguageText.getText();
        
        if (pages[0].fTemplateText != null)
        	fTemplateName = pages[0].fTemplateText.getText();
        
        fClassNamePrefix= Character.toUpperCase(fLanguageName.charAt(0)) + fLanguageName.substring(1);
        
        // The following line is different from the method in the superclass;
        // the usual field of "class" is replaced by one called "identifier"
		String qualifiedClassName= pages[0].getField("identifier").fValue;
		fFullClassName = qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.') + 1);
		fPackageName= qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
		fPackageFolder= fPackageName.replace('.', File.separatorChar);
		
        String[] subPkgs= fPackageName.split("\\.");
        StringBuffer buff= new StringBuffer();
        for(int i= 0; i < subPkgs.length-1; i++) {
            if (i > 0) buff.append('.');
            buff.append(subPkgs[i]);
        }
        buff.append(".parser");
        fParserPackage= buff.toString();
    }
    

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException
    {	
    	// Enable the first extension provided through this wizard
    	// (enable the second one later, to accommodate timing issues)
        ExtensionPointEnabler.enable(
        	fProject, "org.eclipse.imp.runtime", "occurrenceIdentifier", 			//"markOccurrences",
        	new String[][] {
        			{ "occurrenceIdentifier:class", fPackageName + "." + fFullClassName },
        			{ "occurrenceIdentifier:language", fLanguageName }
            	    },
    		false,
    		getPluginDependencies(),
    		mon);

    	
        // Define the substitution values for meta-parameters
        // in the templates from which implementations will be
        // generated
        
        Map<String, String> subs= getStandardSubstitutions(fProject);

//        subs.put("$PARSER_PKG$", fParserPackage);
        subs.put("$AST_PKG$", fParserPackage + "." + Wizards.astDirectory);
//        subs.put("$AST_NODE$", Wizards.astNode);
        
        subs.remove("$OCCURRENCE_IDENTIFIER_CLASS_NAME$");
        subs.put("$OCCURRENCE_IDENTIFIER_CLASS_NAME$", fFullClassName);
        
        subs.remove("$PACKAGE_NAME$");
        subs.put("$PACKAGE_NAME$", fPackageName);
        
//        subs.remove("$CLASS_NAME_PREFIX_UPPER$");
//        subs.put("$CLASS_NAME_PREFIX_UPPER$", fClassNamePrefix.toUpperCase());
//        
//        subs.remove("$CLASS_NAME_PREFIX_LOWER$");
//        subs.put("$CLASS_NAME_PREFIX_LOWER$", fClassNamePrefix.toLowerCase());
        
        // Generate the tree-model-identifier implementation, if requested
//        String occurrenceIdentifierTemplateName = "occurrenceIdentifier.java";
        IFile src= WizardUtilities.createFileFromTemplate(
        		fFullClassName + ".java", fTemplateName,
        		fPackageFolder, getProjectSourceLocation(fProject), subs, fProject, mon);
        editFile(mon, src);
    }
 
	    
	    
    class NewOccurrenceIdentifierWizardPage extends GeneratedComponentWizardPage
    {
//    	protected boolean fGenerateOccurrenceIdentifier = true;
//    	protected boolean fGenerateLabelProvider = true;
//    	protected boolean fActivatorAppend = true;
//    	protected boolean fResourcesAppend = true;
    	NewOccurrenceIdentifierWizardPage thePage;
    	
		public NewOccurrenceIdentifierWizardPage(GeneratedComponentWizard owner) {
		    super(owner, componentID, false, fWizardAttributes, thisWizardName, thisWizardDescription);
		    thePage = this;
		}

		protected void createFirstControls(Composite parent) {
		    super.createFirstControls(parent, componentID);
		    createClassField(parent, "ClassBrowse");
		}
		
	    protected void createAdditionalControls(Composite parent)
	    {	
//            // Put some whitespace into the layout
//    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//    	    	
//	    	final Button generateOccurrenceIdentifierIndicator = createBooleanField(
//	    			parent, "Create tree\nmodel identifier?", "Generate a tree-model identifier that will be used as the basis of an outline view",
//		    		"Check this box to cause the wizard to generate an implementation skeleton for a new tree-model identifier", true, 1);
//
//	    	generateOccurrenceIdentifierIndicator.addSelectionListener(new SelectionListener() {
//	    		    public void widgetSelected(SelectionEvent e) {
//	    		    	fGenerateOccurrenceIdentifier = generateOccurrenceIdentifierIndicator.getSelection();
//	    		    	WizardPageField field = thePage.getField("identifier");
//	    		    	if (field != null) {
//	    		    		field.setEnabled(fGenerateOccurrenceIdentifier);
//	    		    	}
//	    		    }
//	    		    public void widgetDefaultSelected(SelectionEvent e) {}
//	    		});
	    	
	    	createTextField(parent, "Occurrence Identifier", "identifier",
	    		"The qualified name of the occurrence-identifier class to be generated", 
	    		"", "ClassBrowse", true);
	    	
	        createTemplateBrowseField(parent, "OccurrenceIdentifier");

//            // Put some whitespace into the layout
//    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//    	    
//	    	final Button generateLabelProviderIndicator = createBooleanField(
//	    			parent, "Create label\nprovider?", "Generate a label-provider that will provide text and image labels for use in the IDE",
//		    		"Check this box to cause the wizard to generate an implementation skeleton for a new label-provider.", true, 1);
//	    	generateLabelProviderIndicator.addSelectionListener(new SelectionListener() {
//	    		    public void widgetSelected(SelectionEvent e) {
//	    		    	fGenerateLabelProvider = generateLabelProviderIndicator.getSelection();
//	    		    	WizardPageField field = thePage.getField("provider");
//	    		    	if (field != null) {
//	    		    		field.setEnabled(fGenerateLabelProvider);
//	    		    	}
//	    		    }
//	    		    public void widgetDefaultSelected(SelectionEvent e) {}
//	    		});
//	    	
//	    	WizardPageField providerField = createTextField(parent, "Label Provider", "provider",
//		    		"The qualified name of the label-provider class to be generated", 
//		    		"", "ClassBrowse", true);
	    		
//            // Put some whitespace into the layout
//    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    	
    	    
//    	    String activatorAppendInfo = 
//    			"Update the plug-in activator class with members related to label management.\n" +
//    			"Not necessary if done previously; regenerating will not clobber earlier updates\n" +
//    			"but can create duplicate members.";
//    	    	
//	    	final Button activatorAppendIndicator = createBooleanField(
//	    		parent, "Update plug-in\nactivator?",
//	    			activatorAppendInfo, activatorAppendInfo,
//	    			true, 1);
//	    	activatorAppendIndicator.addSelectionListener(new SelectionListener() {
//    		    public void widgetSelected(SelectionEvent e) {
//    		    	fActivatorAppend = activatorAppendIndicator.getSelection();
//    		    }
//    		    public void widgetDefaultSelected(SelectionEvent e) {}
//    		});
//	    	
//	    	
//	    	String resourcesAppendInfo = 
//	    		"Update the class used to define String resources for the plug-in, adding constants for default image names.\n" +
//    			"These are referenced by the label-provider.  Not necessary if previously generated (or there is no label provider).\n" +
//    			"Regenerating will not clobber earlier entries but can create duplicate definitions.";
//	    	
//	    	final Button resourcesAppendIndicator = createBooleanField(
//		    		parent, "Update string\nresources?",
//		    		resourcesAppendInfo, resourcesAppendInfo,
//		    		true, 1);
//		    	resourcesAppendIndicator.addSelectionListener(new SelectionListener() {
//	    		    public void widgetSelected(SelectionEvent e) {
//	    		    	fResourcesAppend = resourcesAppendIndicator.getSelection();
//	    		    }
//	    		    public void widgetDefaultSelected(SelectionEvent e) {}
//	    		});
	    	
	    	
	        fLanguageText.addModifyListener(new ModifyListener() {
	            public void modifyText(ModifyEvent e) {
	                Text text= (Text) e.widget;
	                WizardPageField field= (WizardPageField) text.getData();
	                field.fValue= text.getText();
	                sLanguage = field.fValue;
	                String classNamePrefix = null;
	                String packageNamePrefix = null;
	                if (sLanguage != null && sLanguage.length() > 0) {
	                	classNamePrefix = sLanguage.substring(0, 1).toUpperCase();
	                	packageNamePrefix = sLanguage.substring(0, 1).toLowerCase();
	                	if (sLanguage.length() > 1) {
	                		classNamePrefix = classNamePrefix + sLanguage.substring(1);
	                		packageNamePrefix = packageNamePrefix + sLanguage.substring(1);
	                	}
	                }
	                
	                String defIdentifier = packageNamePrefix +
	                	".imp.occurrenceIdentifier." + classNamePrefix + "OccurrenceIdentifier";	                
	                pages[0].getField("identifier").setText(defIdentifier);

	                //dialogChanged();
	            }
	        });
	    }
	    
	    
//	    public boolean getGenerateOccurrenceIdentifier() {
//	    	return fGenerateOccurrenceIdentifier;
//	    }
//	    
//	    
//	    public boolean getGenerateLabelProvider() {
//	    	return fGenerateLabelProvider;
//	    }
//	    
//	    public boolean getActivatorAppend() {
//	    	return fActivatorAppend;
//	    }
//	    
//	    
//	    public boolean getResourcesAppend() {
//	    	return fResourcesAppend;
//	    }
	    
	    
	    public void createControl(Composite parent) {
			super.createControl(parent);
			// SMS 9 Oct 2007 
			//discoverProjectLanguage();
			try {
				// SMS 10 May 2007
				// setEnabled was called with "false"; I don't know why,
				// but I want an enabled field (and enabling it here
				// hasn't seemed to cause any problems)	
				// SMS 8 Jan 2008:  field not used in this case
				//fQualClassText.setEnabled(true);
				
				// Set the class name based on the language name
				fLanguageText.addModifyListener(new ModifyListener() {
	                public void modifyText(ModifyEvent e) {	
	                    setClassByLanguage();
	                }
	            });
			} catch (Exception e) {
			    ErrorHandler.reportError("NewOccurrenceWizardPage.createControl(..):  Internal error", e);
			}
	    }

    }
    
    
}