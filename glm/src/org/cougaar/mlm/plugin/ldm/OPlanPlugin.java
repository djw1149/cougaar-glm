/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.mlm.plugin.ldm;

import javax.swing.*;
import java.awt.event.*;
import java.awt.LayoutManager;

//import java.io.*;
  
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.core.blackboard.Subscriber;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.SimplePlugin;
import org.cougaar.core.util.UID;

import org.cougaar.core.domain.RootFactory;

import org.cougaar.util.UnaryPredicate;

/*  
import org.cougaar.glm.ldm.*;
import org.cougaar.glm.*;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.asset.*;
*/
import org.cougaar.glm.ldm.oplan.*;

import org.cougaar.mlm.plugin.UICoordinator;


/**
 * The OPlanPlugin instantiates the OPlan
 * and adds it to the LogPlan.
 *
 **/
public class OPlanPlugin extends SimplePlugin 
{ 
  /** frame for 1-button UI **/
  private JFrame frame;

  /** for feedback to user on whether root GLS was successful **/
  JLabel oplanLabel;

  protected JButton oplanButton;

  private IncrementalSubscription oplanSubscription;

  private IncrementalSubscription stateSubscription;

  private ArrayList contributors;

  private static class MyPrivateState implements java.io.Serializable {
    boolean oplanExists = false;
    boolean errorOccurred = false;
  }

  private MyPrivateState myPrivateState;

