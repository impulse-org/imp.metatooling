package org.eclipse.uide;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class WizardPlugin extends AbstractUIPlugin {
    public static final String kPluginID= "org.eclipse.uide";

    // The singleton instance.
    private static WizardPlugin sPlugin;
    // Resource bundle.
    private ResourceBundle resourceBundle;

    public WizardPlugin() {
	super();
	sPlugin= this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
	super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
	super.stop(context);
	sPlugin= null;
	resourceBundle= null;
    }

    /**
     * Returns the shared instance.
     */
    public static WizardPlugin getInstance() {
	return sPlugin;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not found.
     */
    public static String getResourceString(String key) {
	ResourceBundle bundle= WizardPlugin.getInstance().getResourceBundle();
	try {
	    return (bundle != null) ? bundle.getString(key) : key;
	} catch (MissingResourceException e) {
	    return key;
	}
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
	try {
	    if (resourceBundle == null)
		resourceBundle= ResourceBundle.getBundle("org.eclipse.uide.wizard.WizardPluginResources");
	} catch (MissingResourceException x) {
	    resourceBundle= null;
	}
	return resourceBundle;
    }
}
