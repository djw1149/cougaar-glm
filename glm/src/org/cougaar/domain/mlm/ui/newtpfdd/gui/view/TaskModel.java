/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/gui/view/Attic/TaskModel.java,v 1.2 2001-02-23 01:02:18 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Sundar Narasimhan, Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.newtpfdd.gui.view;


import java.util.Date;
import java.util.Vector;

import org.cougaar.domain.mlm.ui.newtpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.newtpfdd.util.OutputHandler;

import org.cougaar.domain.mlm.ui.newtpfdd.producer.ClusterCache;
import org.cougaar.domain.mlm.ui.newtpfdd.producer.PlanElementProvider;


public class TaskModel extends AbstractTreeTableModel implements TreeTableModel
{
    private PlanElementProvider provider;

    protected Date minDate, maxDate;

    static protected String[] columnNames =
    {"Name", "What", "From", "To", "During"};

    static protected Class[] columnTypes =
    { TreeTableModel.class, String.class, String.class, 
      String.class, Node.class };

    public TaskModel(PlanElementProvider provider)
    { 
	super(provider.getRoot());
	this.provider = provider;
    }

    public Date getMinDate()
    {
	return minDate;
    }

    public Date getMaxDate()
    {
	return maxDate;
    }

    /**
     * Builds the parents of node up to and including the root node,
     * where the original node is the last element in the returned array.
     * The length of the returned array gives the node's depth in the
     * tree.
     * 
     * @param aNode  the TreeNode to get the path for
     * @param depth  an int giving the number of steps already taken towards
     *        the root (on recursive calls), used to size the returned array
     * @return an array of TreeNodes giving the path from the root to the
     *         specified node 
     */
    // public methods to manipulate roots
    public Object[] getPathToNode(Object node)
    {
	return getPathToNode(node, 0);
    }

    private Object[] getPathToNode(Object aNode, int depth)
    {
        Object[] retNodes;
	// This method recurses, traversing towards the root in order
	// size the array. On the way back, it fills in the nodes,
	// starting from the root and working back to the original node.

        /* Check for null, in case someone passed in a null node, or
           they passed in an element that isn't rooted at root. */
        if ( aNode == null ) {
            if ( depth == 0 )
                return null;
            else
                retNodes = new Object[depth];
        }
        else {
            depth++;
            if ( aNode == root )
                retNodes = new Object[depth];
            else
                retNodes = getPathToNode(((Node)aNode).getParent(), depth);
            retNodes[retNodes.length - depth] = aNode;
        }
        return retNodes;
    }

    // For ScheduleCellRenderer
    public PlanElementProvider getProvider()
    {
	return provider;
    }

    //
    // The TreeModel interface
    //
    public int getChildCount(Object parent)
    {
	return provider.getChildCount((Node)parent);
    }

    public Object getChild(Object node, int index)
    {
	return provider.getChild(node, index);
    }

    // This is officially part of the model but not used in JTree's default
    // mode. However, it's useful for our own code for calculating the update
    // event messages.
    public int getIndexOfChild(Object parent, Object child)
    {
	return provider.getIndexOfChild(parent, child);
    }

    // The superclass's implementation would work, but this is more efficient. 
    public boolean isLeaf(Object node)
    { 
	if ( node == root )
	    return false;
	return !((Node)node).hasChildren();
    }

    //
    //  The TreeTableNode interface. 
    //
    public int getColumnCount()
    {
	return columnNames.length;
    }

    public String getColumnName(int column)
    {
	if ( column < 0 || column > 4 ) {
	    OutputHandler.out("Error: getColumnName() received invalid column: " + column);
	    return null;
	}
	return columnNames[column];
    }

    public Class getColumnClass(int column)
    {
	if ( column < 0 || column > 4 ) {
	    OutputHandler.out("Error: getColumnClass() received invalid column: " + column);
	    return null;
	}
	return columnTypes[column];
    }

    public Object getValueAt(Object item, int column)
    {
	if ( !(item instanceof Node) ) {
	    OutputHandler.out("Error: getValueAt() received item of class " + item.getClass()
			       + ", expecting Node");
	    return null;
	}
	Node node = (Node)item;



	if ( column == 4 )
	    return item;

	if ( node == root ) {
	    if ( column == 0 )
		return "ALP Tasks";
	    if ( column > 0 && column <= 5 )
		return "";
	}

	switch ( column ) {
	    case 0:
		return node.getDisplayName();
	    case 1:
		if (node instanceof TypeNode)
		    return ((TypeNode)node).getCargoName();
	    case 2:
		if ( node instanceof ItineraryNode )
		    return node.getChild(0).getFromName();
		return node.getFromName();
	    case 3:
		if ( node instanceof ItineraryNode )
		    return node.getChild(node.getChildCount() - 1).getToName();
		return node.getToName();

	    case 4:
		return node;
	}
   
	OutputHandler.out("Error: getValueAt() received invalid column: " + column);
	return null;
    }
}
