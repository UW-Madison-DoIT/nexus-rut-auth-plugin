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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserStatus;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import edu.wisc.nexus.auth.rut.config.AbstractRefreshingFileLoader;
import edu.wisc.nexus.auth.rut.config.RemoteUserTokenAuthPluginConfiguration;
import edu.wisc.nexus.auth.rut.realm.RemoteUserTokenAuthenticatingRealm;

/**
 * @author Eric Dalquist
 * @version $Revision: 287 $
 */
@Component(role = UserDao.class, hint = TokenAuthUserDao.ROLE_NAME, description = "Token User/Password DAO")
public class TokenAuthUserDao extends AbstractRefreshingFileLoader<Map<String, String>> implements UserDao, Initializable, Disposable {
    public static final String ROLE_NAME = "TokenAuthUserDao";

    @Requirement
    private RemoteUserTokenAuthPluginConfiguration tokenAuthPluginConfiguration;
    
    //Map username -> passwordToken
    private volatile Map<String, String> userPasswordMap;
    private volatile Map<String, User> userMap;
    private volatile Set<User> userSet;

    public TokenAuthUserDao() {
        super(ROLE_NAME);
    }

    TokenAuthUserDao(RemoteUserTokenAuthPluginConfiguration tokenAuthPluginConfiguration) {
        super(ROLE_NAME);
        this.tokenAuthPluginConfiguration = tokenAuthPluginConfiguration;
    }

    /* (non-Javadoc)
     * @see edu.wisc.nexus.config.AbstractPluginConfiguration#getConfigurationFile()
     */
    @Override
    protected File getConfigurationFile() {
        return this.tokenAuthPluginConfiguration.getUserFile();
    }

    /* (non-Javadoc)
     * @see edu.wisc.nexus.config.AbstractPluginConfiguration#getRefreshInterval(java.lang.Object)
     */
    @Override
    protected int getRefreshInterval(Map<String, String> configuration) {
        return this.tokenAuthPluginConfiguration.getRefreshInterval();
    }

    /* (non-Javadoc)
     * @see edu.wisc.nexus.config.AbstractPluginConfiguration#readConfiguration(java.io.InputStream)
     */
    @Override
    protected Map<String, String> readConfiguration(Reader r) throws IOException {
        final Logger logger = this.getLogger();
        
        final Pattern userRegex = this.tokenAuthPluginConfiguration.getUserRegex();
        final int userRegexGroup = this.tokenAuthPluginConfiguration.getUserRegexGroup();
        final int passwordRegexGroup = this.tokenAuthPluginConfiguration.getPasswordRegexGroup();
        
        final BufferedReader fileReader = new BufferedReader(r);

        final Builder<String, String> tokenUsersBuilder = ImmutableMap.builder();

        int lineNum = 1;
        for (String htpasswdLine = fileReader.readLine(); null != htpasswdLine; htpasswdLine = fileReader.readLine()) {
            htpasswdLine = htpasswdLine.trim();
            //Ignore commented lines TODO move this into a configured comment regex
            if (htpasswdLine.length() == 0 || htpasswdLine.startsWith("#")) {
                continue;
            }
            
            final Matcher matcher = userRegex.matcher(htpasswdLine);

            if (matcher.matches()) {
                final String userName = matcher.group(userRegexGroup);
                final String password = matcher.group(passwordRegexGroup);
                tokenUsersBuilder.put(userName, password);
            }
            else {
                logger.warn("Line from token auth file doesn't match pattern: '" + userRegex.pattern() 
                        + "', line " + lineNum + ": " + htpasswdLine + "");
            }

            lineNum++;
        }

        final ImmutableMap<String, String> tokenUsers = tokenUsersBuilder.build();
        
        if (logger.isInfoEnabled()) {
            logger.info("Loaded " + tokenUsers.size() + " token users");
        }

        return tokenUsers;
    }

    /* (non-Javadoc)
     * @see edu.wisc.nexus.config.AbstractPluginConfiguration#postLoad(java.lang.Object)
     */
    @Override
    protected void postLoad(Map<String, String> configuration) {
        this.userPasswordMap = ImmutableMap.copyOf(configuration);
        
        final Builder<String, User> userMapBuilder = ImmutableMap.builder();
        for (final String userId : this.userPasswordMap.keySet()) {
            final User user = this.createUser(userId);
            userMapBuilder.put(userId, user);
        }
        this.userMap = userMapBuilder.build();
        this.userSet = ImmutableSet.copyOf(this.userMap.values());
    }

    @Override
    public String getUserPassword(String userId) {
        return this.userPasswordMap.get(userId);
    }

    /* (non-Javadoc)
     * @see edu.wisc.nexus.auth.token.dao.UserDao#listUserIds()
     */
    @Override
    public Set<String> listUserIds() {
        return this.userPasswordMap.keySet();
    }

    /* (non-Javadoc)
     * @see edu.wisc.nexus.auth.token.dao.UserDao#listUsers()
     */
    @Override
    public Set<User> listUsers() {
        return this.userSet;
    }

    /* (non-Javadoc)
     * @see edu.wisc.nexus.auth.token.dao.UserDao#getUser(java.lang.String)
     */
    @Override
    public User getUser(String userId) throws UserNotFoundException {
        final User user = this.userMap.get(userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        
        return user;
    }

    /**
     * Create a user for the userId
     */
    protected User createUser(String userId) {
        final DefaultUser user = new DefaultUser();
        user.setUserId(userId);
        user.setFirstName(userId);
        user.setStatus(UserStatus.active);
        user.setSource(RemoteUserTokenAuthenticatingRealm.REALM_NAME);
        user.setReadOnly(true);
        
        final String emailDomain = this.tokenAuthPluginConfiguration.getEmailDomain();
        if (emailDomain != null) {
            user.setEmailAddress(userId + "@" + emailDomain);
        }
        
        final Set<RoleIdentifier> defaultRoleIdentifiers = this.tokenAuthPluginConfiguration.getDefaultRoleIdentifiers();
        user.setRoles(new LinkedHashSet<RoleIdentifier>(defaultRoleIdentifiers));
        return user;
    }
}
