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
	
	public List<ArrayList<String>> queryExecuteList(String q, int numCols) throws SQLException {
		List<ArrayList<String>> res = new ArrayList<>();
		ResultSet rs = stmt.executeQuery(q);
		while(rs.next()) {
			ArrayList<String> temp = new ArrayList<String>();
			for(int i = 0; i < numCols; i++) {
				temp.add(rs.getString(i+1));
			}
			res.add(temp);
		}
		return res;
	}

	public void queryExecuteUpdate(String q) throws SQLException {
		stmt.executeUpdate(q);
	}
}
