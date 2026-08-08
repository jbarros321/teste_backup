angular.module('credparapp', [
    'ngAnimate',
    'ionic',
    'ionic-modal-select',
    'ngCordova',
    'cfp.loadingBar',
    'cfp.loadingBarInterceptor',
    'credparapp.services',
    'credparapp.controllers',
    'credparapp.directives',
    'chart.js',
    'ngStorage'
])

.run(function($http, $state) {
	ionic.Platform.ready(function(){  
    	// for ios7 style header bars
        if(window.StatusBar) {
            // org.apache.cordova.statusbar required
            StatusBar.styleLightContent();
        }

        // hide the prev/next buttons on the keyboard input
        if(window.cordova && window.cordova.plugins && window.cordova.plugins.Keyboard){
            cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
        }
    	
    	if(window.cordova && window.cordova.plugins && window.cordova.plugins.backgroundMode){
    	    cordova.plugins.backgroundMode.setDefaults({title:'Sankhya Tasks', text:'Monitorando tarefas...'});
    	}
    	
    	if(navigator.splashscreen){
    		navigator.splashscreen.hide();
    	}
    });
})

.config(['cfpLoadingBarProvider', function(cfpLoadingBarProvider) {
    cfpLoadingBarProvider.includeSpinner = false;
}])

.config(['$ionicConfigProvider', function($ionicConfigProvider) {
	$ionicConfigProvider.views.maxCache(10);
	$ionicConfigProvider.views.forwardCache(true);
    $ionicConfigProvider.backButton.text('').icon('ion-chevron-left').previousTitleText(false);
}])

//.config(['$snkwProvider', function($snkwProvider) {
//	var appName = 'SankhyaCredPar';
//	var appID = APP_ID;
//	
//	//Versão mínima do servidor Sankhya-W para rodar esse aplicativo
//	$snkwProvider.setConfig({
//		appName: appName,
//		appID: appID,
//		minVersion: '3.11.20'
//    });
//}])

