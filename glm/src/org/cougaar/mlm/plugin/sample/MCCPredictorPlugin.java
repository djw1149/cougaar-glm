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

package org.cougaar.mlm.plugin.sample;

import java.util.Collection;
import java.util.Iterator;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.asset.NewClusterPG;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.Enumerator;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;

/**
 * MCCPredictorPlugin - 
 * add MCCPredictor to the self org's ClusterPG
 * 
 * Waits to add predictor until after the self org
 * has reported for Service. Delay designed to 
 * test OrgReportPlugin's support for resending asset
 * transfers when the self org changes.
 */
public class MCCPredictorPlugin extends SimplePlugin {
  private IncrementalSubscription orgAssets;
  
  private static UnaryPredicate orgAssetPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Organization) {
          return ((Organization) o).isSelf();
        }
        return false;
      }
    };
  }
  
  private void addPredictor(Enumerator orgs) {
    MCCPredictor predictor = new MCCPredictor();
    while (orgs.hasMoreElements()) {
      // We should only see one copy of self here
      Organization org = (Organization)orgs.nextElement();
      ((NewClusterPG)org.getClusterPG()).setPredictor(predictor);
      publishChange(org);
    }
  }
  
  public void setupSubscriptions() {
    orgAssets = (IncrementalSubscription)subscribe(orgAssetPred());
  }
  
  public void execute() {
    // New copy of myself (should occur only once per society)
    if (orgAssets.hasChanged()) {
      for (Iterator iterator = orgAssets.getCollection().iterator();
           iterator.hasNext();) {
        Organization selfOrg = (Organization) iterator.next();
        
        // Bail if we already have a predictor
        if (selfOrg.getClusterPG().getPredictor() == null) {
          // Wait to add predictor until after we have some customers
          Collection customers = 
            selfOrg.getRelationshipSchedule().getMatchingRelationships(Constants.RelationshipType.CUSTOMER_SUFFIX,
                                                                       TimeSpan.MIN_VALUE,
                                                                       TimeSpan.MAX_VALUE);

          if (!customers.isEmpty()) {
            addPredictor(new Enumerator(orgAssets.getCollection()));
            break;
          }
        }
      }
    }
  }
}





