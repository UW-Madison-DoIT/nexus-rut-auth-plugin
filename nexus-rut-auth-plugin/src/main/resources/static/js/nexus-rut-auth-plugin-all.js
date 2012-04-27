/*
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
(function() {
	Sonatype.repoServer.RUTConfig = {
			loginUrl : undefined,
		    logoutUrl : undefined,
		    loggedInUserSource : undefined
	};
	
	Sonatype.repoServer.RUTHandler = function() {
    	return {
    		loadRutStatus : function() {
    			Ext.Ajax.request({
                	scope : this,
                    method : 'GET',
                    url : Sonatype.config.servicePath + "/rut/status",
                    callback : function(options, success, response) {
                    	var responseData = Ext.decode( response.responseText );
                    	
                    	Sonatype.repoServer.RUTConfig.loginUrl = responseData.loginUrl;
                    	Sonatype.repoServer.RUTConfig.logoutUrl = responseData.logoutUrl;
                    	Sonatype.repoServer.RUTConfig.loggedInUserSource = responseData.loggedInUserSource;
                    }
                });
    		},
    		
    		checkRemoteUser : function() {
                Ext.Ajax.request({
                    scope : this,
                    method : 'GET',
                    url : Sonatype.config.servicePath + "/rut/remote_user",
                    callback : function(options, success, response) {
                        var responseData = Ext.decode( response.responseText );
                        
                        /*
                         * if remote user and user not auth'd - doLogin
                         * if remote user and auth'd - noop
                         * if no remote user and not auth'd - noop 
                         * if no remote user and auth'd doLogout
                         */
                        
                        if (responseData.remoteUser && !Sonatype.user.curr.isLoggedIn) {
                            Sonatype.utils.doLogin(null, response.remoteUser, Sonatype.repoServer.RUTConfig.loggedInUserSource);
                        }
                        else if (!responseData.remoteUser && Sonatype.user.curr.isLoggedIn) {
                            Sonatype.repoServer.RUTHandler.doLogout();
                        }
                        
                        setTimeout("Sonatype.repoServer.RUTHandler.checkRemoteUser()", 30000);
                    }
                });
    		},
    		
    		doLogin : function() {
                Ext.Ajax.request({
                	scope : this,
                    method : 'GET',
                    url : Sonatype.config.servicePath + "/rut/remote_user",
                    callback : function(options, success, response) {
                    	var responseData = Ext.decode( response.responseText );
                        if (responseData.remoteUser) {
                        	Sonatype.utils.doLogin(null, response.remoteUser, Sonatype.repoServer.RUTConfig.loggedInUserSource);
                        }
                        else if (Sonatype.repoServer.RUTConfig.loginUrl != undefined) {
                        	var loginUrl = Sonatype.repoServer.RUTConfig.loginUrl;
                        	if (loginUrl.indexOf("/") == 0) {
                        		loginUrl = Sonatype.config.host + loginUrl;
                        	}

                        	window.location.href = loginUrl;
                        }
                    }
                });
	        },
	        
    		doLogout : function() {
    	        if (Sonatype.user.curr.isLoggedIn) {
    	        	//If if there is another logout url and the user is logged in as a remote user change up the logout redirect
    	        	var redirectLocation = 'index.html#welcome';
	    			if (Sonatype.repoServer.RUTConfig.logoutUrl != undefined && Sonatype.user.curr.loggedInUserSource == Sonatype.repoServer.RUTConfig.loggedInUserSource) {
	    				redirectLocation = Sonatype.repoServer.RUTConfig.logoutUrl;
	    			}
	    			
    	        	// Code from repoServer.RepoServer.js 
	    			// do logout
	    			Ext.Ajax.request({
    	                scope : this,
    	                method : 'GET',
    	                url : Sonatype.config.repos.urls.logout,
    	                callback : function(options, success, response) {
    	                	Sonatype.utils.authToken = null;
    	                	Sonatype.view.justLoggedOut = true;
    	                	Sonatype.utils.loadNexusStatus();
    	                	window.location = redirectLocation;
    	                }
	    			});
    	        }
	        },
	        
	        loginHandler : function() {
	            if (Sonatype.user.curr.isLoggedIn) {
	                alert("SHOULD NEVER BE CALLED WHEN LOGGED IN")
	            }
	            else {
	            	Sonatype.repoServer.RUTHandler.doLogin();
	            }
	        },
	        
	        logoutHandler : function() {
	            if (!Sonatype.user.curr.isLoggedIn) {
	                alert("SHOULD NEVER BE CALLED WHEN LOGGED OUT")
	            }
	            else {
	            	Sonatype.repoServer.RUTHandler.doLogout();
	            }
	        }
    	};
    }();
})();


