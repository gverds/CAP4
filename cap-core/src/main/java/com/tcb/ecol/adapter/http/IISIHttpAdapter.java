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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
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
public abstract class IISIHttpAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    static {
        final Logger logger = LoggerFactory.getLogger(IISIHttpAdapter.class);
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
            logger.error(e.toString());
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.toString());
        }
    }

    public HttpURLConnection connect(String url, String method, Map<String, String> headers, int timeout) throws IOException {
        URL netUrl;
        HttpURLConnection con = null;
        try {
            netUrl = new URL(url);
            con = (HttpURLConnection) netUrl.openConnection();
            con.setRequestMethod(method);
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);
            for (Entry<String, String> header : headers.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }
        } catch (Exception e) {
            throw new IOException(e.toString());
        }
        return con;
    }

    public String doPost(String url, String data, Charset charset, Map<String, String> headers, int timeout) {
        // 添加請求
        String result = null;
        int responseCode = -1;
        HttpURLConnection con = null;
        try {
            String ll1 = "url: " + url + ", data: " + data + "(1)";
            //logger.debug(ll1); //Log Forging(debug)
            con = connect(url, "POST", headers, timeout);
            if(con==null) {
                return result;
            }
            // 發送Post請求
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream());) {
                String ll2 = "url: " + url + ", data: " + data + "(2)";
                logger.debug(ll2);
                wr.write(data.getBytes(charset));
                wr.flush();
            } catch (Exception e) {
                //Log Forging(debug)
                //logger.debug("url: " + url + ", data: " + data + "(3)");
                logger.error(e.toString());
            }
            responseCode = con.getResponseCode();
            String encoding = con.getContentEncoding();
            String ll4 = "url: " + url + ", data: " + data + ", encoding: " + encoding + "(4)";
            //Log Forging(debug)
            //logger.debug(ll4);
            encoding = encoding == null ? (charset == null ? "UTF-8" : charset.name()) : encoding;
            //Unreleased Resource: Streams
            InputStream is = null;
            try {
                if (responseCode == 200 || responseCode == 201) {
                    is = con.getInputStream();
                    result = IOUtils.toString(is, encoding);
                } else {
                    is = con.getErrorStream();
                    result = IOUtils.toString(is, encoding);
                }
            }catch(IOException e) {
            }finally {
                if(is!=null) {
                    is.close();
                }
            }
        } catch (IOException e) {
            String ll5 = "url: " + url + ", data: " + data + "(5)";
            //logger.debug(ll5);  //Log Forging(debug)
            logger.error(e.toString());
        } finally {
            String ll6 = "url: " + url + ", data: " + data + "(6)";
            //logger.debug(ll6);  //Log Forging(debug)
            if (con != null) {
                con.disconnect();
            }
        }
        String ll7 = "url: " + url + ", data: " + data + "(7)";
        //logger.debug(ll7); //Log Forging(debug)
        return result;
    }

    public String doGet(String url, Map<String, String> headers, int timeout) {
        String result = null;
        int responseCode = -1;
        HttpURLConnection con = null;
        try {
            con = connect(url, "GET", headers, timeout);
            responseCode = con.getResponseCode();
        } catch (IOException e) {
            //e.printStackTrace();
            logger.error("[getResponseCode : ]" + e.toString());
        }

        if (responseCode == 200 && con != null) {
            if ("application/pdf".equals(headers.get("contentType"))) {

            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), con.getContentEncoding()));) {
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                    response.append(inputLine);
                }
                result = response.toString();
            } catch (Exception e) {
                //e.printStackTrace();
                logger.error("[doGet : ]" + e.toString());
            }
        }
        return result;
    }

    public ByteArrayOutputStream doGetFile(String url, Map<String, String> headers, int timeout) {
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        int responseCode = -1;
        HttpURLConnection con = null;
        try {
            con = connect(url, "GET", headers, timeout);
            responseCode = con.getResponseCode();
        } catch (IOException e) {
            //e.printStackTrace();
            logger.error("[getResponseCode : ]" + e.toString());
        }
        logger.error("[doGetFile responseCode: ]" + responseCode);
        if (responseCode == 200 && con != null) {
            try {
                is = con.getInputStream();
                logger.error("[doGetFile FileLength: ]" + is.available());
                baos = new ByteArrayOutputStream(is.available());
                byte[] buf = new byte[2000 * 1024];
                int nRead = 0;
                while ((nRead = is.read(buf)) != -1) {
                    baos.write(buf, 0, nRead);
                }
            } catch (Exception e) {
                // e.printStackTrace();
                logger.error("[doGetFile : ]" + e.toString());
            } finally {
                try {
                    //Null Dereference
                    if(is!=null) {
                        is.close();
                    }
                    if(baos!=null) {
                        baos.close();
                    }
                } catch (IOException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error(e.toString());
                    }
                }
            }
        }
        return baos;
    }
}
