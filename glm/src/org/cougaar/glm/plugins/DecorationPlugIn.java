/*--------------------------------------------------------------------------
 *                         RESTRICTED RIGHTS LEGEND
 *
 *   Use, duplication, or disclosure by the Government is subject to
 *   restrictions as set forth in the Rights in Technical Data and Computer
 *   Software Clause at DFARS 52.227-7013.
 *
 *                             BBN Technologies,
 *                               A Division of
 *                              BBN Corporation
 *                             10 Moulton Street
 *                            Cambridge, MA 02138
 *                              (617) 873-3000
 *
 *   Copyright 1999 by
 *             BBN Technologies, A Division of
 *             BBN Corporation, all rights reserved.
 *
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.plugins;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.blackboard.IncrementalSubscription;

import org.cougaar.core.domain.RootFactory;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.glm.ldm.Constants.Role;
import org.cougaar.core.plugin.PlugInDelegate;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.glm.debug.GLMDebug;

import org.cougaar.glm.ldm.asset.Organization;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.Collection;
import java.util.Iterator;

/**
 * Defines common functions described in SimplePlugIn.
 * The plugin is decorated with the proper BasicProcessor at run time
 * by the PlugInDecorator.
 * @see PlugInDecorator
 * @see BasicProcessor
 */
public abstract class DecorationPlugIn extends SimplePlugIn {

    /** a vector of BasicProcessors for this plugin */
    protected Vector                       taskProcessors_;
    /** this plugin's Organization cluster identifier */
    protected ClusterIdentifier            clusterId_;
    protected boolean                      configured_ = false;
    protected Organization                 myOrganization_ = null;
    private IncrementalSubscription        selfOrganizations_;
    protected String                       className_;
    protected Hashtable                    myParams_ = new Hashtable();
    public int transCount_=0;
    protected Vector                       plugInSubscriptions_ = new Vector();
    protected Hashtable                    processorSubscriptions_ = new Hashtable();
    protected boolean                      invoke_;
    public final String                    SUPPLYTYPES = "SUPPLYTYPES";

    private HashSet consumers_ = null;
    public org.cougaar.planning.ldm.plan.Role consumerRole_ = null;
    private Set seenConsumers_ = new HashSet();
    private Alarm customerAlarm_ = null;
    /**
     * Set this for additional delay after all customers have been
     * seen
     **/
    protected long extraCustomerDelay_ = 0L;
    protected String myOrgName_;
    private boolean allConsumersSeen_ = false;
    private int selfConsumer = 0;
    public static final String NEW_CUSTOMER_WAIT_PROP = "org.cougaar.glm.plugins.inventory.newCustomerWait";
    public static final long NEW_CUSTOMER_WAIT_DEFAULT = 120000L;
    public static long NEW_CUSTOMER_WAIT =
        Long.getLong(NEW_CUSTOMER_WAIT_PROP, NEW_CUSTOMER_WAIT_DEFAULT).longValue();


    public DecorationPlugIn()
    {
	super();
	className_ = this.getClass().getName();
	int indx = className_.lastIndexOf(".");
	if (indx > -1) {
	    className_ = className_.substring(indx+1);
	}
	taskProcessors_ = new Vector();
    }

    private static UnaryPredicate orgsPredicate_= new UnaryPredicate() {
	public boolean execute(Object o) {
	    if (o instanceof Organization) {
              return ((Organization)o).isSelf();
	    }
	    return false;
	}
    };  	

    /** Called by the PlugInDecorator to set the task processor. */
    void addTaskProcessor(BasicProcessor task_processor)
    {
	GLMDebug.LOG(className_,clusterId_,"Adding task processor "+task_processor.getClass().toString());
	taskProcessors_.addElement(task_processor);
    }

    /** Must be called after the plugin has been initialized and loaded */
    private void configure()
    {
	if (configured_) {
	    if (selfOrganizations_.getAddedList().hasMoreElements()) {
		// We should handle changes in our org role and update processors accordingly.
		GLMDebug.ERROR("DecorationPlugIn",clusterId_, "orgs changed after decoration");
	    }
	    return;
	}

	// look for this organization
	if (myOrganization_ == null) {
	    Enumeration new_orgs = selfOrganizations_.elements();
	    if (new_orgs.hasMoreElements()) {
		myOrganization_ = (Organization) new_orgs.nextElement();
		String clusterId= myOrganization_.getClusterPG().getClusterIdentifier().toString();
                myOrgName_ = myOrganization_.getItemIdentificationPG().getItemIdentification();
	    }
	}

	if (myOrganization_ == null) return;
	// found the organization
	// Grab plugin parameters for the decorator
	readParameters();
	decoratePlugIn();
	configured_ = true;
    }


