/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.mlm.plugin.organization;

import org.cougaar.planning.ldm.PlanningFactory;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AbstractAsset;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.Subscription;

import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectScorePoint;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.HasRelationships;
import org.cougaar.planning.ldm.plan.NewPlanElement;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewScheduleElement;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.NewWorkflow;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.plan.Workflow;


import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.planning.plugin.util.PluginHelper;

import org.cougaar.util.Enumerator;
import org.cougaar.util.Filters;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.plan.Capability;

/**
 * Version of GLSAllocator without SD support, for use with minitestconfig, eg.
 * Handles GLS for subordinates tasks. We expand such tasks into
 * subtasks, one per subordinate. As our subordinates changes we
 * revise the expansion to follow.
 **/
public class GLSMiniAllocatorPlugin extends SimplePlugin {
	
  private IncrementalSubscription allocatableGLSTask = null;
  private IncrementalSubscription orgAssets;
  private IncrementalSubscription myAllocations;
  private IncrementalSubscription myExpansions;
  private String me = null;
  private HashMap mySubs = new HashMap();
  
    //Override the setupSubscriptions() in the SimplePlugin.
  protected void setupSubscriptions() {
    me = getCluster().getMessageAddress().getAddress();

    // subscribe for GLS tasks for subordinates
    allocatableGLSTask = (IncrementalSubscription)subscribe(allocGLSPred);

    //subscribe for assets to allocate against
    orgAssets = (IncrementalSubscription)subscribe(orgPred);

    // subscribe to my allocations and expansions in order to catch changes in the 
    // allocationresults from the notification process
    myAllocations = (IncrementalSubscription)subscribe(myAllocsPred);
    myExpansions = (IncrementalSubscription)subscribe(myExpsPred);

    if (didRehydrate()) {
      Collection subs = retrieveSubOrgs(orgAssets.getCollection());
      for (Iterator iterator = subs.iterator();iterator.hasNext();) {
        Organization org = (Organization)iterator.next();
        mySubs.put(org.getUID(), org);
      }
    }
  }
    
  /**
   * Process new tasks and subordinates as follows: New tasks are
   * expanded to subtasks for each known subordinate.  New
   * subordinates have tasks appended to known expansions.
   * Duplication is avoided when new tasks and new subordinates arrive
   * together because the expansions for the new tasks don't yet
   * exist.
   **/
  public synchronized void execute() {
    if (allocatableGLSTask.hasChanged() ) {
      Enumeration tasks = allocatableGLSTask.getAddedList();
      
      Collection subs = retrieveSubOrgs(orgAssets.getCollection());
      while (tasks.hasMoreElements()) {
        Task task = (Task) tasks.nextElement();
	expandGLS(task, new Enumerator(subs));
      }
    }

    // check the asset container for new Subordinates
    if (orgAssets.hasChanged()) {
      /**
       * When a new subordinate appears we must modify the existing
       * expansions to include the new subordinate.
       **/
      Collection newSubs = retrieveSubOrgs(orgAssets.getCollection());

      for (Iterator iterator = newSubs.iterator(); iterator.hasNext();) {
        Organization org = (Organization)iterator.next();

        // Remove from set if we already know about it
        if (mySubs.get(org.getUID()) != null) {
          iterator.remove();
        } else {
          mySubs.put(org.getUID(), org);
        }
      }

      for (Enumeration expansions = myExpansions.elements(); expansions.hasMoreElements(); ) {
        Expansion exp = (Expansion) expansions.nextElement();

        augmentExpansion(exp, new Enumerator(newSubs));
      }
      /** Should handle removed assets, too **/
    }
		
    if (myAllocations.hasChanged()) {
      PluginHelper.updateAllocationResult(myAllocations);
    }
    
    if (myExpansions.hasChanged()) {
      PluginHelper.updateAllocationResult(myExpansions);
    }
  } // end of execute

