## REMOTE_USER & Token Authentication
This plugin adds two forms of authentication to Nexus 2.0:

- REMOTE_USER, also known as container-managed authentication for the UI
- Token based authentication for the maven level interactions such uploading artifacts

This two-source authentication system is designed to make Nexus work in an environment where a web-based container or server level authentication system, such as Shibboleth or Jasig CAS, is used. These systems require the user enter their credentials on a central login page and the protected application is then simply told via HTTP headers (that is agross simplification) who is logged in. There is no ability for Nexus to validate the user's actual credentials. The problem is a password is still needed to make Maven command line interactions function as it is not possible to shim in a web based auth system in Maven's artifact resolution. The use of a long random token password allows for secure usage from Maven without requiring the user's SSO system password.

## Plugin Installation
Copy `nexus-rut-auth-plugin-X.Y.Z.jar` to `$NEXUS_BASE/sonatype-work/nexus/plugin-repository/nexus-rut-auth-plugin-X.Y.Z/nexus-rut-auth-plugin-X.Y.Z.jar`

Copy `nexus-rut-auth-filter-X.Y.Z.jar` to `$NEXUS_BASE/nexus-oss-webapp/nexus/WEB-INF/lib/nexus-rut-auth-filter-X.Y.Z.jar`

Create `$NEXUS_BASE/sonatype-work/nexus/conf/rut-auth-plugin.xml` from the following template:

    <?xml version="1.0" encoding="UTF-8"?> 
    <rutAuthConfiguration  
        xmlns="http://www.sonatype.org/xsd/nexus-token-auth-plugin-1.0.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> 
          
        <version>1.0.0</version> 
          
        <userFile>/svn/etc/apache-passwd</userFile> 
        <emailDomain>wisc.edu</emailDomain> 
        <remoteUserLoginRedirectUrl>/maven/FORCE_AUTH</remoteUserLoginRedirectUrl>
        <remoteUserLogoutRedirectUrl>/maven/PubCookie.logout</remoteUserLogoutRedirectUrl>
        <defaultRoles> 
            <defaultRole>uw-sso-user</defaultRole> 
        </defaultRoles> 
    </rutAuthConfiguration>

### Plugin Configuration Options

 - **userFile** - The apache passwd file to manage user password tokens in. This file can be safely modified by other applications and the plugin will read the changes.
 - **emailDomain** - Optional domain to append to the REMOTE_USER username to fill out the user's email field in Nexus.
 - **remoteUserLoginRedirectUrl** - The URL to redirect unauthenticated users to which will force authentication with the external authentication system.
 - **remoteUserLogoutRedirectUrl** - The URL to redirect authenticated users to which will log them out of the external authentication system.
 - **defaultRoles** - A list of **defaultRole** elements that define the Nexus roles that all users authenticated by this realm are placed into.

## How It Works
The `nexus-rut-auth-filter-X.Y.Z.jar` library adds a filter into the Nexus authentication processing that looks at the value of HttpServletRequest.getRemoteUser(). If a value is specified an special `RemoteUserAuthenticationToken` is created making it appear as though the user spefied by REMOTE_USER has authenticated to Nexus.

The `nexus-rut-auth-plugin-X.Y.Z.jar` adds a new `org.apache.shiro.realm.Realm` that knows how to handles the `RemoteUserAuthenticationToken` created by the filter. The realm can also handle traditional username/password authentication using randomly generated password tokens stored in an Apache passwd formatted file allowing Maven clients to authenticate via HTTP Basic Auth.

## Other Stuff
Take a look at the [GroupId Management Plugin](https://github.com/UW-Madison-DoIT/nexus-groupid-management-plugin) for a utility to easily manage groupId level access permissions within repositories.

