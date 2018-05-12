package com.cauc.chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.apache.derby.impl.store.raw.xact.TransactionTableEntry;

public class Message implements Serializable {
	private String srcUser;
	private String dstUser;

	public Message(String srcUser, String dstUser) {
		this.srcUser = srcUser;
		this.dstUser = dstUser;
	}

	public Message(boolean loginState) {
		// TODO Auto-generated constructor stub
	}

	

	public Message(File fis) {
		// TODO Auto-generated constructor stub
	}

	public String getSrcUser() {
		return srcUser;
	}

	public void setSrcUser(String srcUser) {
		this.srcUser = srcUser;
	}

	public String getDstUser() {
		return dstUser;
	}

	public void setDstUser(String dstUser) {
		this.dstUser = dstUser;
	}

	public Message() {
		super();
		// TODO Auto-generated constructor stub
	}
}

class ChatMessage extends Message {
	private String msgContent;

	public ChatMessage(String srcUser, String dstUser, String msgContent) {
		super(srcUser, dstUser);
		this.msgContent = msgContent;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public boolean isPubChatMessage() {
		return getDstUser().equals("");
	}
}

class UserStateMessage extends Message {
	private boolean userOnline;

	public UserStateMessage(String srcUser, String dstUser, boolean userOnline) {
		super(srcUser, dstUser);
		this.userOnline = userOnline;
	}

	public boolean isUserOnline() {
		return userOnline;
	}

	public boolean isUserOffline() {
		return !userOnline;
	}

	public void setUserOnline(boolean userOnline) {
		this.userOnline = userOnline;
	}

	public boolean isPubUserStateMessage() {
		return getDstUser().equals("");
	}
}

class UserLoginMessage extends Message{

	String loginUserName;
	String loginPassword;
	
	public UserLoginMessage(String loginName, String password) {
		super(loginName,password);
		loginUserName=loginName;
		loginPassword=password;
	}
	
	public String getLoginName() {
		return loginUserName;
	}
	
	public String getPassword() {
		return loginPassword;
	}
	
	
}

class UserLoginStateMessage extends Message{
	boolean loginState;
	
	public UserLoginStateMessage(boolean loginState) {
		super(loginState);
		this.loginState=loginState;
	}
	
	public boolean getUserState() {
		return loginState;
	}
}

class UserRegisterMessage extends Message{
	String userName2Register=new String();
	String userPassword2Register=new String();
	String userPhoneNumber=new String();
	public UserRegisterMessage(String userName,String password,String phoneNumber) {
		super(userName, password);
		this.userName2Register=userName;
		this.userPassword2Register=password;
		this.userPhoneNumber=phoneNumber;
	}
	
	public String GetUserName() {
		return userName2Register;
	}
	
	public String GetUserPassword() {
		return userPassword2Register;
	}
	
	public String GetPhoneNumber() {
		return userPhoneNumber;
	}
	
}

class UserRegisterStateMessage extends Message{
	boolean userRegisterState;

	public UserRegisterStateMessage(boolean userRegisterState) {
		super(userRegisterState);
		// TODO Auto-generated constructor stub
		this.userRegisterState=userRegisterState;
	}

	public boolean GetUserRegisterStateMessage() {
		return userRegisterState;
	}
	
}



class FileRequestMessage extends Message{
	String srcUser;
	String fileName;
//	String socket2TransFile;
	String dstUser;
	String filepath;   //这是文件路径
//	Socket socket;
	
	
	public FileRequestMessage(String srcUser,String FileName,String filePath,String dstUser) {
		super(srcUser,dstUser);
		// TODO Auto-generated constructor stub
		this.srcUser=srcUser;
		this.dstUser=dstUser;
		this.fileName=FileName;
		this.filepath=filePath;
		
	}
	
	public String getSourceUserOfFile() {
		return srcUser;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getDstUser() {
		return dstUser;
	}
	
	public String get2SendFilePath() {
		return filepath;
	}
	
	
}



class FileConfirmMessage extends Message{
	boolean fileConfirmState;
	String dstUser;
	String srcUser;
	InetAddress inetAddress;
	int port;
	String path2send;
	
	
	public FileConfirmMessage(boolean fileTransferStateConfirm,String srcUser,String dstUser,InetAddress inetAddress,int port,String path2Send) {
		this.fileConfirmState=fileTransferStateConfirm;
		this.dstUser=dstUser;
		this.srcUser=srcUser;
		this.inetAddress=inetAddress;
		this.path2send=path2Send;
		this.port=port;
	}
	
	public FileConfirmMessage(boolean fileTransferStateConfirm) {
		this.fileConfirmState=fileTransferStateConfirm;
	}
	
	
	
	public FileConfirmMessage(boolean fileTransferStateConfirm, String srcUser, String dstUser) {
		// TODO Auto-generated constructor stub
		this.fileConfirmState=fileTransferStateConfirm;
		this.srcUser=srcUser;
		this.dstUser=dstUser;
	}

	public boolean getFileConfirmState() {
		return fileConfirmState;
	}
	
	public InetAddress getInetAddress() {
		return inetAddress;
	}
	
	public String getSrcUser() {
		return srcUser;
	}
	
	public String getDstUser() {
		return dstUser;
	}
	
	public String getPath2Send() {
		return path2send;
	}
	
	public int getPort() {
		return port;
	}
	
}

class ForcedOfflineMessage extends Message{
	boolean onlineStatus;
	String dstUser;
	public ForcedOfflineMessage(String dstUser,boolean status) {
		// TODO Auto-generated constructor stub
		this.dstUser=dstUser;
		this.onlineStatus=status;
	}
	
	public boolean getStatus() {
		return onlineStatus;
	}
	
	public String getDstUser() {
		return dstUser;
	}
	
}

