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

package org.cougaar.mlm.plugin.generic;

import org.cougaar.util.StateModelException;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.core.domain.RootFactory;

import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.core.blackboard.IncrementalSubscription;

import org.cougaar.glm.GLMPlugin;

import org.cougaar.util.UnaryPredicate;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Calendar;
import java.util.Date;
import java.io.*;

import FESI.jslib.*; 

public class GenericScriptablePlugin  
  extends GLMPlugin
{

  IncrementalSubscription[] mySubscriptions = new IncrementalSubscription[10];
  JSGlobalObject global = null; 

  protected void setupSubscriptions() {
    RootFactory ldmf = theLDMF;

    try { 
      Vector all_parameters = getParameters();
      String scriptFile = (String)all_parameters.elementAt(0);
      System.out.println( " The scriptName is: " + scriptFile );

      String alpInstallPath = System.getProperty("org.cougaar.install.path");
      String sep = System.getProperty("file.separator");
      String helperScriptFile = alpInstallPath + sep + "external" + sep + "alpine" + sep + "data" + sep + "generic" + sep + "scripts" + sep + "HelperFunctions.es";
      //	    String helperScriptFile = "/home/pcheruku/alpine/alp/external/alpine/data/generic/scripts/HelperFunctions.es";

      //Load the two basic FESI extensions.
      String extensions [] = new String[3];
      extensions[0] = "FESI.Extensions.FileIO";
      extensions[1] = "FESI.Extensions.BasicIO";
      extensions[2] = "FESI.Extensions.JavaAccess";
 
      global = JSUtil.makeEvaluator( extensions ); 

      //Read the helper methods.
      FileInputStream helperFile = new FileInputStream( helperScriptFile );
      BufferedReader helperReader = new BufferedReader( new InputStreamReader ( helperFile ) );
      global.eval( helperReader  , "Helper File Evaluator");

      //Get all the subscriptions from the script.
      FileInputStream fi = new FileInputStream( scriptFile );
      BufferedReader bf = new BufferedReader( new InputStreamReader ( fi ) );
      global.eval( bf , "Script File Evaluator");

      //setup properties in ECMAScript for the  helper methods before doing anything else.
      global.setMember("ldmf", ldmf );
      global.setMember("_plugin", this );  //for now use the _plugin variable, but later use of this should be avoided. 

      global.eval( "arrayPredicates = scriptSubscription()" );
      //global.eval( "writeln( \"array predicates are : \"); " );
      //global.eval( "writeln( arrayPredicates )" );
      global.eval( "scriptVector = arrayToVector( arrayPredicates )" );
      Vector myPredicates = new Vector();
      myPredicates = (Vector) global.getMember("scriptVector");

      System.out.println( " The no. of predicates returned by ECMAScript are: " + myPredicates.size()  ); 

      int i = 0;
      Enumeration enumpreds = myPredicates.elements();
      while ( enumpreds.hasMoreElements() ) {
        UnaryPredicate myPred = (UnaryPredicate)(enumpreds.nextElement());
        mySubscriptions[i] =  (IncrementalSubscription) subscribe(myPred);
        i++;
      }


    } catch (JSException e) { 
      System.err.println(e); 
      System.exit(1); 
    } catch (FileNotFoundException fe ) {
      System.err.println("FileNotFoundException. Please give the right parameters to the plugin" );
      fe.printStackTrace();
    }

  }

  public void execute() {

    try {
      global.setMember( "mySubscriptions", mySubscriptions );
      global.eval(" scriptExecute( mySubscriptions ) " );
    } catch ( JSException je ) {
      System.out.println( " Exception when trying to run the scriptExecute method from the script file");
      je.printStackTrace();
    }
	
  }


  //Work around for a possible ( have to investigate more ) bug in ECMAScript. Just calls the getSubscriber();
  public BlackboardService getPluginSubscriber() {
    return getBlackboardService();
  }
}
