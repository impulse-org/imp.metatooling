package $PACKAGE_NAME$;

$IPROJECT_IMPORT$
import org.eclipse.imp.builder.ProjectNatureBase;
import org.eclipse.imp.runtime.IPluginLog;
$SMAPI_IMPORT$

import $PLUGIN_PACKAGE$.$PLUGIN_CLASS$;

public class $CLASS_NAME_PREFIX$Nature extends ProjectNatureBase {
	public static final String k_natureID = $PLUGIN_CLASS$.kPluginID + ".imp.nature";
 
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
        return $PLUGIN_CLASS$.getInstance();
    }

    protected String getDownstreamBuilderID() {
        return null; // TODO If needed, specify the builder that will consume artifacts created by this nature's builder
    }
}
