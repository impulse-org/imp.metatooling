/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

package org.eclipse.imp;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.imp.runtime.PluginBase;
import org.osgi.framework.BundleContext;

public class WizardPlugin extends PluginBase {
    public static final String kPluginID= "org.eclipse.imp.metatooling";

    private static WizardPlugin sPlugin;

    private ResourceBundle resourceBundle;

    /**
     * Returns the shared instance.
     */
    public static WizardPlugin getInstance() {
        return sPlugin;
    }

    public WizardPlugin() {
        super();
        sPlugin= this;
    }

    @Override
    public String getID() {
        return kPluginID;
    }

    @Override
    public String getLanguageID() {
        return "impMetatooling";
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
                resourceBundle= ResourceBundle.getBundle("org.eclipse.imp.wizard.WizardPluginResources");
        } catch (MissingResourceException x) {
            resourceBundle= null;
        }
        return resourceBundle;
    }
}
