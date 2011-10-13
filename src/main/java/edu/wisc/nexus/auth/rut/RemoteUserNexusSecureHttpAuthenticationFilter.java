/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */

package edu.wisc.nexus.auth.rut;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationToken;
import org.sonatype.nexus.security.filter.authc.NexusSecureHttpAuthenticationFilter;

/**
 * Extension to NexusSecureHttpAuthenticationFilter that returns a {@link RemoteUserAuthenticationToken} if
 * {@link HttpServletRequest#getRemoteUser()} is not null. 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RemoteUserNexusSecureHttpAuthenticationFilter extends NexusSecureHttpAuthenticationFilter {

    /* (non-Javadoc)
     * @see org.apache.shiro.web.filter.authc.AuthenticatingFilter#createToken(java.lang.String, java.lang.String, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    protected AuthenticationToken createToken(String username, String password, ServletRequest request, ServletResponse response) {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        
        final String remoteUser = httpServletRequest.getRemoteUser();
        if (remoteUser != null) {
            return new RemoteUserAuthenticationToken(remoteUser);
        }

        //Fall back to normal auth
        return super.createToken(username, password, request, response);
    }

}
