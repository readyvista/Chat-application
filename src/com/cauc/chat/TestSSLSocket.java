package com.cauc.chat;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TestSSLSocket {
	public static void main(String[] args)throws Exception{
		SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		SSLSocket socket=(SSLSocket)factory.createSocket("www.baidu.com", 443);
		String strongSuits[]=new String[] {"TLS_DH_anon_WITH_AES_128_CBC_SHA256",
		"TLS_ECDH_anon_WITH_AES_128_CBC_SHA",
		"TLS_DH_anon_WITH_AES_128_CBC_SHA"};
		socket.setEnabledCipherSuites(strongSuits);
		String[] suits=socket.getEnabledCipherSuites();
		for(String suit:suits) {
			System.out.println(suit);
		}
		socket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
			
			@Override
			public void handshakeCompleted(HandshakeCompletedEvent evt) {
				// TODO Auto-generated method stub
				System.out.println(evt.getCipherSuite());
				System.out.println(evt.getSession().getCreationTime());
				
			}
		});
		System.out.println(socket.getEnabledCipherSuites());
		
		
		
	}
	

}
