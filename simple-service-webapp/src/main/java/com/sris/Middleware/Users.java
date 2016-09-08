package com.sris.Middleware;

import org.apache.commons.dbcp2.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.NamedList;
import org.codehaus.jettison.json.JSONArray;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import com.sris.dao.MySqlDao;
import com.sris.util.ToJSON;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudsearchdomain.model.Bucket;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.io.FileInputStream;
import java.nio.charset.Charset;

/*
 * @author  Rhutwij tulankar
 * @version 1.0
 * @since   2015-03-31
 * class that contains user DAO
 */
public class Users extends MySqlDao implements UsersImp {

	private String accessKey = "xxxxx";
	private String secretKey = "xxxx";
	private static Map<String, Map<String, Double>> transition; // previous tag
																// -> (current
																// tag ->
																// P(current|previous))
	private static Map<String, Map<String, Double>> emission; // tag -> (word ->
																// P(tag|word))
	private static final double unseen = -100; // unseen word penalty of -100
	private static final String start = "#"; // default start

	/**
	 * isUser
	 * 
	 * Method to check if valid user
	 * 
	 * @param String
	 *            userName
	 * @param Strinf
	 *            password
	 * @return boolean true or false
	 */
	@Override
	public boolean isUser(String userName, String password) {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;

		try {
			conn = getConnectionWithPooling();
			query = conn
					.prepareStatement("SELECT UserId FROM users WHERE Username=? AND Ban=0 AND Password=?");
			query.setString(1, userName);
			query.setString(2, password);
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			log.info("Exception thrown method getCollegeList" + e.getMessage());
			return false;
		}

		if (returnString == null || returnString.isEmpty()
				|| returnString.equals("[]")) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * registerUser
	 * 
	 * Method to register user
	 * 
	 * @param String
	 *            userName
	 * @param String
	 *            password
	 * @param int CollegeId
	 * @param int rating
	 * @param int roleId
	 * @return boolean true or false
	 */
	@Override
	public boolean registerUser(String userName, int CollegeId, int rating,
			String password, int roleId) {
		// TODO Auto-generated method stub
		boolean userExists = isUser(userName, password);
		if (userExists) {
			return true;
		} else {
			if (!userExists) {
				return false;
			}
			java.sql.PreparedStatement preparedStatement = null;
			Connection conn = null;
			String query = null;
			try {
				conn = getConnectionWithPooling();
				query = "INSERT IGNORE INTO users (Username,Password,RoleId,Ban,CollegeId,RegDate,LastLogin,Rating) "
						+ "VALUES(?,?,?,?,?,NOW(),NOW(),?)";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setString(1, userName);
				preparedStatement.setString(2, password);
				preparedStatement.setInt(3, roleId);
				preparedStatement.setInt(4, 0);
				preparedStatement.setInt(5, CollegeId);
				preparedStatement.setInt(6, rating);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
			} catch (Exception e) {
				e.printStackTrace();
				log.info("Exception while registering user" + e.getMessage());
				return false;
			} finally {
				if (conn != null)
					Utils.closeQuietly(conn);
			}
			return true;
		}

	}

	/**
	 * authenticateCreateBucketAction
	 * 
	 * Method to authenticateCreateBucketAction
	 * 
	 * @param int userid
	 * @param String
	 *            bucketname
	 * @param int subjectid
	 * @return boolean true or false
	 */
	@Override
	public boolean authenticateCreateBucketAction(int userid,
			String bucketname, int subjectid) {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		int userId = 0;
		int count = 0;

		try {
			conn = getConnectionWithPooling();
			// query to select check if user role is correct and user not banned
			query = conn
					.prepareStatement("SELECT UserId FROM users WHERE UserId=? AND Ban=0 and RoleId=3");
			query.setInt(1, userid);
			ResultSet rs = query.executeQuery();
			if (rs.next()) {
				userId = rs.getInt("UserId");
			}
			// query to select check if user role is correct and user not banned
			if (userId != 0) {
				query = conn
						.prepareStatement("SELECT count(*) as count FROM threads WHERE UserId=? AND SubjectId=?");
				query.setInt(1, userId);
				query.setInt(2, subjectid);
				ResultSet rs1 = query.executeQuery();
				if (rs1 != null && rs1.next()) {
					count = rs1.getInt("count");
				}
			}

			Utils.closeQuietly(conn);
		} catch (Exception e) {
			log.info("Exception thrown method authenticateCreateBucketAction"
					+ e.getMessage());
			return false;
		}

		if (0 == userId || count != 0) {
			log.info("user not authenticated" + userId);
			return false;
		} else {
			log.info("user authenticated" + userId);
			return true;
		}
	}

	/**
	 * getUserInfo
	 * 
	 * Method to getUserInfo
	 * 
	 * @param String
	 *            username
	 * @return String userinfo
	 */
	@Override
	public String getUserInfo(String username) {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;

		try {
			conn = getConnectionWithPooling();
			query = conn
					.prepareStatement("SELECT UserId,CollegeId,Ban FROM users WHERE Username=?");
			query.setString(1, username);
			log.info("username" + username);
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			log.info("Exception thrown method Userinfo" + e.getMessage());
			returnString = "Service Not Available or user not found";
		} finally {
			Utils.closeQuietly(conn);
		}
		log.info("Returning UserInfo" + username + "return String"
				+ returnString);
		return returnString;
	}

	/**
	 * getUserInfo
	 * 
	 * Method to getUserInfo
	 * 
	 * @param String
	 *            userid
	 * @return String userinfo
	 */
	public String getUserInfoById(int userid) {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;

		try {
			conn = getConnectionWithPooling();
			query = conn
					.prepareStatement("SELECT UserId,Username,CollegeId,Ban FROM users WHERE UserId=?");
			query.setInt(1, userid);
			log.info("userid" + userid);
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			log.info("Exception thrown method getUserInfoById" + e.getMessage());
			returnString = "Service Not Available or user not found";
		} finally {
			Utils.closeQuietly(conn);
		}
		log.info("Returning getUserInfoById" + userid + "return String"
				+ returnString);
		return returnString;
	}

	/**
	 * getBannedUsers
	 * 
	 * Method to getBannedUsers
	 * 
	 * @return String banned users
	 */
	public String getBannedUsers() {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;

		try {
			conn = getConnectionWithPooling();
			query = conn
					.prepareStatement("SELECT UserId,Username,RoleId,Ban FROM users WHERE Ban=1");
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			log.info("Exception thrown method getBannedUsers" + e.getMessage());
			returnString = "Service Not Available or user not found";
		} finally {
			Utils.closeQuietly(conn);
		}
		log.info("Returning getBannedUsers");
		return returnString;
	}

	/**
	 * getUnBannedUsers
	 * 
	 * Method to getUnBannedUsers
	 * 
	 * @return String unbanned users
	 */
	public String getUnBannedUsers() {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;

		try {
			conn = getConnectionWithPooling();
			query = conn
					.prepareStatement("SELECT UserId,Username,RoleId,Ban FROM users WHERE Ban=0");
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			log.info("Exception thrown method getUnBannedUsers"
					+ e.getMessage());
			returnString = "Service Not Available or user not found";
		} finally {
			Utils.closeQuietly(conn);
		}
		log.info("Returning getUnBannedUsers");
		return returnString;
	}

	/**
	 * createBucket
	 * 
	 * Method to createBucket
	 * 
	 * @param int userid
	 * @param int subjectid
	 * @param String
	 *            name
	 * @return boolean true or false
	 */
	@Override
	public boolean createBucket(int userid, int subjectid, String name) {
		// TODO Auto-generated method stub
		name = name.replace(' ', '-');
		name = name.replace('_', '-');
		boolean createBucket = authenticateCreateBucketAction(userid, name,
				subjectid);
		if (createBucket) {
			java.sql.PreparedStatement preparedStatement = null;
			Connection conn = null;
			String query = null;
			String query2 = null;
			String query3 = null;
			String bucketname = null;
			int threadid = 0;
			try {

				conn = getConnectionWithPooling();

				query = "INSERT IGNORE INTO threads (SubjectId,UserId,CreationDate,TotalDocs,Name) "
						+ "VALUES(?,?,NOW(),0,?)";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setInt(1, subjectid);
				preparedStatement.setInt(2, userid);
				preparedStatement.setString(3, name);
				preparedStatement.executeUpdate();
				log.info("created thread" + name);

				// createS3Bucket
				AWSCredentials credentials = new BasicAWSCredentials(accessKey,
						secretKey);
				AmazonS3 s3client = new AmazonS3Client(credentials);
				s3client.setRegion(Region.getRegion(Regions.US_WEST_1));
				bucketname = "sris" + userid + subjectid + name.toLowerCase();

				if (!s3client.doesBucketExist(bucketname)) {
					com.amazonaws.services.s3.model.Bucket bucket = s3client
							.createBucket(new CreateBucketRequest(bucketname));// name,userid,subjectid
					log.info("bucket created on s3" + bucket);
				}

				query2 = "SELECT ThreadId FROM Threads WHERE SubjectId=? AND UserId=? AND Name=?";
				preparedStatement = conn.prepareStatement(query2);
				preparedStatement.setInt(1, subjectid);
				preparedStatement.setInt(2, userid);
				preparedStatement.setString(3, name);
				ResultSet rs = preparedStatement.executeQuery();
				if (rs.next()) {
					threadid = rs.getInt("ThreadId");
				}

				// add to s3_buckets
				if (threadid != 0) {
					query3 = "INSERT IGNORE INTO S3_Buckets (ThreadId,Name,Date,Block,Owner) "
							+ "VALUES(?,?,NOW(),0,?)";
					preparedStatement = conn.prepareStatement(query3);
					preparedStatement.setInt(1, threadid);
					preparedStatement.setString(2, bucketname);
					preparedStatement.setString(3, userid + "");
					preparedStatement.executeUpdate();
					log.info("bucket added to s3_buckets" + bucketname);
				}

				Utils.closeQuietly(conn);
			} catch (Exception e) {
				e.printStackTrace();
				log.info("Exception while createBucket" + e.getMessage());
				return false;
			} finally {
				if (conn != null)
					Utils.closeQuietly(conn);
			}
			return true;
		} else {
			return false;
		}

	}

	/**
	 * getUserThreadsById
	 * 
	 * Method to getUserThreadsById
	 * 
	 * @param int userid
	 * @return String get user threads or buckets
	 */
	@Override
	public String getUserThreadsById(int userid) {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		String returnString = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		try {
			conn = getConnectionWithPooling();
			query = conn
					.prepareStatement("SELECT threads.Name,threads.UserId,users.CollegeId,threads.SubjectId,threads.ThreadId,S3_Buckets.BucketId FROM threads "
							+ "JOIN S3_Buckets ON S3_Buckets.ThreadId=threads.ThreadId JOIN users on users.UserId=threads.UserId "
							+ "WHERE threads.UserId=?");
			query.setInt(1, userid);
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			log.info("Exception thrown method getUserThreadsById"
					+ e.getMessage());
			returnString = "Service Not Available or thread not found";
		} finally {
			Utils.closeQuietly(conn);
		}
		return returnString;
	}

	/**
	 * postDocument
	 * 
	 * Method to postDocument
	 * 
	 * @param int bucketid
	 * @param int professorid
	 * @param int userid
	 * @param int subjectid
	 * @param int collegeid
	 * @param String
	 *            title
	 * @param String
	 *            desc
	 * @param InputStream
	 *            fileinputstream
	 * @param String
	 *            filename
	 * @return boolean true or false
	 */
	@Override
	public boolean postDocument(int bucketid, int professorid, int userid,
			int subjectid, int collegeid, String title, String desc,
			InputStream fileinputStream, String filename) {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		int userId = 0;
		String s3bucketname = null;
		int subjectIdDb = 0;
		String insertquery = null;
		String insertquery1 = null;
		long timeNow = System.currentTimeMillis();
		String s3key = "";
		String documentId = null;
		String postedDate = null;
		String username = null;
		int rating = 0;
		int hide = 0;
		try {
			conn = getConnectionWithPooling();

			// query to select check if user role is correct and user not banned
			query = conn
					.prepareStatement("SELECT UserId,Username FROM users WHERE UserId=? AND Ban=0");
			query.setInt(1, userid);
			ResultSet rs = query.executeQuery();
			if (rs.next()) {
				userId = rs.getInt("UserId");
				username = rs.getString("Username");
			}

			if (0 == userId) {
				return false;
			}

			// 2nd query to get bucketinfo
			query = conn
					.prepareStatement("SELECT S3_buckets.Name,threads.SubjectId"
							+ " FROM S3_buckets"
							+ " JOIN threads on threads.ThreadId=S3_buckets.ThreadId"
							+ " WHERE S3_buckets.BucketId=? AND S3_buckets.Block=0");
			query.setInt(1, bucketid);
			ResultSet rs1 = query.executeQuery();
			if (rs1.next()) {
				s3bucketname = rs1.getString("S3_buckets.Name");
				subjectIdDb = rs1.getInt("threads.SubjectId");
				subjectid = subjectIdDb;
			}

			if (s3bucketname != null) {
				// getting filename and storing the file to s3
				ByteArrayInputStream input = new ByteArrayInputStream(
						IOUtils.toByteArray(fileinputStream));
				log.info("input value here!!!" + input);
				String rawtext = IOUtils.toString(fileinputStream);
				// making s3key
				filename = filename.trim().replaceAll("\\s+", "");
				s3key = s3bucketname + "/" + timeNow + filename;

				// creating s3 object
				AmazonS3Client s3client = (AmazonS3Client) createS3Client();
				s3client.putObject(s3bucketname, timeNow + filename, input,
						new ObjectMetadata());
				s3client.setObjectAcl(s3bucketname, timeNow + filename,
						CannedAccessControlList.PublicRead);
				log.info("uploading to s3" + timeNow + filename + "bucket"
						+ s3bucketname);

				String s3Url = s3client.getResourceUrl(s3bucketname, timeNow
						+ filename);
				log.info("s3 url" + s3Url + " s3key" + s3key);

				// inserting document in documents table
				insertquery = "INSERT INTO documents (BucketId,keyname,Size,Date,UserId,Title,Rating,Hide,`Desc`) "
						+ "VALUES(?,?,85,NOW(),?,?,0,0,?)";
				PreparedStatement preparedStatement = conn
						.prepareStatement(insertquery);
				preparedStatement.setInt(1, bucketid);
				preparedStatement.setString(2, s3key);
				preparedStatement.setInt(3, userid);
				preparedStatement.setString(4, title);
				preparedStatement.setString(5, desc);
				preparedStatement.executeUpdate();
				log.info("inserted document" + s3key);

				S3Object object = s3client.getObject(new GetObjectRequest(
						s3bucketname, timeNow + filename));
				InputStream objectData = object.getObjectContent();
				String filecontent = IOUtils.toString(objectData, "UTF-8");
				String halfed="";
				if(filecontent.length()> 30)
				{
			        halfed = filecontent.substring(0, 30);
				}
				else
				{
					halfed = filecontent.substring(0, filecontent.length());
				}

				// getting nouns from text

				query = conn
						.prepareStatement("SELECT DocumentId,DATE_FORMAT(Date,'%Y-%m-%dT%TZ') as Date,rating,hide FROM documents WHERE keyname=?");
				query.setString(1, s3key);
				ResultSet rs2 = query.executeQuery();
				if (rs2.next()) {
					documentId = rs2.getString("DocumentId");
					postedDate = rs2.getString("Date");
					rating = rs2.getInt("rating");
					hide = rs2.getInt("hide");
				}
				/*
				 * //inserting into Index table for search insertquery1 =
				 * "INSERT IGNORE INTO index_table (Title,Rating,Description,Date,Url,UserId,rawtext,Subject,CollegeId,BucketId) "
				 * + "VALUES(?,0,?,NOW(),?,?,?,?,?,?)"; PreparedStatement
				 * preparedStatement1 = conn.prepareStatement(insertquery1);
				 * preparedStatement1.setString(1, title);
				 * preparedStatement1.setString(2, desc);
				 * preparedStatement1.setString(3, s3key);
				 * preparedStatement1.setInt(4, userid);
				 * preparedStatement1.setString(5, "empty");
				 * preparedStatement1.setString(6, subjectid+"");
				 * preparedStatement1.setInt(7, collegeid);
				 * preparedStatement1.setInt(8, bucketid);
				 * preparedStatement1.executeUpdate();
				 * log.info("inserting in index table for search" + s3key);
				 */
				boolean resp = addSolrDocs(documentId, bucketid, collegeid,
						userid, title, desc, s3Url, postedDate, rating, hide,
						username, s3bucketname, halfed);
				log.info("done adding to solr");

				Utils.closeQuietly(conn);
				if (resp) {
					return true;
				} else {
					return false;
				}

			} else {
				return false;
			}
		} catch (Exception e) {
			log.info("Exception thrown method postDocument" + e.getStackTrace());
			return false;
		} finally {
			Utils.closeQuietly(conn);
		}

	}

	/**
	 * createS3Client
	 * 
	 * Method to createS3Client
	 * 
	 * @return AmazonS3 S3Client
	 */
	public AmazonS3 createS3Client() {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey,
				secretKey);
		AmazonS3 s3client = new AmazonS3Client(credentials);
		s3client.setRegion(Region.getRegion(Regions.US_WEST_1));
		return s3client;

	}

