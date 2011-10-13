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

import org.apache.shiro.authc.AuthenticationToken;

/**
 * Auth token for remote-user based auth
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RemoteUserAuthenticationToken implements AuthenticationToken {
    private final String remoteUser;
    
    public RemoteUserAuthenticationToken(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    /* (non-Javadoc)
     * @see org.apache.shiro.authc.AuthenticationToken#getPrincipal()
     */
    @Override
    public Object getPrincipal() {
        return getRemoteUser();
    }

    /* (non-Javadoc)
     * @see org.apache.shiro.authc.AuthenticationToken#getCredentials()
     */
    @Override
    public Object getCredentials() {
        // RemoteUser auth never has credentials
        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((remoteUser == null) ? 0 : remoteUser.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RemoteUserAuthenticationToken other = (RemoteUserAuthenticationToken) obj;
        if (remoteUser == null) {
            if (other.remoteUser != null)
                return false;
        }
        else if (!remoteUser.equals(other.remoteUser))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RemoteUserAuthenticationToken [remoteUser=" + remoteUser + "]";
    }
}
