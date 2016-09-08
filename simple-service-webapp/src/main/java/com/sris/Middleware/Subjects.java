package com.sris.Middleware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbcp2.Utils;

import com.google.gson.JsonArray;
import com.sris.dao.MySqlDao;
import com.sris.util.ToJSON;

/*
 * @author  Rhutwij tulankar
 * @version 1.0
 * @since   2015-03-31
 * class that contains Subjects DAO
 */
public class Subjects extends MySqlDao implements SubjectsImp {

	/**
	 * getSubjectList
	 * 
	 * Method to getSubjectList
	 * 
	 * @param int collegeid
	 * @return String subject list
	 */
	@Override
	public String getSubjectList(int collegeid) {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;

		try {
			conn = getConnectionWithPooling();
			query = conn
					.prepareStatement("SELECT SubjectId,Name FROM subjects WHERE CollegeId=?");
			query.setInt(1, collegeid);
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			log.info("Exception thrown method getSubjectList" + e.getMessage());
			returnString = "Service Not Available or college not found";
		} finally {
			Utils.closeQuietly(conn);
		}
		log.info("Returning SubjectList by collegeid");
		return returnString;
	}

	/**
	 * getSubjectList
	 * 
	 * Method to getSubjectList
	 * 
	 * @return String subject list
	 */
	@Override
	public String getSubjectList() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;

		try {
			conn = getConnectionWithPooling();
			query = conn
					.prepareStatement("SELECT SubjectId,Name FROM subjects");
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			log.info("Exception thrown method getSubjectList" + e.getMessage());
			returnString = "Service Not Available or college not found";
		} finally {
			Utils.closeQuietly(conn);
		}
		log.info("Returning SubjectList");
		return returnString;
	}

	/**
	 * addSubject
	 * 
	 * Method to addSubject
	 * 
	 * @oaram String name
	 * @param int userid
	 * @return boolean true or false
	 */
	@Override
	public boolean addSubject(String name, int userid) {
		// TODO Auto-generated method stub
		if (new Users().isSuperUser(userid) != 0) {
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;

			try {
				conn = getConnectionWithPooling();

				query = "INSERT IGNORE INTO subjects(Name) Values(?)";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setString(1, name);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
			} catch (Exception e) {
				log.info("Exception thrown in addSubject" + e.getMessage());
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}
		return true;
	}

	/**
	 * editSubject
	 * 
	 * Method to editSubject
	 * 
	 * @oaram String name
	 * @param int userid
	 * @param int subjectid
	 * @return boolean true or false
	 */
	@Override
	public boolean editSubject(String name, int userid, int subjectid) {
		// TODO Auto-generated method stub
		if (new Users().isSuperUser(userid) != 0) {
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;

			try {
				conn = getConnectionWithPooling();

				query = "UPDATE subjects SET Name=? WHERE SubjectId=?";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setString(1, name);
				preparedStatement.setInt(2, subjectid);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
			} catch (Exception e) {
				log.info("Exception thrown in editSubject" + e.getMessage());
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}
		return true;
	}

	/**
	 * deleteSubject
	 * 
	 * Method to deleteSubject
	 * 
	 * @param int userid
	 * @param int subjectid
	 * @return boolean true or false
	 */
	@Override
	public boolean deleteSubject(int userid, int subjectid) {
		// TODO Auto-generated method stub
		if (new Users().isSuperUser(userid) != 0) {
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;

			try {
				conn = getConnectionWithPooling();

				query = "DELETE FROM subjects WHERE SubjectId=?";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setInt(1, subjectid);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
			} catch (Exception e) {
				log.info("Exception thrown in deleteSubject" + e.getMessage());
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}
		return true;
	}

}
