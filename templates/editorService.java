package $PACKAGE_NAME$;

import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.SimpleLPGParseController;
import org.eclipse.imp.services.IEditorService;
import org.eclipse.imp.services.base.EditorServiceBase;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

public class $SERVICE_CLASS_NAME$  extends EditorServiceBase implements IEditorService
{

	/**
	 * @return	A value from the enumeration IModelListener.AnalysisRequired,
	 * 			reflecting the dependence of the implementation of this
	 * 			service on other analyses that need to be performed
	 * 			before this one can be executed correctly
	 */
    public AnalysisRequired getAnalysisRequired() {
    	return IModelListener.AnalysisRequired.$ANALYSIS_LEVEL$;
    }
	
    
	/**
	 * This method will be called when the AST maintained by the parseController
	 * has been updated (subject to the completion of analyses on which this
	 * service depends and on the apparent availability of time in which to
	 * perform this analysis)
	 */
    public void update(IParseController parseController, IProgressMonitor monitor) {
    	// START_HERE
    	// Fill in the implementation of your editor service
    	// (or dispatch to a component that will perform the service)
    	//
    	// NOTE:  In addition to the parse controller that is provided when this
    	// method is invoked, the UniversalEditor instance with which this service
    	// instance is associated should also be available in the "editor" field
    	// of the base class.
    	//
    	// The following is some example code that serves as a placeholder for
    	// a real implementation and shows the use of the parse controller and
    	// language editor
    	System.out.println("$SERVICE_CLASS_NAME$:  executing service");
    	if (editor == null) {
    		System.out.println("$SERVICE_CLASS_NAME$:  editor is null");
    	} else {
    		try {
    	    	int docLength = editor.getDocumentProvider().getDocument(editor.getEditorInput()).get().length();
        		System.out.println("$SERVICE_CLASS_NAME$:  document length = " + docLength);
    		} catch (Exception e) {
        		System.err.println("$SERVICE_CLASS_NAME$:  error getting document length");
    		}
    	}

    	if (parseController == null) {
    		System.out.println("$SERVICE_CLASS_NAME$:  parse controller is null");
    	} else {
    		try {
    			int numTokens = 0;
    			if (parseController instanceof SimpleLPGParseController) {
    				SimpleLPGParseController lpgController = (SimpleLPGParseController) parseController;
    				numTokens = lpgController.getParser().getParseStream().getSize();
    			} else {
    				Object currAst = parseController.getCurrentAst();
    				int startOffset = parseController.getNodeLocator().getStartOffset(currAst);
    				int endOffset = parseController.getNodeLocator().getEndOffset(currAst);
    				IRegion region = new Region(startOffset, endOffset-startOffset+1);
    				Iterator tokenIterator = parseController.getTokenIterator(region);
    				for (; tokenIterator.hasNext() && numTokens++ > 0; tokenIterator.next());
    			}
           		System.out.println("$SERVICE_CLASS_NAME$:  number of tokens in document = " + numTokens);
    		} catch (Exception e) {
        		System.err.println("$SERVICE_CLASS_NAME$:  error getting number of tokens");
    		}
    	}

    }

}