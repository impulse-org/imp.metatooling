/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.wizards;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.pde.internal.core.ischema.IMetaAttribute;
import org.eclipse.pde.internal.core.ischema.ISchemaAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * The "New" wizard page allows setting the container for the new file as well as the file name. The page will only
 * accept file name without the extension OR with the extension that matches the expected one.
 */
public class GeneratedComponentWizardPage extends IMPWizardPage	//WizardPage
{
    private final class FocusDescriptionListener extends FocusAdapter {
	public void focusGained(FocusEvent e) {
	    Text text= (Text) e.widget;
	    WizardPageField field= (WizardPageField) text.getData();
	    fDescriptionText.setText(field.fDescription);
	}
    }



    
    // These fields are used in GeneratedComponentWizardPage
    // to represent information that would be provided
    // through an extension-point schema if that were used
    protected String fWizardName = "";
    protected String fWizardDescription = "";
    protected List fAttributes = new ArrayList();
    

    public GeneratedComponentWizardPage(
    	GeneratedComponentWizard owner, String componentID, boolean omitIDName,
    	GeneratedComponentAttribute[] attributes, String wizardName, String wizardDescription)
    {
        this(owner, 0, 1, /*pluginID,*/ componentID, false, attributes, wizardName, wizardDescription);
        fOmitExtensionIDName= omitIDName;
    }

    public GeneratedComponentWizardPage(
    	GeneratedComponentWizard owner, int pageNumber, int totalPages, String componentID, boolean isOptional,
    	GeneratedComponentAttribute[] attributes, String wizardName, String wizardDescription)
    {
        //	super("wizardPage");
    	super("wizardPage", owner, pageNumber, totalPages, isOptional);
        this.fComponentID= componentID;
//        this.fIsOptional= isOptional;
//        this.fThisPageNumber= pageNumber;
//        this.fTotalPages= totalPages;
//        this.fOwningWizard= owner;
        for (int i = 0; i < attributes.length; i++) {
        	this.fAttributes.add(attributes[i]);
        }
        this.fWizardName = wizardName;
        this.fWizardDescription = wizardDescription;

    }



    /**
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
            createProjectLabelText(container);
            try {
            	// Controls that are to appear by default above any
            	// wizard-specific controls
	        	createFirstControls(container, fComponentID);
	        	
				// No schema for generated components (as for ExtensionPointWizardPage)
	        	//createControlsForSchema(fSchema, container);
	        	
	        	// May use "artificially" constructed set of schema attributes
	        	// in lieu of a schema
	        	createControlsForAttributes(fAttributes, null, container);
	        	
	        	// To create any remaining controls
	        	// (specific wizards may just use this in place of attributes)
                createAdditionalControls(container);
                
                createDescriptionText(container, fWizardDescription);

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

    protected void createControlsForAttributes(List attributes, String prefix, Composite container)
    {
    	for(int k= 0; k < attributes.size(); k++) {
    	    ISchemaAttribute attribute = (ISchemaAttribute) attributes.get(k);
    	    createElementAttributeTextField(container, prefix, attribute);
    	}
    }
    
    
    /**
     * Directly create a text field for a wizard from given values rather than using an extension schema
     * element or attributes.
     * 
     * @param container			The wizard page (or other container) in which the field will be placed
     * @param fieldCategoryName	A name for a larger grouping of fields that might contain this one;
     * 							(used in place of the schema name--largely vestigial in this context,
     * 							but still used in toString() of the WizardPageField class, which treats
     * 							fields based on schemas and fields not based on schemas similarly)
     * @param fieldName			A name for the field (used in place of the schema attribute name)
     * @param description		A description of the field (what it's for, how it should be filled, ...)
     * @param value				A value that may be filled into the field by default (may be null)
     * @param basedOn			For fields that represent a Java type, a value (such as the name of
     * 							a parent type) used to evaluate or process the given value
     * @param isRequired		Whether the field must be given a value before the containing wizard can
     * 							be finished
     */
    public void createTextField(Composite container, String fieldCategoryName, String fieldName, String description, String value, String basedOn, boolean isRequired)
    {
        String valueStr= (value == null) ? "" : value;
        String upName= upperCaseFirst(fieldName);

        WizardPageField field= new WizardPageField(fieldCategoryName, fieldName, upName, valueStr, IMetaAttribute.STRING, isRequired, description);
        Text text= createLabelTextBrowse(container, field, basedOn);

        // SMS 13 Jun 2007:  added test for "Language"
        // SMS 25 Sep 2007:  inherited from the original method in ExtensionPointWizardPage
        // on which this one is based; may still be useful
        if (fieldName.equals("language") || fieldName.equals("Language"))
            fLanguageText= text;
        else if (fieldName.equals("class"))
            fQualClassText= text;

        text.setData(field);
        fFields.add(field);
    }


    protected Text createLabelText(Composite container, WizardPageField field) {
        Widget labelWidget= null;
        String name= field.fAttributeName;
        String description= field.fDescription;
        String value= field.fValue;

        name += ":";

        Label label= new Label(container, SWT.NULL);
        label.setText(name);
        label.setToolTipText(description);
        labelWidget= label;
        label.setBackground(container.getBackground());

        Text text= new Text(container, SWT.BORDER | SWT.SINGLE);
        labelWidget.setData(text);
        GridData gd= new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan= 2;
        text.setLayoutData(gd);
        text.setText(value);

        if (field != null)
            field.fText= text;

        text.addFocusListener(new FocusDescriptionListener());
        text.setData(field);

        return text;
    }


    public void setVisible(boolean visible) {
        if (visible) {
            //setTitle((fSchema != null ? fSchema.getName() : "") + " (Step " + (fThisPageNumber + 1) + " of " + fOwningWizard.getPageCount() + ")");
            setTitle(fWizardName + " (Step " + (fThisPageNumber + 1) + " of " + fOwningWizard.getPageCount() + ")");
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

    String stripHTML(String description) {
        StringBuffer buffer= new StringBuffer(description);
        replace(buffer, "<p>", "");
        replace(buffer, "<ul>", "");
        replace(buffer, "</ul>", "");
        replace(buffer, "<li>", " *  ");
        return buffer.toString();
    }

    void replace(StringBuffer buffer, String s1, String s2) {
        int index= buffer.indexOf(s1);
        while (index != -1) {
            buffer.replace(index, index + s1.length(), s2);
            index= buffer.indexOf(s1);
        }
    }

 
    // SMS 13 Nov 2007:
    // Removed method createLanguageFieldForPlatformSchema(Composite parent)
    // that wasn't called and that is probably supplanted by a version now
    // in IMPWizardPage.	

    
    // Not duplicated in practice, but not necessarily specific in logic?
    protected void createClassField(Composite parent, String basedOn) {
	    WizardPageField field = new WizardPageField(null, "class", "Class:", "Compiler", 0, true, "Name of the class that implements " + fComponentID);
	    Text classText= createLabelTextBrowse(parent, field, basedOn);

	    classText.setData(field);
	    fFields.add(field);
    }

}
