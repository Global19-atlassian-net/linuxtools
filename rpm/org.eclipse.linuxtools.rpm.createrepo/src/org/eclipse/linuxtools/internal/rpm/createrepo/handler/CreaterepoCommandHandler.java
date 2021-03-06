/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoUtils;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * Handle the execution of the Update and Execute button.
 */
public class CreaterepoCommandHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) {
        final String executionType = event.getParameter("executionType"); //$NON-NLS-1$
        try {
            IWorkbench wb = PlatformUI.getWorkbench();
            IWorkbenchPage wbPage = wb.getActiveWorkbenchWindow().getActivePage();
            IEditorInput editorInput = wbPage.getActiveEditor().getEditorInput();
            IResource resource = ResourceUtil.getResource(editorInput);
            final CreaterepoProject project = new CreaterepoProject(resource.getProject());
            Job executeCreaterepo = new Job(Messages.Createrepo_jobName) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        monitor.beginTask(Messages.CreaterepoProject_executeCreaterepo, IProgressMonitor.UNKNOWN);
                        MessageConsoleStream os = CreaterepoUtils.findConsole(Messages.CreaterepoProject_consoleName)
                                .newMessageStream();
                        if (executionType.equals("refresh")) { //$NON-NLS-1$
                            return project.update(os);
                        } else {
                            return project.createrepo(os);
                        }
                    } catch (CoreException e) {
                        Activator.logError(Messages.Createrepo_errorExecuting, e);
                    } finally {
                        monitor.done();
                    }
                    return null;
                }
            };
            executeCreaterepo.setUser(true);
            executeCreaterepo.schedule();
        } catch (CoreException e) {
            Activator.logError(Messages.CreaterepoProject_executeCreaterepo, e);
        }
        return null;
    }

}
