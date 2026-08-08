angular.module('credparapp.controllers',['ionic'])

.controller('MainCtrl', function($rootScope, $scope,$state, cfpLoadingBar, $ionicHistory, $interval, $timeout,$ionicPopup, $credparService,$window,$ionicPlatform,$credpar){

	var hardwareOptionMenu = function(){
		$rootScope.$broadcast('$android.optionMenu');
	};

	var hardwareBackButton = function(){
		$rootScope.$broadcast('$android.backButton');
	};
	
	document.addEventListener('menubutton', hardwareOptionMenu, false);
	
	$ionicPlatform.registerBackButtonAction(hardwareBackButton, 110);
	
	$rootScope.needRefreshTasks = false;
	$rootScope.showBackButton = false;
	$rootScope.showSideMenuButton = false;
	$rootScope.syncTasksThread = null;


	$rootScope.formatCpf = function cpf(cpf) {
	    cpf = cpf.replace( /\D/g , ""); //Remove tudo o que não é dígito
	    cpf = cpf.replace( /(\d{3})(\d)/ , "$1.$2"); //Coloca um ponto entre o terceiro e o quarto dígitos
	    cpf = cpf.replace( /(\d{3})(\d)/ , "$1.$2"); //Coloca um ponto entre o terceiro e o quarto dígitos
	    //de novo (para o segundo bloco de números)
	    cpf = cpf.replace( /(\d{3})(\d{1,2})$/ , "$1-$2"); //Coloca um hífen entre o terceiro e o quarto dígitos
	    return cpf;
	}

	$rootScope.goBack = function(){
		var homeId = "apploj.home";
		 
		if($credpar.getStoreItem("isCliente")){
			homeId = "cliente.home";
		}
		
		if($ionicHistory.currentView().stateId == "welcome.login-cliente" || $ionicHistory.currentView().stateId == "welcome.login-lojista"){
			$state.go('welcome.select-city');
			return;
		}

		if($ionicHistory.currentView().stateId == "welcome.select-city"){
			$state.go('welcome.credpar');
			return;
		}

		if($ionicHistory.currentView().stateId == "welcome.credpar"){
			navigator.app.exitApp();
			return;
		}

		if($ionicHistory.currentView().stateId != homeId){
			
	    	if($ionicHistory.backView() == null){
	    		$rootScope.goToHome();
	    	}else{
	    		$ionicHistory.goBack();
	    	}

    	} else {
    		navigator.app.exitApp();
    	}
     };

    $rootScope.goToHome = function(){
        $ionicHistory.nextViewOptions({
            historyRoot: true
        });

        if($credpar.getStoreItem("isCliente")){
			$state.go('cliente.home');
		} else {
			$state.go('apploj.home');
		}
    };
    
    $rootScope.$on('$tasksSync.finish', function(){
    	//$rootScope.needRefreshTasks = true;
    });
    
    $rootScope.$on('$ionicView.beforeLeave', function(){
//        cfpLoadingBar.complete();
    });
    
    $rootScope.$on('$ionicView.beforeEnter', function(){
//    	if($ionicHistory.currentView().stateId == 'app.home'){
//    		$rootScope.showBackButton = false;
//    		$rootScope.showSideMenuButton = true;
//    	}else{
//    		$rootScope.showBackButton = true;
//    		$rootScope.showSideMenuButton = false;
//    	}
    });
    
    $rootScope.$on('$android.backButton', function(){
    	$rootScope.goBack();
    });
    
    $rootScope.doLogout = function(){
		$credparService.logout();		
    };
    
    $window.addEventListener("resize", function(){
        $scope.windowHeight = Math.max(445, document.documentElement.clientHeight) + 'px';
        $scope.windowHeightKeyboard = document.documentElement.clientHeight;
        $scope.$apply();
    });

    $rootScope.windowHeight = Math.max(445, document.documentElement.clientHeight) + 'px';
    $scope.windowHeightKeyboard = document.documentElement.clientHeight;
    
})

.controller('AlterarSenhaCtrl', function($scope, $state,$credpar,$ionicPopup,$credparService) {

	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'block';
    });

	$scope.codParcB2B = $credpar.getStoreItem('codParcB2B');
	$scope.codContatoB2B = $credpar.getStoreItem('codContatoB2B');

	$scope.dadosSenha = {};

	$scope.alterarSenha = function(){
		var internoOutPut = $credpar.getStoreItem('internoOutput');
		var senhaAtualForm = $scope.dadosSenha.SENHAATUAL;

		if(senhaAtualForm == null){
			$ionicPopup.alert({cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>É necessário informar a senha atual</div>'
			});
					
			return true;
		}

		var senhaAtualFormMD5 = CryptoJS.MD5(senhaAtualForm).toString();
		var novaSenhaMD5 = CryptoJS.MD5($scope.dadosSenha.NOVASENHA).toString();


		if(senhaAtualFormMD5 != internoOutPut){
			$ionicPopup.alert({cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>Senha Atual não confere com a registrada no sistema</div>'
			});
					
			return true;
		}

		if($scope.dadosSenha.NOVASENHA != $scope.dadosSenha.CONFNOVASENHA){
			$ionicPopup.alert({cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>Os campos de Nova senha e Confirmação de senha estão divergentes</div>'
			});
					
			return true;
		}

		var params = {
			contato : {
				_parceiro: $scope.codParcB2B,
				_codContato: $scope.codContatoB2B,
				_senha : $scope.dadosSenha.NOVASENHA
			}
		}

		$credparService.alterarSenhaB2B (
				params, 
				function(data){
					$ionicPopup.alert({
						cssClass: 'popUpCred',
						title: 'Alteração de senha',
						template: 'Senha alterada com sucesso.'
					});

					$credpar.setStoreItem('internoOutput', novaSenhaMD5);
					
					$scope.dadosSenha.SENHAATUAL = null;
					$scope.dadosSenha.NOVASENHA = null;
					$scope.dadosSenha.CONFNOVASENHA = null;
				},
				function(msg, status, headers, config){
					$scope.loginRunning = false;
				},
				function(data, status, headers, config){
					$scope.loginRunning = false;
					
					$ionicPopup.alert({cssClass: 'popUpCred',
						title: 'Oops, Sem conexão',
						template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
					});
					
					return true;
				}
		);
		
	};

})
.controller('WelcomeHomeCtrl', function($rootScope, $scope, $state, $credpar, $state) {
	$scope.$on('$ionicView.beforeEnter', function(){
		$rootScope.showBack = false;
	});
	
    $scope.pageName = 'Home';
    $scope.appName = 'CredPar';

    $scope.startConfigCliente = function(){
		$credpar.setStoreItem('isCliente', true);
        $state.go('welcome.select-city');
    };
    
    $scope.startConfigLojista = function(){
    	$credpar.setStoreItem('isCliente', null);
    	$state.go('welcome.select-city');
    };
})

