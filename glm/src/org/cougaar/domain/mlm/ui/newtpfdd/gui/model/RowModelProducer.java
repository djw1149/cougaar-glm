/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/gui/model/Attic/RowModelProducer.java,v 1.2 2001-02-23 01:02:16 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

/**
 * Producer for simple row-model for things like GanttCharts that take
 * care of rendering their rows but need to know when to.
 * Implementations should take care to preserve ordering of events in
 * threaded environments to avoid inconsistencies with clients'
 * model-view.
*/

package org.cougaar.domain.mlm.ui.newtpfdd.gui.model;


public interface RowModelProducer
{
    void addRowNotify(int row);

    void deleteRowNotify(int row);

    void changeRowNotify(int row);

    void addRowsNotify(int[] rows);
    
    void deleteRowsNotify(int[] rows);

    void changeRowsNotify(int[] rows);

    public void addConsumer(RowModelListener consumer);

    public void deleteConsumer(RowModelListener consumer);
}