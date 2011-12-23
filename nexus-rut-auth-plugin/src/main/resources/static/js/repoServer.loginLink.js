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
    		
    		doLogin : function(redirect) {
                Ext.Ajax.request({
                	scope : this,
                    method : 'GET',
                    url : Sonatype.config.servicePath + "/rut/remote_user",
                    callback : function(options, success, response) {
                    	var responseData = Ext.decode( response.responseText );
                        if (responseData.remoteUser) {
                        	Sonatype.utils.doLogin(null, response.remoteUser, Sonatype.repoServer.RUTConfig.loggedInUserSource);
                        }
                        else if (redirect && Sonatype.repoServer.RUTConfig.loginUrl != undefined) {
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
	            	Sonatype.repoServer.RUTHandler.doLogin(true);
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
        	'color' : '#15428B',
            'cursor' : 'pointer',
            'text-align' : 'right'
        });
    },
    
    updateLeftWhenLoggedOut : function(linkEl) {
        linkEl.update('NetID Log In');
        this.setRemoteUserClickLink(linkEl);
        linkEl.setStyle({
            'color' : '#15428B',
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
          'color' : '#15428B',
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
	Sonatype.repoServer.RUTHandler.doLogin(false);
});

