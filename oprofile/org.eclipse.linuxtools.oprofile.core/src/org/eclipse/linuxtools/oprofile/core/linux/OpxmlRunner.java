/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.core.linux;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.oprofile.core.OpxmlException;
import org.eclipse.linuxtools.oprofile.core.opxml.AbstractDataAdapter;
import org.eclipse.linuxtools.oprofile.core.opxml.EventIdCache;
import org.eclipse.linuxtools.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.oprofile.core.opxml.XMLProcessor;
import org.eclipse.linuxtools.oprofile.core.opxml.checkevent.CheckEventAdapter;
import org.eclipse.linuxtools.oprofile.core.opxml.info.InfoAdapter;
import org.eclipse.linuxtools.oprofile.core.opxml.modeldata.ModelDataAdapter;
import org.eclipse.linuxtools.oprofile.core.opxml.sessions.SessionManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * This class will run opxml.
 * 
 * opxml is a small program which acts as a textual interface between Oprofile and
 * BFD and the oprofile plugins. 
 */
public class OpxmlRunner {
	private OprofileSAXHandler _handler;

	/**
	 * Returns the current XMLProcessor handling parsing of opxml output.
	 * @return the processor
	 */
	public XMLProcessor getProcessor() {
		return _handler.getProcessor();
	}
	
	/**
	 * Runs opxml with the given arguments.
	 * @param args the arguments to pass to opxml
	 * @param callData any callData to pass to the processor
	 * @return boolean indicating the success/failure of opxml
	 * @throws OpxmlException 
	 */
	public boolean run(String[] args, Object callData) {
		XMLReader reader = null;
		_handler = OprofileSAXHandler.getInstance(callData);
		
		// Create XMLReader
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
			reader = factory.newSAXParser().getXMLReader();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		// Set content/error handlers
		reader.setContentHandler(_handler);
		reader.setErrorHandler(_handler);
		
		// Run opxml
		try {
			File file = constructFile(args);
			
			//handle the opxml_session file
			if (args[0].equals(SessionManager.SESSIONS)){
				SessionManager sessManNew = new SessionManager(SessionManager.SESSION_LOCATION);
				populateWithCurrentSession(sessManNew);
				sessManNew.write();
				FileReader fr = new FileReader(file);
				reader.parse(new InputSource(fr));
			// file has not been saved
			} else if (! file.exists()){
				AbstractDataAdapter aea;
				if (args[0].equals(CheckEventAdapter.CHECK_EVENTS)){
					aea = new CheckEventAdapter(args[1], args[2], args[3]);
					aea.process();
					BufferedReader bi = new BufferedReader(new InputStreamReader(aea.getInputStream()));
					reader.parse(new InputSource(bi));
				}else if (args[0].equals(InfoAdapter.INFO)){
					aea = new InfoAdapter();
					aea.process();
					BufferedReader bi = new BufferedReader(new InputStreamReader(aea.getInputStream()));
					reader.parse(new InputSource(bi));
				}else if (args[0].equals(ModelDataAdapter.MODEL_DATA)){
					// this should only happen initially when the current session
					// has not been generated
					if (! handleModelData(args)){
						return false;
					}
					FileReader fr = new FileReader(file);
					reader.parse(new InputSource(fr));
				}else{
					throw new RuntimeException("Unrecognized argument encountered");
				}
			}else{
				// always regenerate the 'current' session file
				if (args.length == 3
						&& args[0].equals(SessionManager.MODEL_DATA)
						&& args[2].equals(SessionManager.CURRENT)){
					if (! handleModelData(args)){
						return false;
					}
				}
				FileReader fr = new FileReader(file);
				reader.parse(new InputSource(fr));
			}
			
			return true;
		} catch (SAXException e) {
			e.printStackTrace();
			OprofileCorePlugin.showErrorDialog("opxmlSAXParseException", null); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
			OprofileCorePlugin.showErrorDialog("opxmlParse", null); //$NON-NLS-1$
		}
		return false;
	}

	private File saveOpxmlToFile(BufferedReader bi, String [] args) {
		String fileName = "";
		for (int i = 0; i < args.length; i++){
			fileName += args[i];
		}
		File file = new File(SessionManager.OPXML_PREFIX + fileName);
		String line;
		try {
			file.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			while ((line = bi.readLine()) != null){
				bw.write(line + "\n");
			}
			bi.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}
	
	private File constructFile(String [] args){
		String fileName = "";
		for (int i = 0; i < args.length; i++){
			fileName += args[i];
		}
		return new File (SessionManager.OPXML_PREFIX + fileName);
	}
	
	private boolean handleModelData (String [] args){
		Process p;
		try {
			p = Runtime.getRuntime().exec("opreport -X --details event:" + args[1]); //$NON-NLS-1$
			if (p.waitFor() != 0){
				return false;
			}
			ModelDataAdapter mda = new ModelDataAdapter(p.getInputStream());
			if (! mda.isParseable()){
				return false;
			}
			mda.process();
			BufferedReader bi = new BufferedReader(new InputStreamReader(mda.getInputStream()));
			saveOpxmlToFile(bi, args);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * Add the current session to the session manager for each event
	 * that it was profiled under.
	 * @param session the session manager to populate
	 */
	private void populateWithCurrentSession (SessionManager session){
		int MAX_COUNT = Integer.MAX_VALUE;
		session.removeAllCurrentSessions();
		for (int i = 0; i < MAX_COUNT; i++){
			if (isCounterEnabled(i)){
				String eventName = getEventName(i);
				session.addSession(SessionManager.CURRENT, eventName);
			}else{
				break;
			}
		}
	}

	/**
	 * @param i the counter number
	 * @return the name of the event used on counter i
	 */
	private String getEventName(int i) {
		String ret = null;
		File file = new File (InfoAdapter.DEV_OPROFILE + i + "/event");
		if (file.exists()){
			try {
				BufferedReader bi = new BufferedReader(new FileReader(file));
				String val = bi.readLine();
				int id = Integer.parseInt(val);
				ret =  EventIdCache.getInstance().getEventNameWithID(id);
			} catch (FileNotFoundException e) {
				// the file is checked for existence
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	/**
	 * @param i the counter number
	 * @return true if the counter is enabled, and false otherwise
	 */
	private boolean isCounterEnabled(int i) {
		File file = new File (InfoAdapter.DEV_OPROFILE + i + "/enabled");
		if (file.exists()){
			try {
				BufferedReader bi = new BufferedReader(new FileReader(file));
				String val = bi.readLine();
				int bit = Integer.parseInt(val);
				if (bit == 0){
					return false;
				}else if (bit == 1){
					return true;
				}else{
					throw new RuntimeException("An unexpected counter enabled value was detected");
				}
			} catch (FileNotFoundException e) {
				// the file is checked for existence
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
}
