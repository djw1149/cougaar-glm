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

package org.cougaar.lib.util;

import org.cougaar.core.domain.RootFactory;

import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectLocation;
import org.cougaar.planning.ldm.plan.AspectScorePoint;
import org.cougaar.planning.ldm.plan.AspectScoreRange;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.TimeAspectValue;

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.util.log.Logger;

/** 
 * This class contains preference-related methods.
 * Can ask basic questions like ready at, early, best, and latest date for
 * tasks.
 *
 * Can also create an various kinds of preferences.
 */

public class UTILPreference {
  private static String myName = "UTILPreference";
  private static double ONE_DAY = 1000*60*60*24; // millis
  private static double ONE_OVER_ONE_DAY = 1.0d/ONE_DAY; // millis
  private static double fiftyYears = ONE_DAY*365*50; // millis
  private static double boundaryDefaultScore = 0.75;

  public static double NO_ASPECT_VALUE = -1.0d;

  public static Logger logger;
  public static void setLogger (Logger l) { logger = l; }

  /** 
   * Utility methods
   */
  public static void replacePreference(NewTask t, Preference new_pref) {
    // Remove the preference from the current list of 
    // preferences for this task
    Enumeration prefs_enum = t.getPreferences();
    Vector this_task_prefs = UTILAllocate.enumToVector(prefs_enum);

    t.setPreferences((replacePreference(this_task_prefs,
					new_pref)
		      ).elements());
  }

  public static Vector replacePreference(Vector old_prefs, 
					 Preference new_pref) {
    Vector new_prefs = new Vector(old_prefs.size());

    Enumeration prefs_e = old_prefs.elements();
    while (prefs_e.hasMoreElements()) {
      Preference old_pref = (Preference)prefs_e.nextElement();
      if (old_pref.getAspectType() != new_pref.getAspectType())
	new_prefs.addElement(old_pref);
    }

    // and then add the new one
    new_prefs.addElement(new_pref);
    
    return new_prefs;
  }
    
  /**
   * This call generates a cost preference.  Only using a single value.
   * Should probably be more complex than this.
   *
   * @param ldmf the RootFactory
   * @param cost the single cost value
   * @return Preference
   */

  public static Preference makeCostPreference(RootFactory ldmf, double cost) {
    AspectValue lowAV      = new AspectValue(AspectType.COST, -0.01d);
    AspectValue bestAV     = new AspectValue(AspectType.COST, 0.0d);
    AspectValue costAV     = new AspectValue(AspectType.COST, cost);
    ScoringFunction costSF = ScoringFunction.createVScoringFunction (lowAV, bestAV, costAV, 1.0);
    Preference costPref = ldmf.newPreference(AspectType.COST, costSF);
    return costPref;
  }

  /**
   * Generates a quantity preference.
   *
   * What should we do with the weight of the preference?
   * Should this be set by a policy object?
   *
   * @param quantity desired
   * @return Preference
   */
  public static Preference makeQuantityPreference(RootFactory ldmf, long quantity) {
    AspectValue quantityAV = 
      new AspectValue(AspectType.QUANTITY, quantity);
    ScoringFunction quantitySF = 
      ScoringFunction.createNearOrAbove(quantityAV, 0.0d);
    Preference quantityPref = ldmf.newPreference(AspectType.QUANTITY, quantitySF, 1.0);
    return quantityPref;
  }

  /**
   * Generates a preference from 1 date.
   *
   * What should we do with the weight of the preference?
   * Should this be set by a policy object?
   *
   * @param readyAtDate - the date item is ready to move
   * @return Preference
   */
  public static Preference makeStartDatePreference(RootFactory ldmf,
						   Date readyAtDate) {
    AspectValue readyAtAV = 
      new AspectValue(AspectType.START_TIME, readyAtDate.getTime());
    ScoringFunction startSF = 
      ScoringFunction.createNearOrAbove(readyAtAV, 0.0d);
    Preference startPref = ldmf.newPreference(AspectType.START_TIME, startSF, 1.0);
    return startPref;
  }

