/**
 * 
 */
package org.eclipse.uide.wizards;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.safari.jikespg.parser.ParseController;
import org.eclipse.safari.jikespg.parser.JikesPGParser.JikesPG;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.uide.core.LanguageRegistry;
import org.eclipse.uide.runtime.RuntimePlugin;

public class NewFancyTokenColorer extends CodeServiceWizard {
    private Map<String, FontData> fStyleMap= new HashMap<String, FontData>(); // used in generating code
    private Map<String, RGB> fColorMap= new HashMap<String, RGB>();

    private static final String DEFAULT_TOKEN_COLORER= "DefaultTokenColorer";

    private final class FancyTokenColorerPage extends ExtensionPointWizardPage {
	private FancyTokenColorerPage(ExtensionPointWizard owner, String pluginid, String pointid) {
	    super(owner, pluginid, pointid);
	}
	
	@Override
	protected void createAdditionalControls(Composite parent) {
	    final Button fancyButton= new Button(parent, SWT.PUSH);
	    fancyButton.setText("Token &Styles...");
	    fancyButton.addSelectionListener(new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
		    IProject project= getProject();
		    IPath grammarPath= findGrammarPath(project);
		    if (grammarPath == null) {
			MessageDialog.openError(e.widget.getDisplay().getActiveShell(), "Error", "Unable to find grammar in project " + project.getName());
			return;
		    }
		    IFile file= project.getFile(grammarPath);
		    if (!file.exists()) {
			MessageDialog.openError(e.widget.getDisplay().getActiveShell(), "Error", "Unable to open grammar file " + file.getLocation().toOSString() + " in project " + project.getName());
			return;
		    }
		    ParseController ctlr= new ParseController();
		    ctlr.initialize(grammarPath, project, null);
		    IFileEditorInput fileInput= new FileEditorInput(file);
		    FileDocumentProvider fdp= new FileDocumentProvider();
		    try {
			fdp.connect(fileInput);
			IDocument document= fdp.getDocument(fileInput);
			JikesPG jpg= (JikesPG) ctlr.parse(document.get(), false, new NullProgressMonitor());
			TokenStyleDialog tsd= new TokenStyleDialog(jpg, fancyButton.getShell());

			if (tsd.open() == TokenStyleDialog.OK) {
			    setStyles(tsd.getStyleMap());
			    setColors(tsd.getColorMap());
			}
			fdp.disconnect(fileInput);
		    } catch (CoreException e2) {
			System.err.println("oops: " + e2.getMessage());
		    }
		}
	    });
	}

	protected IPath findGrammarPath(IProject project) {
	    final IFile[] files= new IFile[1];
	    try {
                project.accept(new IResourceVisitor() {
                    public boolean visit(IResource resource) throws CoreException {
                	if (resource instanceof IFile) {
                	    IFile file= (IFile) resource;
                	    if (file.getFileExtension().equals("g"))
                		files[0]= file;
                	}
                	return true;
                    }
                });
	    } catch (CoreException e) {
		return new Path("");
	    }
	    return (files[0] != null) ? files[0].getProjectRelativePath() : new Path("");
	}
    }

    protected void setStyles(Map<String, FontData> styleMap) {
	fStyleMap= styleMap;
    }

    protected void setColors(Map<String, RGB> colorMap) {
	fColorMap= colorMap;
    }

    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new FancyTokenColorerPage(this, RuntimePlugin.UIDE_RUNTIME, "tokenColorer"), });
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] {
                "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.uide.runtime", "org.eclipse.ui", "org.eclipse.jface.text", 
                "org.eclipse.ui.editors", "org.eclipse.ui.workbench.texteditor", "lpg.runtime" });
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
//	ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
//	IProject project= page.getProject();
	Map subs= getStandardSubstitutions();

	subs.put("$PARSER_PKG$", fParserPackage);

