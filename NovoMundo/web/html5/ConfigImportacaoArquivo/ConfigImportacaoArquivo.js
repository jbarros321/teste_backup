angular
    .module('ConfigImportacaoArquivoApp', ['snk'])
    .controller('ConfigImportacaoArquivoController', ['$q', 'i18n', 'ObjectUtils', 'SkApplicationInstance', 'Criteria', 'StringUtils', 'DateUtils', 'ServiceProxy', 'MessageUtils', '$scope', 'SkComponentRegistry',
        function ($q, i18n, ObjectUtils, SkApplicationInstance, Criteria, StringUtils, DateUtils, ServiceProxy, MessageUtils, $scope, SkComponentRegistry) {
            var self = this;
            var _dsConfig;

            self.usaPagamentoRepom = false;
            self.dynaformID = StringUtils.nextUid();
            $scope.loadByPK = loadByPK;
            
            self.onDynaformLoaded = onDynaformLoaded;
            self.voltarLancador = voltarLancador;
            self.alterarLancador = alterarLancador;
            
            function loadByPK(pkObject) {
        		SkComponentRegistry
            		.get(self.dynaformID)
            		.then(function (dynaform) {
            			dynaform.loadByPK(pkObject);
            		});
    		}
            
            function onDynaformLoaded(dynaform, dataset){
            	if(dataset.getEntityName() == "ImportacaoArquivoConfig"){
            		_dsConfig = dataset;
            	}
            }
            
            function voltarLancador(){
            	if(_dsConfig.isEmpty()){
					MessageUtils.showAlert(MessageUtils.TITLE_WARNING, "Selecione um registro antes.");
					return;
				}
				
				if(_dsConfig.isRecordDirty()){
					MessageUtils.showAlert(MessageUtils.TITLE_INFORMATION, "Salve as informacoes antes.");
					return;
				}
				
				var params = {idTela: _dsConfig.getFieldValueAsString("IDTELA")};
				
				ServiceProxy.callService("hnzimp@ImportacaoArquivoSP.voltarLancadorTela", {params: params})
                .then(function(result){
                	MessageUtils.showInfo(MessageUtils.TITLE_INFORMATION, "Tela ajustada para o padrao.");	
					_dsConfig.refreshCurrentRow();
                });
            }

            function alterarLancador(){
            	if(_dsConfig.isEmpty()){
            		MessageUtils.showAlert(MessageUtils.TITLE_WARNING, "Selecione um registro antes.");
            		return;
            	}
            	
            	if(_dsConfig.isRecordDirty()){
            		MessageUtils.showAlert(MessageUtils.TITLE_INFORMATION, "Salve as informacoes antes.");
            		return;
            	}
            	
            	var params = {idTela: _dsConfig.getFieldValueAsString("IDTELA")};
            	
            	ServiceProxy.callService("hnzimp@ImportacaoArquivoSP.alterarLancadorTela", {params: params})
            	.then(function(result){
           			MessageUtils.showInfo(MessageUtils.TITLE_INFORMATION, "Tela ajustada para importar o arquivo.");
           			_dsConfig.refreshCurrentRow();
            	});
            }
    	}
]);