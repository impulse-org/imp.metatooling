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

import java.awt.event.ActionListener;

import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * The "New" wizard page allows setting the container for the new file as well as the
 * file name. The page will only accept file name without the extension OR with the
 * extension that matches the expected one (g).
 */
public class NewHoverHelperWizardPage extends ExtensionPointWizardPage
{
	protected boolean fUseReferenceResolver = true;
	protected boolean fGenReferenceResolver = false;
	protected String fReferenceResolver = null;
	protected boolean fUseCustomProvider = false;
	protected boolean fGenCustomProvider = false;
	protected String fDocumentationProvider = null;
	
	public NewHoverHelperWizardPage(ExtensionPointWizard wizard) {
		super(wizard, RuntimePlugin.IMP_RUNTIME, "hoverHelper");
		setTitle("New Hover Helper");
		setDescription("This wizard supports the creation of a hover-help service for an IMP-based IDE editor.");
    }
	
    protected void createAdditionalControls(Composite parent) {
    	Button urb = createUseResolverButton(parent);
    	Button grb = createGenResolverButton(parent, urb);
    	createRefResolverClassField(parent, grb);
		Button upb = createUseProviderButton(parent);
		Button gpb = createGenProviderButton(parent, upb);
		createDocProviderClassField(parent, gpb);
    }	


    public boolean getUseReferenceResolver() {
    	return fUseReferenceResolver;
    }
    
    public boolean getGenReferenceResolver() {
    	return fGenReferenceResolver;
    }
    
    public String getReferenceResolver() {
    	return fReferenceResolver;
    }
    
