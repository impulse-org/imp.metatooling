/**
 * 
 */
package org.eclipse.uide.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.runtime.RuntimePlugin;

public class NewPreferencesDialog extends CodeServiceWizard {
	
	protected String fPreferencesPackage;
	
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new NewPreferencesDialogWizardPage(this) } );
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
		ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
        IProject project= page.getProject();
        Map subs= getStandardSubstitutions();

        // Get new parameter values from wizard
        // and replace old parameter values
        WizardPageField field = pages[0].getField("class");
        String qualifiedClassName = field.fValue;
        String className = qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.')+1);
        subs.remove("$PREFS_CLASS_NAME$");
        subs.put("$PREFS_CLASS_NAME$", className);
        
        subs.remove("$PREFS_PACKAGE_NAME$");
        String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
        subs.put("$PREFS_PACKAGE_NAME$", packageName);
        
        field = pages[0].getField("category");
        String menuItem = field.fValue;

        field = pages[0].getField("alternative");
        String alternativeMessage = field.fValue;

        
        //System.out.println("Menu item:  " + menuItem);	
        
        String packageFolder = packageName.replace('.', File.separatorChar);
        
        // Note:  In the following, the second parameter is actually the name of the template file,
        // even though it may be given with a ".java" extension
        
        // Check whether there is an "alternative" mesage, in which case
        // assume that a simple page containing that message should be
        // generated rather than a full tabbed preference page.
        if (alternativeMessage.length() != 0){
            subs.remove("$PREFS_ALTERNATIVE_MESSAGE$");
            subs.put("$PREFS_ALTERNATIVE_MESSAGE$", alternativeMessage);
            IFile pageSrc = createFileFromTemplate(className + ".java", "preferencesPageAlternative.java", packageFolder, subs, project, mon);
            editFile(mon, pageSrc);
            return;
        }	
        
        // Generating a full tabbed preference page
        
        IFile pageSrc = createFileFromTemplate(className + ".java", "preferencesPageWithTabs.java", packageFolder, subs, project, mon);
        editFile(mon, pageSrc);
        
        IFile defaultSrc = createFileFromTemplate(className + "DefaultTab.java", "preferencesTabDefaultLevel.java", packageFolder, subs, project, mon);
        editFile(mon, defaultSrc);
        
        IFile configSrc = createFileFromTemplate(className + "ConfigurationTab.java", "preferencesTabConfigurationLevel.java", packageFolder, subs, project, mon);
        editFile(mon, configSrc);
        
        IFile instanceSrc = createFileFromTemplate(className + "InstanceTab.java", "preferencesTabInstanceLevel.java", packageFolder, subs, project, mon);
        editFile(mon, instanceSrc);
        
        IFile projectSrc = createFileFromTemplate(className + "ProjectTab.java", "preferencesTabProjectLevel.java", packageFolder, subs, project, mon);
        editFile(mon, projectSrc);
        
        IFile initializerSrc = createFileFromTemplate(className + "Initializer.java", "preferencesInitializer.java", packageFolder, subs, project, mon);
        editFile(mon, initializerSrc);
          
        IFile constantsSrc = createFileFromTemplate(className + "Constants.java", "preferencesConstants.java", packageFolder, subs, project, mon);
        editFile(mon, constantsSrc);
	}
	
	
	   
    /**
     * This method is called when 'Finish' button is pressed in the wizard.
     * We will create an operation and run it using wizard as execution context.
     * 
     * SMS 18 Dec 2006:
     * Overrode this method so as to call ExtensionPointEnabler with the
     * point id for preference menu items
     */
	public boolean performFinish() {
		collectCodeParms(); // Do this in the UI thread while the wizard fields are still accessible
		
		// SMS 18 Dec 2006
		// This is really the collection of code parameters, done here (in this thread) because
		// they can't be accessed from the runnable thread where they're needed.
		// For now assume that there's just one page; if this works, then can later arrange
		// to pass multiple instances to multiple instances of the runnable (or something)
		ExtensionPointWizardPage page= pages[0];
		WizardPageField prefIdField = page.getField("id");	
		WizardPageField prefNameField = page.getField("name");
		WizardPageField prefClassField = page.getField("class");
		WizardPageField prefCategoryField = page.getField("category");
		WizardPageField prefAlternativeField = page.getField("alternative");
		final String prefID = prefIdField.getText();
		final String prefName = prefNameField.getText();
		final String prefClass = prefClassField.getText();
		final String prefCategory = prefCategoryField.getText();
		final String prefAlternative = prefAlternativeField.getText();

		//
	
		
		IRunnableWithProgress op= new IRunnableWithProgress() {
		    public void run(IProgressMonitor monitor) throws InvocationTargetException {
			IWorkspaceRunnable wsop= new IWorkspaceRunnable() {
			    public void run(IProgressMonitor monitor) throws CoreException {
				try {
				    for(int n= 0; n < pages.length; n++) {
						ExtensionPointWizardPage page= pages[n];	
						if (!page.hasBeenSkipped() && page.fSchema != null) {
							// Enable an extension of org.eclipse.ui.preferencePages;
							// provide only information from fields that correspond to
							// elements for that extension-point schema.  (Any other
							// fields provided in the wizard should be ignored for
							// this purpose.)
							ExtensionPointEnabler.enable(
								page.getProject(), "org.eclipse.ui", "preferencePages", 
								new String[][] {
									{ "extension:id", "ext." + prefID },
									{ "extension:name", "ext." + prefName }, 
									{ "extension.page:id", prefID },
									{ "extension.page:name", prefName },
									{ "extension.page:class", prefClass },
									{ "extension.page:category", prefCategory },
								},
								false,
								monitor);
						}
				    }
				    generateCodeStubs(monitor);
				} catch (Exception e) {
				    ErrorHandler.reportError("Could not add extension points", e);
				} finally {
				    monitor.done();
				}
			    }
			};
			try {
			    ResourcesPlugin.getWorkspace().run(wsop, monitor);
			} catch (Exception e) {
			    ErrorHandler.reportError("Could not add extension points", e);
			}
		    }
		};
		try {
		    getContainer().run(true, false, op);
		} catch (InvocationTargetException e) {
		    Throwable realException= e.getTargetException();
		    ErrorHandler.reportError("Error", realException);
		    return false;
		} catch (InterruptedException e) {
		    return false;
		}
		return true;
	}
    
	
}