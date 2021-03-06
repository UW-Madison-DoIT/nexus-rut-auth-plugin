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
import java.util.Set;

import org.sonatype.security.authorization.Role;
import org.sonatype.security.usermanagement.RoleIdentifier;


/**
 * Interface that manages Remote User Plugin Configuration data.
 */
public interface RemoteUserTokenAuthPluginConfiguration {
    /**
     * File to load usernames and passwords from.
     */
    public File getUserFile();
    
    /**
     * (Optional) Domain to append to usernames to make them into
     * email addresses.
     */
    public String getEmailDomain();
    
    /**
     * The URL to redirect unauthenticated users that wish to use remote user authn
     */
    public String getRemoteUserLoginRedirectUrl();
    
    /**
     * The URL to redirect remote-user authenticated users that wish to logout
     */
    public String getRemoteUserLogoutRedirectUrl();
    
    /**
     * Default roleIds
     */
    public Set<String> getDefaultRolesIds();
    
    /**
     * Default role identifiers
     */
    public Set<RoleIdentifier> getDefaultRoleIdentifiers();
    
    /**
     * Default roles
     */
    public Set<Role> getDefaultRoles();
    
    /**
     * @return Frequency in seconds that the user file should be reloaded
     */
    public int getRefreshInterval();
}
