package com.cauc.chat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.derby.tools.sysinfo;

public class TestKeyStore {
public static void main(String[] args) throws Exception {
	System.out.println(System.getProperty("javax.net.ssl.keyStore"));
	String passphrase="654321";
	KeyStore keyStore=KeyStore.getInstance("JCEKS");
	char[] password=passphrase.toCharArray();
	keyStore.load(new FileInputStream("test.keys"), password);
	
	
    KeyManagerFactory keyManagerFactory=KeyManagerFactory.getInstance("SunX509");
    keyManagerFactory.init(keyStore, password); //password不能省略必须得有
    KeyManager[] keyManagers=keyManagerFactory.getKeyManagers();
    
    TrustManagerFactory trustManagerFactory =TrustManagerFactory.getInstance("SunX509");
    trustManagerFactory.init(keyStore);
    TrustManager[] trustManagers=trustManagerFactory.getTrustManagers();
    //trustManagers不需要保护
    
    SSLContext sslContext=SSLContext.getInstance("TLS");
    sslContext.init(keyManagers, trustManagers, null);
    //第三个是随机种子值，null为使用系统自带的
    
}
}
