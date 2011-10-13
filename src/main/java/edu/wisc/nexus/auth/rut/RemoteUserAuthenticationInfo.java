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

package edu.wisc.nexus.auth.rut;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.MergableAuthenticationInfo;
import org.apache.shiro.subject.MutablePrincipalCollection;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RemoteUserAuthenticationInfo implements MergableAuthenticationInfo {
    private PrincipalCollection principals;
    private Object credentials;
    
    public RemoteUserAuthenticationInfo(String remoteUser, String realmName) {
        this.principals = new SimplePrincipalCollection(remoteUser, realmName);
        this.credentials = remoteUser;
    }

    /* (non-Javadoc)
     * @see org.apache.shiro.authc.AuthenticationInfo#getPrincipals()
     */
    @Override
    public PrincipalCollection getPrincipals() {
        return this.principals;
    }

    /* (non-Javadoc)
     * @see org.apache.shiro.authc.AuthenticationInfo#getCredentials()
     */
    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    /* (non-Javadoc)
     * @see org.apache.shiro.authc.MergableAuthenticationInfo#merge(org.apache.shiro.authc.AuthenticationInfo)
     */
    @Override
    public void merge(AuthenticationInfo info) {
        if (info == null || info.getPrincipals() == null || info.getPrincipals().isEmpty()) {
            return;
        }

        if (this.principals == null) {
            this.principals = info.getPrincipals();
        } else {
            if (!(this.principals instanceof MutablePrincipalCollection)) {
                this.principals = new SimplePrincipalCollection(this.principals);
            }
            ((MutablePrincipalCollection) this.principals).addAll(info.getPrincipals());
        }

        Object thisCredentials = getCredentials();
        Object otherCredentials = info.getCredentials();

        if (otherCredentials == null) {
            return;
        }

        if (thisCredentials == null) {
            this.credentials = otherCredentials;
            return;
        }

        if (!(thisCredentials instanceof Collection)) {
            Set<Object> newSet = new HashSet<Object>();
            newSet.add(thisCredentials);
            this.credentials = newSet;
        }

        // At this point, the credentials should be a collection
        Collection<Object> credentialCollection = (Collection<Object>) getCredentials();
        if (otherCredentials instanceof Collection) {
            credentialCollection.addAll((Collection<?>) otherCredentials);
        }
        else {
            credentialCollection.add(otherCredentials);
        }
    }
}
