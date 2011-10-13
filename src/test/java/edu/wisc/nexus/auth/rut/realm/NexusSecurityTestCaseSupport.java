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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.io.RawInputStreamFacade;
import org.sonatype.nexus.test.PlexusTestCaseSupport;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.realms.tools.ConfigurationManager;

/**
 * Base class for setting up 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class NexusSecurityTestCaseSupport extends PlexusTestCaseSupport {
    private static final File appConfDir = new File("target/app-conf");

    /**
     * Subclasses should override if they need to copy files into the test application config directory
     * @see #copyFile(String)
     * @see #copyFile(String, String)
     */
    @SuppressWarnings("unused")
    protected void copyTestConfig() throws FileNotFoundException, IOException {
    }
    
    /**
     * @return The name of the security XML file, defaults to security.xml
     */
    protected String getSecurityContextName() {
        return "security.xml";
    }
    
    protected void customizeContextInternal(Context ctx) {
    }

    @Override
    protected final void customizeContext(Context ctx) {
        ctx.put( "nexus-work", appConfDir.getAbsolutePath() );
        ctx.put( "application-conf", appConfDir.getAbsolutePath() );
        ctx.put( "security-xml-file", appConfDir.getAbsolutePath() + "/" + getSecurityContextName() );
        
        customizeContextInternal(ctx);
    }
    
    @Override
    protected final void customizeContainerConfiguration(ContainerConfiguration configuration) {
        configuration.setAutoWiring( true );
        configuration.setClassPathScanning( PlexusConstants.SCANNING_CACHE );
    }

    @Override
    protected final void setUp() throws Exception {
        appConfDir.mkdirs();
        
        // copy the test config
        this.copyTestConfig();
    
        // restart security
        this.lookup( ConfigurationManager.class ).clearCache();
        this.lookup( SecuritySystem.class ).start();
    }
    
    protected final void copyFile(String filename) throws IOException {
        copyFile(null, filename);
    }

    protected final void copyFile(String dir, String filename) throws IOException {
        final File confDir;
        if (dir != null && dir.length() > 0) {
            if (!dir.endsWith("/")) {
                dir = dir + "/";
            }
    
            confDir = new File(appConfDir, dir);
            confDir.mkdirs();
        }
        else {
            dir = "";
            confDir = appConfDir;
        }
    
        final Thread currentThread = Thread.currentThread();
        final ClassLoader contextClassLoader = currentThread.getContextClassLoader();
        final String src = dir + filename;
        final InputStream fileStream = contextClassLoader.getResourceAsStream(src);
        final File dest = new File(confDir, filename);
        try {
            FileUtils.copyStreamToFile(new RawInputStreamFacade(fileStream), dest);
        }
        catch (Exception e) {
            throw new IOException("Failed to copy " + src + " to " + dest, e);
        }
        finally {
            IOUtils.closeQuietly(fileStream);
        }
    }
}