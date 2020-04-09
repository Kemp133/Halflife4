package com.halflife3.Controller;

import java.sql.*;

public class DatabaseManager {
	public static Connection getConnection() {
		Connection c = null;
		try {
			Class.forName("org.postgresql.Driver");
			String url = "jdbc:postgresql://rogue.db.elephantsql.com:5432/nuzmlzpr".trim();
			c = DriverManager.getConnection(url, "nuzmlzpr", "pd7OdC_3BiVrAPNU68CETtFtBaqFxJFB");

			if (c != null) {
				System.out.println("Connection complete");
			} else {
				System.out.println("Connection failed");
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return c;
	}

	public static void closeConnections(Connection c, PreparedStatement p, ResultSet r) {
		if (c != null) {
			try {
				c.close();
			} catch (SQLException e) { /* ignored */}
		}
		if (p != null) {
			try {
				p.close();
			} catch (SQLException e) { /* ignored */}
		}
		if (r != null) {
			try {
				r.close();
			} catch (SQLException e) { /* ignored */}
		}
	}

	public static boolean confirmUser(Connection c, String username, String passwordEntered) {
		String saltCheck        = "";
		String securedPassword;
		byte[] securedPassword2 = new byte[0];

		PreparedStatement passwordStatement = null;
		ResultSet         rs                = null;

		try {
			//Creating the query
			String passwordQuery = "SELECT * FROM userdatascore WHERE \"name\" = '" + username + "'";
			//Creating the statement
			passwordStatement = c.prepareStatement(passwordQuery);
			//Executing the query
			rs = passwordStatement.executeQuery();
			while (rs.next()) {
				securedPassword2 = rs.getBytes("password");
				//region Code Related To Hashing
//				String testName = rs.getString(2);
//				saltCheck        = rs.getString("salt");
//				securedPassword2 = rs.getBytes("password");
//				System.out.println("Salt retrieved: " + saltCheck + " name: " + testName + " password: " + new String
//				(securedPassword2));
				//endregion
			}
			securedPassword = new String(securedPassword2);

			//region Create User Account With Hashing and Salting
			            /*Creating the query
            String queryDetails = "SELECT * FROM userdatascore WHERE \"name\" = '" + username + "'";
            //Creating the statement
            preparedStatement = c.prepareStatement(queryDetails);
            //Executing the query
            rsDetails = preparedStatement.executeQuery();
            while (rsDetails.next()) {
                System.out.println("here");
                securedPassword = rs.getString(5);
                System.out.println("Password retrieved: " + securedPassword);
            }*/

			// Generating new secure password with the salt retrieved from the database associated with the username
			//String newSecurePassword = Password.generateSecurePassword(passwordEntered, saltCheck);

			// Check if two passwords are equal and returns true if they are
			//return returnValue = newSecurePassword.equalsIgnoreCase(securedPassword);
			//endregion

			//Code to check password
			System.out.println("Password entered: " + passwordEntered);
			System.out.println("SecuredPassword: " + securedPassword);
			return securedPassword.equals(passwordEntered);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnections(c, passwordStatement, rs);
		}
		return false;
	}
}