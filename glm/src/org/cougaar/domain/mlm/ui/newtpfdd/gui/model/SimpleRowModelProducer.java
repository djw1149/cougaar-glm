/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/gui/model/Attic/SimpleRowModelProducer.java,v 1.2 2001-02-23 01:02:16 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

/**
 * Straightforward implementation of an RowModelProducer. Meant to be
 * extended or contained by classes that really get rows to produce.
 */

package org.cougaar.domain.mlm.ui.newtpfdd.gui.model;


public class SimpleRowModelProducer extends SimpleProducer
    implements RowModelProducer
{
    public SimpleRowModelProducer()
    {
	super("Simple Row Model Producer");
    }

    public void addRowNotify(int row)
    {
	addNotify(new Integer(row));
    }

    public void deleteRowNotify(int row)
    {
	deleteNotify(new Integer(row));
    }

    public void changeRowNotify(int row)
    {
	changeNotify(new Integer(row));
    }

    private Object[] toInteger(int[] rows)
    {
	Object[] rowInts = new Integer[rows.length];
	for ( int i = 0; i < rows.length; i++ )
	    rowInts[i] = new Integer(rows[i]);
	return rowInts;
    }
    
    public void addRowsNotify(int[] rows)
    {
	addNotify(toInteger(rows));
    }

    public void deleteRowsNotify(int[] rows)
    {
	deleteNotify(toInteger(rows));
    }

    public void changeRowsNotify(int[] rows)
    {
	changeNotify(toInteger(rows));
    }

    public void addConsumer(RowModelListener consumer)
    {
	super.addConsumer(consumer);
    }

    public void deleteConsumer(RowModelListener consumer)
    {
	super.deleteConsumer(consumer);
    }
}