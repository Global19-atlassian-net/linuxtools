/*******************************************************************************
 * Copyright (c) 2015, 2019 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.util.regex.Matcher;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.docker.core.DockerImage;

/**
 * Validates that the image name matches
 * [REGISTRY_HOST[:REGISTRY_PORT]/]IMAGE_NAME[:TAG]
 */
public class ImageNameValidator implements IValidator<String> {

	public static enum ImageNameStatus {
		// status when image name is valid and complete
		VALID,
		// status when the image name is empty
		EMPTY,
		// status when the image name does not match the expected pattern
		INVALID_FORMAT,
		// status when the image name does not contain a tag
		TAG_MISSING;
	}

	@Override
	public IStatus validate(final String imageName) {
		final ImageNameStatus imageNameStatus = getStatus(imageName);
		switch (imageNameStatus) {
		case EMPTY:
			return ValidationStatus
					.cancel(WizardMessages.getString("ImagePull.desc")); //$NON-NLS-1$
		case INVALID_FORMAT:
			return ValidationStatus.warning(WizardMessages
					.getString("ImagePull.name.invalidformat.msg")); //$NON-NLS-1$
		case TAG_MISSING:
			return ValidationStatus.warning(
					WizardMessages.getString("ImagePull.assumeLatest.msg")); //$NON-NLS-1$
		default:
			return Status.OK_STATUS;
		}
	}

	public static ImageNameStatus getStatus(final String imageName) {
		if (imageName == null || imageName.isEmpty()) {
			return ImageNameStatus.EMPTY;
		}
		final Matcher matcher = DockerImage.imageNamePattern.matcher(imageName);
		if (!matcher.matches()) {
			return ImageNameStatus.INVALID_FORMAT;
		} else if (matcher.group("tag") == null) { //$NON-NLS-1$
			return ImageNameStatus.TAG_MISSING;
		}
		return ImageNameStatus.VALID;
	}

}