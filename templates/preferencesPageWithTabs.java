package $PREFS_PACKAGE_NAME$;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.imp.preferences.IPreferencesService;
import org.eclipse.imp.preferences.PreferencesTab;
import org.eclipse.imp.preferences.TabbedPreferencesPage;
import $PLUGIN_PACKAGE$.$PLUGIN_CLASS$;	// SMS 27 Mar 2007

/**
 * The IMP-based tabbed preferences page for language $CLASS_NAME_PREFIX$.
 * 
 * Naming conventions:  This template uses the language name as a prefix
 * for naming the language plugin class and the preference-tab classes.
 * 	
 */
public class $PREFS_CLASS_NAME$ extends TabbedPreferencesPage {
	
	public $PREFS_CLASS_NAME$() {
		super();
		// Get the language-specific preferences service
		// SMS 28 Mar 2007:  parameterized full name of plugin class
		prefService = $PLUGIN_CLASS$.getPreferencesService();
	}
	
	
	protected PreferencesTab[] createTabs(
			IPreferencesService prefService, TabbedPreferencesPage page, TabFolder tabFolder) 
	{
		PreferencesTab[] tabs = new PreferencesTab[4];
		
		$PREFS_CLASS_NAME$ProjectTab projectTab = new $PREFS_CLASS_NAME$ProjectTab(prefService);
		projectTab.createProjectPreferencesTab(page, tabFolder);
		tabs[0] = projectTab;

		$PREFS_CLASS_NAME$InstanceTab instanceTab = new $PREFS_CLASS_NAME$InstanceTab(prefService);
		instanceTab.createInstancePreferencesTab(page, tabFolder);
		tabs[1] = instanceTab;
		
		$PREFS_CLASS_NAME$ConfigurationTab configurationTab = new $PREFS_CLASS_NAME$ConfigurationTab(prefService);
		configurationTab.createConfigurationPreferencesTab(page, tabFolder);
		tabs[2] = configurationTab;

		$PREFS_CLASS_NAME$DefaultTab defaultTab = new $PREFS_CLASS_NAME$DefaultTab(prefService);
		defaultTab.createDefaultPreferencesTab(page, tabFolder);
		tabs[3] = defaultTab;
		
		return tabs;
	}

}
