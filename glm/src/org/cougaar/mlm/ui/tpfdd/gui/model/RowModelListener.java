/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/gui/model/Attic/RowModelListener.java,v 1.2 2002-01-30 21:58:59 ahelsing Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

/**
 * Simple row-model for things like GanttCharts that take care of
 * rendering their rows but need to know when to.  Implementations
 * should do error-checking on "row" to catch dataflow bugs -- possible
 * to be told to do something inconsistent with known model.
*/

package org.cougaar.mlm.ui.tpfdd.gui.model;


import org.cougaar.mlm.ui.tpfdd.util.Consumer;


public interface RowModelListener extends Consumer
{
    void fireRowAdded(int row);

    void fireRowDeleted(int row);

    void fireRowChanged(int row);
}