    private void reconfigure() {
	configured_ = false;
	configure();
    }

    private int getConsumerCount() {
        if (consumers_ == null) {
            if (consumerRole_ == null) {
                System.out.println(myOrgName_ + " null consumerRole_");
                return selfConsumer;
            }
            consumers_ = new HashSet();
            RelationshipSchedule rs = myOrganization_.getRelationshipSchedule();
            Collection consumerRelations_ = rs.getMatchingRelationships(consumerRole_);
            Iterator consumerRelationIterator= consumerRelations_.iterator();
            /* Count consumers using a set in case of time-phased relationships */
            System.out.println(myOrgName_ + " adding consumers of " + consumerRole_);
            while (consumerRelationIterator.hasNext()) {
                String customer = ((Organization)((Relationship)consumerRelationIterator.next()).getA()).
                    getClusterPG().getClusterIdentifier().toString();
                System.out.println(myOrgName_ + " adding " + customer);
                consumers_.add(customer);
            }
        }
        return consumers_.size() + selfConsumer;
    }

    public void recordCustomer(String consumer) {
        if (allConsumersSeen_) return;
	if (seenConsumers_.add(consumer)) {
            if (consumer.equals(myOrgName_)) selfConsumer = 1;
	    System.out.println("####### Seeing new consumer: "+consumer+" at supplier: "+
			       myOrganization_.getClusterPG().getClusterIdentifier().toString());
            if (customerAlarm_ != null) {
                /* Cancel current alarm */
                customerAlarm_.cancel();
                customerAlarm_ = null;
            }
            int consumerCount = getConsumerCount();
            if (seenConsumers_.size() >= consumerCount){
                if (extraCustomerDelay_ > 0L) {
                    customerAlarm_ = wakeAfterRealTime(extraCustomerDelay_);
                }
                allConsumersSeen_ = true;
                System.out.println("####### All " + seenConsumers_.size()
                                   + " of " + consumerCount
                                   + " consumers seen at supplier: "
                                   + myOrganization_.getClusterPG().getClusterIdentifier().toString());
            } else {
                System.out.println("####### Only " + seenConsumers_.size()
                                   + " of " + consumerCount
                                   + " consumers seen at supplier: "
                                   + myOrganization_.getClusterPG().getClusterIdentifier().toString());
                customerAlarm_ = wakeAfterRealTime(NEW_CUSTOMER_WAIT);
            }
	} else {
//  	    System.out.println("####### Seeing old consumer: "+consumer+" at supplier: "+
//  			       myOrganization_.getClusterPG().getClusterIdentifier().toString());
	}
    }

    /* Called when MaintainInventory tasks are rescinded. */
    public void clearRecordedCustomers() {
        allConsumersSeen_ = false;
        selfConsumer = 0;
        seenConsumers_.clear();
        System.out.println("####### Removing seen consumers: at supplier: "+
                           myOrganization_.getClusterPG().getClusterIdentifier().toString());
        if (customerAlarm_ != null) {
            /* Cancel current alarm */
            customerAlarm_.cancel();
            customerAlarm_ = null;
        }
    }

    public boolean hasSeenAllConsumers() {
        if (customerAlarm_ != null) {
            if (customerAlarm_.hasExpired()) {
                customerAlarm_ = null;
                if (!allConsumersSeen_) {
                    allConsumersSeen_ = true;
                    System.out.println("####### Tired of waiting at supplier: "+
                                       myOrgName_);
                } else {
                    System.out.println("####### Finished waiting at supplier: "+
                                       myOrgName_);
                }
            } else {
                return false;   // Still waiting
            }
        }
        return allConsumersSeen_;
    }


    protected abstract void decoratePlugIn();

    protected void setupSubscriptions() {
 	clusterId_ = getClusterIdentifier();
	selfOrganizations_ = (IncrementalSubscription)subscribe( orgsPredicate_);
	monitorPlugInSubscription(selfOrganizations_);	

	if (didRehydrate()) {
	    reconfigure();	    
	}

    }
   
