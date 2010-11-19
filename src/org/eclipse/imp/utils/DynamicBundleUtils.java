/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.imp.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.imp.WizardPlugin;
import org.eclipse.imp.language.ServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * @author rfuhrer
 */
public class DynamicBundleUtils {
    public static Bundle activateWorkspaceBundleForExtension(String langName, String extensionPointID) {
        if (langName == null)
            return null;
        IProject owningProject= ExtensionPointUtils.findProjectForLanguageExtension(langName, extensionPointID);
        if (owningProject != null) {
            try {
                BundleContext context= WizardPlugin.getInstance().getBundle().getBundleContext();
                URL bundleURL= new URL("file", null, owningProject.getLocation().toPortableString());
                Bundle bundle= context.installBundle(bundleURL.toExternalForm());

                if (bundle != null && bundle.getState() != Bundle.ACTIVE && bundle.getState() != Bundle.STARTING) {
                    try {
                        bundle.start(Bundle.START_TRANSIENT);
                    } catch (BundleException e) {
                        WizardPlugin.getInstance().logException("Unable to activate bundle project containing extension " + extensionPointID + " for language " + langName, e);
                    }
                }
                return bundle;
            } catch (BundleException e) {
                WizardPlugin.getInstance().logException("Unable to install bundle project containing extension " + extensionPointID + " for language " + langName, e);
            } catch (MalformedURLException e) {
                WizardPlugin.getInstance().logException("Unable to form install URL for bundle project containing extension " + extensionPointID + " for language " + langName, e);
            }
        }
        return null;
    }

    public static void deactivateWorkspaceBundle(Bundle bundle) {
        try {
            if (bundle != null) {
                bundle.uninstall();
            }
        } catch (BundleException e) {
            WizardPlugin.getInstance().logException("Unable to deactivate bundle " + bundle.getBundleId(), e);
        }
    }

    public static Bundle activateWorkspaceBundleForLanguage(String langName) {
        return activateWorkspaceBundleForExtension(langName, ServiceFactory.LANGUAGE_DESCRIPTION_QUALIFIED_POINT_ID);
    }
}
