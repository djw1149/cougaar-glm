/*
 * <copyright>
 * Copyright 1997-2000 Defense Advanced Research Projects Agency (DARPA)
 * and Clark Software Engineering (CSE) This software to be used in
 * accordance with the COUGAAR license agreement.  The license agreement
 * and other information on the Cognitive Agent Architecture (COUGAAR)
 * Project can be found at http://www.cougaar.org or email: info@cougaar.org.
 * </copyright>
 */
package org.cougaar.domain.mlm.ui.readiness;

import java.io.PrintStream;

import java.util.Vector;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Date;
import java.util.GregorianCalendar;

import java.util.Collection;
import java.util.List;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.glm.ldm.Constants;

import org.cougaar.lib.planserver.HttpInput;
import org.cougaar.lib.planserver.PlanServiceContext;
import org.cougaar.lib.planserver.PlanServiceUtilities;

import org.cougaar.core.cluster.Subscription;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.planning.ldm.plan.AspectValue;

import org.cougaar.domain.mlm.plugin.assessor.ReadinessAssessorPSPPlugIn;
import org.cougaar.domain.glm.plugins.MaintainedItem;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.ItemIdentificationPG;


/***********************************************************************************************************************
<b>Description</b>: PSP class to provide initial HTML page for the readiness assessor chart applet.  This PSP sends
                    the
										required HTML for finding the readiness assesssor chart applet and accessing the KeepAlive PSP that
										provides the readiness assessor chart applet with data.

<br><br><b>Notes</b>:<br>
									-

@author Eric B. Martin, &copy;2000 Clark Software Engineering, Ltd. & Defense Advanced Research Projects Agency (DARPA)
@version 1.0
***********************************************************************************************************************/
public class PSP_ReadinessAssessor extends org.cougaar.lib.planserver.PSP_BaseAdapter implements org.cougaar.lib.planserver.PlanServiceProvider, org.cougaar.lib.planserver.UISubscriber
{


	/*********************************************************************************************************************
  <b>Description</b>: Default constructor.  This constructor simply calls its super class default constructor to ensure
  										the instance is properly constructed.

  <br><b>Notes</b>:<br>
	                  -
	*********************************************************************************************************************/
	public PSP_ReadinessAssessor()
	{
		super();
	}

	/*********************************************************************************************************************
  <b>Description</b>: Constructor.  This constructor sets the PSP's resource location according to the parameters
  										passed in.

  <br><b>Notes</b>:<br>
	                  -

  <br>
  @param pkg The package id
  @param id The PSP name

  @throws RuntimePSPException
	*********************************************************************************************************************/
	public PSP_ReadinessAssessor (String pkg, String id) throws org.cougaar.lib.planserver.RuntimePSPException
	{
		setResourceLocation(pkg, id);
	}

