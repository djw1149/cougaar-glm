package org.cougaar.glm.servlet;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.domain.*;
import org.cougaar.core.service.AlarmService;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.NamingService;
import org.cougaar.core.service.SchedulerService;
import org.cougaar.core.servlet.SimpleServletSupportImpl;
import org.cougaar.util.ConfigFinder;
import org.cougaar.core.servlet.BlackboardServletSupport;

/** 
 * <pre>
 * This support class offers additional services on top of the
 * SimpleServletSupport class, including access to the blackboard,
 * config finder, root factory, ldm serves plugin, and scheduler service.
 * </pre>
 */
public class CompletionWatcherSupport extends BlackboardServletSupport {
  public CompletionWatcherSupport(
      String path,
      ClusterIdentifier agentId,
      BlackboardService blackboard,
      NamingService ns,
      LoggingService logger,
      ConfigFinder configFinder,
      RootFactory ldmf,
      LDMServesPlugin ldm,
      SchedulerService scheduler,
      AlarmService alarm) {
    super (path, agentId, blackboard, ns, logger, configFinder, ldmf, ldm, scheduler);
    this.alarm = alarm;
  }

  protected AlarmService alarm;
  public AlarmService getAlarmService () { return alarm; }
}
