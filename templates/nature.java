package $PACKAGE_NAME$;

$IPROJECT_IMPORT$
import org.eclipse.uide.core.ProjectNatureBase;
import org.eclipse.uide.runtime.IPluginLog;
$SMAPI_IMPORT$
//import $LANG_NAME$.$CLASS_NAME_PREFIX$Plugin;
//import $PLUGIN_PACKAGE$.$CLASS_NAME_PREFIX$Plugin;
import $PLUGIN_PACKAGE$.$PLUGIN_CLASS$;	// SMS 27 Mar 2007

public class $CLASS_NAME_PREFIX$Nature extends ProjectNatureBase {
	// SMS 28 Mar 2007:  plugin class now totally parameterized
	public static final String k_natureID = $PLUGIN_CLASS$.kPluginID + ".safari.nature";
 
    public String getNatureID() {
        return k_natureID;
    }

    public String getBuilderID() {
        return $BUILDER_CLASS_NAME$.BUILDER_ID;
    }
    
$SMAP_SUPPORT$
    protected void refreshPrefs() {
        // TODO implement preferences and hook in here
    }

    public IPluginLog getLog() {
    	// SMS 28 Mar 2007:  plugin class now totally parameterized
        return $PLUGIN_CLASS$.getInstance();
    }

    protected String getDownstreamBuilderID() {
        // TODO Auto-generated method stub
        return null;
    }
}