	/**
	 * addSolrDocs
	 * 
	 * Method to addSolrDocs
	 * 
	 * @param int bucketid
	 * @param int professorid
	 * @param int userid
	 * @param int subjectid
	 * @param int collegeid
	 * @param String
	 *            title
	 * @param String
	 *            desc
	 * @param String
	 *            url
	 * @param String
	 *            lastmodified
	 * @param int rating
	 * @param int hide
	 * @param String
	 *            username
	 * @param String
	 *            s3bucketname
	 * @param String
	 *            rawtext
	 * @return boolean true or false
	 */
	public boolean addSolrDocs(String id, int bucketid, int collegeid,
			int userid, String title, String desc, String url,
			String lastmodified, int rating, int hide, String username,
			String s3bucketname, String rawtext) throws Exception {
		HttpSolrServer server = new HttpSolrServer("http://localhost:8983/solr");
		SolrInputDocument doc1 = new SolrInputDocument();
		doc1.addField("id", id);
		doc1.addField("bucketid", bucketid);
		doc1.addField("collegeid", collegeid);
		doc1.addField("userid", userid);
		doc1.addField("title", title);
		doc1.addField("rawtext", rawtext);
		doc1.addField("description", desc);
		doc1.addField("url", url);
		doc1.addField("bucketname", s3bucketname);
		doc1.addField("username", username);
		doc1.addField("rating", rating);
		doc1.addField("hide", hide);
		doc1.addField("lastmodified", lastmodified);
		UpdateResponse response = server.add(doc1);
		log.info("solr message" + response.getResponse());
		server.commit(false, true, true);
		if (0 == response.getStatus()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * searchDocumentSolrByBucketId
	 * 
	 * Method search documents in solr in bucket
	 * 
	 * @param int id bucketid
	 * @param int start start page
	 * @param String
	 *            q search query
	 * @param String
	 *            sort sort preference
	 * @return String containing search results
	 */
	@Override
	public HashMap<String, String> searchDocumentSolrByBucketId(int id,
			int start, String q, int sort) {
		// TODO Auto-generated method stub
		try {
			HttpSolrServer server = new HttpSolrServer(
					"http://localhost:8983/solr");
			SolrQuery query = null;
			if ("" == q) {
				query = new SolrQuery("*:*");
			} else {
				query = new SolrQuery();
				query.set("q", q);
			}
			query.setRequestHandler("/select");
			// query.remove(FacetParams.FACET_FIELD);
			query.addFilterQuery("bucketid:" + id);
			query.addFilterQuery("hide:0");
			query.addFacetField("rating");
			query.setFields("id", "collegeid", "bucketname", "userid",
					"username", "url", "title", "description", "bucketid",
					"hide", "rating", "lastmodified");
			query.set("wt", "json");
			if (0 == sort) {
				log.info("ascending lastmodified");
				query.set("sort", "lastmodified asc");
			} else if (sort == 1) {
				log.info("descending lastmodified");
				query.set("sort", "lastmodified desc");
			} else if (sort == 2) {
				log.info("desc rating");
				query.set("sort", "rating desc");
			} else if (sort == 3) {
				log.info("asc title");
				query.set("sort", "title asc");
			} else {
				log.info("asc username");
				query.set("sort", "username asc");
			}

			query.setFacet(true);
			query.setStart(start);
			query.setRows(10);
			QueryResponse rsp = null;
			try {
				rsp = server.query(query);
				log.info("response" + rsp);
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SolrDocumentList docs = rsp.getResults();

			// facets
			// maintain value Counts
			Map<String, Map<String, Long>> facetCounts;
			Map<String, Long> valueCounts;
			List<FacetField.Count> values;

			List<FacetField> fields = rsp.getFacetFields();
			if (fields != null) {
				facetCounts = new HashMap<String, Map<String, Long>>(
						fields.size());

				for (FacetField facetField : fields) {
					values = facetField.getValues();
					// log.info(facetField.getName());

					valueCounts = new HashMap<String, Long>(values.size());

					for (FacetField.Count value : values) {
						valueCounts.put(value.getName(), value.getCount());
					}

					facetCounts.put(facetField.getName(), valueCounts);
				}

			} else {
				facetCounts = Collections.emptyMap();
			}

			// generating reponse
			long numFound = rsp.getResults().getNumFound();
			Gson gson = new Gson();
			server.close();
			HashMap<String, String> solrresponse = new HashMap<String, String>();
			solrresponse.put("numFound", numFound + "");
			solrresponse.put("response", gson.toJson(docs));
			solrresponse.put("facet", gson.toJson(facetCounts));
			return solrresponse;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.info("error while querying solr method searchDocumentSolrByBucketId"
					+ e.getMessage());
			return null;
		}

	}

	/**
	 * searchDocumentSolrByBucketId
	 * 
	 * Method to convert date to UTC timestamp for solr
	 * 
	 * @param String
	 *            datestr
	 * @return date UTC
	 */
	public static String toUtcDate(String dateStr) {
		SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		// Add other parsing formats to try as you like:
		String[] dateFormats = { "yyyy-MM-dd", "MMM dd, yyyy hh:mm:ss Z",
				"yyyy-MM-dd HH:mm:ss" };
		for (String dateFormat : dateFormats) {
			try {
				return out.format(new SimpleDateFormat(dateFormat)
						.parse(dateStr));
			} catch (Exception ignore) {
			}
		}
		throw new IllegalArgumentException("Invalid date: " + dateStr);
	}

	/**
	 * \postComment
	 * 
	 * Method to postComment
	 * 
	 * @param int userid
	 * @param int documentId
	 * @param String
	 *            comment
	 * @return boolean true or false
	 */
	@Override
	public boolean postComment(int userid, int documentId, String comment) {
		// TODO Auto-generated method stub
		if (isBanned(userid) == 0 || checkDocumentExists(documentId) == 0) {
			log.info("User is banned" + documentId + " userid" + userid);
			return false;
		} else {
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;
			try {
				conn = getConnectionWithPooling();

				query = "INSERT IGNORE INTO Comments (DocumentId,Comment,Date,UserId,Hide) "
						+ "VALUES(?,?,NOW(),?,0)";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setInt(1, documentId);
				preparedStatement.setString(2, comment);
				preparedStatement.setInt(3, userid);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
				return true;
			} catch (Exception e) {
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}
	}

	/**
	 * isBanned
	 * 
	 * Method to check isBanned user
	 * 
	 * @param int userid
	 * @return int userid or 0
	 */
	@Override
	public int isBanned(int userid) {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		int userId = 0;
		conn = getConnectionWithPooling();
		// query to select check if user role is correct and user not banned
		try {
			query = conn
					.prepareStatement("SELECT UserId FROM users WHERE UserId=? AND Ban=0");
			query.setInt(1, userid);
			ResultSet rs = query.executeQuery();
			if (rs.next()) {
				userId = rs.getInt("UserId");
			}
			Utils.closeQuietly(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Utils.closeQuietly(conn);
		}
		log.info("banned");
		return userId;
	}

	/**
	 * reportDocument
	 * 
	 * Method to reportDocument
	 * 
	 * @param int reporter
	 * @param int documentid
	 * @param String
	 *            reason
	 * @return boolean true or false
	 */
	@Override
	public boolean reportDocument(int reporter, int documentid, String reason) {
		// TODO Auto-generated method stub
		if (isBanned(reporter) == 0) {
			log.info("User is banned");
			return false;
		} else {
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;
			try {
				conn = getConnectionWithPooling();

				query = "INSERT IGNORE INTO reported_documents (documentId,reporter,reason) "
						+ "VALUES(?,?,?)";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setInt(1, documentid);
				preparedStatement.setInt(2, reporter);
				preparedStatement.setString(3, reason);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
				return true;
			} catch (Exception e) {
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}
	}

	/**
	 * reportComment
	 * 
	 * Method to reportComment
	 * 
	 * @param int reporter
	 * @param int commentid
	 * @return boolean true or false
	 */
	@Override
	public boolean reportComment(int reporter, int commentid) {
		// TODO Auto-generated method stub
		if (isBanned(reporter) == 0) {
			log.info("User is banned");
			return false;
		} else {
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;
			try {
				conn = getConnectionWithPooling();

				query = "INSERT IGNORE INTO reported_comments (commentId,reporter) "
						+ "VALUES(?,?)";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setInt(1, commentid);
				preparedStatement.setInt(2, reporter);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
				return true;
			} catch (Exception e) {
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}
	}

	/**
	 * isSuperUser
	 * 
	 * Method to check isSuperUser user
	 * 
	 * @param int userid
	 * @return int userid or 0
	 */
	@Override
	public int isSuperUser(int userid) {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		int userId = 0;
		conn = getConnectionWithPooling();
		// query to select check if user role is correct and user not banned
		try {
			query = conn
					.prepareStatement("SELECT UserId FROM users WHERE UserId=? AND Ban=0 and RoleId=3 OR RoleId=2 ");
			query.setInt(1, userid);
			ResultSet rs = query.executeQuery();
			if (rs.next()) {
				userId = rs.getInt("UserId");
			}
			Utils.closeQuietly(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.info("sql exception in isSuperUser" + e.getMessage());
		} finally {
			Utils.closeQuietly(conn);
		}
		return userId;
	}

	/**
	 * hideDocument
	 * 
	 * Method to hideDocument
	 * 
	 * @param int documentid
	 * @param int userid
	 * @return boolean true or false
	 */

	@Override
	public boolean hideDocument(int documentid, int userid) {
		// TODO Auto-generated method stub
		if (isSuperUser(userid) == 0) {
			log.info("User is banned");
			return false;
		} else {
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;
			try {
				conn = getConnectionWithPooling();

				query = "UPDATE documents set Hide=? WHERE DocumentId=?";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setInt(1, 1);
				preparedStatement.setInt(2, documentid);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
				updateSolrHide(documentid, 1);
				return true;
			} catch (Exception e) {
				log.info("exception thrown in method hideDocument"
						+ e.getMessage());
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}

	}

	/**
	 * hideReportedDocument
	 * 
	 * Method to hideReportedDocument
	 * 
	 * @param int documentid
	 * @param int userid
	 * @return boolean true or false
	 */
	@Override
	public boolean hideReportedDocument(int documentid, int userid) {
		// TODO Auto-generated method stub
		if (isSuperUser(userid) == 0) {
			log.info("User is banned");
			return false;
		} else {
			hideDocument(documentid, userid);
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;
			try {
				conn = getConnectionWithPooling();

				query = "DELETE FROM reported_documents WHERE documentId=?";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setInt(1, documentid);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
				return true;
			} catch (Exception e) {
				log.info("exception thrown in method hideReportedDocument"
						+ e.getMessage());
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}

	}

	/**
	 * banUser
	 * 
	 * Method to banUser
	 * 
	 * @param int userid
	 * @oaram int banid
	 * @return boolean true or false
	 */
	@Override
	public boolean banUser(int banid, int userid) {
		// TODO Auto-generated method stub
		if (isSuperUser(userid) == 0) {
			log.info("User is banned");
			return false;
		} else {
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;
			try {
				conn = getConnectionWithPooling();

				query = "UPDATE users set Ban=? WHERE UserId=?";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setInt(1, 1);
				preparedStatement.setInt(2, banid);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
				return true;
			} catch (Exception e) {
				log.info("exception thrown in method banUser" + e.getMessage());
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}

	}

	/**
	 * unBanUser
	 * 
	 * Method to unBanUser
	 * 
	 * @param int banid user to be banned
	 * @param int userid user banning the banid user
	 * @return boolean true or false
	 */
	@Override
	public boolean unBanUser(int banid, int userid) {
		// TODO Auto-generated method stub
		if (isSuperUser(userid) == 0) {
			log.info("User is banned");
			return false;
		} else {
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;
			try {
				conn = getConnectionWithPooling();

				query = "UPDATE users set Ban=? WHERE UserId=?";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setInt(1, 0);
				preparedStatement.setInt(2, banid);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
				return true;
			} catch (Exception e) {
				log.info("exception thrown in method banUser" + e.getMessage());
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}

	}

	/**
	 * updateSolrHide
	 * 
	 * Method to remove documents from solr
	 * 
	 * @param int documentid
	 * @param int hide
	 * @return boolean true or false
	 */
	public boolean updateSolrHide(int documentid, int hide) throws Exception {
		HttpSolrClient server = new HttpSolrClient("http://localhost:8983/solr");
		SolrInputDocument doc1 = new SolrInputDocument();
		doc1.addField("id", documentid);
		Map<String, Integer> partialUpdate = new HashMap<String, Integer>();
		partialUpdate.put("set", 1);
		doc1.setField("hide", partialUpdate);
		UpdateResponse response = server.add(doc1);
		server.commit(false, true, true);
		if (0 == response.getStatus()) {
			log.info("updated solr hide flag to" + hide);
			return true;
		} else {
			log.info("couldnt update solr hide flag to" + hide);
			return false;
		}
	}

	/**
	 * likeDocument
	 * 
	 * Method to update document rating by 1
	 * 
	 * @param int documentid
	 * @param int userid
	 * @return boolean true or false
	 */
	@Override
	public boolean likeDocument(int documentid, int userid) {
		// TODO Auto-generated method stub
		if (isSuperUser(userid) == 0) {
			log.info("User is banned");
			return false;
		} else {
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;
			try {
				conn = getConnectionWithPooling();

				query = "UPDATE documents set rating=rating+1 WHERE DocumentId=?";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setInt(1, documentid);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
				updateSolrRating(documentid, 1);
				return true;
			} catch (Exception e) {
				log.info("exception thrown in method likeDocument"
						+ e.getMessage());
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}
	}

	/**
	 * updateSolrRating
	 * 
	 * Method to update document rating
	 * 
	 * @param int documentid
	 * @param int rating
	 * @return boolean true or false
	 */
	public boolean updateSolrRating(int documentid, int rating)
			throws Exception {
		HttpSolrClient server = new HttpSolrClient("http://localhost:8983/solr");
		SolrInputDocument doc1 = new SolrInputDocument();
		doc1.addField("id", documentid);
		Map<String, Integer> partialUpdate = new HashMap<String, Integer>();
		partialUpdate.put("inc", rating);
		doc1.setField("rating", partialUpdate);
		UpdateResponse response = server.add(doc1);
		server.commit(false, true, true);
		if (0 == response.getStatus()) {
			log.info("updated solr rating to" + rating);
			return true;
		} else {
			log.info("couldnt update solr rating to" + rating);
			return false;
		}
	}

	/**
	 * checkDocumentExists
	 * 
	 * Method to check document exists
	 * 
	 * @param int documentid
	 * @return boolean true or false
	 */

	@Override
	public int checkDocumentExists(int documentid) {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		int DocumentId = 0;
		conn = getConnectionWithPooling();
		// query to select check if user role is correct and user not banned
		try {
			query = conn
					.prepareStatement("SELECT DocumentId FROM documents WHERE DocumentId=? AND Hide=0");
			query.setInt(1, documentid);
			ResultSet rs = query.executeQuery();
			if (rs.next()) {
				DocumentId = rs.getInt("DocumentId");
			}
			Utils.closeQuietly(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Utils.closeQuietly(conn);
		}
		return DocumentId;
	}

	/**
	 * getComments
	 * 
	 * Method to get comments
	 * 
	 * @param int documentid
	 * @oaram int lastcommentid for going to next page of comments
	 * @return String get Comment
	 */
	@Override
	public String getComments(int documentid, int lastcommentid) {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;
		conn = getConnectionWithPooling();
		// query to select check if user role is correct and user not banned
		try {
			query = conn
					.prepareStatement("SELECT Comment,Username,Date,commentId FROM comments "
							+ "JOIN users on comments.UserId=users.UserId  WHERE comments.DocumentId=? AND Hide=0 "
							+ "AND comments.commentId>=? LIMIT 10");
			query.setInt(1, documentid);
			query.setInt(2, lastcommentid);
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info("Exception thrown method getComments" + e.getMessage());
			returnString = "Service Not Available or user not found";
		} finally {
			Utils.closeQuietly(conn);
		}
		log.info("returning comments" + returnString);
		return returnString;
	}

	/**
	 * getCommentsCount
	 * 
	 * Method to get comments
	 * 
	 * @param int documentid
	 * @return String get Comment Counts
	 */
	@Override
	public String getCommentsCount(int documentid) {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;
		conn = getConnectionWithPooling();
		// query to select check if user role is correct and user not banned
		try {
			query = conn
					.prepareStatement("SELECT max(commentId) as max,min(commentId) as min,count(*) as count FROM comments "
							+ "JOIN users on comments.UserId=users.UserId  WHERE comments.DocumentId=? AND Hide=0");
			query.setInt(1, documentid);
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info("Exception thrown method getCommentsCount"
					+ e.getMessage());
			returnString = "Service Not Available or user not found";
		} finally {
			Utils.closeQuietly(conn);
		}
		log.info("returning comments" + returnString);
		return returnString;
	}

	/**
	 * hideComment
	 * 
	 * Method to remove comment
	 * 
	 * @param int commentid
	 * @param int userid
	 * @return boolean true or false
	 */

	@Override
	public boolean hideComment(int commentid, int userid) {
		// TODO Auto-generated method stub
		if (isSuperUser(userid) == 0 && doesCommentBelongToUser(userid) == 0) {
			log.info("User is banned");
			return false;
		} else {
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;
			try {
				conn = getConnectionWithPooling();

				query = "UPDATE comments set Hide=? WHERE commentId=?";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setInt(1, 1);
				preparedStatement.setInt(2, commentid);
				preparedStatement.executeUpdate();
				Utils.closeQuietly(conn);
				return true;
			} catch (Exception e) {
				log.info("exception thrown in method hideDocument"
						+ e.getMessage());
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}

	}

	/**
	 * hideReportedComment
	 * 
	 * Method to remove reported comment
	 * 
	 * @param int commentid
	 * @param int userid
	 * @return boolean true or false
	 */
	@Override
	public boolean hideReportedComment(int commentid, int userid) {
		// TODO Auto-generated method stub
		if (isSuperUser(userid) == 0) {
			log.info("User is banned");
			return false;
		} else {
			hideComment(commentid, userid);
			PreparedStatement preparedStatement = null;
			String query = null;
			Connection conn = null;
			try {
				conn = getConnectionWithPooling();

				query = "DELETE FROM reported_comments WHERE commentId=?";
				preparedStatement = conn.prepareStatement(query);
				preparedStatement.setInt(1, commentid);
				preparedStatement.executeUpdate();
				log.info("CommentId" + commentid);
				Utils.closeQuietly(conn);
				return true;
			} catch (Exception e) {
				log.info("exception thrown in method hideReportedComment"
						+ e.getMessage());
				return false;
			} finally {
				Utils.closeQuietly(conn);
			}
		}

	}

	/**
	 * doesCommentBelongToUser
	 * 
	 * Method to check if document belongs to user
	 * 
	 * @param int userid
	 * @return int userid or zero
	 */
	@Override
	public int doesCommentBelongToUser(int userid) {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		int UserId = 0;
		conn = getConnectionWithPooling();
		// query to select check if user role is correct and user not banned
		try {
			query = conn
					.prepareStatement("SELECT UserId FROM comments WHERE UserId=? AND Hide=0");
			query.setInt(1, userid);
			ResultSet rs = query.executeQuery();
			if (rs.next()) {
				UserId = rs.getInt("UserId");
			}
			Utils.closeQuietly(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Utils.closeQuietly(conn);
		}
		return UserId;
	}

	/**
	 * searchDocumentSolrGeneral
	 * 
	 * Method search documents in solr
	 * 
	 * @param String
	 *            bucketname
	 * @param String
	 *            rating
	 * @param String
	 *            username
	 * @param int start Page
	 * @param String
	 *            q string that is searched
	 * @param String
	 *            sort sort preference
	 * @return String containing search results
	 */
	@Override
	public HashMap<String, String> searchDocumentSolrGeneral(String bucketname,
			String rating, String username, int start, String q, int sort) {
		// TODO Auto-generated method stub
		try {
			HttpSolrServer server = new HttpSolrServer(
					"http://localhost:8983/solr");
			SolrQuery query = null;
			if ("" == q) {
				query = new SolrQuery("*:*");
			} else {
				query = new SolrQuery();
				query.set("q", q);
			}
			query.setRequestHandler("/select");
			query.set("defType", "edismax");
			query.set("facet.sort", "count desc");
			// query.remove(FacetParams.FACET_FIELD);
			if (bucketname != "" && bucketname.length() != 0) {
				query.addFilterQuery("bucketid:(" + bucketname + ")");
			}
			if (!rating.equals("-1") && rating != "" && rating.length() != 0) {
				query.addFilterQuery("rating:("+rating+")");
			}
			log.info("rating" + rating);
			if (username != "" && username.length() != 0) {
				query.addFilterQuery("userid:(" + username + ")");
			}
			query.setFacetSort("count desc");
			query.addFilterQuery("hide:0");
			query.addFacetField("rating");
			query.addFacetField("username");
			query.addFacetField("bucketname");
			query.addFacetField("bucketid");
			query.addFacetField("userid");
			query.addFacetField("title");
			query.setFields("id", "collegeid", "bucketname", "userid",
					"username", "url", "title", "description", "bucketid",
					"hide", "rating", "lastmodified");
			query.set("wt", "json");
			if (0 == sort) {
				log.info("ascending lastmodified");
				query.set("sort", "lastmodified asc");
			} else if (sort == 1) {
				log.info("descending lastmodified");
				query.set("sort", "lastmodified desc");
			} else if (sort == 2) {
				log.info("desc rating");
				query.set("sort", "rating desc");
			} else if (sort == 3) {
				log.info("asc title");
				query.set("sort", "title asc");
			} else {
				log.info("asc username");
				query.set("sort", "username asc");
			}

			query.setFacet(true);
			query.setStart(start);
			query.setRows(10);
			QueryResponse rsp = null;
			try {
				rsp = server.query(query);
				log.info("response" + rsp);
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SolrDocumentList docs = rsp.getResults();

			// facets
			// maintain value Counts
			Map<String, Map<String, Long>> facetCounts;
			Map<String, Long> valueCounts;
			List<FacetField.Count> values;

			List<FacetField> fields = rsp.getFacetFields();
			if (fields != null) {
				facetCounts = new LinkedHashMap<String, Map<String, Long>>(
						fields.size());

				for (FacetField facetField : fields) {
					values = facetField.getValues();
					// log.info(facetField.getName());

					valueCounts = new LinkedHashMap<String, Long>(values.size());

					for (FacetField.Count value : values) {
						valueCounts.put(value.getName(), value.getCount());
					}

					facetCounts.put(facetField.getName(), valueCounts);
				}

			} else {
				facetCounts = Collections.emptyMap();
			}

			// generating reponse
			long numFound = rsp.getResults().getNumFound();
			Gson gson = new Gson();
			server.close();
			HashMap<String, String> solrresponse = new HashMap<String, String>();
			solrresponse.put("numFound", numFound + "");
			solrresponse.put("response", gson.toJson(docs));
			solrresponse.put("facet", gson.toJson(facetCounts));
			return solrresponse;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.info("error while querying solr method searchDocumentSolrGeneral"
					+ e.getMessage());
			return null;
		}
	}

	/**
	 * getBucketOwner
	 * 
	 * Method to owner of bucket based on bucketid
	 * 
	 * @param int bucketid
	 * @return String containing user information owner of bucket
	 */
	@Override
	public String getBucketOwner(int bucketid) {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;

		try {
			conn = getConnectionWithPooling();
			query = conn
					.prepareStatement("SELECT Owner,Name FROM `S3_Buckets` WHERE BucketId=?");
			query.setInt(1, bucketid);
			log.info("bucketid" + bucketid);
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			log.info("Exception thrown method getBucketOwner" + e.getMessage());
			returnString = "Service Not Available or user not found";
		} finally {
			Utils.closeQuietly(conn);
		}
		log.info("Returning getBucketOwner" + bucketid + "return String"
				+ returnString);
		return returnString;
	}

	/**
	 * getReportedDocuments
	 * 
	 * Method to get reported documents
	 * 
	 * @return String JsonString containing reported documents
	 */

	@Override
	public String getReportedDocuments() {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;

		try {
			conn = getConnectionWithPooling();
			query = conn
					.prepareStatement("SELECT documents.UserId,users.Username,reported_documents.documentId,documents.Title,documents.Desc,"
							+ "Count(documents.documentId) as count, reported_documents.reason as reason FROM reported_documents "
							+ "JOIN documents ON documents.DocumentId=reported_documents.documentId JOIN"
							+ " users ON documents.UserId=users.UserId "
							+ "group by reported_documents.documentId  order by count desc");
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			log.info("Exception thrown method getReportedDocuments"
					+ e.getMessage());
			returnString = "Service Not Available or user not found";
		} finally {
			Utils.closeQuietly(conn);
		}
		log.info("Returning getReportedDocuments");
		return returnString;
	}

	/**
	 * getReportedComments
	 * 
	 * Method to get reported documents
	 * 
	 * @return String JsonString containing reported comments
	 */

	@Override
	public String getReportedComments() {
		// TODO Auto-generated method stub
		PreparedStatement query = null;
		Connection conn = null;
		ToJSON converter = new ToJSON();
		JsonArray json = new JsonArray();
		String returnString = null;

		try {
			conn = getConnectionWithPooling();
			query = conn
					.prepareStatement("SELECT comments.UserId,users.Username,reported_comments.commentId,comments.Comment,Count(comments.commentId) as count "
							+ "FROM reported_comments JOIN comments "
							+ "ON comments.CommentId=reported_comments.commentId "
							+ "JOIN users ON users.UserId=comments.UserId"
							+ " group by reported_comments.commentId  "
							+ "order by count desc");
			ResultSet rs = query.executeQuery();
			json = converter.toJsonArray(rs);
			returnString = json.toString();
			Utils.closeQuietly(conn);
		} catch (Exception e) {
			log.info("Exception thrown method getReportedComments"
					+ e.getMessage());
			returnString = "Service Not Available or user not found";
		} finally {
			Utils.closeQuietly(conn);
		}
		log.info("Returning getReportedComments");
		return returnString;
	}

	@Override
	public boolean hideBanUser(int banid, int userid, int id, String type) {
		// TODO Auto-generated method stub
		boolean banuser = banUser(banid, userid);
		log.info("In hideBanUser");
		boolean hide = false;
		// hide document/Comment
		if (type.equals("comment")) {
			hide = hideReportedComment(id, userid);
			log.info("hiding comment" + id);
		} else {
			hide = hideReportedDocument(id, userid);
			log.info("hiding document" + id);
		}
		if (hide && banuser) {
			log.info("user and document/comment banned");
			return true;
		} else {
			log.info("user and document/comment not banned");
			return false;
		}
	}

	/**
	 * Train
	 * 
	 * Method to train the POS tagger using the Hidden Markov model
	 * 
	 * @param trainfile
	 *            the file with the the train sentences
	 * @param tagfile
	 *            the file with the corresponding tags
	 */
	private static void Train(String trainfile, String tagfile) {

		BufferedReader test = null, tag = null;
		try {
			test = new BufferedReader(new FileReader(trainfile)); // Open train
																	// file
			tag = new BufferedReader(new FileReader(tagfile)); // Open tag file

			String word, tagword;

			while ((word = test.readLine()) != null
					&& (tagword = tag.readLine()) != null) { // read the file
				String[] wordarray = word.split(" "); // split by spaces
				String[] tagarray = tagword.split(" ");

				// Fill in the emission map with number of emissions
				for (int i = 0; i < wordarray.length; i++) {
					if (!emission.containsKey(tagarray[i])) { // if the tag
																// appears for
																// the first
																// time
						Map<String, Double> word2value = new TreeMap<>();
						word2value.put(wordarray[i], 1.0);
						emission.put(tagarray[i], word2value);
					} else {
						if (!emission.get(tagarray[i])
								.containsKey(wordarray[i])) { // if the word
																// appeared with
																// the tag for
																// the first
																// time
							emission.get(tagarray[i]).put(wordarray[i], 1.0);
						} else { // if the word has already appeared with the
									// tag
							double newvalue = emission.get(tagarray[i]).get(
									wordarray[i]) + 1;
							emission.get(tagarray[i]).put(wordarray[i],
									newvalue);
						}
					}
				}

				// Fill in the transition map with number of transitions
				for (int i = 0; i < wordarray.length; i++) {
					if (i == 0) { // the first transition is # -> (first tag ->
									// P(first tag|#)
						if (!transition.containsKey(start)) {
							Map<String, Double> first2value = new TreeMap<>();
							first2value.put(tagarray[i], 1.0);
							transition.put(start, first2value);
						} else if (!transition.get(start).containsKey(
								tagarray[i])) {
							transition.get(start).put(tagarray[i], 1.0);
						} else {
							double newvalue = transition.get(start).get(
									tagarray[i]) + 1.0;
							transition.get(start).put(tagarray[i], newvalue);
						}

					} else if (!transition.containsKey(tagarray[i - 1])) { // if
																			// transition
																			// doesn't
																			// contain
																			// the
																			// previous
																			// tag
						Map<String, Double> curr2value = new TreeMap<>();
						curr2value.put(tagarray[i], 1.0);
						transition.put(tagarray[i - 1], curr2value);
					} else { // if transition contains the previous tag
						if (!transition.get(tagarray[i - 1]).containsKey(
								tagarray[i])) { // but not to the current tag
							transition.get(tagarray[i - 1]).put(tagarray[i],
									1.0);
						} else { // and to the current tag
							double newvalue = transition.get(tagarray[i - 1])
									.get(tagarray[i]) + 1;
							transition.get(tagarray[i - 1]).put(tagarray[i],
									newvalue);
						}
					}
				}
			}
		} catch (Exception e) {
			log.info("error" + e.getStackTrace());
		} finally {
			// Close file if file exist. If not, catch the exception
			try {
				test.close();
			} catch (Exception e) {
				log.info("error" + e.getStackTrace());
			}
		}
	}

	/**
	 * Normalize
	 * 
	 * Method to normalize the probablity of the emission and transition maps
	 * and convert it to log10
	 * 
	 */
	private static void Normalize() {

		for (String a : emission.keySet()) {
			double count = 0;
			for (String b : emission.get(a).keySet())
				count += emission.get(a).get(b); // count the number of words in
													// each tag
			for (String b : emission.get(a).keySet()) { // normalize the
														// probability by
														// dividing by count and
														// taking log10
				double logprob = Math.log10(emission.get(a).get(b) / count);
				emission.get(a).put(b, logprob);
			}
		}

		for (String a : transition.keySet()) {
			double count = 0;
			for (String b : transition.get(a).keySet())
				count += transition.get(a).get(b); // count the number of
													// transitions
			for (String b : transition.get(a).keySet()) { // normalize the
															// probability by
															// dividing by count
															// and taking log10
				double logprob = Math.log10(transition.get(a).get(b) / count);
				transition.get(a).put(b, logprob);
			}
		}
	}

	/**
	 * Viterbi
	 * 
	 * Method to perform Viterbi tagging and backtracing
	 * 
	 * @param input
	 *            the sentence to be tagged
	 * @return result - the tags associated with the sentence
	 */
	public static String Viterbi(String input) {

		String result = null, lasttag = null;
		double highestscore = Double.NEGATIVE_INFINITY; // highest score in the
														// Viterbi tagging. Set
														// to negative infinity
														// since score < 0
		Stack<String> toprint = new Stack<String>(); // the stack for printing
														// to result
		List<Map<String, String>> backtrace = new ArrayList<Map<String, String>>(); // List
																					// of
																					// current
																					// tag
																					// ->
																					// previous
																					// tag
		String[] words = input.split(" "); // the array of words

		Set<String> prevstates = new TreeSet<>(); // the previous states
		prevstates.add(start); // start with "#"

		Map<String, Double> prevscores = new TreeMap<>(); // the previous scores
		prevscores.put(start, 0.0); // probability of starting is 100%

		for (int i = 0; i < words.length; i++) {
			Set<String> nextstates = new TreeSet<>();
			Map<String, Double> nextscores = new TreeMap<>();
			Map<String, String> backpoint = new TreeMap<>();
			double score;

			for (String state : prevstates) { // for all previous tags
				if (transition.containsKey(state)
						&& !transition.get(state).isEmpty()) {
					for (String transit : transition.get(state).keySet()) { // for
																			// all
																			// transition
																			// of
																			// previous
																			// ->
																			// current
																			// tag
						nextstates.add(transit);

						if (emission.containsKey(transit)
								&& emission.get(transit).containsKey(words[i])) { // if
																					// word
																					// is
																					// in
																					// emission
							score = prevscores.get(state)
									+ transition.get(state).get(transit)
									+ emission.get(transit).get(words[i]);
						} else { // if word is unseen, use an unseen score
							score = prevscores.get(state)
									+ transition.get(state).get(transit)
									+ unseen;
						}
						if (!nextscores.containsKey(transit)
								|| score > nextscores.get(transit)) {
							nextscores.put(transit, score); // keep track of the
															// highest score
							backpoint.put(transit, state); // for backtracing
															// later on
							if (backtrace.size() > i)
								backtrace.remove(i); // remove the last element
														// if required to update
														// the last element
							backtrace.add(backpoint);
						}
					}
				}
			}
			prevscores = nextscores;
			prevstates = nextstates;
		}

		// Find the last tag for backtracing
		for (String score : prevscores.keySet()) {
			if (prevscores.get(score) > highestscore) {
				highestscore = prevscores.get(score);
				lasttag = score;
			}
		}

		// Perform Backtrace
		toprint.push(lasttag);
		for (int i = words.length - 1; i > 0; i--) {
			toprint.push(backtrace.get(i).get(toprint.peek()));
		}

		// Print to result
		while (!toprint.isEmpty()) {
			if (result == null)
				result = (toprint.pop() + " ");
			else
				result += (toprint.pop() + " ");
		}
		return (result);
	}

}
