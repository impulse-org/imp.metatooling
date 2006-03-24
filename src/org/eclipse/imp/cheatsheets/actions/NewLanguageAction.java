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
import org.eclipse.uide.wizards.Wizards.NewLanguage;

public class NewLanguageAction extends Action implements ICheatSheetAction {
    public NewLanguageAction() {
	this("Create a new programming language IDE");
    }

    public NewLanguageAction(String text) {
	super(text, null);
    }

    public void run(String[] params, ICheatSheetManager manager) {
	NewLanguage newLangWizard= new NewLanguage();
	Shell shell= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	WizardDialog wizDialog= new WizardDialog(shell, newLangWizard);

	wizDialog.open();
    }
}
