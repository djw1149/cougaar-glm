/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

package org.cougaar.glm.ldm.plan;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.plan.Directive;
import org.cougaar.planning.ldm.plan.DirectiveImpl;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

public class QueryRequestAssignment extends DirectiveImpl
{
  private QueryRequest _queryRequest;

  public QueryRequestAssignment(QueryRequest request) {

    _queryRequest = request;
    // we are sending this from the requesting cluster
    super.setSource(request.getRequestingCluster());

    // The source of the information, not the source of the request
    super.setDestination(request.getSourceCluster());
  }

  public QueryRequest getQueryRequest() {
    return _queryRequest;
  }
  
  public void setQueryRequest(QueryRequest request) {
    _queryRequest = request;
  }

  public String toString() {
    String descr = "(Null AssignedQueryRequest)";
    if( _queryRequest != null ) descr = _queryRequest.toString();

    return "<QueryRequestAssignment "+descr+", " + ">" + super.toString();
  }
}