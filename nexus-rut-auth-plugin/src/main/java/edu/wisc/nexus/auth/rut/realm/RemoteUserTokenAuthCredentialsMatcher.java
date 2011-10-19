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

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;

import edu.wisc.nexus.auth.rut.RemoteUserAuthenticationInfo;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
class RemoteUserTokenAuthCredentialsMatcher implements CredentialsMatcher {
    private final CredentialsMatcher tokenCredentialsMatcher = new SimpleCredentialsMatcher();

    /* (non-Javadoc)
     * @see org.apache.shiro.authc.credential.CredentialsMatcher#doCredentialsMatch(org.apache.shiro.authc.AuthenticationToken, org.apache.shiro.authc.AuthenticationInfo)
     */
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        //Remote user auth is always true
        if (info instanceof RemoteUserAuthenticationInfo) {
            return true;
        }
        
        return this.tokenCredentialsMatcher.doCredentialsMatch(token, info);
    }

}
