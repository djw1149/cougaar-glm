/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/callback/GLMOrganizationListener.java,v 1.4 2004-03-18 20:50:17 mthome Exp $ */
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

package org.cougaar.glm.callback;

import java.util.Enumeration;

import org.cougaar.lib.callback.UTILFilterCallbackListener;

/**
 * defines interface for org listener.
 * currently only concerned about new and changed organizations
 */

public interface GLMOrganizationListener extends UTILFilterCallbackListener {
  /**
   * Place to handle new organizations.
   * @param  e -- new organizations found in the container
   */
  void handleNewOrganization     (Enumeration e);

  /**
   * Place to handle changed organizations.
   * @param  e -- changed organizations found in the container
   */
  void handleChangedOrganization (Enumeration e);
}
        
        
                
                        
                
        
        
