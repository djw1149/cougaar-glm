/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.xmlservice;

import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.oplan.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.policy.*;
import org.cougaar.domain.mlm.ui.data.UIInventoryImpl;
import org.cougaar.domain.mlm.ui.data.UISimpleInventory;
import org.cougaar.domain.mlm.ui.data.UISimpleNamedSchedule;
import org.cougaar.domain.mlm.ui.data.UISimpleNamedScheduleNames;

public class PSP_Inventory
  extends PSP_BaseAdapter
  implements PlanServiceProvider, UISubscriber, UISimpleNamedScheduleNames
{
  private String myID;
  public String desiredAssetName;

  public PSP_Inventory() throws RuntimePSPException {
    super();
  }

  public PSP_Inventory(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  /* This PSP is referenced directly (in the URL from the client)
     and hence this shouldn't be called.
     */

  public boolean test(HttpInput query_parameters, PlanServiceContext psc) {
    super.initializeTest();
    return false; 
  }

  /** Called after submitting a subscription; the container
    contains the asset and tasks we need to compute the inventory
    object that we'll return to the client.
    */

  private UIInventoryImpl getInventoryFromLogPlan(Collection container) {
    UIInventoryImpl inventory = new UIInventoryImpl();
    Allocation lastAllocation=null;
    Object o=null;
    Iterator i=null;

    //first set the asset
    for (i = container.iterator(); i.hasNext(); ) {
      o = i.next();
      if (o instanceof Asset) {
	inventory.setAsset((Asset)o);
      }
    }
    //next do the allocation
    for (i = container.iterator(); i.hasNext(); ) {
      o = i.next();
      if (o instanceof Allocation) {
	lastAllocation = (Allocation) o;
	inventory.addDueInSchedule((Allocation)o);
	inventory.addRequestedDueInSchedule((Allocation)o);
      }
    }
    //MWD new code if there are no GLMAsset Inventories it's likely
    //the Asset has not been set on this UIInventoryImpl.
    //There are just allocations in this InventoryPredicate returned
    //collection and hence if (o instanceof Asset) has been used.
    if((lastAllocation != null) &&
       (inventory.getAsset() == null)) {
	Asset directAsset = lastAllocation.getTask().getDirectObject();
	inventory.setAsset(directAsset);	    
    }

    return inventory;
  }

  /** Called to make a UISimpleInventory object which gets serialized
    and sent to the client.  Note that at the client end, the schedule
    type must be in org.cougaar.domain.planning.ldm.plan.ScheduleType, so we fix any bogus
    schedules here and issue warnings.
    */

  private UISimpleInventory getInventoryForClient(UIInventoryImpl inventory,
						  boolean provider,
						  Date cDay) {
    UISimpleInventory inv = new UISimpleInventory();
    
    inv.setAssetName(inventory.getAssetName());
    inv.setUnitType(inventory.getUnitType());
    inv.setProvider(provider); // ui uses this to determine chart labels
    String scheduleType = inventory.getScheduleType();
    if (scheduleType.equals(ScheduleType.OTHER)) {
      scheduleType = PlanScheduleType.TOTAL_INVENTORY;
      System.out.println("WARNING: Treating Other schedule as total inventory");
    }
    inv.setScheduleType(scheduleType);
    inv.setBaseCDay(cDay);

    if (scheduleType.equals(PlanScheduleType.TOTAL_CAPACITY)) {
      inv.addNamedSchedule(ALLOCATED,   inventory.getDueOutLaborSchedule());
      inv.addNamedSchedule(TOTAL_LABOR, inventory.getLaborSchedule());
    } else if (scheduleType.equals(PlanScheduleType.ACTUAL_CAPACITY)) {
      inv.addNamedSchedule(ALLOCATED, inventory.getDueOutLaborSchedule());
      inv.addNamedSchedule(ON_HAND,   inventory.getOnHandSchedule());
    } else if (scheduleType.equals(PlanScheduleType.TOTAL_INVENTORY)) {

      inventory.computeSimulatedProjectionSchedules();

      inv.addNamedSchedule(ON_HAND,                               inventory.getOnHandSchedule());
      inv.addNamedSchedule(DUE_IN,                                inventory.getDueInSchedule());
      inv.addNamedSchedule(UNCONFIRMED_DUE_IN,                    inventory.getUnconfirmedDueInSchedule());
      inv.addNamedSchedule(REQUESTED_DUE_IN,                      inventory.getRequestedDueInSchedule());
      inv.addNamedSchedule(PROJECTED_DUE_IN,                      inventory.getProjectedDueInSchedule());
      inv.addNamedSchedule(PROJECTED_REQUESTED_DUE_IN,            inventory.getProjectedRequestedDueInSchedule());

      inv.addNamedSchedule(DUE_IN                      +INACTIVE, inventory.getInactiveDueInSchedule());
      inv.addNamedSchedule(UNCONFIRMED_DUE_IN          +INACTIVE, inventory.getInactiveUnconfirmedDueInSchedule());
      inv.addNamedSchedule(REQUESTED_DUE_IN            +INACTIVE, inventory.getInactiveRequestedDueInSchedule());
      inv.addNamedSchedule(PROJECTED_DUE_IN            +INACTIVE, inventory.getInactiveProjectedDueInSchedule());
      inv.addNamedSchedule(PROJECTED_REQUESTED_DUE_IN  +INACTIVE, inventory.getInactiveProjectedRequestedDueInSchedule());
          
      inv.addNamedSchedule(DUE_OUT,                               inventory.getDueOutSchedule());
      inv.addNamedSchedule(PROJECTED_DUE_OUT,                     inventory.getProjectedDueOutSchedule());
      inv.addNamedSchedule(REQUESTED_DUE_OUT,                     inventory.getRequestedDueOutSchedule());
      inv.addNamedSchedule(PROJECTED_REQUESTED_DUE_OUT,           inventory.getProjectedRequestedDueOutSchedule());
                                     
      inv.addNamedSchedule(DUE_OUT                     +INACTIVE, inventory.getInactiveDueOutSchedule());
      inv.addNamedSchedule(PROJECTED_DUE_OUT           +INACTIVE, inventory.getInactiveProjectedDueOutSchedule());
      inv.addNamedSchedule(REQUESTED_DUE_OUT           +INACTIVE, inventory.getInactiveRequestedDueOutSchedule());
      inv.addNamedSchedule(PROJECTED_REQUESTED_DUE_OUT +INACTIVE, inventory.getInactiveProjectedRequestedDueOutSchedule());

      //inv.addNamedSchedule(ON_HAND,                               inventory.getOnHandMockSchedule());
      //inv.addNamedSchedule(PROJECTED_DUE_IN,                      inventory.getProjectedMockDueInSchedule());
      //inv.addNamedSchedule(PROJECTED_REQUESTED_DUE_IN,            inventory.getProjectedRequestedMockDueInSchedule());
      //inv.addNamedSchedule(PROJECTED_DUE_OUT,                     inventory.getProjectedMockDueOutSchedule());
      //inv.addNamedSchedule(PROJECTED_REQUESTED_DUE_OUT,           inventory.getProjectedRequestedMockDueOutSchedule());

      inv.addNamedSchedule(ON_HAND_MOCK_PERIOD,                    inventory.getOnHandMockSchedule());
      inv.addNamedSchedule(PROJECTED_DUE_IN_MOCK_PERIOD,           inventory.getProjectedMockDueInSchedule());
      inv.addNamedSchedule(PROJECTED_REQUESTED_DUE_IN_MOCK_PERIOD, inventory.getProjectedRequestedMockDueInSchedule());
      inv.addNamedSchedule(PROJECTED_DUE_OUT_MOCK_PERIOD,          inventory.getProjectedMockDueOutSchedule());
      inv.addNamedSchedule(PROJECTED_REQUESTED_DUE_OUT_MOCK_PERIOD,inventory.getProjectedRequestedMockDueOutSchedule());
      inv.addNamedSchedule(GOAL_LEVEL,              inventory.getGoalLevelSchedule());
      inv.addNamedSchedule(REORDER_LEVEL,           inventory.getReorderLevelSchedule());
      inv.addNamedSchedule(AVERAGE_DEMAND_LEVEL,    inventory.getAverageDemandSchedule());

    } 
    else if (scheduleType.equals(inventory.NO_INVENTORY_SCHEDULE_JUST_CONSUME)){
	inv.addNamedSchedule(DUE_IN,                              inventory.getDueInSchedule());
	inv.addNamedSchedule(UNCONFIRMED_DUE_IN,                  inventory.getUnconfirmedDueInSchedule());
	inv.addNamedSchedule(REQUESTED_DUE_IN,                    inventory.getRequestedDueInSchedule());
	inv.addNamedSchedule(PROJECTED_DUE_IN,                    inventory.getProjectedDueInSchedule());
	inv.addNamedSchedule(PROJECTED_REQUESTED_DUE_IN,          inventory.getProjectedRequestedDueInSchedule());
	inv.addNamedSchedule(DUE_IN                    +INACTIVE, inventory.getInactiveDueInSchedule());
	inv.addNamedSchedule(PROJECTED_DUE_IN          +INACTIVE, inventory.getInactiveProjectedDueInSchedule());
	inv.addNamedSchedule(UNCONFIRMED_DUE_IN        +INACTIVE, inventory.getInactiveUnconfirmedDueInSchedule());
	inv.addNamedSchedule(REQUESTED_DUE_IN          +INACTIVE, inventory.getInactiveRequestedDueInSchedule());
	inv.addNamedSchedule(PROJECTED_REQUESTED_DUE_IN+INACTIVE, inventory.getInactiveProjectedRequestedDueInSchedule());

	System.out.println("getInventoryForClient: returning schedule type: " + scheduleType);

    } 
    else {
      System.out.println("WARNING: Unsupported schedule type: " + 
			 scheduleType);
      return null;
    }
    return inv;
  }

  /*
    Called when a request is received from a client.
    Either gets the command ASSET to return the names of all the assets
    that contain a ScheduledContentPG or
    gets the name of the asset to plot from the client request.
  */

  public void execute( PrintStream out,
		       HttpInput query_parameters,
		       PlanServiceContext psc,
		       PlanServiceUtilities psu) throws Exception {
    try {
      myExecute(out, query_parameters, psc, psu);
    } catch (Exception e) {
      e.printStackTrace();
    };
  }

  /** The query data is one of:
    ASSET -- meaning return list of assets
    nomenclature:type id -- return asset matching nomenclature & type id
    UID: -- return asset with matching UID
    */

  private void myExecute( PrintStream out,
		       HttpInput query_parameters,
		       PlanServiceContext psc,
		       PlanServiceUtilities psu) throws Exception {
    desiredAssetName = "";
    if (query_parameters.hasBody()) {
      desiredAssetName = query_parameters.getBodyAsString();
      desiredAssetName = desiredAssetName.trim();
      System.out.println("POST DATA: " + desiredAssetName);
    } else {
      System.out.println("WARNING: No asset to plot");
      return;
    }

    // return list of asset names
    if (desiredAssetName.equals("ASSET")) {

	/****
	 **

      Vector assetNames = new Vector();
      Subscription subscription = 
	psc.getServerPlugInSupport().subscribe(this, new AssetPredicate());

      Collection container = 
	((CollectionSubscription)subscription).getCollection();
      for (Iterator i = container.iterator(); i.hasNext(); ) {
	GLMAsset asset = (GLMAsset)(i.next());
	TypeIdentificationPG typeIdPG = 
	  asset.getScheduledContentPG().getAsset().getTypeIdentificationPG();
	String nomenclature = typeIdPG.getNomenclature();
	String typeId = typeIdPG.getTypeIdentification();
	if (nomenclature != null)
	  nomenclature = nomenclature + ":" + typeId;
	else
	  nomenclature = typeId;
	assetNames.addElement(nomenclature);
      }

      ****
       * MWD fix and try this out -below
       * MWD get rid of old commented out above replaced by below
       * to get demand even where no inventories.
       ***/
       
       HashSet assetNamesSet = new HashSet();
       Subscription subscription = 
         psc.getServerPlugInSupport().subscribe(this, new DemandObjectPredicate());

      Collection container = 
	((CollectionSubscription)subscription).getCollection();
      for (Iterator i = container.iterator(); i.hasNext(); ) {
	Asset asset = ((Task)(i.next())).getDirectObject();
	TypeIdentificationPG typeIdPG = asset.getTypeIdentificationPG();
	String nomenclature = typeIdPG.getNomenclature();
	String typeId = typeIdPG.getTypeIdentification();
	if (nomenclature != null)
	  nomenclature = nomenclature + ":" + typeId;
	else
	  nomenclature = typeId;
	assetNamesSet.add(nomenclature);
      }

      Vector assetNames = new Vector(assetNamesSet);

      // unsubscribe, don't need this subscription any more
      psc.getServerPluginSupport().unsubscribeForSubscriber(subscription);
      // send the results
      ObjectOutputStream p = new ObjectOutputStream(out);
      p.writeObject(assetNames);
      System.out.println("Sent asset names");
      return;
    } // end returning list of asset names

    if (desiredAssetName.startsWith("UID:")) {
      String desiredAssetUID = desiredAssetName.substring(4);
      Subscription subscription = 
	psc.getServerPluginSupport().subscribe(this, new AssetUIDPredicate(desiredAssetUID));
      Collection collection = 
	((CollectionSubscription)subscription).getCollection();
      for (Iterator i = collection.iterator(); i.hasNext(); ) {
	GLMAsset asset = (GLMAsset)(i.next());
	TypeIdentificationPG typeIdPG = 
	  asset.getScheduledContentPG().getAsset().getTypeIdentificationPG();
	String nomenclature = typeIdPG.getNomenclature();
	String typeId = typeIdPG.getTypeIdentification();
	if (nomenclature == null)
	  return;
	desiredAssetName = nomenclature + ":" + typeId;
      }
      // unsubscribe, don't need this subscription any more
      psc.getServerPluginSupport().unsubscribeForSubscriber(subscription);
    } // end getting asset name from UID

    Date cDay=null;

    // get oplan
    Subscription oplanSubscription =
      psc.getServerPluginSupport().subscribe(this, oplanPredicate());
    Collection oplanCollection =
      ((CollectionSubscription)oplanSubscription).getCollection();
    if (!oplanCollection.isEmpty()) {
      Oplan plan = (Oplan) ((CollectionSubscription)oplanSubscription).first();
      cDay = plan.getCday();
      psc.getServerPluginSupport().unsubscribeForSubscriber(oplanSubscription);
    }

    // get roles and determine if this cluster is a provider (or consumer)
    Subscription roleSubscription =
      psc.getServerPluginSupport().subscribe(this, 
       new RolePredicate(psc.getServerPluginSupport().getClusterIDAsString()));
    Collection roleCollection =
      ((CollectionSubscription)roleSubscription).getCollection();
    boolean provider = false;
    if (!roleCollection.isEmpty()) {
      Organization asset = (Organization) ((CollectionSubscription)roleSubscription).first();
      Collection roles = asset.getOrganizationPG().getRoles();
      Iterator i = roles.iterator();
      while (i.hasNext()) {
	Role role = (Role)i.next();
	if (role.getName().endsWith("Provider")) {
	  provider = true;
	  break;
	}
      }
    }
    psc.getServerPluginSupport().unsubscribeForSubscriber(roleSubscription);

    // get asset and tasks we need to create the inventory
    InventoryPredicate inventoryPredicate = 
      new InventoryPredicate(desiredAssetName, 
		     psc.getServerPluginSupport().getClusterIDAsString());
    Subscription subscription = 
      psc.getServerPluginSupport().subscribe(this, inventoryPredicate);
    Collection collection = 
      ((CollectionSubscription)subscription).getCollection();

    if (collection.isEmpty())
      return;

    // create UIInventory data object from the log plan objects
    UIInventoryImpl inventory = getInventoryFromLogPlan(collection);

    // unsubscribe, don't need this subscription any more
    psc.getServerPluginSupport().unsubscribeForSubscriber(subscription);

    // set values in UISimpleInventory, a serializable object
    UISimpleInventory simpleInventory = 
      getInventoryForClient(inventory, provider, cDay);

    // send the UISimpleInventory object
    if (simpleInventory != null) {
      ObjectOutputStream p = new ObjectOutputStream(out);
      p.writeObject(simpleInventory);
      System.out.println("Sent XML document");
    }
  }

  public boolean returnsXML() {
    return true;
  }

  public boolean returnsHTML() {
    return false;
  }

  public String getDTD() {
    return "myDTD";
  }

  /* The UISubscriber interface.
     This PSP doesn't care if subscriptions change
     because it treats each request as a new request.
  */

  public void subscriptionChanged(Subscription subscription) {
  }

  private static UnaryPredicate oplanPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return (o instanceof Oplan);
      }
    };
  }

}

  /** Get asset which represents this cluster.
    */

