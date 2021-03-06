/*******************************************************************************
 * Copyright (c) 2009, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.systemtap.structures.LoggingStreamDaemon;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoggingStreamDaemonTest {

    private List<File> tmpFiles = new ArrayList<>();
    private LoggingStreamDaemon daemon;

    @Before
    public void setUp() {
        tmpFiles.clear();
        daemon = new LoggingStreamDaemon();
    }

    @Test
    public void testGetOutput() {
        assertTrue(daemon.getOutput().isEmpty());
        daemon.handleDataEvent("test");
        assertEquals("test", daemon.getOutput());
    }

    @Test
    public void testSaveLog() {
        File f = makeTmpFile("/tmp/loggingstreamdaemon.test");
        daemon.handleDataEvent("test");
        assertTrue(daemon.saveLog(f));
        assertTrue(f.exists());
        f.delete();

        daemon.handleDataEvent("test");
        assertTrue(daemon.saveLog(f));
        assertTrue(f.exists());
        assertTrue(daemon.saveLog(f));
        f.delete();

        assertTrue(daemon.saveLog(f));
        assertTrue(f.exists());
        f.delete();

		f = new File("/tmp/loggingstreamdaemon.test.dir/");
		try {
			f.mkdir();
			assertFalse(daemon.saveLog(f));
		} finally {
			f.delete();
		}
		assertFalse(f.exists());
		assertFalse(daemon.saveLog(f));
    }

    @Test
    public void testPersistence() {
        File f = makeTmpFile("/tmp/loggingstreamdaemon.test");
        assertTrue(daemon.saveLog(f));
        daemon.dispose();
        assertTrue(f.exists());
    }

    @Test
    public void testSwapSaveLog() {
        daemon.handleDataEvent("test");
        assertTrue(daemon.saveLog(makeTmpFile("/tmp/loggingstreamdaemon1.test")));
        assertTrue(daemon.saveLog(makeTmpFile("/tmp/loggingstreamdaemon2.test")));
        assertEquals("test", daemon.getOutput());
    }

    @Test
    public void testSaveConflict() {
        assertTrue(daemon.saveLog(makeTmpFile("/tmp/loggingstreamdaemonComm.test")));

        LoggingStreamDaemon daemonDiff = new LoggingStreamDaemon();
        boolean savedDiff = daemonDiff.saveLog(makeTmpFile("/tmp/loggingstreamdaemonDiff.test"));

        LoggingStreamDaemon daemonComm = new LoggingStreamDaemon();
        boolean savedComm = daemonComm.saveLog(makeTmpFile("/tmp/loggingstreamdaemonComm.test"));

        daemonDiff.dispose();
        daemonComm.dispose();

        assertTrue(savedDiff);
        assertFalse(savedComm);
    }

    @Test
    public void testDispose() {
        File f = makeTmpFile("/tmp/shouldNotExist.test");
        daemon.dispose();
        daemon.handleDataEvent("test");
        assertFalse(daemon.saveLog(f));
        assertFalse(f.exists());
        assertNull(daemon.getOutput());
    }

    @After
    public void tearDown() {
        daemon.dispose();
        assertNull(daemon.getOutput());
        for (File tmpFile : tmpFiles) {
            tmpFile.delete();
        }
    }

    private File makeTmpFile(String name) {
        File tmpFile = new File(name);
        tmpFiles.add(tmpFile);
        return tmpFile;
    }
}
