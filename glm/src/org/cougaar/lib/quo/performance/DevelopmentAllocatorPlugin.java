/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
//package org.cougaar.lib.quo.performance;
package org.cougaar.lib.quo.performance;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.util.UnaryPredicate;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import org.cougaar.lib.quo.performance.assets.*;
import org.cougaar.core.blackboard.ChangeReport;
import org.cougaar.core.plugin.Annotation;
import java.io.*;

/**
 * This COUGAAR Plugin subscribes to tasks in a workflow and allocates
 * the workflow sub-tasks to programmer assets.
 * @author ALPINE (alpine-software@bbn.com)
 * @version $Id: DevelopmentAllocatorPlugin.java,v 1.1 2002-02-12 17:48:10 jwinston Exp $
 **/
public class DevelopmentAllocatorPlugin extends CommonUtilPlugin
{
    private IncrementalSubscription allCodeTasks;   // Tasks that I'm interested in
    private IncrementalSubscription allProgrammers;  // Programmer assets that I allocate to

    protected int CPUCONSUME,  MESSAGESIZE, MAXCOUNT;
    protected int THINK_TIME,OUTSTANDING_MESSAGES ;
    protected String FILENAME, VERB;
    protected Task task;
    protected boolean DEBUG = false, LOG=false;
    protected Date startTime;
    protected long minDelta=0;
    protected FileWriter fw;
    protected int wakeUpCount, taskAllocationCount, count = 1;
    protected AspectValue allocAspectVal;
    protected double allocNum = 0;
    protected int expectedSeqNum = 1;

    /**
     * parsing the plugIn arguments and setting the values
     */
    protected void parseParameter(){
	Vector p = getParameters();
	CPUCONSUME=getParameterIntValue(p, "CPUCONSUME");
	MESSAGESIZE=getParameterIntValue(p, "MESSAGESIZE");
	FILENAME=getParameterValue(p, "FILENAME");
	MAXCOUNT=getParameterIntValue(p, "MAXCOUNT");
	OUTSTANDING_MESSAGES =getParameterIntValue(p, "OUTSTANDING_MESSAGES");
	DEBUG=getParameterBooleanValue(p, "DEBUG");
	LOG=getParameterBooleanValue(p, "LOG");
	VERB=getParameterValue(p, "VERB");
	THINK_TIME=getParameterIntValue(p, "THINK_TIME");
    }

    static class MyChangeReport implements ChangeReport {
	private byte[] bytes;
	MyChangeReport(byte[] bytes){
	    this.bytes = bytes;
	}
    }

    /**
     * Predicate matching all ProgrammerAssets
     */
    private UnaryPredicate allProgrammersPredicate = new UnaryPredicate() {
	    public boolean execute(Object o) {
		return o instanceof ProgrammerAsset;
	    }
	};

    /**
     * Predicate that matches all Test tasks
     */
    private UnaryPredicate codeTaskPredicate = new UnaryPredicate() {
	    public boolean execute(Object o) {
		if (o instanceof Task)
		    {	
			Task task = (Task)o;
			return task.getVerb().equals(Verb.getVerb(VERB));
		    }
		return false;
	    }
	};

     
    /**
     * Establish subscription for tasks and assets
     **/
    public void setupSubscriptions() {
	parseParameter();
	allProgrammers = 
	    (IncrementalSubscription)subscribe(allProgrammersPredicate);
	allCodeTasks =  
	    (IncrementalSubscription)subscribe(codeTaskPredicate);
    }

    /**
     * Top level plugin execute loop.  Handle changes to my subscriptions.
     **/
    public void execute() {
	wakeUpCount++;
	debug(DEBUG, "" +System.currentTimeMillis() 
	      + " wakeUpcount " + wakeUpCount+"------------------") ;
	allocateTasks(allCodeTasks.getAddedList());
	allocateTasks(allCodeTasks.getChangedList());
    }
  
    private void allocateTasks(Enumeration task_enum) {
	while (task_enum.hasMoreElements()) {
	    taskAllocationCount++;
	    task = (Task)task_enum.nextElement();
	    startTime = new Date();
	    waitFor(THINK_TIME);
	    allocateTask(task, startMonth(task));
	}
    }
   
    /**
     * Extract the start month from a task
     */
    private int startMonth(Task t) {
	return 0;
    }

    /**
     * Find an available ProgrammerAsset for this task.  Task must be scheduled
     * after the month "after"
     */
    private int allocateTask(Task task, int after) {
	if(CPUCONSUME != -1)  //i.e. cpuconsume passed to plugin as a arg
	    consumeCPU(CPUCONSUME);
    
	int end = after;    int duration = 3;  int earliest = 0;
	end = earliest + duration;  int desired_delivery =10; //bogus
	boolean onTime = (end <= desired_delivery);
        
	// select an available programmer at random
	Vector programmers = new Vector(allProgrammers.getCollection());
	boolean allocated = false;
    	
	while ((!allocated) && (programmers.size() > 0)) {
	    int stuckee = (int)Math.floor(Math.random() * programmers.size());
	    ProgrammerAsset asset = 
		(ProgrammerAsset)programmers.elementAt(stuckee);
	    allocNum =  task.getPreferredValue(AspectType._ASPECT_COUNT );
	    String msg = "expectedSeqNum::receivedSeNum::" +
		      expectedSeqNum +":"+allocNum;
	    if (expectedSeqNum > allocNum){
		debug(DEBUG, "Warning out of sequence task: " + msg); 
	    }
	    else {
		if (expectedSeqNum < allocNum){
		    debug(DEBUG,"Skipped a  task: " + msg); 
		}
		if (expectedSeqNum== allocNum){
		    debug( DEBUG,"expectedSeqNum == receivedSeNum::" + msg);
		}
		int []aspect_types = {AspectType._ASPECT_COUNT};
		double []results = {allocNum};
		AllocationResult estAR =  theLDMF.newAllocationResult
		    (1.0, onTime,aspect_types, results  );

		ChangeReport cr = null;
		if (MESSAGESIZE != -1)
		    cr = new MyChangeReport(alterMessageSize(MESSAGESIZE));
		else 
		    cr = new MyChangeReport(alterMessageSize(0));

		//Allocation  planElement
		PlanElement pe = task.getPlanElement(); 
		if (pe == null) {
		    pe = theLDMF.createAllocation(task.getPlan(), task,
						  asset, estAR, Role.ASSIGNED);
		    publishAdd(pe);
		} else {
		    pe.setEstimatedResult(estAR);
		    publishChange(pe, Collections.singleton(cr));
		}
		publishRemove(task);
		printTheChange(task);
		expectedSeqNum=(int)allocNum+1; //updating seq num
		breakFromLoop(count, MAXCOUNT);
	    }
	    allocated = true;
	}
       
	return end;
    }

    protected void printTheChange(Task task){
	Date endTime = new Date();
	long delta = endTime.getTime() - startTime.getTime();
	if (count == 1)
	    minDelta = delta;
	else
	    minDelta = Math.min(minDelta, delta);
	int taskCount = (int)task.getPreferredValue(AspectType._ASPECT_COUNT);
	String msg=task.getVerb() + "=>" +taskCount+","+delta+","+ minDelta;
	debug(DEBUG, " TaskAllocationCount:" + taskAllocationCount);
	log(LOG, FILENAME, fw, msg);
	count++;
    }
}





















