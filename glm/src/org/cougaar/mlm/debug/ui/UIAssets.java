/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */


package org.cougaar.mlm.debug.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.ldm.asset.PhysicalAsset;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.util.UnaryPredicate;


/** Creates the values for a bar graph displaying assets owned by the cluster.
 */

public class UIAssets implements UIBarGraphSource, UISubscriber {
  private UIPlugin uiPlugin;
  private Vector listeners;
  private MessageAddress clusterId;
  private Date startDate;
  private int numberOfXIntervals;
  private String xLegend;
  private String xLabels[];
  private int numberOfYIntervals;
  private String yLegend;
  private String yLabels[];
  private String legend[] = new String[0]; // not used
  private int numberOfBars;
  // for each bar, the y-value for the xth interval
  private int values[][] = new int[1][]; 
  private boolean contiguous;
  private Vector myAssets = new Vector(10);

  /** Display the assets assigned to the cluster in a bar graph.
      The x-axis is type of asset (i.e. truck, tank, HET) and
      the y-axis is the quantity of that asset.
      @param uiPlugin this user interface plugin
      @param planName the name of the plan for which to display assets
      @param clusterId the cluster for which to display assets
      @exception UINoPlanException thrown when the plan does not exist
  */

  public UIAssets(UIPlugin uiPlugin, String planName, 
		  MessageAddress clusterId, 
		  Object listener) throws UINoPlanException {
    listeners = new Vector(1);
    listeners.addElement(listener);
    this.uiPlugin = uiPlugin;
    contiguous = false; // don't make bars in bar graph contiguous
  }

  /** Can't start subscription in constructor, because you could
    get a subscriptionChanged before the UIBarGraphDisplay is ready.
    */

  public void startSubscription() {
    uiPlugin.subscribe(this, assetPredicate());
  }