.config(function($stateProvider, $urlRouterProvider) {
    
	$stateProvider
    
	.state('welcome', {
        url: '/welcome',
        abstract: true,
        templateUrl: 'templates/welcome.html'
    })
    
    .state('welcome.credpar', {
        url: '/credpar',
        views: {
            'content': {
                templateUrl: 'templates/welcome-credpar.html',
                controller: 'WelcomeHomeCtrl'
            }
        }
    })
    
    .state('welcome.select-city', {
    	url: '/select-city',
    	views: {
    		'content': {
    			templateUrl: 'templates/selecionar-cidade.html',
    			controller: 'SelectCityCtrl'
    		}
    	}
    })
    
    .state('welcome.login-cliente', {
    	url: '/login/cliente',
    	views: {
    		'content': {
    			templateUrl: 'templates/welcome-login-cliente.html',
    			controller: 'WelcomeLoginClienteCtrl'
    		}
    	}
    })
    
    .state('welcome.login-lojista', {
    	url: '/login/lojista',
    	views: {
    		'content': {
    			templateUrl: 'templates/welcome-login-lojista.html',
    			controller: 'WelcomeLoginCtrl'
    		}
    	}
    })
	
    .state('apploj', {
        url: '/apploj',
        abstract: true,
        templateUrl: "templates/workspace.html"
    })

    .state('apploj.home', {
        url: '/home',
        views: {
            'content': {
                templateUrl: 'templates/home-lojista.html',
                controller: 'LojistaCtrl'
            }
        }
    })
    
    .state('apploj.autorizacao-vendas', {
    	url: '/autorizacao',
    	views: {
    		'content': {
    			templateUrl: 'templates/autorizacao-vendas.html',
    			controller: 'AutorizacaoVendasCtrl'
    		}
    	}
    })
    
    .state('apploj.autorizacao-vendas-parceiro', {
    	url: '/parceiro',
    	views: {
    		'content': {
    			templateUrl: 'templates/autorizacao-vendas-parceiro.html',
    			controller: 'AutorizacaoVendasParcCtrl'
    		}
    	}
    })
    
    .state('apploj.proposta-adesao', {
    	url: '/propostaadesao',
    	views: {
    		'content': {
    			templateUrl: 'templates/proposta-adesao.html',
    			controller: 'PropostaAdesaoCtrl'
    		}
    	}
    })
    
    .state('apploj.consulta-proposta', {
    	url: '/consultaproposta',
    	views: {
    		'content': {
    			templateUrl: 'templates/consulta-proposta.html',
    			controller: 'ConsultaPropostaCtrl'
    		}
    	}
    })
    
    .state('apploj.nova-proposta', {
    	url: '/novaproposta',
    	views: {
    		'content': {
    			templateUrl: 'templates/nova-proposta.html',
    			controller: 'NovaPropostaCtrl'
    		}
    	}
    })
    
    .state('apploj.bordero-pagamento', {
    	url: '/borderopagamento',
    	views: {
    		'content': {
    			templateUrl: 'templates/bordero-pagamento.html',
    			controller: 'BorderoPagamentoCtrl'
    		}
    	}
    })
    
    .state('apploj.extrato-bordero-pagamento', {
    	url: '/extrato',
    	views: {
    		'content': {
    			templateUrl: 'templates/autorizacoes-bordero-pagamento.html',
    			controller: 'ExtratoBorderoPagamentoCtrl'
    		}
    	}
    })
    
    .state('apploj.extrato-vendas', {
    	url: '/extratovendas',
    	views: {
    		'content': {
    			templateUrl: 'templates/extrato-vendas.html',
    			controller: 'ExtratoVendasPagamentoCtrl'
    		}
    	}
    })
    
    .state('apploj.autorizacoes-extrato-vendas', {
    	url: '/autorizacoes',
    	views: {
    		'content': {
    			templateUrl: 'templates/autorizacoes-extrato-vendas.html',
    			controller: 'AutorizacoesVendasPagamentoCtrl'
    		}
    	}
    })
    
   
    
   .state('apploj.ondeComprar', {
        url: '/ondeComprar',
        views: {
            'content': {
            	templateUrl: 'templates/onde-comprar.html',
            	controller: 'OndeComprarCtrl'
            }
        }
    })
    

    .state('apploj.alterar-senha', {
        url: '/alterarsenha',
        views: {
            'content': {
                templateUrl: 'templates/alterar-senha.html',
                controller: 'AlterarSenhaCtrl'
            }
        }
    })
    

    .state('cliente', {
        url: '/cliente',
        abstract: true,
        templateUrl: "templates/workspace.html"
    })
    
    .state('cliente.home', {
        url: '/home',
        views: {
            'content': {
                templateUrl: 'templates/home-cliente.html',
                controller: 'ClienteCtrl'
            }
        }
    })

	.state('cliente.saldo', {
		url: '/saldo',
		views: {
			'content': {
				templateUrl: 'templates/saldo-cliente.html',
				controller: 'SaldoClienteCtrl'
			}
		}
	})

	.state('cliente.extrato', {
		url: '/extrato',
		views: {
			'content': {
				templateUrl: 'templates/extrato-cliente.html',
				controller: 'ExtratoClienteCtrl'
			}
		}
	})

	.state('cliente.atendimento', {
		url: '/atendimento',
		views: {
			'content': {
				templateUrl: 'templates/atendimento-cliente.html',
				controller: 'AtendimentoClienteCtrl'
			}
		}
	})
	
	
	
	.state('cliente.ondeComprar', {
		url:'/ondeComprar',
		views: {
			'content': {
				templateUrl: 'templates/onde-comprar.html',
				controller: 'OndeComprarCtrl'
			}
		}
	})
    
	.state('cliente.alterarsenha', {
		url: '/alterarsenha',
		views: {
			'content': {
				templateUrl: 'templates/alterar-senha-cliente.html',
				controller: 'AlterarSenhaClienteCtrl'
			}
		}
	});
  
    $urlRouterProvider.otherwise(function ($injector) {
        var $state = $injector.get('$state');
        var $credPar = $injector.get('$credpar');
//        var $credparService = $injector.get('$credparService');
//
        var hasConnectionConfig = $credPar.hasConnectionConfig();
        var hasConnectionConfigClient = $credPar.hasConnectionConfigClient();
//
        if(hasConnectionConfig){
    		$state.go('apploj.home');
        }else if (hasConnectionConfigClient){
        	$state.go('cliente.home');
        }else{
        	$state.go('welcome.credpar');
        }
    });
    
});   