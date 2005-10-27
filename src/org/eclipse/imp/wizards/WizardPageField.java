/*
 * Created on Oct 27, 2005
 */
package org.eclipse.uide.wizards;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class WizardPageField {
    public String name;
    public String label;
    public int kind;
    public boolean required;
    public String value;
    public String description;
    public Text text;
    public String schemaName;
    public Button button;
    public Hyperlink link;

    WizardPageField(String schemaName, String name, String label, String value, int kind, boolean required, String description) {
        this.name= name;
        this.label= label;
        this.value= value;
        this.kind= kind;
        this.required= required;
        this.description= description;
        this.schemaName= schemaName;
    }

    public void setEnabled(boolean enabled) {
        text.setEnabled(enabled);
        if (button != null)
    	button.setEnabled(enabled);
        if (link != null)
    	link.setEnabled(enabled);
    }

    public void setText(String string) {
        text.setText(string);
    }

    public String getText() {
        return text.getText();
    }
}