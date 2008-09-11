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

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

import java.io.FileInputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.pde.internal.core.ischema.ISchema;
import org.eclipse.pde.internal.core.ischema.ISchemaAttribute;
import org.eclipse.pde.internal.core.ischema.ISchemaComplexType;
import org.eclipse.pde.internal.core.ischema.ISchemaCompositor;
import org.eclipse.pde.internal.core.ischema.ISchemaElement;
import org.eclipse.pde.internal.core.ischema.ISchemaObject;
import org.eclipse.pde.internal.core.ischema.ISchemaType;
import org.eclipse.pde.internal.core.schema.Schema;
import org.eclipse.pde.internal.core.schema.SchemaComplexType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;

/**
 * The "New" wizard page allows setting the container for the new file as well as the file name. The page will only
 * accept file name without the extension OR with the extension that matches the expected one (g).
 */
public class ExtensionPointWizardPage extends IMPWizardPage //	 WizardPage	
{
    protected String fExtPluginID;
    protected String fExtPointID;
    protected String fExtensionID;
    protected String fExtensionName;
    protected Schema fSchema;
    protected WizardPageField fFirstTemplateField = null;
    

    public ExtensionPointWizardPage(ExtensionPointWizard owner, String pluginID, String pointID) {
    	this(owner, pluginID, pointID, true);
    }
    
    public ExtensionPointWizardPage(ExtensionPointWizard owner, String pluginID, String pointID, boolean omitIDName) {
    	this(owner, 0, 1, pluginID, pointID, false);
        fOmitExtensionIDName= omitIDName;
    }
    
    public ExtensionPointWizardPage(ExtensionPointWizard owner, String pluginID, String pointID, boolean omitIDName, boolean local) {
        this(owner, 0, 1, pluginID, pointID, false, local);
        fOmitExtensionIDName= omitIDName;
    }

