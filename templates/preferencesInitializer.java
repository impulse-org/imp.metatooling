package $PREFS_PACKAGE_NAME$;
	
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.imp.preferences.IPreferencesService;
import $PLUGIN_PACKAGE$.$PLUGIN_CLASS$;

/**
 * Provides a method to initialize the default-level preference for $PREFS_CLASS_NAME$
 * in the preferences service.
 * 
 * The purpose of this class is to initialize default-level preferences only.  Preference
 * values on other levels will be initialized from files managed by the preferences service.
 * Preferences on the default level are not stored but are defined programmatically (which
 * is the purpose of this class).
 */
public class $PREFS_CLASS_NAME$Initializer extends AbstractPreferenceInitializer {
    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences() {
    
		IPreferencesService service = $PLUGIN_CLASS$.getPreferencesService();
		
		// TODO:  Initialize preferences values here ...
		// Example:
		// 	service.setBooleanPreference(
		//		IPreferencesService.DEFAULT_LEVEL, $PREFS_CLASS_NAME$Constants.P_EMIT_MESSAGES, getDefaultEmitMessages());
		// Note the stipulation of the default level
		// We typically use a separate package to define constant identifiers for preferences
		// This example uses a function (defined below) to represent the initial value
    }
    
   
    // TODO:  Optionally, define constants or functions that represent the default values
    // Example:
    // public static boolean getDefaultEmitMessages() { return true; }
    
}
