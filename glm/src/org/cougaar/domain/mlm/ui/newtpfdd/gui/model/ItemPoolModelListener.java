/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/gui/model/Attic/ItemPoolModelListener.java,v 1.1 2001-02-22 22:42:24 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

/**
  Simple pool model for things like LogPlans that take care of
  rendering their own view of data and need to know only what's in it.
  Implementations should do error-checking on "item" to catch dataflow
  bugs -- possible to be told to do something inconsistent with known
  model.  This is a bit redundant WRT AscentConsumer; there is no
  specialization like in RowModelListener.  Keeping it anyway for
  parallelness with RowModel; might add future flexibilty.
*/

package org.cougaar.domain.mlm.ui.tpfdd.gui.model;


import org.cougaar.domain.mlm.ui.tpfdd.util.Consumer;


public interface ItemPoolModelListener extends Consumer
{
    public void fireItemAdded(Object item);

    public void fireItemDeleted(Object item);

    public void fireItemValueChanged(Object item);

    public void fireItemWithIndexDeleted(Object item, int index);
}
