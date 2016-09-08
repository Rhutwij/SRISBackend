package com.sris.rest;

import java.io.InputStream;
import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.sris.Middleware.Users;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

/*
 * @author  Rhutwij tulankar
 * @version 1.0
 * @since   2015-03-31
 * class that contains user actions routed to methods in middle layer
 */

@Path("/users")
public class UserService extends Users implements DefaultMessageInterface {

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response msg() {
		// TODO Auto-generated method stub
		return Response.ok("Provide more path params").build();
	}

	@Path("/register")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerUser(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean userRegistered = registerUser(
					myObject.optString("username"), myObject.optInt("college"),
					myObject.optInt("rating"),
					myObject.optString("srispassword"), myObject.optInt("role"));
			if (userRegistered) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "registered");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("error", "wrong password");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
			}

		} catch (Exception e) {
			log.info("exception while registering user" + e.getMessage());
			rp = Response.status(500).entity("unable to process request")
					.build();
		}
		return rp;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{username}")
	public Response getUserInfoByUsername(@PathParam("username") String username)
			throws Exception {
		Response rb = null;
		String userinfo = null;
		userinfo = getUserInfo(username);
		rb = Response.ok(userinfo).build();
		log.info("returning getUserInfoByUsername" + username);
		return rb;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list/banned")
	public Response getBannedUsersRest() throws Exception {
		Response rb = null;
		String userinfo = null;
		userinfo = getBannedUsers();
		rb = Response.ok(userinfo).build();
		log.info("returning getBannedUsersRest");
		return rb;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list/unbanned")
	public Response getUnBannedUsersRest() throws Exception {
		Response rb = null;
		String userinfo = null;
		userinfo = getUnBannedUsers();
		rb = Response.ok(userinfo).build();
		log.info("returning getUnBannedUsersRest");
		return rb;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/info/{userid}")
	public Response getUserInfoByUserId(@PathParam("userid") int userid)
			throws Exception {
		Response rb = null;
		String userinfo = null;
		userinfo = getUserInfoById(userid);
		rb = Response.ok(userinfo).build();
		log.info("returning getUserInfoById" + userid);
		return rb;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/bucket/{bucketid}")
	public Response getBucketOwnerRest(@PathParam("bucketid") int bucketid)
			throws Exception {
		Response rb = null;
		String owner = null;
		owner = getBucketOwner(bucketid);
		rb = Response.ok(owner).build();
		log.info("returning getBucketOwner" + bucketid);
		return rb;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/document/{documentid}/{lastcommentid}")
	public Response getCommentsByDocumentId(
			@PathParam("documentid") int documentid,
			@PathParam("lastcommentid") int lastcommentid) throws Exception {
		Response rb = null;
		String comments = null;
		comments = getComments(documentid, lastcommentid);
		rb = Response.ok(comments).build();
		log.info("returning getCommentsByDocumentId" + lastcommentid);
		return rb;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/reportedComments")
	public Response getReportedCommentsRest() throws Exception {
		Response rb = null;
		String comments = null;
		comments = getReportedComments();
		rb = Response.ok(comments).build();
		log.info("returning getReportedCommentsRest");
		return rb;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/reportedDocuments")
	public Response getReportedDocumentsRest() throws Exception {
		Response rb = null;
		String comments = null;
		comments = getReportedDocuments();
		rb = Response.ok(comments).build();
		log.info("returning getReportedDocumentsRest");
		return rb;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/document/{documentid}")
	public Response getCommentsCountRest(@PathParam("documentid") int documentid)
			throws Exception {
		Response rb = null;
		String comments = null;
		comments = getCommentsCount(documentid);
		rb = Response.ok(comments).build();
		log.info("returning getCommentsCount");
		return rb;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{userid}/threads")
	public Response getUserThreadsByIdRest(@PathParam("userid") int userid)
			throws Exception {
		Response rb = null;
		String userthreads = null;
		userthreads = getUserThreadsById(userid);
		rb = Response.ok(userthreads).build();
		log.info("returning getUserThreadsByIdRest" + userid);
		return rb;
	}

	// searchDocumentSolrByBucketId

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("bucket/{bucketid}/documents")
	public Response searchDocumentSolrByBucketIdRest(
			@PathParam("bucketid") int bucketid,
			@QueryParam("start") int start, @QueryParam("q") String q,
			@QueryParam("sort") int sort) throws Exception {
		Response rb = null;
		HashMap<String, String> documents = null;
		if (q.length() == 0)
			q = "";
		documents = searchDocumentSolrByBucketId(bucketid, start, q, sort);
		rb = Response.ok(new JSONObject(documents).toString()).build();
		log.info("returning searchDocumentSolrByBucketIdRest" + bucketid);
		return rb;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("search")
	public Response searchDocumentSolrGeneralRest(@QueryParam("q") String q,
			@QueryParam("rating") String rating,
			@QueryParam("username") String username,
			@QueryParam("start") int start,
			@QueryParam("bucketname") String bucketname,
			@QueryParam("sort") int sort) throws Exception {
		Response rb = null;
		HashMap<String, String> documents = null;
		log.info("bucketname >>>>" + bucketname);
		if (q.length() == 0)
			q = "";
		documents = searchDocumentSolrGeneral(bucketname, rating, username,
				start, q, sort);
		rb = Response.ok(new JSONObject(documents).toString()).build();
		log.info("returning searchDocumentSolrGeneralRest" + q);
		return rb;
	}

	@POST
	@Path("/document/postComment")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response postCommentRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = postComment(myObject.optInt("userid"),
					myObject.optInt("documentid"),
					myObject.optString("comment"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("comment not posted postComment method");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "comment posted");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("comment  posted postComment method");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method postCommentRest"
					+ E.getMessage());
		}
		return rp;
	}

	@POST
	@Path("/document/reportDocument")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response reportDocumentRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = reportDocument(myObject.optInt("reporter"),
					myObject.optInt("documentid"), myObject.optString("reason"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("document not reported");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "document reported");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("document reported");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method reportDocumentRest"
					+ E.getMessage());
		}
		return rp;
	}

	@POST
	@Path("/comment/reportComment")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response reportCommentRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = reportComment(myObject.optInt("reporter"),
					myObject.optInt("commentid"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("comment not reported");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "comment reported");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("comment reported");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method reportCommentRest"
					+ E.getMessage());
		}
		return rp;
	}

	@POST
	@Path("/document/hideDocument")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response hidetDocumentRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = hideDocument(myObject.optInt("documentid"),
					myObject.optInt("userid"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("document cant be hidden");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "document hidden");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("document hidden");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method hidetDocumentRest"
					+ E.getMessage());
		}
		return rp;
	}

	@POST
	@Path("/document/hideReportedDocument")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response hidetReportedDocumentRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = hideReportedDocument(
					myObject.optInt("documentid"), myObject.optInt("userid"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("document cant be hidden");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "document hidden");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("document hidden");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method hidetReportedDocumentRest"
					+ E.getMessage());
		}
		return rp;
	}

	@Path("/hideBanUser")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response hideBanUserRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = hideBanUser(myObject.optInt("banid"),
					myObject.optInt("userid"), myObject.optInt("id"),
					myObject.optString("type"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("user and Document/Comment cant be banned");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "user banned hidden");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("user and Document/Comment is banned");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method hideBanUser"
					+ E.getMessage());
		}
		return rp;
	}

	@Path("/banUser")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response banUserRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = banUser(myObject.optInt("banid"),
					myObject.optInt("userid"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("user cant be banned");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "user banned hidden");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("user is banned");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method banUserRest"
					+ E.getMessage());
		}
		return rp;
	}

	@Path("/UnBanUser")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response unBanUserRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = unBanUser(myObject.optInt("banid"),
					myObject.optInt("userid"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("user cant be unbanned");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "user unbanned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("user is unbanned");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method unBanUserRest"
					+ E.getMessage());
		}
		return rp;
	}

	@POST
	@Path("/comment/hideComment")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response hideCommentRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = hideComment(myObject.optInt("commentid"),
					myObject.optInt("userid"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("comment cant be hidden");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "comment hidden");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("comment hidden");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method hideCommentRest"
					+ E.getMessage());
		}
		return rp;
	}

	@POST
	@Path("/comment/hideReportedComment")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response hideReportedCommentRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = hideReportedComment(
					myObject.optInt("commentid"), myObject.optInt("userid"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("comment cant be hidden");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "comment hidden");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("comment hidden");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method hideReportedCommentRest"
					+ E.getMessage());
		}
		return rp;
	}

	@POST
	@Path("/document/rateDocument")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response rateDocumentRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = likeDocument(myObject.optInt("documentid"),
					myObject.optInt("userid"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("document cant be rated");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "document rated");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("document rated");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method rateDocumentRest"
					+ E.getMessage());
		}
		return rp;
	}

	@Path("/createBucket")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	@Produces(MediaType.APPLICATION_JSON)
	public Response createBucket(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean created = createBucket(myObject.optInt("userid"),
					myObject.optInt("subjectid"), myObject.optString("name"));
			if (created) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "bucket created");
				log.info("bucket created");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("error", "unable to create bucket");
				log.info("unable to create bucket");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
			}

		} catch (Exception e) {
			log.info("exception while registering user" + e.getMessage());
			rp = Response.status(500).entity("unable to process request")
					.build();
		}
		return rp;
	}

	@Path("/postDocument")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postDocumentToS3(
			@FormDataParam("filedata") InputStream fileinputstream,
			@FormDataParam("filename") String fileMetaData,
			@FormDataParam("userid") int userid,
			@FormDataParam("professorid") int professorid,
			@FormDataParam("file") int subjectid,
			@FormDataParam("subjectid") int collegeid,
			@FormDataParam("title") String title,
			@FormDataParam("desc") String desc,
			@FormDataParam("bucketid") int bucketid) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			boolean posted = postDocument(bucketid, professorid, userid,
					subjectid, collegeid, title, desc, fileinputstream,
					fileMetaData);
			if (posted) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "posted");
				log.info("document posted");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("error", "unable to post");
				log.info("unable to post document");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
			}

		} catch (Exception e) {
			log.info("exception posting document" + e.getMessage());
			rp = Response.status(500).entity("unable to process request")
					.build();
		}
		return rp;
	}

}
