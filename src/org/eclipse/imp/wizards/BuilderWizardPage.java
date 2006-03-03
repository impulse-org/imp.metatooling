package org.eclipse.uide.wizards;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Custom wizard page for the extension point "org.eclipse.core.resources.builders",
 * which adds a "language" field to what gets automatically generated from the
 * extension point schema.
 */
public class BuilderWizardPage extends ExtensionPointWizardPage {
    public BuilderWizardPage(ExtensionPointWizard owner) {
        super(owner, "org.eclipse.core.resources", "builders");
    }

    protected void createAdditionalControls(Composite parent) {
        WizardPageField wizardPageField= new WizardPageField("builders", "language", "Language", "", 0, true, "Language for which to create a builder");

        fLanguageText= createLabelTextBrowse(parent, "Language", "Language for which to create a builder",
                "IncrementalProjectBuilder", "", true, wizardPageField);

        fFields.add(wizardPageField);
        fLanguageText.setData(wizardPageField);
        fLanguageText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                Text text= (Text) e.widget;
                WizardPageField field= (WizardPageField) text.getData();
                field.value= text.getText();
                sLanguage= field.value;
                dialogChanged();
            }
        });
    }
}
