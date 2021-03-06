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

package org.cougaar.mlm.construction;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.NewWorkflow;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.planning.plugin.util.ExpanderHelper;
import org.cougaar.planning.plugin.util.PluginHelper;
import org.cougaar.util.UnaryPredicate;


/** ExpanderPlugin that takes Determine Requirements of Type construction tasks and expands them into predefined construction tasks. 
  *
  **/


public class DTConstructionExpanderPlugin extends SimplePlugin {

    private IncrementalSubscription interestingTasks;
    private IncrementalSubscription myExpansions;
    private IncrementalSubscription myOrgActivities;

    protected Date cday = null;
    
    public void setupSubscriptions() {
      //System.out.println("In DTConstructionExpanderPlugin.setupSubscriptions");
      myOrgActivities = (IncrementalSubscription)subscribe(orgActsPred());
      
      if (didRehydrate()) {
        // Only need one
        Object first =  myOrgActivities.first();
        if (first != null) {
          getCDay((OrgActivity) first);
        }
      }
    }

  // only setup the task subscriptions after we have a valid cday from the
  // orgactivity.
  private void setupTaskSubscriptions() {
    myExpansions = (IncrementalSubscription)subscribe(expansionsPredicate());
    interestingTasks = (IncrementalSubscription)subscribe(tasksPredicate());
  }
  
    
   
    public void execute() 
    {
      	//System.out.println("DTConstructionExpanderPlugin.execute");
      
        if ( cday == null && myOrgActivities.hasChanged()) {
          // Only need one
          Object first =  myOrgActivities.first();
          if (first != null) {
            getCDay((OrgActivity) first);
          }
        }
        // We may still be waiting for the orgactivity
        if (interestingTasks == null) {
          return;
        }
            
   	for(Enumeration e = interestingTasks.getAddedList();e.hasMoreElements();) 
	    {
		Task task = (Task)e.nextElement();
		Collection subtasks = createSubTasks(task);
                NewWorkflow wf = theLDMF.newWorkflow();
		wf.setParentTask(task);
		Iterator stiter = subtasks.iterator();
		while (stiter.hasNext()) {
		  NewTask subtask = (NewTask) stiter.next();
		  subtask.setWorkflow(wf);
		  wf.addTask(subtask);
		  publishAdd(subtask);
		}
		// create the Expansion ...??? Do we want to provide an EstimatedAllocationResult ???
		Expansion newexpansion = theLDMF.createExpansion(theLDMF.getRealityPlan(), task, wf, null);
		publishAdd(newexpansion);

	    }

        for(Enumeration exp = myExpansions.getChangedList();exp.hasMoreElements();) {
           Expansion myexp = (Expansion)exp.nextElement();
           if (PluginHelper.updatePlanElement(myexp)) {
		publishChange(myexp);
           }
        }
    }

  

    private Collection createSubTasks(Task task)  {
     
      Vector subtasks = new Vector();
      Task apronsubtask = createSubTask(task, ConstructionConstants.Verb.EXPANDRUNWAYAPRON, 10, 60);
      Task shortrunway = createSubTask(task, ConstructionConstants.Verb.BUILDSHORTRUNWAY, 20, 50);
      Task tentcity = createSubTask(task, ConstructionConstants.Verb.BUILDSMALLTENTCITY, 10, 60);
      subtasks.add(apronsubtask);
      subtasks.add(shortrunway);
      subtasks.add(tentcity);

      //System.out.println("\n About to return: " + subtasks.size() + "Subtasks for Workflow");
      return subtasks;
  }


  /** Create a simple subtask
    * @param parent - parent task
    * @param st_name - new subtask's verb 
    * @param offset new subtask's offset time from C0 start time
    * @param duration new subtask's duration
    * @return Task - The new subtask
    **/
  private Task createSubTask(Task parent, String st_verb, int offset, int duration) {
    NewTask subtask = theLDMF.newTask();
    subtask.setParentTask(parent);
    subtask.setVerb(Verb.get(st_verb));
    subtask.setPlan(parent.getPlan());
    // use parent's prep phrases for now.
    subtask.setPrepositionalPhrases(parent.getPrepositionalPhrases());
    //set up preferences - first find the start time of the parent task
    Date childST = null;
    Date childET = null;
    Enumeration parentprefs = parent.getPreferences();
    while (parentprefs.hasMoreElements()) {
      Preference apref = (Preference)parentprefs.nextElement();
      if (apref.getAspectType() == AspectType.START_TIME) {
	Calendar cal = Calendar.getInstance();
	cal.setTime(cday);
	cal.add(Calendar.DATE, offset);
        childST = cal.getTime();
        cal.add(Calendar.DATE, duration);
        childET = cal.getTime();
      }
      break;
    }
	
    Vector prefs = new Vector();
    AspectValue startAV = AspectValue.newAspectValue(AspectType.START_TIME, childST.getTime());
    ScoringFunction startSF = ScoringFunction.createPreferredAtValue(startAV, 2);
    Preference startPref = theLDMF.newPreference(AspectType.START_TIME, startSF);
    prefs.addElement(startPref); 
    AspectValue endAV = AspectValue.newAspectValue(AspectType.END_TIME, childET.getTime());
    ScoringFunction endSF = ScoringFunction.createPreferredAtValue(endAV, 2);
    Preference endPref = theLDMF.newPreference(AspectType.END_TIME, endSF);
    prefs.addElement(endPref);

    subtask.setPreferences(prefs.elements());

    return subtask;
  }

  private void getCDay(OrgActivity orgact) {
    Collection oplanCol = query(new OplanByUIDPred(orgact.getOplanUID()));
    // Should only be one
    Oplan oplan = (Oplan) oplanCol.iterator().next();
    this.cday = new Date(oplan.getCday().getTime());
    if (interestingTasks == null) {
      setupTaskSubscriptions();
    }
  }



  // predicates
  protected static UnaryPredicate expansionsPredicate() {
     return new UnaryPredicate() {
	public boolean execute(Object o) {
          if (o instanceof Expansion) {
            Task task = ((Expansion)o).getTask();
            if (Constants.Verb.DetermineRequirements.equals(task.getVerb())) {
                return true;
	     }
          }
          return false;
        }
     };
    }

     /**
   * Predicate to find a specific Oplan by UID
   **/
  protected static class OplanByUIDPred implements UnaryPredicate {
    UID oplanUID;

    OplanByUIDPred (UID uid) {
      oplanUID = uid;
    }
    public boolean execute(Object o) {
      if (o instanceof Oplan) {
	if (oplanUID.equals(((Oplan)o).getUID())) {
	  return true;
	}
      }
      return false;
    }
  }

    protected static UnaryPredicate tasksPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Task) {
          Task t = (Task) o;
          if (Constants.Verb.DetermineRequirements.equals(t.getVerb())) {
            return (ExpanderHelper.isOfType(t, Constants.Preposition.OFTYPE,
                     "Construction"));
          }
        }
        return false;
      }
    };
  }

  protected static UnaryPredicate orgActsPred() {
     return new UnaryPredicate() {
       public boolean execute(Object o) {
         return (o instanceof OrgActivity);
       }
     };
   }

 
 
}
