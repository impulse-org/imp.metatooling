/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/**
 * 
 */
package org.eclipse.imp.wizards;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.runtime.RuntimePlugin;

public class NewFormatter extends CodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.IMP_RUNTIME, "formatter"), });
    }

    // SMS 25 May 2007:  added "org.eclipse.jface.text"
    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
                "org.eclipse.imp.runtime", "org.eclipse.jface.text" });
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
        ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
        IProject project= page.getProjectOfRecord();
        Map subs= getStandardSubstitutions();

        // SMS 9 Aug 2006 (but see following note of 21 Jul 2006)
        // $AST_PACKAGE$ is in the standard substitutions now, so I'm
        // commenting out the setting of it here.
        // $PARSER_PACKAGE$ seems to get the wrong value (i.e., the
        // original default value, not the one specified by the user)
        subs.put("$PARSER_PACKAGE$", fParserPackage); // These should be in the standard substitutions...
        //	subs.put("$AST_PACKAGE$", fParserPackage);
        // SMS 21 Jul 2006
        // Regarding the above, the parser package is set in the standard
        // substitutions (at least insofar as being set by CodeServiceWiazrd.
        // getStandardSubstitutions()) but the AST package is not--not sure
        // whether it would be set anywhere else so as to be in effect here

        // SMS 21 Jul 2006
        // Added (or modified) following to accommodate
        // values provided through wizard by user
        
        WizardPageField field = pages[0].getField("class");
        String qualifiedClassName = field.fValue;
        String className = qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.')+1);
        subs.remove("$FORMATTER_CLASS_NAME$");
        subs.put("$FORMATTER_CLASS_NAME$", className);
        
        subs.remove("$PACKAGE_NAME$");
        String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
        subs.put("$PACKAGE_NAME$", packageName);
        
        String packageFolder = packageName.replace('.', File.separatorChar);
        
        String formatterTemplateName = "formatter.java";
        IFile formatterSrc= WizardUtilities.createFileFromTemplate(
        	className + ".java", formatterTemplateName, packageFolder, getProjectSourceLocation(fProject), subs, project, mon);

        editFile(mon, formatterSrc);
    }
}