/**
 * 
 */
package com.sris.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
import com.sris.Middleware.Colleges;

/*
 * @author  Rhutwij tulankar
 * @version 1.0
 * @since   2015-03-31
 * class for Main SRIS enpoints for naviation in this class
 */

@Path("/colleges")
public class SRISService extends Colleges implements DefaultMessageInterface {
	// public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response msg() {
		return Response.ok("Provide more path params").build();
	}

	@Path("/list")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getColleges() throws Exception {
		Response rb = null;
		String colleges = null;
		colleges = getCollegeList();
		rb = Response.ok(colleges).build();
		log.info("returning college list");
		return rb;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}")
	public Response getCollegesById(@PathParam("id") int id) throws Exception {
		Response rb = null;
		String colleges = null;
		colleges = getCollegeListById(id);
		rb = Response.ok(colleges).build();
		log.info("returning college" + id);
		return rb;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/professors")
	public Response getProfessorsByCollegeId(@PathParam("id") int id)
			throws Exception {
		Response rb = null;
		String colleges = null;
		colleges = getCollegeProfessorsById(id);
		rb = Response.ok(colleges).build();
		log.info("returning professorlistByCollegeId" + id);
		return rb;
	}

	@POST
	@Path("/addCollege")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response addCollegeRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = addCollege(myObject.optString("name"),
					myObject.optString("type"), myObject.optInt("userid"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("college not added");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "college added");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("college added");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method addCollegeRest"
					+ E.getMessage());
		}
		return rp;
	}

	@PUT
	@Path("/editCollege")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response editCollegeRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = editCollege(myObject.optString("name"),
					myObject.optString("type"), myObject.optInt("userid"),
					myObject.optInt("collegeid"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("college not updated");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "college updated");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("college updated");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method editCollegeRest"
					+ E.getMessage());
		}
		return rp;
	}

	@DELETE
	@Path("/deleteCollege")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	public Response deleteCollegeRest(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			// getting postdata
			JSONObject myObject = new JSONObject(postData);
			boolean isBanned = deleteCollege(myObject.optInt("userid"),
					myObject.optInt("collegeid"));
			if (isBanned == false) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "you are banned");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("college not deleted");
			} else {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "college deleted");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
				log.info("college deleted");

			}
		} catch (Exception E) {
			log.info("Exception occured service not available method deleteCollegeRest"
					+ E.getMessage());
		}
		return rp;
	}

}
