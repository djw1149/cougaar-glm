/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/gui/view/Attic/ScheduleElement.java,v 1.2 2002-01-30 21:58:59 ahelsing Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Sundar Narasimhan, Daniel Bromberg
*/

package org.cougaar.mlm.ui.tpfdd.gui.view;


public interface ScheduleElement
{
    String getUUID();

    long getActualStart();

    long getActualEnd();

    long getEstimatedStart();

    long getEstimatedEnd();

    int getMode();

    boolean isContiguous();
}
