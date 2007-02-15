package $PREFS_PACKAGE_NAME$;

import org.eclipse.uide.preferences.SafariAlternativePreferencesPage;
import $LANG_NAME$.$CLASS_NAME_PREFIX$Plugin;

/**
 * A Safari-based preferences page placeholder for language $CLASS_NAME_PREFIX$.
 * 
 * Naming conventions:  This template uses the language name as a prefix
 * for naming the language plugin class and the preference-tab classes.
 * 	
 */
public class $PREFS_CLASS_NAME$ extends SafariAlternativePreferencesPage {

	
	String alternativeMessage = "$PREFS_ALTERNATIVE_MESSAGE$";
	
	protected String getAlternativeMessage() {
		return alternativeMessage;
	}
	
}
