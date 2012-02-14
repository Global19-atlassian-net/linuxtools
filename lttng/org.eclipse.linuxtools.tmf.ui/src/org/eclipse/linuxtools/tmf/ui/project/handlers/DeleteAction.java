/*******************************************************************************
* Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.IHandlerService;

public class DeleteAction extends Action {

    private static final String DELETE_TRACE_COMMAND_ID = "org.eclipse.linuxtools.tmf.ui.command.project.trace.delete"; //$NON-NLS-1$
    private static final String DELETE_EXPERIMENT_COMMAND_ID = "org.eclipse.linuxtools.tmf.ui.command.project.experiment.delete"; //$NON-NLS-1$

    private IWorkbenchPage page;
    private ISelectionProvider selectionProvider;
    private boolean tracesSelected;
    private boolean experimentsSelected;

    /**
     * Default constructor
     * @param page the workbench page
     * @param selectionProvider the selection provider
     */
    public DeleteAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
        this.page = page;
        this.selectionProvider = selectionProvider;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        ISelection selection = selectionProvider.getSelection();
        if (!selection.isEmpty()) {
            tracesSelected = false;
            experimentsSelected = false;
            IStructuredSelection sSelection = (IStructuredSelection) selection;
            for (Object aSelection : sSelection.toList()) {
                if (aSelection instanceof TmfTraceElement) {
                    tracesSelected = true;
                }
                if (aSelection instanceof TmfExperimentElement) {
                    experimentsSelected = true;
                }
            }
            if (tracesSelected && experimentsSelected) {
                return false;
            }
            return (tracesSelected || experimentsSelected); 
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        try {
            IHandlerService handlerService = (IHandlerService) page.getActivePart().getSite().getService(IHandlerService.class);
            if (tracesSelected) {
                handlerService.executeCommand(DELETE_TRACE_COMMAND_ID, null);
            } else if (experimentsSelected) {
                handlerService.executeCommand(DELETE_EXPERIMENT_COMMAND_ID, null);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (NotDefinedException e) {
            e.printStackTrace();
        } catch (NotEnabledException e) {
            e.printStackTrace();
        } catch (NotHandledException e) {
            e.printStackTrace();
        }
    }

}
