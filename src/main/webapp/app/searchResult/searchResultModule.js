
'use strict';

var wkCommon = angular.module('wkSearchResult', ['ngRoute']).config(function($routeProvider, $logProvider){
	$routeProvider.when('/searchResult', {
		templateUrl: 'app/searchResult/searchResult',
		controller: 'SearchResultCtrl',
		data: {
			standalonePage: true
		}
	})
});

