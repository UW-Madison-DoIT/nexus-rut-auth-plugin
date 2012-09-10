/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package edu.wisc.nexus.auth.rut.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.logging.Slf4jPlexusLogger;

import edu.wisc.nexus.auth.rut.config.RemoteUserTokenAuthPluginConfiguration;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ApachePasswdUserDaoTest {
    @Test
    public void testCreateUser() throws Exception {
        final File passwdFile = new File(getBasedir(), "target/apache-passwd-home-" + new Random(System.currentTimeMillis()).nextLong() + "/apache-passwd");
        passwdFile.getParentFile().mkdirs();
        IOUtils.copy(this.getClass().getResourceAsStream("/conf/apache-passwd"), new FileOutputStream(passwdFile));
        
        final RemoteUserTokenAuthPluginConfiguration tokenAuthPluginConfiguration = mock(RemoteUserTokenAuthPluginConfiguration.class);
        
        when(tokenAuthPluginConfiguration.getUserFile()).thenReturn(passwdFile);
        when(tokenAuthPluginConfiguration.getRefreshInterval()).thenReturn(60);
        
        ApachePasswdUserDao tokenAuthUserDao = new ApachePasswdUserDao(tokenAuthPluginConfiguration);
        tokenAuthUserDao.enableLogging(new Slf4jPlexusLogger(LoggerFactory.getLogger(getClass())));
        tokenAuthUserDao.initialize();
        
        assertFalse(tokenAuthUserDao.userExists("dalquista"));
        tokenAuthUserDao.createUser("dalquista");
        assertTrue(tokenAuthUserDao.userExists("dalquista"));
    }
    
    public static final String BASE_DIR_KEY = "basedir";
    private static String basedir;

    private String getBasedir() {
        if (basedir != null) {
            return basedir;
        }

        basedir = System.getProperty(BASE_DIR_KEY);

        if (basedir == null) {
            // Find the directory which this class is defined in.
            final String path = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();

            // We expect the file to be in target/test-classes, so go up 2 dirs
            final File baseDir = new File(path).getParentFile().getParentFile();

            // Set ${basedir}
            System.setProperty(BASE_DIR_KEY, baseDir.getPath());
        }

        return basedir;
    }
}
