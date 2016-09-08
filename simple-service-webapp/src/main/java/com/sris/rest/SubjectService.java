package com.sris.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.sris.Middleware.Subjects;

/*
 * @author  Rhutwij tulankar
 * @version 1.0
 * @since   2015-03-31
 * class for subjects related information endpoints here
 */

@Path("/subjects")
public class SubjectService extends Subjects implements DefaultMessageInterface {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response msg() {
		return Response.ok("Provide more path params").build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}")
	public Response getCollegesById(@PathParam("id") int id) throws Exception {
		Response rb = null;
		String subjects = null;
		subjects = getSubjectList(id);
		rb = Response.ok(subjects).build();
		log.info("returning Subjects by college" + id);
		return rb;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list")
	public Response getColleges() throws Exception {
		Response rb = null;
		String subjects = null;
		subjects = getSubjectList();
		rb = Response.ok(subjects).build();
		log.info("returning Subjects list");
		return rb;
	}

	@POST
	@Path("/addSubject")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response addSubjectRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = addSubject(myObject.optString("name"),
					myObject.optInt("userid"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("Subject not added");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "Subject added");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("Subject added");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method addSubjectRest"
					+ E.getMessage());
		}
		return rp;
	}

	@PUT
	@Path("/editSubject")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response editSubjectRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = editSubject(myObject.optString("name"),
					myObject.optInt("userid"), myObject.optInt("subjectid"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("Subject not updated");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "Subject updated");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("Subject updated");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method editSubjectRest"
					+ E.getMessage());
		}
		return rp;
	}

	@DELETE
	@Path("/deleteSubject")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response deleteSubjectRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = deleteSubject(myObject.optInt("userid"),
					myObject.optInt("subjectid"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("Subject not deleted");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "Subject deleted");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("Subject deleted");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method deleteSubjectRest"
					+ E.getMessage());
		}
		return rp;
	}

}
