package org.eclipse.uide.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.runtime.RuntimePlugin;
import org.eclipse.uide.wizards.ExtensionPointEnabler;
import org.eclipse.uide.wizards.ExtensionPointWizard;
import org.eclipse.uide.wizards.ExtensionPointWizardPage;
import org.eclipse.uide.wizards.WizardPageField;
	

/**

 */
public class NewPreferencesDialogWizardPage extends ExtensionPointWizardPage
{
	
 	 NewPreferencesDialogWizardPage(ExtensionPointWizard owner) {
 		 // The "false" value provided at the end of the parameters
 		 // controls whether fields for extension name and id are shown
 		 // in the wizard--the parameter is "omitIDName", so "false"
 		 // means don't omit them (and "true" means do omit them)
 		 //super(owner, RuntimePlugin.UIDE_RUNTIME, "preferencesDialog", false);
 		 super(owner, RuntimePlugin.UIDE_RUNTIME, "preferencesDialog", false, true);
    }


 	 /**
 	  * Create the other controls, then use the language name
 	  * to set a default value for the (preferences menu) cagetory
 	  * under which the new preferences dialog should appear.
 	  */
    public void createControl(Composite parent)
    {
		super.createControl(parent);
								
		// Try to assure that the language is defined
		setLanguageIfEmpty();
		
		// Don't set category if empty, because the most
		// likely (or, anyway, safest) default value is
		// no category (i.e., a top-level item)
    }

    
    // copied from package org.jikespg.uide.wizards.NewUIDEParserWizardPage
    
    public String determineLanguage()
    {
		try {
		    IPluginModel pluginModel= ExtensionPointEnabler.getPluginModel(getProject());
	
		    if (pluginModel != null) {
				IPluginExtension[] extensions= pluginModel.getExtensions().getExtensions();
		
				for(int n= 0; n < extensions.length; n++) {
				    IPluginExtension extension= extensions[n];
		
                    if (!extension.getPoint().equals("org.eclipse.uide.runtime.languageDescription"))
                        continue;

                    IPluginObject[] children= extension.getChildren();
		
				    for(int k= 0; k < children.length; k++) {
						IPluginObject object= children[k];
			
						if (object.getName().equals("language")) {
						    return ((IPluginElement) object).getAttribute("language").getValue();
						}
				    }
				    System.err.println("NewPreferencesDialogWizardPage.determineLanguage():  Unable to determine language for plugin '" + pluginModel.getBundleDescription().getName() + "': no languageDescription extension.");
				}
		    } else if (getProject() != null)
		    	System.out.println("Not a plugin project: " + getProject().getName());
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return "";
    }

    	
    protected void setLanguageIfEmpty() {
        try {
            String pluginLang= determineLanguage(); // if a languageDesc exists
            if (pluginLang.length() == 0)
                return;

            WizardPageField field= getField("language");

            if (field.getText().length() == 0)
                field.setText(pluginLang);
        } catch (Exception e) {
            ErrorHandler.reportError("NewPreferencesDialogWizardPage.setLanguageIfEmpty():  Cannot set language", e);
        }
    }
    
 
    
}
