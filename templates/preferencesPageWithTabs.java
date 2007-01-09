package $PREFS_PACKAGE_NAME$;


import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.uide.preferences.ISafariPreferencesService;
import org.eclipse.uide.preferences.SafariPreferencesTab;
import org.eclipse.uide.preferences.SafariTabbedPreferencesPage;
import $LANG_NAME$.$CLASS_NAME_PREFIX$Plugin;

/**
 * The Safari-based tabbed preferences page for language $CLASS_NAME_PREFIX$.
 * 
 * Naming conventions:  This template uses the language name as a prefix
 * for naming the language plugin class and the preference-tab classes.
 * 	
 */
public class $PREFS_CLASS_NAME$ extends SafariTabbedPreferencesPage {
	
	public $PREFS_CLASS_NAME$() {
		super();
		// Get the language-specific preferences service
		prefService = $CLASS_NAME_PREFIX$Plugin.getPreferencesService();
	}
	
	
	protected SafariPreferencesTab[] createTabs(
			ISafariPreferencesService prefService, SafariTabbedPreferencesPage page, TabFolder tabFolder) 
	{
		SafariPreferencesTab[] tabs = new SafariPreferencesTab[4];
		
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
