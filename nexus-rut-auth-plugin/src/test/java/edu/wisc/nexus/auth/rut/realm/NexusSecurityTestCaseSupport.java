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

package edu.wisc.nexus.auth.rut.realm;

import java.io.File;
import java.io.IOException;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.sonatype.nexus.test.NexusTestSupport;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.guice.SecurityModule;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.sisu.ehcache.CacheManagerComponent;

import com.google.inject.Module;

/**
 * Base class for setting up 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class NexusSecurityTestCaseSupport extends NexusTestSupport {

    @Override
    protected Module[] getTestCustomModules() {
        return new Module[] { new SecurityModule() };
    }

    @Override
    protected void copyDefaultSecurityConfigToPlace() throws IOException {
        copyResource("/security-configuration.xml", getSecurityConfiguration());
        copyResource("/security.xml", getNexusSecurityConfiguration());
    }
    
    protected String getRutAuthPluginConfiguration() {
        return new File(getConfHomeDir(), "rut-auth-plugin.xml").getAbsolutePath();
    }
    
    protected String getApachePasswdConfiguration() {
        return new File(getConfHomeDir(), "apache-passwd").getAbsolutePath();
    }

    protected void copyDefaultRutConfigToPlace() throws IOException {
        copyResource("/conf/rut-auth-plugin.xml", getRutAuthPluginConfiguration());
        copyResource("/conf/apache-passwd", getApachePasswdConfiguration());
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // configure the logging
        SLF4JBridgeHandler.install();

        this.copyDefaultSecurityConfigToPlace();
        this.copyDefaultRutConfigToPlace();
        
        // restart security system
        this.lookup( ConfigurationManager.class ).clearCache();
        this.lookup( SecuritySystem.class ).start();
    }

    @Override
    protected void tearDown() throws Exception {
        lookup(CacheManagerComponent.class).shutdown();

        // configure the logging
        SLF4JBridgeHandler.uninstall();

        super.tearDown();
    }
}