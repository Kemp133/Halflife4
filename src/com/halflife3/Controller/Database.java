package com.halflife3.Controller;

import com.halflife3.Controller.Exceptions.PasswordNotMatchingException;
import com.halflife3.Controller.Exceptions.UsernameAlreadyExistsException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;

public class Database {
    private static final String url = "jdbc:mariadb://localhost:3306/test";
    private static final String user = "root";
    private static final String password = "abc123";

    public static void main(String[] args) {
        try {
            SHA512HashAndSaltPair test = hashAndSaltPassword("ThisIsATest!");
            String testing = "";
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to the database successfully!!");
            addUser(connection, "testuser101", "testing", "testing");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void addUser(Connection conn, String username, String password, String passwordCheck) throws Exception {
        if (!password.equals(passwordCheck))
            throw new PasswordNotMatchingException("The given passwords do not match!");

        try {
            System.out.println("Checking if user exists");

            //Create the SQL to call on the server
            PreparedStatement checkUserExists = conn.prepareStatement("call test.p_username_exists(?)");
            //Set passed variables here
            checkUserExists.setString(1, username);
            //Execute the procedure (returns a bool, I think of whether it was successful or not
            checkUserExists.execute();
            //Get the result set from the query
            ResultSet val = checkUserExists.getResultSet();
            //Move onto the next row of the resultset
            val.next();
            //Get the first value from the result set
            boolean exists = val.getBoolean(1);

            if (exists)
                throw new UsernameAlreadyExistsException("Username already exists!");

            PreparedStatement addUser = conn.prepareStatement("call test.p_username_exists(?)");
//            addUser.setString(1, username);
//            addUser.setString(2, hashAndSaltPassword(password));
            addUser.execute();
            System.out.println("User added successfully!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static SHA512HashAndSaltPair hashAndSaltPassword(String password) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);

        return new SHA512HashAndSaltPair(salt, md.digest(password.getBytes(StandardCharsets.UTF_8)));
    }
}

class SHA512HashAndSaltPair {
    public byte[] salt;
    public byte[] hash;

    SHA512HashAndSaltPair() {
        salt = new byte[16];
        hash = new byte[64];
    }

    SHA512HashAndSaltPair(byte[] salt, byte[] hash) throws Exception {
        if (salt.length != 16 || hash.length != 64)
            throw new Exception("Either salt length != 16 bytes || hash length != 64 bytes!!!");

        this.salt = salt;
        this.hash = hash;
    }
}
