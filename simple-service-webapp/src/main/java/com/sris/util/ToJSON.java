package com.sris.util;

import com.google.gson.*;

import java.sql.ResultSet;

import org.owasp.esapi.ESAPI;

import com.google.gson.JsonArray;

/*
 * @author  Rhutwij tulankar
 * @version 1.0
 * @since   2015-03-31
 * Utility class for encoding query response to json
 */
public class ToJSON {

	/**
	 * toJsonArray
	 * 
	 * Method to convert database result set to json
	 * 
	 * @param ResultSet
	 *            rs
	 * @return JsonArray array json
	 */
	public JsonArray toJsonArray(ResultSet rs) throws Exception {
		JsonArray json = new JsonArray();
		String temp = null;
		try {
			java.sql.ResultSetMetaData rsmd = rs.getMetaData();
			while (rs.next()) {
				int numColumns = rsmd.getColumnCount();
				JsonObject obj = new JsonObject();
				for (int i = 1; i < numColumns + 1; i++) {
					String columnName = rsmd.getColumnName(i);
					if (rsmd.getColumnType(i) == java.sql.Types.ARRAY) {
						obj.add(columnName,
								(JsonElement) rs.getArray(columnName));
					} else if (rsmd.getColumnType(i) == java.sql.Types.BIGINT) {
						obj.addProperty(columnName, rs.getInt(columnName));
					} else if (rsmd.getColumnType(i) == java.sql.Types.BINARY) {
						obj.addProperty(columnName, rs.getBoolean(columnName));
					} else if (rsmd.getColumnType(i) == java.sql.Types.BIT) {
						obj.addProperty(columnName, rs.getInt(columnName));
					} else if (rsmd.getColumnType(i) == java.sql.Types.BLOB) {
						obj.add(columnName,
								(JsonElement) rs.getBlob(columnName));
					} else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE) {
						obj.addProperty(columnName, rs.getDouble(columnName));
					} else if (rsmd.getColumnType(i) == java.sql.Types.FLOAT) {
						obj.addProperty(columnName, rs.getFloat(columnName));
					} else if (rsmd.getColumnType(i) == java.sql.Types.INTEGER) {
						obj.addProperty(columnName, rs.getInt(columnName));
					} else if (rsmd.getColumnType(i) == java.sql.Types.NVARCHAR) {
						obj.addProperty(columnName, rs.getString(columnName));
					} else if (rsmd.getColumnType(i) == java.sql.Types.VARCHAR) {
						temp = rs.getString(columnName);
						temp = ESAPI.encoder().canonicalize(temp);
						temp = ESAPI.encoder().encodeForHTML(temp);

						obj.addProperty(columnName, temp);
					} else if (rsmd.getColumnType(i) == java.sql.Types.TINYINT) {
						obj.addProperty(columnName, rs.getInt(columnName));
					} else if (rsmd.getColumnType(i) == java.sql.Types.DATE) {
						obj.addProperty(columnName, rs.getDate(columnName)
								.toString());
					} else if (rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
						obj.addProperty(columnName, rs.getTimestamp(columnName)
								.toString());
					} else if (rsmd.getColumnType(i) == java.sql.Types.NUMERIC) {
						obj.addProperty(columnName, rs.getInt(columnName));
					} else {
						obj.add(columnName,
								(JsonElement) rs.getObject(columnName));
					}
				}
				json.add(obj);
			}
		} catch (Exception e) {
			e.getStackTrace();
		}
		return json;

	}
}
