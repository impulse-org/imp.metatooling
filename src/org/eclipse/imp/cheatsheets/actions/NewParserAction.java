/*
 * Created on Mar 23, 2006
 */
package org.eclipse.uide.cheatsheets.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.jikespg.uide.wizards.NewUIDEParserWizard;

public class NewParserAction extends Action implements ICheatSheetAction {
    public NewParserAction() {
	this("Create a new parser");
    }

    public NewParserAction(String text) {
	super(text, null);
    }

    public void run(String[] params, ICheatSheetManager manager) {
	NewUIDEParserWizard newParserWizard= new NewUIDEParserWizard();
	Shell shell= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	WizardDialog wizDialog= new WizardDialog(shell, newParserWizard);

	wizDialog.open();
    }
}
