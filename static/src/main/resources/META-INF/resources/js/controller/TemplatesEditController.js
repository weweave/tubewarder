define(['angular', 'app'], function(angular, app) {
	'use strict';

    app.lazy.controller('TemplatesEditController', ['$scope', '$http', '$location', '$routeParams', 'appServices', function($scope, $http, $location, $routeParams, appServices) {
        appServices.setActiveNavItem('templates');
        
        $scope.model = {
            id: '',
            name: '',
            groupId: '',
            channelTemplates: []
        };
        $scope.groups = [];

        $scope.submit = function(form) {
            appServices.setLoading(true);
            var payload = {
                token: appServices.getToken(),
                object: {
                    id: $scope.model.id,
                    name: $scope.model.name,
                    group: {
                        id: $scope.model.groupId
                    }
                }
            };
            appServices.post('/rs/template/set', payload,
                function(data) {
                    $location.path('/templates');
                },
                function(fieldErrors) {
                    if (fieldErrors.name) {
                        form.name.$setValidity('invalid', false);
                        appServices.focus('#name');
                        if ($.inArray(appServices.getErrors().FIELD_NAME_ALREADY_EXISTS, fieldErrors.name) !== -1) {
                            appServices.error('Name already exists');
                        } else if ($.inArray(appServices.getErrors().FIELD_REQUIRED, fieldErrors.name) !== -1) {
                            appServices.error('Name is required');
                        }
                    }
                    appServices.setLoading(false);
                }
            );
		};
        
        var loadGroups = function(cb) {
            var payload = {
                token: appServices.getToken()
            };
            $http.get('/rs/group/get/small', {params: payload}).success(function(data) {
                $scope.groups = data.groups;
                cb();
            });
        };
        
        var load = function() {
            if ($routeParams.id) {
                var payload = {
                    token: appServices.getToken(),
                    id: $routeParams.id
                };
                $http.get('/rs/template/get', {params: payload}).success(function(data) {
                    var template = data.templates[0];
                    $scope.model.id = template.id;
                    $scope.model.name = template.name;
                    $scope.model.groupId = template.group.id;
                    $scope.model.channelTemplates = template.channelTemplates;
                    appServices.setLoading(false); 
                });
            } else {
                appServices.setLoading(false);
            }
        };
        
        $scope.deleteChannelTemplate = function(id) {
            if (!confirm("Delete this channel association?")) return;
            appServices.setLoading(true);
            var payload = {
                token: appServices.getToken(),
                id: id
            };
            $http.post('/rs/channeltemplate/delete', payload).success(function(data) {
                load();
            });
        };
        
        loadGroups(load);
    }]);
});
