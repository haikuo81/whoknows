'use strict';

angular.module('wkCommon').directive('wkNameSpan', function ($location, $window, UserService, DEFAULT_IMG) {

	return {
		restrict: 'EA',
		templateUrl: 'app/commons/directives/nameSpan',
		replace: true,
		scope: {
			user : '=',
			hideImg : '='
		},
		link: function (scope, elem) {
			scope.defaultPeopleImg = DEFAULT_IMG.PEOPLE_NO_IMG;
		}
	};
});