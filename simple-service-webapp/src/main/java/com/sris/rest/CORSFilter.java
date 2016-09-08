package com.sris.rest;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/*
 * @author  Rhutwij tulankar
 * @version 1.0
 * @since   2015-03-31
 * class for cross domain ajax
 */
public class CORSFilter implements ContainerResponseFilter {

	@Override
	public ContainerResponse filter(ContainerRequest request,
			ContainerResponse response) {

		response.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
		response.getHttpHeaders().add("Access-Control-Allow-Headers",
				"origin, content-type, accept, authorization");
		response.getHttpHeaders().add("Access-Control-Allow-Credentials",
				"true");
		response.getHttpHeaders().add("Access-Control-Allow-Methods",
				"GET, POST, PUT, DELETE, OPTIONS, HEAD");

		return response;
	}
}
