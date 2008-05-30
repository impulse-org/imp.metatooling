package $PACKAGE_NAME$;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.SimpleLPGParseController;
import org.eclipse.imp.services.IEditorService;
import org.eclipse.imp.services.base.EditorServiceBase;
import org.eclipse.ui.IEditorInput;

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
    }

}