    public ExtensionPointWizardPage(ExtensionPointWizard owner, int pageNumber, int totalPages, String pluginID, String pointID, boolean isOptional) {
        //super("wizardPage");
    	super("wizardPage", owner, pageNumber, totalPages, isOptional);
        this.fExtPluginID= pluginID;
        this.fExtPointID= pointID;
        fComponentID = fExtPointID;

        try {
            IExtensionPoint ep= (IExtensionPoint) Platform.getExtensionRegistry().getExtensionPoint(pluginID, pointID);
            String schemaLoc;
            URL localSchemaURL;

            if (ep == null)
        	return;
//        	throw new IllegalArgumentException("Unknown extension point: " + pluginID + "." + pointID);

            if (ep.getUniqueIdentifier().startsWith("org.eclipse.") && !ep.getUniqueIdentifier().startsWith("org.eclipse.imp")) {
                // RMF 1/5/2006 - Hack to get schema for extension points defined by Eclipse
                // platform plug-ins: attempts to find them in org.eclipse.platform.source,
            	// or, failing that, org.eclipse.rcp.source
            	
                URL schemaURL = locateSchema(ep, "org.eclipse.platform.source");

                if (schemaURL == null)
                    schemaURL = locateSchema(ep, "org.eclipse.rcp.source");
                
                if (schemaURL == null)
                	schemaURL = locateSchema(ep, "org.eclipse.core.resources");
                
                if (schemaURL == null)
                	schemaURL = locateSchema(ep, "org.eclipse.core.resources.source");	// <= builders.exsd is here in 3.4
                
                if (schemaURL == null)
                	schemaURL = locateSchema(ep, "org.eclipse.imp.metatooling");		// local backup for schemas we can't find elsewhere
                
                if (schemaURL == null)
                    throw new Exception("Cannot find schema source for " + ep.getSchemaReference());

                localSchemaURL= FileLocator.toFileURL(schemaURL);
                schemaLoc= localSchemaURL.getPath();
                
            } else {
                Bundle core= Platform.getBundle(pluginID);

                localSchemaURL= FileLocator.toFileURL(FileLocator.find(core, new Path("schema/" + ep.getSimpleIdentifier() + ".exsd"), null));
                schemaLoc= localSchemaURL.getPath();
            }
            fSchema= new Schema(pluginID, pointID, "", false);
            fSchema.load(new FileInputStream(schemaLoc));
            setDescription(fSchema.getDescription());
        } catch (Exception e) {
            ErrorHandler.reportError("Cannot create wizard page for " + pluginID + "." + pointID, e);
            setTitle("Extension point: " + pluginID + "." + pointID);
            setDescription("Cannot create wizard page: " + e);
        }
    }

    
    // SMS 27 Sep 2007:  "local" parameter (added some time ago) just to differentiate this consctructor from the previous one	
    public ExtensionPointWizardPage(
    	ExtensionPointWizard owner, int pageNumber, int totalPages, String pluginID, String pointID, boolean isOptional, boolean local)
    {
    	super("wizardPage", owner, pageNumber, totalPages, isOptional);
        fExtPluginID= pluginID;
        fExtPointID= pointID;
        fComponentID = fExtPointID;
        
        try {
            //IExtensionPoint ep= (IExtensionPoint) Platform.getExtensionRegistry().getExtensionPoint(pluginID, pointID);
            String schemaLoc;
            URL localSchemaURL;


                Bundle core= Platform.getBundle(pluginID);

                localSchemaURL= FileLocator.toFileURL(FileLocator.find(core, new Path("schema/" + /*ep.getSimpleIdentifier()*/ pointID + ".exsd"), null));
                schemaLoc= localSchemaURL.getPath();

            fSchema= new Schema(pluginID, pointID, "", false);
            fSchema.load(new FileInputStream(schemaLoc));
            setDescription(fSchema.getDescription());
        } catch (Exception e) {
            ErrorHandler.reportError("Cannot create wizard page for " + pluginID + "." + pointID, e);
            setTitle("Extension point: " + pluginID + "." + pointID);
            setDescription("Cannot create wizard page: " + e);
        }
    }

    
    private URL locateSchema(IExtensionPoint ep, String srcBundle) {
		Bundle platSrcPlugin= Platform.getBundle(srcBundle);
		if (platSrcPlugin == null)
			return null;
		Bundle extProviderPlugin= Platform.getBundle(ep.getContributor().getName());
		if (extProviderPlugin == null)
			return null;
		String extPluginVersion= (String) extProviderPlugin.getHeaders().get("Bundle-Version");
		Path schemaPath= new Path("src/" + ep.getContributor().getName() + "_" + extPluginVersion + "/" + ep.getSchemaReference());
		URL schemaURL= FileLocator.find(platSrcPlugin, schemaPath, null);

		if (schemaURL == null) {
			// Special case for Eclipse 3.4 M5
			// Schemas are not located in the same sort place as in released for
			// Eclipse 3.2 and 3.3.  In 3.4 M5 the builder schema is found in
			// org.eclipse.core.resources.source_3.4.0.v20080205.jar/schema/builder.exsd
			// (along with schemas for natures, markers, and a few others)
			// This block is formulated to find schemas in jar files (at least that one)
			schemaPath = new Path(ep.getSchemaReference());
			schemaURL = FileLocator.find(platSrcPlugin, schemaPath, null);
		}
		
		return schemaURL;
    }

