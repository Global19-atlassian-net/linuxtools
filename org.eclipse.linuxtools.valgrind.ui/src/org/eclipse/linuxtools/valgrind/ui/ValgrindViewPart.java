/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.ViewPart;

public class ValgrindViewPart extends ViewPart {

	protected Composite dynamicViewHolder;
	protected IValgrindToolView dynamicView;
	protected HistoryDropDownAction historyAction;

	@Override
	public void createPartControl(Composite parent) {
		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		historyAction = new HistoryDropDownAction(Messages.getString("ValgrindViewPart.Select_a_recent_launch"), IAction.AS_DROP_DOWN_MENU); //$NON-NLS-1$
		toolbar.add(historyAction);
		toolbar.update(true);
		
		dynamicViewHolder = new Composite(parent, SWT.NONE);
		dynamicViewHolder.setLayout(new GridLayout());
		dynamicViewHolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		ValgrindUIPlugin.getDefault().setView(this);
	}

	public void createDynamicView(String toolID) throws CoreException {
		// remove tool specific toolbar controls
		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		for (IContributionItem item : toolbar.getItems()) {
			if (!(item instanceof ActionContributionItem) || ((ActionContributionItem) item).getAction() != historyAction) {
				toolbar.remove(item);
			}
		}
		toolbar.update(true);
		
		// remove old view controls
		for (Control child : dynamicViewHolder.getChildren()) {
			child.dispose();
		}
		dynamicView = ValgrindUIPlugin.getDefault().getToolView(toolID);
		dynamicView.createPartControl(dynamicViewHolder);
		
		dynamicViewHolder.layout(true);
	}

	@Override
	public void setFocus() {
		if (dynamicView != null) {
			dynamicView.setFocus();
		}
	}

	public void refreshView() {
		if (dynamicView != null) {
			dynamicView.refreshView();
		}
	}

	@Override
	public void dispose() {
		if (dynamicView != null) {
			dynamicView.dispose();
		}
		super.dispose();
	}

	public IValgrindToolView getDynamicView() {
		return dynamicView;
	}

}