class RolePredicate implements UnaryPredicate {
  String myCluster;

  public RolePredicate(String myCluster) {
    this.myCluster = myCluster;
  }

  public boolean execute(Object o) {
    if (o instanceof Organization) {
      Organization asset = (Organization)o;
      String s = asset.getItemIdentificationPG().getNomenclature();
      if (s != null)
	if (s.equals(myCluster))
	  return true;
    }
    return false;
  }

}

  /** Subscribes to objects that provide quantity on hand, due-ins, & due-outs
    for the specified asset.
    For quantity on hand, get assets with a ScheduledContentPG in which
    the asset.typeIdentificationPG.nomenclature matches the specified asset.
    The ScheduledContentPG schedule indicates the start and end dates and
    quantities.

    For due-outs, get allocations with assets with a ScheduledContentPG in which
    the asset.typeIdentificationPG.nomenclature matches the specified asset.
    The allocation's reportedResult aspectTypes and results encode
    the quantity and start date.
    PAS - get these from the inventory role schedule?

    For due-ins, get tasks with direct objects in which the
    asset.typeIdentificationPG.nomenclature is the desired asset.
    Get the allocations for these tasks, 
    the allocation's reportedResult encodes
    the quantity, and the start and end dates.
    (Actually obtained by getting the allocations and then checking
    the task in the allocation.)
    */