  private static UnaryPredicate oplanPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Oplan);
    }
  };

  private static UnaryPredicate statePredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof MyPrivateState);
    }
  };

  /*
   * Creates a subscription.
   */
  protected void setupSubscriptions() 
  {	
    getBlackboardService().setShouldBePersisted(false);
    oplanSubscription = (IncrementalSubscription) subscribe(oplanPredicate);
    stateSubscription = (IncrementalSubscription) subscribe(statePredicate);

    contributors = new ArrayList(13);
    // refill contributors Collection on rehydrate
    processOplanAdds(oplanSubscription.getCollection());

    createGUI();

    if (didRehydrate()) {
      checkForPrivateState(stateSubscription.elements());
    } else {
      publishAdd(new MyPrivateState());
    }
  }	   		 
  
  
  /**
   * Executes Plugin functionality.
   */
  protected void execute(){
    if (stateSubscription.hasChanged()) {
      checkForPrivateState(stateSubscription.getAddedList());
    }
    if (oplanSubscription.hasChanged()) {
      Collection adds = oplanSubscription.getAddedCollection();
      if (adds != null) {
	processOplanAdds(adds);
      }
      Collection changes = oplanSubscription.getChangedCollection();
      if (changes != null) {
	processChanges(changes);
      }
      Collection deletes = oplanSubscription.getRemovedCollection();
      if (deletes !=null) {
	processOplanDeletes(deletes);
      }
    }

    for (Iterator it = contributors.iterator(); it.hasNext();) {
      IncrementalSubscription is = (IncrementalSubscription) it.next();
      // we don't care about adds, just deletes and changes
      if (is.hasChanged()) {
	Collection changes = is.getChangedCollection();
	if (changes != null) {
	  processChanges(changes);
	}

	// Deletes of OplanContributors are treated as changes to the Oplan
	changes = is.getRemovedCollection();
	if (changes != null) {
	  processChanges(changes);
	}	
      }
    }
  }

  private void checkForPrivateState(Enumeration e) {
    if (myPrivateState == null) {
      while(e.hasMoreElements()) {
        myPrivateState = (MyPrivateState) e.nextElement();
        checkButtonEnable();
      }
    }
  }

  private void createGUI() {
    frame = new JFrame("OplanPlugin");
    JPanel panel = new JPanel((LayoutManager) null);
    // Create the button
    oplanButton = new JButton("Publish Oplan");
    oplanLabel = new JLabel("No Oplan has been published.");

    // Register a listener for the check box
    OplanButtonListener myOplanListener = new OplanButtonListener();
    oplanButton.addActionListener(myOplanListener);
    oplanButton.setEnabled(false);
    UICoordinator.layoutButtonAndLabel(panel, oplanButton, oplanLabel);
	frame.getRootPane().setDefaultButton(oplanButton); // hitting return sends the oplan
    frame.setContentPane(panel);
    frame.pack();
    UICoordinator.setBounds(frame);
    frame.setVisible(true);
  }
 
  /** An ActionListener that listens to the GLS buttons. */
  class OplanButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      publishOplan();
    }
  }

  private void checkButtonEnable() {
    // Log status
    oplanButton.setEnabled(!myPrivateState.oplanExists);
    if (myPrivateState.oplanExists) {
      oplanLabel.setText("Published successfully");
    } else if (myPrivateState.errorOccurred) {
      oplanLabel.setText("ERROR::Unable to add the Oplan to the LogPlan.");
    } else {
      oplanLabel.setText("No Oplan has been published.");
    }
  }

  private void publishOplan() {
    // Get the Plugin Parameters
    Vector params = getParameters();
    ArrayList oplans = new ArrayList(params.size());
    ArrayList oplanComponents = new ArrayList(params.size());
    // Instantiate the OPlanFileReader to create the OPlan
    if (params.size() != 0) {
      for (int i = 0, n = params.size(); i < n; i++) {
        String fileName = (String)(params.elementAt(i));
        OplanFileReader ofr =
          new OplanFileReader(fileName, getFactory(), getCluster());
        Oplan oplan = ofr.readOplan();
        oplans.add(oplan);
        oplanComponents.addAll(ofr.getForcePackages(oplan));
        oplanComponents.addAll(ofr.getOrgActivities(oplan));
        oplanComponents.addAll(ofr.getOrgRelations(oplan));
        oplanComponents.addAll(ofr.getPolicies(oplan));

        if (oplan.getEndDay() == null) {
          oplan.inferEndDay(oplanComponents);
        }
      }
    } else {
      System.err.println("OPlanPlugin : No parameters were specified");
    }
    openTransaction();
    int n = oplans.size();
    if (n > 0) {
      for (int i = 0; i < n; i++) {
        Oplan oplan = (Oplan) oplans.get(i);
        // Add the OPLAN to the LogPlan.
        publishAdd(oplan);
        // publish the subs
        for (Iterator iterator = oplanComponents.iterator();
             iterator.hasNext();) {
          publishAdd(iterator.next());
        }

        OplanCoupon ow = new OplanCoupon(oplan.getUID(), getClusterIdentifier());
        getCluster().getUIDServer().registerUniqueObject(ow);
        publishAdd(ow);
      }
      myPrivateState.oplanExists = true;
    } else {
      myPrivateState.errorOccurred = true;
    }

    publishChange(myPrivateState);
    closeTransaction(false);
    checkButtonEnable();
  }

  private void processOplanAdds(Collection adds) {
    for (Iterator it = adds.iterator(); it.hasNext();) {
      Oplan oplan = (Oplan) it.next();
      
      IncrementalSubscription is = (IncrementalSubscription) 
	subscribe(new ContributorPredicate(oplan.getUID()));
      contributors.add(is);
    }
  }

  private void processChanges(Collection changes) {
    for (Iterator it = changes.iterator(); it.hasNext();) {
      UID oplanUID = null;
      Object o = it.next();
      if (o instanceof Oplan) {
	oplanUID = ((Oplan) o).getUID();
      } else if (o instanceof OplanContributor) {
	oplanUID = ((OplanContributor) o).getOplanUID();
      } else continue;

      Collection coupons = query( new CouponPredicate(oplanUID) );
      for (Iterator couponIt = coupons.iterator(); couponIt.hasNext();) {
	//System.out.println("OPlanPlugin: publishChanging OplanCoupon");
	publishChange(couponIt.next());
      }
    }
  }

  private void processOplanDeletes(Collection deletes) {
    for (Iterator it = deletes.iterator(); it.hasNext();) {
      Oplan oplan = (Oplan) it.next();
      Collection coupons = query( new CouponPredicate(oplan.getUID()) );
      for (Iterator couponIt = coupons.iterator(); couponIt.hasNext();) {
	publishRemove(couponIt.next());
      }
    }
  }

  private class CouponPredicate implements UnaryPredicate {
    UID _oplanUID;
    public CouponPredicate(UID oplanUID) {
      _oplanUID = oplanUID;
    }
    public boolean execute(Object o) {
      if (o instanceof OplanCoupon) {
	if (((OplanCoupon ) o).getOplanUID().equals(_oplanUID)) {
	  return true;
	}
      }
      return false;
    }
  }

  private class ContributorPredicate implements UnaryPredicate {
    UID _oplanUID;
    public ContributorPredicate(UID oplanUID) {
      _oplanUID = oplanUID;
    }
    public boolean execute(Object o) {
      if (o instanceof OplanContributor) {
	if (((OplanContributor ) o).getOplanUID().equals(_oplanUID)) {
	  return true;
	}
      }
      return false;
    }
  }
}