	/*********************************************************************************************************************
  <b>Description</b>: Main entry point for starting the BOL inventroy chart applet KeepAlive PSP connection.

  <br><b>Notes</b>:<br>
	                  - Uses a BOLPSPState object to hold PSP configuration and HTTP request data<BR>
	                  - Catches all Throwable objects and prints a stack trace to the HTTP response output

  <br>
  @param out HTTP response socket stream
  @param queryParameters HTTP parameter data and connection information
  @param psc Current Plan Service Context object
  @param psu Utility functions for the PSP

  @throws Exception 
	*********************************************************************************************************************/
	public void execute(PrintStream out, HttpInput query_parameters, PlanServiceContext psc, PlanServiceUtilities psu) throws Exception
	{

    String clusterId = psc.getServerPluginSupport().getClusterIDAsString();

		try
		{

			// Default page displayed when PSP is first accessed
			out.println("<HTML><HEAD><TITLE>Readiness Assessor</TITLE></HEAD><BODY BACKGROUND='images/clouds.gif'>");
//out.println("<OBJECT classid=\"clsid:CAFEEFAC-0013-0001-0000-ABCDEFFEDCBA\"");
	      out.println("<OBJECT classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\"");
				//out.println("WIDTH = 600 HEIGHT = 600 NAME = \"NChartApplet\"  codebase=\"http://java.sun.com/products/plugin/1.3.1/jinstall-131-win32.cab#Version=1,3,1,0\">");
				out.println("WIDTH = 600 HEIGHT = 600 codebase=\"http://java.sun.com/products/plugin/1.2/jinstall-12-win32.cab#Version=1,2,0,0\">");
				out.println("<PARAM NAME = CODE VALUE = \"NChartApplet.class\" >");
				out.println("<PARAM NAME = ARCHIVE VALUE = \"/big.jar\" >");
				//out.println("<PARAM NAME = CODEBASE = \".\" >");
				//out.println("<PARAM NAME=\"type\" VALUE=\"application/x-java-applet;jpi-version=1.3.1\">");
				out.println("<PARAM NAME=\"type\" VALUE=\"application/x-java-applet;version=1.2\">");
				//out.println("<PARAM NAME=\"scriptable\" VALUE=\"false\">");
			sendReadinessParameters(clusterId, psc, out);
			out.println("<COMMENT>");
				//out.println("<EMBED type=\"application/x-java-applet;jpi-version=1.3.1\"");  
				out.println("<EMBED type=\"application/x-java-applet;version=1.2.2\"" + " java_CODE=\"NChartApplet.class\"" + " java_ARCHIVE=\"/big.jar\"");
			  //out.println("ARCHIVE=\"/big.jar\""); 
			  //out.println("CODEBASE=\".\"");
			  //out.println("CODE=\"NChartApplet.class\""); 
			  out.println("NAME=\"NChartApplet\""); 
			  out.println("WIDTH = 600"); 
			  out.println("HEIGHT = 600"); 
      sendnetscapeReadinessParameters(clusterId, psc, out);
			//out.println("pluginspage=\"http://java.sun.com/products/plugin/1.3.1/plugin-install.html\"><NOEMBED>");
			  out.println(" pluginspage=\"http://java.sun.com/products/plugin/1.2/plugin-install.html\"><NOEMBED>");
		  	out.println("</NOEMBED>");
			  out.println("</EMBED>");
			  out.println("</COMMENT>");
			  out.println("</OBJECT>");
			//out.println("<CENTER><APPLET ARCHIVE='/rasschart.jar' CODEBASE='./bin' CODE='org.cougaar.domain.mlm.plugin.assessor.ReadinessAssessorApplet.class' WIDTH=600 HEIGHT=450>");
      //out.println("<CENTER><APPLET ARCHIVE='/big.jar'  CODE='NChartApplet.class' WIDTH=600 HEIGHT=450>");
//			out.println("<PARAM NAME=ReadinessURL VALUE='" + pspURL + "'>");
			//sendReadinessParameters(out);
			//out.println("</APPLET>");

			//out.println("<BR><IMG SRC='images/CougaarLogo.gif' ALIGN=ABSMIDDLE> <I><B>Powered by Cougaar</B></I>");
			out.println("</CENTER></BODY></HTML>");

		}

		// Catch all exceptions and send the stack trace of them as part of the HTTP response
		catch (Throwable e)
		{
			// Send the stack trace to the standard error output
			e.printStackTrace();

			// Send the stack trace to the HTTP response stream
			out.print("<HTML><BODY><H1><FONT COLOR=RED>Unexpected Exception!</FONT></H1><P><PRE>");
			e.printStackTrace(out);
			out.print("</PRE></BODY></HTML>");
		}

		out.flush();

	}

	/*********************************************************************************************************************
  <b>Description</b>: Creates applet parameters which represent the initial inventory list and counts.

  <br><b>Notes</b>:<br>
	                  -

  <br>
  @param out HTTP response socket stream
  @param pspState Current state of the PSP including HTTP request parameters
	*********************************************************************************************************************/
	private void sendReadinessParameters(String cId, PlanServiceContext psc, PrintStream out)
	{
    Iterator itSet, itDOs;
    int ii, jj, kk;

		// Get the readiness assessment

    //HashMap allClusterData = ReadinessAssessorPSPPlugIn.pspData;
    Hashtable allClusterData = ReadinessAssessorPSPPlugIn.pspData;

    synchronized (allClusterData)
    {

//      System.out.println ("PSP_ReadinessAssessor: getting cluster data with key " + cId);

      HashMap sendData = (HashMap) allClusterData.get (cId);
      if (sendData == null)
      {
        sendData = getRollUpData(psc);
        if (sendData == null)
        {
          out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.NUMMAINTAINED + " VALUE='0'>");
          return;
        }
      }
      
      Set maintainedItems = sendData.keySet();

      out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.NUMMAINTAINED + " VALUE='" + maintainedItems.size() + "'>");

      for (itSet = maintainedItems.iterator(), ii=1; itSet.hasNext(); ii ++ )
      {
//        MaintainedItem maint = (MaintainedItem) itSet.next();
        String maint = (String) itSet.next();

//        System.out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.MAITAINEDCLASSNAME + ii + " VALUE='" + maint.getItemIdentification() + "'>");
//        System.out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.MAITAINEDCLASSNAME + ii + " VALUE='" + maint.getTypeIdentification() + "'>");
//        System.out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.MAITAINEDCLASSNAME + ii + " VALUE='" + maint.getNomenclature() + "'>");

        out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.MAITAINEDCLASSNAME + ii + " VALUE='" + maint + "'>");
        HashMap itemBuckets = (HashMap) sendData.get(maint);