class InventoryPredicate implements UnaryPredicate {
  String desiredAssetName; // nomenclature:type id
  ClusterIdentifier myClusterId;

  public InventoryPredicate(String desiredAssetName, String myCluster) {
    this.desiredAssetName = desiredAssetName;
    myClusterId = new ClusterIdentifier(myCluster);
  }

  private boolean assetMatch(Asset asset) {
    TypeIdentificationPG typeIdPG = asset.getTypeIdentificationPG();
    if (typeIdPG == null) {
      System.out.println("WARNING: No typeIdentificationPG for asset");
      return false;
    }
    String nomenclature = typeIdPG.getNomenclature();
    String typeId = typeIdPG.getTypeIdentification();
    if (nomenclature == null)
      return false;
    nomenclature = nomenclature + ":" + typeId;
    return nomenclature.equals(desiredAssetName);
  }

  /** Get assets with scheduledContentPG such that
    scheduledContentPG.getAsset().getTypeIdentificationPG().getNomenclature 
    equals desiredAssetName and get Tasks from Allocations such that
    Task.getDirectObject.getTypeIdentificationPG().getNomenclature
    equals desiredAssetName and task.getVerb is SUPPLY and
    Allocation.getAsset is an organization asset.
    Also matches if asset uid is equal to desiredAssetName -- i.e.
    the client can pass in a UID instead of the asset name.
    */

