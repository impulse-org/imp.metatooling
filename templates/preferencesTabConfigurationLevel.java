package $PREFS_PACKAGE_NAME$;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.uide.preferences.ConfigurationPreferencesTab;
import org.eclipse.uide.preferences.ISafariPreferencesService;
import org.eclipse.uide.preferences.SafariPreferencesUtilities;
import org.eclipse.uide.preferences.fields.SafariFieldEditor;
//TODO:  Import additional classes for specific field types from
//org.eclipse.uide.preferences.fields

public class $PREFS_CLASS_NAME$ConfigurationTab extends ConfigurationPreferencesTab {
	
	
	public $PREFS_CLASS_NAME$ConfigurationTab(ISafariPreferencesService prefService) {
		super(prefService);
	}
	
	
	/**
	 * Creates specific preference fields with settings appropriate to
	 * the Workspace Configuration preferences level.
	 * 
	 * Overrides an unimplemented method in SafariPreferencesTab.
	 * 
	 * @return	An array that contains the created preference fields
	 */
	protected SafariFieldEditor[] createFields(Composite composite) {
		
		// TODO:  Declare preference fields here ...

		// TODO:  Construct the specific fields, including a "details" link
		// for each field; also create "toggle" listeners between fields whose
		// editability is linked.  Add spaces, boxes, etc. as apprpriate.
		//
		// SafariPreferencesUtilities has factory-like methods for creating
		// fields and links of specific types.
		//
		// Among the various parameters that can be set for a Safari preferences
		// field, fields below the default level should generally be removable.
		
		
		// TODO:  Put the created fields into an array and return it
		SafariFieldEditor fields[] = new SafariFieldEditor[0];		// change length as appropriate
		// 	Add fields here ...
		
		return fields;

	}
	
	
	
	
}
