package com.seigneurin.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.core.rest.APIContext;
import com.paypal.core.rest.PayPalRESTException;

//import java.util.ArrayList;
//
//import org.apache.log4j.Logger;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.paypal.api.payments.Amount;
//import com.paypal.api.payments.CreditCard;
//import com.paypal.api.payments.Details;
//import com.paypal.api.payments.FundingInstrument;
//import com.paypal.api.payments.Payer;
//import com.paypal.api.payments.Payment;
//import com.paypal.api.payments.Transaction;
//import com.paypal.core.ConfigManager;
//import com.paypal.core.rest.APIContext;
//import com.paypal.core.rest.OAuthTokenCredential;
//import com.paypal.core.rest.PayPalRESTException;
//
//import com.seigneurin.dto.PaymentDTO;
//
@Controller
public class PaypalPaymentController {

    static Logger logger = Logger.getLogger(PaypalPaymentController.class);

    static Map<String, PaymentDetails> paymentDetailsMap = new HashMap<String, PaymentDetails>();

    @RequestMapping(value = "/payWithPaypal", method = RequestMethod.GET)
    @ResponseBody
    public View pay(HttpServletRequest request) throws PayPalRESTException {

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

        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath();
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setReturnUrl(baseUrl + "/web/paypalPaymentApproved");
        redirectUrls.setCancelUrl(baseUrl + "/web/paypalPaymentCanceled");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);
        payment.setRedirectUrls(redirectUrls);

        APIContext apiContext = PaypalUtils.getAPIContext();

        Payment createdPayment = payment.create(apiContext);

        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.id = createdPayment.getId();
        for (Links link : createdPayment.getLinks()) {
            if ("approval_url".equals(link.getRel()))
                paymentDetails.approvalUrl = link.getHref();
            if ("execute".equals(link.getRel()))
                paymentDetails.executeUrl = link.getHref();
        }
        String token = paymentDetails.approvalUrl.substring(paymentDetails.approvalUrl.indexOf("token=")
                + "token=".length());
        paymentDetailsMap.put(token, paymentDetails);

        logger.info("Created Paypal payment:");
        logger.info("- id: " + createdPayment.getId());
        logger.info("- token: " + token);
        logger.info("- state: " + createdPayment.getState());
        logger.info("- approval URL: " + paymentDetails.approvalUrl);
        logger.info("- execute URL: " + paymentDetails.executeUrl);

        return new RedirectView(paymentDetails.approvalUrl);
    }

    @RequestMapping(value = "/paypalPaymentApproved", method = RequestMethod.GET)
    @ResponseBody
    public View paymentApproved(@RequestParam("token") String token, @RequestParam("PayerID") String payerId,
            HttpServletRequest request) throws PayPalRESTException {

        PaymentDetails paymentDetails = paymentDetailsMap.get(token);

        APIContext apiContext = PaypalUtils.getAPIContext();

        PaymentExecution paymentExecution = new PaymentExecution(payerId);

        Payment payment = new Payment();
        payment.setId(paymentDetails.id);
        Payment executedPayment = payment.execute(apiContext, paymentExecution);

        logger.info("Executed Paypal payment:");
        logger.info("- id: " + executedPayment.getId());
        logger.info("- state: " + executedPayment.getState());

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath();

        if (!"approved".equals(executedPayment.getState()))
            return new RedirectView(baseUrl + "/paypalPaymentFailed.html");
        return new RedirectView(baseUrl + "/paypalPaymentApproved.html");
    }

    @RequestMapping(value = "/paypalPaymentCanceled", method = RequestMethod.GET)
    @ResponseBody
    public View paymentCanceled(@RequestParam("token") String token, HttpServletRequest request) {

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath();
        return new RedirectView(baseUrl + "/paypalPaymentFailed.html");
    }

    class PaymentDetails {
        String id;
        String approvalUrl;
        String executeUrl;
    }
}