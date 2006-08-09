/**
 * 
 */
package org.eclipse.uide.wizards;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.plugin.PluginElement;

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
        
        // SMS 17 May 2006
        // Need to get a name for the AST package and AST node type for use in
        // the NewFoldingUpdater wizard
        // Note:  The method used assumes that these are the default values
        // (if that assumption is wrong, then the generated folding service won't
        // compile, but if we don't provide any values then it won't compile in
        // any case--specifically because substitutions for these parameters will
        // not have been made)
        result = getASTInformation();
        
        // continuing with original:
        result.put("$LANG_NAME$", fLanguageName);
        result.put("$CLASS_NAME_PREFIX$", fClassName);
        result.put("$PACKAGE_NAME$", fPackageName);

        
        return result;
    }
    
    
    /**
     * Return a Map containing the the names of the AST package and class
     * bound to "well-known" symbols, "$AST_PACKAGE$" and "$AST_CLASS$", 
     * respectively.
     * 
     * WARNING:  The names returned are currently the DEFAULT names (which
     * should be the most commonly occurring but which may not be appropriate
     * in general).
     * 
     * The actual values for the AST package and class are generated in the
     * NewParser wizard but are not (yet) stored anywhere for reference by
     * other wizards.  There is at least one other wizard, the NewFoldingUpdater
     * wizard, which does need these names to complete a template.  In order
     * to make available some reasonable values for these names, this method
     * recomputes the names using the same assumptions as are used for the
     * default case in the NewParser wizard.
     * 
     * TODO:  Provide a means for (more) persistently maintaining the names
     * of hte AST package and class in such a way that they can become part
     * of the "standard substitutions."  (ALTERNATIVELY:  the class could just
     * be obtained in wizards where needed, in which case it need not be part
     * of the standard substitutions.)
     * 
     * @return	A Map that contains two valuse:  the name of the package that
     * 			contains the AST class, and the name of the AST class.
     * 			These values keyed, respectively, by the symbols "$AST_PACKAGE$"
     * 			and "$AST_NODE$".
     * 
     * Updates:  Stan Sutton, 9 Aug 2006
     * 			Changed return from $AST_CLASS$ to $AST_NODE$ since the latter
     * 			is the symbol more commonly used (and the one on which I will
     * 			try to standardize)
     * 
     * @author	Stan Sutton
     * @since	17 May 2006
     */
    protected Map getASTInformation()
    {
    	Map result = new HashMap();
    	
        // Get the extension that represents the parser
        IPluginModelBase pluginModel = pages[0].getPluginModel();
        IExtensions extensionsThing = pluginModel.getExtensions();
        IPluginExtension[] extensions = extensionsThing.getExtensions();
        IPluginExtension parserExtension = null;
        for (int i = 0; i < extensions.length; i++) {
        	if(extensions[i].getPoint().equals("org.eclipse.uide.runtime.parser")) {
        		parserExtension = extensions[i];
        		break;
        	}
        }

        // Get the plugin element that represents the class of the parser
        PluginElement parserPluginElement = null;
        if (parserExtension != null) {
        	IPluginObject[] children = parserExtension.getChildren();
        	for (int i = 0; i < children.length; i++) {
        		if(children[i].getName().equals("parser")) {
        			parserPluginElement = (PluginElement) children[i];
        				break;
        		}
        	}
        }
        if (parserPluginElement == null) return result;
        
        // Assume that the AST package name is the parser package name extended 
        // with ".Ast" (this is the default when auto-generated)
        IPluginAttribute astPackageAttribute = parserPluginElement.getAttribute("class");
        String astPackageName = astPackageAttribute.getValue();
        astPackageName = astPackageName.substring(0, astPackageName.lastIndexOf('.'));
        astPackageName += ".Ast";
        String astClassName = "ASTNode";
        
        // Check whether this hypothetical AST class actually exists
        // (maybe someday ...)
        
        // Save these values in the substitutions map
        result.put("$AST_PACKAGE$", astPackageName);
        result.put("$AST_NODE$", astClassName);
        
        return result;
    }
    
}
