/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/gui/component/Attic/VirtualXComponent.java,v 1.1 2001-12-27 22:44:23 bdepass Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Harry Tsai
*/

package org.cougaar.mlm.ui.tpfdd.gui.component;

/**
  * A VirtualXComponent has an extra coordinate system, in addition to
  * those present in a regular Component.  This virtual coordinate system
  * has one dimension, X, which is of type long.
  * <p>
  * It is the responsibility of the parent VirtualXContainer to translate
  * virtual coordinates into screen coordinates and reshape the VirtualXComponent
  * accordingly.
  */

import java.awt.Component;

public class VirtualXComponent extends Component
implements VirtualX
{
	private long myVirtualLeft;
	private long myVirtualWidth;

	public long getVirtualXLocation() { return myVirtualLeft; }
	public long getVirtualXSize() { return myVirtualWidth; }
	public int ScreenWidthOfVirtualWidth( long vw )
	{
		return
			(int)(
				(double)vw / (double)getVirtualXSize()
			    * getSize().width );
	}
	public int ScreenXOfVirtualX( long vx )
	{
		return ScreenWidthOfVirtualWidth( vx - getVirtualXLocation() );
	}
	public void setVirtualXLocation( long newLocation )
	{ myVirtualLeft = newLocation; }
	public void setVirtualXSize( long newSize )
	{ myVirtualWidth = newSize; }
}