package com.seigneurin.controller;

import com.paypal.core.ConfigManager;
import com.paypal.core.rest.APIContext;
import com.paypal.core.rest.OAuthTokenCredential;
import com.paypal.core.rest.PayPalRESTException;

public class PaypalUtils {

    public static APIContext getAPIContext() throws PayPalRESTException {
        String clientID = ConfigManager.getInstance().getValue("clientID");
        String clientSecret = ConfigManager.getInstance().getValue("clientSecret");
        OAuthTokenCredential tokenCredential = new OAuthTokenCredential(clientID, clientSecret);
        String accessToken = tokenCredential.getAccessToken();
        APIContext apiContext = new APIContext(accessToken);
        return apiContext;
    }
}
