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

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.junit.Test;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authentication.AuthenticationException;

import edu.wisc.nexus.auth.rut.RemoteUserAuthenticationToken;

public class RemoteUserTokenAuthenticatingRealmTest extends NexusSecurityTestCaseSupport {
    /* (non-Javadoc)
     * @see edu.wisc.nexus.auth.token.realm.NexusSecurityTestCaseSupport#copyTestConfig()
     */
    @Override
    protected void copyTestConfig() throws FileNotFoundException, IOException {
        copyFile("nexus.xml");
        copyFile("security.xml");
        copyFile("security-configuration.xml");
        copyFile("conf", "rut-auth-plugin.xml");
        copyFile("conf", "apache-passwd");
    }

    // Realm Tests
    /**
     * Test authentication with a valid user and password.
     *
     * @throws Exception
     */
    @Test
    public void testValidTokenAuthentication() throws Exception {
        SecuritySystem plexusSecurity = this.lookup(SecuritySystem.class);
        AuthenticationToken token = new UsernamePasswordToken("johnson", "RJDgUmkY6YtCGf5a");
        AuthenticationInfo authInfo = plexusSecurity.authenticate(token);

        Assert.assertNotNull(authInfo);
    }
    
    /**
     * Test authentication with a valid user and password.
     *
     * @throws Exception
     */
    @Test
    public void testValidRemoteUserAuthentication() throws Exception {
        SecuritySystem plexusSecurity = this.lookup(SecuritySystem.class);
        AuthenticationToken token = new RemoteUserAuthenticationToken("johnson");
        AuthenticationInfo authInfo = plexusSecurity.authenticate(token);

        Assert.assertNotNull(authInfo);
    }

    /**
     * Test authorization using the NexusMethodAuthorizingRealm. <BR/> Take a look a the security.xml in
     * src/test/resources this maps the users in the UserStore to nexus roles/privileges
     *
     * @throws Exception
     */
    @Test
    public void testValidUserRole() throws Exception {
        SecuritySystem plexusSecurity = this.lookup(SecuritySystem.class);
        
        //Auth first to cache username
        AuthenticationToken token = new RemoteUserAuthenticationToken("johnson");
        plexusSecurity.authenticate(token);

        PrincipalCollection principal = new SimplePrincipalCollection("johnson", SecuritySystem.class.getSimpleName());

        Assert.assertTrue(plexusSecurity.hasRole(principal, "uw-sso-user"));
    }

    /**
     * Test authentication with a valid user and invalid password.
     *
     * @throws Exception
     */
    @Test(expected=AuthenticationException.class)
    public void testInvalidPasswordAuthentication() throws Exception {
        SecuritySystem plexusSecurity = this.lookup(SecuritySystem.class);
        AuthenticationToken token = new UsernamePasswordToken("dalquist", "INVALID");

        plexusSecurity.authenticate(token);
    }

    /**
     * Test authentication with a invalid user and password.
     *
     * @throws Exception
     */
    @Test(expected=AuthenticationException.class)
    public void testInvalidUserAuthentication() throws Exception {
        SecuritySystem plexusSecurity = this.lookup(SecuritySystem.class);
        AuthenticationToken token = new UsernamePasswordToken("INVALID", "INVALID");

        plexusSecurity.authenticate(token);
    }

    /**
     * Test authorization using the NexusMethodAuthorizingRealm. <BR/> Take a look a the security.xml in
     * src/test/resources this maps the users in the UserStore to nexus roles/privileges
     *
     * @throws Exception
     */
    @Test
    public void testPrivileges() throws Exception {
        SecuritySystem plexusSecurity = this.lookup(SecuritySystem.class);

        PrincipalCollection principal = new SimplePrincipalCollection("dalquist", SecuritySystem.class
                .getSimpleName());

        // test one of the privleges that the admin user has
        Assert.assertFalse(plexusSecurity.isPermitted(principal, "nexus:repositories:create"));// Repositories -
        // (create,read)

    }

    /**
     * Tests a valid privilege for an invalid user
     * @throws Exception
     */
    @Test
    public void testPrivilegesInvalidUser() throws Exception {
        SecuritySystem plexusSecurity = this.lookup(SecuritySystem.class);

        PrincipalCollection principal = new SimplePrincipalCollection("INVALID", SecuritySystem.class.getSimpleName());

        // test one of the privleges
        Assert.assertFalse(plexusSecurity.isPermitted(principal, "nexus:repositories:create"));// Repositories -
        // (create,read)
    }
}