.controller('WelcomeLoginClienteCtrl', function($rootScope, $scope, $state, $credpar, $credparService, $ionicPopup) {
    $scope.pageName = 'Login Cliente';
    $scope.serverName = 'CredPar';
    $scope.loginRunning = false;
    $scope.isDefaultProviderConnection = false;
    $scope.account = {};
    $scope.account.userName = $credpar.getStoreItem('lastUser');
    $scope.account.password = null;

    $scope.$on('$ionicView.beforeEnter', function(){
    	$rootScope.showBack = true;

    	var serverConfig = $credpar.getServerConfig();
    	$scope.cityName = serverConfig.name;
    });
    
    $scope.esqueciMinhaSenha = function(){
    	if($scope.account.userName == null){
            $ionicPopup.alert({cssClass: 'popUpCred',
                title: 'Oops!',
                template: 'Informe o <b>CPF</b> antes.'
            });

            return;
        }
    	
    	var confirmPopup = $ionicPopup.confirm({cssClass: 'popUpCred',
    		title: 'Esqueci minha senha',
    		template: 'Será enviado uma nova senha para o seu email.\nDeseja continuar?',
    		cancelText: 'Cancelar',
    		okText: 'Enviar'
    	});

    	confirmPopup.then(function(res) {
    	     if(res) {
    	    	 $credparService.esqueciMinhaSenhaCliente(
    	        	{cpf: $scope.account.userName}, 
    	        	function(data){
    	                $scope.loginRunning = false;
    	                
    	                $ionicPopup.alert({cssClass: 'popUpCred',
    	                    title: 'E-mail enviado',
    	                    template: 'Foi enviado um e-mail com a nova senha.\nVerifique o seu email.'
    	                });
    	            },
    	            function(msg, status, headers, config){
    	                $scope.loginRunning = false;
    	            },
    	            function(data, status, headers, config){
    	                $scope.loginRunning = false;

    	                $ionicPopup.alert({cssClass: 'popUpCred',
    	                    title: 'Oops, Sem conexão',
    	                    template: 'Verifique se você está conectado a Internet.',
    	                    okText: 'Ok'
    	                });

    	                return true;
    	            }
    	        );
    	     }
    	});
    }
    
    $scope.login = function(){
    	
        if($scope.account.userName == null){
            $ionicPopup.alert({cssClass: 'popUpCred',
                title: 'Oops!',
                template: 'Informe o <b>CPF</b> para entrar.'
            });

            return;
        }

        $scope.loginRunning = true;
        $credpar.setStoreItem('lastUser', $scope.account.userName);
        
        var data = {
        	ID: $scope.account.userName, 
        	INTERNO: $scope.account.password
        };
        
        $credparService.loginCliente(
        	data, 
        	function(data){
                $scope.loginRunning = false;
                
                $credpar.redirectToApp();

                if(data.ID.equals(data.INTERNO)){
					$ionicPopup.alert({cssClass: 'popUpCred',
	                    title: 'Atenção',
	                    template: 'É necessário alterar sua senha, a mesma.',
	                    okText: 'Ok'
                 	}); 
                }
            },
            function(msg, status, headers, config){
                $scope.loginRunning = false;
            },
            function(data, status, headers, config){
                $scope.loginRunning = false;

                $ionicPopup.alert({cssClass: 'popUpCred',
                    title: 'Oops, Sem conexão',
                    template: 'Verifique se você está conectado a Internet.',
                    okText: 'Ok'
                });

                return true;
            }
        );
    };

})

