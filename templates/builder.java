package $PACKAGE_NAME$;

import java.io.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uide.core.SAFARIBuilderBase;
import org.eclipse.uide.model.ISourceProject;
import org.eclipse.uide.model.ModelFactory;
import org.eclipse.uide.model.ModelFactory.ModelException;
import org.eclipse.uide.runtime.SAFARIPluginBase;

//import $LANG_NAME$.$CLASS_NAME_PREFIX$Plugin;
//import $PLUGIN_PACKAGE$.$CLASS_NAME_PREFIX$Plugin;
import $PLUGIN_PACKAGE$.$PLUGIN_CLASS$;

import org.eclipse.uide.core.Language;
import org.eclipse.uide.core.LanguageRegistry;

import org.eclipse.uide.builder.BuilderUtils;
import org.eclipse.uide.builder.MarkerCreator;
import org.eclipse.uide.parser.IParseController;
import $PARSER_PKG$.$CLASS_NAME_PREFIX$ParseController;

/**
 * @author
 */
public class $BUILDER_CLASS_NAME$ extends SAFARIBuilderBase {
    /**
     * Extension ID of the $CLASS_NAME_PREFIX$ builder. Must match the ID in the corresponding
     * extension definition in plugin.xml.
     * SMS 22 Mar 2007:  If that ID is set through the NewBuilder wizard, then so must this one be.
     */
	// SMS 28 Mar 2007:  Make plugin class name totally parameterized
	public static final String BUILDER_ID= $PLUGIN_CLASS$.kPluginID + ".$BUILDER_ID$";
	// SMS 28 Mar 2007:  Make problem id parameterized (rather than just ".problem") so that
	// it can be given a builde-specific value (not simply composed here using the builder id
	// because the problem id is also needed in ExtensionPointEnabler for adding the marker
	// extension to the plugin.xml file)
    public static final String PROBLEM_MARKER_ID= $PLUGIN_CLASS$.kPluginID + ".$PROBLEM_ID$";
    
    // SMS 11 May 2006
    public static final String LANGUAGE_NAME = "$LANG_NAME$";
    public static final Language LANGUAGE = LanguageRegistry.findLanguage(LANGUAGE_NAME);
    public static final String[] EXTENSIONS = LANGUAGE.getFilenameExtensions();


    protected SAFARIPluginBase getPlugin() {
        //return $CLASS_NAME_PREFIX$Plugin.getInstance();
        return $PLUGIN_CLASS$.getInstance();
    }

    protected String getErrorMarkerID() {
        return PROBLEM_MARKER_ID;
    }

    protected String getWarningMarkerID() {
        return PROBLEM_MARKER_ID;
    }

    protected String getInfoMarkerID() {
        return PROBLEM_MARKER_ID;
    }


    // SMS 11 May 2006
    // Incorporated realisitic handling of filename extensions
    // using information recorded in the language registry
    protected boolean isSourceFile(IFile file) {
        IPath path= file.getRawLocation();
        if (path == null) return false;

        String pathString = path.toString();
        if (pathString.indexOf("/bin/") != -1) return false;
        
        for (int i = 0; i < EXTENSIONS.length; i++) {
            if (EXTENSIONS[i].equals(path.getFileExtension())) return true;
        }
        return false;
    }


    /**
     * @return true iff the given file is a source file that this builder should scan
     * for dependencies, but not compile as a top-level compilation unit.<br>
     * <code>isNonRootSourceFile()</code> and <code>isSourceFile()</code> should never
     * return true for the same file.
     */
    protected boolean isNonRootSourceFile(IFile resource)
    {
    	// TODO:  If your language has non-root source files (e.g., header files), then
    	// reimplement this method to test for those
        System.err.println("$BUILDER_CLASS_NAME$.isNonRootSourceFile(..) returning FALSE by default");
        return false;
    }

    /**
     * Collects compilation-unit dependencies for the given file, and records
     * them via calls to <code>fDependency.addDependency()</code>.
     */
    protected void collectDependencies(IFile file)
    {   
    	// TODO:  If your langauge has inter-file dependencies then reimplement
    	// this method to collect those
        System.err.println("$BUILDER_CLASS_NAME$.collectDependencies(..) doing nothing by default");
        return;
    }

    
    protected boolean isOutputFolder(IResource resource) {
        return resource.getFullPath().lastSegment().equals("bin");
    }

    
    protected void compile(final IFile file, IProgressMonitor monitor) {
        try {
            // START_HERE
            System.out.println("Builder.compile with file = " + file.getName());
            //$CLASS_NAME_PREFIX$Compiler compiler= new $CLASS_NAME_PREFIX$Compiler(PROBLEM_MARKER_ID);
            //compiler.compile(file, monitor);
            // Here we provide a substitute for the compile method that simply
            // runs the parser in place of the compiler but creates problem
            // markers for errors that will show up in the problems view
            runParserForCompiler(file, monitor);

            doRefresh(file.getParent());
        } catch (Exception e) {
            getPlugin().writeErrorMsg(e.getMessage());

            e.printStackTrace();
        }
    }

    protected void runParserForCompiler(final IFile file, IProgressMonitor monitor) {
        try {
            // Parse controller is the "compiler" here; parses and reports errors
            IParseController parseController = new $CLASS_NAME_PREFIX$ParseController();

            // Marker creator handles error messages from the parse controller (and
            // uses the parse controller to get additional information about the errors)
            MarkerCreator markerCreator = new MarkerCreator(file, parseController, 	PROBLEM_MARKER_ID);

            // If we have a kind of parser that might be receptive, tell it
            // what types of problem marker the builder will create
            parseController.addProblemMarkerType(getErrorMarkerID());
            
            // Need to tell the parse controller which file in which project to parse
            // and also the message handler to which to report errors
            IProject project= file.getProject();
    		ISourceProject sourceProject = null;
        	try {
        		sourceProject = ModelFactory.open(project);
        	} catch (ModelException me){
                System.err.println("$CLASS_NAME_PREFIX$ParseController.runParserForComplier(..):  Model exception:\n" + me.getMessage() + "\nReturning without parsing");
                return;
        	}
            parseController.initialize(file.getProjectRelativePath(), sourceProject, markerCreator);
	
            // Get file contents for parsing
            //BuilderUtils.extractContentsToString(file.getLocation().toString());
            String contents = getFileContents(file);
            
            	// Finally parse it
            parseController.parse(contents, false, monitor);

            doRefresh(file.getParent());
        } catch (Exception e) {
            getPlugin().writeErrorMsg(e.getMessage());
            e.printStackTrace();
        }
    }
    
	// Copied from the compiler template, so that the builder (here)
    // and compiler (when used) will evaluate the same string
    public String getFileContents(IFile file) {
        char[] buf= null;
        try {
            File javaFile= new File(file.getLocation().toOSString());
            FileReader fileReader= new FileReader(javaFile);
            int len= (int) javaFile.length();

            buf= new char[len];
            fileReader.read(buf, 0, len);
            return new String(buf);
        } catch(FileNotFoundException fnf) {
            System.err.println(fnf.getMessage());
            return "";
        } catch(IOException io) {
            System.err.println(io.getMessage());
            return "";
        }
    }

}
