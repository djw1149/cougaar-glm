/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.glm.ldm.plan;

import java.util.ArrayList;
import java.util.Date;

import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleImpl;
import org.cougaar.util.Collectors;
import org.cougaar.util.Thunk;

public class LaborScheduleImpl 
  extends ScheduleImpl 
  implements LaborSchedule 
{

  private Schedule qtySchedule;
  private Schedule rateSchedule;

  /** constructor for factory that takes both schedules **/
  public LaborScheduleImpl(Schedule qsched, Schedule rsched) {
    super(getCombinedSet(qsched,rsched));
    qtySchedule = qsched;
    rateSchedule = rsched;

    // For now all labor schedules will represent a type of TOTAL_CAPACITY.
    // Also, all returns from the Schedule interface will return the 
    // product of the quantityscheduleelements and the ratescheduleelements
    // in the form of quantityscheduleelements.

    this.scheduleType = PlanScheduleType.TOTAL_CAPACITY;
    this.scheduleElementType = PlanScheduleElementType.QUANTITY;
  }
  
  public Schedule getQuantitySchedule() {
    return qtySchedule;
  }
  
  public Schedule getRateSchedule() {
    return rateSchedule;
  }
  
  public synchronized void setQuantitySchedule(Schedule aQuantitySchedule) {
    qtySchedule = aQuantitySchedule;
    super.clear();
    super.addAll(getCombinedSet(qtySchedule,rateSchedule));
  }
  
  public synchronized void setRateSchedule(Schedule aRateSchedule) {
    rateSchedule = aRateSchedule;
    super.clear();
    super.addAll(getCombinedSet(qtySchedule,rateSchedule));
  }
  
  
  // use the qtySchedule for this info
  /** @deprecated use getStartTime() */
  public synchronized Date getStartDate() {
    return new Date(qtySchedule.getStartTime());
  }

  // duplicated code from above to avoid consing a date
  public synchronized long getStartTime() {
    return qtySchedule.getStartTime();
  }

  // use the qtySchedule for this info
  /** @deprecated use getEndTime() */
  public synchronized Date getEndDate() {
    return new Date(qtySchedule.getEndTime());
  }
  // duplicated above to avoid consing the date
  public synchronized long getEndTime() {
    return qtySchedule.getEndTime();
  }
  
  private static ArrayList getCombinedSet(Schedule q, Schedule r) {
    return getProductElements(q, r);
  }

  private static final ArrayList emptySet = new ArrayList(0);

  private static ArrayList getProductElements(Schedule qtys, final Schedule rates) {
    // if one of the schedules has no matching elements, return an empty OrderedSet.
    if (qtys.isEmpty() || rates.isEmpty()) {
      return emptySet;
    }

    // make a new orderedset to hold the new quantityschedulelelements
    final Schedule result = new ScheduleImpl();
    
    Thunk qt = new Thunk() {
        public void apply(Object o) {
          final QuantityScheduleElement qse = (QuantityScheduleElement) o;
          final long qse_s = qse.getStartTime();
          final long qse_e = qse.getEndTime();
          final double q = qse.getQuantity();

          Thunk rt = new Thunk() {
              public void apply(Object ro) {
                RateScheduleElement rse = (RateScheduleElement) ro;
                long rse_s = rse.getStartTime();
                long rse_e = rse.getEndTime();
                if (rse_s < qse_e && rse_e >qse_s) {
                  // find the boundaries
                  long s = (qse_s>rse_s)?qse_s:rse_s;
                  long e = (qse_e<rse_e)?qse_e:rse_e;
                  
                  result.add(newQSE(s,e,q*rse.getRate()));
                }
              }
            };
          Collectors.apply(rt, rates);
        }
      };
    Collectors.apply(qt, qtys);
    return new ArrayList(result);
  }

          
  private static QuantityScheduleElement newQSE(long start, long end, double v) {
    return new QuantityScheduleElementImpl(start, end, v);
  }


  public String toString() {
    String tstring = "?";
    String setstring = "?";
    if (scheduleType!=null) tstring = scheduleType;
    if (scheduleElementType != null) setstring = scheduleElementType.toString();
      
    String qestring = qtySchedule.toString();
    String restring = rateSchedule.toString();
    return "<LaborSchedule "+tstring+" "+setstring+EOL+"\tQuantities: "+qestring+
      EOL+"\tRates: "+restring+">"+EOL;
  }
}