.controller('AutorizacaoVendasParcCtrl', function($scope, $state,$credparService, $ionicPopup,$credpar,$rootScope) {
	var btnConfirmar = document.getElementById("btnConfirmar");

	btnConfirmar.disabled = true;

	$scope.autorizacaoParc = {};

	$scope.popUpOpen = false;

	$scope.cartao = {};					
	
	$scope.parceiros = [];
	
	$scope.parceiroSelecionado = {};
	
	$scope.autorizacaoParc.codParcB2B = $credpar.getStoreItem('codParcB2B');
	
	
	
	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'block';
    });

    $scope.changeValueAuto= function(){
    };
    
	
	$scope.selectParc = function($event, $parceiro){
    	$event.preventDefault();
    	$event.stopPropagation();
    	$scope.parceiroSelecionado = $parceiro;

    	if($scope.parceiroSelecionado.CGC_CPF != null){
    		$scope.parceiroSelecionado.CGC_CPF = $rootScope.formatCpf($scope.parceiroSelecionado.CGC_CPF);
    	} 

    	$scope.closePopUp();
	};
	
	$scope.closePopUp = function() {
		$scope.popUpOpen = false;
		$scope.myPopup.close();
	};
	
	$scope.tiposNeg = {};
	
	function validarDataAutorizacao(dataStr) {
		var parts = dataStr.split('/');
		var date = new Date(parts[2],parts[1]-1,parts[0]);
		
		var today = new Date();       
        today = new Date(today.getFullYear(),today.getMonth(),today.getDate());
		
		if (date < today) {
			$ionicPopup.alert({cssClass: 'popUpCred',
				title: 'Atenção',
				template: '<div>A data do primeiro vencimento não pode ser retroativa.</div>'
			});
			return false;
		}
		
		return true;
	}
	
	$scope.showPopUpVerificaSenha = function() {
		$rootScope.nomeParceiro = $scope.parceiroSelecionado.NOMEPARC;
		$rootScope.CGC_CPF =  $scope.parceiroSelecionado.CGC_CPF;
		
		$rootScope.senhaAutorizacao = '';
		
		$scope.myPopup = $ionicPopup.show({
			cssClass : 'popUpParceiro',
			templateUrl : 'popUp-valida-senha.html',
			scope: $scope
		});
		
		$scope.popUpOpen = true;
	}
	
	
	$scope.incluirAutorizacao = function(){
		var senha = $rootScope.senhaAutorizacao;
		var senhaCriptografada =  CryptoJS.MD5(senha).toString();
		
		if(!senha) {
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Atenção',
				template: 'Os campos de Nova senha e Confirmação de senha estão diferentes.'
			});
			
			return;
		}
		
		if($scope.totalParcelas == null || $scope.totalParcelas <= 0){
			$ionicPopup.alert({cssClass: 'popUpCred',
				title: 'Atenção',
				template: '<div>É necessário gerar as parcelas para incluir a autorização.</div>'
			});
			
			return;
		}
		
		if($scope.parceiroSelecionado == null){
			$ionicPopup.alert({cssClass: 'popUpCred',
                title: 'Atenção!',
                template: "É necessário selecionar um parceiro"
            });
			
			return;
		}
		
		var dtAtual = new Date();

		var date = $scope.autorizacaoParc.DATA.toISOString().substring(0, 10).split("-");
                    
        var dataStr = date[2]+"/"+date[1]+"/"+date[0];
		
        if (!validarDataAutorizacao(dataStr)) {
			return;
		}
        
		var params = {
        		autorizacaoCredito : {
        			_codLogista : $scope.autorizacaoParc.codParcB2B, 
        			_codCliente : $scope.parceiroSelecionado.CODPARC,
        			_valorTotal: $scope.totalParcelas,
        			_valorTotalBase : $scope.autorizacaoParc.VALORTOTAL, 
        			_codTipoNegociacao : $scope.tiposNeg.selection.CODTIPVENDA,
        			_dtPrimeiroVencimento: dataStr, 
        			_dtNegociacao : dataStr, 
        			//_nroCartao: $scope.cartao.NROCARTAO,
        			_modoB2B : "true", 
        			_isApp : "true", 
        			_isCartao : "false",
        			parcelas : { parcela : $scope.parcelaParam },
        			_senha: senhaCriptografada
        			        			
        		}        		
    	};
				
		$credparService.incluirAutorizacao (
				params, 
				function(data){
					var resposta = data.respostaAutorizacao;
					
					$scope.autorizacaoParc.nroAutorizacao = resposta["_numeroAutorizacao"];

					if($scope.autorizacaoParc.nroAutorizacao){
						var btnConfirmar = document.getElementById("btnConfirmar");
						
						btnConfirmar.disabled = true;

						$ionicPopup.alert({cssClass: 'popUpCred',
							title: 'Sucesso',
							template: '<div>Autorização realizada  com sucesso!  Número : '+$scope.autorizacaoParc.nroAutorizacao+'</div>'
						});	
					}
					
				},
				function(msg, status, headers, config){
					$scope.loginRunning = false;
				},
				function(data, status, headers, config){
					$scope.loginRunning = false;
					
					$ionicPopup.alert({
						cssClass: 'popUpCred',
						title: 'Oops, Sem conexão',
						template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
					});
					
					return true;
				}
		);
	};
	
	$scope.gerarParcelas = function(){
		
		
		if($scope.autorizacaoParc.DATA == null ||  typeof($scope.autorizacaoParc.DATA) == "undefined" || $scope.autorizacaoParc.DATA.length == 0){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
                title: 'Atenção!',
                template: "É necessário informar uma data de vencimento"                
            });
			
			return;
		}
		
		if($scope.parceiroSelecionado == null || $scope.parceiroSelecionado.CODPARC == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
                title: 'Atenção!',
                template: "É necessário selecionar um parceiro"
            });
			
			return;
		}
		
		if($scope.tiposNeg.selection == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
                title: 'Atenção!',
                template: "É necessário selecionar o tipo de negociação"
            });
			
			return;
		}
		
		var date = $scope.autorizacaoParc.DATA.toISOString().substring(0, 10).split("-");
                    
        var dataStr = date[2]+"/"+date[1]+"/"+date[0];
        
        if (!validarDataAutorizacao(dataStr)) {
			return;
		}
		
		var paramsPar = {
    		dadosParcelamento : { 
	        		_valorTotal: $scope.autorizacaoParc.VALORTOTAL,
	        		_dtVencimento: dataStr, 
	        		_dataNegociacao: dataStr, 
	        		_isCartao: "false",
	        		_codParc: $scope.parceiroSelecionado.CODPARC,							        		
	        		tipoNegociacao : {
	        			_codTipoNegociacao : $scope.tiposNeg.selection.CODTIPVENDA,
	        			_dhAlterNegociacao: $scope.tiposNeg.selection.DHALTER
	        		}
			}
    		
	    };
		
		 $credparService.gerarParcelas(
			paramsPar, 
        	function(data){
        		var btnConfirmar = document.getElementById("btnConfirmar");

				$scope.autorizacaoParc.nroAutorizacao = null; 
				
        		$scope.parcelas = [];
        		$scope.parcelaParam = [];
        		$scope.totalParcelas = Number(0);
        		
        		if (data.parcelas.parcela && Object.prototype.toString.call(data.parcelas.parcela) !== '[object Array]') {
        			data.parcelas.parcela = [data.parcelas.parcela];
        		}
        		
    			data.parcelas.parcela.forEach(function(entry) {
    				$scope.totalParcelas += Number(entry.VALORPARCELA.replace(".","").replace(",","."));        				
    				
    				var parcela = {};
    				parcela.DTVENCIMENTO = entry.DTVENCIMENTO;
    				parcela.NROPARCELA = entry.NROPARCELA;
    				parcela.VALORPARCELA = entry.VALORPARCELA;
    				
    				$scope.parcelas.push(entry);
    				$scope.parcelaParam.push(parcela);
        		});

            	if($scope.totalParcelas > 0){
					btnConfirmar.disabled = false;
            	}else{
            		btnConfirmar.disabled = true;
            	}
        		
            },
            function(msg, status, headers, config){
                $scope.loginRunning = false;                
            },
            function(data, status, headers, config){
                $scope.loginRunning = false;

                $ionicPopup.alert({
                	cssClass: 'popUpCred',
                    title: 'Oops, Sem conexão',
                    template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
                });

                return true;
            }
        );
	};
	
	$scope.buscarNegociacao = function(){
		$scope.tiposNeg = {};
		
		$credparService.getTipoNegociacao(
				$scope.autorizacaoParc, 
				function(data){					
					
					$scope.tiposNeg = {};
					
					var records = [];
					
					if(Object.prototype.toString.call(data.negociacoes.negociacao) === '[object Array]'){
						for(var i in data.negociacoes.negociacao){
	        				var tipoNegociacao = data.negociacoes.negociacao[i];
	        				
	        				tipoNegociacao.selection = false;
	        				
	    					records.push(tipoNegociacao);
	        			}


						
					}else{
						var tipoNegociacao = data.negociacoes.negociacao;
						
						
						if(tipoNegociacao != null){
	    					records.push(tipoNegociacao);
						}
					}

					if(records.length >= 10){ //O componente tem um problema para exibir a lista quando tem mais de 10 itens, logo é necessário incluir 1 a mais para o decimo aparecer.
						var tipoNegociacao = {PREENCHE : 'S'};

						records.push(tipoNegociacao);
					}
				
					$scope.tiposNeg.options = records;
					
					
				},
				function(msg, status, headers, config){
					$scope.loginRunning = false;					
				},
				
				function(data, status, headers, config){
					$scope.loginRunning = false;
					
					$ionicPopup.alert({
						cssClass: 'popUpCred',
						title: 'Oops, Sem conexão',
						template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
					});
					
					return true;
				}
		);
	};
	
	$scope.buscarParceiros = function(){
		
    	$scope.parcelas = null;
		
		$credparService.buscarParceiro(
				$scope.autorizacaoParc, 
				function(data){
					$scope.parceiros = [];
					
					var fields = data.entities.metadata.fields.field;
										
					var records = [];
					
					if(Object.prototype.toString.call(data.entities.entity) === '[object Array]'){
						for(var entity in data.entities.entity){
	        				var entityRecord = data.entities.entity[entity];
	        					        				
	        				var record = {};
	        				
	    					for(var field in fields){
	    						var fieldId = "f"+field;
	    						var fieldName = fields[field]._name;
	    						
	    						record[fieldName] = entityRecord[fieldId];
	    					}
	    					
	    					records.push(record);
	        			}
						
					}else{
						var entityRecord = data.entities.entity;
						
						if(entityRecord != null){
								        				
	        				var record = {};
	        				
	    					for(var field in fields){
	    						var fieldId = "f"+field;
	    						var fieldName = fields[field]._name;
	    						
	    						record[fieldName] = entityRecord[fieldId];
	    					}
	    					
	    					records.push(record);
						}
					}
				
					$scope.parceiros = records;

					if($scope.parceiros.length == 0){
						$ionicPopup.alert({
							cssClass: 'popUpCred',
							title: 'Atenção',
							template: '<div>Não existe cliente com o filtro informado.</div>'
						});
					} else {

						if($scope.popUpOpen == false){
						
							$scope.myPopup = $ionicPopup.show({
								cssClass: 'popUpParceiro',
								templateUrl : 'popUp-Parceiros.html',
								scope: $scope					    
							});

							$scope.popUpOpen = true;
						}
					}
				},
				
				function(msg, status, headers, config){
					$scope.loginRunning = false;					
				},
				
				function(data, status, headers, config){
					$scope.loginRunning = false;
					
					$ionicPopup.alert({
						cssClass: 'popUpCred',
						title: 'Oops, Sem conexão',
						template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
					});
					
					return true;
				}
		);
	};
})