        Set directObjects = itemBuckets.keySet();
        out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.NUMDIRECTOBJS + ii + " VALUE='" + directObjects.size() + "'>");

        for (itDOs = directObjects.iterator(), jj=1; itDOs.hasNext(); jj ++)
        {

//          Asset ast = (Asset) itDOs.next();
          String ast = (String) itDOs.next();

          out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.ASSETNAMEPARAM + ii + "_" + jj + " VALUE='" + ast + "'>");

          ArrayList arList = (ArrayList) itemBuckets.get(ast);

          out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.NUMASPECTITEMS + ii + "_" + jj + " VALUE='" + arList.size() + "'>");

          for (kk = 0; kk < arList.size(); kk ++)
          {

            AspectValue[] avs = (AspectValue[]) arList.get(kk);

            // right now we just pass the readiness value and the end time
            out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.READINESSPARAM + ii + "_" + jj + "_" + (kk + 1) + " VALUE='" + avs[2].getValue() + "'>" );
            out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.ATDATEPARAM + ii + "_" + jj + "_" + (kk + 1) + " VALUE='" +  toYYYYMMDD (avs[1].longValue()) + "'>" );
//            out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.ATDATEPARAM + ii + "_" + jj + "_" + (kk + 1) + " VALUE='" +  (avs[1].longValue() / 1000) + "'>" );

          }

        }

      }

          // Create the parameter for the number of book titles in the inventory
