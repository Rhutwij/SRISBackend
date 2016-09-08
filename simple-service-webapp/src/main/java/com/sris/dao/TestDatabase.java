package com.sris.dao;

import com.google.gson.JsonArray;

import java.sql.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.dbcp2.Utils;

import com.sris.util.ToJSON;

/*
 * @author  Rhutwij tulankar
 * @version 1.0
 * @since   2015-03-31
 * class test class to check database connection and database health
 */
public class TestDatabase {

	public String returnTableData() throws Exception {
		Connection conn = null;
		String selectSQL = null;
		ResultSet rs = null;
		PreparedStatement query = null;
		String returnstring = "";
		try {
			conn = new MySqlDao().getConnectionWithPooling();
			selectSQL = "Show tables";
			query = conn.prepareStatement(selectSQL);
			rs = query.executeQuery(selectSQL);
			while (rs.next()) {
				returnstring += "<p>" + rs.getString("Tables_in_sris") + "</p>";
			}
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			returnstring = "<p>No connection</p>" + e.getStackTrace()
					+ "Message" + e.getMessage();
		} finally {
			if (conn != null)
				Utils.closeQuietly(conn);
		}
		return "query executed:" + returnstring;
	}

	public Response returnColleges() throws Exception {
		java.sql.PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		Connection conn = null;
		String query = null;
		String returnString = null;
		Response rb = null;
		try {
			conn = new MySqlDao().getConnectionWithPooling();
			query = "Select * from Colleges";
			preparedStatement = conn.prepareStatement(query);
			rs = preparedStatement.executeQuery(query);
			ToJSON converter = new ToJSON();
			JsonArray json = new JsonArray();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			rb = Response.ok(returnString).build();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					Utils.closeQuietly(conn);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return rb;
	}

	public Response queryReturnTableDataQP(int id) throws Exception {
		PreparedStatement query = null;
		Response rb = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;

		try {
			conn = new MySqlDao().getConnectionWithPooling();
			query = conn
					.prepareStatement("SELECT Name,Type FROM Colleges WHERE CollegeId= ?");
			query.setInt(1, id);
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			rb = Response.ok(returnString).build();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(" ERROR" + e.getStackTrace());
			rb = Response.status(400).entity("Error: method needs argument id")
					.build();
		}
		return rb;
	}

	public int addCollege(String Name, String Type) throws Exception {
		java.sql.PreparedStatement preparedStatement = null;
		Connection conn = null;
		String query = null;
		String returnString = null;
		try {
			conn = new MySqlDao().getConnectionWithPooling();
			query = "Insert IGNORE INTO Colleges (Name,Type) VALUES(?,?)";
			preparedStatement = conn.prepareStatement(query);
			preparedStatement.setString(1, Name);
			preparedStatement.setString(2, Type);
			preparedStatement.executeUpdate();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			e.printStackTrace();
			return 500;
		} finally {
			if (conn != null)
				Utils.closeQuietly(conn);
		}
		return 200;
	}

	public int updateCollege(String Name, String Type, int id) throws Exception {
		java.sql.PreparedStatement preparedStatement = null;
		Connection conn = null;
		String query = null;
		String returnString = null;
		try {
			conn = new MySqlDao().getConnectionWithPooling();
			query = "UPDATE Colleges SET Name=?,Type=? WHERE CollegeId=?";
			preparedStatement = conn.prepareStatement(query);
			preparedStatement.setString(1, Name);
			preparedStatement.setString(2, Type);
			preparedStatement.setInt(3, id);
			preparedStatement.executeUpdate();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			e.printStackTrace();
			return 500;
		} finally {
			if (conn != null)
				Utils.closeQuietly(conn);
		}
		return 200;
	}

	public int deleteCollege(String Name) throws Exception {
		java.sql.PreparedStatement preparedStatement = null;
		Connection conn = null;
		String query = null;
		String returnString = null;
		try {
			conn = new MySqlDao().getConnectionWithPooling();
			query = "DELETE FROM Colleges WHERE Name=?";
			preparedStatement = conn.prepareStatement(query);
			preparedStatement.setString(1, Name);
			preparedStatement.executeUpdate();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			e.printStackTrace();
			return 500;
		} finally {
			if (conn != null)
				Utils.closeQuietly(conn);
		}
		return 200;
	}
}
