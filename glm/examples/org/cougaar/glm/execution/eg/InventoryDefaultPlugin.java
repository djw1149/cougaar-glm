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
package org.cougaar.glm.execution.eg;

import  org.cougaar.glm.execution.common.*;

public class InventoryDefaultPlugin implements InventoryPlugin {
  /**
   * @return the name of this plugin
   **/
  public String getPluginName() {
    return "Default";
  }

  public String getDescription() {
    return "Default plugin leaves inventory reports unmodified";
  }

  public void setParameter(String parameter) {
    // No parameter needed
  }

  public boolean isConfigurable() {
    return false;
  }

  public void configure(java.awt.Component c) {}

  public void save(java.util.Properties props, String prefix) {
  }

  public void restore(java.util.Properties props, String prefix) {
  }

  public void setEventGenerator(EventGenerator eg) {
 }


  /**
   * Apply this plugin to an InventoryReport,
   * @return true if this plugin was applicable to the report.
   **/
  public boolean apply(TimedInventoryReport tir, long theExecutionTime) {
    return true;
  }
}