//		      out.println("<PARAM NAME=columns VALUE='" + overviewList.size() + "'>");
    }

	}

  /*********************************************************************************************************************
  <b>Description</b>: Creates applet parameters which represent the initial inventory list and counts.

  <br><b>Notes</b>:<br>
	                  -

  <br>
  @param out HTTP response socket stream
  @param pspState Current state of the PSP including HTTP request parameters
	*********************************************************************************************************************/
	private void sendnetscapeReadinessParameters(String cId, PlanServiceContext psc, PrintStream out)
	{
    Iterator itSet, itDOs;
    int ii, jj, kk;

		// Get the readiness assessment

    //HashMap allClusterData = ReadinessAssessorPSPPlugIn.pspData;
    Hashtable allClusterData = ReadinessAssessorPSPPlugIn.pspData;

    synchronized (allClusterData)
    {

//      System.out.println ("PSP_ReadinessAssessor: getting cluster data with key " + cId);

      HashMap sendData = (HashMap) allClusterData.get (cId);
      if (sendData == null)
      {
        sendData = getRollUpData(psc);
        if (sendData == null)
        {
          out.println (ReadinessAssessorPspUtil.NUMMAINTAINED + "=0");
          return;
        }
      }

      Set maintainedItems = sendData.keySet();

      out.println (ReadinessAssessorPspUtil.NUMMAINTAINED + "=" + maintainedItems.size());

      for (itSet = maintainedItems.iterator(), ii=1; itSet.hasNext(); ii ++ )
      {
//        MaintainedItem maint = (MaintainedItem) itSet.next();
        String maint = (String) itSet.next();

//        System.out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.MAITAINEDCLASSNAME + ii + " VALUE='" + maint.getItemIdentification() + "'>");
//        System.out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.MAITAINEDCLASSNAME + ii + " VALUE='" + maint.getTypeIdentification() + "'>");
//        System.out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.MAITAINEDCLASSNAME + ii + " VALUE='" + maint.getNomenclature() + "'>");

        out.println (ReadinessAssessorPspUtil.MAITAINEDCLASSNAME + ii + "=" + maint);
        HashMap itemBuckets = (HashMap) sendData.get(maint);

        Set directObjects = itemBuckets.keySet();
        out.println (ReadinessAssessorPspUtil.NUMDIRECTOBJS + ii + "=" + directObjects.size());

        for (itDOs = directObjects.iterator(), jj=1; itDOs.hasNext(); jj ++)
        {

//          Asset ast = (Asset) itDOs.next();
          String ast = (String) itDOs.next();

          out.println (ReadinessAssessorPspUtil.ASSETNAMEPARAM + ii + "_" + jj + "=" + ast);

          ArrayList arList = (ArrayList) itemBuckets.get(ast);

          out.println (ReadinessAssessorPspUtil.NUMASPECTITEMS + ii + "_" + jj + "=" + arList.size());

          for (kk = 0; kk < arList.size(); kk ++)
          {

            AspectValue[] avs = (AspectValue[]) arList.get(kk);

            // right now we just pass the readiness value and the end time
            out.println (ReadinessAssessorPspUtil.READINESSPARAM + ii + "_" + jj + "_" + (kk + 1) + "=" + avs[2].getValue());
            out.println (ReadinessAssessorPspUtil.ATDATEPARAM + ii + "_" + jj + "_" + (kk + 1) + "=" +  toYYYYMMDD (avs[1].longValue()));
//            out.println ("<PARAM NAME=" + ReadinessAssessorPspUtil.ATDATEPARAM + ii + "_" + jj + "_" + (kk + 1) + " VALUE='" +  (avs[1].longValue() / 1000) + "'>" );

          }

        }

      }

          // Create the parameter for the number of book titles in the inventory
//		      out.println("<PARAM NAME=columns VALUE='" + overviewList.size() + "'>");
    }

	}
  private long toYYYYMMDD (long millisecs)
  {

    long yyyymmdd = millisecs;

    Date d = new Date (millisecs);
    GregorianCalendar c = new GregorianCalendar();
    c.setTime(d);

    yyyymmdd = c.get(GregorianCalendar.DAY_OF_MONTH);
    yyyymmdd += (c.get(GregorianCalendar.MONTH) + 1) * 100;
    yyyymmdd += c.get(GregorianCalendar.YEAR) * 10000;

    return yyyymmdd;
  }



  private final UnaryPredicate readinessExpansionPred = new UnaryPredicate()
  {
     public boolean execute(Object o)
     {
       if (o instanceof Expansion)
       {
         Expansion e = (Expansion) o;
         if (e.getReportedResult() != null)
         {
           Task t = e.getTask();
           if (t.getVerb().equals(Constants.Verb.AssessReadiness))
           {
             if (!t.getPrepositionalPhrases().hasMoreElements())
             {
               return true;
             }
           }
	       }
	     }
	     return false;
	   }
  };

  private HashMap getRollUpData (PlanServiceContext psc)
  {

    System.out.println ("Entering getRollUpData");
    
    Collection stuffToDo = psc.getServerPluginSupport().queryForSubscriber(readinessExpansionPred);
    Iterator itr = stuffToDo.iterator();

    HashMap retMap = null;
    System.out.println ("Query returned: " + stuffToDo.size() + " Expansion objects");
    
    if (! itr.hasNext())
      return null;
    else
      retMap = new HashMap();


    HashMap assetMap = new HashMap();

    retMap.put(new String ("Overall"), assetMap);

    ArrayList arl = new ArrayList();

    assetMap.put("All Assets", arl);

    // there should be only one
    Expansion exp = (Expansion) itr.next();

    AllocationResult ar = exp.getReportedResult();

    List phasedResults = ar.getPhasedAspectValueResults();

    for ( int ii=0; ii < phasedResults.size(); ii ++)
    {
      AspectValue[] avs = (AspectValue[]) phasedResults.get(ii);
      arl.add(avs);
    }

    return retMap;
    
  }
  
	/*********************************************************************************************************************
  <b>Description</b>: Required by the UISubscriber interface.

  <br><b>Notes</b>:<br>
	                  -

  <br>
  @param subscription Subscription object
	*********************************************************************************************************************/
	public void subscriptionChanged(Subscription subscription)
	{
	}

	/*********************************************************************************************************************
  <b>Description</b>: Required by the PlanServiceProvider interface.

  <br><b>Notes</b>:<br>
	                  - Always returns false

  <br>
  @return True if PSP returns XML, false otherwise
	*********************************************************************************************************************/
	public boolean returnsXML()
	{
		return(false);
	}
	
	/*********************************************************************************************************************
  <b>Description</b>: Required by the PlanServiceProvider interface.

  <br><b>Notes</b>:<br>
	                  - Always returns true

  <br>
  @return True if PSP returns HTML, false otherwise
	*********************************************************************************************************************/
	public boolean returnsHTML()
	{
		return(true);
	}

	/*********************************************************************************************************************
  <b>Description</b>: Required by the PlanServiceProvider interface.

  <br><b>Notes</b>:<br>
	                  - Always returns null

  <br>
  @return DTD String
	*********************************************************************************************************************/
	public String getDTD()
	{
		return(null);
	}
	
	/*********************************************************************************************************************
  <b>Description</b>: Required by the PlanServiceProvider interface.

  <br><b>Notes</b>:<br>
	                  - Always returns false

  <br>
  @return True if interested, false otherwise
	*********************************************************************************************************************/
	public boolean test(HttpInput query_parameters, PlanServiceContext sc)
	{
		super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
		return(false);  // This PSP is only accessed by direct reference.
	}
}