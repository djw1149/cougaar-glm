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
import org.cougaar.domain.planning.ldm.measure.Capacity;

/**
 * A CapacityScheduleElement is a subclass of ScheduleElement which
 * provides a slot for a Capacity.
 **/

public interface CapacityScheduleElement 
  extends ScheduleElement 
{
  /** @return The capacity related to this schedule */
  Capacity getCapacity();
}