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

    protected void createFirstControls(Composite parent) {
        createLanguageFieldForPlatformSchema(parent);
    }
}
