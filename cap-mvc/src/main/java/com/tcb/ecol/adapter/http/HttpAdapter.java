/* 
 * HttpAdapter.java
 * 
 * Copyright (c) 2020 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.tcb.ecol.adapter.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * TODO Write a short description on the purpose of the program
 * </pre>
 * 
 * @since 2020年4月26日
 * @author Lancelot
 * @version
 *          <ul>
 *          <li>2020年4月26日,Lancelot,new
 *          </ul>
 */
public class HttpAdapter extends IISIHttpAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    static {
        final Logger logger = LoggerFactory.getLogger(HttpAdapter.class);
        try {
            // 略過https
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
            SSLContext sc;
            sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, new TrustManager[] { new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    // TODO Auto-generated method stub
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    // TODO Auto-generated method stub
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    // TODO Auto-generated method stub
                    return new java.security.cert.X509Certificate[] {};
                }
            } }, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException e) {
            logger.warn("HttpAdapter initial KeyManagementException"); // logger.error(e.toString(), e);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("HttpAdapter initial NoSuchAlgorithmException"); // logger.error(e.toString(), e);
        }
    }

    public HttpURLConnection connect(String url, String method, Map<String, String> headers, int timeout) throws IOException {
        return super.connect(url, method, headers, timeout);
    }

    public String doPost(String url, String data, Charset charset, Map<String, String> headers, int timeout) {
        return super.doPost(url, data, charset, headers, timeout);
    }

    public String doGet(String url, Map<String, String> headers, int timeout) {
        return super.doGet(url, headers, timeout);
    }

    public ByteArrayOutputStream doGetFile(String url, Map<String, String> headers, int timeout) {
        return super.doGetFile(url, headers, timeout);
    }
}
