package com.cauc.chat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.apache.derby.tools.sysinfo;

public class SQLEnjectableUserDatabase {
	// ## DEFINE VARIABLES SECTION ##
	// define the driver to use
	String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	// the database name
	String dbName = "USERDB";
	// define the Derby connection URL to use
	String connectionURL = "jdbc:derby:" + dbName + ";create=true";
	Connection conn;

	public SQLEnjectableUserDatabase() {
		// ## LOAD DRIVER SECTION ##
		try {
			/*
			 * * Load the Derby driver.* When the embedded Driver is used this
			 * action start the Derby engine.* Catch an error and suggest a
			 * CLASSPATH problem
			 */
			Class.forName(driver); //加载JDBC驱动器
			System.out.println(driver + " loaded. ");
		} catch (java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
			System.out.println("\n    >>> Please check your CLASSPATH variable   <<<\n");
		}
		String createString = "create table USERTABLE " // 表名
				+ "(USERNAME varchar(20) primary key not null, " // 用户名
				+ "PWD varchar(20), " // 口令的HASH值
				+ "REGISTERTIME timestamp default CURRENT_TIMESTAMP)"; // 注册时间

		try {
			DriverManager.setLogWriter(new PrintWriter(new File("aaa.txt")));
			// Create (if needed) and connect to the database
			conn = DriverManager.getConnection(connectionURL);
			// Create a statement to issue simple commands.
			Statement s = conn.createStatement();
			// Call utility method to check if table exists.
			// Create the table if needed
			if (!checkTable(conn)) {
				System.out.println(" . . . . creating table USERTABLE");
				s.execute(createString);
			}
			s.close();
			System.out.println("Database openned normally");
		} catch (SQLException e) {
			errorPrint(e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Insert a new user into the USERTABLE table
	public boolean insertUser(String userName, String userPwd) {
		try {
			if (!userName.isEmpty() && !userPwd.isEmpty()) {//入口参数检查
				PreparedStatement psTest = conn.prepareStatement(
						"select * from USERTABLE where USERNAME=?",
						ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				psTest.setString(1, userName);
				ResultSet rs = psTest.executeQuery();
				rs.last();
				int n = rs.getRow();
				psTest.close();
				if (n == 0) {
					PreparedStatement psInsert = conn
							.prepareStatement("insert into USERTABLE values (?,?,?)");					
					psInsert.setString(1, userName);
					psInsert.setString(2, userPwd);
					psInsert.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
					psInsert.executeUpdate();
					psInsert.close();
					System.out.println("成功注册新用户" + userName);
					return true;
				}
			}
		} catch (SQLException e) {
			errorPrint(e);
		}
		System.out.println("用户" + userName + "已经存在");
		return false;
	}

	public boolean deleteUser(String userName, String userPwd) {
		if (checkUserPassword(userName, userPwd) == true) {
			try {
				PreparedStatement psDelete = conn
						.prepareStatement("delete from USERTABLE where USERNAME=?");
				psDelete.setString(1, userName);
				int n = psDelete.executeUpdate();
				psDelete.close();
				if (n > 0) {
					System.out.println("成功删除用户" + userName);
					return true;
				} else {
					System.out.println("删除用户" + userName + "失败");
					return false;
				}
			} catch (SQLException e) {
				errorPrint(e);
			}
		}
		return false;
	}

	// check if userName with password userPwd can logon
	public boolean checkUserPassword(String userName, String userPwd) {
		try {
			if (!userName.isEmpty() && !userPwd.isEmpty()) {				
				Statement psTest = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				
				String loginSQL = 
						"select * from USERTABLE where "
						+ "USERNAME='" + userName + "' and PWD='" + userPwd + "'";				
				System.out.println(loginSQL);
				ResultSet rs = psTest.executeQuery(loginSQL);
				rs.last();
				int n = rs.getRow();
				psTest.close();
				return n > 0 ? true : false;
			}
		} catch (SQLException e) {
			errorPrint(e);
		}
		return false;
	}

	// show the information of all users in table USERTABLE, should be called
	// before the program exited
	public void showAllUsers() {
		String printLine = "  ______________当前所有注册用户______________";
		try {
			Statement s = conn.createStatement();
			// Select all records in the USERTABLE table
			ResultSet users = s
					.executeQuery("select USERNAME, PWD, REGISTERTIME from USERTABLE order by REGISTERTIME");

			// Loop through the ResultSet and print the data
			System.out.println(printLine);
			while (users.next()) {
				System.out.println("User-Name: " + users.getString("USERNAME")
						+ " Pasword: " + users.getString("PWD")
						+ " Regiester-Time " + users.getTimestamp("REGISTERTIME"));
			}
			System.out.println(printLine);
			// Close the resultSet
			s.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 关闭数据库
	public void shutdownDatabase() {
		/***
		 * In embedded mode, an application should shut down Derby. Shutdown
		 * throws the XJ015 exception to confirm success.
		 ***/
		if (driver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
			boolean gotSQLExc = false;
			try {
				conn.close();
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
			} catch (SQLException se) {
				if (se.getSQLState().equals("XJ015")) {
					gotSQLExc = true;
				}
			}
			if (!gotSQLExc) {
				System.out.println("Database did not shut down normally");
			} else {
				System.out.println("Database shut down normally");
			}
		}
	}

	/*** Check for USER table ****/
	public boolean checkTable(Connection conTst) throws SQLException {
		try {
			Statement s = conTst.createStatement();
			s.execute("update USERTABLE set USERNAME= 'TEST', REGISTERTIME = CURRENT_TIMESTAMP where 1=3");
		} catch (SQLException sqle) {
			String theError = (sqle).getSQLState();
			// System.out.println("  Utils GOT:  " + theError);
			/** If table exists will get - WARNING 02000: No row was found **/
			if (theError.equals("42X05")) // Table does not exist
			{
				return false;
			} else if (theError.equals("42X14") || theError.equals("42821")) {
				System.out
						.println("checkTable: Incorrect table definition. Drop table USERTABLE and rerun this program");
				throw sqle;
			} else {
				System.out.println("checkTable: Unhandled SQLException");
				throw sqle;
			}
		}
		return true;
	}

	// Exception reporting methods with special handling of SQLExceptions
	static void errorPrint(Throwable e) {
		if (e instanceof SQLException) {
			SQLExceptionPrint((SQLException) e);
		} else {
			System.out.println("A non SQL error occured.");
			e.printStackTrace();
		}
	}

	// Iterates through a stack of SQLExceptions
	static void SQLExceptionPrint(SQLException sqle) {
		while (sqle != null) {
			System.out.println("\n---SQLException Caught---\n");
			System.out.println("SQLState:   " + (sqle).getSQLState());
			System.out.println("Severity: " + (sqle).getErrorCode());
			System.out.println("Message:  " + (sqle).getMessage());
			sqle.printStackTrace();
			sqle = sqle.getNextException();
		}
	}
}
