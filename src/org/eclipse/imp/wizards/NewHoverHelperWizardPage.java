package org.eclipse.imp.wizards;

import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.search.JavaWorkspaceScope;
import org.eclipse.jdt.internal.ui.dialogs.TypeSelectionDialog2;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * The "New" wizard page allows setting the container for the new file as well as the
 * file name. The page will only accept file name without the extension OR with the
 * extension that matches the expected one (g).
 */
public class NewHoverHelperWizardPage extends ExtensionPointWizardPage
{
	protected boolean fResolveReferences = true;
	protected boolean fCustomizeContent = false;
	protected String fDocumentationProvider = null;
	
	public NewHoverHelperWizardPage(ExtensionPointWizard wizard) {
		super(wizard, RuntimePlugin.IMP_RUNTIME, "hoverHelper");
		setTitle("New Hover Helper");
		setDescription("This wizard supports the creation of a hover-help service for an IMP-based IDE editor.");
    }
	
    protected void createAdditionalControls(Composite parent) {
    	createResolveField(parent);
		Button cfb = createCustomizeField(parent);
		//createDocProviderClassField(parent, cfb);
    }	


    public Button createResolveField(Composite parent)
    {
		Label resolvedLabel= new Label(parent, SWT.NULL);
		resolvedLabel.setText("Use available\nreference resolver:");
		resolvedLabel.setToolTipText("Option to use an existing reference resolver to obtain text for hover-help messages");

		final Button cbResolved= new Button(parent, SWT.CHECK);
		cbResolved.setToolTipText(
			"Check this to take advantage of an existing reference resolver in determining hover-help messages.");
		cbResolved.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		    	fResolveReferences = cbResolved.getSelection();
		    }
		    public void widgetDefaultSelected(SelectionEvent e) {}
		});
        cbResolved.setSelection(true);
        fResolveReferences = true;

        // Put some whitespace into the layout
	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		return cbResolved;
    }
    
    
    public boolean getResolveReferences() {
    	return fResolveReferences;
    }
    
    
    public Button createCustomizeField(Composite parent)
    {
		Label customizedLabel= new Label(parent, SWT.NULL);
		customizedLabel.setText("Use available\ncontent provider:");
		customizedLabel.setToolTipText("Option to use an existing documentation provider to obtain text for hover-help messages");
	
		final Button cbCustomizedContent= new Button(parent, SWT.CHECK);
		cbCustomizedContent.setToolTipText("Check this to take advantage of an existing documentation provider in determining hover-help messages.");
		cbCustomizedContent.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		    	fCustomizeContent = cbCustomizedContent.getSelection();
		    }
		    public void widgetDefaultSelected(SelectionEvent e) {}
		});
        cbCustomizedContent.setSelection(true);
        fCustomizeContent = true;

        // Put some whitespace into the layout
	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		return cbCustomizedContent;
    }
    
	
    public boolean getCustomizeContent() {
    	return fCustomizeContent;
    }
	
    
    private void createDocProviderClassField(Composite parent, final Button cfb)
    {	
		final String toolTip = "Specify the qualified name of the DocumentationProvider class that will provide customized hover-help texts";
		
		final Label docProviderLabel = new Label(parent, SWT.NULL);
		docProviderLabel.setText("Content-provider\nimplementation class:");
		docProviderLabel.setBackground(parent.getBackground());
		docProviderLabel.setToolTipText(toolTip);
        
		final Text docProviderText = new Text(parent, SWT.BORDER | SWT.SINGLE);
		docProviderText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		docProviderText.setText(getDefaultDocProviderText());
		
		boolean selected = cfb.getSelection();
		docProviderText.setEditable(selected);
		docProviderText.setEnabled(selected);
		docProviderLabel.setEnabled(selected);
		fDocumentationProvider = docProviderText.getText();

		docProviderText.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
				    Text text= (Text) e.widget;
					fDocumentationProvider = text.getText();
				}
			}
		);
		
		fLanguageText.addModifyListener(
		    new ModifyListener() {
				public void modifyText(ModifyEvent e) {
				    Text text= (Text) e.widget;	
				    String languageName = text.getText();
			    	if (languageName == null || languageName.length() == 0) {
			    		docProviderText.setText("imp.documentationProvider.HelperDocumentationProvider");
			    	}
			    	docProviderText.setText(languageName + ".imp.documentationProvider.HelperDocumentationProvider")	;
				}
			}
		);
		
		final Button browseButton = createClassBrowseButton(parent, docProviderText);
        
		cfb.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		    	boolean selected = cfb.getSelection();
				docProviderText.setEditable(selected);
				docProviderText.setEnabled(selected);
				docProviderLabel.setEnabled(selected);
				browseButton.setEnabled(selected);
		    }
		    public void widgetDefaultSelected(SelectionEvent e) {}
		});

        docProviderText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {	
        	if (fDescriptionText != null)
        	    fDescriptionText.setText(toolTip);
            }
        });
    }

    
    public String getDefaultDocProviderText()
    {
    	String languageName = fLanguageText.getText();
	    	if (languageName == null || languageName.length() == 0) {
	    		return "imp.documentationProvider.HelperDocumentationProvider";
	    	}
	    	return languageName + ".imp.documentationProvider.HelperDocumentationProvider";
    }
    
    
    public String getDocumentationProvider() {
    	return fDocumentationProvider;
    }
    
    
    private Button createClassBrowseButton(Composite container, Text text) {
        Button button= new Button(container, SWT.PUSH);

        button.setText("Browse...");
        button.setData(text);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    IRunnableContext context= PlatformUI.getWorkbench().getProgressService();
                    IJavaSearchScope scope= new JavaWorkspaceScope();
                    TypeSelectionDialog2 dialog= new TypeSelectionDialog2(null, false, context, scope, IJavaSearchConstants.CLASS);

                    if (dialog.open() == TypeSelectionDialog2.OK) {
                        Text text= (Text) e.widget.getData();
                        Object res = dialog.getFirstResult();
                        if (res instanceof BinaryType) {
                        	BinaryType type= (BinaryType) dialog.getFirstResult();
                        	text.setText(type.getFullyQualifiedName());
                        } else if (res instanceof SourceType) {
                        	SourceType type = (SourceType) dialog.getFirstResult();
                        	text.setText(type.getFullyQualifiedName());
                        }
                    }
                } catch (Exception ee) {
                    ErrorHandler.reportError("Could not browse type", ee);
                }
            }
        });
        return button;
    }
	
}
