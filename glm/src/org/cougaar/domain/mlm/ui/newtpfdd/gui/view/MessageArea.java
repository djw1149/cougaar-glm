/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/gui/view/Attic/MessageArea.java,v 1.1 2001-02-22 22:42:27 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.tpfdd.gui.view;


import javax.swing.SwingUtilities;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;

import org.cougaar.domain.mlm.ui.tpfdd.util.SwingQueue;

import org.cougaar.domain.mlm.ui.tpfdd.gui.model.ItemPoolModelListener;


public class MessageArea extends JTextArea implements ItemPoolModelListener
{
    JScrollBar scrollbar;

    public MessageArea(String text, int rows, int columns)
    {
	super(text, rows, columns);
    }

    public void setScrollBar(JScrollBar scrollbar)
    {
	this.scrollbar = scrollbar;
    }

    public void fireAddition(Object o)
    {
	fireItemAdded(o);
    }

    public void fireDeletion(Object o)
    {
	fireItemAdded(o);
    }

    public void fireChange(Object o)
    {
	fireItemValueChanged(o);
    }

    public void fireItemAdded(Object o)
    {
	myAppend(o.toString());
    }

    public void fireItemValueChanged(Object o)
    {
	myAppend("MA:fID not implemented.\n");
    }

    public void fireItemDeleted(Object o)
    {
	myAppend("MA:fID: not implemented.\n");
    }

    public void fireItemWithIndexDeleted(Object item, int index)
    {
	myAppend("MA:fIWID: Error: UNIMPLEMENTED Should not ever be called.");
    }

    public void firingComplete()
    {
	invalidate();
	repaint();
    }

    // IMPORTANT: this could get called from any thread.
    public void myAppend(String str)
    {
	final String message = str;
	final MessageArea me = this;
	final JScrollBar myScrollbar = scrollbar;

	Runnable appendIt = new Runnable() {
		public void run()
		{
		    me.append(message);
		    // if ( myScrollbar != null )
		    // myScrollbar.setValue(myScrollbar.getMaximum() - myScrollbar.getVisibleAmount());
		    firingComplete();
		}
	    };
	SwingQueue.invokeLater(appendIt);
    }
}
