/*--------------------------------------------------------------------------
 *                         RESTRICTED RIGHTS LEGEND
 *
 *   Use, duplication, or disclosure by the Government is subject to
 *   restrictions as set forth in the Rights in Technical Data and Computer
 *   Software Clause at DFARS 52.227-7013.
 *
 *                             BBNT Solutions LLC,
 *                             10 Moulton Street
 *                            Cambridge, MA 02138
 *                              (617) 873-3000
 *
 *   Copyright 2000 by
 *             BBNT Solutions LLC,
 *             all rights reserved.
 *
 * --------------------------------------------------------------------------*/
package org.cougaar.domain.glm.plugins.inventory;

import org.cougaar.*;
import org.cougaar.core.cluster.*;
import org.cougaar.core.plugin.util.AllocationResultHelper;
import org.cougaar.domain.planning.ldm.*;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.util.UnaryPredicate;
import java.io.*;
import java.lang.*;
import java.util.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.oplan.Oplan;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.GLMFactory;
import org.cougaar.domain.glm.plugins.*;
import org.cougaar.domain.glm.debug.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.asset.ProjectionWeight;

/** Allocate SUPPLY tasks to local inventory (if there is any) or to 
 *  the closest supplier.
 */
public class GeneralInventoryManager extends InventoryManager {
    
  private IncrementalSubscription refillAllocs_ = null;

  /** Constructor */
  public GeneralInventoryManager(InventoryPlugIn plugin, Organization org, String type)
  {
    super(plugin, org, type);
    printLog("Constructor type:"+type);
    initialize();

  }

  // ********************************************************
  //                                                        *
  // Point Of Entry to GeneralInventoryManager              *
  //                                                        *
  // ********************************************************

  // Uses update() from superclass (Template Method pattern) (learned at JavaOne)
  // It should not be overridden.
  // This method is called everytime a subscription has changed.



  // ********************************************************
  //                                                        *
  // Need Update / Reset Section                            *
  //                                                        *
  // ********************************************************

  // RJB changed
  protected Set needUpdate(Set invSet) {
    if (invSet == null) invSet = new HashSet();
    // Determine if this inventory processor needs to run
    // return a set of changed inventories to re-process
    boolean refill_changed = checkRefills(refillAllocs_, invSet);
    boolean inventory_changed = checkInventories(modifiedInventorySubscription_.getChangedList(), invSet);
    boolean inventory_policy_changed = checkInventoryPolicies(inventoryPolicySubscription_, 
							      modifiedInventorySubscription_.elements(), invSet);
            
    // Allocations of tasks with quantity > 0 to Inventory objects
    // inventoryAllocSubscription_ only used to determine when to run processor.
    // Inventory objects held in the plugin.
    boolean allocatedInventories = checkInventoryAllocations(inventoryAllocSubscription_, invSet);

    if (refill_changed  || allocatedInventories || inventory_changed || inventory_policy_changed) {
      String prefix = "<" + supplyType_ + "> UPDATING INVENTORIES: ";
      if (refill_changed          ) printLog(prefix + "refill changed.");
      if (allocatedInventories    ) printLog(prefix + "allocations added/removed.");
      if (inventory_changed       ) printLog(prefix + "inventory changed.");
      if (inventory_policy_changed) printLog(prefix + "inventory policy changed.");
    }
    return invSet;
  }

  private boolean checkRefills(IncrementalSubscription refillAllocs, Set invSet) {
    Enumeration refills = refillAllocs.getChangedList();
    boolean changed = false;
    while (refills.hasMoreElements()) {
      Allocation alloc = (Allocation) refills.nextElement();
      Set changes = refillAllocs.getChangeReports(alloc);
      if (TaskUtils.checkChangeReports(changes, PlanElement.EstimatedResultChangeReport.class)) {
	Task refill = alloc.getTask();
	MaintainedItem inventoryID = 
	  (MaintainedItem)refill.getPrepositionalPhrase(Constants.Preposition.MAINTAINING).getIndirectObject();
	Inventory inv = inventoryPlugIn_.findOrMakeInventory(supplyType_, inventoryID.getTypeIdentification());
	if (inv != null) {
	  invSet.add(inv);
	  changed = true;
	}
      }
    }
    return changed;
  }

