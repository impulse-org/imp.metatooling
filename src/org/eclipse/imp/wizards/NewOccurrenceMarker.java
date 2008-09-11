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

public class NewOccurrenceMarker extends GeneratedComponentWizard {
	
	// fFullClassName is defined and used in GeneratedComponentWizard
	
    protected static final String thisWizardName = "New Occurrence Marker";
    protected static final String thisWizardDescription =
    	"This wizard supports the development of a occurrence marker";
    protected static final String componentID = "occurrenceMarker";
    
    protected NewOccurrenceMarkerWizardPage thePage = null;
    
    public void addPages() {
    	thePage = new NewOccurrenceMarkerWizardPage(this);
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
        // SMS 5 Aug 2008:  field name reverted to "class"
		String qualifiedClassName= pages[0].getField("class").fValue;
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
        	fProject, "org.eclipse.imp.runtime", "markOccurrences",
        	new String[][] {
        			{ "occurrenceMarker:class", fPackageName + "." + fFullClassName },
        			{ "occurrenceMarker:language", fLanguageName }
            	    },
    		false,
    		getPluginDependencies(),
    		mon);

    	
        // Define the substitution values for meta-parameters
        // in the templates from which implementations will be
        // generated
        
        Map<String, String> subs= getStandardSubstitutions(fProject);

        subs.put("$AST_PKG$", fParserPackage + "." + Wizards.astDirectory);
//        subs.put("$AST_NODE$", Wizards.astNode);
        
        subs.remove("$OCCURRENCE_MARKER_CLASS_NAME$");
        subs.put("$OCCURRENCE_MARKER_CLASS_NAME$", fFullClassName);
        
        subs.remove("$PACKAGE_NAME$");
        subs.put("$PACKAGE_NAME$", fPackageName);

//        String occurrenceMarkerTemplateName = "occurrenceMarker.java";
        IFile src= createFileFromTemplate(
        		fFullClassName + ".java", fTemplateName,
        		fPackageFolder, subs, fProject, mon);
        editFile(mon, src);
    }
 
	    
	    
    class NewOccurrenceMarkerWizardPage extends GeneratedComponentWizardPage
    {
    	NewOccurrenceMarkerWizardPage thePage;
    	
		public NewOccurrenceMarkerWizardPage(IMPWizard owner) {
		    super(owner, componentID, false, fWizardAttributes, thisWizardName, thisWizardDescription);
		    thePage = this;
		}

		protected void createFirstControls(Composite parent) {
		    super.createFirstControls(parent, componentID);
		    createClassField(parent, "ClassBrowse");
		}
		
	    protected void createAdditionalControls(Composite parent)
	    {	
    	
	    	createTextField(parent, "Occurrence Marker", "class",
	    		"The qualified name of the occurrence-marker class to be generated", 
	    		"", "ClassBrowse", true);
	    	
	        createTemplateBrowseField(parent, "OccurrenceMarker");
	    	
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
	                	".imp.occurrenceMarker." + classNamePrefix + "OccurrenceMarker";	                
	                pages[0].getField("class").setText(defIdentifier);

	                //dialogChanged();
	            }
	        });
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