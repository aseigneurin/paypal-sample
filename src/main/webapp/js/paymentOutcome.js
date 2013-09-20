angular.module('paymentOutcome', [])
	.config(['$routeProvider', function($routeProvider) {
		$routeProvider
			.when('/approved', {templateUrl: 'partials/paypalPaymentApproved.html'})
			.when('/failed', {templateUrl: 'partials/paypalPaymentFailed.html'});
	}]);