  /**
   * Generates a preference from 3 dates.
   *
   * What should we do with the weight of the preference?
   * Should this be set by a policy object?
   *
   * What should be the relative scores of early and late?
   *
   * @param earlyDate - the earliest possible date
   * @param bestDate - the best date
   * @param lateDate - the latest possible date
   * @return Preference
   */
  public static Preference makeEndDatePreference(RootFactory ldmf,
						 Date earlyDate,  
						 Date bestDate,
						 Date lateDate){  
    return 
      ldmf.newPreference(AspectType.END_TIME, 
			 new UTILEndDateScoringFunction (earlyDate, 
							 bestDate,
							 lateDate, 
							 boundaryDefaultScore));
  }

  /**
   * When you don't know early or latest, only best time for the task.
   *
   * What should we do with the weight of the preference?
   * Should this be set by a policy object?
   *
   * Note that it uses one day as the slope -- i.e.
   * a day after the bestDate, the pref is exceeded.
   *
   * @param bestDate - the best date
   * @return Preference
   */
  public static Preference makeEndDatePreference(RootFactory ldmf,
						 Date bestDate) {
    AspectValue endAV = new AspectValue(AspectType.END_TIME, bestDate.getTime());
    ScoringFunction endSF = ScoringFunction.createPreferredAtValue(endAV, ONE_OVER_ONE_DAY);
    Preference endPref = ldmf.newPreference(AspectType.END_TIME, endSF, 1.0);
    return endPref;
  }

  public static Preference makeEndDateBelowPreference(RootFactory ldmf, Date bestDate) {
    AspectValue endAV = new AspectValue(AspectType.END_TIME, bestDate.getTime());
    ScoringFunction endSF = ScoringFunction.createNearOrBelow(endAV, 0.0);
    Preference endPref = ldmf.newPreference(AspectType.END_TIME, endSF, 1.0);
    return endPref;
  }

  /**
   * What should we do with the weight of the preference?
   * Should this be set by a policy object?
   *
   * Note that it uses one day as the slope -- i.e.
   * a day after the POD date, the pref is exceeded.
   */
  public static Preference makePODDatePreference(RootFactory ldmf,
						 Date bestDate) {
    if (bestDate == null || bestDate.before(new Date(1000))) {
      logger.error("UTILPreference creating bad POD_Date preference: the date is " + bestDate);
    }
    AspectValue podAV = new AspectValue(AspectType.POD_DATE, bestDate.getTime());
    ScoringFunction podSF = ScoringFunction.createPreferredAtValue(podAV, 
								   ONE_OVER_ONE_DAY);
    Preference podPref = ldmf.newPreference(AspectType.POD_DATE, podSF, 1.0);
    return podPref;
  }

  /**
    * @return true if task has specified aspect type
    */
  public static boolean hasPrefWithAspectType (Task taskToExamine, int aspectType) {
    return (getPrefWithAspectType(taskToExamine, aspectType) != null);
  }

  /**
    * Utility function to get preference from a task based on its aspect type
    * 
    * @param Task task to examine for matching preference
    * @param int aspect type you're hoping to find on the task's preference list
    * @return Preference first preference found (earliest in pref enum) with the
    *  specified aspect type, null if none found
    */
  public static Preference getPrefWithAspectType(Task taskToExamine, int aspect_type) {
    Enumeration pref_enum = taskToExamine.getPreferences();
    while (pref_enum.hasMoreElements()) {
      Preference return_pref = (Preference)pref_enum.nextElement();
      if (return_pref.getAspectType() == aspect_type)
	return return_pref;
    }
    
    return null;
  }
  

  /**
    * Utility function to remove pref from a task based on its aspect type
    * 
    * @param Task task to examine for matching preference
    * @param int aspect type you're hoping to find on the task's pref list
    * @return Preference last preference found (latest in pref enum) with the
    *  specified aspect type, null if none found
    * NOTE that if more than one pref with this aspect type is found, ALL 
    * are removed but only the latest is returned.
    */
  public static Preference removePrefWithAspectType(Task taskToChange,
						    int aspect_type) {
    Vector old_prefs =
      UTILAllocate.enumToVector(taskToChange.getPreferences());
    Vector new_prefs = new Vector(old_prefs.size());
    Preference pref_to_return = null;

    Enumeration prefs_e = old_prefs.elements();
    while (prefs_e.hasMoreElements()) {
      Preference old_pref = (Preference)prefs_e.nextElement();
      if (old_pref.getAspectType() != aspect_type)
	new_prefs.addElement(old_pref);
      else
	pref_to_return = old_pref;
    }
    
    ((NewTask)taskToChange).setPreferences(new_prefs.elements());
    
    return pref_to_return;
  }

