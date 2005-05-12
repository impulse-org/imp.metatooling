package org.eclipse.uide.wizards;
/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.uide.core.ErrorHandler;

/**
 * This wizard creates a new file resource in the provided container. 
 * The wizard creates one file with the extension "g". 
 */

public class ExtensionPointWizard extends Wizard implements INewWizard {
	
	protected int currentPage;
	protected ExtensionPointWizardPage pages[];
	protected int NPAGES;
	
	public ExtensionPointWizard() {
		super();
		setNeedsProgressMonitor(true);
	}	
	
	public int getPageCount() {
		return NPAGES;
	}
		
	protected void addPages(ExtensionPointWizardPage[] pages) {
		this.pages = pages;
		NPAGES = pages.length;
		for (int n=0; n<pages.length; n++) {
			addPage(pages[n]);
		}
	}

	public IWizardPage getPreviousPage(IWizardPage page) {
		if (currentPage == 0)
			return null;
		return pages[currentPage];
	}
	
	public IWizardPage getNextPage(IWizardPage page) {
		if (currentPage == pages.length-1)
			return null;
		return pages[++currentPage];
	}
	
	public boolean canFinish() {
		return pages[currentPage].canFlipToNextPage() && 
                (pages.length == 1 || currentPage > 0);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				IWorkspaceRunnable wsop = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						try {
							for (int n = 0; n < pages.length; n++) {
								ExtensionPointWizardPage page = pages[n];
								if (!page.hasBeenSkipped())
									ExtensionPointEnabler.enable(page, monitor);
							}
						} catch (Exception e) {
							ErrorHandler.reportError("Could not add extension points", e);
						} finally {
							monitor.done();
						}
					}
				};
				try {
					ResourcesPlugin.getWorkspace().run(wsop, monitor);
				} catch (Exception e) {
					ErrorHandler.reportError("Could not add extension points", e);
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			ErrorHandler.reportError("Error", realException);
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}
	
	public void setPage(int page) {
		currentPage = page;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

}