  /**
   * Allocate the unallocated subtasks of a workflow
   **/
  private void allocateGLS(Workflow wf) {
    Enumeration tasks = wf.getTasks();
    while (tasks.hasMoreElements()) {
      Task t = (Task) tasks.nextElement();
      if (t.getPlanElement() != null) continue; // Already allocated
      Enumeration pp = t.getPrepositionalPhrases();
      while (pp.hasMoreElements()) {
	PrepositionalPhrase p = (PrepositionalPhrase) pp.nextElement();
	if (p.getPreposition().equals(Constants.Preposition.FOR)) {
	  if (p.getIndirectObject() instanceof Organization) {
	    Asset a = (Asset) p.getIndirectObject();
            AllocationResult estimatedresult =
              PluginHelper.createEstimatedAllocationResult(t, theLDMF, 0.0, true);
            Allocation alloc = theLDMF.createAllocation(theLDMF.getRealityPlan(),
                                                        t, a, estimatedresult, Role.BOGUS);
            publishAdd(alloc);
            break;              // Terminate processing of prep phrases
	  }
	}
      }
    }
  }

  /**
   * Expand a new GLS task to include the given subordinates.
   **/
  private void expandGLS(Task t, Enumeration esubs) {
    NewWorkflow wf = theLDMF.newWorkflow();
    //wf.setIsPropagatingToSubtasks();
    wf.setParentTask(t);
    
    // Add tasks to workflow but do not publish them. Done later in the call
    // to PluginHelper.publishAddExpansion();
    augmentWorkflow(wf, esubs, false);
    // package up the workflow in an expansion
    AllocationResult estAR = null;
    if (!esubs.hasMoreElements()) {
      estAR = PluginHelper.createEstimatedAllocationResult(t, theLDMF, 1.0, true);
    }
    Expansion exp = theLDMF.createExpansion(theLDMF.getRealityPlan(), t, wf, estAR);
    //publish the Expansion and the workflow tasks all in one
    PluginHelper.publishAddExpansion(getSubscriber(), exp);
    //now allocate the tasks in the new workflow
    allocateGLS(wf);
  }

  private void augmentWorkflow(NewWorkflow wf, Enumeration esubs, 
                               boolean publishInPlace) {
    while (esubs.hasMoreElements()) {
      Asset orga = (Asset) esubs.nextElement();
      NewTask newTask = createSubTask(wf.getParentTask(), orga);
      newTask.setWorkflow(wf);
      wf.addTask(newTask);
      if (publishInPlace) {
        publishAdd(newTask);
      }
    }
  }

  static final Role cinc = Role.getRole("CINC");

  private synchronized void augmentExpansion(Expansion exp, Enumeration esubs) {
    if (esubs.hasMoreElements()) {
      NewWorkflow wf = (NewWorkflow) exp.getWorkflow();

      // Add tasks to workflow and publish new tasks
      augmentWorkflow(wf, esubs, true);

      publishChange(wf);
      allocateGLS(wf);
    }
  }

  /**
   * Create a subtask that matches the given Task plus a FOR
   * Organization phrase.
   **/
  private NewTask createSubTask(Task t, Asset subasset) {
    Vector prepphrases = new Vector();
    
    NewTask subtask = theLDMF.newTask();
    
    // Create copy of parent Task
    subtask.setParentTask( t );
    if (t.getDirectObject() != null) {
      subtask.setDirectObject(theLDMF.cloneInstance(t.getDirectObject()));
    } else {
      subtask.setDirectObject(null);
    }

    // Code removed because oplan is in context
     // pull out the "with OPlan" prep phrase and store for later
     Enumeration origpp = t.getPrepositionalPhrases();
     while (origpp.hasMoreElements()) {
       PrepositionalPhrase theorigpp = (PrepositionalPhrase) origpp.nextElement();
       if ( theorigpp.getPreposition().equals("WithC0") ) {
 	prepphrases.addElement(theorigpp);
       }
     }
	  
    NewPrepositionalPhrase newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(Constants.Preposition.FOR);
    newpp.setIndirectObject(theLDMF.cloneInstance(subasset));
    prepphrases.addElement(newpp);
    subtask.setPrepositionalPhrases(prepphrases.elements());
    
    subtask.setVerb( t.getVerb() );
    subtask.setPlan( t.getPlan() );
    // for now set the preferences the same as the parent task's
    // in a real expander you would want to distribute the parents preferences
    // across the subtasks.
    subtask.setPreferences( t.getPreferences() );
    subtask.setSource(this.getCluster().getMessageAddress());
    
    return subtask;

  }
      
