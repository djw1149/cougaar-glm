/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/producer/Attic/IDKey.java,v 1.1 2001-12-27 22:44:29 bdepass Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.mlm.ui.tpfdd.producer;


import java.lang.Comparable;

import org.cougaar.mlm.ui.tpfdd.util.OutputHandler;


public class IDKey implements Comparable
{
    String ID;

    IDKey(String ID)
    {
	this.ID = ID;
    }

    public int compareTo(Object object)
    {
	if ( !(object instanceof IDKey) )
	    OutputHandler.out("DK:cT Error: " + getClass() + " cannot compare to " + object.getClass());
	IDKey otherKey = (IDKey)object;
	return ID.compareTo(otherKey.ID);
    }

    public String toString()
    {
	return ID;
    }
}