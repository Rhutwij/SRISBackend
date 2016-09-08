package com.sris.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.commons.dbcp2.Utils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.*;

import com.sris.dao.TestDatabase;
import com.sris.util.ToJSON;

import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/*
 * @author  Rhutwij tulankar
 * @version 1.0
 * @since   2015-03-31
 * class for testing whether SRIS is running and database is running
 */

@Path("/status/v1")
public class V1_status {

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/webservice")
	public String returnStatus() {
		return "<p>SRIS is Running</p>";
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/database")
	public String returnTables() throws Exception {
		TestDatabase obj = new TestDatabase();
		return obj.returnTableData();
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/getSampleData")
	public Response returnsAllExampleData() throws Exception {
		TestDatabase obj = new TestDatabase();
		return obj.returnColleges();
	}

	// GET method with QueryParam
	@Path("/getQueryParamData")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response returnDataQP(@QueryParam("id") int id) throws Exception {
		if (0 == id) {
			return Response.status(400)
					.entity("Error: method needs argument id").build();
		}
		TestDatabase q = new TestDatabase();
		return q.queryReturnTableDataQP(id);

	}

	// GET method with PathParam

	@Path("/getPathParamData/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response returnDataPP(@PathParam("id") int id) throws Exception {
		if (0 == id) {
			return Response.status(400)
					.entity("Error: method needs argument id").build();
		}
		TestDatabase q = new TestDatabase();
		return q.queryReturnTableDataQP(id);
	}

	// POST Method PathParam
	@Path("/addCollege")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	@Produces(MediaType.APPLICATION_JSON)
	public Response addCollege(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		TestDatabase q = new TestDatabase();
		try {
			JSONObject myObject = new JSONObject(postData);
			int http_code = q.addCollege(myObject.optString("Name"),
					myObject.optString("Type"));
			if (200 == http_code) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "item inserted");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
			}

		} catch (Exception e) {
			rp = Response.status(500).entity("unable to process request")
					.build();
			e.getStackTrace();
		}
		return rp;
	}

	// PUT Method PathParam

	@Path("/updateCollege")
	@PUT
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateCollege(@QueryParam("id") int id, String postData)
			throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		TestDatabase q = new TestDatabase();
		try {
			JSONObject myObject = new JSONObject(postData);
			int http_code = q.updateCollege(myObject.optString("Name"),
					myObject.optString("Type", ""), id);
			if (200 == http_code) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "item updated");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
			}

		} catch (Exception e) {
			rp = Response.status(500).entity("unable to process request")
					.build();
			e.getStackTrace();
		}
		return rp;
	}

	// DELETE Method PathParam
	@Path("/deleteCollege")
	@DELETE
	@Consumes({ MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_FORM_URLENCODED })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteCollege(String postData) throws Exception {
		String returnString = null;
		Response rp = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		TestDatabase q = new TestDatabase();
		try {
			System.out.println("postData" + postData);
			JSONObject myObject = new JSONObject(postData);
			int http_code = q.deleteCollege(myObject.optString("Name"));
			if (200 == http_code) {
				jsonObject.put("HTTP_CODE", "200");
				jsonObject.put("msg", "item deleted");
				returnString = jsonArray.put(jsonObject).toString();
				rp = Response.ok(returnString).build();
			}

		} catch (Exception e) {
			rp = Response.status(500).entity("unable to process request")
					.build();
			e.getStackTrace();
		}
		return rp;
	}

	@Path("/addSolrDoc")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String addSolrDocs() throws Exception {

		HttpSolrServer server = new HttpSolrServer("http://localhost:8983/solr");
		for (int i = 1; i < 1000; ++i) {
			SolrInputDocument doc1 = new SolrInputDocument();
			doc1.addField("id", "id1" + i, 1.0f);
			doc1.addField("name", "doc1", 1.0f);
			doc1.addField("price", 10);
			SolrInputDocument doc2 = new SolrInputDocument();
			doc2.addField("id", "id2" + i, 1.0f);
			doc2.addField("name", "doc2", 1.0f);
			doc2.addField("price", 20);
			Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
			docs.add(doc1);
			docs.add(doc2);
			System.out.println(docs.toString());
			UpdateResponse response = server.add(docs);
			if (i % 100 == 0) {
				server.commit(); // periodically flush
			}
		}
		server.commit();
		return "<p>added docs</p>";
	}

	@Path("/getSolrDoc")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getDocs() throws Exception {
		HttpSolrServer server = new HttpSolrServer("http://localhost:8983/solr");
		SolrQuery query = new SolrQuery("*:*");
		query.remove(FacetParams.FACET_FIELD);
		query.addFacetField("name");
		query.setFacetMinCount(5);
		query.setFacet(true);
		query.setStart(0);
		query.setRows(10);
		QueryResponse rsp = server.query(query);
		SolrDocumentList docs = rsp.getResults();
		Gson gson = new Gson();
		server.close();
		return gson.toJson(docs);
	}

}
