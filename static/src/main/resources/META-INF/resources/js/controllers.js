define(['angular', 'app'], function(angular, app) {
	'use strict';

	var controllers = angular.module('controllers', []);

	controllers.run(['$location', '$http', '$rootScope', 'appServices', function($location, $http, $rootScope, appServices) {
	    appServices.setLoading(true);
		appServices.loadSession();
		$rootScope.licensed = true;

        if ($rootScope.isLoggedIn) {
            var payload = {
                token: appServices.getToken()
            };
            $http.post('/rs/ping', payload).success(function(data) {
                if (data.error) {
                    appServices.setSession(null);
                    $location.path('/login');
                }
            });
        }
	}]);

	controllers.controller('RootController', ['$scope', '$rootScope', '$location', '$http', 'appServices', function($scope, $rootScope, $location, $http, appServices) {
		$scope.resetFormInvalid = function(form, field) {
			form[field].$setValidity('invalid', true);
			form[field].$validate();
		};

		$rootScope.$on('$routeChangeStart', function(event, next, current) {
			appServices.setLoading(true);
            if (!$rootScope.isLoggedIn) {
				if (next.originalPath != '/login') {
					$location.path('/login');
				}
			}
		});
        
        $scope.logout = function() {
			appServices.logout();
		};
	}]);

    return controllers;
});
