package com.sris.Middleware;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.core.header.FormDataContentDisposition;

/*
 * @author  Rhutwij tulankar
 * @version 1.0
 * @since   2015-03-31
 * Interface that contains User DAO methods
 */
public interface UsersImp {
	static Logger log = Logger.getLogger(UsersImp.class.getName());

	// get functions
	public boolean isUser(String userName, String password);

	public String getUserInfo(String username);

	public boolean authenticateCreateBucketAction(int userid,
			String bucketname, int subjectid);

	public boolean createBucket(int userid, int subjectid, String name);

	public String getUserThreadsById(int userid);

	public HashMap<String, String> searchDocumentSolrByBucketId(int id,
			int start, String q, int sort);

	public HashMap<String, String> searchDocumentSolrGeneral(String bucketname,
			String rating, String username, int start, String q, int sort);

	public int isBanned(int userid);

	public int isSuperUser(int userid);

	public int checkDocumentExists(int documentid);

	public String getComments(int documentid, int lastcommentid);

	public String getCommentsCount(int documentid);

	public int doesCommentBelongToUser(int userid);

	public String getBucketOwner(int bucketid);

	public String getReportedDocuments();

	public String getReportedComments();

	// set functions and operations
	public boolean reportComment(int reporter, int commentid);

	public boolean hideDocument(int documentid, int userid);

	public boolean hideComment(int commentid, int userid);

	public boolean banUser(int banid, int userid);

	public boolean hideBanUser(int banid, int userid, int id, String type);

	public boolean hideReportedComment(int commentid, int userid);

	public boolean hideReportedDocument(int documentid, int userid);

	public boolean unBanUser(int banid, int userid);

	public String getBannedUsers();

	public String getUnBannedUsers();

	public boolean likeDocument(int documentid, int userid);

	public boolean reportDocument(int reporter, int documentid, String reason);

	public boolean postComment(int userid, int documentId, String comment);

	public boolean registerUser(String userName, int CollegeId, int rating,
			String password, int roleId);

	public boolean postDocument(int bucketId, int userId, int professorId,
			int subjectId, int collegeId, String title, String desc,
			InputStream fileinputStream, String filemetadata);
}
