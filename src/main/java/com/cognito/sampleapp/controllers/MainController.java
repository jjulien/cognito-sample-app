package com.cognito.sampleapp.controllers;

import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentity;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClientBuilder;
import com.amazonaws.services.cognitoidentity.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@PropertySource("classpath:config.properties")
public class MainController {

    @Value("${USER_POOL_ID}")
    private String userPoolId;

    @Value("${IDENTITY_POOL_ID}")
    private String identityPoolId;

    @Value("${COGNITO_IDP_NAME}")
    private String cognitoIDPName;

    @Value("${ACCOUNT_NUMBER}")
    private String accountNumber;

    @Value("${REGION}")
    private String region;

    @Value("${CUSTOM_DOMAIN}")
    private String customDomain;

    @Value("${LOGIN_URL}")
    private String loginUrl;

    @Value("${CLIENT_APP_ID}")
    private String clientAppId;

    @Value("${ASSUME_ROLE_ARN}")
    private String assumeRoleArn;

    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

    @GetMapping("/")
    public ModelAndView index(Model model) {
        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/login")
    public String login(@RequestParam Map<String, String> queryParameters, HttpServletRequest request, Model model) {
        model.addAttribute("loginurl", getLoginUrl());
        LOGGER.debug("Doing GET /login with URI " + request.getRequestURI() + " and URL " + request.getRequestURL());
        for (String key : queryParameters.keySet()) {
            LOGGER.debug(key + ": " + queryParameters.get(key));
        }
        if ( queryParameters.containsKey("error_description") ) {
            model.addAttribute("error", true);
            model.addAttribute("error_description", queryParameters.get("error_description"));
        };

        if ( queryParameters.containsKey("access_token")) {

        }
        return "login";
    }

    @GetMapping("/logout")
    public String logout(Model model) {
        return "logout";
    }

    @GetMapping("/s3")
    public String s3Browser(@RequestParam Map<String, String> queryParameters, @RequestHeader Map<String, String> headers, Model model) {
        LOGGER.debug("\n\nHeaders\n===============================================\n");
        for (String header : headers.keySet())  {
            LOGGER.debug(header + "=" + headers.get(header));
        }
        LOGGER.debug("\n\n");
        LOGGER.debug("\n\nQuery Parameters\n===============================================\n");
        for (String param : queryParameters.keySet()) {
            LOGGER.debug(param + "=" + queryParameters.get(param));
        }
        LOGGER.debug("\n\n");
        if (queryParameters.containsKey("access_token")) {
            DecodedJWT jwt = JWT.decode(queryParameters.get("access_token"));
            model.addAttribute("access_token_header", new String(Base64.getDecoder().decode(jwt.getHeader())));
            model.addAttribute("access_token_payload", new String(Base64.getDecoder().decode(jwt.getPayload())));
            model.addAttribute("access_token_signature", jwt.getSignature());
        }
        if (queryParameters.containsKey("id_token")) {
            DecodedJWT jwt = JWT.decode(queryParameters.get("id_token"));
            model.addAttribute("id_token_header", new String(Base64.getDecoder().decode(jwt.getHeader())));
            model.addAttribute("id_token_payload", new String(Base64.getDecoder().decode(jwt.getPayload())));
            model.addAttribute("id_token_signature", jwt.getSignature());
        }
        try {
            Credentials creds = getCredentialsFromToken(queryParameters.get("id_token"));
            AWSSessionCredentials sessionCredentials = new BasicSessionCredentials(
                    creds.getAccessKeyId(),
                    creds.getSecretKey(),
                    creds.getSessionToken()
            );
            AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(sessionCredentials)).build();
            GetCallerIdentityRequest request = new GetCallerIdentityRequest().withRequestCredentialsProvider(new AWSStaticCredentialsProvider(sessionCredentials));
            AWSSecurityTokenService client = AWSSecurityTokenServiceClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(sessionCredentials)).build();
            GetCallerIdentityResult result = client.getCallerIdentity(request);
            LOGGER.debug("Identity: " + result.getArn());

            List<Bucket> buckets = s3.listBuckets();
            model.addAttribute("all_buckets", buckets);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "s3";
    }

    public Credentials getCredentialsFromToken(String token) {
        Map<String,String> logins = new HashMap<>();
        logins.put(getUserPoolLoginKey(), token);

        AmazonCognitoIdentity cognitoIdentityClient = AmazonCognitoIdentityClientBuilder.defaultClient();
        GetIdRequest request = new GetIdRequest()
                .withIdentityPoolId(identityPoolId)
                .withAccountId(accountNumber)
                .withLogins(logins);

        GetIdResult result = cognitoIdentityClient.getId(request);
        LOGGER.debug("ID: " + result.getIdentityId());

        GetCredentialsForIdentityRequest getCredentialsRequest =
                new GetCredentialsForIdentityRequest()
                        .withIdentityId(result.getIdentityId())
                        .withCustomRoleArn(assumeRoleArn)
                        .withLogins(logins);
        GetCredentialsForIdentityResult getCredentialsResult = cognitoIdentityClient.getCredentialsForIdentity(getCredentialsRequest);
        return getCredentialsResult.getCredentials();
    }

    public String getUserPoolLoginKey() {
        return String.format("cognito-idp.%s.amazonaws.com/%s", region, userPoolId);
    }

    public String getLoginUrl() {
        String format = "https://%s.auth.%s.amazoncognito.com/oauth2/authorize?identity_provider=%s&redirect_uri=%s&response_type=TOKEN&client_id=%s&scope=openid";
        return String.format(format, customDomain, region, cognitoIDPName, URLEncoder.encode(loginUrl), clientAppId);
    }
}

