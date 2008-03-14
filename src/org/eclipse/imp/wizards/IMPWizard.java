/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

package org.eclipse.imp.wizards;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jface.wizard.Wizard;

public class IMPWizard extends Wizard {

    protected int currentPage;
	
	
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

	
    public void setPage(int page) {
    	currentPage= page;
    }
    
    
    // SMS 13 Apr 2007
    // A step toward relaxing assumptions about the location
    // of source files within the project
    public static String getProjectSourceLocation() {
    		return "src/";
    }

	
    
    public static String getProjectSourceLocation(IProject project) {
		try {
			if (project == null)
				return null;
			JavaModel jm = JavaModelManager.getJavaModelManager().getJavaModel();
			IJavaProject jp = jm.getJavaProject(project);
			if (jp == null)
				return null;
			else {
				IPackageFragmentRoot[] roots = jp.getPackageFragmentRoots();
				for (int i = 0; i < roots.length; i++) {
					if (roots[i].getCorrespondingResource() instanceof IFolder) {
						IPath lcnPath = roots[i].getPath();
						lcnPath = lcnPath.removeFirstSegments(1);
						String lcn = lcnPath.toString();
						if (lcn.startsWith("/"))
							lcn = lcn.substring(1);
						if (!lcn.endsWith("/"))
							lcn = lcn + "/";
						return lcn;
					}
				}
			}
		} catch (JavaModelException e) {
			
		}
		return null;
    }
    
    
    
}
