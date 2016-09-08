package com.sris.Middleware;

import org.apache.log4j.Logger;

/*
 * @author  Rhutwij tulankar
 * @version 1.0
 * @since   2015-03-31
 * Interface that contains Subject DAO implementation methods
 */
public interface SubjectsImp {
	static Logger log = Logger.getLogger(SubjectsImp.class.getName());

	public String getSubjectList(int collegeid);

	public String getSubjectList();

	// set functions
	public boolean addSubject(String name, int userid);

	public boolean editSubject(String name, int userid, int subjectid);

	public boolean deleteSubject(int userid, int subjectid);
}
