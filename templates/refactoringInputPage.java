package $PACKAGE_NAME$;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class $REFACTORING_PREFIX$InputPage extends UserInputWizardPage {
    public $REFACTORING_PREFIX$InputPage(String name) {
        super(name);
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite result= new Composite(parent, SWT.NONE);
        setControl(result);
        GridLayout layout= new GridLayout();
        layout.numColumns= 1;
        result.setLayout(layout);

        final Button deleteButton= new Button(result, SWT.CHECK);

        deleteButton.setText("Delete declarations after inlining");
        deleteButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        deleteButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                // Set a parameter on the refactoring, e.g. get$REFACTORING_PREFIX$Refactoring().setDoDelete(deleteButton.getSelection());
            }
            public void widgetDefaultSelected(SelectionEvent e) { }
        });
    }

    private $REFACTORING_PREFIX$Refactoring get$REFACTORING_PREFIX$Refactoring() {
        return ($REFACTORING_PREFIX$Refactoring) getRefactoring();
    }
}
