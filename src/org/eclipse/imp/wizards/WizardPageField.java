/*
 * Created on Oct 27, 2005
 */
package org.eclipse.uide.wizards;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class WizardPageField {
    public String fName;
    public String fLabel;
    public int fKind;
    public boolean fRequired;
    public String fValue;
    public String fDescription;
    public Text fText;
    /**
     * Possibly "qualified schema element" name, indicating the element nesting.
     */
    public String fSchemaElementName;
    public Button fButton;
    public Hyperlink fLink;

    WizardPageField(String schemaName, String name, String label, String value, int kind, boolean required, String description) {
        fName= name;
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
	return "<" + fSchemaElementName + ":" + fName + "=" + fValue + ">";
    }
}