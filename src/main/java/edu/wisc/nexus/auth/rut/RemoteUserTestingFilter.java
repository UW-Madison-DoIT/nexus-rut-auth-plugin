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

import java.io.File;
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.web.servlet.AbstractFilter;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RemoteUserTestingFilter extends AbstractFilter {
    
    private File remoteUserFile;

    /* (non-Javadoc)
     * @see org.apache.shiro.web.servlet.AbstractFilter#onFilterConfigSet()
     */
    @Override
    protected void onFilterConfigSet() throws Exception {
        final String remoteUserFileName = this.getInitParam("remoteUserFile");
        this.remoteUserFile = new File(remoteUserFileName);
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final String remoteUser = StringUtils.trimToNull(FileUtils.readFileToString(this.remoteUserFile));
        
        if (remoteUser != null) {
            request = new HttpServletRequestWrapper((HttpServletRequest) request) {
                /* (non-Javadoc)
                 * @see javax.servlet.http.HttpServletRequestWrapper#getRemoteUser()
                 */
                @Override
                public String getRemoteUser() {
                    return remoteUser;
                }
            };
        }
        
        chain.doFilter(request, response);
    }
}
