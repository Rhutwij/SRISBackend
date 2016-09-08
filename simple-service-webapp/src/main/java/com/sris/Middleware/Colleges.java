package com.sris.Middleware;

import org.apache.commons.dbcp2.Utils;

import com.sris.dao.MySqlDao;
import com.sris.util.ToJSON;
import com.google.gson.JsonArray;
import com.sris.Middleware.Users;

import java.sql.*;

/*
 * @author  Rhutwij tulankar
 * @version 1.0
 * @since   2015-03-31
 * class that contains college DAO 
 */
public class Colleges extends MySqlDao implements CollegesImp {

	/**
	 * getCollegeList
	 * 
	 * Method to getCollegeList
	 * 
	 * @return String college list
	 */
	public String getCollegeList() {
		PreparedStatement query = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;

		try {
			conn = getConnectionWithPooling();
			query = conn
					.prepareStatement("SELECT CollegeId,Name,Type FROM Colleges WHERE NAME!='ALL'");
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			log.info("Exception thrown method getCollegeList" + e.getMessage());
			returnString = "Service Not Available";
		}
		return returnString;
	}

	/**
	 * getCollegeListById
	 * 
	 * Method to get college by id
	 * 
	 * @param int id collegeid
	 * @return String college info
	 */
	public String getCollegeListById(int id) {
		PreparedStatement query = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;

		try {
			conn = getConnectionWithPooling();
			query = conn
					.prepareStatement("SELECT CollegeId,Name,Type FROM Colleges WHERE Colleges.CollegeId=?");
			query.setInt(1, id);
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			log.info("Exception thrown method getCollegeListById"
					+ e.getMessage());
			returnString = "Service Not Available or college not found";
		} finally {
			Utils.closeQuietly(conn);
		}
		log.info("Returning CollegeInfo");
		return returnString;
	}

	/**
	 * getCollegeProfessorsById
	 * 
	 * Method to get college profs by id
	 * 
	 * @param int CollegeIdid collegeid
	 * @return String college professor info
	 */
	@Override
	public String getCollegeProfessorsById(int CollegeId) {
		// TODO Auto-generated method stub select * from users where RoleId=3
		// and Ban=0 and CollegeId=4
		PreparedStatement query = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;

		try {
			conn = getConnectionWithPooling();
			query = conn
					.prepareStatement("SELECT Username,UserId,CollegeId FROM users WHERE RoleId=3 AND Ban=0 and CollegeId=?");
			query.setInt(1, CollegeId);
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			log.info("Exception thrown method getCollegeProfessorsById"
					+ e.getMessage());
			returnString = "Service Not Available or college not found";
		} finally {
			Utils.closeQuietly(conn);
		}
		log.info("Returning professor list");
		return returnString;
	}

	/**
	 * addCollege
	 * 
	 * Method to addCollege
	 * 
	 * @param String
	 *            name
	 * @param String
	 *            type
	 * @param int userid user who added college
	 * @return boolean true or false
	 */
	@Override
	public boolean addCollege(String name, String type, int userid) {
		// TODO Auto-generated method stub
		if (new Users().isSuperUser(userid) != 0) {
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;

			try {
				conn = getConnectionWithPooling();

				query = "INSERT IGNORE INTO colleges(Name,Type) Values(?,?)";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setString(1, name);
				preparedStatement.setString(2, type);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
			} catch (Exception e) {
				log.info("Exception thrown in addCollege" + e.getMessage());
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}
		return true;
	}

	/**
	 * editCollege
	 * 
	 * Method to editCollege
	 * 
	 * @param String
	 *            name
	 * @param String
	 *            type
	 * @param int collegeid
	 * @param int userid user who added college
	 * @return boolean true or false
	 */
	@Override
	public boolean editCollege(String name, String type, int userid,
			int collegeid) {
		// TODO Auto-generated method stub
		if (new Users().isSuperUser(userid) != 0) {
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;

			try {
				conn = getConnectionWithPooling();

				query = "UPDATE colleges SET Name=?,Type=? WHERE Collegeid=?";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setString(1, name);
				preparedStatement.setString(2, type);
				preparedStatement.setInt(3, collegeid);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
			} catch (Exception e) {
				log.info("Exception thrown in editCollege" + e.getMessage());
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}
		return true;
	}

	/**
	 * deleteCollege
	 * 
	 * Method to deleteCollege by id
	 * 
	 * @param int collegeidid
	 * @param int userid
	 * @return boolean true or false
	 */
	@Override
	public boolean deleteCollege(int userid, int collegeid) {
		// TODO Auto-generated method stub
		if (new Users().isSuperUser(userid) != 0) {
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;

			try {
				conn = getConnectionWithPooling();

				query = "DELETE FROM colleges WHERE Collegeid=?";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setInt(1, collegeid);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
			} catch (Exception e) {
				log.info("Exception thrown in deleteCollege" + e.getMessage());
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}
		return true;
	}
}
