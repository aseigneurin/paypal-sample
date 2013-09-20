package com.seigneurin

import com.paypal.core.rest.OAuthTokenCredential
import com.paypal.api.payments.CreditCard
import com.paypal.api.payments.Payment
import com.paypal.api.payments.Transaction
import com.paypal.api.payments.Amount
import com.paypal.api.payments.Details
import com.paypal.api.payments.FundingInstrument
import com.paypal.api.payments.Payer
import java.util.ArrayList
import com.paypal.core.rest.APIContext
import com.paypal.core.ConfigManager
import com.paypal.api.payments.RedirectUrls

object PaypalPaymentExample extends Application {

  var clientID = ConfigManager.getInstance().getValue("clientID")
  var clientSecret = ConfigManager.getInstance().getValue("clientSecret")
  var tokenCredential = new OAuthTokenCredential(clientID, clientSecret)

  var accessToken = tokenCredential.getAccessToken

  var amountDetails = new Details
  amountDetails.setSubtotal("7.41")
  amountDetails.setTax("0.03")
  amountDetails.setShipping("0.03")

  var amount = new Amount
  amount.setTotal("7.47")
  amount.setCurrency("USD")
  amount.setDetails(amountDetails)

  var transaction = new Transaction
  transaction.setAmount(amount)
  transaction.setDescription("This is the payment transaction description.")

  var transactions = new ArrayList[Transaction]
  transactions.add(transaction)

  var payer = new Payer
  payer.setPaymentMethod("paypal")

  var redirectUrls = new RedirectUrls
  redirectUrls.setReturnUrl("http://localhost:8080/paypal-sample/paypalPaymentApproved")
  redirectUrls.setCancelUrl("http://localhost:8080/paypal-sample/paypalPaymentCanceled")

  var payment = new Payment
  payment.setIntent("sale")
  payment.setPayer(payer)
  payment.setTransactions(transactions)
  payment.setRedirectUrls(redirectUrls)

  var apiContext = new APIContext(accessToken);

  var createdPayment = payment.create(accessToken)
  println("Id: " + createdPayment.getId())
  println("State: " + createdPayment.getState())
  println("Links: " + createdPayment.getLinks())

}