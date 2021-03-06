require.config({
	paths: {
        'jquery': 'lib/jquery-2.1.4.min',
        'angular': 'lib/angular-1.4.8.min',
		'angular-route': 'lib/angular-route-1.4.8.min',
		'angular-route-resolver': 'lib/angular-route-resolver',
        'bootstrap': 'lib/bootstrap-3.3.6.min',
        'moment': 'lib/moment-2.11.2.min',
        'bootstrap-datetimepicker': 'lib/bootstrap-datetimepicker-4.17.45.min',
        'autofill-event': 'lib/autofill-event',
		'typeahead': 'lib/typeahead-0.11.1.min',
		'bloodhound': 'lib/bloodhound-0.11.1.min',
		'webfontloader': 'lib/webfontloader-1.6.27',
		'ace-ext-language_tools': 'lib/ace/ext-language_tools',
		'ace': 'lib/ace/ace',
		'ui-ace': 'lib/ui-ace-0.2.3'
	},
	shim: {
        'bootstrap': {
			deps: ['jquery']
		},
		'typeahead': {
			deps: ['jquery'],
			init: function($) {
            	return require.s.contexts._.registry['typeahead.js'].factory($);
        	}
		},
		'bloodhound': {
			deps: ['jquery'],
			exports: 'Bloodhound'
		},
		'angular': {
            deps: ['jquery'],
			exports: 'angular'
		},
		'angular-route': {
			deps: ['angular']
		},
		'angular-route-resolver': {
			deps: ['angular-route']
		},
        'bootstrap-datetimepicker': {
            deps: ['bootstrap', 'moment']
        },
        'autofill-event': {
            deps: ['angular', 'jquery']
        },
		'webfontloader': {
			exports: 'WebFont'
		},
		'ace-ext-language_tools': {
			deps: ['ace']
		},
		'ui-ace': {
			deps: ['ace', 'ace-ext-language_tools', 'angular']
		},
		'app': {
			deps: [
				'bootstrap',
                'bootstrap-datetimepicker',
				'angular-route',
				'angular-route-resolver',
                'autofill-event',
				'typeahead',
				'bloodhound',
				'webfontloader',
				'ui-ace'
			],
			exports: 'app'
		},
		'controllers': {
			deps: ['app'],
			exports: 'controllers'
		}
	},
	urlArgs: "v="+new Date().getTime()
});

require(['controllers', 'app'], function(controllers, app) {
	app.init();
});
