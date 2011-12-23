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

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.noelios.restlet.ext.servlet.ServletCall;
import com.noelios.restlet.http.HttpCall;
import com.noelios.restlet.http.HttpRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Component( role = PlexusResource.class, hint = "RemoteUserLookupPlexusResource" )
@Path( RemoteUserLookupPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class RemoteUserLookupPlexusResource extends AbstractPlexusResource {
    public static final String RESOURCE_URI = "/rut/remote_user"; 
    
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
        //TODO would love a RESTLET specific way to get at the actual remote_user value :(
        final RemoteUserResource remoteUserResource = new RemoteUserResource();
        
        if (request instanceof HttpRequest) {
            final HttpCall httpCall = ((HttpRequest) request).getHttpCall();
            if (httpCall instanceof ServletCall) {
                HttpServletRequest httpServletRequest = ((ServletCall) httpCall).getRequest();
                while (httpServletRequest instanceof HttpServletRequestWrapper) {
                    final ServletRequest servletRequest = ((HttpServletRequestWrapper)httpServletRequest).getRequest();
                    if (!(servletRequest instanceof HttpServletRequest)) {
                        break;
                    }
                    
                    httpServletRequest = (HttpServletRequest)servletRequest;
                }
                final String remoteUser = httpServletRequest.getRemoteUser();
                remoteUserResource.setRemoteUser(remoteUser);
            }
        }
        
        return remoteUserResource;
    }

    @SuppressWarnings( "all" )
    @javax.xml.bind.annotation.XmlType( name = "remote-user-resource" )
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    public static class RemoteUserResource implements Serializable {
        private String remoteUser;

        public String getRemoteUser() {
            return remoteUser;
        }
        public void setRemoteUser(String remoteUser) {
            this.remoteUser = remoteUser;
        }
    }
}
