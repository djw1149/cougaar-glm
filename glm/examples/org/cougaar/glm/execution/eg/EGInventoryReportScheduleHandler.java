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
package org.cougaar.glm.execution.eg;

import org.cougaar.glm.execution.common.*;

public class EGInventoryReportScheduleHandler {
  private InventoryScheduleManager theScheduleManager;

  public EGInventoryReportScheduleHandler(InventoryScheduleManager aScheduleManager) {
    theScheduleManager = aScheduleManager;
  }

  public void execute(String source, InventoryReportSchedule schedule) {
    execute(source, new InventoryReportSchedule[] {schedule});
  }

  public void execute(String source, InventoryReportSchedule[] schedules) {
    theScheduleManager.receiveInventoryReportSchedules(source, schedules);
  }

  public void execute(String source, InventoryReportSchedule.Rescind schedule) {
    execute(source, new InventoryReportSchedule.Rescind[] {schedule});
  }

  public void execute(String source, InventoryReportSchedule.Rescind[] schedules) {
    theScheduleManager.receiveInventoryReportSchedules(source, schedules);
  }
}

  
