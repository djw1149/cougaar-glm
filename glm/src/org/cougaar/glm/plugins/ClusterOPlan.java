/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.glm.plugins;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.util.UID;

import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.debug.GLMDebug;

import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class ClusterOPlan implements Serializable {
    ClusterIdentifier clusterId_;
    long startTime_, endTime_;
    Vector orgActivities_ = null;
    Oplan oplan_;
//      IncrementalSubscription orgActivitySubscription_;

    public ClusterOPlan(ClusterIdentifier id, Oplan op/*, IncrementalSubscription sub*/) {
//  	System.out.println("--- Creating ClusterOPlan for "+id+", oplan "+op);
	clusterId_ = id;
	oplan_ = op;
	startTime_ = oplan_.getCday().getTime();
	endTime_ = oplan_.getCday().getTime();
//  	orgActivitySubscription_ = sub;
//  	updateOrgActivities(orgActivitySubscription_.elements());
//  	updateOPlanTimes();
    }

    /* If OPlan does not change but OrgActivities for the OPlan change
     * then get the updated OrgActivities.
     */
    public boolean updateOrgActivities(IncrementalSubscription orgActivitySubscription_) {
	// Only update OrgActivities if subscription has changed
	if (orgActivitySubscription_.getChangedList().hasMoreElements()
	    || orgActivitySubscription_.getAddedList().hasMoreElements() 
	    || orgActivitySubscription_.getRemovedList().hasMoreElements()) {
//  	    System.out.println("--- New/Changed/Removed OrgActivities for "+clusterId_);
	    updateOrgActivities(orgActivitySubscription_.elements());
	    updateOPlanTimes();
	    return true;
	}
	return false;
    }

    private void updateOPlanTimes() {
	long end_time;
	long start_time;
	OrgActivity activity;
	Enumeration activities = orgActivities_.elements();

	// initialize endTime/startTime values
	if (activities.hasMoreElements()) {
	    activity = (OrgActivity)activities.nextElement();
	    endTime_ = getEndTime(activity);
	    startTime_ = getStartTime(activity);
	}
	// search for first/last times
	while (activities.hasMoreElements()) {
	    activity = (OrgActivity)activities.nextElement();
	    end_time = getEndTime(activity);
	    if ((end_time > endTime_)) {
		endTime_ = end_time;
	    }
	    start_time = getStartTime(activity);
	    if (start_time < startTime_) {
		startTime_ = start_time;
	    }
	}
    }

    public void updateOrgActivities(Enumeration activities) {
	orgActivities_ = new Vector();
	OrgActivity orgact;
	String cluster_name = clusterId_.toString();
	while (activities.hasMoreElements()) {
	    // only deal w/ org activities for this cluster
	    orgact = (OrgActivity)activities.nextElement();
	    if (orgact.getOrgID().equals(cluster_name)) {
//  		System.out.println("--- Adding OrgActivity for "+clusterId_+", activity "+orgact.getActivityName()+
//  				   ", "+orgact.getOpTempo()+", "+orgact);
		orgActivities_.add(orgact);
	    }
	}
    }

    /* getOplan() returns the OPlan UID this object is handling
     */
    public UID getOplanUID() {
	return oplan_.getUID();
    }

    public long getEndTime(OrgActivity act) {
	return act.getTimeSpan().getEndDate().getTime();
    }

    public long getStartTime(OrgActivity act) {
	return act.getTimeSpan().getStartDate().getTime();
    }

    /* Latest end time of the OrgActivities
     * If now orgActivities are received, end time is not set
     */
    public long getEndTime() {
	return endTime_;
    }

    /* Earliest start time of the OrgActivities
     * If now orgActivities are received, start time is not set
     */
    public long getStartTime() {
	return startTime_;
    }

    public GeolocLocation getGeoLoc(long time) {
	OrgActivity oa = getOrgActivity(time);
	if (oa != null) {
	    return oa.getGeoLoc();
	}
	return null;
    }

    public String getOpTempo(long time) {
	OrgActivity oa = getOrgActivity(time);
	if (oa != null) {
	    return oa.getOpTempo();
	}
	return null;
    }
  
    public long getOplanCday() {
      return  oplan_.getCday().getTime();
    } 
    
    public OrgActivity getOrgActivity(long t) {
	long end_time, start_time;
	Enumeration enum = orgActivities_.elements();
	OrgActivity oa;
	while (enum.hasMoreElements()) {
	    oa = (OrgActivity)enum.nextElement();
	    start_time = getStartTime(oa);
	    end_time = getEndTime(oa);
	    if ((t >= start_time) && (t < end_time)) {
		return oa;
	    }
	}
	return null;
    }

//      /* When disposing of a ClusterOPlan object, need to get the
//       * OrgActivity subscription to do an 'unsubscribe'
//       */
//      public IncrementalSubscription getOrgActivitySubscription() {
//  	return orgActivitySubscription_;
//      }

    public String toString() {
	return oplan_.toString();
    }
}




