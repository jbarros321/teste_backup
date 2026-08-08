angular.module('credparapp.services', ['ionic'])

	.value('X2JS', X2JS)
	.value('Base64', Base64)
	.value('CryptoJS', CryptoJS)
	
	.provider('x2js', function(){
	    this.config = {};
	
	    this.$get = ['X2JS', function (X2JS) {
	      return new X2JS(this.config);
	    }];
	})
	
	.provider('$credpar', function() {

        this._serverConfigKey = 'serverConfigCred';
        this._gKey = '__gCred';
        this._appName = 'CredPar';
        this._fileSystemName = 'credpar.log';
        this._minVersion = '3.9.0'; //Versão mínima para usar o aplicativo
        this._appID = 'unknow';

        var dummy = document.createElement('a');
        dummy.setAttribute('href', '.');

        this._baseURL = dummy.href;

        this.setConfig = function(config){
        	this._appID = config.appID != null ? config.appID : this._appID;
            this._appName = config.appName != null ? config.appName : this._appName;
            this._baseURL = config.baseURL != null ? config.baseURL : this._baseURL;
            this._minVersion = config.minVersion != null ? config.minVersion : this._minVersion;
            this._defaultProvider = config.hasDefaultProvider != null ? config.hasDefaultProvider : this._defaultProvider;
            
            //Salvamos no storage para que as telas de controle (login e altreção do servidor) possam utilizar
            window.localStorage.setItem('appID', this._appID);
            window.localStorage.setItem('appName', this._appName);
            window.localStorage.setItem('minVersion', this._minVersion);
        };
        
		var appNameValue = window.localStorage.getItem('appName');
		var appIDValue = window.localStorage.getItem('appID');
		var minVersionValue = window.localStorage.getItem('minVersion');
		
		if(appNameValue != null){
			this._appName = appNameValue;	
		}
		
		if(appIDValue != null){
			this._appID = appIDValue;	
		}
		
		if(minVersionValue != null){
			this._minVersion = minVersionValue;
		}
        
		var ctx = this;

		var onDeviceReady = function(){
        	ctx._loadSharedConfig();
        };
        
        document.addEventListener('deviceready', onDeviceReady, false);
        
        this._loadSharedConfig = function(){
    		var isAndroid = navigator.userAgent.match(/Android/i) == 'Android';

    		if(isAndroid && cordova.file != null){
        		window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, ctx._gotFileSystemToRead, ctx._fileSystemFail);	
        	}
        };

        this._gotFileSystemToRead = function(fileSystem) {
            fileSystem.root.getFile(
            	ctx._fileSystemName, 
            	{create: false, exclusive: false}, 
            	ctx._readFileSystem, 
            	ctx._fileSystemFail
            );
        };
        
        this._readFileSystem = function(fileEntry) {
        	fileEntry.file(function(file){
        		
        		var reader = new FileReader();
                reader.onloadend = function(event) {
                    var text = event.target.result;
                    
                    if(text != null){
                    	try{
                        	var json = CryptoJS.AES.decrypt(text, window.device.uuid).toString(CryptoJS.enc.Utf8);
                        	var config = eval('(' + json + ')');
                        	
                        	for (var key in config) {
                        		//Esses atributos não devem ser compartilhados entre as Apps
                        		var value = config[key];
                        		
                        		if(value != null){
                        			window.localStorage.setItem(key, value);
                        		}
                        	}
                        }catch(ignored){
                        	console.log('Oops, arquivo de configuração inválido.');	
                        }	
                    }
                };

                reader.readAsText(file);	
        	}, ctx._fileSystemFail);
        };

        this._writeSharedConfig = function(){
        	var isAndroid = navigator.userAgent.match(/Android/i) == 'Android';
        	
        	if(isAndroid && cordova.file != null){
        		window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, ctx._gotFileSystemToWrite, ctx._fileSystemFail);	
        	}
        };

        this._gotFileSystemToWrite = function(fileSystem) {
            fileSystem.root.getFile(
            	ctx._fileSystemName, 
            	{create: true, exclusive: false}, 
            	ctx._writeFileSystem, 
            	ctx._fileSystemFail
            );
        };
        
        this._writeFileSystem = function(fileEntry) {
        	fileEntry.createWriter(function(writer){
                var data = {};
                
                for (var key in window.localStorage){
                	data[key] = window.localStorage.getItem(key);
            	}
                
                var json = JSON.stringify(data);
                var encrypted = CryptoJS.AES.encrypt(json, window.device.uuid).toString();
                
                writer.write(encrypted);
        	}, ctx._fileSystemFail);
        };
        
        this._fileSystemFail = function(error) {
            console.log(error.code);
        };
        
        this.$get = ['$injector', '$document', '$timeout', '$rootScope', '$window', function ($injector, $document, $timeout, $rootScope, $window) {

            var ctx = this;

            function _setStoreItem(key, value){
                if(value != null){
                    $window.localStorage.setItem(key, value);
                }else{
                    $window.localStorage.removeItem(key);
                }
                
                ctx._writeSharedConfig();
            }

            function _getStoreItem(key){
                return $window.localStorage.getItem(key);
            }

            /*
                Params
                serverConfig = {address: '127.0.0.1', port: '8080', protocol: 'http'}
            */
            function _setServerConfig(serverConfig){
                var serverConfigJson = JSON.stringify(serverConfig);
                _setStoreItem(ctx._serverConfigKey, serverConfigJson);
            }
            
            function _clearServerConfig(){
            	_setStoreItem(ctx._serverConfigKey, null);
            }

            function _getServerConfig(){
                var serverConfig = null;
                var serverConfigJson = _getStoreItem(ctx._serverConfigKey);

                if(serverConfigJson != null){
                    serverConfig = JSON.parse(serverConfigJson);
                    
                    if(serverConfig.protocol == 'default'){
                    	serverConfig.address = _getDefaultProvider().address;
                    	serverConfig.port = _getDefaultProvider().port;
                    }
                }else{
                	
            		//Se o provedor padrão estiver habilitado e não houver configuração,
            		//conectamos no provedor padrão
            		serverConfig = {
            			protocol: 'default',
            			address: _getDefaultProvider().address,
            			port: _getDefaultProvider().port
            		}
                }

                return serverConfig;
            }

            function _isDefaultProviderConnection(){
            	var serverConfig = _getServerConfig();

                if(serverConfig != null && serverConfig.protocol == 'default'){
                	return true;
                }
                
                return false;
            }
            
            function _hasConnectionConfig(){
                return _getServerConfig() != null && _getMgeSessionId() != null;
            }

            function _hasConnectionConfigClient(){
            	return _getServerConfig() != null && _getStoreItem("isCliente") && _getStoreItem("userCredID") != null;
            }

            function _getServerUrl(){
                var serverConfig = _getServerConfig();
                var serverUrl = null;

                if(serverConfig != null){
                    var protocol = serverConfig.protocol;
                    var address = serverConfig.address;
                    var port = serverConfig.port;
                    
                    if(protocol == 'default'){
                    	protocol = _getDefaultProvider().protocol;
                    	address = _getDefaultProvider().address;
                    	port = _getDefaultProvider().port;
                    }
                	
                	serverUrl = protocol + '://';
                    serverUrl += address;
                    
                    if(port != null){
                        serverUrl += ':' + port;
                    }
                }

                return serverUrl;
            }

            function _getMgeSessionId(){
                return _getStoreItem(ctx._gKey);
            }

            function _setMgeSessionId(value){
            	if(value == null){
            		_setStoreItem('userCredID', null);
            		_setStoreItem('kCredID', null);
            	}
            	
                return _setStoreItem(ctx._gKey, value);
            }

            function _redirectToLogin() {
            	_setStoreItem('lastHrefCred', ctx._baseURL + 'index.html');
                
                $window.location.href = _getRedirectLoginUrl();
            }

            function _getRedirectLoginUrl(){
                var url = ctx._baseURL + 'index.html';
                
                if(_getStoreItem("isCliente")){
            		url += '#/welcome/login/cliente';
                } else {
                	url += '#/welcome/login/lojista'
                }
                
                return url;
            }

            function _updateRequestLoginUrl(){
                $window.top.window.REQUEST_LOGIN_URL = _getRedirectLoginUrl();
            }

            function _redirectToApp() {
                var lastHrefCred = _getStoreItem('lastHrefCred');

                if(lastHrefCred == null){
                    lastHrefCred = ctx._baseURL + 'index.html';
                }

                $window.location.href = lastHrefCred;
            }
            
            function _getAppName() {
            	return ctx._appName;
            }
            
            function _getMinVersion() {
            	return ctx._minVersion;
            }
            
            function _hasDefaultProvider() {
            	return ctx._defaultProvider;
            }
            
            function _getAppID() {
            	return ctx._appID;
            }
            
            function _getDefaultProvider(){
            	var config = {};
            	config.protocol = 'http';
            	config.port = 8280;
            	config.userIOS = 'APPLE';
            	config.userAndroid = 'GOOGLE';
            	
                //http://192.168.1.215:8512/

//	    		config.passIOS = 'sankhya123';
//	    		config.passAndroid = 'sankhya123';
	    		config.address = 'credpar.ddns.com.br';
	    		// usar local - config.address = '192.168.0.118'
            	return config;
            }
            
            function _emptyAsNull(value){
            	if(value != null && value.trim().length == 0){
    				return null;
    			}else{
    				return value;
    			}
            }
            
            return {
            	getDefaultProvider:				_getDefaultProvider,
            	hasDefaultProvider:         	_hasDefaultProvider,
            	isDefaultProviderConnection:	_isDefaultProviderConnection,
                getAppName:            			_getAppName,
                getMinVersion:          		_getMinVersion,
                getAppID:         				_getAppID,
                redirectToLogin:        		_redirectToLogin,
                redirectToApp:          		_redirectToApp,
                setStoreItem:           		_setStoreItem,
                getStoreItem:           		_getStoreItem,
                getServerConfig:        		_getServerConfig,
                clearServerConfig:        		_clearServerConfig,
                setServerConfig:        		_setServerConfig,
                getServerUrl:           		_getServerUrl,
                getMgeSessionId:        		_getMgeSessionId,
                setMgeSessionId:        		_setMgeSessionId,
                hasConnectionConfig:    		_hasConnectionConfig,
                hasConnectionConfigClient:    	_hasConnectionConfigClient,
                emptyAsNull:			    	_emptyAsNull
            };
        }];
    })

	.config(['$httpProvider', function ($httpProvider) {
	
	        var snkwInterceptor = ['$q', '$credpar', 'x2js', function ($q, $credpar, x2js) {
	
	            function responseIsXml(response) {
	                var contentType = response.headers('content-type'),
	                    XML = '/xml',
	                    minIndex = 'text/xml'.indexOf(XML);
	
	                if (contentType) {
	                    return contentType.indexOf(XML) >= minIndex;
	                } else {
	                    return false;
	                }
	            }
	
	            return {
	                request: function (config) {
	                    //console.log(config);
	                    return config;
	                },
	
	                response: function (result) {
	                    if (result && responseIsXml(result)) {
	                        result.data = x2js.xml_str2json(result.data);
	                        return result;
	                    } else {
	                        return $q.when(result);
	                    }
	                }
	            };
	        }];
	
	        $httpProvider.interceptors.push(snkwInterceptor);
	   }])
	   
   .factory('$credparService', ['$http', '$q', 'x2js', '$credpar', 'Base64', '$ionicPopup', '$interval', '$rootScope', '$window', function($http, $q, x2js, $credpar, Base64, $ionicPopup, $interval, $rootScope, $window) {

        var STATUS_ERROR            = '0';
        var STATUS_OK               = '1';
        var STATUS_INFO             = '2';
        var STATUS_TIMEOUT          = '3';
        var STATUS_SERVICE_CANCELED = '4';
        var DEFAULT_SERVICE_TIMEOUT = 30000;

        var defaultModule = 'mge';
        var fallbackErrorShowing = false;
        var counter = 0;
        var serviceUrlTemplate = '/{0}/service.sbr?serviceName={1}&counter={2}&application={3}&allowConcurrentCalls=true';
         
        function _callService(callConfig){

            var service = callConfig.service;
            var params = callConfig.params;
            var callback = callConfig.callback;
            var timeout = callConfig.timeout != null ? callConfig.timeout : DEFAULT_SERVICE_TIMEOUT;
            var throwsErrors = callConfig.throwsErrors == null || callConfig.throwsErrors == true;
            var ignoreLoadingBar = callConfig.ignoreLoadingBar === true;

            var serverAddress = $credpar.getServerUrl();
            
            //Redirecionamos para que a App solicite as configurações
            if(serverAddress == null){
                $credpar.redirectToLogin();
                return;
            }

            var module = defaultModule;
            var serviceName = service;

            if (service.indexOf('@') > -1) {
                var s = service.split('@');
                module = s[0];
                serviceName = s[1];
            }

            var url = serviceUrlTemplate.replace('{0}', module);
            url = url.replace('{1}', serviceName);
            url = url.replace('{2}', counter++);
            url = url.replace('{3}', $credpar.getAppName());

            var mgeSession = $credpar.getMgeSessionId();

            if(mgeSession != null){
                url += '&mgeSession=' + mgeSession;
            }

            var headersConfig = {'Content-Type': 'text/xml; charset=utf-8'};
            var kCredID = $credpar.getStoreItem('kCredID');
            
            if(kCredID != null){
            	headersConfig.Authorization = 'Bearer ' + kCredID;
            }
            
            var requestBodyObj = {
                serviceRequest : {
                    _serviceName: serviceName,
                    requestBody : {}
                }
            };

            if(params != null){
                requestBodyObj.serviceRequest.requestBody = params;
            }

            var xmlData = x2js.json2xml_str(requestBodyObj);

            var canceler = $q.defer();
            
            $http({
                url: serverAddress + url,
                method: 'POST',
                data: xmlData,
                headers: headersConfig,
                timeout: timeout,
                ignoreLoadingBar: ignoreLoadingBar
            }).success(function (data, status, headers, config) {
                var serverStatus = data.serviceResponse._status;
                var serverMsg = null;

                if(data.serviceResponse.statusMessage != null){
                   var msg = Base64.doDecode(data.serviceResponse.statusMessage.__cdata, false);
                   serverMsg = msg;
                }

                if(STATUS_TIMEOUT == serverStatus){
                	_redirectToLogin();
                }else if(STATUS_ERROR == serverStatus){
                	if(throwsErrors){
                		if(callback != null && callback.error != null){
                            callback.error(serverMsg, status, headers, config);
                        }else{
                            $ionicPopup.alert({cssClass: 'popUpCred',
                                title: 'Oops!',
                                template: '<div>'+ serverMsg +'</div>'
                            });
                        }
                	}
                }else if(STATUS_INFO == serverStatus || STATUS_OK == serverStatus){
                    if(callback != null){
                        if(callback.success != null){
                            callback.success(data.serviceResponse.responseBody);
                        }else{
                        	if (typeof callback === 'function') {
                        		callback(data.serviceResponse.responseBody);
                        	}
                        }
                    }
                }else if(STATUS_SERVICE_CANCELED == serverStatus){
                	if(throwsErrors){
	                	if(callback != null && callback.error != null){
	                        callback.error(serverMsg, status, headers, config);
	                    }else{
	                        $ionicPopup.alert({cssClass: 'popUpCred',
	                            title: 'Oops!',
	                            template: '<div>'+ serverMsg +'</div>'
	                        });
	                    }
                	}
                }
                
                if(callback != null){
                	if(callback['finally'] != null){
                		callback['finally']();
                	}else if(callback['finish'] != null){
                		callback['finish']();
                	}
            	}
            }).error(function (data, status, headers, config) {
                console.log('Connection Error: ' + status);

                if(throwsErrors){
	                var originalCallConfig = callConfig;
	
	                if(callback != null && callback.httpError != null){
	                    var takeControl = callback.httpError(data, status, headers, config);
	
	                    if( ! takeControl){
	                        _fallbackError(originalCallConfig);
	                    }else{
	                    	if(callback != null){
	                        	if(callback['finally'] != null){
	                        		callback['finally']();
	                        	}else if(callback['finish'] != null){
	                        		callback['finish']();
	                        	}
	                    	}
	                    }
	                }else{
	                    _fallbackError(originalCallConfig);
	                }
                }
            });
            
            return canceler;
        }

        function _fallbackError(originalCallConfig){

        	if( ! fallbackErrorShowing){
	        	var fallbackScope = $rootScope.$new(true);
	            fallbackScope.timeToRetry = 60;
	            fallbackScope.config = function($event){
	            	fallbackErrorShowing = false;
                	$interval.cancel(fallbackScope.retryTimer);
                    $credpar.redirectToLogin();
	            };
	
	            var fallbackPopup = $ionicPopup.show({
	                template: '<div style="text-align:center;"><span>Reconectar em <b>{{ timeToRetry }}</b>.</span></br><a style="color: #E42012; text-decoration: none;" href="#" ng-click="config($event)">Configurar conexão</a></div>',
	                title: 'Sem conexão com o Servidor',
	                subTitle: 'Verifique se você possui Internet.',
	                scope: fallbackScope,
	                buttons: [
	                    {
	                        text: 'Cancelar',
	                        onTap: function(e) {
	                        	fallbackErrorShowing = false;
	                        	$interval.cancel(fallbackScope.retryTimer);
	                        }
	                    },
	                    {
	                        text: '<b>Conectar</b>',
	                        type: 'button-positive',
	                        onTap: function(e) {
	                        	fallbackErrorShowing = false
	                            $interval.cancel(fallbackScope.retryTimer);
	                            _callService(originalCallConfig);
	                        }
	                    }
	                ]
	            });
	            
	            fallbackErrorShowing = true;
	
	            fallbackScope.retryTimer = $interval(function(){
	                if(fallbackScope.timeToRetry === 0){
	                    $interval.cancel(fallbackScope.retryTimer);
	                    fallbackPopup.close();
	                    fallbackErrorShowing = false;
	
	                    _callService(originalCallConfig);
	                }else{
	                    fallbackScope.timeToRetry--;
	                }
	            }, 1000);
        	}else{
        		if(originalCallConfig.callback != null){
                	if(originalCallConfig.callback['finally'] != null){
                		originalCallConfig.callback['finally']();
                	}else if(originalCallConfig.callback['finish'] != null){
                		originalCallConfig.callback['finish']();
                	}
            	}
        	}
        }

        function _logout(){
            _callService({
                service: 'MobileLoginSP.logout',
                throwsErrors: false
            });
            

            $credpar.clearServerConfig(); 
            $credpar.setStoreItem('userName', null);
            $credpar.setMgeSessionId(null);
            $credpar.redirectToLogin();
        }
    
        function _redirectToLogin(){
        	if($credpar.isDefaultProviderConnection()){
        		_defaultProviderLogin();
        	}else{
                $credpar.redirectToLogin();	
        	}
        }
        
        function _defaultProviderLogin(){
        	var data = {
            	NOMUSU: ionic.Platform.isAndroid() ? $credpar.getDefaultProvider().userAndroid : $credpar.getDefaultProvider().userIOS, 
            	INTERNO: ionic.Platform.isAndroid() ? $credpar.getDefaultProvider().passAndroid : $credpar.getDefaultProvider().passIOS
            };
        	
        	_login(data, function(data){
        		$credpar.redirectToApp();
        	});
        }
        
        function _login(data, success, error, httpError){
        	if(ionic.Platform.isReady){
        		_doLogin(data, success, error, httpError);
        	}else{
        		ionic.Platform.ready(function(){
            		_doLogin(data, success, error, httpError);
            	});		
        	}
        }

        function _loginCliente(data, success, error, httpError){
        	if(ionic.Platform.isReady){
        		_doLoginCliente(data, success, error, httpError);
        	}else{
        		ionic.Platform.ready(function(){
        			_doLoginCliente(data, success, error, httpError);
        		});		
        	}
        }
        
        
        function _buscaCidadesSegmentos(data, success, error, httpError){
       	 
          	_callService({
        		service: 'credparapp@CredParSP.buscaCidadesSegmentos',
        		params: data,
        		timeout: 60000,
        		callback: {
        			success: function(data){        				
        				if(success != null){
        					success(data);
        				}
        			},
        			error: function(msg, status, headers, config){
        				showAlert('Oops!', msg);
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		}
        	});
        	
        }
        
        
        function _salvarParceiroProspect(data, success, error, httpError){
        	var paramsPar = {
        			dataSet : { 
        				_rootEntity: "ParceiroProspect",
        				_includePresentationFields : "S", 
        				_parallelLoader: "true", 
        				entity : 
                        [
                            {
                                _path:"",
                                fieldset : {
                                    _list : "*"
                                }
                            },                            
        				    {
            					_path:"Parceiro",
            					field : {
            						_name : "NOMEPARC"
            					}
        				    },
        				    {
            					_path:"UnidadeFederativa",
            					field : {
            						_name : "UF"
            					}
        				    },
        				    {
            					_path:"Perfil",
            					field : {
            						_name : "DESCRTIPPARC"
            				    }
        				    },
        				    {
            					_path:"Cidade",
            					field : {
            						_name : "NOMECID"
            					}
        				    },
        				    {
            					_path:"ParceiroF2",
            					field : {
            						_name : "NOMEPARC"
            					}
        				    },
        				    {
        					_path:"Vendedor",
        					field : {
        						_name : "APELIDO"
        					}
        				    },
            				    {
            					_path:"UnidadeFederativaDestino",
            					field : {
            						_name : "UF"
            					}
        				    }
                        ],
        				dataRow : {
        					localFields : {
        						NOMEPAP : {
        							__cdata : data.NOMEPAP
        						},
        						CGC_CPF : {
        							__cdata : data.CGC_CPF
        						},
        						ENDERECO : {
        							__cdata : data.ENDERECO
        						},
        						NUMEND : {
        							__cdata : data.NUMEND
        						},
        						COMPLEMENTO : {
        							__cdata : data.COMPLEMENTO
        						},
                                EMISSAORG : {
                                    __cdata : data.EMISSAORG
                                },
        						NOMEBAI : {
        							__cdata : data.NOMEBAI
        						},
        						NOMECID : {
        							__cdata : data.NOMECID
        						},
        						CODUF : {
        							__cdata : data.CODUF
        						},
        						CEP : {
        							__cdata : data.CEP
        						},
        						TELEFONE : {
        							__cdata : data.TELEFONE
        						},
        						EMAIL : {
        							__cdata : data.EMAIL
        						},
        						DTNASC : {
        							__cdata : _getCorrectDate(data.DTNASC)
        						},
        						SEXO : {
        							__cdata : data.SEXO
        						},
        						RG : {
        							__cdata : data.RG
        						},
        						DATAEMISSAORG : {
        							__cdata : _getCorrectDate(data.DATAEMISSAORG)
        						},
        						EMISSAORG : {
        							__cdata : data.EMISSAORG
        						},
        						UFRG : {
        							__cdata : data.UFRG
        						},
        						NATURALIDADE : {
        							__cdata : data.NATURALIDADE
        						},
        						NOMEMAE : {
        							__cdata : data.NOMEMAE
        						},
        						NOMEPAI : {
        							__cdata : data.NOMEPAI
        						},
        						ESTADOCIVIL : {
        							__cdata : data.ESTADOCIVIL
        						},
        						NOMECONJUGE : {
        							__cdata : data.NOMECONJUGE
        						},
        						CELULARCONJUGE : {
        							__cdata : data.CELULARCONJUGE
        						},
        						TELCELULAR : {
        							__cdata : data.TELCELULAR
        						},
        						DIAPAGTO : {
        							__cdata : data.DIAPAGTO
        						},
        						NOMECARTAO : {
        							__cdata : data.NOMECARTAO
        						},
        						AD_OBS : {
        							__cdata : data.OBSERVACOES
        						},
        						TIPPESSOA : {
        							__cdata : "F"
        						},
                                CODPARCB2B :{
                                    __cdata : data.CODPARCB2B
                                },
        						ISPROPOSTACARTAO : {
        							__cdata : "S"
        						},
        						AD_LOCALTRABALHO : {
        							__cdata : data.AD_LOCALTRABALHO
        						},
        						AD_CARGO : {
        							__cdata : data.AD_CARGO
        						},
        						AD_TEMPOTRABALHO : {
        							__cdata : _getCorrectDate(data.AD_TEMPOTRABALHO)
        						},
        						AD_RENDA : {
        							__cdata : data.AD_RENDA
        						},
        						AD_TELTRABALHO : {
        							__cdata : data.AD_TELTRABALHO
        						},
        						AD_CPFCONJUGE : {
        							__cdata : data.AD_CPFCONJUGE
        						},
        						AD_TRABALHOCONJUGE : {
        							__cdata : data.AD_TRABALHOCONJUGE
        						},
        						AD_TEMPOTRABCONJUGE : {
        							__cdata : _getCorrectDate(data.AD_TEMPOTRABCONJUGE)
        						},
        						AD_RENDACONJUGE : {
        							__cdata : data.AD_RENDACONJUGE
        						},
        						AD_REFPESSOAL : {
        							__cdata : data.AD_REFPESSOAL
        						},
        						AD_TELPESSOAL1 : {
        							__cdata : data.AD_TELPESSOAL1
        						},
        						AD_REFPESSOAL2 : {
        							__cdata : data.AD_REFPESSOAL2
        						},
        						AD_TELREF2 : {
        							__cdata : data.AD_TELREF2
        						},
        						AD_REFCOMERCIAL1 : {
        							__cdata : data.AD_REFCOMERCIAL1
        						},
        						AD_TELCOM1 : {
        							__cdata : data.AD_TELCOM1
        						},
        						AD_REFCOMERCIAL2 : {
        							__cdata : data.AD_REFCOMERCIAL2
        						},
        						AD_TELCOM2 : {
        							__cdata : data.AD_TELCOM2
        						},
        						AD_VALORCOMPRA : {
        							__cdata : data.AD_VALORCOMPRA
        						}
        					} 
        				}
        			}
        	
        	};
        	
        	_callService({
        		service: 'CRUDServiceProvider.saveRecord',
        		params: paramsPar,
        		timeout: 60000,
        		callback: {
        			success: function(data){
        				if(success != null){
        					success(data);
        				}
        			},
        			error: function(msg, status, headers, config){
        				$ionicPopup.alert({cssClass: 'popUpCred',
        					title: 'Oops!',
        					template: msg
        				});
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		}
        	});

        	
        }
        
        function _getCorrectDate(strDate){
        	if(strDate != null && strDate != ""){
        		var pRegexp = /(\d{4})-(\d{1,2})-(\d{1,2})?/ig;
        		var match = pRegexp.exec(strDate);
        		
                console.log(match);

        		if(match == null){
                    var date = strDate.toISOString().substring(0, 10).split("-");
                    
                    var dataStr = date[2]+"/"+date[1]+"/"+date[0];
        		      
                    match = pRegexp.exec(dataStr);	

                    if(match == null){
                        return dataStr;
                    }
                    
        		}


        		
        		return match[3] +'/'+ match[2] +'/'+ match[1];
        	} else {
        		return "";
        	}
        }
        
        function _buscarParceiroProspect(data, success, error, httpError){
        	
	        	var paramsPar = {
	        			dataSet : { 
	        				_rootEntity: "ParceiroProspect",
	        				_includePresentationFields : "S", 
	        				_parallelLoader: "true", 
	        				entity : [
	        			    {
	        					_path:"",
	        					fieldset : {
	        						_list : "*"
	        					}
	        				},
	        				{
	        					_path:"Parceiro",
	        					field : {
	        						_name : "NOMEPARC"
	        					}
	        				},
	        				{
	        					_path:"UnidadeFederativa",
	        					field : {
	        						_name : "UF"
	        					}
	        				},
	        				{
	        					_path:"Perfil",
	        					field : {
	        						_name : "DESCRTIPPARC"
	        					}
	        				},
	        				{
	        					_path:"Cidade",
	        					field : {
	        						_name : "NOMECID"
	        					}
	        				},
	        				{
	        					_path:"ParceiroF2",
	        					field : {
	        						_name : "NOMEPARC"
	        					}
	        				},
	        				{
	        					_path:"Vendedor",
	        					field : {
	        						_name : "APELIDO"
	        					}
	        				},
	        				{
	        					_path:"UnidadeFederativaDestino",
	        					field : {
	        						_name : "UF"
	        					}
	        				}
	        				],
	        				criteria : {
	        					expression : "(this.ISPROPOSTACARTAO = ? AND CODPARCB2B = ? AND (this.CGC_CPF = ? OR this.CODPAP = ?) )",
	        					parameter : [{
	        						_type : "S",
	        						__text : "S" 
	        					},
					        	{
					        		_type : "S",
					        		__text :  data.codParcB2B
					        	},
					        	{
					        		_type : "S",
					        		__text :  data.CGC_CPF
					        	},					        	
                                {
                                    _type : "S",
                                    __text :  data.CGC_CPF
                                }
	        					]
	        				}
	        			}
	        	
	        	};
	        	
	        	_callService({
	        		service: 'CRUDServiceProvider.loadRecords',
	        		params: paramsPar,
	        		timeout: 60000,
	        		callback: {
	        			success: function(data){
	        				if(success != null){
	        					success(data);
	        				}
	        			},
	        			error: function(msg, status, headers, config){
	        				$ionicPopup.alert({cssClass: 'popUpCred',
	        					title: 'Oops!',
	        					template: msg
	        				});
	        				
	        				if(error != null){
	        					error(msg, status, headers, config);
	        				}
	        			},
	        			httpError: httpError
	        		}
	        	});
        

        }

        /*
        ** NAO REMOVER ESSA FUNCAO POIS PODE SER USADA NO FUTURO.
        function _buscarCartaoCliente(data, success, error, httpError){
            
                var paramsPar = {
                        dataSet : { 
                            _rootEntity: "CartaoCredito",
                            _includePresentationFields : "S", 
                            _parallelLoader: "true", 
                            entity : [
                            {
                                _path:"",
                                fieldset : {
                                    _list : "*"
                                }
                            }],
                            criteria : {
                                expression : "this.CODPARC = ?",
                                parameter : [{
                                    _type : "S",
                                    __text :  data.CODPARC
                                }]
                            }
                        }
                
                };
                
                _callService({
                    service: 'CRUDServiceProvider.loadRecords',
                    params: paramsPar,
                    timeout: 60000,
                    callback: {
                        success: function(data){
                            if(success != null){
                                success(data);                                                            
                            }
                        },
                        error: function(msg, status, headers, config){
                            $ionicPopup.alert({cssClass: 'popUpCred',
                                title: 'Oops!',
                                template: msg
                            });
                            
                            if(error != null){
                                error(msg, status, headers, config);
                            }
                        },
                        httpError: httpError
                    }
                });
        
        }*/
        
        
        
        function _buscarCidade(data, success, error, httpError){
        	
        	var paramsCid = {
        			pesquisa : { 
        				_nomeInstancia: "Cidade",
        				_campoCriterio: "NOMECID",
        				_valorCriterio: "",
        				_relationName: "Cidade",
        				_nomeInstanciaLocal: "ParceiroProspect",
        				_showInactives: "false",
        				clientEventList : {
        					clientEvent : "br.com.sankhya.opcredito.convertprospect"
        				} 
        			}
        	
        	};
        	
        	
        	_callService({
        		service: 'credparapp@CredParSP.buscarCidades',
        		params: paramsCid,
        		timeout: 60000,
        		callback: {
        			success: function(data){        				
        				if(success != null){
        					success(data);
        				}
        			},
        			error: function(msg, status, headers, config){
        				$ionicPopup.alert({cssClass: 'popUpCred',
        					title: 'Oops!',
        					template: msg
        				});
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		}
        	});
        	
        }
        
        function _buscarEstado(data, success, error, httpError){
        	
        	var paramsCid = {
        			pesquisa : { 
        				_nomeInstancia: "UnidadeFederativa",
        				_campoCriterio: "UF",
        				_valorCriterio: "",
        				_relationName: "UnidadeFederativa",
        				_nomeInstanciaLocal: "ParceiroProspect",
        				_showInactives: "false",
        				clientEventList : {
        					clientEvent : "br.com.sankhya.opcredito.convertprospect"
        				} 
        			}
        	
        	};
        	
        	_callService({
        		service: 'Pesquisa.applySearch',
        		params: paramsCid,
        		timeout: 60000,
        		callback: {
        			success: function(data){        				
        				if(success != null){
        					success(data);
        				}
        			},
        			error: function(msg, status, headers, config){
        				$ionicPopup.alert({cssClass: 'popUpCred',
        					title: 'Oops!',
        					template: msg
        				});
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		}
        	});
        	
        }
        
        function _buscarServidores(data, success, error, httpError){
        	
        	
        	_callService({
        		service: 'credparapp@CredParSP.buscarServidores',
        		params: null,
        		timeout: 60000,
        		callback: {
        			success: function(data){
        				if(success != null){
        					success(data);
        					        					
        				}
        			},
        			error: function(msg, status, headers, config){
        				$ionicPopup.alert({cssClass: 'popUpCred',
        					title: 'Oops!',
        					template: msg
        				});
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		}
        	});
        	
        }
        
		function _buscarNomeLojista(codParcB2B, codContatoB2B, success, error, httpError){
        	var paramsPar = {
        			entity : { 
        				_name: "Contato",
        				literalCriteria : {
        					expression : "this.CODPARC = ? AND this.CODCONTATO = ?",
        					param : [
        					         {
					        		  _type : "N",
					        		  _value : codParcB2B
        					         },
        					         {
					        		  _type : "N",
					        		  _value : codContatoB2B
					        	  	 }
        					        ]
        				},
        				fields: {
        					field : [{
        						_name: "NOMECONTATO"
        					},
        					{
        						_name: "ACESSOAPP"
        					}]
        				}
        			}
        	};
        	
        	_callService({
        		service: 'crud.find',
        		params: paramsPar,
        		timeout: 60000,
        		callback: {
        			success: function(data){
        				if(success != null){
        					success(data);
        				}
        			},
        			error: function(msg, status, headers, config){
        				$ionicPopup.alert({cssClass: 'popUpCred',
        					title: 'Oops!',
        					template: msg
        				});
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		}
        	});
        	
        }
        
        function _buscarParceiro(data, success, error, httpError){
        	
        	var paramsPar = {
        			dataSet : { 
        				_rootEntity: "Parceiro",
        				entity : [
        				          {
        				        	  field : [
												{
													_name : "CODPARC"
												},
												{
													_name : "NOMEPARC"
												},
												{
													_name : "IDENTINSCESTAD"
												},
												{
													_name : "CGC_CPF"
												},
												{
													_name : "DTNASC"
												},
												{
													_name : "DTCAD"
												},
        				        	  ]
        				          },        				          
        				          {
        				        	  _path:"ComplementoParc",
        				        	  field : {
        				        		  _name : "PAI"
        				        	  }
        				          }
        				          ],
        				          criteria : {
        				        	  expression : "( ( this.CGC_CPF = ? OR this.CODPARC = ? ) AND this.CLIENTE = 'S' )",
        				        	  parameter : [{
        				        		  _type : "S",
        				        		  __text : data.CARTAO_CGC_CPF 
        				        	  	},
        				        	  	{ _type : "S",
        				        	  	    __text : data.CARTAO_CGC_CPF 
    				        	  		}]
        				          }
        			}
        	
        	};
        	
        	_callService({
        		service: 'CRUDServiceProvider.loadRecords',
        		params: paramsPar,
        		timeout: 60000,
        		callback: {
        			success: function(data){
        				if(success != null){
        					success(data);
        				}
        			},
        			error: function(msg, status, headers, config){
        				$ionicPopup.alert({cssClass: 'popUpCred',
        					title: 'Oops!',
        					template: msg
        				});
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		}
        	});
        	
        }
        
        function _getSessionInfo(data, success, error, httpError){
        	
        	_callService({
        		service: 'SystemUtilsSP.getSessionInfo',
        		params: null,
        		timeout: 60000,
        		callback: {
        			success: function(data){
        				if(success != null){
        					success(data);
        				}
        			},
        			error: function(msg, status, headers, config){
        				$ionicPopup.alert({cssClass: 'popUpCred',
        					title: 'Oops!',
        					template: msg
        				});
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		}
        	});
        	
        }
        
        function _getTipoNegociacao(data, success, error, httpError){        	
        	
        	if(data.DATA == null ||  typeof(data.DATA) == "undefined" || data.DATA.length == 0){
    			
    			$ionicPopup.alert({cssClass: 'popUpCred',
                    title: 'Atenção!',
                    template: "É necessário informar uma data de vencimento"
                });
    			
    			return;
    		}

        	
                    
        	
        	var date = data.DATA.toISOString().substring(0, 10).split("-");
			var dataStr = date[2]+"/"+date[1]+"/"+date[0];


			
        	var paramsNeg = {
        			parceiro : {
        				_codParc: data.codParcB2B,
        				_dtPriVencimento: dataStr
        			}
    	    };
        	
        	_callService({
                service: 'mgefin@OperacoesCreditoSP.getTipoNegociacao',
                params: paramsNeg,
                timeout: 60000,
                callback: {
                    success: function(data){                    	
                    	
                        if(success != null){
                        	success(data);
                        }
                    },
                    error: function(msg, status, headers, config){
                        $ionicPopup.alert({cssClass: 'popUpCred',
                            title: 'Oops!',
                            template: msg
                        });
                        
                        if(error != null){
                        	error(msg, status, headers, config);
                        }
                    },
                    httpError: httpError
                }
            });
			
        }

         function _buscarAutorizacoesBordero(data, success, error, httpError){
            var hrInicialDate = new Date();
            
          /*  var autorizacoes =  {
                bordero : [
                {
                    CONTRATO : "23",
                    DESDOBRAMENTO : "1",
                    DTVENC: "20/06/2016",
                    NOME: "Charles Teles Ferreira da Fonseca",            
                    VALOR: "168.69"            
                },{
                    CONTRATO : "2352",
                    DESDOBRAMENTO : "2",
                    DTVENC: "21/06/2016",
                    NOME: "Tiao Teles Ferreira da Fonseca",            
                    VALOR: "168.69"            
                },{
                    CONTRATO : "233",
                    DESDOBRAMENTO : "3",
                    DTVENC: "22/06/2016",
                    NOME: "Joana Teles Ferreira da Fonseca",            
                    VALOR: "168.69"            
                },{
                    CONTRATO : "123",
                    DESDOBRAMENTO : "4",
                    DTVENC: "23/06/2016",
                    NOME: "Dark Teles Ferreira da Fonseca",            
                    VALOR: "168.69"            
                },{
                    CONTRATO : "2113",
                    DESDOBRAMENTO : "5",
                    DTVENC: "24/06/2016",
                    NOME: "Tito Teles Ferreira da Fonseca",            
                    VALOR: "168.69"            
                },{
                    CONTRATO : "2453",
                    DESDOBRAMENTO : "6",
                    DTVENC: "25/06/2016",
                    NOME: "Bastiao Teles Ferreira da Fonseca",            
                    VALOR: "168.69"            
                }    
                ],
                totalBordero: "1000,00"
            };
            

            success(autorizacoes);*/

            _callService({
                service: 'credparapp@CredParSP.buscarAutorizacoesBordero',
                params: data,
                timeout: 60000,
                callback: {
                    success: function(data){
                        if(success != null){
                            success(data);
                        }
                    },
                    error: function(msg, status, headers, config){
                        $ionicPopup.alert({cssClass: 'popUpCred',
                            title: 'Oops!',
                            template: msg
                        });
                        
                        if(error != null){
                            error(msg, status, headers, config);
                        }
                    },
                    httpError: httpError
                }
            });
        }

        function _buscarExtratoVendas(data, success, error, httpError){
            var hrInicialDate = new Date();
            
            /*var extrato =  {
                vendas : [
                {
                    CONTRATO : "23",
                    PLANO : "PLANO 01 30DD",
                    DTVENDA: "20/06/2016",
                    NOME: "Charles Teles Ferreira da Fonseca",            
                    VALOR: "168.69"            
                },{
                    CONTRATO : "2352",
                    PLANO : "PLANO 02 30DD",
                    DTVENDA: "21/06/2016",
                    NOME: "Tiao Teles Ferreira da Fonseca",            
                    VALOR: "168.69"            
                },{
                    CONTRATO : "233",
                    PLANO : "PLANO 03 30DD",
                    DTVENDA: "22/06/2016",
                    NOME: "Joana Teles Ferreira da Fonseca",            
                    VALOR: "168.69"            
                },{
                    CONTRATO : "123",
                    PLANO : "PLANO 04 30DD",
                    DTVENDA: "23/06/2016",
                    NOME: "Dark Teles Ferreira da Fonseca",            
                    VALOR: "168.69"            
                },{
                    CONTRATO : "2113",
                    PLANO : "PLANO 05 30DD",
                    DTVENDA: "24/06/2016",
                    NOME: "Tito Teles Ferreira da Fonseca",            
                    VALOR: "168.69"            
                },{
                    CONTRATO : "2453",
                    PLANO : "PLANO 06 30DD",
                    DTVENDA: "25/06/2016",
                    NOME: "Bastiao Teles Ferreira da Fonseca",            
                    VALOR: "168.69"            
                }    
                ],
                totalExtrato: "1000,00"
            };*/
            

//            success(extrato);

            _callService({
                service: 'credparapp@CredParSP.buscarExtratoVendas',
                params: data,
                timeout: 60000,
                callback: {
                    success: function(data){
                        if(success != null){
                            success(data);
                        }
                    },
                    error: function(msg, status, headers, config){
                        $ionicPopup.alert({cssClass: 'popUpCred',
                            title: 'Oops!',
                            template: msg
                        });
                        
                        if(error != null){
                            error(msg, status, headers, config);
                        }
                    },
                    httpError: httpError
                }
            });
        }
        
        
        function _gerarParcelas(data, success, error, httpError){
        	var hrInicialDate = new Date();
        	
            _callService({
                service: 'mgefin@OperacoesCreditoSP.gerarParcelas',
                params: data,
                timeout: 60000,
                callback: {
                    success: function(data){
                        if(success != null){
                        	success(data);
                        }
                    },
                    error: function(msg, status, headers, config){
                        $ionicPopup.alert({cssClass: 'popUpCred',
                            title: 'Oops!',
                            template: msg
                        });
                        
                        if(error != null){
                        	error(msg, status, headers, config);
                        }
                    },
                    httpError: httpError
                }
            });
        }

          function _alterarSenhaB2B(data, success, error, httpError){
                        
            _callService({
                service: 'mge@ParceiroSP.alteraSenhaContato',
                params: data,
                timeout: 60000,
                callback: {
                    success: function(data){
                        if(success != null){
                            success(data);
                        }
                    },
                    error: function(msg, status, headers, config){
                        $ionicPopup.alert({cssClass: 'popUpCred',
                            title: 'Oops!',
                            template: msg
                        });
                        
                        if(error != null){
                            error(msg, status, headers, config);
                        }
                    },
                    httpError: httpError
                }
            });
        }
        
        function _incluirAutorizacao(data, success, error, httpError){
        	
        	_callService({
        		service: 'mgefin@OperacoesCreditoSP.incluirAutorizacaoCredito',
        		params: data,
        		timeout: 60000,
        		callback: {
        			success: function(data){
        				if(success != null){
        					success(data);                            
        				}
        			},
        			error: function(msg, status, headers, config){
        				$ionicPopup.alert({cssClass: 'popUpCred',
        					title: 'Oops!',
        					template: msg
        				});
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		}
        	});
        }
        
        function _doLogin(data, success, error, httpError){
        	var deviceModel = $window.device ? $window.device.model : 'unknown_model';
        	var devicePlatform = $window.device ? $window.device.platform : 'unknown_platform';
        	var deviceID = $window.device ? $window.device.uuid : 'unknown_id';
        	
        	var aparelho = deviceModel + ';' + devicePlatform;
            var aparelhoID = deviceID;
            
            data.KEEPCONNECTED = true;
        	data.APPNAME = $credpar.getAppName();
        	data.APARELHO = aparelho;
        	data.APARELHO_ID = aparelhoID;
        	
        	$credpar.setStoreItem('codParcB2B', null);
            $credpar.setStoreItem('codContatoB2B', null);
        	$credpar.setStoreItem('codUsuB2B', null);          
        	$credpar.setStoreItem('nomeUsuB2B', null);
            $credpar.setStoreItem('internoOutput', null);                        

            var internoOutPut = "";
            
            if(data.INTERNO != null){
                internoOutPut = CryptoJS.MD5(data.INTERNO).toString();
            }

            
                    	
            _callService({
                service: 'MobileLoginSP.login',
                params: data,
                timeout: 60000,
                callback: {
                    success: function(data){
                        var userID = Base64.doDecode(data.idusu, false);
                        $credpar.setStoreItem('userCredID', userID);

                        $credpar.setStoreItem('internoOutput', internoOutPut);                        
                        
                        if(data.kID != null){
                        	var kCredID = Base64.doDecode(data.kID, false);
                            $credpar.setStoreItem('kCredID', kCredID);	
                        }

                        var mgeSession = data.jsessionid;
                        $credpar.setMgeSessionId(mgeSession);

                        if(success != null){
                        	success(data);
                        }
                    },
                    error: function(msg, status, headers, config){
                        $ionicPopup.alert({cssClass: 'popUpCred',
                            title: 'Oops!',
                            template: msg
                        });
                        
                        if(error != null){
                        	error(msg, status, headers, config);
                        }
                    },
                    httpError: httpError
                }
            });
        }
        
        function _doLoginCliente(data, success, error, httpError){
        	var deviceModel = $window.device ? $window.device.model : 'unknown_model';
        	var devicePlatform = $window.device ? $window.device.platform : 'unknown_platform';
        	var deviceID = $window.device ? $window.device.uuid : 'unknown_id';
        	
        	var aparelho = deviceModel + ';' + devicePlatform;
        	var aparelhoID = deviceID;
        	
        	data.KEEPCONNECTED = true;
        	data.APPNAME = $credpar.getAppName();
        	data.APARELHO = aparelho;
        	data.APARELHO_ID = aparelhoID;
        	data.ID = Base64.encode(data.ID);
        	data.INTERNO = CryptoJS.MD5(data.INTERNO).toString();
        	
        	_callService({
        		service: 'credparapp@CredParClienteSP.loginCliente',
        		params: data,
        		timeout: 60000,
        		callback: {
        			success: function(data){
        				var clienteID = Base64.doDecode(data.id, false);
        				var clienteName = Base64.doDecode(data.name, false);

        				$credpar.setStoreItem('userCredID', clienteID);
        				$credpar.setStoreItem('userName', clienteName);
        				
        				if(success != null){
        					success(data);
        				}
        			},
        			error: function(msg, status, headers, config){
        				$ionicPopup.alert({cssClass: 'popUpCred',
        					title: 'Oops!',
        					template: msg
        				});
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		}
        	});
        }
        
        function _versionValidation(okCallback){
        	if($credpar.getMinVersion() != null ){
        		var data = {minVersion: $credpar.getMinVersion()};
        		
        		var systemName = 'Sankhya-W';
        		
        		_callService({
                    service: 'MobileLoginSP.versionValidation',
                    params: data,
                    callback: {
                    	success: function(data){
                			if(data.version._ok == 'true'){
                				
                				var serverPlatform = data.version._serverPlatform;
                        		
                        		if(serverPlatform != null){
                        			
                        			if(serverPlatform != 'sankhya'){
                        				console.log('Incompatibilidade de plataformas.');
                        				
                        				$ionicPopup.alert({cssClass: 'popUpCred',
                                            title: 'Oops!',
                                            template: '<div>Este aplicativo se conecta apenas em servidores Sankhya-W. Por gentileza, verifique o endereço do servidor.</div>'
                                        });
                        				
                        				return;
                        			}
                        		}
                				
                				if(okCallback != null){
                					okCallback();
                				}
                			}else{
                				var popup = $ionicPopup.alert({cssClass: 'popUpCred',
                                    title: 'Oops!',
                                    template: '<div>Este aplicativo exige a versão <b>' + $credpar.getMinVersion() + '</b> ou superior do sistema. Por gentileza, atualize seu '+ systemName +'.</div>'
                                });
                				
                				popup.then(function(res) {
                					$credpar.redirectToLogin();
                				});
                			}
                    	},
                    	error: function(serverMsg, status, headers, config){
                    		var popup = $ionicPopup.alert({cssClass: 'popUpCred',
                                title: 'Oops!',
                                template: '<div>Não conseguimos validar a versão. Este aplicativo exige a versão <b>' + $credpar.getMinVersion() + '</b> ou superior do sistema. Por gentileza, atualize seu '+ systemName +'.</div>'
                            });
                			
                			popup.then(function(res) {
            					$credpar.redirectToLogin();
            				});
                    	}
                    }
                });	
        	}else{
        		$ionicPopup.alert({cssClass: 'popUpCred',
                    title: 'Oops!',
                    template: '<div>Não foi definido versão mínima para ser validada. Configure utilizando $credpar.config({minVersion: \'x.x.x\'})</div>'
                });
        	}
        }

        function _getInfoSaldo(success, error, httpError){
            
            _callService({
                service: 'credparapp@CredParClienteSP.getInfoSaldo',
                params: {ID: Base64.encode($credpar.getStoreItem('userCredID'))},
                timeout: 60000,
                callback: {
                    success: function(data){
                        if(success != null){
                            success(data);
                        }
                    },
                    error: function(msg, status, headers, config){
                        $ionicPopup.alert({cssClass: 'popUpCred',
                            title: 'Oops!',
                            template: msg
                        });
                        
                        if(error != null){
                            error(msg, status, headers, config);
                        }
                    },
                    httpError: httpError
                },
                ignoreLoadingBar: true
            });
        }

        function _getInfoExtrato(success, error, httpError){
        	
        	_callService({
        		service: 'credparapp@CredParClienteSP.getInfoExtrato',
        		params: {ID: Base64.encode($credpar.getStoreItem('userCredID'))},
        		timeout: 60000,
        		callback: {
        			success: function(data){
        				if(success != null){
        					success(data);
        				}
        			},
        			error: function(msg, status, headers, config){
        				$ionicPopup.alert({cssClass: 'popUpCred',
        					title: 'Oops!',
        					template: msg
        				});
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		},
        		ignoreLoadingBar: true
        	});
        }

        function _getInfoContato(success, error, httpError){
        	
        	_callService({
        		service: 'credparapp@CredParClienteSP.getInfoContato',
        		params: {ID: Base64.encode($credpar.getStoreItem('userCredID'))},
        		timeout: 60000,
        		callback: {
        			success: function(data){
        				if(success != null){
        					success(data);
        				}
        			},
        			error: function(msg, status, headers, config){
        				$ionicPopup.alert({cssClass: 'popUpCred',
        					title: 'Oops!',
        					template: msg
        				});
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		},
        		ignoreLoadingBar: true
        	});
        }

        function _enviarEmail(data, success, error, httpError){
        	
        	_callService({
        		service: 'credparapp@CredParClienteSP.enviarEmail',
        		params: data,
        		timeout: 60000,
        		callback: {
        			success: function(data){
        				if(success != null){
        					success(data);
        				}
        			},
        			error: function(msg, status, headers, config){
        				$ionicPopup.alert({cssClass: 'popUpCred',
        					title: 'Oops!',
        					template: msg
        				});
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		}
        	});
        }

        function _alterarSenha(data, success, error, httpError){
        	
        	data.ID = Base64.encode($credpar.getStoreItem('userCredID'));
        	
        	_callService({
        		service: 'credparapp@CredParClienteSP.alterarSenha',
        		params: data,
        		callback: {
        			success: function(data){
        				if(success != null){
        					success(data);
        				}
        			},
        			error: function(msg, status, headers, config){
        				$ionicPopup.alert({cssClass: 'popUpCred',
        					title: 'Oops!',
        					template: msg
        				});
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		}
        	});
        }

        function _esqueciMinhaSenha(data, success, error, httpError){
        	
        	_callService({
        		service: 'credparapp@CredParSP.esqueciMinhaSenha',
        		params: data,
        		callback: {
        			success: function(data){
        				if(success != null){
        					success(data);
        				}
        			},
        			error: function(msg, status, headers, config){
        				$ionicPopup.alert({cssClass: 'popUpCred',
        					title: 'Oops!',
        					template: msg
        				});
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		},
        		ignoreLoadingBar: true
        	});
        }

        function _esqueciMinhaSenhaCliente(data, success, error, httpError){
        	
        	_callService({
        		service: 'credparapp@CredParClienteSP.esqueciMinhaSenhaCliente',
        		params: data,
        		callback: { 
        			success: function(data){
        				if(success != null){
        					success(data);
        				}
        			},
        			error: function(msg, status, headers, config){
        				$ionicPopup.alert({cssClass: 'popUpCred',
        					title: 'Oops!',
        					template: msg
        				});
        				
        				if(error != null){
        					error(msg, status, headers, config);
        				}
        			},
        			httpError: httpError
        		},
        		ignoreLoadingBar: true
        	});
        }
        
        return {
            callService: 				_callService,
            logout: 					_logout,                        
            login: 						_login,            
            loginCliente:				_loginCliente,
            buscarCidade: 				_buscarCidade,            
            buscarEstado: 				_buscarEstado,
            buscarParceiroProspect: 	_buscarParceiroProspect,
            buscarServidores: 			_buscarServidores,
            buscarNomeLojista: 			_buscarNomeLojista,
            buscarParceiro: 			_buscarParceiro,
            salvarParceiroProspect: 	_salvarParceiroProspect,
            gerarParcelas: 				_gerarParcelas,
            getSessionInfo: 			_getSessionInfo,
            incluirAutorizacao: 		_incluirAutorizacao,
            redirectToLogin: 			_redirectToLogin,
            getTipoNegociacao: 			_getTipoNegociacao,
            versionValidation: 			_versionValidation,
            alterarSenhaB2B :  			_alterarSenhaB2B,
            buscarAutorizacoesBordero : _buscarAutorizacoesBordero,
            buscarExtratoVendas : 		_buscarExtratoVendas,
            getInfoSaldo:				_getInfoSaldo,
            getInfoExtrato:				_getInfoExtrato,
            getInfoContato:				_getInfoContato,
            enviarEmail:				_enviarEmail,
            alterarSenha:				_alterarSenha,
            esqueciMinhaSenha:			_esqueciMinhaSenha,
            esqueciMinhaSenhaCliente:	_esqueciMinhaSenhaCliente,
            buscaCidadesSegmentos:		_buscaCidadesSegmentos
        };
	   
}]);	   

