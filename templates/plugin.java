package $PLUGIN_PACKAGE$;
//SMS 28 Mar 2007:  plugin package now a separate parameter

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.uide.runtime.SAFARIPluginBase;
//import $PACKAGE_NAME$.preferences.$CLASS_NAME_PREFIX$PreferenceCache;
//import $PACKAGE_NAME$.preferences.$CLASS_NAME_PREFIX$PreferenceConstants;
import org.osgi.framework.BundleContext;

// SMS 28 Mar 2007:  plugin class name now totally parameterized
// in all uses here

public class $PLUGIN_CLASS$ extends SAFARIPluginBase {
	// SMS 28 Mar 2007:  now assign actual plugin id from parameter
    public static final String kPluginID= "$PLUGIN_ID$";

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

        // SMS 29 Mar 2007:  outdated use of preference store;
        // TODO  Replace with use of preferences service
		// Initialize the Preferences fields with the preference store data.
		IPreferenceStore prefStore= getPreferenceStore();
	
		// SMS 27 Mar 2007
		// Deleted examples of use of preferences cache, since preference cache
		// has been deleted.
		// TODO  Add some examples using new preferences service
    }

    public String getID() {
    	return kPluginID;
    }
}
