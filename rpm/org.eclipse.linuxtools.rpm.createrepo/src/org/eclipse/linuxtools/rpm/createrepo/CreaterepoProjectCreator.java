/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.createrepo;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

/**
 * Utility class to help create a createrepo project.
 */
public class CreaterepoProjectCreator {

	/**
	 * Create a createrepo project given a project name and the progress
	 * monitor. The new project will contain an empty repodata folder.
	 *
	 * @param projectName The name of the project.
	 * @param locationPath The location path of the project
	 * @param monitor The progress monitor.
	 * @return The newly created project.
	 * @throws CoreException Thrown when creating a project fails.
	 */
	public static IProject create(String projectName, IPath locationPath,
			IProgressMonitor monitor) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		IProjectDescription description = ResourcesPlugin.getWorkspace()
				.newProjectDescription(projectName);
		if (!Platform.getLocation().equals(locationPath)) {
			description.setLocation(locationPath);
		}
		project.create(description, monitor);
		project.open(monitor);
		new CreaterepoProject(project);
		return project;
	}

}