    public void monitorSubscription(BasicProcessor bp, IncrementalSubscription subscript){
	Vector subscriptions = (Vector)processorSubscriptions_.get(bp);
	if (subscriptions == null) {
	    subscriptions = new Vector();
	    processorSubscriptions_.put(bp,subscriptions);
	}
	subscriptions.add(subscript);
    }

    public void monitorPlugInSubscription(IncrementalSubscription subscript){
	plugInSubscriptions_.add(subscript);
    }

    public boolean isSubscriptionChanged(IncrementalSubscription sub) {
	return (sub.getChangedList().hasMoreElements()
		|| sub.getAddedList().hasMoreElements() 
		|| sub.getRemovedList().hasMoreElements() );
    }


    public Object subscriptionChangedObject(IncrementalSubscription sub) {
	if (sub.getChangedList().hasMoreElements()){
	    return sub.getChangedList().nextElement();
	} else if(sub.getAddedList().hasMoreElements()) {
	    return sub.getAddedList().nextElement();
	} else if(sub.getRemovedList().hasMoreElements()){
	    return sub.getRemovedList().nextElement();
	} else {
	    return null;
	}
    }


    public boolean areSubscriptionsChanged() {
	if (areSubscriptionsChanged(plugInSubscriptions_)) {
	    return true;
	}
	Enumeration enum = processorSubscriptions_.elements();
	Vector subscriptions;
	while (enum.hasMoreElements()) {
	    subscriptions = (Vector) enum.nextElement();
	    if (areSubscriptionsChanged(subscriptions)) {
		return true;
	    }
	}
	return false;
    }

    public boolean areSubscriptionsChanged(Vector subscriptions) {
	Enumeration enum = subscriptions.elements();
	while (enum.hasMoreElements()) {
	    if (isSubscriptionChanged((IncrementalSubscription)enum.nextElement())) {
		return true;
	    }
	}
	return false;
    }


    /** Invokes all of the processors used to decorate this plugin.
     *  The first time execute() is invoked, it configures the plugin by
     *  setting the task processor, and unsubscribing to 'self'
     *  (subscribed in setSubscriptions()).
     *  subclasses need to call runProcessors()!
     */
    public void execute()
    {
	if (!areSubscriptionsChanged()&&
	    !wasAwakened()) {
   	    GLMDebug.DEBUG(this.getClass().getName(), clusterId_, " execute without timer or subscription change !!!!");
	    invoke_ = false;
	    return;
	}
	invoke_ = true;
//  	GLMDebug.DEBUG(this.getClass().getName(), clusterId_, "__________________________ PLUGIN started   Transaction: "+transCount_);
	transCount_=transCount_+1;

	// Configures the task processor list when my organization is added
	// to the list.
	configure();
    }

    protected void runProcessors() {
	// Invoke each of the task processors.
	BasicProcessor tp;
	Vector subscriptions;
	Enumeration e = taskProcessors_.elements();
	boolean runProcessor = false;
	if (areSubscriptionsChanged(plugInSubscriptions_) ||
	    wasAwakened()) {
	    // wake all processors
	    while (e.hasMoreElements()) {
		tp = (BasicProcessor)e.nextElement();
		if (areSubscriptionsChangedPrint(plugInSubscriptions_)) {
		    //System.out.println("#### Plugin subscriptions caused running of processor: "+tp+"####");
		    tp.update();
		    runProcessor = true;
		} else if(wasAwakened()){
		    //System.out.println("#### Alarm caused running of processor: "+tp+"####");
		    tp.update();

//  		    subscriptions = (Vector)processorSubscriptions_.get(tp);
//  		    if (subscriptions != null) {
//  			if (areSubscriptionsChangedPrint(subscriptions)) {
//  			    System.out.println("#### Running processor: "+tp+"####");
//  			    tp.update();
//  			    runProcessor = true;
//  			}
//		    }
		} else {
		    //System.out.println("#### Running processor: "+tp+" with no subscriptions ####");
		    tp.update();
		}
		    
	    }
	} else {
	    // wake processors iff their subscriptions changed
	    while (e.hasMoreElements()) {
		tp = (BasicProcessor)e.nextElement();
		subscriptions = (Vector)processorSubscriptions_.get(tp);
		if (subscriptions != null) {
		    if (areSubscriptionsChangedPrint(subscriptions)) {
			//System.out.println("#### Running processor: "+tp+"####");
			tp.update();
			runProcessor = true;
		    }
		}
	    }
	}
	if(false && !runProcessor){
	    System.out.println("NO PROCESSOR RUN!!!");
	}
    }