  /**
   * Given a Preference get its best aspect value
   * @param pref the preference to examine
   * @return double best aspect value for the preference
   */
  public static double getPreferenceBestValue(Preference pref){
    ScoringFunction sfunc = pref.getScoringFunction();
    return sfunc.getBest().getAspectValue().getValue();
  }

  /**
   * Examine plan element for a reported aspect value
   *
   * Many times easier to use type specific variants -- like getReportedReadyAt
   */
  public static double getReportedAspectValue (PlanElement pe, int aspectType) {
    AllocationResult result = pe.getReportedResult();
    if (result == null)
      return NO_ASPECT_VALUE;
    return result.getValue (aspectType);
  }

  /**
   * get the Cost preference of a task.
   *
   * @param  Task t - the Task object
   * @return Date date at which task is ready
   */

  public static double getCost(Task t) {
    try {
      Preference pref = getPrefWithAspectType (t, AspectType.COST);
      ScoringFunction sfunc = pref.getScoringFunction ();
      return sfunc.getBest ().getAspectValue().getValue ();
    } catch (NullPointerException npe) {
      throw new UTILRuntimeException (classname + 
				      ".getCost () : task\n\t" + t.getUID () + 
				      "\n\thas no COST preference?");
    }
  }
 
  /**
   * Get reported cost from plan element
   */
  public static double getReportedCost (PlanElement pe) {
    return getReportedAspectValue (pe, AspectType.COST);
  }

  /**
   * get the Quantity preference of a task.
   *
   * @param  Task t - the Task object
   * @return Date date at which task is ready
   */

  public static long getQuantity(Task t) {
    try {
      Preference pref = getPrefWithAspectType (t, AspectType.QUANTITY);
      ScoringFunction sfunc = pref.getScoringFunction ();
      return sfunc.getBest ().getAspectValue().longValue ();
    } catch (NullPointerException npe) {
      throw new UTILRuntimeException (classname + 
				      ".getQuantity () : task\n\t" + t.getUID () + 
				      "\n\thas no QUANTITY preference?");
    }
  }
 
  /**
   * Get reported quantity from plan element
   */
  public static long getReportedQuantity (PlanElement pe) {
    return (long) getReportedAspectValue (pe, AspectType.QUANTITY);
  }

  /**
   * Returns the READYAT Date from task object, or new Date if READYAT 
   * date is null
   *
   * @param  Task t - the Task object
   * @return Date date at which task is ready
   */

  public static Date getReadyAt(Task t) {
    try {
      Preference pref = getPrefWithAspectType (t, AspectType.START_TIME);
      ScoringFunction sfunc = pref.getScoringFunction ();
      return new Date(sfunc.getBest ().getAspectValue().longValue ());
    } catch (NullPointerException npe) {
      throw new UTILRuntimeException (classname + 
				      ".getReadyAt () : task\n\t" + t + 
				      "\n\thas no START_TIME preference?");
    }
  }
 
  /**
   * Get reported ready at date from plan element
   */
  public static Date getReportedReadyAt (PlanElement pe) {
    double value = getReportedAspectValue (pe, AspectType.START_TIME);
    if (value != NO_ASPECT_VALUE)
      return new Date ((long) value);
    return null;
  }

  /**
   * Returns the POD Date from task object, null if POD date not a pref on this task
   *
   * @param  Task t - the Task with the pref
   * @return Date point of departure date for task, null if no POD date pref
   */

  public static Date getPODDate(Task t) {
    Preference pod_pref = getPrefWithAspectType(t, AspectType.POD_DATE);
    if (pod_pref == null)
      return null;

    Date pod_date = 
      new Date((long)(pod_pref.getScoringFunction().getBest().getValue()));

    return pod_date;
  }
 
  /**
   * Get reported POD date from plan element
   */
  public static Date getReportedPODDate (PlanElement pe) {
    return getReportedPODDate (pe.getReportedResult());
  }

  /**
   * Get reported POD date from allocation result
   */
  public static Date getReportedPODDate (AllocationResult result) {
    double value = result.getValue (AspectType.POD_DATE);
    if (value != NO_ASPECT_VALUE)
      return new Date ((long) value);
    return null;
  }

