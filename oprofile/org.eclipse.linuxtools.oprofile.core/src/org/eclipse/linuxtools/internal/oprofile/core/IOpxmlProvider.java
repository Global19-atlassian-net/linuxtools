/*******************************************************************************
 * Copyright (c) 2004, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> -
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core;

import java.util.ArrayList;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpInfo;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSession;

/**
 * Interface for the core to utilize opxml. Platform plugins should
 * define/register an OpxmlProvider for the core to use.
 */
public interface IOpxmlProvider {

	/**
	 * Returns an <code>IRunnableWithProgress</code> that fetches generic
	 * information from opxml
	 * 
	 * @param info <code>OpInfo</code> object for results
	 * @return <code>IRunnableWithProgress</code> that may be run by the caller
	 */
	IRunnableWithProgress info(OpInfo info);

	/**
	 * Returns an <code>IRunnableWithProgress</code> that fetches samples for the
	 * given <code>OpModelSession</code>
	 * 
	 * @param eventName   the event for which to fetch samples
	 * @param sessionName the session for which to fetch samples
	 * @param image       the image being profiled to be returned to the caller
	 * @return <code>IRunnableWithProgress</code> that may be run by the caller
	 */
	IRunnableWithProgress modelData(String eventName, String sessionName, OpModelImage image);

	/**
	 * Returns an <code>IRunnableWithProgress</code> that checks the validity of the
	 * given event, unit mask, and counter combination
	 * 
	 * @param ctr        the counter
	 * @param event      the String event name
	 * @param um         the integer unit mask
	 * @param eventValid a size one array to hold the return result (see
	 *                   <code>CheckEventsProcessor</code>)
	 * @return <code>IRunnableWithProgress</code> that may be run by the caller
	 */
	IRunnableWithProgress checkEvents(int ctr, String event, int um, int[] eventValid);

	/**
	 * Returns an <code>IRunnableWithProgress</code> that fetches the list of
	 * sessions
	 * 
	 * @param info        the <code>OpInfo</code> for oprofile
	 * @param sessionList an <code>ArrayList</code> in which to return the list of
	 *                    sessions
	 * @return <code>IRunnableWithProgress</code> that may be run by the caller
	 */
	IRunnableWithProgress sessions(ArrayList<OpModelSession> sessionList);
}
