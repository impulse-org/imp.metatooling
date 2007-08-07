package $PLUGIN_PACKAGE$;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.imp.preferences.SafariPreferencesService;
import org.eclipse.imp.runtime.SAFARIPluginBase;
import org.osgi.framework.BundleContext;

/*
 * SMS 27 Mar 2007:  Deleted creation of preferences cache (now obsolete)
 * SMS 28 Mar 2007:
 * 	- Plugin class name now totally parameterized
 *  - Plugin package made a separate parameter
 * SMS 19 Jun 2007:
 * 	- Added kLanguageName (may be used by later updates to the template)
 * 	- Added field and method related to new preferences service; deleted
 *	  code for initializing preference store from start(..) method
 */ 

public class $PLUGIN_CLASS$ extends SAFARIPluginBase {

    public static final String kPluginID= "$PLUGIN_ID$";
    public static final String kLanguageName = "$LANG_NAME$";
    
    /**
     * The unique instance of this plugin class
     */
    protected static $PLUGIN_CLASS$ sPlugin;

    public static $PLUGIN_CLASS$ getInstance() {
    	// SMS 11 Jul 2007
    	// Added conditional call to constructor in case the plugin
    	// class has not been auto-started
    	if (sPlugin == null)
    		new $PLUGIN_CLASS$();
        return sPlugin;
    }

    public $PLUGIN_CLASS$() {
    	super();
    	sPlugin= this;
    }

    public void start(BundleContext context) throws Exception {
        super.start(context);

    }

    public String getID() {
    	return kPluginID;
    }  
    
    
    protected static SafariPreferencesService preferencesService = null;
    
    public static SafariPreferencesService getPreferencesService() {
    	if (preferencesService == null) {
    		preferencesService = new SafariPreferencesService(ResourcesPlugin.getWorkspace().getRoot().getProject());
    		preferencesService.setLanguageName(kLanguageName);
    		// TODO:  When some actual preferences are created, put
    		// a call to the preferences initializer here
    		// (The SAFARI New Preferences Support wizard creates such
    		// an initizlizer.)
    		
    	}
    	return preferencesService;
    }
    
}
