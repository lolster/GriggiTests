package com.griggi.app;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLHandler {
	// Connection URL Syntax: "jdbc:mysql://ipaddress:portnumber/db_name"
	String dbUrl = "jdbc:mysql://localhost:3306/authpuppy";

	// Database Username
	String username = "authpuppy";

	// Database Password
	String password = "authpuppydev";

	Connection con;
	Statement stmt;

	public SQLHandler() throws ClassNotFoundException, SQLException {
		// Load mysql jdbc driver
		Class.forName("com.mysql.jdbc.Driver");

		// Create Connection to DB
		con = DriverManager.getConnection(dbUrl, username, password);

		// Create Statement Object
		stmt = con.createStatement();
	}

	public List<String> queryExecute(String queryString, int colIndex) throws SQLException {
		// Execute the SQL Query. Store results in ResultSet
		ResultSet rs = stmt.executeQuery(queryString);

		List<String> result = new ArrayList<>();
		// While Loop to iterate through all data and print results
		while (rs.next()) {
			// System. out.println(rs.getString(0));
			result.add(rs.getString(colIndex));
		}

		return result;
	}
}
