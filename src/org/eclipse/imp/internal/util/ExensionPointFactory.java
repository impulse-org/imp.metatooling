package org.eclipse.uide.internal.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.core.Language;
import org.osgi.framework.Bundle;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

/**
 * @author Claffra
 *
 * TODO add documentation
 */
public class ExensionPointFactory {

	public static ILanguageService createExtensionPoint(Language language, String pluginID, String extentionPointId) {
	    if (language == null) {
            ErrorHandler.reportError("Cannot obtain service on null language: "+extentionPointId);
	        return null;
        }
        ILanguageService service = null;
	    try {
		    IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(pluginID, extentionPointId);
		    if (extensionPoint != null) {
                service = getLanguageContributor(extensionPoint, language.getName());
 		    }
		} catch (Throwable e) {
		    ErrorHandler.reportError("Error finding \""+extentionPointId+"\" service for \""+language+"\"", e);
		}
		try {
            if (service == null) {
                String className = "Default" + Character.toUpperCase(extentionPointId.charAt(0)) + extentionPointId.substring(1);
    		    String defaultClass = pluginID + ".defaults." + className;
                service = (ILanguageService) Class.forName(defaultClass).newInstance();
            }
	    }
		catch (Throwable ee) {
			ErrorHandler.reportError("Universal Editor Error", ee);
		}
        if (service != null)
            service.setLanguage(language.getName());
        return service;
	}

	public static ILanguageService getLanguageContributor(IExtensionPoint extensionPoint, String language) throws CoreException {
        IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
        if (elements != null) {
        	for (int n = 0; n < elements.length; n++) {
                IConfigurationElement element = elements[n];
        		Bundle bundle= Platform.getBundle( element.getDeclaringExtension().getNamespace());
        		if (bundle != null) {
        		    if (language.equals(element.getAttribute("language"))) {
        				return (ILanguageService) element.createExecutableExtension("class");
        			}
        		}                    
            }
        }
        return null;
    }


}