.controller('ConsultaPropostaCtrl', function($rootScope,$scope, $state,$credpar,$credparService, $ionicPopup) {
	
	$scope.consultaProposta = {};
	
	$scope.codParcB2B = $credpar.getStoreItem('codParcB2B');
	
	$scope.loginRunning = false;
	
	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'block';
    });
	
	$scope.buscarParceiroProspect = function(){
		if($scope.consultaProposta.CGC_CPF == null){
			
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Atenção',
				template: '<div>É necessário informar o número do cartão ou CPF.</div>'
			});
			
			return;
		}
		
		var data = {
				codParcB2B: $scope.codParcB2B,
				CGC_CPF: $scope.consultaProposta.CGC_CPF
        };
		
		$credparService.buscarParceiroProspect (
				data, 
				function(data){
					$scope.records = [];
					$scope.parProsList = [];
					
					if(Object.prototype.toString.call(data.entities.entity) === '[object Array]'){
						
						var fields = data.entities.metadata.fields.field;
												
						
						
						for(var entity in data.entities.entity){
	        				var entityRecord = data.entities.entity[entity];
	        				
	        				var record = {};
	        				
	    					for(var field in fields){
	    						var fieldId = "f"+field;
	    						var fieldName = fields[field]._name;
	    						
	    						record[fieldName] = entityRecord[fieldId];
	    						
	    						if(fieldName == "STATUSPROPOSTA"){
	    							record["STATUSPROPOSTASTR"] = $scope.buscarStatusPropStr(entityRecord[fieldId]);
	    						}else if(fieldName == "DTCAD"){
	    							record["DTCAD"] =  entityRecord[fieldId].substring(0, 10); 
	    						}
	    					}
	    					
	    					$scope.records.push(record);
	        			}
						
					}else{
						var entityRecord = data.entities.entity;
        				
        				var record = {};
        				
        				if(entityRecord != null){
        					var fields = data.entities.metadata.fields.field;
        					
	    					for(var field in fields){
	    						var fieldId = "f"+field;
	    						var fieldName = fields[field]._name;
	    						
	    						record[fieldName] = entityRecord[fieldId];
	    						
	    						if(fieldName == "STATUSPROPOSTA"){
	    							record["STATUSPROPOSTASTR"] = $scope.buscarStatusPropStr(entityRecord[fieldId]);
	    						}else if(fieldName == "DTCAD"){
	    							record["DTCAD"] =  entityRecord[fieldId].substring(0, 10); 
	    						}
	    					}
	    					
	    					$scope.records.push(record);
        				}else{
        					$ionicPopup.alert({
								cssClass: 'popUpCred',
								title: 'Oops',
								template: '<div>Não conseguimos encontrar nenhuma proposta com os dados informados</div>'
							});		
        				}
    					
					}
					
					$scope.parProsList = $scope.records;
				},
				function(msg, status, headers, config){
					$scope.loginRunning = false;					
				},
				function(data, status, headers, config){
					$scope.loginRunning = false;
					
					$ionicPopup.alert({
						cssClass: 'popUpCred',
						title: 'Oops, Sem conexão',
						template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
					});
					
					return true;
				}
		);
	};
	
	$scope.buscarStatusPropStr = function(status){
		var strStatus = "";
		
		if(status == "N"){
			strStatus = "EM ANALISE";
		}else if(status == "T"){
			strStatus = "PENDENTE";
		}else if(status == "I"){
			strStatus = "REPROVADA";
		}else if(status == "D"){
			strStatus = "APROVADA";
		}
		return strStatus;
	};
})

.controller('NovaPropostaCtrl', function($rootScope,$scope, $state,$credpar,$credparService, $ionicPopup) {
	
	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'block';
    });
	
	$scope.estadoCivil = {
			options : [{
				DESCRICAO : "Casado(a)",
				VALUE: "C"			
			},{
				DESCRICAO : "Divorciado(a)",
				VALUE: "D"
			},{
				DESCRICAO : "Solteiro(a)",
				VALUE: "O"
			},{
				DESCRICAO: "Separado(a) Judicialmente",
				VALUE: "S"
			}		 
			]
	};
	
	$scope.sexo = {
			options : [{
				DESCRICAO : "Feminino",
				VALUE: "F"			
			},{
				DESCRICAO : "Masculino",
				VALUE: "M"
			}		 
			]
	};

	$scope.escolaridade = {
			options : [{
				DESCRICAO : "Ensino Fundamental",
				VALUE: "F"			
			},{
				DESCRICAO : "Ensino Médio",
				VALUE: "M"
			},{
				DESCRICAO : "Superior",
				VALUE: "S"
			},{
				DESCRICAO : "Mestrado",
				VALUE: "E"
			},{
				DESCRICAO : "Doutorado",
				VALUE: "D"
			},{
				DESCRICAO : "Analfabeto",
				VALUE: "A"
			}		 
			]
	};

	$scope.possuicartao = {
			options : [{
				DESCRICAO : "Sim",
				VALUE: "S"			
			},{
				DESCRICAO : "Não",
				VALUE: "N"
			}		 
			]
	};

	$scope.diaPagamento = {
			options : [{
				DESCRICAO : "5",
				VALUE: "5"			
			},{
				DESCRICAO : "10",
				VALUE: "10"
			},{
				DESCRICAO : "15",
				VALUE: "15"
			},{
				DESCRICAO : "25",
				VALUE: "25"
			}		 		 
			]
	};

	$scope.tipoResidencia = {
			options : [{
				DESCRICAO : "Própria",
				VALUE: "P"
			},{
				DESCRICAO : "Alugada",
				VALUE: "A"
			},{
				DESCRICAO : "Financiada",
				VALUE: "F"
			},{
				DESCRICAO : "Outros sem despesa",
				VALUE: "S"
			},{
				DESCRICAO : "Outros com despesa",
				VALUE: "O"
			}		 		 
			]
	};
	
	$scope.novaProposta = {};
	
	$scope.codParcB2B = $credpar.getStoreItem('codParcB2B');

	$scope.clearSelections = function(){

		if($scope.estadoCivil != null && $scope.estadoCivil.selection != null){
			$scope.estadoCivil.selection = null;
		}
		
		if($scope.sexo != null && $scope.sexo.selection != null) {
			$scope.sexo.selection = null;
		}

		if($scope.cidades != null && $scope.cidades.selection != null){
			$scope.cidades.selection = null;
		}
		
		if($scope.estados != null && $scope.estados.selection != null){
			$scope.estados.selection = null;
		}

		if($scope.estadosRG != null && $scope.estadosRG.selection != null){
			$scope.estadosRG.selection = null;
		}

		if($scope.escolaridade != null && $scope.escolaridade.selection != null){
			$scope.escolaridade.selection = null;
		}
		
		if($scope.possuicartao != null && $scope.possuicartao.selection != null){
			$scope.possuicartao.selection = null;
		}
		
		if($scope.tipoResidencia != null && $scope.tipoResidencia.selection != null){
			$scope.tipoResidencia.selection = null;
		}
		
		if($scope.diaPagamento != null && $scope.diaPagamento.selection != null){
			$scope.diaPagamento.selection = null;
		}
	};

	$scope.salvarParceiroProspect = function(){

		$scope.novaProposta.CODPARCB2B = $credpar.getStoreItem('codParcB2B');

		if($credpar.emptyAsNull($scope.novaProposta.NOMEPAP) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>É necessário informar o campo "NOME"</div>'
			});
			return;
		}

		if($credpar.emptyAsNull($scope.novaProposta.NOMECARTAO) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>É necessário informar o campo "NOME CARTÃO"</div>'
			});
			return;
		}

		if($scope.diaPagamento == null || $scope.diaPagamento.selection == null ){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>É necessário informar o campo "DIA DE PAGAMENTO"</div>'
			});
			return;
		}

		if($credpar.emptyAsNull($scope.novaProposta.NOMEMAE) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>É necessário informar o campo "MÃE"</div>'
			});
			return;
		}

		if($credpar.emptyAsNull($scope.novaProposta.CGC_CPF) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>É necessário informar o campo "CPF"</div>'
			});
			return;
		}
		
		if($credpar.emptyAsNull($scope.novaProposta.RG) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>É necessário informar o campo "IDENTIDADE"</div>'
			});
			return;
		}
		
		if($credpar.emptyAsNull($scope.novaProposta.TELCELULAR) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>É necessário informar o campo "TELEFONE CELULAR"</div>'
			});
			return;
		}
		
		if($credpar.emptyAsNull($scope.novaProposta.NOMECID) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>É necessário informar o campo "CIDADE"</div>'
			});
			return;
		}
		
		if($credpar.emptyAsNull($scope.novaProposta.AD_LOCALTRABALHO) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>É necessário informar o campo "LOCAL DE TRABALHO"</div>'
			});
			return;
		}

		if($scope.novaProposta.DTNASC == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>É necessário informar o campo "DATA DE NASCIMENTO"</div>'
			});
			return;
		}
		
		if($credpar.emptyAsNull($scope.novaProposta.AD_REFPESSOAL) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>É necessário informar o campo "REFERENCIA PESSOAL 1"</div>'
			});
			return;
		}
		
		if($credpar.emptyAsNull($scope.novaProposta.AD_TELPESSOAL1) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>É necessário informar o campo "TELEFONE REF PESSOAL 1"</div>'
			});
			return;
		}
		
		if($credpar.emptyAsNull($scope.novaProposta.AD_REFPESSOAL2) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>É necessário informar o campo "REFERENCIA PESSOAL 2"</div>'
			});
			return;
		}
		
		if($credpar.emptyAsNull($scope.novaProposta.AD_TELREF2) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Erro',
				template: '<div>É necessário informar o campo "TELEFONE REF PESSOAL 2"</div>'
			});
			return;
		}
		
		if($scope.diaPagamento != null && $scope.diaPagamento.selection != null){
			$scope.novaProposta.DIAPAGTO = $scope.diaPagamento.selection.VALUE;
		}

		if($scope.estadoCivil != null && $scope.estadoCivil.selection != null){
			$scope.novaProposta.ESTADOCIVIL = $scope.estadoCivil.selection.VALUE;
		}
		
		if($scope.sexo != null && $scope.sexo.selection != null) {
			$scope.novaProposta.SEXO = $scope.sexo.selection.VALUE;	
		}

		if($scope.cidades != null && $scope.cidades.selection != null){
			$scope.novaProposta.NATURALIDADE = $scope.cidades.selection.CODCID;
		}
		
		if($scope.estados != null && $scope.estados.selection != null){
			$scope.novaProposta.CODUF = $scope.estados.selection.CODUF;
		}

		if($scope.estadosRG != null && $scope.estadosRG.selection != null){
			$scope.novaProposta.UFRG = $scope.estadosRG.selection.CODUF;
		}

		if($scope.escolaridade != null && $scope.escolaridade.selection != null) {
			$scope.novaProposta.ESCOLARIDADE = $scope.escolaridade.selection.VALUE;	
		}

		if($scope.possuicartao != null && $scope.possuicartao.selection != null) {
			$scope.novaProposta.POSSUICARTAOCREDITO = $scope.possuicartao.selection.VALUE;	
		}

		if($scope.tipoResidencia != null && $scope.tipoResidencia.selection != null) {
			$scope.novaProposta.TIPORESIDENCIA = $scope.tipoResidencia.selection.VALUE;	
		}
		
		$credparService.salvarParceiroProspect (
				$scope.novaProposta, 
				function(data){
					$ionicPopup.alert({
						cssClass: 'popUpCred',
						title: 'Sucesso',
						template: '<div>Proposta incluida com sucesso.</div>'
					});

					$scope.novaProposta = {};
					$scope.clearSelections();
				},
				function(msg, status, headers, config){
				},
				function(data, status, headers, config){
					
					$ionicPopup.alert({
						cssClass: 'popUpCred',
						title: 'Oops, Sem conexão',
						template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
					});
					
					return true;
				}
		);
		
	}
	
	$scope.buscarCidades = function(){
		$scope.cidades = {};
		
		$credparService.buscarCidade(
				null, 
				function(data){					
					
					var records = [];
					
					if(Object.prototype.toString.call(data.result.row) === '[object Array]'){
						for(var i in data.result.row){
	        				var cidade = data.result.row[i];
	        				
	        				cidade.CODCID = data.result.row[i]["CODCID"];
	        						        					        			
	    					records.push(cidade);
	        			}
						
					}else{
						var cidade = data.result.row;
												
        				
						if(cidade != null){
	    					records.push(cidade);
						}
					}
				
					$scope.cidades.options = records;
				},
				function(msg, status, headers, config){
					$scope.loginRunning = false;					
				},
				
				function(data, status, headers, config){
					$scope.loginRunning = false;
					
					$ionicPopup.alert({
						cssClass: 'popUpCred',
						title: 'Oops, Sem conexão',
						template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
					});
					
					return true;
				}
		);
	};
	
	$scope.buscarEstados = function(){
		$scope.estados = {};
		
		$credparService.buscarEstado(
				null, 
				function(data){					
					
					$scope.tiposNeg = {};
					
					var records = [];
					
					if(Object.prototype.toString.call(data.result.row) === '[object Array]'){
						for(var i in data.result.row){
							var estado = data.result.row[i];
							
							estado.CODUF = data.result.row[i]["CODUF"];														
							
							records.push(estado);
						}
						
					}else{
						var estado = data.result.row;
											
						
						if(estado != null){
							records.push(estado);
						}
					}
					
					$scope.estados.options = records;
				},
				function(msg, status, headers, config){
					$scope.loginRunning = false;					
				},
				
				function(data, status, headers, config){
					$scope.loginRunning = false;
					
					$ionicPopup.alert({cssClass: 'popUpCred',
						title: 'Oops, Sem conexão',
						template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
					});
					
					return true;
				}
		);
	};

	$scope.buscarEstadosRG = function(){
		$scope.estadosRG = {};
		
		$credparService.buscarEstado(
				null, 
				function(data){					
					var records = [];
					
					if(Object.prototype.toString.call(data.result.row) === '[object Array]'){
						for(var i in data.result.row){
							var estado = data.result.row[i];
							
							estado.CODUF = data.result.row[i]["CODUF"];														
							
							records.push(estado);
						}
						
					}else{
						var estado = data.result.row;
						
						if(estado != null){
							records.push(estado);
						}
					}
					
					$scope.estadosRG.options = records;
				},
				function(msg, status, headers, config){
				},
				
				function(data, status, headers, config){
					$ionicPopup.alert({cssClass: 'popUpCred',
						title: 'Oops, Sem conexão',
						template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
					});
					
					return true;
				}
		);
	};
	
})

