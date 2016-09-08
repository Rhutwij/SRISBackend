package com.sris.Middleware;

import org.apache.log4j.Logger;

/*
 * @author  Rhutwij tulankar
 * @version 1.0
 * @since   2015-03-31
 * Interface for colleges DAO
 */
public interface CollegesImp {
	static Logger log = Logger.getLogger(CollegesImp.class.getName());

	public String getCollegeList();

	public String getCollegeListById(int id);

	public String getCollegeProfessorsById(int CollegeId);

	// set operations
	public boolean addCollege(String name, String Type, int userid);

	public boolean editCollege(String name, String Type, int userid,
			int collegeid);

	public boolean deleteCollege(int userid, int collegeid);
}
