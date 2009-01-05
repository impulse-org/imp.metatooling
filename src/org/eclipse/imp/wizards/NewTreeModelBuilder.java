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

public class NewTreeModelBuilder extends GeneratedComponentWizard {
	
	protected String fFullBuilderClassName = "";
	protected String fFullProviderClassName = "";
	
	protected String fBuilderTemplateName = null;
	protected String fProviderTemplateName = null;
	
	protected WizardPageField treeModelBuilderField = null;
	protected WizardPageField labelProviderField = null;
	
    protected static final String thisWizardName = "New Tree Model Builder";
    protected static final String thisWizardDescription =
    	"Create a skeleton tree-model builder, e.g., for use in constructing a document outline";
    protected static final String componentID = "treeModelBuilder";
    
    protected NewTreeModelBuilderPage thePage = null;
    
    public void addPages() {
    	thePage = new NewTreeModelBuilderPage(this);
        addPages(new GeneratedComponentWizardPage[] { thePage } );
//        		new NewTreeModelBuilderPage(this
////        				"treeModelBuilder", true, null, thisWizardName, thisWizardDescription
//        				)}
//        	);
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] {
                "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	        "org.eclipse.imp.runtime", "org.eclipse.ui", "org.eclipse.jface.text", 
                "org.eclipse.ui.editors", "org.eclipse.ui.workbench.texteditor", "lpg.runtime" });
    }
    
    
    protected void collectCodeParms() {
        // TODO call super.collectCodeParms() and just add to/overwrite what it set up
    	fProject = pages[0].getProjectOfRecord();
    	fProjectName = pages[0].fProjectText.getText();
        fLanguageName= pages[0].fLanguageText.getText();
        
        fClassNamePrefix= Character.toUpperCase(fLanguageName.charAt(0)) + fLanguageName.substring(1);
        
		String qualifiedClassName= pages[0].getField("builder").fValue;
		fFullBuilderClassName = qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.') + 1);
		fPackageName= qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
		fPackageFolder= fPackageName.replace('.', File.separatorChar);
		
		qualifiedClassName= pages[0].getField("provider").fValue;
		fFullProviderClassName = qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.') + 1);
		
		fBuilderTemplateName = treeModelBuilderField.getText();
		fProviderTemplateName = labelProviderField.getText();
		
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
        	fProject, "org.eclipse.imp.runtime", "modelTreeBuilder",
        	new String[][] {
        			{ "treeBuilder:class", fPackageName + "." + fFullBuilderClassName },
        			{ "treeBuilder:language", fLanguageName }
            	    },
    		false,
    		getPluginDependencies(),
    		mon);

    	
        // Define the substitution values for meta-parameters
        // in the templates from which implementations will be
        // generated
        
        Map<String, String> subs= getStandardSubstitutions(fProject);

        subs.put("$PARSER_PKG$", fParserPackage);
        subs.put("$AST_PKG$", fParserPackage + "." + Wizards.astDirectory);
        subs.put("$AST_NODE$", Wizards.astNode);
        subs.put("$TREE_MODEL_BUILDER_CLASS_NAME$", fFullBuilderClassName);
        subs.put("$LABEL_PROVIDER_CLASS_NAME$", fFullProviderClassName);
        subs.put("$PACKAGE_NAME$", fPackageName);
        subs.put("$CLASS_NAME_PREFIX_UPPER$", fClassNamePrefix.toUpperCase());
        subs.put("$CLASS_NAME_PREFIX_LOWER$", fClassNamePrefix.toLowerCase());
        subs.put("$RESOURCES_CLASS$", "I" + fClassNamePrefix + "Resources");


        // Generate the tree-model-builder implementation, if requested
        if (thePage.fGenerateTreeModelBuilder) {
//	        String treeModelBuilderTemplateName = "treeModelBuilder.java";
	        IFile outlinerSrc= createFileFromTemplate(
	        		fFullBuilderClassName + ".java", fBuilderTemplateName,
	        		fPackageFolder, subs, fProject, mon);
	        editFile(mon, outlinerSrc);
        }
 
        // Generate the label-provider implementation, if requested
        if (thePage.fGenerateLabelProvider) {
//	        String labelProviderTemplateName = "labelProvider.java";
	        IFile labelProviderSrc = createFileFromTemplate(
	        		fFullProviderClassName + ".java", fProviderTemplateName,
	        		fPackageFolder, subs, fProject, mon);
	        editFile(mon, labelProviderSrc);
        }
        
        // Get some names related to the plug-in activator class for use
        // in the next steps
        String pluginPackageName = subs.get("$PLUGIN_PACKAGE$");
        String pluginFolderName = pluginPackageName.replace('.', '/');
        String pluginClassName = subs.get("$PLUGIN_CLASS$");
        if (!pluginClassName.endsWith(".java"))
        	pluginClassName = pluginClassName + ".java";
        
        // Create or append to the interface in which String constants
        // are defined for the plug-in, as appropriate.  Constants of
        // concern here are names for standard or default images that
        // may be used in the label provider
        String resourcesClassFileName = "I" + fClassNamePrefix + "Resources.java";
        String resourcesFilePath =
        	fProject.getLocation().toString() + "/" + getProjectSourceLocation(fProject) + "/" + pluginFolderName + "/" + resourcesClassFileName;
        File file = new File(resourcesFilePath);
        if (!file.exists()) {
        	// Create in any case
        	createFileFromTemplate(
            		resourcesClassFileName, "labelResources.java",
            		pluginFolderName, subs, fProject, mon);
        } else if (((NewTreeModelBuilderPage)pages[0]).getResourcesAppend()) {
        	// Append only if the user has indicated so
        	WizardUtilities.extendFileFromTemplate(
            	resourcesClassFileName, "labelResourcesAddendum.java",
            	pluginFolderName, getProjectSourceLocation(fProject), subs, fProject, mon); 
        }

        // Append the plug-in activator class with members related to image management
        // (assume that the file exists)
        if (((NewTreeModelBuilderPage)pages[0]).getActivatorAppend()) {
	        WizardUtilities.extendFileFromTemplate(
	        	pluginClassName, "plugin_addendum_for_images.java",
	        	pluginFolderName, getProjectSourceLocation(fProject), subs, fProject, mon);  
        }
        
        // Make the appropriate generic icons available for the label provider
        WizardUtilities.copyLiteralFile("../icons/sample.gif", fClassNamePrefix.toLowerCase() + "_default_image.gif", "icons", fProject, mon);
        WizardUtilities.copyLiteralFile("../icons/outline_item.gif", fClassNamePrefix.toLowerCase() + "_default_outline_item.gif", "icons", fProject, mon);
        WizardUtilities.copyLiteralFile("../icons/file.gif", fClassNamePrefix.toLowerCase() + "_file.gif", "icons", fProject, mon);
        WizardUtilities.copyLiteralFile("../icons/file_warning.gif", fClassNamePrefix.toLowerCase() + "_file_warning.gif", "icons", fProject, mon);
        WizardUtilities.copyLiteralFile("../icons/file_error.gif", fClassNamePrefix.toLowerCase() + "_file_error.gif", "icons", fProject, mon);

    	// Enable the second extension is here.  When they are enabled in consecutive statements
        // it seems that sometimes the second enabling doesn't succeed, i.e., the plugin.xml
        // file would not be updated to include this extension.  This seems to be some sort
        // of timing problem (you can see it happen in the debugger if you put the two
        // enablings in sequence but stop after the first one).  Separating the enablings by
        // the rest of the code in this method seems to allow both of them to succeed regularly.
        ExtensionPointEnabler.enable(
            	fProject, "org.eclipse.imp.runtime", "labelProvider",
            	new String[][] {
            			{ "labelProvider:class", fPackageName + "." + fFullProviderClassName },
            			{ "labelProvider:language", fLanguageName }
                	    },
        		false,
        		getPluginDependencies(),
        		mon);
        
    }
 
	    
	    
    class NewTreeModelBuilderPage extends GeneratedComponentWizardPage
    {
    	protected boolean fGenerateTreeModelBuilder = true;
    	protected boolean fGenerateLabelProvider = true;
    	protected boolean fActivatorAppend = true;
    	protected boolean fResourcesAppend = true;
    	NewTreeModelBuilderPage thePage;
    	
		public NewTreeModelBuilderPage(IMPWizard owner) {
		    super(owner, componentID, false, fWizardAttributes, thisWizardName, thisWizardDescription);
		    thePage = this;
		}

		protected void createFirstControls(Composite parent) {
		    super.createFirstControls(parent, componentID);
		    createClassField(parent, "ClassBrowse");
		}
		
	    protected void createAdditionalControls(Composite parent)
	    {	
            // Put some whitespace into the layout
    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	    	
	    	final Button generateTreeModelBuilderIndicator = createBooleanField(
	    			parent, "Create tree\nmodel builder?", "Generate a tree-model builder that will be used as the basis of an outline view",
		    		"Check this box to cause the wizard to generate an implementation skeleton for a new tree-model builder", true, 1);

	    	generateTreeModelBuilderIndicator.addSelectionListener(new SelectionListener() {
	    		    public void widgetSelected(SelectionEvent e) {
	    		    	fGenerateTreeModelBuilder = generateTreeModelBuilderIndicator.getSelection();
	    		    	WizardPageField field = thePage.getField("builder");
	    		    	if (field != null) {
	    		    		field.setEnabled(fGenerateTreeModelBuilder);
	    		    	}
	    		    	if (treeModelBuilderField != null) {
	    		    		treeModelBuilderField.setEnabled(fGenerateTreeModelBuilder);
	    		    	}
	    		    }
	    		    public void widgetDefaultSelected(SelectionEvent e) {}
	    		});
	    	
	    	createTextField(parent, "Tree Model Builder", "builder",
	    		"The qualified name of the tree-model builder class to be generated", 
	    		"", "ClassBrowse", true);
	    	

	        treeModelBuilderField = createTemplateBrowseField(parent, "TreeModelBuilder");
	    	
	    	
            // Put some whitespace into the layout
    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	    
	    	final Button generateLabelProviderIndicator = createBooleanField(
	    			parent, "Create label\nprovider?", "Generate a label-provider that will provide text and image labels for use in the IDE",
		    		"Check this box to cause the wizard to generate an implementation skeleton for a new label-provider.", true, 1);
	    	generateLabelProviderIndicator.addSelectionListener(new SelectionListener() {
	    		    public void widgetSelected(SelectionEvent e) {
	    		    	fGenerateLabelProvider = generateLabelProviderIndicator.getSelection();
	    		    	WizardPageField field = thePage.getField("provider");
	    		    	if (field != null) {
	    		    		field.setEnabled(fGenerateLabelProvider);
	    		    	}
	    		    	if (labelProviderField != null) {
	    		    		labelProviderField.setEnabled(fGenerateLabelProvider);
	    		    	}
	    		    }
	    		    public void widgetDefaultSelected(SelectionEvent e) {}
	    		});
	    	
	    	WizardPageField providerField = createTextField(parent, "Label Provider", "provider",
		    		"The qualified name of the label-provider class to be generated", 
		    		"", "ClassBrowse", true);
	    		
	        labelProviderField = createTemplateBrowseField(parent, "LabelProvider");
	    	
	    	
            // Put some whitespace into the layout
    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    	
    	    
    	    String activatorAppendInfo = 
    			"Update the plug-in activator class with members related to label management.\n" +
    			"Not necessary if done previously; regenerating will not clobber earlier updates\n" +
    			"but can create duplicate members.";
    	    	
	    	final Button activatorAppendIndicator = createBooleanField(
	    		parent, "Update plug-in\nactivator?",
	    			activatorAppendInfo, activatorAppendInfo,
	    			true, 1);
	    	activatorAppendIndicator.addSelectionListener(new SelectionListener() {
    		    public void widgetSelected(SelectionEvent e) {
    		    	fActivatorAppend = activatorAppendIndicator.getSelection();
    		    }
    		    public void widgetDefaultSelected(SelectionEvent e) {}
    		});
	    	
	    	
	    	String resourcesAppendInfo = 
	    		"Update the class used to define String resources for the plug-in, adding constants for default image names.\n" +
    			"These are referenced by the label-provider.  Not necessary if previously generated (or there is no label provider).\n" +
    			"Regenerating will not clobber earlier entries but can create duplicate definitions.";
	    	
	    	final Button resourcesAppendIndicator = createBooleanField(
		    		parent, "Update string\nresources?",
		    		resourcesAppendInfo, resourcesAppendInfo,
		    		true, 1);
		    	resourcesAppendIndicator.addSelectionListener(new SelectionListener() {
	    		    public void widgetSelected(SelectionEvent e) {
	    		    	fResourcesAppend = resourcesAppendIndicator.getSelection();
	    		    }
	    		    public void widgetDefaultSelected(SelectionEvent e) {}
	    		});
	    	
	    	
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
	                
	                String defBuilder = packageNamePrefix + ".imp.treeModelBuilder." + classNamePrefix + "TreeModelBuilder";
	                String defProvider = packageNamePrefix + ".imp.treeModelBuilder." + classNamePrefix + "LabelProvider";
	                
	                pages[0].getField("builder").setText(defBuilder);
	                pages[0].getField("provider").setText(defProvider);

	                //dialogChanged();
	            }
	        });
	    }
	    
	    
	    public boolean getGenerateTreeModelBuilder() {
	    	return fGenerateTreeModelBuilder;
	    }
	    
	    
	    public boolean getGenerateLabelProvider() {
	    	return fGenerateLabelProvider;
	    }
	    
	    public boolean getActivatorAppend() {
	    	return fActivatorAppend;
	    }
	    
	    
	    public boolean getResourcesAppend() {
	    	return fResourcesAppend;
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
			    ErrorHandler.reportError("NewTreeModelWizardPage.createControl(..):  Internal error", e);
			}
	    }

    }
    
    
}