.controller('PropostaAdesaoCtrl', function($scope, $state,$credparService, $ionicPopup) {
	
	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'block';
    });
	
	$scope.goToConsultaProposta = function(){
		$state.go('apploj.consulta-proposta');
	};
	
	$scope.goToNovaProposta = function(){
		$state.go('apploj.nova-proposta');
	};
	
})

.controller('BorderoPagamentoCtrl', function($scope, $state,$credparService, $ionicPopup,$rootScope,$credpar) {
	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'block';
    });

    $scope.bordero = {};
	
	$scope.getAutorizacoesBordero = function($event){
    	$event.preventDefault();
    	$event.stopPropagation()

    	$scope.dtIniBordero = null;
		$scope.dtFinBordero = null;

    	if($scope.bordero.DTINI != null){    		
			$scope.dtIniBordero = $scope.bordero.DTINI.getTime();
    	}else{
		    $ionicPopup.alert({
		    	cssClass: 'popUpCred',
                title: 'Erro',
                template: '<div>É necessário informar a data inicial.</div>'
            });

            return;
    	}

    	if($scope.bordero.DTFIN != null){
			$scope.dtFinBordero = $scope.bordero.DTFIN.getTime();
    	}else{
    		$ionicPopup.alert({
    			cssClass: 'popUpCred',
                title: 'Erro',
                template: '<div>É necessário informar a data final.</div>'
            });

            return;
    	}    	

		$scope.buscarAutorizacoes();
    	
	};

	$scope.buscarAutorizacoes = function($event){
		$scope.autorizacoes = null;
		$rootScope.autorizacoes = null;
		$rootScope.totalBordero = null;
		
    	var codParcB2B = $credpar.getStoreItem("codParcB2B");

	    var param = {
			params : {
				_CODPARC : codParcB2B,
				_DTNEGINI : $scope.dtIniBordero,
				_DTNEGFIN: $scope.dtFinBordero
			}
		}
	
		$credparService.buscarAutorizacoesBordero(
			param, 
	    	function(data){

	    		if(data.extrato != null && data.extrato.bordero){
	    			if((Object.prototype.toString.call(data.extrato.bordero) === '[object Array]')){
	    				$rootScope.autorizacoes = data.extrato.bordero;	
	    			}else{
	    				$rootScope.autorizacoes = [];
	    				$rootScope.autorizacoes.push(data.extrato.bordero);
	    			}
	    			
	    			$rootScope.totalBordero = data.extrato.totalBordero;
	    		}

	    		$state.go('apploj.extrato-bordero-pagamento');
			},
			function(msg, status, headers, config){
	            $scope.loginRunning = false;	            
	        },
	        function(data, status, headers, config){
	            $scope.loginRunning = false;

	            $ionicPopup.alert({
	            	cssClass: 'popUpCred',
	                title: 'Oops, Sem conexão',
	                template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
	            });

	            return true;
	        }
								
		);
	};
	
})

