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

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserSearchCriteria;

public class RemoteUserTokenUserManagerTest extends NexusSecurityTestCaseSupport {
    
    @Test
    public void testLocatorLookup() throws Exception {
        // a bit of plexus back ground, this is how you can look up a component from a test class
        this.lookup(UserManager.class, RemoteUserTokenAuthenticatingRealm.REALM_NAME);
    }

    @Test
    public void testSearch() throws Exception {
        UserManager userLocator = this.lookup(UserManager.class, RemoteUserTokenAuthenticatingRealm.REALM_NAME);

        Set<User> result = userLocator.searchUsers(new UserSearchCriteria("joh"));
        Assert.assertEquals(1, result.size());
        // your test could be a bit more robust
        Assert.assertEquals("johnson", result.iterator().next().getUserId());
    }

    @Test
    public void testIdList() throws Exception {
        UserManager userLocator = this.lookup(UserManager.class, RemoteUserTokenAuthenticatingRealm.REALM_NAME);

        Set<String> ids = userLocator.listUserIds();

        Assert.assertTrue(ids.contains("johnson"));
        Assert.assertTrue(ids.contains("feldstein"));
        Assert.assertTrue(ids.contains("rome"));
        Assert.assertTrue(ids.contains("james"));
        Assert.assertTrue(ids.contains("jdoe"));
        Assert.assertTrue(ids.contains("role_test2"));
        Assert.assertTrue(ids.contains("role_test3"));
        Assert.assertTrue(ids.contains("role_test1"));

        Assert.assertEquals(8, ids.size());
    }

    @Test
    public void testUserList() throws Exception {
        UserManager userLocator = this.lookup(UserManager.class, RemoteUserTokenAuthenticatingRealm.REALM_NAME);

        Set<User> users = userLocator.listUsers();
        // your test could be a bit more robust
        Assert.assertEquals(8, users.size());
    }
}
