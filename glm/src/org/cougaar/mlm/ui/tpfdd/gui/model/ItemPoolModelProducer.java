/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/gui/model/Attic/ItemPoolModelProducer.java,v 1.2 2002-01-30 21:58:59 ahelsing Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

/**
 * Simple pool model for things like LogPlans that take care of
 * rendering their own view of data and need to know only what's in it.
 * Implementations should take care to preserve ordering of events in
 * threaded environments to avoid inconsistencies with clients'
 * model-view.  The non-plural methods are for conenvience but usually
 * very convenient.
 */

package org.cougaar.mlm.ui.tpfdd.gui.model;


public interface ItemPoolModelProducer
{
    void addItemNotify(Object item);

    void deleteItemWithIndexNotify(Object item, int index);

    void deleteItemNotify(Object item);

    void changeItemNotify(Object item);

    void addItemsNotify(Object[] items);

    void deleteItemsNotify(Object[] items);

    void changeItemsNotify(Object[] items);

    void addConsumer(ItemPoolModelListener consumer);

    void deleteConsumer(ItemPoolModelListener consumer);
}
