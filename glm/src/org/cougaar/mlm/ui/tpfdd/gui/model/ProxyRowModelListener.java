/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/gui/model/Attic/ProxyRowModelListener.java,v 1.1 2001-12-27 22:44:23 bdepass Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

/**
 * Proxy to help flesh out the AscentConsumer part of implementations
 * of the RowModelListener interface.  This way they don't have to
 * implement the below three methods themselves.  Not that these methods
 * are huge or anything, but in principle it saves repitition and allows
 * flexibility for change.
*/

package org.cougaar.mlm.ui.tpfdd.gui.model;


import org.cougaar.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.mlm.ui.tpfdd.util.Consumer;


public class ProxyRowModelListener implements Consumer
{
    private RowModelListener peer;

    public ProxyRowModelListener(RowModelListener peer)
    {
	this.peer = peer;
    }
    
    public void fireAddition(Object item)
    {
	if ( !(item instanceof Integer) ) {
	    OutputHandler.out("SRML:fA Error: Received non-integer: " + item.getClass().getName());
	    return;
	}
	peer.fireRowAdded(((Integer)item).intValue());
    }

    public void fireDeletion(Object item)
    {
	if ( !(item instanceof Integer) ) {
	    OutputHandler.out("SRML:fD Error: Received non-integer: " + item.getClass().getName());
	    return;
	}
	peer.fireRowDeleted(((Integer)item).intValue());
    }

    public void fireChange(Object item)
    {
	if ( !(item instanceof Integer) ) {
	    OutputHandler.out("SRML:fC Error: Received non-integer: " + item.getClass().getName());
	    return;
	}
	peer.fireRowChanged(((Integer)item).intValue());
    }

    public void firingComplete()
    {
	peer.firingComplete();
    }
}