  public boolean execute(Object o) {
    if (o instanceof GLMAsset) {
	// looking for Inventory Assets
      GLMAsset asset = (GLMAsset)o;
      ScheduledContentPG scheduledContentPG = asset.getScheduledContentPG();
      if (scheduledContentPG == null)
	return false;
      Asset a1 = scheduledContentPG.getAsset();
      if (a1 == null) {
	System.out.println("WARNING: no asset in scheduledContentPG");
	return false;
      }
      return assetMatch(a1);
    } else if (o instanceof Allocation) {
	// looking for due ins
      Allocation allocation = (Allocation)o;
      if (!(allocation.getAsset() instanceof Organization))
	return false;
      Task task = allocation.getTask();
      if (!((task.getVerb().equals(Constants.Verb.SUPPLY)) ||
	    (task.getVerb().equals(Constants.Verb.PROJECTSUPPLY))))
	  return false;
      Object directObject = task.getDirectObject();
      if (directObject == null)
	return false;
      if (!(directObject instanceof Asset))
	return false;
      boolean aMatch = assetMatch((Asset)directObject);
      /** MWD Debug
      if(aMatch) {
	  System.out.println("PSP_Inventory::InventoryPredicate:Matching allocations task is with Verb: " + task.getVerb());
      }
      */
      return aMatch;
    }
    return false; 
  }
}

