package org.eclipse.imp.wizards;

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
	
}