.controller('ExtratoBorderoPagamentoCtrl', function($scope, $state,$credparService, $ionicPopup,$rootScope,$credpar) {
	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'block';		

		$scope.autorizacoes = $rootScope.autorizacoes;
    });
	
})

.controller('AutorizacoesVendasPagamentoCtrl', function($credpar,$rootScope,$scope, $state,$credparService, $ionicPopup) {
	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'block';		

		$scope.extratos = $rootScope.extratos;			
    });
	
})

.controller('ExtratoVendasPagamentoCtrl', function($rootScope,$scope, $state,$credparService, $ionicPopup,$credpar) {
	$scope.extratoVendas = {};

	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'block';
		
    });
	
	$scope.getAutorizacoesExtrato = function($event){
		$event.preventDefault();
    	$event.stopPropagation()
    	

		$rootScope.dtIniExtrato = null;
		$rootScope.dtFimExtrato = null;

    	if($scope.extratoVendas.DTINI != null){    		
			$rootScope.dtIniExtrato = $scope.extratoVendas.DTINI.getTime();
    	}else{
		    $ionicPopup.alert({
		    	cssClass: 'popUpCred',
                title: 'Erro',
                template: '<div>É necessário informar a data inicial.</div>'
            });

            return;
    	}

    	if($scope.extratoVendas.DTFIM != null){
			$rootScope.dtFimExtrato = $scope.extratoVendas.DTFIM.getTime();
    	}else{
    		$ionicPopup.alert({
    			cssClass: 'popUpCred',
                title: 'Erro',
                template: '<div>É necessário informar a data final.</div>'
            });

            return;
    	}    	

		$scope.buscarExtrato();    	
    	
	};


	$scope.buscarExtrato = function($event){
		$rootScope.extratos = null;
		$rootScope.totalExtrato = null;
		
		var codParcB2B = $credpar.getStoreItem("codParcB2B");

		var param = {
			params : {
				_CODPARC : codParcB2B,
				_DTNEGINI : $rootScope.dtIniExtrato,
				_DTNEGFIM: $rootScope.dtFimExtrato
			}
		}

		$credparService.buscarExtratoVendas(
			param, 
	    	function(data){

	    		if(data.extrato != null && data.extrato.vendas != null){
		    		if((Object.prototype.toString.call(data.extrato.vendas) === '[object Array]')){	    				
	    				$rootScope.extratos = data.extrato.vendas;	
		    		}else{
	    				$rootScope.extratos = [];
	    				$rootScope.extratos.push(data.extrato.vendas);
		    		}
				}
	    		
	    		$rootScope.totalExtrato = data.extrato && data.extrato.totalExtrato;
	    		
				$state.go('apploj.autorizacoes-extrato-vendas');
			},
			function(msg, status, headers, config){
	            $scope.loginRunning = false;	            
	        },
	        function(data, status, headers, config){
	            $scope.loginRunning = false;

	            $ionicPopup.alert({cssClass: 'popUpCred',
	                title: 'Oops, Sem conexão',
	                template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
	            });

	            return true;
	        }
								
		);
	};

})

.controller('WelcomeLoginCtrl', function($rootScope, $scope, $state, $credpar, $credparService, $ionicPopup, $window, Base64) {
    $scope.pageName = 'Login';
    $scope.serverName = 'CredPar';
    $scope.loginRunning = false;
    $scope.isDefaultProviderConnection = false;
    $scope.account = {};
    $scope.account.userName = $credpar.getStoreItem('lastUserCli');
    $scope.account.password = null;
    
    $scope.$on('$ionicView.beforeEnter', function(){
    	$rootScope.showBack = true;
    	
    	var serverConfig = $credpar.getServerConfig();
    	$scope.cityName = serverConfig.name;
    	
    	if($credpar.hasDefaultProvider()){
    		if($credpar.isDefaultProviderConnection()){
    			$scope.isDefaultProviderConnection = true;
    		}else{
    			$scope.isDefaultProviderConnection = false;
    		}
    	}
    });
    
    $scope.login = function(){
    	
        if($scope.account.userName == null){
            $ionicPopup.alert({
            	cssClass: 'popUpCred',
                title: 'Oops!',
                template: 'Informe o <b>Usuário</b> para entrar.'
            });

            return;
        }

        $scope.loginRunning = true;
        $credpar.setStoreItem('lastUserCli', $scope.account.userName);
        
        var data = {
        	NOMUSU: $scope.account.userName, 
        	INTERNO: $scope.account.password
        };
        
        $credparService.login(
        	data, 
        	function(data){
                $scope.loginRunning = false;
                
                $credpar.redirectToApp();
            },
            function(msg, status, headers, config){
                $scope.loginRunning = false;
            },
            function(data, status, headers, config){
                $scope.loginRunning = false;

                $ionicPopup.alert({
                	cssClass: 'popUpCred',
                    title: 'Oops, Sem conexão',
                    template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
                });

                return true;
            }
        );
    };
    
    $scope.esqueciMinhaSenha = function(){
    	if($scope.account.userName == null){
            $ionicPopup.alert({cssClass: 'popUpCred',
                title: 'Oops!',
                template: 'Informe o <b>Usuário</b> antes.'
            });

            return;
        }
    	
    	var confirmPopup = $ionicPopup.confirm({cssClass: 'popUpCred',
    		title: 'Esqueci minha senha',
    		template: 'Será enviado uma nova senha para o seu email.\nDeseja continuar?',
    		cancelText: 'Cancelar',
    		okText: 'Enviar'
    	});

    	confirmPopup.then(function(res) {
    	     if(res) {
    	    	 $credparService.esqueciMinhaSenha(
    	        	{usuario: $scope.account.userName}, 
    	        	function(data){
    	                
    	                $ionicPopup.alert({cssClass: 'popUpCred',
    	                    title: 'E-mail enviado',
    	                    template: 'Foi enviado um e-mail com a nova senha.\nVerifique o seu email.'
    	                });
    	            },
    	            function(msg, status, headers, config){
    	            },
    	            function(data, status, headers, config){

    	                $ionicPopup.alert({cssClass: 'popUpCred',
    	                    title: 'Oops, Sem conexão',
    	                    template: 'Verifique se você está conectado a Internet.',
    	                    okText: 'Ok'
    	                });

    	                return true;
    	            }
    	        );
    	     }
    	});
    };

})

.controller('SelectCityCtrl', function($rootScope, $scope, $state, $timeout, $ionicScrollDelegate, $credpar, $credparService, $ionicPopup) {
	$scope.$on('$ionicView.beforeEnter', function(){
		$rootScope.showBack = true;
	});
	
	/*$scope.fieldServer = {
		options : [{
			NOMESERV : "Uberlândia",
			IPSERV: "192.168.1.215",
			PORTSERV: "8512"			
		},{
			NOMESERV : "Araxa",
			IPSERV: "192.168.1.215",
			PORTSERV: "8513"
		},{
			NOMESERV : "Uberaba",
			IPSERV: "192.168.0.12",
			PORTSERV: "8080"
		},{
			NOMESERV : "Ituiutaba",
			IPSERV: "192.168.0.45",
			PORTSERV: "8080"	
		}		 
		]
	};*/

	$scope.$on('$ionicView.loaded', function(){
		$credpar.clearServerConfig();
    });
	
	$scope.fieldServerChange = function(field){
		if(field.selection != ''){
			var serverConfig = {address: field.selection.IPSERV, port: field.selection.PORTSERV, protocol: 'http', name : field.selection.NOMESERV}
			$credpar.setServerConfig(serverConfig);
			
			if($credpar.getStoreItem("isCliente")){
				$state.go('welcome.login-cliente');
			} else {
				$state.go('welcome.login-lojista');
			}
		}
    };

	$scope.setupOptionSelected = function(field){
    	field.selection = {};
		
		for(var i = 0; i < field.entidades.length; i++){
			if(field.value == field.entidades[i].CODSERV){
				field.selection = field.entidades[i];
				break;
			}
		}
    };
    
	$scope.getServidores = function(){
		
		$credparService.buscarServidores(
				null, 
				function(data){
					$scope.fieldServer  = data.servidores;		
					
					if ($scope.fieldServer.options && !angular.isArray($scope.fieldServer.options)) {
						$scope.fieldServer.options = [$scope.fieldServer.options];
					}
				},
				function(msg, status, headers, config){					
				},
				function(data, status, headers, config){
					$scope.loginRunning = false;
					
					$ionicPopup.alert({
						cssClass: 'popUpCred',
						title: 'Oops, Sem conexão',
						template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
					});
					
					return true;
				}
		);
	};

	$scope.$on('$ionicView.loaded', function(){
        $scope.getServidores();
    });
})