    public Button createUseResolverButton(Composite parent)
    {
		Label resolvedLabel= new Label(parent, SWT.NULL);
		resolvedLabel.setText("Use available\nreference resolver:");
		resolvedLabel.setToolTipText("Option to use an existing reference resolver to use in obtaining text for hover-help messages.");

		final Button cbResolved= new Button(parent, SWT.CHECK);
		cbResolved.setToolTipText(
			"Check this to take advantage of an existing reference resolver in determining hover-help messages.");
		cbResolved.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		    	fUseReferenceResolver = cbResolved.getSelection();
		    }
		    public void widgetDefaultSelected(SelectionEvent e) {}
		});
        cbResolved.setSelection(true);
        fUseReferenceResolver = true;

        // Put some whitespace into the layout
	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		return cbResolved;
    }
    
    
    private Button createGenResolverButton(Composite parent, final Button cbUseResolver)
    {
		final Label genResolverLabel= new Label(parent, SWT.NULL);
		genResolverLabel.setText("Generate a new\nreference resolver:");
		genResolverLabel.setToolTipText("Option to generate a new reference resolver to use in obtaining text for hover-help messages.");
		genResolverLabel.setBackground(parent.getBackground());

		final Button cbGenResolver = new Button(parent, SWT.CHECK);
		cbGenResolver.setToolTipText(
			"Check this to generate a new reference resolver for use in determining hover-help messages.");
		cbGenResolver.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		    	fUseReferenceResolver = cbGenResolver.getSelection();
		    }
		    public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		boolean selected = cbUseResolver.getSelection();
		genResolverLabel.setEnabled(selected);
		cbGenResolver.setSelection(false);
		cbGenResolver.setEnabled(selected);
        fGenReferenceResolver = false;

        // Put some whitespace into the layout
	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	    cbUseResolver.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		    	boolean selected = cbUseResolver.getSelection();
				genResolverLabel.setEnabled(selected);
				//cbGenResolver.setSelection(selected);		// leave as whatever it was
				cbGenResolver.setEnabled(selected);
				// Notify listeners (i.e., Resolver class field) so that they
				// can change their enabled state in sync with ours (can't find
				// a way to enable them to listen for changes in our enabled state,
				// so send them a fictitious selection event instead to prod them
				// to check their state against ours)
				cbGenResolver.notifyListeners(SWT.Selection, null);
		        fGenReferenceResolver = selected;
		    }
		    public void widgetDefaultSelected(SelectionEvent e) {
		    	widgetSelected(e);
		    }
	    });
	    
		return cbGenResolver;
    }
    
    /*
     * For making link text look enabled or disabled; disabling a link
     * doesn't make the text look disabled.  I tried setting it to the
     * foreground color of the associated field, which appears gray in
     * its disabled state, but the foreground color of text in a disabled
     * text field seems to be black, not gray (not sure how they get the
     * gray effect).  So I just set the link text to black or gray
     * explicitly.
     */
    Color black = new Color(null, 0, 0, 0);
    Color gray = new Color(null, 160, 160, 160);
    
    
    private void createRefResolverClassField(Composite parent, final Button cbGenResolver)
    {	
		final String toolTip = "Specify the qualified name of the ReferenceResolver class that will provide customized reference resolution";
		
    	WizardPageField resolverField = createTextField(parent, "Reference Resolver", "New reference-resolver\nimplementation class:",
	    		"The qualified name of the ReferenceResolver class to be generated", 
	    		"", "ClassBrowse", true);
		final Text resolverText = resolverField.fText;
    	final Hyperlink resolverLink = resolverField.fLink;
		final Button browseButton = resolverField.fButton;
    	
    	boolean enabled = cbGenResolver.getEnabled();
    	boolean selected = cbGenResolver.getSelection();
		resolverText.setEditable(enabled && selected);
		resolverText.setEnabled(enabled && selected);
		resolverLink.setEnabled(enabled && selected);
		resolverLink.setForeground((enabled && selected) ? black : gray); 
		resolverLink.setBackground(parent.getBackground());
		resolverLink.setToolTipText(toolTip);
		browseButton.setEnabled(enabled && selected);
		fReferenceResolver = resolverText.getText();

		resolverText.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
				    Text text= (Text) e.widget;
				    fReferenceResolver = text.getText();
				}
			}
		);
		
		fLanguageText.addModifyListener(
		    new ModifyListener() {
				public void modifyText(ModifyEvent e) {
				resolverText.setText(getDefaultRefResolverName());
			}}
		);
		

		cbGenResolver.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		       	boolean enabled = cbGenResolver.getEnabled();
		    	boolean selected = cbGenResolver.getSelection();
				resolverText.setEditable(enabled && selected);
				resolverText.setEnabled(enabled && selected);
				resolverLink.setEnabled(enabled && selected);
				resolverLink.setForeground((enabled && selected) ? black : gray); 
				browseButton.setEnabled(enabled && selected);
				fReferenceResolver = resolverText.getText();
		    }
		    public void widgetDefaultSelected(SelectionEvent e) {}
		});		
		
		
        resolverText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {	
        	if (fDescriptionText != null)
        	    fDescriptionText.setText(toolTip);
            }
        });
    }
    
    
    /**
     * @return The default name of the hover-helper reference-resolver class
     */
    public String getDefaultRefResolverName()
    {
    	String languageName = fLanguageText.getText();
    	if (languageName == null || languageName.length() == 0) {
    		return "imp.documentationProvider.HelperReferenceResolver";
    	}
    	return languageName + ".imp.documentationProvider.HelperReferenceResolver";
    }
    
    
    
    public Button createUseProviderButton(Composite parent)
    {
		Label customizedLabel= new Label(parent, SWT.NULL);
		customizedLabel.setText("Use available\ncontent provider:");
		customizedLabel.setToolTipText("Option to use an existing documentation provider to obtain text for hover-help messages");
	
		final Button cbCustomizedContent= new Button(parent, SWT.CHECK);
		cbCustomizedContent.setToolTipText("Check this to take advantage of an existing documentation provider in determining hover-help messages.");
		cbCustomizedContent.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		    	fUseCustomProvider = cbCustomizedContent.getSelection();
		    }
		    public void widgetDefaultSelected(SelectionEvent e) {}
		});
        cbCustomizedContent.setSelection(true);
        fUseCustomProvider = true;

        // Put some whitespace into the layout
	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		return cbCustomizedContent;
    }
    
	
    public boolean getUseCustomProvider() {
    	return fUseCustomProvider;
    }
    
    public boolean getGenCustomProvider() {
    	return fGenCustomProvider;
    }
	
    public String getDocumentationProvider() {
    	return fDocumentationProvider;
    }
    
    
    
    private Button createGenProviderButton(Composite parent, final Button cbUseResolver)
    {
		final Label genProviderLabel= new Label(parent, SWT.NULL);
		genProviderLabel.setText("Generate a new\ndocumentation provider:");
		genProviderLabel.setToolTipText("Option to generate a new documentation provider for text for hover-help messages.");
		genProviderLabel.setBackground(parent.getBackground());

		final Button cbGenProvider = new Button(parent, SWT.CHECK);
		cbGenProvider.setToolTipText(
			"Check this to generate a new documentation provider for text in hover-help messages.");
		cbGenProvider.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		    	fUseCustomProvider = cbGenProvider.getSelection();
		    }
		    public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		boolean selected = cbUseResolver.getSelection();
		genProviderLabel.setEnabled(selected);
		cbGenProvider.setSelection(false);
		cbGenProvider.setEnabled(selected);
        fGenCustomProvider = false;

        // Put some whitespace into the layout
	    new Label(parent, SWT.NULL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	    cbUseResolver.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		    	boolean selected = cbUseResolver.getSelection();
				genProviderLabel.setEnabled(selected);
				//cbGenProvider.setSelection(selected);				// leave as whatever it was
				cbGenProvider.setEnabled(selected);
				// Notify listeners (i.e., Provider class field) so that they
				// can change their enabled state in sync with ours (can't find
				// a way to enable them to listen for changes in our enabled state,
				// so send them a fictitious selection event instead to prod them
				// to check their state against ours)
				cbGenProvider.notifyListeners(SWT.Selection, null);
				fGenCustomProvider = selected;
		    }
		    public void widgetDefaultSelected(SelectionEvent e) {
		    	widgetSelected(e);
		    }
	    });
	    
		return cbGenProvider;
    }
    
    
    protected void createDocProviderClassField(Composite parent, final Button cbGenProvider)
    {	
		final String toolTip = "Specify the qualified name of the DocumentationProvider class that will provide customized hover-help texts";
		
    	WizardPageField providerField = createTextField(parent, "Dcoumentation Provider", "New content-provider\nimplementation class:",
	    		"The qualified name of the DocumentationProvider class to be generated", 
	    		"", "ClassBrowse", true);
		final Text providerText = providerField.fText;
    	final Hyperlink providerLink = providerField.fLink;
		final Button browseButton = providerField.fButton;

    	boolean enabled = cbGenProvider.getEnabled();
    	boolean selected = cbGenProvider.getSelection();
		providerText.setEditable(enabled && selected);
		providerText.setEnabled(enabled && selected);
		providerLink.setEnabled(enabled && selected);
		providerLink.setForeground((enabled && selected) ? black : gray); 
		providerLink.setBackground(parent.getBackground());
		providerLink.setToolTipText(toolTip);
		browseButton.setEnabled(enabled && selected);
		fDocumentationProvider = providerText.getText();
		
		providerText.addModifyListener(
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
//				    Text text= (Text) e.widget;	
//				    String languageName = text.getText();
				    providerText.setText(getDefaultDocProviderName());
				}
			}
		);

		cbGenProvider.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		    	boolean enabled = cbGenProvider.getEnabled();
		    	boolean selected = cbGenProvider.getSelection();
				providerText.setEditable(enabled && selected);
				providerText.setEnabled(enabled && selected);
				providerLink.setEnabled(enabled && selected);
				providerLink.setForeground((enabled && selected) ? black : gray); 
				browseButton.setEnabled(enabled && selected);
				fDocumentationProvider = providerText.getText();
		    }
		    public void widgetDefaultSelected(SelectionEvent e) {}
		});

        providerText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {	
        	if (fDescriptionText != null)
        	    fDescriptionText.setText(toolTip);
            }
        });
    }

    
    /**
     * @return The default name of the hover-helper documentation provider class
     */
    public String getDefaultDocProviderName()
    {
    	String languageName = fLanguageText.getText();
	    	if (languageName == null || languageName.length() == 0) {
	    		return "imp.documentationProvider.HelperDocumentationProvider";
	    	}
	    	return languageName + ".imp.documentationProvider.HelperDocumentationProvider";
    }
    

	
}