  private boolean checkInventories(Enumeration changedInventories, Set invSet) {
    boolean changed = changedInventories.hasMoreElements();
    while (changedInventories.hasMoreElements()) {
      invSet.add((Inventory) changedInventories.nextElement());
    }
    return changed;
  }

  private boolean checkInventoryPolicies(IncrementalSubscription policySubscription, Enumeration inventories, Set invSet) {
    boolean changed = updateInventoryPolicy(policySubscription.getAddedList()) ||
      updateInventoryPolicy(policySubscription.getChangedList());
    if (changed) {
      while (inventories.hasMoreElements()) {
	invSet.add((Inventory) inventories.nextElement());
      }
    }
    return changed;
  }

  private boolean checkInventoryAllocations(IncrementalSubscription invAllocSubscription, Set invSet) {
    boolean changed = false;
    if (invAllocSubscription.hasChanged()) {
      Enumeration allocs = invAllocSubscription.getAddedList();
      while (allocs.hasMoreElements()) {
	Allocation alloc = (Allocation) allocs.nextElement();
	if (!inventoryPlugIn_.hasSeenAllConsumers()) {
	  inventoryPlugIn_.recordCustomerForTask(alloc.getTask());
	}
	invSet.add(alloc.getAsset());
	changed = true;
      }
      allocs = invAllocSubscription.getRemovedList();
      while (allocs.hasMoreElements()) {
	invSet.add(((Allocation) allocs.nextElement()).getAsset());
	changed = true;
      }
    }
    return changed;
  }

  // ********************************************************
  //                                                        *
  // Utilities Section                                      *
  //                                                        *
  // ********************************************************

  // Public

  private Enumeration getAllScheduleElements(Inventory inventory) {
    ScheduledContentPG scp = inventory.getScheduledContentPG();
    return scp.getSchedule().getAllScheduleElements();
  }
        
  // this should be over-ridden for specialized inventory managers
  protected double getMinRefillQuantity(Inventory inventory) {
    return inventory.getVolumetricStockagePG().getMinReorderVolume().getGallons();
  }

  public void printInventoryBins() {
    printInventoryBins(0);
  }

  // ********************************************************
  //                                                        *
  // Initialization  Section                                *
  //                                                        *
  // ********************************************************

  //Allocation of refill tasks
  static class RefillAllocPredicate implements UnaryPredicate
  {
    String type_;
    String orgName_;

    public RefillAllocPredicate(String type, String orgName) {
      type_ = type;
      orgName_ = orgName;
    }

    public boolean execute(Object o) {
      if (o instanceof Allocation ) {
	Task task = ((Allocation)o).getTask();
	Verb verb = task.getVerb();
	if (verb.equals(Constants.Verb.SUPPLY)
	    || verb.equals(Constants.Verb.PROJECTSUPPLY)) {
	  if (TaskUtils.isDirectObjectOfType(task, type_)) {
	    // need to check if externally allocated
	    if(((Allocation)o).getAsset() instanceof Organization) {
	      if (TaskUtils.isMyRefillTask(task, orgName_)){
		return true;
	      }
	    }
	  }
	}
      }
      return false;
    }
  }

  /** Initialize this instance. */
  protected void initialize() {
    setupSubscriptions();
  }

  /** Initialize my subscriptions. */
  protected void setupSubscriptions() {
	
    refillAllocs_ = subscribe(new RefillAllocPredicate(supplyType_, myOrgName_));
	
    if (plugin_.didRehydrate()) {
      updateInventoryPolicy(Collections.enumeration(inventoryPolicySubscription_.getCollection()));
    }

  }
}