  /**
   * Returns the desired earliest arrival date from task object or null if
   * that date is null. 
   *
   * Should work with both TOPSEndDates and end dates generated from outside of TOPS.
   *
   * If not an TOPSEndDate, then looks at the valid ranges for the scoring function,
   * gets the first one, and returns its start point.
   *
   * @param t - the Task object
   * @return Date
   */
  public static Date getEarlyDate(Task t) {
    UTILEndDateScoringFunction edsf = getEndDateSF (t);
    if (edsf == null) {
      Enumeration validRanges = getValidEndDateRanges (t);
      for (; validRanges.hasMoreElements (); ) {
	AspectScoreRange range = (AspectScoreRange) validRanges.nextElement ();
	return new Date (((AspectScorePoint) range.getRangeStartPoint ()).getAspectValue ().longValue ());
      }
    }
    return edsf.getEarlyDate ();
  }

  /**
   * Get reported End date from plan element
   */
  public static Date getReportedEndDate (PlanElement pe) {
    return getReportedEndDate(pe.getReportedResult());
  }

  /**
   * Get reported End date from allocation result
   */
  public static Date getReportedEndDate (AllocationResult ar) {
    double value = ar.getValue (AspectType.END_TIME);
    if (value != NO_ASPECT_VALUE)
      return new Date ((long) value);
    return null;
  }

  /**
   * Returns the desired best arrival date from task object or null if
   * that date is null. 
   *
   * Should work with both TOPSEndDates and end dates generated from outside of TOPS.
   *
   * @param t - the Task object
   * @return Date
   */
  public static Date getBestDate(Task t) {
    UTILEndDateScoringFunction edsf = getEndDateSF (t);
    if (edsf == null) {
      Preference endDatePref = getPrefWithAspectType(t, AspectType.END_TIME);
      return new Date ((long) getPreferenceBestValue (endDatePref));
    }
    return edsf.getBestDate ();
  }

  /**
   * Returns the desired latest arrival date from task object or null if
   * that date is null. 
   *
   * Should work with both TOPSEndDates and end dates generated from outside of TOPS.
   *
   * If not an TOPSEndDate, then looks at the valid ranges for the scoring function,
   * gets the last one, and returns its end point.
   *
   * @param t - the Task object
   * @return Date
   */
  public static Date getLateDate(Task t) {
    UTILEndDateScoringFunction edsf = getEndDateSF (t);
    if (edsf == null) {
      Enumeration validRanges = getValidEndDateRanges (t);
      for (; validRanges.hasMoreElements (); ) {
	AspectScoreRange range = (AspectScoreRange) validRanges.nextElement ();

	if  (!validRanges.hasMoreElements ())
	  return new Date (((AspectScorePoint) range.getRangeEndPoint ()).getAspectValue ().longValue ());
      }
    }
    return edsf.getLateDate ();
  }

  protected static Enumeration getValidEndDateRanges (Task t) {
    Preference endDatePref = getPrefWithAspectType(t, AspectType.END_TIME);
    return getValidEndDateRanges (endDatePref);
  }

  protected static Enumeration getValidEndDateRanges (Preference endDatePref) {
    Calendar cal = java.util.Calendar.getInstance();
    cal.set(2200, 0, 0, 0, 0, 0);
    cal.set(Calendar.MILLISECOND, 0);
    Date endOfRange = (Date) cal.getTime();

    Enumeration validRanges = 
      endDatePref.getScoringFunction().getValidRanges (new TimeAspectValue (AspectType.END_TIME,
									    0l),
						       new TimeAspectValue (AspectType.END_TIME,
									    endOfRange));
    return validRanges;
  }

  protected static UTILEndDateScoringFunction getEndDateSF (Task t) {
    try {
      Preference endDatePref = getPrefWithAspectType(t, AspectType.END_TIME);
      ScoringFunction func = endDatePref.getScoringFunction ();
      if (!(func instanceof UTILEndDateScoringFunction))
	return null;
      return (UTILEndDateScoringFunction) endDatePref.getScoringFunction ();
    } catch (NullPointerException npe) {
      throw new UTILRuntimeException (classname + ".getBestDate () : \n\ttask\n\t" + t + 
				      "\n\thas no END_TIME preference to ask for best date.");
    }
  }

  private static final String classname = UTILPreference.class.getName ();
}
