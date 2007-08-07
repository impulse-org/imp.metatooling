package $PREFS_PACKAGE_NAME$;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.imp.preferences.DefaultPreferencesTab;
import org.eclipse.imp.preferences.ISafariPreferencesService;
import org.eclipse.imp.preferences.SafariPreferencesUtilities;
import org.eclipse.imp.preferences.fields.SafariFieldEditor;
// TODO:  Import additional classes for specific field types from
// org.eclipse.imp.preferences.fields


public class $PREFS_CLASS_NAME$DefaultTab extends DefaultPreferencesTab {
	
	public $PREFS_CLASS_NAME$DefaultTab(ISafariPreferencesService prefService) {
		super(prefService);
	}

	
	/**
	 * Creates a language-specific preferences initializer.
	 * Overrides an unimplemented method in DefaultPreferecnesTab.
	 * 
	 * @return 	The preference initializer to be used to initialize
	 * 			preferences in this tab
	 */
	public AbstractPreferenceInitializer getPreferenceInitializer() {
		$PREFS_CLASS_NAME$Initializer preferencesInitializer = new $PREFS_CLASS_NAME$Initializer();
		return preferencesInitializer;
	}	
	
	
	/**
	 * Creates specific preference fields with settings appropriate to
	 * the Default preferences level.
	 * 
	 * Overrides an unimplemented method in SafariPreferencesTab.
	 * 
	 * @return	An array that contains the created preference fields
	 */
	protected SafariFieldEditor[] createFields(Composite composite)
	{
		// TODO:  Declare preference fields here ...

		// TODO:  Construct the specific fields, including a "details" link
		// for each field; also create "toggle" listeners between fields whose
		// editability is linked.  Add spaces, boxes, etc. as apprpriate.
		//
		// SafariPreferencesUtilities has factory-like methods for creating
		// fields and links of specific types.
		//
		// Among the various parameters that can be set for a Safari preferences
		// field, fields on the default level should generally not be removable.
		
		
		// TODO:  Put the created fields into an array and return it
		SafariFieldEditor fields[] = new SafariFieldEditor[0];		// change length as appropriate
		// 	Add fields here ...
		
		return fields;
	}
	
	
}
