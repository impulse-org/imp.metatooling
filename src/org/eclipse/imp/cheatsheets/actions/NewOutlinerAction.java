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
import org.eclipse.uide.wizards.Wizards.NewOutliner;

public class NewOutlinerAction extends Action implements ICheatSheetAction {
    public NewOutlinerAction() {
	this("Create a new outliner");
    }

    public NewOutlinerAction(String text) {
	super(text, null);
    }

    public void run(String[] params, ICheatSheetManager manager) {
	NewOutliner newOutlinerWizard= new NewOutliner();
	Shell shell= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	WizardDialog wizDialog= new WizardDialog(shell, newOutlinerWizard);

	wizDialog.open();
    }
}
