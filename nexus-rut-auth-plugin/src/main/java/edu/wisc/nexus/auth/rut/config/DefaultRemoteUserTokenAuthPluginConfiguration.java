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
package edu.wisc.nexus.auth.rut.config;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.usermanagement.RoleIdentifier;

import com.google.common.collect.ImmutableSet;

import edu.wisc.nexus.auth.rut.config.v1_0_0.Configuration;
import edu.wisc.nexus.auth.rut.config.v1_0_0.io.stax.NexusRemoteUserTokenAuthPluginConfigurationStaxReader;
import edu.wisc.nexus.auth.rut.config.v1_0_0.io.stax.NexusRemoteUserTokenAuthPluginConfigurationStaxWriter;
import edu.wisc.nexus.auth.rut.realm.RemoteUserTokenAuthenticatingRealm;

@Component(role = RemoteUserTokenAuthPluginConfiguration.class, hint = "default")
public class DefaultRemoteUserTokenAuthPluginConfiguration extends AbstractRefreshingFileLoader<Configuration> implements
    RemoteUserTokenAuthPluginConfiguration {

    @org.codehaus.plexus.component.annotations.Configuration(value = "${nexus-work}/conf/rut-auth-plugin.xml")
    private File configurationFile;

    //Thread safe vars
    private volatile File userFile;
    private volatile String emailDomain;
    private volatile String remoteUserLoginRedirectUrl;
    private volatile String remoteUserLogoutRedirectUrl;
    private volatile Set<String> defaultRoleIds;
    private volatile Set<RoleIdentifier> defaultRoleIdentifiers;
    private volatile Set<Role> defaultRoles;
    private volatile int refreshInterval;
    
    public DefaultRemoteUserTokenAuthPluginConfiguration() {
        super("RUTAuthPluginConfiguration");
    }
    
    /* (non-Javadoc)
     * @see edu.wisc.nexus.auth.token.config.TokenAuthPluginConfiguration#getUserFile()
     */
    @Override
    public File getUserFile() {
        return this.userFile;
    }

    /* (non-Javadoc)
     * @see edu.wisc.nexus.auth.rut.config.RemoteUserTokenAuthPluginConfiguration#getEmailDomain()
     */
    @Override
    public String getEmailDomain() {
        return this.emailDomain;
    }
    
    @Override
    public String getRemoteUserLoginRedirectUrl() {
        return this.remoteUserLoginRedirectUrl;
    }
    
    @Override
    public String getRemoteUserLogoutRedirectUrl() {
        return this.remoteUserLogoutRedirectUrl;
    }

    /* (non-Javadoc)
     * @see edu.wisc.nexus.auth.token.config.TokenAuthPluginConfiguration#getDefaultRoles()
     */
    @Override
    public Set<String> getDefaultRolesIds() {
        return this.defaultRoleIds;
    }
    
    /* (non-Javadoc)
     * @see edu.wisc.nexus.auth.token.config.TokenAuthPluginConfiguration#getDefaultRoleIdentifiers()
     */
    @Override
    public Set<RoleIdentifier> getDefaultRoleIdentifiers() {
        return this.defaultRoleIdentifiers;
    }
    
    /* (non-Javadoc)
     * @see edu.wisc.nexus.auth.rut.config.RemoteUserTokenAuthPluginConfiguration#getDefaultRoles()
     */
    @Override
    public Set<Role> getDefaultRoles() {
        return this.defaultRoles;
    }

    /* (non-Javadoc)
     * @see edu.wisc.nexus.auth.token.config.TokenAuthPluginConfiguration#getRefreshInterval()
     */
    @Override
    public int getRefreshInterval() {
        return this.refreshInterval;
    }

    /* (non-Javadoc)
    * @see edu.wisc.nexus.config.AbstractPluginConfiguration#getConfigurationFile()
    */
    @Override
    protected File getConfigurationFile() {
        return this.configurationFile;
    }

    /* (non-Javadoc)
    * @see edu.wisc.nexus.config.AbstractPluginConfiguration#getRefreshInterval(java.lang.Object)
    */
    @Override
    protected int getRefreshInterval(Configuration configuration) {
        return configuration.getRefreshInterval();
    }

    /* (non-Javadoc)
    * @see edu.wisc.nexus.config.AbstractPluginConfiguration#writeConfiguration(java.io.OutputStream, java.lang.Object)
    */
    @Override
    protected void writeConfiguration(Writer w, Configuration configuration) throws IOException {
        try {
            new NexusRemoteUserTokenAuthPluginConfigurationStaxWriter().write(w, configuration);
        }
        catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /* (non-Javadoc)
    * @see edu.wisc.nexus.config.AbstractPluginConfiguration#readConfiguration(java.io.InputStream)
    */
    @Override
    protected Configuration readConfiguration(Reader r) throws IOException {
        try {
            return new NexusRemoteUserTokenAuthPluginConfigurationStaxReader().read(r);
        }
        catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /* (non-Javadoc)
    * @see edu.wisc.nexus.config.AbstractPluginConfiguration#postLoad(java.lang.Object)
    */
    @Override
    protected void postLoad(Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException(configurationFile.getAbsolutePath() + " is a required configuration file");
        }
        
        //Password file, try absolute path first, if not default to relative
        final String userFile = configuration.getUserFile();
        File passwordFile = new File(userFile);
        if (passwordFile.exists()) {
            this.userFile = passwordFile;
        }
        else {
            this.userFile = new File(this.configurationFile.getParentFile(), userFile);
        }
        
        //Optional email domain
        this.emailDomain = configuration.getEmailDomain();

        //Remote user login url
        this.remoteUserLoginRedirectUrl = configuration.getRemoteUserLoginRedirectUrl();
        
        //Remote user logout url
        this.remoteUserLogoutRedirectUrl = configuration.getRemoteUserLogoutRedirectUrl();
        
        //Grab the new refresh interval
        this.refreshInterval = configuration.getRefreshInterval();

        //Create an immutable wrapper of the default roles
        this.defaultRoleIds = ImmutableSet.copyOf(configuration.getDefaultRoles());

        final Set<RoleIdentifier> defaultRoleIdentifiers = new LinkedHashSet<RoleIdentifier>();
        final Set<Role> defaultRoles = new LinkedHashSet<Role>();
        for (final String roleId : this.defaultRoleIds) {
            defaultRoleIdentifiers.add(createRoleIdentifier(roleId));
            defaultRoles.add(createRole(roleId));
        }
        this.defaultRoles = ImmutableSet.copyOf(defaultRoles);
        this.defaultRoleIdentifiers = ImmutableSet.copyOf(defaultRoleIdentifiers);
    }

    protected RoleIdentifier createRoleIdentifier(final String roleName) {
        return new RoleIdentifier(RemoteUserTokenAuthenticatingRealm.REALM_NAME, roleName);
    }
    
    protected Role createRole(final String roleId) {
        final Role role = new Role();
        role.setRoleId(roleId);
        role.setName(roleId);
        role.setReadOnly(true);
        role.setSource(RemoteUserTokenAuthenticatingRealm.REALM_NAME);
        return role;
    }
}
