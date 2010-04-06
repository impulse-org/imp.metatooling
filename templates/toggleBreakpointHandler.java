package $PACKAGE_NAME$;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.services.IToggleBreakpointsHandler;

/**
 * This class is responsible for passing requests to manipulate breakpoints (create them,
 * delete them, enable/disable them) to the underlying debugger framework.
 */
public class $HANDLER_CLASS_NAME$ implements IToggleBreakpointsHandler {
    public void clearLineBreakpoint(IFile file, int lineNumber) throws CoreException {
        // START_HERE
    }

    public void setLineBreakpoint(IFile file, int lineNumber) throws CoreException {
        // START_HERE
    }

    public void disableLineBreakpoint(IFile file, int lineNumber) throws CoreException {
        // START_HERE
    }

    public void enableLineBreakpoint(IFile file, int lineNumber) throws CoreException {
        // START_HERE
    }
}
