/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/xml/Attic/FacilityPG.java,v 1.2 2001-02-23 01:02:22 wseitz Exp $ */

/*
  Copyright (C) 1998-1999 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.newtpfdd.xml;


import org.w3c.dom.Element;


public class FacilityPG extends LogPlanObject
{
    public FacilityPG(Element xml)
    {
	super(null, xml);
    }

    // Yes, this comes back from the current XML servers as an empty object.
    // Here for algorithmic consistency, and if any fields ever do appear.
}