    /**
     * Creates controls that are to appear above the schema attributes
     * on the wizard page. Derived classes may override.
     * 
     * SMS 13 Nov 2007:  The only control we create here is the language
     * field.  We look through the extension point schema to see if there
     * is a "language" attribute.  We don't actually use the schema to
     * create the field, but it is probably a good idea to create the
     * field only if the attribute exists.
     * 
     * SMS 26 Nov 2007:  We assume here that if a language field is present
     * in the schema then it is located under the elements extension.<elementName>.
     * We've designed all of our schemas that way, but this is potentially a
     * point of brittleness.  Given that we have agreed informally that we
     * want to move away from the use of schemas as the basis for creating
     * fields on wizard pages, there may not be much point to implementing
     * a more flexible mechanism here.  But be aware of the possibility of
     * problems from "non-standard" schemas.
     * 
     * @param parent
     */
    // SMS 10 Oct 2007:  was no componentID; used in place of fExtPointID
    protected void createFirstControls(Composite parent, String componentID) {
		// Attempt to locate the "language" element attribute, so that we can
		// create that field first, since it's got a listener that populates
		// several other fields with reasonable values based on the language
		// name.  As a result, it's nicer to have the language field near the
		// top, so that it's easier to get at than if it were at the bottom.
    	// SMS 9 Oct 2007
    	// As a consequence of this approach, it may be a bad idea to have
    	// more than one element or attribute in the schema that is named
    	// "language" (but that may be a bad idea for other reasons, too).
    	// 
    	
		if (fSchema == null)
		    return;
	
		ISchemaElement elt= fSchema.findElement(componentID);		//fExtPointID);

		if (elt == null) {
		    ISchemaElement[] elements= fSchema.getElements();
		    for(int i= 0; i < elements.length; i++) {
				if (elements[i].getAttribute("language") != null) {
				    elt= elements[i];
				    break;
				}
		    }
		}
		if (elt != null)
			// SMS 13 Nov 2007:  Create language field using the
			// method in IMPWizardPage (put there for that purpose)
		    //createElementAttributeTextField(parent, "extension." + elt.getName(), elt.getAttribute("language"));
			// SMS 26 Nov 2007:  Added "extension" prefix and used elt name rather than componentID
			// (those are often, but not always, the same, and the elt name is required)
			createLanguageFieldForComponent(parent, "extension." + elt.getName());
    }
    
    
    /**
     * Creates additional controls that are to appear below the schema
     * attributes on the wizard page. Derived classes may override.
     * @param parent
     */
    protected void createAdditionalControls(Composite parent) {
        // Optionally overridden in derived classes
    }

