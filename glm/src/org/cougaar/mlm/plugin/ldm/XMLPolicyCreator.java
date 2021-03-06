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

package org.cougaar.mlm.plugin.ldm;

import java.util.Vector;

import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.policy.BooleanRuleParameter;
import org.cougaar.planning.ldm.policy.ClassRuleParameter;
import org.cougaar.planning.ldm.policy.DoubleRuleParameter;
import org.cougaar.planning.ldm.policy.EnumerationRuleParameter;
import org.cougaar.planning.ldm.policy.IntegerRuleParameter;
import org.cougaar.planning.ldm.policy.KeyRuleParameter;
import org.cougaar.planning.ldm.policy.KeyRuleParameterEntry;
import org.cougaar.planning.ldm.policy.LongRuleParameter;
import org.cougaar.planning.ldm.policy.Policy;
import org.cougaar.planning.ldm.policy.RangeRuleParameter;
import org.cougaar.planning.ldm.policy.RangeRuleParameterEntry;
import org.cougaar.planning.ldm.policy.RuleParameter;
import org.cougaar.planning.ldm.policy.RuleParameterIllegalValueException;
import org.cougaar.planning.ldm.policy.StringRuleParameter;
import org.cougaar.util.ConfigFinder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/** XMLPolicyCreator - creates policies from xml file
 *
 **/

public class XMLPolicyCreator {

	
  private String xmlfilename;
  private Document doc;
  private PlanningFactory ldmf = null;
  private ConfigFinder configFinder;

  public XMLPolicyCreator(String xmlfilename, ConfigFinder configFinder) {
    this.xmlfilename = xmlfilename;
    this.configFinder = configFinder;
  }

  public XMLPolicyCreator(String xmlfilename, ConfigFinder configFinder, 
                          PlanningFactory ldmf ) {
    this(xmlfilename, configFinder);
    this.ldmf = ldmf;
  }

  /*
  public XMLPolicyCreator(PlanningFactory ldmf) {
    this.ldmf = ldmf;
  }
  */

  public void setPlanningFactory(PlanningFactory ldmf) {
    this.ldmf = ldmf;
  }

