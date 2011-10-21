(function() {
    Sonatype.repoServer.RUTHandler = function() {
    	return {
    		doLogin : function(redirect) {
                Ext.Ajax.request({
                	scope : this,
                    method : 'GET',
                    url : Sonatype.config.servicePath + "/rut/remote_user",
                    callback : function(options, success, response) {
                    	var responseData = Ext.decode( response.responseText );
                        if (responseData.remoteUser) {
                        	Sonatype.utils.doLogin(null, response.remoteUser, "RUT");
                        }
                        else if (redirect) {
                        	var loginUrl = responseData.loginUrl;
                        	if (loginUrl.indexOf("/") == 0) {
                        		loginUrl = Sonatype.config.host + loginUrl;
                        	}

                        	window.location.href = loginUrl;
                        }
                    }
                });
	        },
	        
	        loginHandler : function() {
	            if (Sonatype.user.curr.isLoggedIn) {
	                alert("SHOULD NEVER BE CALLED WHEN LOGGED IN")
	            }
	            else {
	            	Sonatype.repoServer.RUTHandler.doLogin(true);
	            }
	        }
    	};
    }();
})();


Ext.apply(Sonatype.headLinks.prototype, {
    tokenLinkEventApplied : false,
    
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
    }
});

//Check if the user is already logged in
Ext.onReady(function() {
	Sonatype.repoServer.RUTHandler.doLogin(false);
});

