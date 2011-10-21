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
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserStatus;

import edu.wisc.nexus.auth.rut.config.AbstractRefreshingFileLoader;
import edu.wisc.nexus.auth.rut.config.RemoteUserTokenAuthPluginConfiguration;
import edu.wisc.nexus.auth.rut.realm.RemoteUserTokenAuthenticatingRealm;

/**
 * @author Eric Dalquist
 * @version $Revision: 287 $
 */
@Component(role = UserDao.class, hint = ApachePasswdUserDao.ROLE_NAME, description = "Apache Passwd User DAO")
public class ApachePasswdUserDao extends AbstractRefreshingFileLoader<Set<TokenAuthUser>> implements UserDao, Initializable, Disposable {
    public static final String ROLE_NAME = "TokenAuthUserDao";

    @Requirement
    private RemoteUserTokenAuthPluginConfiguration tokenAuthPluginConfiguration;
    
    //Map username -> passwordToken
    private volatile Map<String, TokenAuthUser> userMap;
    private volatile Set<User> userSet;

    public ApachePasswdUserDao() {
        super(ROLE_NAME);
    }

    ApachePasswdUserDao(RemoteUserTokenAuthPluginConfiguration tokenAuthPluginConfiguration) {
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
    protected int getRefreshInterval(Set<TokenAuthUser> configuration) {
        return this.tokenAuthPluginConfiguration.getRefreshInterval();
    }

    /* (non-Javadoc)
     * @see edu.wisc.nexus.config.AbstractPluginConfiguration#readConfiguration(java.io.InputStream)
     */
    @Override
    protected Set<TokenAuthUser> readConfiguration(Reader r) throws IOException {
        final Logger logger = this.getLogger();
        
        final BufferedReader fileReader = new BufferedReader(r);

        final Set<TokenAuthUser> users = new LinkedHashSet<TokenAuthUser>();

        int lineNum = 1;
        for (String htpasswdLine = fileReader.readLine(); null != htpasswdLine; htpasswdLine = fileReader.readLine()) {
            htpasswdLine = htpasswdLine.trim();
            if (htpasswdLine.length() == 0 || htpasswdLine.startsWith("#")) {
                continue;
            }
            
            final String[] htpasswdParts = htpasswdLine.split(":");

            if (htpasswdParts.length == 3) {
                final String userId = htpasswdParts[0];
                final String encryptedPassword = htpasswdParts[1];
                final String passwordToken = htpasswdParts[2];
                
                final TokenAuthUser tokenAuthUser = this.createTokenAuthUser(userId, encryptedPassword, passwordToken);
                users.add(tokenAuthUser);
            }
            else {
                logger.warn("Line from htpasswd file doesn't split into 3 parts, line " + lineNum + ": " + htpasswdLine + "");
            }

            lineNum++;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Loaded " + users.size() + " token users");
        }

        return users;
    }

    /* (non-Javadoc)
     * @see edu.wisc.nexus.config.AbstractPluginConfiguration#postLoad(java.lang.Object)
     */
    @Override
    protected void postLoad(Set<TokenAuthUser> configuration) {
        final Map<String, TokenAuthUser> userMapBuilder = new ConcurrentHashMap<String, TokenAuthUser>();
        final Set<User> userSetBuilder = Collections.newSetFromMap(new ConcurrentHashMap<User, Boolean>());
        for (final TokenAuthUser user : configuration) {
            userMapBuilder.put(user.getUserId(), user);
            userSetBuilder.add(user);
        }
        
        this.userMap = userMapBuilder;
        this.userSet = userSetBuilder;
    }
    
    /* (non-Javadoc)
     * @see edu.wisc.nexus.auth.rut.config.AbstractRefreshingFileLoader#writeConfiguration(java.io.Writer, java.lang.Object)
     */
    @Override
    protected void writeConfiguration(Writer w, Set<TokenAuthUser> configuration) throws IOException {
        final PrintWriter pw = new PrintWriter(w);
        try {
            for (final TokenAuthUser user : configuration) {
                pw.println(user.getUserId() + ":" + user.getEncryptedPassword() + ":" + user.getPasswordToken());
            }
        }
        finally {
            pw.flush();
        }
    }
    
    @Override
    protected boolean preSave(Set<TokenAuthUser> configuration) {
        boolean modified = false;

        //Add any NEW users to the configuration map
        for (final TokenAuthUser user : this.userMap.values()) {
            if (!configuration.contains(user)) {
                configuration.add(user);
                modified = true;
            }
        }
        
        return modified;
    }

    @Override
    public String getUserPasswordToken(String userId) {
        final TokenAuthUser user = this.userMap.get(userId);
        if (user == null) {
            return null;
        }
        
        return user.getPasswordToken();
    }

    /* (non-Javadoc)
     * @see edu.wisc.nexus.auth.token.dao.UserDao#listUserIds()
     */
    @Override
    public Set<String> listUserIds() {
        return Collections.unmodifiableSet(this.userMap.keySet());
    }

    /* (non-Javadoc)
     * @see edu.wisc.nexus.auth.token.dao.UserDao#listUsers()
     */
    @Override
    public Set<User> listUsers() {
        return Collections.unmodifiableSet(this.userSet);
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
    
    @Override
    public void createUser(String userId) {
        if (this.userExists(userId)) {
            this.getLogger().warn("Not creating user " + userId + " they already exist");
            return;
        }
        
        final String passwordToken = MD5Crypt.createRandomSalt(16);
        final String encryptedPassword = MD5Crypt.apacheCrypt(passwordToken);
        
        final TokenAuthUser user = this.createTokenAuthUser(userId, encryptedPassword, passwordToken);
        
        this.userMap.put(userId, user);
        this.userSet.add(user);
        
        this.getLogger().info("Created user: " + userId);
        
        //Call refresh to force an immediate save
        this.refreshConfiguration();
    }
    
    /* (non-Javadoc)
     * @see edu.wisc.nexus.auth.rut.dao.UserDao#userExists(java.lang.String)
     */
    @Override
    public boolean userExists(String userId) {
        return this.userMap.containsKey(userId);
    }

    /**
     * Create a user for the userId
     */
    protected TokenAuthUser createTokenAuthUser(String userId, String encryptedPassword, String passwordToken) {
        final TokenAuthUser user = new TokenAuthUser();
        user.setUserId(userId);
        user.setFirstName(userId);
        user.setStatus(UserStatus.active);
        user.setSource(RemoteUserTokenAuthenticatingRealm.REALM_NAME);
        user.setReadOnly(true);
        user.setEncryptedPassword(encryptedPassword);
        user.setPasswordToken(passwordToken);
        
        final String emailDomain = this.tokenAuthPluginConfiguration.getEmailDomain();
        if (emailDomain != null) {
            user.setEmailAddress(userId + "@" + emailDomain);
        }
        
        final Set<RoleIdentifier> defaultRoleIdentifiers = this.tokenAuthPluginConfiguration.getDefaultRoleIdentifiers();
        user.setRoles(new LinkedHashSet<RoleIdentifier>(defaultRoleIdentifiers));
        return user;
    }
}