.controller('LojistaCtrl', function($rootScope, $scope, $state, $timeout, $ionicScrollDelegate,$credpar,$credparService, $ionicPopup, $localStorage) {
	$scope.$on('$ionicView.beforeEnter', function(){
		$rootScope.showBack = false;
	});
	
	$scope.data = {}; 
	
	$scope.nomeB2B = $credpar.getStoreItem("nomeB2B");
	$scope.addClassAlterarSenha = addClassAlterarSenha;
		
	$scope.getSessionInfo = function(){
				var codParcB2B = $credpar.getStoreItem("codParcB2B");
				var codUsuB2B = $credpar.getStoreItem("codUsuB2B");
				var codContatoB2B = $credpar.getStoreItem("codContatoB2B");
				var nomeB2B = $credpar.getStoreItem("nomeB2B");
				$scope.data.acessoApp = $localStorage.acessoApp;
				
				if(codParcB2B == null){
					$credparService.getSessionInfo(
				        	null, 
				        	function(data){
			        			var sessionInfo = data.sessionInfo;
			        			var isB2BMode = false;
			        			var codParcB2B = "";
			        			
								if(sessionInfo != null){				
									isB2BMode = Boolean(sessionInfo._b2bMode);
									codParcB2B = sessionInfo._parceiro;
									codUsuB2B = sessionInfo._usuario;
									codContatoB2B = sessionInfo._contato;
									
									$credpar.setStoreItem('codParcB2B', codParcB2B);
									$credpar.setStoreItem('codContatoB2B', codContatoB2B);
									$credpar.setStoreItem('codUsuB2B', codUsuB2B);
								}
							
								if(isB2BMode && codParcB2B != null && codContatoB2B != null){
									
									$credparService.buscarNomeLojista(
										codParcB2B, 
										codContatoB2B, 
							        	function(data){
											var entidadeObj = data.entidades.entidade;
											
											if(entidadeObj != null){
												$scope.nomeB2B = entidadeObj.NOMECONTATO["__cdata"];
												$scope.data.acessoApp =  entidadeObj.ACESSOAPP["__cdata"];
												$localStorage.acessoApp = $scope.data.acessoApp;
												$credpar.setStoreItem('nomeB2B', $scope.nomeB2B);
//												addClassAlterarSenha($scope.data.acessoApp);
											}
										},
										function(msg, status, headers, config){
							                $scope.loginRunning = false;						                
							            },
							            function(data, status, headers, config){
							                $scope.loginRunning = false;
						
							                $ionicPopup.alert({
							                	cssClass: 'popUpCred',
							                    title: 'Oops, Sem conexão',
							                    template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
							                });
						
							                return true;
							            }
									);
									
								}else{
									$ionicPopup.alert({
										cssClass: 'popUpCred',
					        			title: 'Login não permitido',
					        			template: "O usuário não é B2B"
					        		});
									$timeout(function(){
										$credparService.logout();
								    }, 3000);
									
								}
				        		
				            },
				            function(msg, status, headers, config){
				                $scope.loginRunning = false;		                
				            },
				            function(data, status, headers, config){
				                $scope.loginRuninng = false;
			
				                $ionicPopup.alert({
				                	cssClass: 'popUpCred',
				                    title: 'Oops, Sem conexão',
				                    template: '<div>Verifique se você está conectado a Internet ou se os dados do servidor estão corretos.</div>'
				                });
			
				                return true;
				            }
			        );
				}
			};
			
			
//	addClassAlterarSenha($localStorage.acessoApp);
		
	function addClassAlterarSenha(value) {
		
		if (value == "E" || value == "B" ) {

			var myEl = angular.element(document.querySelector('#divAlterarSenha'));
			myEl.addClass('alterarSenhaExtratoBordero');
		}else if (value == "N") {
			
			var myEl = angular.element(document.querySelector('#divAlterarSenha'));
			myEl.addClass('alterarSenhaNenhum');
		}else if (value == "T") {
			var myEl = angular.element(document.querySelector('#divAlterarSenha'));
			myEl.addClass('alterarSenhaNenhum');
			
		}
	}
	
	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'none';
	    
        $scope.getSessionInfo();
        
    });

	$scope.goToAutorizacao = function($event){
    	$event.preventDefault();
    	$event.stopPropagation()
    	
    	$state.go('apploj.autorizacao-vendas-parceiro');
    	
	};
	
	$scope.goToProposta = function($event){
		$event.preventDefault();
		$event.stopPropagation()
		
		$state.go('apploj.proposta-adesao');
	};
	
	$scope.goToBordero = function($event){
		$event.preventDefault();
		$event.stopPropagation()
		
		$state.go('apploj.bordero-pagamento');
	};
	
	$scope.goToExtrato = function($event){
		$event.preventDefault();
		$event.stopPropagation()
		
		$state.go('apploj.extrato-vendas');
	};
	
	$scope.goToAlterarSenha = function($event){
		$event.preventDefault();
		$event.stopPropagation()
		
		$state.go('apploj.alterar-senha');
	};
	
	$scope.goToOndeComprar = function(){
		$state.go('apploj.ondeComprar');
	};

	$scope.doLogout = function(){
		$credparService.logout();		
    };
})

.controller('ClienteCtrl', function($rootScope, $scope, $state, $timeout, $ionicScrollDelegate, $credpar, $credparService, $ionicPopup) {
	$scope.$on('$ionicView.beforeEnter', function(){
		$rootScope.showBack = false;
	});
	$scope.account = {};
	$scope.account.userName = $credpar.getStoreItem('userName');

	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'none';	    
    });
	
	$scope.goToConsultaSaldo = function(){
		$state.go('cliente.saldo');
	};

	$scope.goToExtrato = function(){
		$state.go('cliente.extrato');
	};

	$scope.goToAtendimento = function(){
		$state.go('cliente.atendimento');
	};

	$scope.goToAlterarSenha = function(){
		$state.go('cliente.alterarsenha');
	};
	
	$scope.goToOndeComprar = function(){
		$state.go('cliente.ondeComprar');
	};
})

.controller('SaldoClienteCtrl', function($rootScope, $scope, $state, $timeout, $ionicScrollDelegate, $credpar, $credparService, $ionicPopup) {
	$scope.showLoading = true;
	$scope.showError = false;

	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'block';	    
    });
	
	$credparService.getInfoSaldo (
		function(data){
			$scope.showLoading = false;
			$scope.showError = false;
			
			if($credpar.emptyAsNull(data.mensagem) != null){
				$scope.showError = true;
				$scope.data = {mensagem: data.mensagem};
				return;
			}

			$scope.data = data;
		},
		function(msg, status, headers, config){
			$scope.showLoading = false;
			$scope.showError = true;
		},
		function(data, status, headers, config){
			$scope.showLoading = false;
			$scope.showError = true;
			
			return true;
		}
	);
})

