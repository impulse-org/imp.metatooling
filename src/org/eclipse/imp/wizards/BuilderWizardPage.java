package org.eclipse.uide.wizards;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Custom wizard page for the extension point "org.eclipse.core.resources.builders",
 * which adds a "language" field to what gets automatically generated from the
 * extension point schema.
 */
public class BuilderWizardPage extends ExtensionPointWizardPage {
    boolean fAddSMAPSupport= true;

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

    @Override
    protected void createAdditionalControls(Composite parent) {
	Button b= new Button(parent, SWT.CHECK);

	b.setText("Add SMAP support");

	b.addSelectionListener(new SelectionAdapter() {
	    @Override
	    public void widgetSelected(SelectionEvent e) {
		fAddSMAPSupport= ((Button) e.widget).getSelection();
		if (!fRequiredPlugins.contains("com.ibm.watson.smapifier"))
		    fRequiredPlugins.add("com.ibm.watson.smapifier");
	    }
	});
	b.setSelection(true); // Turn on SMAP support by default
    }
}
