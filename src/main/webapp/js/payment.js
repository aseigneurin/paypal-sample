angular.module('myApp', ['ui.bootstrap']);

var PaymentCtrl = function ($scope, $modal, $http) {
    $scope.payWithCreditCard = function() {

        var modalInstance = $modal.open({
            templateUrl: "partials/creditCardModal.html",
            controller: CreditCardCtrl
        });

        modalInstance.opened.then(function() {
        });

        modalInstance.result.then(function(result) {
            showPaymentProgress();
            var data = {
                number: result.number,
                type: result.type,
                expire_month: result.expire_month,
                expire_year: result.expire_year,
                cvv2: result.cvv2,
                first_name: result.first_name,
                last_name: result.last_name
            };
            $http.post("web/payWithCreditCard", data)
            .success(function(data, status) {
                $scope.paymentOutcome = "Payment approved: " + data;
            })
            .error(function(data, status) {
                $scope.paymentOutcome = "Payment failed";
            });
        });
    };

    showPaymentProgress = function() {
        var paymentOutcomeModalInstance = $modal.open({
            templateUrl: "partials/paymentOutcome.html",
            controller: PaymentOutcomeCtrl,
            scope: $scope
        });
    }
}

var CreditCardCtrl = function($scope, $modalInstance, $modal) {

    // default values
    $scope.number = "5500005555555559";
    $scope.type = "mastercard";
    $scope.expire_month = 1;
    $scope.expire_year = 2018;
    $scope.cvv2 = 111;
    $scope.first_name = "Joe";
    $scope.last_name = "Shopper";

    $scope.validate = function() {
        $modalInstance.close(this);
    };

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    };
}

var PaymentOutcomeCtrl = function($scope, $modalInstance) {
}