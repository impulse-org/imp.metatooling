package $PACKAGE_NAME$;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.services.ILanguageActionsContributor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;

public class $ACTION_CONTRIBUTOR_CLASS_NAME$ implements ILanguageActionsContributor {
	
	public void contributeToEditorMenu(final UniversalEditor editor,
			IMenuManager menuManager) {
		IMenuManager languageMenu = new MenuManager("$LANG_NAME$");
		menuManager.add(languageMenu);
		languageMenu.add(new Action("Example") { 
			// TODO implement run method here
		});
	}

	public void contributeToMenuBar(UniversalEditor editor, IMenuManager menu) {
		// TODO implement contributions and add them to the menu
	}

	public void contributeToStatusLine(final UniversalEditor editor,
			IStatusLineManager statusLineManager) {
		// TODO add ControlContribution objects to the statusLineManager
	}

	public void contributeToToolBar(UniversalEditor editor,
			IToolBarManager toolbarManager) {
		// add ControlContribution objects to the toolbarManager
	}
}
