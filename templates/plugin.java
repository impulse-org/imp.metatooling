package $PACKAGE_NAME$;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.uide.runtime.SAFARIPluginBase;
import $PACKAGE_NAME$.preferences.$CLASS_NAME_PREFIX$PreferenceCache;
import $PACKAGE_NAME$.preferences.$CLASS_NAME_PREFIX$PreferenceConstants;
import org.osgi.framework.BundleContext;

public class $CLASS_NAME_PREFIX$Plugin extends SAFARIPluginBase {
    public static final String kPluginID= "$LANG_NAME$";

    /**
     * The unique instance of this plugin class
     */
    protected static $CLASS_NAME_PREFIX$Plugin sPlugin;

    public static $CLASS_NAME_PREFIX$Plugin getInstance() {
        return sPlugin;
    }

    public $CLASS_NAME_PREFIX$Plugin() {
	super();
	sPlugin= this;
    }

    public void start(BundleContext context) throws Exception {
        super.start(context);

	// Initialize the Preferences fields with the preference store data.
	IPreferenceStore prefStore= getPreferenceStore();

	$CLASS_NAME_PREFIX$PreferenceCache.builderEmitMessages= prefStore.getBoolean($CLASS_NAME_PREFIX$PreferenceConstants.P_EMIT_MESSAGES);

	fEmitInfoMessages= $CLASS_NAME_PREFIX$PreferenceCache.builderEmitMessages;
    }

    public String getID() {
	return kPluginID;
    }
}