  public Policy[] getPolicies() {
    try {
      doc = configFinder.parseXMLConfigFile(xmlfilename);
      if (doc == null) {
	System.err.println("XML Parser could not handle file " + xmlfilename);
	return null;
      }
      return parseDoc(doc);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  
  

  public Policy[] parseDoc(Document doc) {
    Element root = doc.getDocumentElement();
    Vector policyVector = new Vector();
    Policy[] pols = null;
    if( root.getNodeName().equals( "Policies" )){
      NodeList nlist = root.getChildNodes();
      int nlength = nlist.getLength();
      //System.out.println("There are " + nlength + " Child Nodes");
      for (int i=0; i<nlength; i++) {
	Node policyNode = nlist.item(i);
	if (policyNode.getNodeType() == Node.ELEMENT_NODE) {
	  Policy p = getPolicy(policyNode);
	  if (p != null){
	    policyVector.addElement(p);
	  }
	}
      }
      pols = new Policy[policyVector.size()];
      for (int i=0; i<policyVector.size(); i++) {
	pols[i] = (Policy)policyVector.elementAt(i);
      }
    }
    return pols;
  }

  protected Policy createPolicy(String policyType) {
    Policy p = null;

    if (ldmf != null) {
      p = ldmf.newPolicy(policyType);
    } else {
      try {
	Class c = Class.forName(policyType);
	Object o = c.newInstance();
	p = (Policy) o;
      }	catch(Exception e) {
	System.err.println("Couldn't instantiate policy type " 
			   + policyType + e);
	System.err.println("Using default class org.cougaar.planning.ldm.policy.Policy");
      }
    }
    if (p == null)
      p = new Policy(policyType);

    return p;
    }

  public Policy getPolicy(Node policyNode) {
    Policy p = null;

    if( policyNode.getNodeName().equals( "Policy" )){

      String policyName = policyNode.getAttributes().getNamedItem("name").getNodeValue();
      String policyType  = policyNode.getAttributes().getNamedItem("type").getNodeValue();
      //System.out.println("Creating new policy " + policyName);

      p = createPolicy(policyType);
      p.setName(policyName);

      NodeList nlist = policyNode.getChildNodes();
      int nlength = nlist.getLength();
      //System.out.println("There are " + nlength + " Child Nodes");
      for (int i=0; i<nlength; i++) {
	Node ruleParamNode = nlist.item(i);
	if (ruleParamNode.getNodeType() == Node.ELEMENT_NODE) {
	  if (ruleParamNode.getNodeName().equals("RuleParam")) {
	    RuleParameter rp = parseRuleParamNode((Element) ruleParamNode);
	    if (rp != null)
	      p.Add(rp);
	  }
	  else 
            System.out.println(ruleParamNode.getNodeName());
	}
      }
    }
    return p;
  }

  protected RuleParameter parseRuleParamNode(Element ruleParamNode) {
    RuleParameter rp = null;
    String paramName = ruleParamNode.getAttributes().getNamedItem("name").getNodeValue();
    NodeList nl = ruleParamNode.getChildNodes();
    Node child = null;
    for (int i=0; i<nl.getLength(); i++) {
      child = nl.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE)
	break;
    }
    if (child.getNodeType() != Node.ELEMENT_NODE)
      return null;

    try {
      String nodeType = child.getNodeName();
      //System.out.println("ParamName " + paramName + " paramType " + nodeType);
      
      if (nodeType.equals("Integer")) {
        String stringval = child.getAttributes().getNamedItem("value").getNodeValue();
        Integer val = Integer.valueOf(stringval);
        stringval = child.getAttributes().getNamedItem("min").getNodeValue();
        int min= Integer.valueOf(stringval).intValue();
        stringval = child.getAttributes().getNamedItem("max").getNodeValue();
        int max = Integer.valueOf(stringval).intValue();
        IntegerRuleParameter irp = 
          new IntegerRuleParameter(paramName, min, max);
        //System.out.println("new IntegerRuleParameter(" + paramName 
        //+ ", " + min  +", " + max + ")" );
        
        try {
          irp.setValue(val);
        } catch (RuleParameterIllegalValueException ve) {
          System.err.println(ve);
        }
        rp = irp;
        
      } else if (nodeType.equals("Double")) {
        String stringval = child.getAttributes().getNamedItem("value").getNodeValue();
        Double val = Double.valueOf(stringval);
        stringval = child.getAttributes().getNamedItem("min").getNodeValue();
        double min= Double.valueOf(stringval).doubleValue();
        stringval = child.getAttributes().getNamedItem("max").getNodeValue();
        double max = Double.valueOf(stringval).doubleValue();
        DoubleRuleParameter drp = 
          new DoubleRuleParameter(paramName, min, max);
        //    System.out.println("new DoubleRuleParameter(" + paramName 
        // 			 + ", " + min  +", " + max + ")" );
        
        try {
          drp.setValue(val);
        } catch (RuleParameterIllegalValueException ve) {
          System.err.println(ve);
        }
        
        rp = drp;
      } else if (nodeType.equals("Long")) {
        String stringval = child.getAttributes().getNamedItem("value").getNodeValue();
        Long val = Long.valueOf(stringval);
        stringval = child.getAttributes().getNamedItem("min").getNodeValue();
        long min= Long.valueOf(stringval).longValue();
        stringval = child.getAttributes().getNamedItem("max").getNodeValue();
        long max = Long.valueOf(stringval).longValue();
        LongRuleParameter lrp = 
          new LongRuleParameter(paramName, min, max);
        //    System.out.println("new LongRuleParameter(" + paramName 
        // 			 + ", " + min  +", " + max + ")" );
        
        try {
          lrp.setValue(val);
        } catch (RuleParameterIllegalValueException ve) {
          System.err.println(ve);
        }
        
        rp = lrp;
      } else if (nodeType.equals("String")) {
        String stringval = child.getAttributes().getNamedItem("value").getNodeValue();
        
        StringRuleParameter srp 
          = new StringRuleParameter(paramName);
        //       System.out.println("new StringRuleParameter(" + paramName + ")" +
        // 			 "  value=" + stringval);
        
        try {
          srp.setValue(stringval);
        } catch (RuleParameterIllegalValueException ve) {
          System.err.println(ve);
        }
        
        rp = srp;
      } else if (nodeType.equals("Class")) {
        String interfaceType = child.getAttributes().getNamedItem("interface_type").getNodeValue();
        String classType = child.getAttributes().getNamedItem("class_type").getNodeValue();
        try {
          Class c = Class.forName(interfaceType);
          ClassRuleParameter crp = 
            new ClassRuleParameter(paramName, c);
          
          c = Class.forName(classType);
          crp.setValue(c);
          rp = crp;
        } catch (Exception e) {
          System.err.println("Couldn't create class " + interfaceType + e);
        } 
      } else if (nodeType.equals("Boolean")) {
        String boolvalue = child.getAttributes().getNamedItem("value").getNodeValue();
        boolvalue = boolvalue.trim();
        Boolean b=null;
        if (boolvalue.compareToIgnoreCase("true") == 0)
          b = new Boolean(true);
        else if (boolvalue.compareToIgnoreCase("false") == 0)
          b = new Boolean(false);
        
        BooleanRuleParameter brp =  new BooleanRuleParameter(paramName);
        if (b !=null){
          try {
            brp.setValue(b);
          } catch (RuleParameterIllegalValueException e) {
            System.err.println("Couldn't set value for boolean rule parameter "
                               + paramName);
            System.err.println(e);
          }
        }
        
        rp = brp;
        
      } else if (nodeType.equals("Enumeration")) {
        String stringval = child.getAttributes().getNamedItem("value").getNodeValue();
        
        // Read the children, stuff them in an array
        NodeList nlist = child.getChildNodes();
        int nlength = nlist.getLength();
        Vector enumOptVector = new Vector();
        for (int i=0; i<nlength; i++) {
          Node enumOptionNode = nlist.item(i);
          if (enumOptionNode.getNodeType() != Node.ELEMENT_NODE)
            continue;
          enumOptVector.addElement( enumOptionNode.getAttributes().getNamedItem("value").getNodeValue());
        }
        
        String [] enumOptions = new String[enumOptVector.size()];
        for (int i=0; i<enumOptVector.size(); i++)
          enumOptions[i] = (String) enumOptVector.elementAt(i);
	
        EnumerationRuleParameter erp = 
          new EnumerationRuleParameter(paramName, enumOptions);
        //       System.out.println("new EnumerationRuleParameter(" + paramName 
        // 			 + enumOptions +")" );
        
        try {
          erp.setValue(stringval);
        } catch (RuleParameterIllegalValueException ve) {
          System.err.println(ve);
        }
        
        rp = erp;
        
      } else if (nodeType.equals("KeySet")) {
	String default_value = 
          child.getAttributes().getNamedItem("value").getNodeValue();
	
	// Read the children, stuff them in an array
	NodeList nlist = child.getChildNodes();
	int nlength = nlist.getLength();
	Vector keyVector = new Vector();
	for(int i = 0; i < nlength; i++) {
          Node keyNode = nlist.item(i);
          if (keyNode.getNodeType() != Node.ELEMENT_NODE)
            continue;
          String key = keyNode.getAttributes().getNamedItem("key").getNodeValue();
          String value = keyNode.getAttributes().getNamedItem("value").getNodeValue();
          keyVector.addElement(new KeyRuleParameterEntry(key, value));
	}
	KeyRuleParameterEntry []keys = 
          new KeyRuleParameterEntry[keyVector.size()];
	for(int i = 0; i < keys.length; i++) 
          keys[i] = (KeyRuleParameterEntry)keyVector.elementAt(i);
        
	KeyRuleParameter krp = new KeyRuleParameter(paramName, keys);
	try {
          krp.setValue(default_value);
	} catch (RuleParameterIllegalValueException ve) {
          System.err.println(ve);
	}
        
	rp = krp;
      } else if (nodeType.equals("RangeSet")) {
	String default_value = 
          child.getAttributes().getNamedItem("value").getNodeValue();
	
	// Read the children, stuff them in an array
	NodeList nlist = child.getChildNodes();
	int nlength = nlist.getLength();
	Vector rangeVector = new Vector();
	for(int i = 0; i < nlength; i++) {
          Node rangeNode = nlist.item(i);
          if (rangeNode.getNodeType() != Node.ELEMENT_NODE)
            continue;
          int min = Integer.valueOf(rangeNode.getAttributes().getNamedItem
                                    ("min").getNodeValue()).intValue();
          int max = Integer.valueOf(rangeNode.getAttributes().getNamedItem
                                    ("max").getNodeValue()).intValue();
          NodeList valList = rangeNode.getChildNodes();
          Object value = null;
          for (int j = 0; j < valList.getLength(); j++) {
            Node valNode = valList.item(j);
            String valType = valNode.getNodeName();
            switch (valNode.getNodeType()) {
            case Node.ELEMENT_NODE:
              if (valType.equals("String")) {
                value = valNode.getAttributes().getNamedItem("value").getNodeValue();
              } else {
                value = parseRuleParamNode((Element) valNode);
              }
              break;
            default:
              //System.out.println("Node type: " + valNode.getNodeType());
            }
          }
          if (value == null) {
            System.err.println("XMLPolicyCreator: unable to parse range entry value for " +
                               paramName + ". Min = " + min + " , max = " + max);
          } else {
            rangeVector.addElement(new RangeRuleParameterEntry(value, min, max));
          }
        } 
	RangeRuleParameterEntry []ranges = 
          new RangeRuleParameterEntry[rangeVector.size()];
	for(int i = 0; i < ranges.length; i++) 
          ranges[i] = (RangeRuleParameterEntry)rangeVector.elementAt(i);
        
	RangeRuleParameter rrp = new RangeRuleParameter(paramName, ranges);
	try {
          rrp.setValue(default_value);
	} catch (RuleParameterIllegalValueException ve) {
          System.err.println(ve);
	}
        
	rp = rrp;
      }
      
    } catch (NumberFormatException nfe) {
      System.err.println("Unable to parse xml for " + paramName + 
                         " RuleParameter.");
      nfe.printStackTrace();

      rp = null;
    }

    return rp;
  }

  public static void main (String[] args) {
    XMLPolicyCreator xmlpc = new XMLPolicyCreator( args[0], new ConfigFinder());
    Policy policies[] = xmlpc.getPolicies();
    if (policies != null) {
      System.out.println("There are " + policies.length + " policies");
      System.out.println(policies);
    }
    else
      System.out.println("Couldn't parse file");
  }
}
