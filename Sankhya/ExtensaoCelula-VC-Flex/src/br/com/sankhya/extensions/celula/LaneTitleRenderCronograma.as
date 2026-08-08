package br.com.sankhya.extensions.celula{
	
	import mx.containers.Canvas;
	
	import mx.containers.Canvas;
	import mx.controls.Image;
	import mx.controls.Label;
	import br.com.sankhya.controls.timelinediagram.TimeLineLane;
	import br.com.sankhya.controls.timelinediagram.ILaneTitleRenderer;

	public class LaneTitleRenderCronograma extends Canvas implements ILaneTitleRenderer
	{
		private var _lane:TimeLineLane;
		private var _alertIcon:Image;
		private var _alertIconRed:Image;
		private var _label:Label;
		
		[Embed("assets/iconAlert.png")]
		private var _iconAlert:Class;
		
		[Embed("assets/iconAlertRed.png")]
		private var _iconAlertRed:Class;
		
		
		private var _topPadding:int = 5;
		private var _bottomPadding:int = 5;
		private var _leftPadding:int = 4;
		private var _rightPadding:int = 5;
		private var _codUsu:String;
		private var _expandedHeight:Number = 20;
		
		public function LaneTitleRenderCronograma(lane:TimeLineLane){
			super();
			
			_lane = lane;
			
			_alertIcon = new Image();
			_alertIcon.source = _iconAlert;
			_alertIcon.toolTip = "Este usuário não é membro da(s) célula(s) selecionada(s)."
			addChild(_alertIcon);
			
			_alertIconRed = new Image();
			_alertIconRed.source = _iconAlertRed;
			_alertIconRed.toolTip = "Este usuário não possui tarefas."
			addChild(_alertIconRed);
			
			
			_label = new Label();
			_label.setStyle("fontWeight", "bold");
			addChild(_label);
		}
		
		public function updateHeaderData():void{
			_label.text = _lane.description;
			
			var codUsu:String = _lane.getDynamicProperty("CODUSU");
			
			_label.toolTip = codUsu + " - " + _lane.description;
			
			if(_codUsu != codUsu){
				_codUsu = codUsu;
			}
		}
		
		public function getExpandedHeight():Number{
			return _expandedHeight;
		}
		
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void{
			super.updateDisplayList(unscaledWidth, unscaledHeight);
			var xPos:int = _leftPadding;
			_alertIcon.visible = "S" == _lane.getDynamicProperty("FOREIGNUSU");
			_alertIconRed.visible = "S" == _lane.getDynamicProperty("NOTASKS");
			
			if(_alertIcon.visible){
				_alertIcon.x = xPos;
				_alertIcon.y = 2;
				xPos += _alertIcon.width + 2;
			}
			
			if(_alertIconRed.visible){
				_alertIconRed.x = xPos;
				_alertIconRed.y = 2;
				xPos += _alertIconRed.width + 2;
			}
			
			_label.x = xPos;
			_label.y = 1;
			_label.width = (unscaledWidth - _label.x);
		}
	}

}