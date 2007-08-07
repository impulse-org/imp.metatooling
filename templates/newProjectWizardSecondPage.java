package $PACKAGE_NAME$;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.jdt.internal.ui.wizards.ClassPathDetector;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.imp.core.ProjectNatureBase;
import org.eclipse.imp.wizards.NewProjectWizardSecondPage;
import org.osgi.framework.Bundle;

public class $CLASS_NAME_PREFIX$ProjectWizardSecondPage extends NewProjectWizardSecondPage {
    public $CLASS_NAME_PREFIX$ProjectWizardSecondPage($CLASS_NAME_PREFIX$ProjectWizardFirstPage firstPage) {
	super(firstPage);
    }

    protected ProjectNatureBase getProjectNature() {
	return new $NATURE_CLASS_NAME$();
    }

    protected IPath getLanguageRuntimePath() {
	Bundle langRuntimeBundle= Platform.getBundle("$LANG_NAME$.runtime");
	String bundleVersion= (String) langRuntimeBundle.getHeaders().get("Bundle-Version");
	IPath langRuntimePath= new Path("ECLIPSE_HOME/plugins/$LANG_NAME$.runtime_" + bundleVersion + ".jar");

	return langRuntimePath;
    }
}
