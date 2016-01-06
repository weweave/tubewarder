define(['angular', 'app'], function(angular, app) {
	'use strict';

    app.lazy.controller('ChannelsController', ['$scope', '$http', 'appServices', function($scope, $http, appServices) {
        appServices.setActiveNavItem('channels');
        
        $scope.model = {
            channels: []
        };
        
        var load = function() {
            $http.get('/rs/channel/get', {}).success(function(data) {
                $scope.model.channels = data.channels;
                for (var i=0; i<$scope.model.channels.length; i++) {
                    var channel = $scope.model.channels[i];
                    if (channel.outputHandler == 'SYSOUT') channel.outputHandlerReadable = 'Console';
                    if (channel.outputHandler == 'EMAIL') channel.outputHandlerReadable = 'Email';
                }
            });
        };
        
        var deleteConfig = function(id, cb) {
            var payload = {
                id: id
            };
            $http.post('/rs/outputhandlerconfiguration/delete', payload).success(function(data) {
                cb();
            });
        };
        
        $scope.deleteChannel = function(configId, channelId) {
            if (!confirm("Delete this channel?")) return;
            deleteConfig(configId, function() {
                var payload = {
                    id: channelId
                };
                $http.post('/rs/channel/delete', payload).success(function(data) {
                    load();
                });
            });
        };
        
        load();
    }]);
});