package $PLUGIN_PACKAGE$;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.uide.runtime.SAFARIPluginBase;
//import $PACKAGE_NAME$.preferences.$CLASS_NAME_PREFIX$PreferenceCache;
//import $PACKAGE_NAME$.preferences.$CLASS_NAME_PREFIX$PreferenceConstants;
import org.osgi.framework.BundleContext;

/*
 * SMS 27 Mar 2007:  Deleted creation of preferences cache (now obsolete)
 * SMS 28 Mar 2007:
 * 	- Plugin class name now totally parameterized
 *  - Plugin package made a separate parameter
 * SMS 19 Jun 2007:  Added kLanguageName (may be used by later
 * 	updates to the template)
 */ 

public class $PLUGIN_CLASS$ extends SAFARIPluginBase {

    public static final String kPluginID= "$PLUGIN_ID$";
    public static final String kLanguageName = "$LANG_NAME$";
    
    /**
     * The unique instance of this plugin class
     */
    protected static $PLUGIN_CLASS$ sPlugin;

    public static $PLUGIN_CLASS$ getInstance() {
        return sPlugin;
    }

    public $PLUGIN_CLASS$() {
    	super();
    	sPlugin= this;
    }

    public void start(BundleContext context) throws Exception {
        super.start(context);

        // TODO  Replace with use of preferences service
		// Initialize the Preferences fields with the preference store data.
		IPreferenceStore prefStore= getPreferenceStore();
    }

    public String getID() {
    	return kPluginID;
    }  
    
}
