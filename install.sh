#!/bin/bash

set -e

RUT_VERSION=1.0.3-SNAPSHOT
NEXUS_BASE=/Users/edalquist/tmp/nexus

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

