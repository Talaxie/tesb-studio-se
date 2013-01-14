// ============================================================================
//
// Copyright (C) 2006-2013 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.services.ui;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.model.services.ServicesFactory;
import org.talend.repository.services.utils.ESBRepositoryNodeType;
import org.talend.repository.ui.utils.ConnectionContextHelper;
import org.talend.repository.ui.wizards.CheckLastVersionRepositoryWizard;
import org.talend.repository.ui.wizards.metadata.connection.Step0WizardPage;

/**
 * hwang class global comment. Detailled comment
 */
public class ESBWizard extends CheckLastVersionRepositoryWizard {

    /** Main page. */
    private Step0WizardPage mainPage;

    private OpenWSDLPage wsdlPage;

    /** Created project. */
    private ServiceItem serviceItem;

    private Property property;

    private RepositoryNode node = null;

    /**
     * Constructs a new NewProjectWizard.
     * 
     * @param author Project author.
     * @param server
     * @param password
     */
    public ESBWizard(IWorkbench workbench, boolean creation, ISelection selection) {
        super(workbench, creation);
        this.selection = selection;
        setNeedsProgressMonitor(true);
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        if (obj instanceof RepositoryNode) {
            node = (RepositoryNode) obj;
        } else {
            return;
        }

        switch (node.getType()) {
        case SIMPLE_FOLDER:
        case REPOSITORY_ELEMENT:
            pathToSave = RepositoryNodeUtilities.getPath(node);
            break;
        case SYSTEM_FOLDER:
            pathToSave = new Path(""); //$NON-NLS-1$
            break;
        }

        switch (node.getType()) {
        case SIMPLE_FOLDER:
        case SYSTEM_FOLDER:
            this.property = PropertiesFactory.eINSTANCE.createProperty();
            this.property.setAuthor(((RepositoryContext) CoreRuntimePlugin.getInstance().getContext()
                    .getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
            this.property.setVersion(VersionUtils.DEFAULT_VERSION);
            this.property.setStatusCode(""); //$NON-NLS-1$

            serviceItem = ServicesFactory.eINSTANCE.createServiceItem();

            serviceItem.setProperty(property);
            property.setItem(serviceItem);
            break;

        case REPOSITORY_ELEMENT:
            this.property = node.getObject().getProperty();
            serviceItem = (ServiceItem) node.getObject().getProperty().getItem();
            // set the repositoryObject, lock and set isRepositoryObjectEditable
            setRepositoryObject(node.getObject());
            isRepositoryObjectEditable();
            initLockStrategy();
            break;
        }
        // initialize the context mode
        ConnectionContextHelper.checkContextMode(connectionItem);

    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        mainPage = new Step0WizardPage(property, pathToSave, ESBRepositoryNodeType.SERVICES, false, true);
        wsdlPage = new OpenWSDLPage(node, pathToSave, serviceItem, "Edit WSDL", creation);
        addPage(mainPage);
        addPage(wsdlPage);
        setWindowTitle("Services"); //$NON-NLS-1$
        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(EImage.DEFAULT_IMAGE));
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean performFinish() {
        // IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        // try {
        // property.setId(repositoryFactory.getNextId());
        // serviceItem.setServiceConnection(ServicesFactory.eINSTANCE.createServiceConnection());
        // repositoryFactory.create(serviceItem, mainPage.getDestinationPath());
        // } catch (PersistenceException e) {
        //            MessageDialog.openError(getShell(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$
        // ExceptionHandler.process(e);
        // }
        return wsdlPage.finish();
    }

    /**
     * Getter for project.
     * 
     * @return the project
     */
    public ServiceItem getBean() {
        return this.serviceItem;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#canFinish()
     */
    @Override
    public boolean canFinish() {
        return super.canFinish() && wsdlPage.isPageComplete();
    }

}