class AssetPredicate implements UnaryPredicate {

  public boolean execute(Object o) {
    if (!(o instanceof GLMAsset))
      return false;
    GLMAsset asset = (GLMAsset)o;
    ScheduledContentPG scheduledContentPG = asset.getScheduledContentPG();
    if (scheduledContentPG == null)
      return false;
    Asset a1 = scheduledContentPG.getAsset();
    if (a1 == null) {
      System.out.println("WARNING: no asset in scheduledContentPG");
      return false;
    }
    TypeIdentificationPG typeIdPG = a1.getTypeIdentificationPG();
    if (typeIdPG == null) {
      System.out.println("WARNING: No typeIdentificationPG for asset");
      return false;
    }
    return true;
  }
}

class DemandObjectPredicate implements UnaryPredicate {

  public boolean execute(Object o) {
    if (!(o instanceof Task))
      return false;
    Task task = (Task)o;
    if(!((task.getVerb().equals(Constants.Verb.PROJECTSUPPLY)) ||
	 (task.getVerb().equals(Constants.Verb.SUPPLY)))) 
	return false;
    Asset asset = task.getDirectObject();
    if (asset == null)
	return false;
    TypeIdentificationPG typeIdPG = asset.getTypeIdentificationPG();
    if (typeIdPG == null) {
      System.out.println("WARNING: No typeIdentificationPG for asset");
      return false;
    }
    return true;
  }
}

class AssetUIDPredicate implements UnaryPredicate {
  String desiredAssetUID;

  public AssetUIDPredicate(String desiredAssetUID) {
    this.desiredAssetUID = desiredAssetUID;
  }

  public boolean execute(Object o) {
    if (!(o instanceof GLMAsset))
      return false;
    GLMAsset asset = (GLMAsset)o;
    if (asset.getUID() == null)
      return false;
    if (!asset.getUID().toString().equals(desiredAssetUID))
      return false;
    ScheduledContentPG scheduledContentPG = asset.getScheduledContentPG();
    if (scheduledContentPG == null)
      return false;
    Asset a1 = scheduledContentPG.getAsset();
    if (a1 == null) {
      System.out.println("WARNING: no asset in scheduledContentPG");
      return false;
    }
    TypeIdentificationPG typeIdPG = a1.getTypeIdentificationPG();
    if (typeIdPG == null) {
      System.out.println("WARNING: No typeIdentificationPG for asset");
      return false;
    }
    return true;
  }

}