//	WizardPageField field= pages[0].getField("class");
//	String qualifiedClassName= field.fValue;
//	String className= qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.') + 1);
	subs.remove("$COLORER_CLASS_NAME$");
	subs.put("$COLORER_CLASS_NAME$", fFullClassName);

//	subs.remove("$PACKAGE_NAME$");
//	String packageName= qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
	subs.put("$PACKAGE_NAME$", fPackageName);

	// TODO Need to add the base language plugin (if any) as a plugin dependency
	final String baseLang= ExtensionPointEnabler.findServiceAttribute(RuntimePlugin.UIDE_RUNTIME + ".languageDescription", fLanguageName, "language", "derivedFrom", "");
	final String baseLangServiceImpl= ExtensionPointEnabler.findServiceImplClass(RuntimePlugin.UIDE_RUNTIME + ".tokenColorer", baseLang, DEFAULT_TOKEN_COLORER);
	subs.put("$BASE_CLASS$", baseLangServiceImpl);

	subs.put("$TOKEN_ATTRIBUTE_DECLS$", computeTokenAttribDecls());
	subs.put("$TOKEN_ATTRIBUTE_INITS$", computeTokenAttribInits());
	subs.put("$TOKEN_ATTRIBUTE_CASES$", computeTokenAttribCases());

//	String packageFolder= packageName.replace('.', File.separatorChar);
	String colorerTemplateName = "colorer_fancy.java";
	IFile colorerSrc= createFileFromTemplate(fFullClassName + ".java", colorerTemplateName, fPackageFolder, subs, fProject, mon);

	editFile(mon, colorerSrc);
    }

    private String attributeNameForToken(String token) {
	return token + "Attribute";
    }

    private String symbolNameForToken(String token) {
	return "TK_" + token;
    }

    private String computeTokenAttribCases() {
	StringBuffer buff= new StringBuffer();
	for(Iterator iter= fStyleMap.keySet().iterator(); iter.hasNext(); ) {
	    String token= (String) iter.next();
	    buff.append("    case ");
	    buff.append(symbolNameForToken(token));
	    buff.append(":\n");
	    buff.append("        return ");
	    buff.append(attributeNameForToken(token));
	    buff.append(";\n");
	}
	return buff.toString();
    }

    private String computeTokenAttribInits() {
	StringBuffer buff= new StringBuffer();
	for(Iterator iter= fStyleMap.keySet().iterator(); iter.hasNext(); ) {
	    String token= (String) iter.next();
	    FontData fd= fStyleMap.get(token);
	    RGB rgb= fColorMap.get(token);

	    buff.append("        ");
	    buff.append(attributeNameForToken(token));
	    buff.append(" = new TextAttribute(new Color(display, ");
	    buff.append(rgb.red);
	    buff.append(',');
	    buff.append(rgb.green);
	    buff.append(',');
	    buff.append(rgb.blue);
	    buff.append("), null, ");
	    buff.append(mapFontDataToSWTStyle(fd));
	    buff.append(");\n");
	}
	return buff.toString();
    }

    private String mapFontDataToSWTStyle(FontData fd) {
	StringBuffer buff= new StringBuffer();
	int style= fd.getStyle();

	buff.append("0");
	if ((style & SWT.NORMAL) != 0) buff.append(" | SWT.NORMAL");
	if ((style & SWT.BOLD)   != 0) buff.append(" | SWT.BOLD");
	if ((style & SWT.ITALIC) != 0) buff.append(" | SWT.ITALIC");
	return buff.toString();
    }

    private String computeTokenAttribDecls() {
	if (fStyleMap.keySet().isEmpty())
	    return "";
	StringBuffer buff= new StringBuffer();
	buff.append("    protected TextAttribute ");
	for(Iterator iter= fStyleMap.keySet().iterator(); iter.hasNext(); ) {
	    String token= (String) iter.next();
	    buff.append(attributeNameForToken(token));
	    if (iter.hasNext()) buff.append(", ");
	}
	buff.append(";\n");
	return buff.toString();
    }
}
