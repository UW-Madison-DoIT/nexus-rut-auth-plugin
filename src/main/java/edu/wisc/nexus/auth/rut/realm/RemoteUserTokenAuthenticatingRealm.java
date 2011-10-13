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

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import edu.wisc.nexus.auth.rut.RemoteUserAuthenticationInfo;
import edu.wisc.nexus.auth.rut.RemoteUserAuthenticationToken;
import edu.wisc.nexus.auth.rut.config.RemoteUserTokenAuthPluginConfiguration;
import edu.wisc.nexus.auth.rut.dao.UserDao;

/**
 * Allows use of SVN tokens for authenticating with Nexus
 * 
 * @author Eric Dalquist
 * @version $Revision: 287 $
 */
@Component( role = Realm.class, hint = RemoteUserTokenAuthenticatingRealm.REALM_NAME, description = "RemoteUser/Token Authenticating Realm" )
public class RemoteUserTokenAuthenticatingRealm extends AuthorizingRealm {
    public static final String REALM_NAME = "RUT";
    
    protected final Log logger = LogFactory.getLog(this.getClass());
        
    @Requirement
    private UserDao userDao;
    
    @Requirement
    private RemoteUserTokenAuthPluginConfiguration tokenAuthPluginConfiguration;
    
    
    public RemoteUserTokenAuthenticatingRealm() {
        this.setCredentialsMatcher(new RemoteUserTokenAuthCredentialsMatcher());
        this.setCachingEnabled(false);
    }
    
    
    @Override
    public boolean supports(AuthenticationToken token) {
        return token != null && (
                UsernamePasswordToken.class.isAssignableFrom(token.getClass()) ||
                RemoteUserAuthenticationToken.class.isAssignableFrom(token.getClass()));
    }
    
    /* (non-Javadoc)
     * @see org.jsecurity.realm.AuthenticatingRealm#doGetAuthenticationInfo(org.jsecurity.authc.AuthenticationToken)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("doGetAuthenticationInfo(" + token + "), principal=" + token.getPrincipal() + ", credentials=" + token.getCredentials());
        }

        
        // Handle REMOTE_USER auth first
        if (RemoteUserAuthenticationToken.class.isAssignableFrom(token.getClass())) {
            final String remoteUser = ((RemoteUserAuthenticationToken) token).getRemoteUser();
            if (remoteUser == null) {
                throw new AuthenticationException(RemoteUserAuthenticationToken.class.getSimpleName() + " provided but no remoteUser value is set");
            }
            
            return new RemoteUserAuthenticationInfo(remoteUser, getName());
        }
            
        // Handle token auth second
        if (UsernamePasswordToken.class.isAssignableFrom(token.getClass())) {
            final String userId = ((UsernamePasswordToken) token).getUsername();
            final String password = this.userDao.getUserPassword(userId);
            if (password == null) {
                return null;
            }
    
            return new SimpleAuthenticationInfo(userId, password, getName());
        }
        
        // Neither REMOTE_USER or token auth
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Token '" + token.getClass() + "' is not assignable to: " + UsernamePasswordToken.class + " or " + RemoteUserAuthenticationToken.class);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.jsecurity.realm.AuthorizingRealm#doGetAuthorizationInfo(org.jsecurity.subject.PrincipalCollection)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("doGetAuthorizationInfo(" + principals + "), user=" + principals.iterator().next());
        }
        
        final String userId = (String) principals.iterator().next();
        
        final String password = this.userDao.getUserPassword(userId);
        if (password == null) {
            return null;
        }
        
        final Set<String> defaultRoles = tokenAuthPluginConfiguration.getDefaultRolesIds();
        return new SimpleAuthorizationInfo(new LinkedHashSet<String>(defaultRoles));
    }
}
