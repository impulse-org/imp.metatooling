/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/*
 * Created on Oct 27, 2005
 */
package org.eclipse.imp.wizards;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class WizardPageField {
    /**
     * Schema element attribute name.
     */
    public String fAttributeName;

    /**
     * Possibly "qualified schema element" name, indicating the element nesting.
     */
    public String fSchemaElementName;

    /**
     * The label to be displayed to the left of the attribute's text field.
     */
    public String fLabel;

    /**
     * The attribute's kind according to the extension point schema (i.e., STRING, JAVA, or RESOURCE).
     */
    public int fKind;

    /**
     * If true, this field must have a non-empty value for the dialog's "Finish" button to be enabled.
     */
    public boolean fRequired;

    /**
     * The current value of this attribute.
     */
    public String fValue;

    public String fDescription;

    /**
     * The editable text field holding this attribute's user-specified value
     */
    public Text fText;

    /**
     * The "Browse..." button for this attribute's value (if any; may be null)
     */
    public Button fButton;

    /**
     * The hyperlink for the class corresponding to this attribute's value (if any;
     * may be null if this attribute does not specify a Java class)
     */
    public Hyperlink fLink;

    // SMS 25 Sep 2007
    // Changed visibility from package to public since some wizards that are outside
    // of this package may need to create fields directly (e.g., the LPG related
    // wizards, which are located in the LPG metatooling project and which create
    // fields directly rather than relying on an extension schema)
    public WizardPageField(String schemaName, String name, String label, String value, int kind, boolean required, String description) {
        fAttributeName= name;
        fLabel= label;
        fValue= value;
        fKind= kind;
        fRequired= required;
        fDescription= description;
        fSchemaElementName= schemaName;
    }

    public void setEnabled(boolean enabled) {
        fText.setEnabled(enabled);
        if (fButton != null)
    	fButton.setEnabled(enabled);
        if (fLink != null)
    	fLink.setEnabled(enabled);
    }

    public void setText(String string) {
        fText.setText(string);
    }

    public String getText() {
        return fText.getText();
    }

    public String toString() {
	return "<" + fSchemaElementName + ":" + fAttributeName + "=" + fValue + ">";
    }
}