.controller('ExtratoClienteCtrl', function($rootScope, $scope, $state, $timeout, $ionicScrollDelegate, $credpar, $credparService, $ionicPopup) {
	$scope.showLoading = true;
	$scope.showError = false;

	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'block';	    
    });
	
	$scope.valores = [];
	$scope.labels = ["Disp.", "Usado"];
	$scope.colors = ['#FAD8B1', '#D85904'];
	 
	$credparService.getInfoExtrato (
		function(data){
			$scope.showLoading = false;
			$scope.showError = false;
			
			if($credpar.emptyAsNull(data.mensagem) != null){
				$scope.showError = true;
				$scope.data = {mensagem: data.mensagem};
				return;
			}
			
			if(data && data.itens && data.itens.item && !angular.isArray(data.itens.item)) {
				data.itens.item = [data.itens.item];
			}

			$scope.data = data;

			$scope.valores.push(data.valorDisponivel);
			$scope.valores.push(data.valorUsado);

		},
		function(msg, status, headers, config){
			$scope.showLoading = false;
			$scope.showError = true;
		},
		function(data, status, headers, config){
			$scope.showLoading = false;
			$scope.showError = true;
			
			return true;
		}
	);
})

.controller('AtendimentoClienteCtrl', function($rootScope, $scope, $state, $timeout, $ionicScrollDelegate, $credpar, $credparService, $ionicPopup) {
	$scope.showLoading = true;
	$scope.showError = false;
	$scope.data = {};

	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'block';	    
    });
	
	$credparService.getInfoContato(
		function(data){
			$scope.data = data;
			$scope.data.cpf = $credpar.getStoreItem('userCredID');
			$scope.data.cpfformat = $rootScope.formatCpf($credpar.getStoreItem('userCredID'));
			
			$scope.showLoading = false;
			$scope.showError = false;
		},
		function(msg, status, headers, config){
			$scope.showLoading = false;
			$scope.showError = true;
		},
		function(data, status, headers, config){
			$scope.showLoading = false;
			$scope.showError = true;
			
			return true;
		}
	);
	
	$scope.enviar = function(){
		if($credpar.emptyAsNull($scope.data.cpf) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Atenção',
				template: 'É necessário informar o CPF.'
			});
			return;
		}

		if($credpar.emptyAsNull($scope.data.email) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Atenção',
				template: 'É necessário informar o E-mail.'
			});
			return;
		}

		if($scope.data.telefone == ""){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Atenção',
				template: 'É necessário informar o Telefone.'
			});
			return;
		}
		
		$credparService.enviarEmail(
			$scope.data,
			function(data){
				$ionicPopup.alert({
					cssClass: 'popUpCred',
					title: 'Solicitação de contato',
					template: 'Solicitação de contato enviada, em breve a Credpar entrará em contato.'
				});
				
			},
			function(msg, status, headers, config){
			},
			function(data, status, headers, config){
				$ionicPopup.alert({
					cssClass: 'popUpCred',
                    title: 'Oops, Sem conexão',
                    template: 'Não foi possível enviar a solicitação. Verifique sua conexão com a internet.'
                });

				return false;
			}
		);
	};
})


.controller('OndeComprarCtrl', function($scope, $state, $credpar, $credparService, $timeout, $compile,$document, $cordovaGeolocation) {
	
	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'block';
    });

	$scope.data = {};
	$scope.traceValue = [];
	$scope.data.descricao;
	
	 $scope.choice = 'cidade';
	  
	$scope.buscaCidadeSegmentos = buscaCidadeSegmentos;
	$scope.statusValue =  statusValue;
	$scope.goChildren = goChildren;
	$scope.gpsOpen = gpsOpen;
	$scope.goToback = goToback;
	$scope.i = 0;
	$scope.j = 0;
	$scope.actualNode = [];
	$scope.anterior = {};
	$scope.actualNode = [];
	$scope.title = [];
	buscaCidadeSegmentos();
	
	function buscaCidadeSegmentos(){
		$credparService.buscaCidadesSegmentos({DESCRICAO: $scope.data.descricao, TIPOPESQUISA: $scope.choice},
				function(data){
					var json = JSON.parse(JSON.stringify(eval("(" + data + ")")));					
					$scope.actualNode = json;
					$scope.cidade = json;
					$scope.anterior[$scope.i] = json;
					angular.element($document[0].getElementById('treeOndeComprar')).replaceWith($compile(angular.element('<abn-tree tree-data="traceValue"></abn-tree>'))($scope));
				}
			);
		
		$scope.data.descricao = "";
		$scope.i = 0;
		$scope.j = 0;
		$scope.title = [];
	}
	
	function gpsOpen (local) {
		
		
		if (window.cordova) {
			  cordova.plugins.diagnostic.isGpsLocationEnabled(
			                function(e) {
			                    if (e){
			                      alert("location on")
			                     
			                    }   
			                    else {
			                      alert("Location Not Turned ON");
			                      cordova.plugins.diagnostic.switchToLocationSettings();
			                    }
			                },
			                function(e) {
			                    alert('Error ' + e);
			                }
			            );
			        }
		
		$scope.latitude;
		$scope.longitude;
		$scope.localidade;
		
		var posOptions = { timeout: 50000, enableHighAccuracy: false };
			
		$cordovaGeolocation.getCurrentPosition(posOptions)
        .then(function(position) {
        	
        	$scope.latitude = position.coords.latitude;
        	$scope.longitude = position.coords.longitude;
        	
        	var localidade =   $scope.latitude.toString() + " , " +  $scope.longitude.toString();
        	
        	var i = 0;
			var end = [];
								
			launchnavigator.navigate(local, {
				    start: localidade
			});
        	
        }, function(err) {
            console.log(err);
        });
	}
	
	function statusValue (value) {
		
		 $scope.choice = value;
	}
	
	function goChildren(children, label) {
		
		if($scope.i == 3) {
//			gpsOpen($scope.actualNode);
			return;
		}else {
			
			if(children) {
				$scope.actualNode = children;
				$scope.anterior[$scope.i] = $scope.actualNode;
				$scope.title.push(label);
				$scope.i = $scope.i + 1;
			
			 }
		}
	}
	
	function goToback(){
			
			$scope.actualNode = $scope.anterior[$scope.i - 2];
			$scope.i = $scope.i - 1;
			
			if($scope.i == 0) {
				buscaCidadeSegmentos();
			}
			
			$scope.title.pop();
	}
	
})


.controller('AlterarSenhaClienteCtrl', function($scope, $state, $credpar, $ionicPopup, $credparService, $timeout) {
	$scope.data = {};

	$scope.$on('$ionicView.loaded', function(){
		var navBar = document.getElementById("navBar");
		navBar.style.display = 'block';	    
    });
	
	$scope.alterarSenha = function(){
		if($credpar.emptyAsNull($scope.data.senhaAtual) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Atenção',
				template: 'É necessário informar a Senha atual.'
			});
					
			return;
		}

		if($credpar.emptyAsNull($scope.data.novaSenha) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Atenção',
				template: 'É necessário informar a Nova senha.'
			});
			
			return;
		}

		if($credpar.emptyAsNull($scope.data.confNovaSenha) == null){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Atenção',
				template: 'É necessário informar a Confirmação da senha.'
			});
			
			return;
		}

		if($scope.data.novaSenha != $scope.data.confNovaSenha){
			$ionicPopup.alert({
				cssClass: 'popUpCred',
				title: 'Atenção',
				template: 'Os campos de Nova senha e Confirmação de senha estão diferentes.'
			});
			
			return;
		}

		$credparService.alterarSenha (
			{atual: CryptoJS.MD5($scope.data.senhaAtual).toString(), nova: CryptoJS.MD5($scope.data.confNovaSenha).toString()}, 
			function(data){
				$ionicPopup.alert({
					cssClass: 'popUpCred',
					title: 'Alteração de senha',
					template: 'Senha alterada com sucesso.'
				});
				
				$scope.data.senhaAtual = null;
				$scope.data.novaSenha = null;
				$scope.data.confNovaSenha = null;
				
			},
			function(msg, status, headers, config){
			},
			function(data, status, headers, config){
				$ionicPopup.alert({
					cssClass: 'popUpCred',
                    title: 'Oops, Sem conexão',
                    template: 'Não foi possível alterar a senha. Verifique sua conexão com a internet.'
                });

				return true;
			}
		);
	};
});




onblurDateBox = function(input){
	if(input.value == ''){
		input.type = 'text';
	}
};