  private static UnaryPredicate assetPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	//System.out.println("Predicate called with: " + o.toString());
	return ( o instanceof Asset );
      }
    };
  }

  public synchronized void subscriptionChanged(IncrementalSubscription container) {
    //System.out.println("Container changed");
    Enumeration added = container.getAddedList();
    while (added.hasMoreElements())
      myAssets.addElement(added.nextElement());
    Enumeration removed = container.getRemovedList();
    while (removed.hasMoreElements())
      myAssets.removeElement(removed.nextElement());
    recomputeAssets();
    //////////////////////
//     clusterCollection = uiPlugin.getClusterCollection(clusterId);
//     plan = clusterCollection.getPlan(planName);
//     if (plan == null)
//       throw new UINoPlanException("There is no plan in cluster: " +
//                                   clusterId.getAddress() +
//                                   " named: " + planName);
//     computeAssets();
    //////////////////////
  }

  private void computeAssets() {
    Vector assetNames = new Vector(10);
    Vector assetCounts = new Vector(10);
    // count the assets
    for (int j = 0; j < myAssets.size(); j++) {
      Asset asset = (Asset)myAssets.elementAt(j);
      if (asset instanceof AggregateAsset) {
      	AggregateAsset aa = (AggregateAsset) asset;
        String name = UIAsset.getName(aa.getAsset());
        if ((name == null) || name.equals(""))
          continue;
        int quantity = 1;
        quantity = (int)aa.getQuantity();
        int i = assetNames.indexOf(name);
        if (i == -1) {
          assetNames.addElement(name);
          assetCounts.addElement(new Integer(quantity));
        } 
        else {
          Integer count = (Integer)assetCounts.elementAt(i);
          assetCounts.setElementAt(new Integer(count.intValue()+ quantity), i);
        }
        
      }else if (asset instanceof PhysicalAsset) {
        String name = UIAsset.getName(asset);
        if ((name == null) || name.equals(""))
          continue;
        int i = assetNames.indexOf(name);
        if (i == -1) {
          assetNames.addElement(name);
          assetCounts.addElement(new Integer(1));
        }
        else {
          Integer count = (Integer)assetCounts.elementAt(i);
          assetCounts.setElementAt(new Integer(count.intValue()+1), i);
        }
      }
    }
    int nAssets = assetNames.size();
    xLegend = "Assets";
    yLegend = "Quantity";
    numberOfXIntervals = nAssets;
    xLabels = new String[nAssets];
    assetNames.copyInto(xLabels);
    values[0] = new int[nAssets];
    for (int i = 0; i < nAssets; i++) {
      Integer count = (Integer)assetCounts.elementAt(i);
      values[0][i] = count.intValue();
    }
    int maxAssetCount = 0;
    for (int i = 0; i < nAssets; i++)
      maxAssetCount = Math.max(maxAssetCount, values[0][i]);
    numberOfYIntervals = maxAssetCount;
    yLabels = new String[numberOfYIntervals];
    for (int i = 0; i < numberOfYIntervals; i++)
      yLabels[i] = String.valueOf(i+1);
  }

  /** Number of different assets.
      @return number of different assets
  */

  public int getNumberOfXIntervals() {
    return numberOfXIntervals;
  }

  /** "Assets"
      @return "Assets"
  */

  public String getXLegend() {
    return xLegend;
  }

  /** Asset names.
      @return names of the assets
  */

  public String[] getXLabels() {
    return xLabels;
  }

  /** Maximum quantity of any one asset.
      @return maximum quantity of any one asset
  */

  public int getNumberOfYIntervals() {
    return numberOfYIntervals;
  }

  /** "Quantity"
      @return "Quantity"
  */

  public String getYLegend() {
    return yLegend;
  }

  /** Quantity of assets.
      @return numeric labels for quantities of assets
  */

  public String[] getYLabels() {
    return yLabels;
  }

  /** Not used. 
      @return empty
  */

  public String[] getLegend() {
    return legend;
  }

  /** Quantity of each asset.
      @return for each asset (with the same name), the quantity of that asset
  */

  public int[][] getValues() {
    return values;
  }

  /** Should the bars in the bar graph be contiguous.
      @return false
  */

  public boolean getContiguous() {
    return contiguous;
  }

  /** Listen for changes in type or number of assets assigned to the
      cluster.
      @param listener object to notify when assets change
  */

  public void registerListener(ActionListener listener) {
    // MIK
    // listeners.addElement(listener);
  }

  /** Handle added, deleted or changed events on the asset collection.
      Update the information to display and notify any listeners on this object.
      The UIBarGraphDisplay object listens for changes
      and invokes methods in the UIBarGraph object to get
      the updated values from this object and repaint the graph.
      @param e event (new, changed, or deleted object)
  */

  /*
  public synchronized void dispatchEvent( ObjectEvent e)
    {
      handleEvent( e );
    }
  */
  /*
  private void handleEvent(ObjectEvent e)
    {
      switch( e.getEventID() )
        {
        case ObjectEvent.NEW_OBJECT:
          processNewObjectEvent(e);
          break;
        case ObjectEvent.DELETE_OBJECT:
          processDeleteObjectEvent(e);
          break;
        case ObjectEvent.CHANGE_OBJECT:
          processChangedObjectEvent(e);
        default:
          System.err.println("Unrecognized event listener event: "+e);
          break;
        }
    }
  */

  /** In response to any addition, change or deletion to the assets,
      recompute the information for the bar graph display
      and notify listeners of the change.  
      The UIBarGraphDisplay object listens for changes
      and invokes methods in the UIBarGraph object to get
      the updated values from this object and repaint the graph.
  */

  private void recomputeAssets() {
    computeAssets();
    for (int i = 0; i < listeners.size(); i++) {
      ActionListener listener = (ActionListener)listeners.elementAt(i);
      listener.actionPerformed(new ActionEvent(this, 0, ""));
    }
  }

  /** New asset was added.
      @param e the new object event
  */

  /*
  public synchronized void processNewObjectEvent(ObjectEvent e) {
    recomputeAssets();
  }
  */

  /** Asset was changed.
      @param e the change object event
  */

  /* public synchronized void processChangedObjectEvent(ObjectEvent e) {
    recomputeAssets();
  }
  */
  /** Asset was deleted.
      @param e the deleted object event
  */

  /*
  public synchronized void processDeleteObjectEvent(ObjectEvent e) {
    recomputeAssets();
  }
  */

  /** Called to force an update of the asset information.
   */

  public void update() {
    recomputeAssets();
  }
}
