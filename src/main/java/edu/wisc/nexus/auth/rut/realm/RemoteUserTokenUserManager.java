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
import org.sonatype.security.usermanagement.AbstractReadOnlyUserManager;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserSearchCriteria;

import edu.wisc.nexus.auth.rut.dao.UserDao;

/**
 * @author Eric Dalquist
 * @version $Revision: 287 $
 */
@Component(role = UserManager.class, hint = RemoteUserTokenAuthenticatingRealm.REALM_NAME, description = "RemoteUser/Token User Manager")
public class RemoteUserTokenUserManager extends AbstractReadOnlyUserManager {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Requirement
    private UserDao userDao;

    /* (non-Javadoc)
     * @see org.sonatype.jsecurity.locators.users.PlexusUserLocator#getSource()
     */
    @Override
    public String getSource() {
        return RemoteUserTokenAuthenticatingRealm.REALM_NAME;
    }

    @Override
    public String getAuthenticationRealmName() {
        return RemoteUserTokenAuthenticatingRealm.REALM_NAME;
    }

    @Override
    public User getUser(String userId) throws UserNotFoundException {
        return this.userDao.getUser(userId);
    }

    @Override
    public Set<String> listUserIds() {
        final Set<String> userIds = this.userDao.listUserIds();
        logger.debug("listUserIds: {}", userIds);
        return userIds;
    }

    @Override
    public Set<User> listUsers() {
        final Set<User> users = this.userDao.listUsers();
        logger.debug("listUsers: {}", users);
        return users;
    }

    @Override
    public Set<User> searchUsers(UserSearchCriteria criteria) {
        final Set<User> users = this.listUsers();
        final Set<User> filteredUsers = this.filterListInMemeory(users, criteria);
        logger.debug("filteredUsers({}): {}", criteria, filteredUsers);
        return filteredUsers;
    }
}
