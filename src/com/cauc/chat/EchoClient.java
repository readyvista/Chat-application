package com.cauc.chat;

import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.Arrays;
import java.util.stream.Stream;

public class EchoClient {
	private String host = "localhost";
	private int port = 8000;
	private SSLSocket socket;

	public EchoClient()throws Exception{		
		
		String passphrase = "654321";
		char[] password = passphrase.toCharArray();
		String trustStoreFile = "test.keys";    
		KeyStore ts = KeyStore.getInstance("JCEKS");
		ts.load(new FileInputStream(trustStoreFile), password);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ts); //客户端用的是别人的证书，当然没有私钥。
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, tmf.getTrustManagers(), null);	//对于客户端来说，不需要向服务器出示证书，第一个参数为空；第二个是获取信任证书；第三个参数是随机种子	
		SSLSocketFactory factory=sslContext.getSocketFactory();
		socket=(SSLSocket)factory.createSocket(host,port);		
		String[] supportedSuites=socket.getSupportedCipherSuites();
		Stream.of(supportedSuites).forEach(x -> System.out.println(x));		
		socket.setEnabledCipherSuites(supportedSuites);
		System.out.println(socket.getUseClientMode()? "客户模式":"服务器模式");
		socket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
			@Override
			public void handshakeCompleted(HandshakeCompletedEvent event) {				
				System.out.println(event.getCipherSuite());				
				System.out.println(event.getSession().getPeerHost());
			}
		});
	}

	public static void main(String args[]) throws Exception {
		new EchoClient().talk();
	}

	private PrintWriter getWriter(Socket socket) throws IOException {
		OutputStream socketOut = socket.getOutputStream();
		return new PrintWriter(socketOut, true);
	}

	private BufferedReader getReader(Socket socket) throws IOException {
		InputStream socketIn = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(socketIn));
	}

	public void talk() throws IOException {
		try {
			BufferedReader br = getReader(socket);
			PrintWriter pw = getWriter(socket);
			BufferedReader localReader = new BufferedReader(
					new InputStreamReader(System.in));
			String msg = null;
			while ((msg = localReader.readLine()) != null) {

				pw.println(msg);
				System.out.println(br.readLine());

				if (msg.equals("bye"))
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

/****************************************************
 * 作者：孙卫琴 * 来源：<<Java网络编程精解>> * 技术支持网址：www.javathinker.org *
 ***************************************************/