  /**
   * Select Tasks that are GETLOGSUPPORT FOR Asset where the Asset is
   * identified as "Subordinates"
   **/
  public static UnaryPredicate allocGLSPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Task) {
        Task t = (Task) o;
        if (t.getVerb().equals(Constants.Verb.GetLogSupport)) {
          PrepositionalPhrase app = t.getPrepositionalPhrase(Constants.Preposition.FOR);
          if (app != null) {
            Object indObject = app.getIndirectObject();
            if (indObject instanceof Asset) {
              Asset asset = (Asset) indObject;
              try {
                return asset.getTypeIdentificationPG().getTypeIdentification().equals("Subordinates");
              }
              catch (Exception e) {
                System.out.println("GLSMiniAlloc error while trying to get the TypeIdentification of an asset");
                e.printStackTrace();
              }
            }
          } 
        }
      }
      return false;
    }
  };

  /**
   * Select all Organization assets
   **/
  public static UnaryPredicate orgPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Organization) {
        return true;
      } else {
        return false;
      }
    }
  };

  public static Collection retrieveSubOrgs(Collection orgs) {
    HashSet subs = new HashSet(3);

    Iterator iterator = orgs.iterator();
    while (iterator.hasNext()) {
      Organization org = (Organization)iterator.next();

      if (org.isSelf()) {
        RelationshipSchedule schedule = org.getRelationshipSchedule();
        Collection orgCollection = org.getSubordinates(TimeSpan.MIN_VALUE,
                                                       TimeSpan.MAX_VALUE);

        if (orgCollection.size() > 0) {
          for (Iterator relIterator = orgCollection.iterator();
               relIterator.hasNext();) {
            Relationship relationship = (Relationship) relIterator.next();
            HasRelationships sub = schedule.getOther(relationship);
            
            if (!subs.contains(sub)) {
              subs.add(sub);
            }
          }
        }
      
        /** BOZO - how would this relationship ever get created? 
         **/
        orgCollection = 
          schedule.getMatchingRelationships(cinc.getConverse(),
                                            TimeSpan.MIN_VALUE,
                                            TimeSpan.MAX_VALUE);
        if (orgCollection.size() > 0) {
          for (Iterator relIterator = orgCollection.iterator();
               relIterator.hasNext();) {
            Relationship relationship = (Relationship) relIterator.next();
            HasRelationships sub = schedule.getOther(relationship);
            
            if (!subs.contains(sub)) {
              subs.add(sub);
            }
          }
        }
      } 
    }
    return subs;
  }
    

  /**
   * Select all the allocations we create. These are allocations of
   * GETLOGSUPPORT tasks whose parent task is selected by the
   * allocGLSPred
   **/
  public static UnaryPredicate myAllocsPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Allocation) {
        Task t = ((Allocation)o).getTask();
        if (t.getVerb().equals(Constants.Verb.GETLOGSUPPORT)) {
//            return allocGLSPred.execute(t.getParentTask());
          return true;
        }
      }
      return false;
    }
  };

  /**
   * Select the expansions we create. These are expansions of the
   * tasks selected by allocGLSPred.
   **/
  public static UnaryPredicate myExpsPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Expansion) {
        return allocGLSPred.execute(((Expansion)o).getTask());
      }
      return false;
    }
  };
}