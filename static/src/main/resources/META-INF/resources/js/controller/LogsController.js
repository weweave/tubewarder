define(['angular', 'moment', 'app'], function(angular, moment, app) {
	'use strict';

    app.lazy.controller('LogsController', ['$scope', '$http', 'appServices', function($scope, $http, appServices) {
        appServices.setActiveNavItem('logs');
        
        var dateTimeFormat = 'YYYY-MM-DD HH:mm:ss';
        var endDate = moment();
        var startDate = moment().subtract(1, 'month');
        $scope.model = {
            logs: [],
            startDate: startDate.format(dateTimeFormat),
            endDate: endDate.format(dateTimeFormat),
            keyword: '',
            searchString: '',
            log: {}
        };
        $scope.dateTimePickerOptions = {
            format: dateTimeFormat
        };
        
        $scope.showLog = function(id) {
            appServices.setLoading(true);
            $scope.model.log = {};
            var payload = {
                token: appServices.getToken(),
                id: id
            };
            $http.get('/rs/log/get', {params: payload}).success(function(data) {
                $scope.model.log = data.logs[0];
                $('#logModal').modal('show');
                appServices.setLoading(false);
            });
        };
        
        var load = function() {
            appServices.setLoading(true);
            var payload = {
                token: appServices.getToken(),
                startDate: moment($scope.model.startDate).format(dateTimeFormat),
                endDate: moment($scope.model.endDate).format(dateTimeFormat),
                keyword: $scope.model.keyword,
                searchString: $scope.model.searchString
            };
            $http.get('/rs/log/get', {params: payload}).success(function(data) {
                $scope.model.logs = data.logs;
                appServices.setLoading(false);
            });
        };
        
        load();
        /*
        $('.datetimepicker').datetimepicker({
            format: 'YYYY-MM-DD HH:mm:ss'
        });
        */
        
        $scope.applyFilter = function() {
            load();
        };
    }]);
});
