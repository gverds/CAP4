package com.iisigroup.cap.mvc.auth.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.tcb.ecol.core.utils.IisiSSLUtils;

/**
 * Trust All SSL certificate
 * 
 * @author TimCiang
 * @since  <li>2021/04/14</li>
 * @version	<li>2021/04/14,Tim,new
 *
 */
public class UnsecuredSSLSocketFactory extends SSLSocketFactory {
	private SSLSocketFactory socketFactory;

	public UnsecuredSSLSocketFactory() {
		try {
			socketFactory = IisiSSLUtils.getAllTrustSSLSocketFactory("TLS");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static SocketFactory getDefault() {
		return new UnsecuredSSLSocketFactory();
	}

	@Override
	public Socket createSocket(final String s, final int i) throws IOException {
		return socketFactory.createSocket(s, i);
	}

	@Override
	public Socket createSocket(final String s, final int i, final InetAddress inetAddress, final int i1)
			throws IOException {
		return socketFactory.createSocket(s, i, inetAddress, i1);
	}

	@Override
	public Socket createSocket(final InetAddress inetAddress, final int i) throws IOException {
		return socketFactory.createSocket(inetAddress, i);
	}

	@Override
	public Socket createSocket(final InetAddress inetAddress, final int i, final InetAddress inetAddress1, final int i1)
			throws IOException {
		return socketFactory.createSocket(inetAddress, i, inetAddress1, i1);
	}

	@Override
	public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
		return socketFactory.createSocket(s, host, port, autoClose);
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return socketFactory.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return socketFactory.getSupportedCipherSuites();
	}

}
