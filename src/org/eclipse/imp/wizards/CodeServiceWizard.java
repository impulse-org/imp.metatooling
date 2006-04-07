/**
 * 
 */
package org.eclipse.uide.wizards;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * An ExtensionPointWizard that also generates source code from one or more template files.
 * @author rfuhrer@watson.ibm.com
 */
public abstract class CodeServiceWizard extends ExtensionPointWizard {
    protected String fLanguageName;
    protected String fPackageName;
    protected String fPackageFolder;
    protected String fParserPackage;
    protected String fClassName;

    protected void collectCodeParms() {
        fLanguageName= pages[0].fLanguageText.getText();
        fPackageName= pages[0].fPackageName;
        fPackageName= Character.toLowerCase(fPackageName.charAt(0)) + fPackageName.substring(1);
        fPackageFolder= fPackageName.replace('.', File.separatorChar);

        String[] subPkgs= fPackageName.split("\\.");
        StringBuffer buff= new StringBuffer();

        for(int i= 0; i < subPkgs.length-1; i++) {
            if (i > 0) buff.append('.');
            buff.append(subPkgs[i]);
        }
        buff.append(".parser");
        fParserPackage= buff.toString();
        fClassName= Character.toUpperCase(fLanguageName.charAt(0)) + fLanguageName.substring(1);
    }

    protected Map getStandardSubstitutions() {
        Map result= new HashMap();

        result.put("$LANG_NAME$", fLanguageName);
        result.put("$CLASS_NAME_PREFIX$", fClassName);
        result.put("$PACKAGE_NAME$", fPackageName);
        return result;
    }
}