Ext.apply(Sonatype.headLinks.prototype, {
    tokenLinkEventApplied : false,
    logoutLinkEventApplied : false,
    
    updateLeftWhenLoggedIn : function(linkEl) {
        this.removeRemoteUserClickLink(linkEl);
        linkEl.update(Sonatype.user.curr.username);
        //TODO figure out how to get EXT to remove the styling set in updateLeftWhenLoggedOut
        
        Ext.Ajax.request({
        	scope : this,
            method : 'GET',
            url : Sonatype.config.servicePath + "/rut/user_token",
            callback : function(options, success, response) {
            	var responseData = Ext.decode( response.responseText );
                
            	var logoDiv = Ext.get('logo');
                var logoSpan = logoDiv.select('span');
                logoSpan.update("Maven Password Token: " + responseData.passwordToken);
            }
        });
    },
    
    updateRightWhenLoggedIn : function(linkEl) {
        linkEl.update('Log Out');
        this.setLogoutClickLink(linkEl);
        linkEl.setStyle({
        	'color' : '#FAC500',
            'cursor' : 'pointer',
            'text-align' : 'right'
        });
    },
    
    updateLeftWhenLoggedOut : function(linkEl) {
        linkEl.update('NetID Log In');
        this.setRemoteUserClickLink(linkEl);
        linkEl.setStyle({
            'color' : '#FAC500',
            'cursor' : 'pointer',
            'text-align' : 'right'
        })
        
        var logoDiv = Ext.get('logo');
        var logoSpan = logoDiv.select('span');
        logoSpan.update("");
    },

    updateMiddleWhenLoggedOut : function(linkEl) {
        linkEl.update(' | ');
    },

    updateRightWhenLoggedOut : function(linkEl) {
      this.removeLogoutClickLink(linkEl);
      linkEl.update('Token Log In');
      this.setClickLink(linkEl);
      linkEl.setStyle({
          'color' : '#FAC500',
          'cursor' : 'pointer',
          'text-align' : 'right'
      });
    },
    
    setRemoteUserClickLink : function(el) {
    	if (!this.tokenLinkEventApplied) {
	        el.on('click', 
	                Sonatype.repoServer.RUTHandler.loginHandler);
	        this.tokenLinkEventApplied = true;
    	}
    },
    removeRemoteUserClickLink : function(el) {
        el.un('click', 
                Sonatype.repoServer.RUTHandler.loginHandler);
        this.tokenLinkEventApplied = false;
    },
    setLogoutClickLink : function(el) {
    	if (!this.logoutLinkEventApplied) {
	        el.on('click', 
	                Sonatype.repoServer.RUTHandler.logoutHandler);
	        this.logoutLinkEventApplied = true;
    	}
    },
    removeLogoutClickLink : function(el) {
        el.un('click', 
                Sonatype.repoServer.RUTHandler.logoutHandler);
        this.logoutLinkEventApplied = false;
    }
});

//Check if the user is already logged in
Ext.onReady(function() {
	Sonatype.repoServer.RUTHandler.loadRutStatus();
	Sonatype.repoServer.RUTHandler.checkRemoteUser();
});