    public boolean areSubscriptionsChangedPrint(Vector subscriptions)  {
	Enumeration enum = subscriptions.elements();
	while (enum.hasMoreElements()) {
	    IncrementalSubscription is = (IncrementalSubscription)enum.nextElement();
	    if (isSubscriptionChanged(is)) {
		//System.out.println("####### Subscription changed: "+subscriptionChangedObject(is));
		return true;
	    }
	}
	return false;
    }


    public boolean publishAddObject(Object obj) {
	return this.publishAdd(obj);
    }


    public void publishRemoveFromExpansion(Task subtask) {
        NewWorkflow wf = (NewWorkflow) subtask.getWorkflow();
        publishRemove(subtask);
        if (wf == null) return; // Already removed
        wf.removeTask(subtask);
    }

    public void publishAddToExpansion(Task parent, Task subtask) {
	// Publish new task
	if (!publishAddObject(subtask)) {
	    GLMDebug.DEBUG(className_, "publishAddToExpansion fail to publish task "+TaskUtils.taskDesc(subtask));
	}
	PlanElement pe = parent.getPlanElement();
	Expansion expansion;
	NewWorkflow wf;
        ((NewTask) subtask).setParentTask(parent);
	// Task has not been expanded, create an expansion
	if (pe == null) {
	    RootFactory factory = getMyDelegate().getFactory();
	    // Create workflow
	    wf = (NewWorkflow)factory.newWorkflow();
	    wf.setParentTask(parent);
	    wf.setIsPropagatingToSubtasks(true);
	    wf.addTask(subtask);
            ((NewTask) subtask).setWorkflow(wf);
	    // Build Expansion
	    expansion = factory.createExpansion(parent.getPlan(), parent, wf, null);
	    // Publish Expansion
	    publishAdd(expansion);
	}
	// Task already has expansion, add task to the workflow and publish the change
	else if (pe instanceof Expansion) {
	    expansion =(Expansion)pe;
	    wf = (NewWorkflow)expansion.getWorkflow();
	    wf.addTask(subtask);
            ((NewTask) subtask).setWorkflow(wf);
	    publishChange(expansion);
	}
	else {
	    GLMDebug.DEBUG(className_, "publishAddToExpansion: problem pe not Expansion? "+pe);	    
	}
    }

    public Alarm wakeAfterDelay(int delay) {
	return wakeAfterRealTime(delay);
    }

    public PlugInDelegate getMyDelegate() {
	return this.getDelegate();
    }

    public boolean rehydrated() {
	return didRehydrate();
    }

    protected void readParameters() {
	Vector p = getParameters();
	Vector supply_types = new Vector();

	if (p.isEmpty()) {
	    GLMDebug.DEBUG(className_, getClusterIdentifier(),
			    "getParameters(), Using default decorator");
	    // no parameters to read so our work here is done
	    return;
	}
	for (Enumeration e = p.elements(); e.hasMoreElements();) {
	    String s = (String)e.nextElement();
	    if (s.charAt(0) == '+') {
		myParams_.put(new String(s.substring(1)), new Boolean(true));
		GLMDebug.DEBUG(className_, getClusterIdentifier(),
				"readParameters(), adding "+s.substring(1));
	    }
	    else if (s.charAt(0) == '-') {
		myParams_.put(new String(s.substring(1)), new Boolean(false));
		GLMDebug.DEBUG(className_, getClusterIdentifier(),
				"readParameters(), removing "+s.substring(1));
	    }
	    else if (s.indexOf('=') != -1) {
		int i = s.indexOf('=');
		String key = new String(s.substring(0, i));
		String value = new String(s.substring(i+1, s.length()));
		myParams_.put(key.trim(), value.trim());
	    }
	    else {
		Vector types = (Vector)myParams_.get(SUPPLYTYPES);		
		if (types == null) {
		    types = new Vector();
		    myParams_.put(SUPPLYTYPES, types);
		}
		types.add(s);
		GLMDebug.DEBUG(className_, getClusterIdentifier(),
			       "readParameters(), assuming a demand type parameter: "+s);
	    }		
	}
    }

    public Object getParam (String key) {
	return myParams_.get(key);
    }
}