define(['angular', 'app'], function(angular, app) {
	'use strict';

    app.lazy.controller('ChannelsController', ['$scope', '$http', 'appServices', function($scope, $http, appServices) {
        appServices.setActiveNavItem('channels');
        
        $scope.model = {
            channels: []
        };
        
        var load = function() {
            var payload = {
                token: appServices.getToken()
            };
            $http.get('/rs/channel/get', {params: payload}).success(function(data) {
                $scope.model.channels = data.channels;
                appServices.setLoading(false);
            });
        };
        
        $scope.deleteChannel = function(configId, channelId) {
            if (!confirm("Delete this channel? This will delete all channel-templates associated with this channel.")) return;
            appServices.setLoading(true);
            var payload = {
                id: channelId,
                token: appServices.getToken()
            };
            $http.post('/rs/channel/delete', payload).success(function(data) {
                load();
            });
        };
        
        load();
    }]);
});
