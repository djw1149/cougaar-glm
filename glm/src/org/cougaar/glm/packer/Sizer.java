// Copyright (11/99) Honeywell Inc.
// Unpublished - All rights reserved. This software was developed with funding 
// under U.S. government contract MDA972-97-C-0800

package org.cougaar.glm.packer;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.util.log.Logging;

import org.cougaar.planning.ldm.measure.Mass;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.core.domain.RootFactory;


import org.cougaar.glm.ldm.asset.GLMAsset;

/**
  * This class provides one of two "wheels" that drive the packing process.
  * This class provides a Generator that yields a stream of Tasks that
  * are components of the input tasks and that are sized per the
  * requirements that are generated by the Filler.
  * @see Filler
  */
class Sizer {
  static public final String TRUE = "True";

  /**
    * How much of the quantity remains in the current task
    */
  private double _remainder;

  /**
    * The tasks we are packing from
    */
  private ArrayList _tasks;

  /**
    * As we break the current task, of the tasks to be packed,
    * into right-sized bits, we accumulate component tasks for
    * a subsidiary workflow in this SList.
    */
  private ArrayList _expansionQueue;

  /**
    * The task we are currently processing.
    */
  private Task _curTask;

  private GenericPlugin _gp;

  Sizer(ArrayList _packus, GenericPlugin gp) {
    _tasks = new ArrayList(_packus);
    _gp = gp;
    _remainder = 0.0;
  }

  /**
    * The Filler will request some amount of quantity from the
    * Sizer.
    * This method will return a Task whose quantity is <= to requestedAmt,
    * or null if there are no more tasks to be packed.
    */
  Task provide(double requestedAmt) {
    Task ret = null;

    // first, if we've gotten a request, we need to check
    // to see if the _curTask has anything left in it...
    if (_remainder == 0.0) {
      if ((_curTask = getNextTask()) != null) {
	Mass taskMass = getTaskMass(_curTask);
	_remainder = taskMass.getShortTons();

	if (_expansionQueue != null) {
	  _gp.getLoggingService().error ("Sizer.provide : ERROR - Expansion queue is not null - we will drop tasks :");
	  for (Iterator iter = _expansionQueue.iterator(); iter.hasNext(); )
	    _gp.getLoggingService().error ("\t" + ((Task)iter.next()).getUID());
	}
        _expansionQueue = new ArrayList();
      } else {
        return null;
      }
    }
    // precondition:  _curTask is bound to a Task and remainder >= 0.0
    // remainder can be == 0.0 because some Plugin developers make such
    // tasks instead of rescinding requests.

    if (_remainder <= requestedAmt ) {
      // we are going to use up the entire _curTask and need to
      // take care of the expansion
      ret = sizedTask(_curTask, _remainder);
      _expansionQueue.add(0, ret);
      makeExpansion(_curTask, _expansionQueue);
      _expansionQueue = null; // it has been used - we shouldn't try to use it again
      _remainder = 0.0;
    } else {
      ret = sizedTask(_curTask, requestedAmt);
      _expansionQueue.add(0, ret);
      _remainder = _remainder - requestedAmt;
      if (_remainder == 0.0)
	_gp.getLoggingService().error ("Sizer.provide : ERROR - We will drop task " + ret.getUID());
    }
    return ret;
  }

  /**
    * This method should only be called after areMoreTasks
    * has returned true; it does no safety checking itself.
    * @see #areMoreTasks
    */
  private Task getNextTask() {
    if (!_tasks.isEmpty()) {
      Task t = (Task)_tasks.remove(0);
      return t;
    } else {
      return null;
    }
  }

  public int sizedMade = 0;

  /**
    * Returns a copy of the input task that is identical in every way,
    * but whose quantity has been set to size.
    * This task will <em>not</em> be published yet.
    */
  private Task sizedTask (Task t, double size) {
    RootFactory factory = _gp.getGPFactory();
    NewTask nt = factory.newTask();

    sizedMade++;

    nt.setVerb(t.getVerb());
    nt.setParentTask(t);
    nt.setDirectObject(factory.createInstance(t.getDirectObject()));

    // copy the PPs
    ArrayList preps = new ArrayList();
    Enumeration e = t.getPrepositionalPhrases();
    NewPrepositionalPhrase npp;
    while ( e.hasMoreElements() ) {
      npp = factory.newPrepositionalPhrase();
      PrepositionalPhrase pp = (PrepositionalPhrase)e.nextElement();
      npp.setPreposition(pp.getPreposition());
      npp.setIndirectObject(pp.getIndirectObject());
      preps.add(npp);
    }

    npp = factory.newPrepositionalPhrase();
    // Mark as INTERNAL so we recognize it as an expansion task.
    npp.setPreposition(GenericPlugin.INTERNAL);
    npp.setIndirectObject(TRUE);
    preps.add(npp);

    //BOZO
    nt.setPrepositionalPhrases(new Vector(preps).elements());
    
    // now copy the preferences, with the exception of quantity...
    ArrayList prefs = new ArrayList();
    e = t.getPreferences();
    while (e.hasMoreElements()) {
      Preference p = (Preference)e.nextElement();
      if ( p.getAspectType() != AspectType.QUANTITY ) {
        Preference np =
          factory.newPreference(p.getAspectType(),
                                p.getScoringFunction(),
                                p.getWeight());
        prefs.add(np);
      }
    }
      
    AspectValue av = new AspectValue( AspectType.QUANTITY, size );
    ScoringFunction sf = ScoringFunction.createNearOrBelow(av, 0.1);
    Preference pref = factory.newPreference( AspectType.QUANTITY, sf );
    prefs.add(0, pref);

    //BOZO
    nt.setPreferences(new Vector(prefs).elements());

    return nt;
  }

  /**
    * Make an expansion by putting all the tasks in subtasks into a new
    * workflow and making them the workflow of expandme.  This can probably
    * be done using a method on the GenericPlugin.
    * @see GenericPlugin#createExpansion
    */
  private void makeExpansion(Task expandme, ArrayList subtasks) {

    _gp.createExpansion(subtasks.iterator(), expandme);
  }

  public static Mass getTaskMass(Task task) {
    GLMAsset assetToBePacked = (GLMAsset) task.getDirectObject();
    if (assetToBePacked.hasPhysicalPG()) {
      Mass massPerEach = assetToBePacked.getPhysicalPG().getMass();
      double taskWeight = task.getPreferredValue(AspectType.QUANTITY) *
	massPerEach.getShortTons();
      if (Logging.defaultLogger().debugEnabled()) {
	Logging.defaultLogger().debug("Sizer.getTaskMass: Quantity: " + 
				      task.getPreferredValue(AspectType.QUANTITY) + 
				      " * massPerEach: " + massPerEach.getShortTons() +
				      " = " + taskWeight + " short tons");
	return new Mass(taskWeight, Mass.SHORT_TONS);
      }
    } else {
      Logging.defaultLogger().warn("Sizer.getTaskMass: asset - " + 
				   assetToBePacked + 
				   " - does not have a PhysicalPG. " +
				   "Assuming QUANTITY preference is in stort tons.");
      return new Mass(task.getPreferredValue(AspectType.QUANTITY), 
		      Mass.SHORT_TONS);
    }
  }
}