    /**
     * Create the controls and related elements for the ExtensionPointWizardPage.
     * Controls are created in four categories:
     * - First controls:  the project and language fields (by definition)
     * - Schema-based controsl:  controls based on elements in the extension-point
     *		schema (possible only for wizards based on an extension point)
     * - Additional controls:  controls for additional elements that are needed by
     * 		the wizard or its minions
     * Having created the controls, we discover the currently selected project
     * (which fills in the project field).  It is assumed that the values of
     * other fields will be set by other means (notably by listeners on various
     * fields).
     * 
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        try {
            Composite container= new Composite(parent, SWT.NULL | SWT.BORDER);
            // container.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

            GridLayout layout= new GridLayout();
            container.setLayout(layout);
            layout.numColumns= 3;
            layout.verticalSpacing= 9;
            if (fTotalPages > 1 && fIsOptional) {
                addServiceEnablerCheckbox(container);
            }
            createProjectField(container);
            try {              
               	// Controls that are to appear by default above any
            	// wizard-specific controls
	        	createFirstControls(container, fComponentID);
	        	
				// Create wizard-specific controls based on the
	        	// extension-point schema
	        	createControlsForSchema(fSchema, container);

	        	// Don't use "artificially" constructed set of schema attributes
	        	// in lieu of a schema as in GeneratedComponentWizardPage
	        	//createControlsForAttributes(fAttributes, null, container);
	        	
                // SMS 28 Jul 2008
	        	// Note:  this isn't very convenient for wizards
	        	// that may nave more than one template
                fFirstTemplateField = createTemplateBrowseField(container, fComponentID);
	        	
	        	// To create any remaining wizard-specific controls in addition to
	        	// those based on the schema
                createAdditionalControls(container);
                
                createDescriptionText(container, fSchema.getDescription());

                // Set the selected project (doesn't happen otherwise);
                // trust listeners to set dependent fields (esp. language)
                discoverSelectedProject();
                
            } catch (Exception e) {
                new Label(container, SWT.NULL).setText("Could not create wizard page");
                ErrorHandler.reportError("Could not create wizard page", e);
            }
            dialogChanged();
            setControl(container);
            fProjectText.setFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createControlsForSchema(ISchema schema, Composite container) {
	if (schema != null)
	    createControlsForSchemaElement(schema.findElement("extension"), schema, "extension", container);
    }

    private void createControlsForSchemaElement(ISchemaElement element, ISchema schema, String prefix, Composite container) {
	ISchemaType eltType= element.getType();

	createControlsForElementAttributes(element, prefix, container);

	if (eltType instanceof ISchemaComplexType) {
	    SchemaComplexType complexType= (SchemaComplexType) eltType;
	    ISchemaCompositor comp= complexType.getCompositor();

	    if (comp != null) {
		ISchemaObject[] children= comp.getChildren();

		for(int i= 0; i < children.length; i++) {
		    ISchemaElement child= (ISchemaElement) children[i];
		    String subPrefix= ((prefix.length() > 0) ? (prefix + ".") : prefix) + child.getName();

		    createControlsForSchemaElement(schema.findElement(child.getName()), schema, subPrefix, container);
		}
	    }
	}
    }

    private void createControlsForElementAttributes(ISchemaElement element, String prefix, Composite container) {
	ISchemaAttribute[] attributes= element.getAttributes();

	for(int k= 0; k < attributes.length; k++) {
	    ISchemaAttribute attribute= attributes[k];

	    if (attribute.getName().equals("point"))
		continue;

	    if (fOmitExtensionIDName && element.getName().equals("extension") &&
		    (attribute.getName().equals("id") || attribute.getName().equals("name")))
		continue;

	    createElementAttributeTextField(container, prefix, attribute);
	}
    }


        

    private void createFileBrowseButton(Composite container, WizardPageField field, Text text) {
        Button button= new Button(container, SWT.PUSH);
        button.setText("Browse...");
        button.setData(text);
        button.addSelectionListener(new FileBrowseSelectionAdapter(/*container,*/ field));
        if (field != null)
            field.fButton= button;
    }
    

    	
    private void createFolderBrowseButton(Composite container, WizardPageField field, Text text) {
        Button button= new Button(container, SWT.PUSH);
        button.setText("Browse...");
        button.setData(text);
        button.addSelectionListener(new FolderBrowseSelectionAdapter(/*container,*/ field));
        if (field != null)
            field.fButton= button;
    }
    


    public void setVisible(boolean visible) {
        if (visible) {
            setTitle((fSchema != null ? fSchema.getName() : "") + " (Step " + (fThisPageNumber + 1) + " of " + fOwningWizard.getPageCount() + ")");
            fOwningWizard.setPage(fThisPageNumber);
            if (fLanguageText != null) {
                fLanguageText.setText(sLanguage);
            }
            if (fThisPageNumber > 0 && fProjectText.getCharCount() == 0) {
                fProjectText.setText(sProjectName);
            }
        }
        super.setVisible(visible);
        dialogChanged();
    }
   

    public String getExtensionID() {
           WizardPageField idField = getField("id");
           if (idField == null) return null;
	       if (!idField.fText.isDisposed()	|| idField.getText() == null || idField.getText().length() == 0)
	    	   setIDByLanguage();
           return idField.getText();
    }
    

    
    public String getExtensionName() {
        WizardPageField nameField = getField("name");
        if (nameField == null) return null;
	       if (nameField.getText() == null) setNameByLanguage();
        return nameField.getText();
    }
    
    
    
    public String getExtensionClass() {
        WizardPageField classField = getField("class");
        if (classField == null) return null;
	       if (classField.getText() == null) setClassByLanguage();
        return classField.getText();
    }
    

    // Subtype specific in use and logic
    public WizardPageField getFieldByFullName(String fullName) {
        for(int n= 0; n < fFields.size(); n++) {
            WizardPageField field= (WizardPageField) fFields.get(n);
            String fieldFullName= field.fSchemaElementName + ":" + field.fAttributeName;
            if (fieldFullName.equals(fullName)) {
                return field;
            }
        }
        return null;
    }

    // fExtPointID is wizard-subtype-specific
    protected void createLanguageFieldForPlatformSchema(Composite parent) {
        WizardPageField languageField= new WizardPageField(fExtPointID, "language", "Language", "", 0, true, "Language for which to create a " + fExtPointID);
    
        fLanguageText= createLabelTextBrowse(parent, languageField, null);

        fFields.add(languageField);
    
        fLanguageText.setData(languageField);
        fLanguageText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                Text text= (Text) e.widget;
                WizardPageField field= (WizardPageField) text.getData();
                field.fValue= text.getText();
                sLanguage= field.fValue;
                dialogChanged();
            }
        });
    }
    
    
    public void dispose() {
    	if (getField("id") != null)
    		fExtensionID = getField("id").getText();
    	if (getField("name") != null)
    		fExtensionName = getField("name").getText();
    }
    
    
}
