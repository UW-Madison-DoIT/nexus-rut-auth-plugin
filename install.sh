#!/bin/bash
#
# Licensed to Jasig under one or more contributor license
# agreements. See the NOTICE file distributed with this work
# for additional information regarding copyright ownership.
# Jasig licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file
# except in compliance with the License. You may obtain a
# copy of the License at:
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on
# an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#


set -e

RUT_VERSION=1.0.4-SNAPSHOT
NEXUS_BASE=/home/edalquist/Downloads/nexus

# Download and install rut plugin
RUT_PLUGIN_BASEDIR=${NEXUS_BASE}/sonatype-work/nexus/plugin-repository/nexus-rut-auth-plugin-
RUT_PLUGIN_DIR=${RUT_PLUGIN_BASEDIR}${RUT_VERSION}
rm -Rf ${RUT_PLUGIN_BASEDIR}*
mkdir -p ${RUT_PLUGIN_DIR}
cp ./nexus-rut-auth-plugin/target/nexus-rut-auth-plugin-${RUT_VERSION}.jar ${RUT_PLUGIN_DIR}/nexus-rut-auth-plugin-${RUT_VERSION}.jar
	

# Download and install rut filter
RUT_FILTER_BASE=${NEXUS_BASE}/nexus-oss-webapp/nexus/WEB-INF/lib/nexus-rut-auth-filter-
RUT_FILTER=${RUT_FILTER_BASE}${RUT_VERSION}.jar
rm -f ${RUT_FILTER_BASE}*
cp ./nexus-rut-auth-filter/target/nexus-rut-auth-filter-${RUT_VERSION}.jar ${RUT_FILTER}

# Update the web.xml to enable the remote-user filter
sed -ie 's/org.sonatype.nexus.security.filter.authc.NexusSecureHttpAuthenticationFilter/edu.wisc.nexus.auth.rut.RemoteUserNexusSecureHttpAuthenticationFilter/g' ${NEXUS_BASE}/nexus-oss-webapp/nexus/WEB-INF/web.xml

