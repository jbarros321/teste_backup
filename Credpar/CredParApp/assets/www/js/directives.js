angular.module('credparapp.directives',[])

.directive('formattedTime', function ($filter) {
	return {
		require: '?ngModel',
		link: function(scope, elem, attr, ngModel) {
			if( !ngModel )
	            return;
	        if( attr.type !== 'time' )
	            return;
	                
	        ngModel.$formatters.unshift(function(value) {
	            return value.replace(/:[0-9]+.[0-9]+$/, '');
	        });
		}
	};  
})

.directive('fallBackSrc', function () {
    return{
        link: function postLink(scope, element, attrs) {
            element.bind('error', function () {
                angular.element(this).attr('src', attrs.fallBackSrc);
            });
        }
    }
})

.directive('ngEnter', function() {
    return function(scope, element, attrs) {
        element.bind("keydown keypress", function(event) {
            if(event.which === 13) {
                scope.$apply(function(){
                    scope.$eval(attrs.ngEnter, {'event': event});
                });

                event.preventDefault();
            }
        });
    };
})

.directive('ngRestrict', [function(){
    return function (scope, ele, attrs) {
    	 var element = ele[0];
    	 var regex = RegExp(attrs.ngRestrict);
         var value = element.value;

         ele.bind("keydown keypress", function(event) {
             if (regex.test(element.value)){
                 value = element.value;
             }else{
             	element.value = value;
             	event.preventDefault();
             }
         });
    };
}]);