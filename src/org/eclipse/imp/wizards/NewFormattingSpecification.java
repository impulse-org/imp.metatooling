package org.eclipse.imp.wizards;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.WizardPlugin;
import org.eclipse.imp.runtime.RuntimePlugin;

public class NewFormattingSpecification extends ExtensionPointWizard {
    private String fSpecFilename;

    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new NewFormattingSpecificationWizardPage(this) });
    }

    protected List<String> getPluginDependencies() {
        return Arrays.asList(new String[] { 
                "org.eclipse.core.runtime", 
                "org.eclipse.core.resources",             
                "org.eclipse.imp.runtime", 
                "org.eclipse.imp.formatting" });
    }
    
    private class NewFormattingSpecificationWizardPage extends ExtensionPointWizardPage {
        public NewFormattingSpecificationWizardPage(ExtensionPointWizard owner) {
            super(owner, WizardPlugin.kPluginID, "formattingSpecification");
        }
    }
    
    @Override
    public boolean canFinish() {
        if (!fileFieldIsValid()) {
            pages[0].setErrorMessage("File name should end with \".fsp\"");
            return false;
        }
        
        return super.canFinish();
    }
    
    protected void collectCodeParms() {
        fSpecFilename = pages[0].getValue("file");
        fLanguageName = pages[0].getValue("language");
        fProject = pages[0].getProjectBasedOnNameField();
    }

    @Override
    protected void generateCodeStubs(IProgressMonitor mon) throws CoreException {
        Map<String,String> subs= getStandardSubstitutions();
        
        WizardUtilities.createFileFromTemplate(
                        fSpecFilename, WizardPlugin.kPluginID, "formatter.fsp", "", getProjectSourceLocation(fProject),
                        subs, fProject, new NullProgressMonitor());
        
        ExtensionPointEnabler.
        enable(
                fProject, RuntimePlugin.IMP_RUNTIME, "formatter", 
                new String[][] {
                        { "extension:id", fProject.getName() + ".formatter" },
                        { "extension:name", fLanguageName + " Formatter" },
                        { "formatter:class", "org.eclipse.imp.formatting.SourceFormatter" },
                        { "formatter:language", fLanguageName }   
                }
                , false, 
                getPluginDependencies(), 
                new NullProgressMonitor());
    }

    protected Map<String,String> getStandardSubstitutions() {
        Map<String,String> result= new HashMap<String,String>();
        result.put("$LANGUAGE_NAME$", fLanguageName);
        return result;
    }

    private boolean fileFieldIsValid() {
        final String text = pages[0].getField("file").fText.getText();
        return text == null || text.length() == 0 || text.endsWith(".fsp");
    }
       
}