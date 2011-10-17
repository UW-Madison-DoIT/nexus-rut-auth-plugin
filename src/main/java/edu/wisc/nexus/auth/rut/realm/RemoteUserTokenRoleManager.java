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

import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.security.authorization.AbstractReadOnlyAuthorizationManager;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;

import edu.wisc.nexus.auth.rut.config.RemoteUserTokenAuthPluginConfiguration;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Component( role = AuthorizationManager.class, hint = RemoteUserTokenAuthenticatingRealm.REALM_NAME, description = "RemoteUser/Token Authorization Manager" )
public class RemoteUserTokenRoleManager extends AbstractReadOnlyAuthorizationManager {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Requirement
    private RemoteUserTokenAuthPluginConfiguration tokenAuthPluginConfiguration;

    /* (non-Javadoc)
     * @see org.sonatype.security.authorization.AuthorizationManager#getSource()
     */
    @Override
    public String getSource() {
        return RemoteUserTokenAuthenticatingRealm.REALM_NAME;
    }

    /* (non-Javadoc)
     * @see org.sonatype.security.authorization.AuthorizationManager#listRoles()
     */
    @Override
    public Set<Role> listRoles() {
        final Set<Role> roles = tokenAuthPluginConfiguration.getDefaultRoles();
        logger.debug("listRoles(): {}", roles);
        return roles;
    }

    /* (non-Javadoc)
     * @see org.sonatype.security.authorization.AuthorizationManager#getRole(java.lang.String)
     */
    @Override
    public Role getRole(String roleId) throws NoSuchRoleException {
        final Set<String> defaultRoles = tokenAuthPluginConfiguration.getDefaultRolesIds();
        
        if (defaultRoles.contains(roleId)) {
            for (final Role role : this.listRoles()) {
                if (role.getRoleId().equals(roleId)) {
                    logger.debug("getRole({}): {}", roleId, role);
                    return role;
                }
            }
        }

        logger.debug("getRole({}): {}", roleId, null);
        return null;
    }

    /* (non-Javadoc)
     * @see org.sonatype.security.authorization.AuthorizationManager#listPrivileges()
     */
    @Override
    public Set<Privilege> listPrivileges() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.sonatype.security.authorization.AuthorizationManager#getPrivilege(java.lang.String)
     */
    @Override
    public Privilege getPrivilege(String privilegeId) throws NoSuchPrivilegeException {
        return null;
    }
}
