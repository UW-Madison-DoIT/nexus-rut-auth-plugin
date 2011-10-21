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

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Component( role = NexusIndexHtmlCustomizer.class, hint = "RemoteUserTokenIndexHtmlCustomizer" )
public class RemoteUserTokenIndexHtmlCustomizer extends AbstractNexusIndexHtmlCustomizer {
    @Override
    public String getPostHeadContribution(Map<String, Object> ctx) {
        String version = getVersionFromJarFile("/META-INF/maven/edu.wisc/nexus-rut-auth-plugin/pom.properties");
        if (version.endsWith("-SNAPSHOT")) {
            //Get around caching on snapshot dev
            version = Long.toString(System.currentTimeMillis());
        }

        return "<script src=\"static/js/nexus-rut-auth-plugin-all.js" + (version == null ? "" : "?" + version)
                + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";

    }
}
