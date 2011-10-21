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

import org.codehaus.plexus.logging.slf4j.Slf4jLogger;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import edu.wisc.nexus.auth.rut.config.RemoteUserTokenAuthPluginConfiguration;
import edu.wisc.nexus.auth.rut.realm.NexusSecurityTestCaseSupport;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ApachePasswdUserDaoTest {
    private final File tempTestDir = new File("target/ApachePasswdUserDaoTest");
    
    @Test
    public void testCreateUser() throws Exception {
        NexusSecurityTestCaseSupport.copyFile(tempTestDir, "conf", "apache-passwd");
        
        final RemoteUserTokenAuthPluginConfiguration tokenAuthPluginConfiguration = mock(RemoteUserTokenAuthPluginConfiguration.class);
        
        when(tokenAuthPluginConfiguration.getUserFile()).thenReturn(new File(tempTestDir, "conf/apache-passwd"));
        when(tokenAuthPluginConfiguration.getRefreshInterval()).thenReturn(60);
        
        ApachePasswdUserDao tokenAuthUserDao = new ApachePasswdUserDao(tokenAuthPluginConfiguration);
        tokenAuthUserDao.enableLogging(new Slf4jLogger(Slf4jLogger.LEVEL_DEBUG, LoggerFactory.getLogger(getClass())));
        tokenAuthUserDao.initialize();
        
        assertFalse(tokenAuthUserDao.userExists("dalquista"));
        tokenAuthUserDao.createUser("dalquista");
        assertTrue(tokenAuthUserDao.userExists("dalquista"));
    }
}
