/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.assessor;

import javax.swing.*;
import java.awt.event.*;
import java.awt.LayoutManager;

//import java.io.*;
  
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.core.cluster.Subscriber;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.plugin.LDMService;
import org.cougaar.core.society.UID;

import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.AspectValue;
import org.cougaar.domain.planning.ldm.plan.ContextOfUIDs;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.plan.ScoringFunction;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.Verb;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.mlm.plugin.UICoordinator;

import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.oplan.Oplan;

/**
 * Publishes an AssessReadiness task to the logplan every time the button is pressed.
 *
 **/
public class InjectAssessReadinessGUIPlugin extends ComponentPlugin
{ 
  /** frame for UI **/
  private JFrame frame;

  /** for feedback to user on whether root GLS was successful **/
  JLabel label;

  protected JButton arButton;
  protected JButton rescindButton;

  protected RootFactory rootFactory;
  private long rollupSpan = 10;

  private IncrementalSubscription assessReadinessSubscription;
  private static UnaryPredicate assessReadinessPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Task) {
	Task t = (Task)o;
	if (t.getVerb().equals(Constants.Verb.AssessReadiness)) {
	  if (!t.getPrepositionalPhrases().hasMoreElements()) {
	    return true;
	  }
	}
      }
      return false;
    }
  };


  private IncrementalSubscription oplanSubscription;
  private static UnaryPredicate oplanPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Oplan) {
	return true;
      }
      return false;
    }
  };


  // called by introspection
  public void setLDMService(LDMService service) {
    rootFactory = service.getFactory();
  }

  protected void setupSubscriptions() 
  {	
    assessReadinessSubscription = (IncrementalSubscription) blackboard.subscribe(assessReadinessPredicate);
    oplanSubscription = (IncrementalSubscription) blackboard.subscribe(oplanPredicate);

    // refill contributors Collection on rehydrate
    processAdds(assessReadinessSubscription.getCollection());

    createGUI();
  }	   		 
  
  /**
   * Executes PlugIn functionality.
   */
  protected void execute(){

    if (assessReadinessSubscription.hasChanged()) {
      Collection adds = assessReadinessSubscription.getAddedCollection();
      if (adds != null) {
	processAdds(adds);
      }
      Collection changes = assessReadinessSubscription.getChangedCollection();
      if (changes != null) {
	processChanges(changes);
      }
      Collection deletes = assessReadinessSubscription.getRemovedCollection();
      if (deletes !=null) {
	processDeletes(deletes);
      }
    }
    
    if (oplanSubscription.hasChanged()) {
      if (oplanSubscription.size() > 0) 
	if (!arButton.isEnabled())
	  arButton.setEnabled(true);
      else
	arButton.setEnabled(false);
    }
  }

  private long getOplanStartTime() {
    // Yes, I know there can be more than one Oplan. I'll deal with it if I have time
    Oplan oplan = (Oplan) oplanSubscription.iterator().next();
    return oplan.getCday().getTime();
  }

  private UID getOplanUID() {
    // Yes, I know there can be more than one Oplan. I'll deal with it if I have time
    Oplan oplan = (Oplan) oplanSubscription.iterator().next();
    return oplan.getUID();
  }

  private void createGUI() {
    frame = new JFrame("InjectAssessReadinessGUIPlugin for " + getBindingSite().getAgentIdentifier());
    JPanel panel = new JPanel((LayoutManager) null);
    // Create the button
    arButton = new JButton("Publish AssessReadiness Task");
    arButton.setEnabled(false);
    label = new JLabel("No AssessReadiness task have been published.");
    // Register a listener for the check box
    ARButtonListener myARListener = new ARButtonListener();
    arButton.addActionListener(myARListener);
    UICoordinator.layoutButtonAndLabel(panel, arButton, label);

    rescindButton = new JButton("Rescind AssessReadiness Tasks");
    rescindButton.setEnabled(false);
    RescindButtonListener myRescindListener = new RescindButtonListener();
    rescindButton.addActionListener(myRescindListener);
    UICoordinator.layoutButton(panel, rescindButton);

    frame.setContentPane(panel);
    frame.pack();
    UICoordinator.setBounds(frame);
    frame.setVisible(true);
  }
 
  /** An ActionListener that listens to the GLS buttons. */
  class ARButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      rescindAssessReadiness();
      publishAssessReadiness();
    }
  }

  private void updateLabel() {
    int subSize = assessReadinessSubscription.size();
    label.setText(subSize +  " AssessReadiness tasks on blackboard");
    if (subSize > 0)
      rescindButton.setEnabled(true);
    else
      rescindButton.setEnabled(false);
  }

  private void publishAssessReadiness() {
    blackboard.openTransaction();
    NewTask task = rootFactory.newTask();
    task.setVerb(Constants.Verb.AssessReadiness);
    
    Vector prefs = new Vector(2);
    Preference p = rootFactory.newPreference(AspectType.START_TIME, 
					     ScoringFunction.createStrictlyAtValue(new AspectValue(AspectType.START_TIME, getOplanStartTime())));

    prefs.add(p);
    p = rootFactory.newPreference(AspectType.INTERVAL,
				  ScoringFunction.createStrictlyAtValue(new AspectValue(AspectType.INTERVAL, rollupSpan)));
    prefs.add(p);

    task.setPreferences(prefs.elements());
    task.setContext(new ContextOfUIDs(getOplanUID()));
    blackboard.publishAdd(task);
    blackboard.closeTransaction(false);
    updateLabel();
  }


  /** An ActionListener that listens to the Rescind button. */
  class RescindButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      rescindAssessReadiness();
    }
  }

  private void rescindAssessReadiness() {
    blackboard.openTransaction();
    for (Iterator taskIt = assessReadinessSubscription.iterator(); taskIt.hasNext();) {
      Task task = (Task) taskIt.next();
      blackboard.publishRemove(task);
    }
    blackboard.closeTransaction(false);
    updateLabel();
  }

  private void processAdds(Collection adds) {
    for (Iterator it = adds.iterator(); it.hasNext();) {
      Task t = (Task) it.next();
      updateLabel();
    }
  }

  private void processChanges(Collection changes) {
  }

  private void processDeletes(Collection deletes) {
    for (Iterator it = deletes.iterator(); it.hasNext();) {
      Task t = (Task) it.next();
      updateLabel();
    }
  }

  // found by introspection
  public void setParameter(Object param) {
    System.out.println("InjectAssessReadiness.setParameter()");
    for (Iterator paramIt = ((Collection)param).iterator(); paramIt.hasNext();) {
      String sParam = (String)paramIt.next();
      int sep = sParam.indexOf('=');
      if (sep > 0) {
        String name=sParam.substring(0, sep).trim();
        String val=sParam.substring(sep+1).trim();
	if (name.equalsIgnoreCase("rollupspan"))
	  rollupSpan = Long.parseLong(val);
	else
	  System.out.println("InjectAssessReadinessGUIPlugin.parseParameters() unexpected parameter " + sParam);
      }
    }
  }
}











