package $PLUGIN_PACKAGE$;

import org.eclipse.imp.runtime.PluginBase;
import org.osgi.framework.BundleContext;

public class $PLUGIN_CLASS$ extends PluginBase {

    public static final String kPluginID= "$PLUGIN_ID$";
    public static final String kLanguageID = "$LANG_NAME$";
    
    /**
     * The unique instance of this plugin class
     */
    protected static $PLUGIN_CLASS$ sPlugin;

    public static $PLUGIN_CLASS$ getInstance() {
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

    @Override
    public String getID() {
    	return kPluginID;
    }  

    @Override
    public String getLanguageID() {
        return kLanguageID;
    }
}
