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

import java.io.Serializable;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.rest.authentication.AbstractUIPermissionCalculatingPlexusResource;
import org.sonatype.security.rest.model.AuthenticationClientPermissions;

import edu.wisc.nexus.auth.rut.dao.UserDao;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Component( role = PlexusResource.class, hint = "UserTokenLookupPlexusResource" )
@Path( UserTokenLookupPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class UserTokenLookupPlexusResource extends AbstractUIPermissionCalculatingPlexusResource {
    public static final String RESOURCE_URI = "/rut/user_token"; 
    
    @Requirement
    private UserDao userDao;

    /* (non-Javadoc)
     * @see org.sonatype.plexus.rest.resource.AbstractPlexusResource#getResourceUri()
     */
    @Override
    public String getResourceUri() {
        return RESOURCE_URI;
    }

    /* (non-Javadoc)
     * @see org.sonatype.plexus.rest.resource.AbstractPlexusResource#getResourceProtection()
     */
    @Override
    public PathProtectionDescriptor getResourceProtection() {
        return new PathProtectionDescriptor( getResourceUri(), "authcNxBasic,perms[nexus:status]" );
    }

    /* (non-Javadoc)
     * @see org.sonatype.plexus.rest.resource.AbstractPlexusResource#getPayloadInstance()
     */
    @Override
    public Object getPayloadInstance() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.sonatype.plexus.rest.resource.AbstractPlexusResource#get(org.restlet.Context, org.restlet.data.Request, org.restlet.data.Response, org.restlet.resource.Variant)
     */
    @Override
    @PUT
    public Object get(Context context, Request request, Response response, Variant variant) throws ResourceException {
        final UserTokenResource userTokenResource = new UserTokenResource();
        
        AuthenticationClientPermissions originalClientPermissions = getClientPermissionsForCurrentUser( request );
        
        final String loggedInUsername = originalClientPermissions.getLoggedInUsername();
        if (loggedInUsername != null) {
            final String passwordToken = this.userDao.getUserPasswordToken(loggedInUsername);
            userTokenResource.setPasswordToken(passwordToken);
        }
        
        return userTokenResource;
    }

    @SuppressWarnings( "all" )
    @javax.xml.bind.annotation.XmlType( name = "user-token-resource" )
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    public static class UserTokenResource implements Serializable {
        private String passwordToken;

        /**
         * @return the passwordToken
         */
        public String getPasswordToken() {
            return passwordToken;
        }

        /**
         * @param passwordToken the passwordToken to set
         */
        public void setPasswordToken(String passwordToken) {
            this.passwordToken = passwordToken;
        }
    }
}
