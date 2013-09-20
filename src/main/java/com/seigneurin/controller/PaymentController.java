package com.seigneurin.controller;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.CreditCard;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.FundingInstrument;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.Transaction;
import com.paypal.core.ConfigManager;
import com.paypal.core.rest.APIContext;
import com.paypal.core.rest.OAuthTokenCredential;
import com.paypal.core.rest.PayPalRESTException;

import com.seigneurin.dto.PaymentDTO;

@Controller
public class PaymentController {

    static Logger logger = Logger.getLogger(PaymentController.class);

    @RequestMapping(value = "/pay", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public String pay(@RequestBody PaymentDTO paymentRequest) throws JsonProcessingException, PayPalRESTException {

        ObjectMapper mapper = new ObjectMapper();
        logger.info("Received payment request: " + mapper.writeValueAsString(paymentRequest));

        String clientID = ConfigManager.getInstance().getValue("clientID");
        String clientSecret = ConfigManager.getInstance().getValue("clientSecret");
        OAuthTokenCredential tokenCredential = new OAuthTokenCredential(clientID, clientSecret);

        String accessToken = tokenCredential.getAccessToken();

        CreditCard creditCard = new CreditCard();
        creditCard.setNumber("4417119669820331");
        creditCard.setType("visa");
        creditCard.setExpireMonth(11);
        creditCard.setExpireYear(2018);
        creditCard.setCvv2("874");
        creditCard.setFirstName("Joe");
        creditCard.setLastName("Shopper");
        // creditCard.setBillingAddress(billingAddress)

        Details amountDetails = new Details();
        amountDetails.setSubtotal("7.41");
        amountDetails.setTax("0.03");
        amountDetails.setShipping("0.03");

        Amount amount = new Amount();
        amount.setTotal("7.47");
        amount.setCurrency("USD");
        amount.setDetails(amountDetails);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("This is the payment transaction description.");

        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        FundingInstrument fundingInstrument = new FundingInstrument();
        fundingInstrument.setCreditCard(creditCard);

        ArrayList<FundingInstrument> fundingInstruments = new ArrayList<FundingInstrument>();
        fundingInstruments.add(fundingInstrument);

        Payer payer = new Payer();
        payer.setFundingInstruments(fundingInstruments);
        payer.setPaymentMethod("credit_card");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        APIContext apiContext = new APIContext(accessToken);

        Payment createdPayment = payment.create(apiContext);
        logger.info("Created payment: " + createdPayment.getId() + " " + createdPayment.getState());

        return createdPayment.getId() + " " + createdPayment.getState();
        // return "PAY-4B384645BV241280CKI5PW3Y approved";
    }
}