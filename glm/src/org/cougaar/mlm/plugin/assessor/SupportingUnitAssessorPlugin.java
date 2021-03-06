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

package org.cougaar.mlm.plugin.assessor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.planning.ldm.plan.NewReport;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;

/**
 * The SupportingUnitAssessorPlugin monitors organization assets and generates
 * an Report if one is added/modified/deleted.
 *
 */

public class SupportingUnitAssessorPlugin extends SimplePlugin {

  private IncrementalSubscription mySelfOrgSubscription;
  private String []myMessageArgs;

  /* Changes to formats ==> changes to getMessageArgs()
   */
  private final MessageFormat myFormat = 
    new MessageFormat("Cluster {0}: {1} has changed: supporting and subordinate relationships {2}.");

  private final MessageFormat myRemoveFormat = 
    new MessageFormat("Cluster {0}: {1} has been removed: supporting and subordinate relationships {2}.");


  /**
   * selfOrgPred - returns an UnaryPredicate to find self organizations.
   *
   * @return UnaryPredicate 
   */
  private UnaryPredicate selfOrgPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        boolean match = false;

        if (o instanceof Organization) {
          Organization org = (Organization)o;

          return (org.isSelf());
        } else {
          return false;
        }
      }
    };
  }

  /**
   * execute - CCV2 execute method - called whenever IncrementalSubscription
   * has changed. 
   *
   */
  public synchronized void execute() {
    if (mySelfOrgSubscription.hasChanged()) {
      checkAdd(mySelfOrgSubscription.getAddedCollection());
      checkChange(mySelfOrgSubscription.getChangedCollection());
      checkRemove(mySelfOrgSubscription.getRemovedCollection());
    } 
  }

  /**
   * setUpSubscriptions - sets up subscription to screen for supporting
   * organization assets
   */
  protected void setupSubscriptions() {
    initMessageArgs();
    mySelfOrgSubscription = 
      (IncrementalSubscription)subscribe(selfOrgPred());
  }

  /**
   * initMessageArgs - initializes message argument array. Changes to
   * message formats (myAddFormat, myModifyFormat, myRemoveFormat) must be
   * accompanied with cahnges to initMessageArgs.
   *
   * Using one arg array for all formats at this point. Cluster name at 
   * index 0.
   */
  private void initMessageArgs() {
    myMessageArgs = new String[3];

    myMessageArgs[0] = getMessageAddress().toString();
  }

  /**
   * checkAdd - handle new self organizations
   * Generates an Report for each.
   *
   * @param newOrgs Collection of the added self orgs
   */
  private void checkAdd(Collection newOrgs) {
    if (newOrgs == null) {
      return;
    }

    Iterator iterator = newOrgs.iterator();

    while (iterator.hasNext()) {
      Organization org = (Organization)iterator.next();
      Collection supportingRelationships = findSupportingRelationships(org);

      if (supportingRelationships.size() > 0) {
        NewReport report = getFactory().newReport();
        
        report.setDate(new Date(getAlarmService().currentTimeMillis()));
        report.setText(myFormat.format(getMessageArgs(org, 
                                                      supportingRelationships)));
        
        System.out.println(report.getText());
        
        publishAdd(report);
      }
    }
  }

  /**
   * checkChange - handle all the modified self orgs
   * Generates an Report for each.
   * BOZO - do we really want to know about all changes?
   *
   * @param changedOrgs Collection of all the modified self Orgs.
   */
  private void checkChange(Collection changedOrgs)  {
    if (changedOrgs == null) {
      return;
    }

    Iterator iterator = changedOrgs.iterator();

    while (iterator.hasNext()) {
      Organization org = (Organization)iterator.next();
      Collection supportingRelationships = findSupportingRelationships(org);

      if (supportingRelationships.size() > 0) {
        NewReport report = getFactory().newReport();
        
        report.setDate(new Date(getAlarmService().currentTimeMillis()));
        report.setText(myFormat.format(getMessageArgs(org, 
                                                      supportingRelationships)));
        
        System.out.println(report.getText());
        publishAdd(report);
      }
    }
  }

  /**
   * checkRemove - handle all the removed self orgs
   * Generates an Report for each.
   *
   * @param removedOrgs Collection of all the removed self orgs
   */
  private void checkRemove(Collection removedOrgs)  {
    if (removedOrgs == null) {
      return;
    }

    Iterator iterator = removedOrgs.iterator();

    while (iterator.hasNext()) {
      Organization org = (Organization)iterator.next();
      Collection supportingRelationships = findSupportingRelationships(org);

      if (supportingRelationships.size() > 0) {
        NewReport report = getFactory().newReport();
        
        report.setDate(new Date(getAlarmService().currentTimeMillis()));
        report.setText(myRemoveFormat.format(getMessageArgs(org, 
                                                            supportingRelationships)));
        
        System.out.println(report.getText());
        
        publishAdd(report);
      }
    }
  }

  private static Role SUPERIOR = Role.getRole("Superior");

  /**
   * findSupportingRelationships - returns all supporting relationships for 
   * the specified organization
   *
   * @param org Organization
   * @return Collection supporting relationships
   */
  private Collection findSupportingRelationships(Organization org) {
    RelationshipSchedule schedule = org.getRelationshipSchedule();

    Collection subordinates = org.getSubordinates(TimeSpan.MIN_VALUE,
                                                  TimeSpan.MAX_VALUE);

    Collection providers = 
      schedule.getMatchingRelationships(Constants.RelationshipType.PROVIDER_SUFFIX,
                                        TimeSpan.MIN_VALUE,
                                        TimeSpan.MAX_VALUE); 

    ArrayList supporting = new ArrayList(providers);

    for (Iterator iterator = subordinates.iterator();
         iterator.hasNext();) {
      supporting.add(iterator.next());
    }

    return supporting;
  }

  /**
   * getMessageArgs - returns array of message arguments to be used 
   * with message formats.
   * Changes to message formats must be coordinated with changes to
   * getMessageArgs.
   *
   * @param org Organization 
   * @return String[] info from org which will be used by the message 
   * formattor.
   */
  private String []getMessageArgs(Organization org, 
                                  Collection supportingRelationships) {
    //myMessageArgs[0] set to cluster name
    
    myMessageArgs[1] = getName(org);

    boolean first = true;
    String supportInfo = "";
    RelationshipSchedule schedule = org.getRelationshipSchedule();
    for (Iterator iterator = supportingRelationships.iterator();
         iterator.hasNext();) {
      Relationship relationship = (Relationship) iterator.next();
      String start = (relationship.getStartTime() == TimeSpan.MIN_VALUE) ?
        "TimeSpan.MIN_VALUE" : new Date(relationship.getStartTime()).toString();
      String end = (relationship.getEndTime() == TimeSpan.MAX_VALUE) ?
        "TimeSpan.MAX_VALUE" : new Date(relationship.getEndTime()).toString();

      String text = supportInfo + " " + schedule.getOtherRole(relationship) + 
        " " +  schedule.getOther(relationship) + 
        " start:" + start + " end:" + end;
      if (first) {
        supportInfo = text;
        first = false;
      } else {
        supportInfo = supportInfo + ", " + text;
      }
    }
     
    myMessageArgs[2] = supportInfo;
    return myMessageArgs;
  }

  /**
   * getName - return organization's name
   *
   * @param org Organization
   * @return String
   */
  private static String getName(Organization org) {
    ItemIdentificationPG itemIdentificationPG = org.getItemIdentificationPG();
    
    return itemIdentificationPG.getNomenclature();
  }
}







