package org.eclipse.uide.wizards;

import org.eclipse.swt.widgets.Composite;

/**
 * Custom wizard page for the extension point "org.eclipse.core.resources.builders",
 * which adds a "language" field to what gets automatically generated from the
 * extension point schema.
 */
public class BuilderWizardPage extends ExtensionPointWizardPage {
    public BuilderWizardPage(ExtensionPointWizard owner) {
        super(owner, "org.eclipse.core.resources", "builders", false);
    }

    @Override
    public void createControl(Composite parent)
    {
        super.createControl(parent);
        // RMF 10/18/2006: Hack to make wizard page be "finishable" right away.
        getFieldByFullName("extension.builder.run.parameter:name").fText.setText("foo");
        getFieldByFullName("extension.builder.run.parameter:value").fText.setText("bar");
    }

    protected void createFirstControls(Composite parent) {
        createLanguageFieldForPlatformSchema(parent);
    }
}
