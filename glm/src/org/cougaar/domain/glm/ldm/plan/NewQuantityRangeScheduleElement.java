/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.ldm.plan;

import org.cougaar.domain.planning.ldm.plan.*;
/**
 * NewQuantityRangeScheduleElement provides setters to build a complete object.
 **/

public interface NewQuantityRangeScheduleElement 
  extends QuantityRangeScheduleElement, NewScheduleElement 
{
  /** @param aStartQuantity set the start quantity related to this schedule */
  void setStartQuantity(double aStartQuantity);
	
  /** @param anEndQuantity set the end quantity related to this schedule */
  void setEndQuantity(double anEndQuantity);
	

}