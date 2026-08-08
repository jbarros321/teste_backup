angular
    .module('UploadArquivoApp', ['snk'])
    .controller('UploadArquivoController', ['$q', 'i18n', 'ObjectUtils', 'SkApplicationInstance', 'Criteria', 'StringUtils', 'ArrayUtils', 'ServiceProxy', 'MessageUtils', '$scope', 'SkComponentRegistry', 'AngularUtil', 'SessionFileUpload', 'SkFileInputConstant',
        function ($q, i18n, ObjectUtils, SkApplicationInstance, Criteria, StringUtils, ArrayUtils, ServiceProxy, MessageUtils, $scope, SkComponentRegistry, AngularUtil, SessionFileUpload, SkFileInputConstant) {
            var self = this;
            var _dsEntity;
            var _chaveSessao = "ARQUIVO_IMPORTACAO_";

            self.usaPagamentoRepom = false;
            $scope.loadByPK = loadByPK;
            self.resourceID = SkApplicationInstance.getResourceID();
            
            $scope.importar = importar;
            $scope.onDynaformLoaded = onDynaformLoaded;
            
            init();
            
            function init() {
                var entityBody = {
                    entity: { name: "ImportacaoArquivoConfig",
                        literalCriteria: {
                            expression: {"$": "IDTELA = '" + self.resourceID + "'"}
                        },
                        fields: {field: [
                            {name: 'INSTANCIA'}
                        ]}
                    }
                };

                ServiceProxy.callService("mge@crud.find", entityBody)
                    .then(function(result){
                        var entidadeElem = result.responseBody.entidades.entidade;

                        if(entidadeElem){
                        	self.instancia = entidadeElem.INSTANCIA.$;
                        	
                        	_chaveSessao += self.instancia;
                        	
                            var dynaform = angular.element(AngularUtil.createDirective('sk-dynaform', {
                                 'sk-entity-name': self.instancia,
                                 //'sk-skip-start-page': 'true',
                                 'sk-on-dynaform-loaded': 'onDynaformLoaded(dynaform, dataset)'
                            }, $scope, 
                            false,
                            function(element){
                            	var dynaformTag = angular.element(AngularUtil.createDirective('dynaform-' + StringUtils.toDashCase(self.instancia), {}, $scope));
                            	
                            	var topBar = angular.element(AngularUtil.createDirective('sk-right-top-bar', {}, $scope));
                            	
                            	/*var button = AngularUtil.createDirective('button', {
                            		'class': 'btn btn-default',
                            		'tooltip-placement': 'bottom',
                            		'sk-i18n': 'Importar arquivo',
                            		'tooltip': 'Importar arquivo',
                            		'ng-click': 'importar()'
                            	}, $scope);
                            	
                            	topBar.append(button);*/
                            	
                            	$scope.onUploadFinished = onUploadFinished;
                            	$scope.beforeState = beforeState;
                            	
                            	var importacao = AngularUtil.createDirective('sk-file-input', {
                            		'id': 'btnImportar',
                            		'class': 'btn btn-default btn-importar',
                            		'sk-file-key': _chaveSessao,
                            		'sk-upload-state': 'onUploadFinished($state, $value)',
                            		'sk-before-upload': 'beforeState()',
                            		'sk-btn-label': 'Importar arquivo',
                            		'sk-tooltip': 'Importar arquivo'
                            	}, $scope);
                            	
                            	topBar.append(importacao);
                            	
                            	dynaformTag.append(topBar);
                            	element.append(dynaformTag);
                            }));
                            
                            var application = AngularUtil.getControllerFromElement(document.getElementById('idApplication'));
                            application.getElement().append(dynaform);
                        }
                    });
            }
            
            function onUploadFinished(state, value, fileInput) {
                if (state != SkFileInputConstant.UPLOAD_SUCCESS) {
                    return;
                }
                
                importarArquivo(value.name);
                
                var btnImportar = AngularUtil.getControllerFromElement(document.getElementById('btnImportar'));
                btnImportar.clear();
            }
    
            function beforeState() {
            	//var btnImportar = AngularUtil.getControllerFromElement(document.getElementById('btnImportar'));
                //btnImportar.clear();
            }
            
            function loadByPK(pkObject) {
            	self.pkObject = pkObject;
            	
            	if(self.pkObject != null && _dsEntity != null){
    				_dsEntity.refresh(getPkObject());
        		}
    		}
            
            function onDynaformLoaded(dynaform, dataset){
            	if(dataset.getEntityName() == self.instancia){
            		_dsEntity = dataset;
            		
            		if(self.pkObject != null){
            			dynaform.onSelectShowGridOnStartPage();
            			
            			AngularUtil.timeout(function () {
            				_dsEntity.cancelEdition();
            				_dsEntity.refresh(getPkObject());
            			}, 500);
            		}
            	}
            }
            
            function getPkObject(){
            	var pks = [];

                if (self.pkObject.ACTION_PARAMETERS) {
                    angular.forEach(self.pkObject.ACTION_PARAMETERS, function (param) {
                    	pks.push({
                            name: param.fieldName,
                            value: param.value
                        });
                    });
                } else {
                    angular.forEach(self.pkObject, function (value, key) {
                    	pks.push({
                            name: key,
                            value: value
                        });
                    });
                }
                
                if (!angular.isArray(pks)) {
                    pks = [pks];
                }

                var criteria = Criteria();

                for (var index in pks) {
                    var pk = pks[index];

                    var pkMD = _dsEntity.getFieldMetadata(pk.name);

                    if (pkMD) {
                        criteria.and(pk.name + ' = ?');
                        criteria.addParameter(Criteria.buildParameter(pkMD.dataType, pk.value));
                    }
                }
                
                return criteria;
            }
            
            function importar(){
				if(_dsEntity.isRecordDirty()){
					MessageUtils.showAlert(MessageUtils.TITLE_INFORMATION, "Salve as informacoes antes de importar o arquivo.");
					return;
				}
            	
				var props = {
					message: "Selecione o arquivo para importar",
					title: "Importador"
                };
                
				SessionFileUpload.openCustomSessionUpload(
					_chaveSessao,
					function onUpload(fileKey, fileName){
						importarArquivo(fileName);
					},
					props
				);
            }
            
            function importarArquivo(nomeArquivo){
            	var params = {chaveSessao: _chaveSessao, idTela: self.resourceID, nomeArquivo: nomeArquivo};
				
				var pks = _dsEntity.getPrimaryKeys();
				
				for (var index in pks) {
		            var pkField = pks[index];
		            var field = _dsEntity.getFieldMetadata(pkField);
				
					params[field.id] = {type: field.dataType, "$": _dsEntity.getFieldValueAsString(field.id)};
				}
				
				ServiceProxy.callService("hnzimp@ImportacaoArquivoSP.importarArquivo", {params: params})
                .then(function(result){
                    var mensagem = result.responseBody.mensagem;

                    if(mensagem != null){
                    	mensagem = StringUtils.replaceAll(mensagem.$, '\n', '<br/>');
                    	
                    	MessageUtils.showInfo(MessageUtils.TITLE_INFORMATION, mensagem);
                    }
                    
                    _dsEntity.refreshCurrentRow();
                });
            }
    	}
]);