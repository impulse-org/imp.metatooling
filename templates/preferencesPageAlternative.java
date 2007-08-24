package $PREFS_PACKAGE_NAME$;

import org.eclipse.imp.preferences.AlternativePreferencesPage;
import $PLUGIN_PACKAGE$.$PLUGIN_CLASS$;

/**
 * An IMP-based preferences page placeholder for language $CLASS_NAME_PREFIX$.
 * 
 * Naming conventions:  This template uses the language name as a prefix
 * for naming the language plugin class and the preference-tab classes.
 * 	
 */
public class $PREFS_CLASS_NAME$ extends AlternativePreferencesPage {

	
	String alternativeMessage = "$PREFS_ALTERNATIVE_MESSAGE$";
	
	protected String getAlternativeMessage() {
		return alternativeMessage;
	}
	
}
