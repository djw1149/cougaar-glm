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

package org.cougaar.lib.xml.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Hashtable;

import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.util.log.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses fields of objects.
 *
 * (This is quite involved and complicated, actually.)
 */
public class FieldParser{

  public FieldParser (Logger l, ObjectParser objectParser) { 
    logger = l; 
    valueParser = new ValueParser(l, objectParser);
  }

  /**
   * set a field given by node in an object obj
   */
  public Object setField(LDMServesPlugin ldm, Node node, Object obj){
    if(node.getNodeName().equals("field")){
      Class  objClass   = obj.getClass(); 
      String fieldName  = node.getAttributes().getNamedItem("name").getNodeValue();
      String fieldType  = node.getAttributes().getNamedItem("type").getNodeValue();
      String setterName = null;

      // Get the field setter method
      Method fieldSetter = null;
      Class  fieldClass  = null;
      Class  origFieldClass = null; // for reporting errors only
      Object fieldValue  = null;

      // check for correct field type
      if(fieldType.equals("object")){
        fieldType = getObjectType(node);
      }
      else if(fieldType.equals("String")){
        fieldType = "java.lang." + fieldType;
      }

      // assuming normal bean-like setters
      if(fieldSetter == null){
        try {
          setterName     = "set" + fieldName;
          fieldClass     = getFieldClass(fieldType);
          origFieldClass = fieldClass;
          //fieldSetter  = objClass.getMethod(setterName, fieldClass);
          fieldSetter = getSetterMethod(objClass, setterName, fieldClass);

          if (fieldSetter != null) {
            if(fieldName.equals("TypeIdentificationPG")){
              fieldValue = getTypeIdentificationPG(ldm, node, (Asset)obj);
            } else {
              fieldValue = 
                ((javaUtilCollectionClass.isAssignableFrom(fieldClass)) ?
                 getFieldCollection(ldm, node, fieldClass) :
                 getFieldValue(ldm, node));
              callMethod(obj, fieldSetter, fieldValue, fieldType);
            }
          }
        }
        catch (Exception e) {
          // BOZO, need to do something here.
          fieldSetter = null;
          
          //logger.error(e.getMessage());
          //e.printStackTrace();
          //System.exit(1);
        }
      }

      // assume that it is an org.cougaar.planning.ldm.asset and set using addOtherPropertyGroup()
      if(fieldSetter == null){ 
        try{
          setterName    = "addOtherPropertyGroup";
          fieldClass    = getFieldClass("org.cougaar.planning.ldm.asset.PropertyGroup");
          //fieldSetter = objClass.getMethod(setterName, fieldClass);
          fieldSetter   = getSetterMethod(objClass, setterName, fieldClass);

	  if (fieldSetter != null) {
            fieldValue = 
              ((javaUtilCollectionClass.isAssignableFrom(fieldClass)) ?
               getFieldCollection(ldm, node, fieldClass) :
               getFieldValue(ldm, node));
            callMethod(obj, fieldSetter, fieldValue, fieldType);
	  }
        }
        catch(Exception e){
          // BOZO, need to do something here.
          fieldSetter = null;
          
          //logger.error(e.getMessage());
          //e.printStackTrace();
          //System.exit(1);
        }
      }

      // assume that we are setting with a new measure class
      if(fieldSetter == null){ 
        try{
          objClass      = (Class)obj;  // for this very special case
          setterName    = "new" + fieldName;
          fieldClass    = getFieldClass(fieldType);
          //fieldSetter = objClass.getMethod(setterName, fieldClass);
          fieldSetter   = getSetterMethod(objClass, setterName, fieldClass);

	  if (fieldSetter != null) {
            fieldValue = 
              ((javaUtilCollectionClass.isAssignableFrom(fieldClass)) ?
               getFieldCollection(ldm, node, fieldClass) :
               getFieldValue(ldm, node));
            obj = callMethod(null, fieldSetter, fieldValue, fieldType);
	  }
        }
        catch(Exception e){
          // BOZO, need to do something here.
          //logger.error(e.getMessage());
          //e.printStackTrace();
          fieldSetter = null;
          
          //logger.error(e.getMessage());
          //e.printStackTrace();
          //System.exit(1);
        }
      }
      
      // we are hosed
      if(fieldSetter == null){
        logger.error("");
        logger.error("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        logger.error("XXXX  Method not found: set" + fieldName + "(" + origFieldClass.getName() + ")");
        logger.error("XXXX: In object: " + obj);
        logger.error("XXXX: Please revise your ldm.xml file");
	logger.error("XXXX: NOTE: If you are suddenly having unexplained problems with ");
	logger.error("XXXX:   this code, please contact gvidaver@bbn.com.");
	//	logger.error("XXXX:   method oldGetSetterMethod to the name getSetterMethod in");
	//	logger.error("XXXX:   xmlparser.FieldParser");
        logger.error("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        logger.error("");
	// System.exit(1);
      }
    }
    return obj;
  }

  private static Class javaUtilCollectionClass;
  static {
    try {
      javaUtilCollectionClass = Class.forName("java.util.Collection");
    } catch (Exception e) { }
  }

  static final Class PG_CLASS = org.cougaar.planning.ldm.asset.PropertyGroup.class;
  /**
   * <pre>
   * Wrapper arround the java.lang.Class functionality to allow to 
   * get a method on an object that is called with a superclass of its
   * argument.  Assumes only one argument per method (good for setters).
   * 
   * NOTE: It is actually faster to do the algorithm implemented below rather
   * than call class.getMethods() and look for a method with the appropriate
   * (isAssignableFrom(argtype)) argument.
   * For several societies tested 100% of the arguments of the methods desired 
   * were either equal to their arguments or were propertygroups.  It is still
   * an open question whether it is faster to do the compare (endsWith("PG"))
   * before looking for the method or whether one should just let the 
   * NoSuchMethodException be thrown before trying the PG approach.
   * </pre>
   */
  private Method getSetterMethod(Class objc, String name, Class argtype){
    Method method  = null;
    Class[] argclasses = { argtype };
    // For strict accuracy we should also add the argtype to the 
    // hash_str, but since most classes have only one method with
    // the given type of arg, we can just fail in the exceptional 
    // case and pick it up later.
    String classname = objc.getName();

    // Preprocessing for speed!
    if (classname.equals("java.lang.Class"))
      return null;
    if (argtype.getName().endsWith("PG"))
      argtype = PG_CLASS;

    String hash_str = classname.concat(name); // + argtype.getName();

    // This 3-level nesting is somewhat arbitrary but gets most
    // methods (statistically)
    // That is, if we haven't seen this method for this class before,
    // try to find it within the class.  If that fails, try the arg's
    // super, then that super's super(class), then revert to the old
    // slow way of doing things
    if ((method = (Method)seen_classmethods.get(hash_str)) == null) {
      try {
	method = objc.getMethod(name, argclasses);
      } catch (NoSuchMethodException nsme) {

  	argclasses[0] = argclasses[0].getSuperclass();
  	try {
  	  if (argclasses[0] != null)
  	    method = objc.getMethod(name, argclasses);
  	  else 
  	    method = oldGetSetterMethod(objc, name, argtype);
  	} catch (NoSuchMethodException nsme2) {
  	  argclasses[0] = argclasses[0].getSuperclass();
  	  try {
  	    if (argclasses[0] != null)
  	      method = objc.getMethod(name, argclasses);
  	    else 
  	      method = oldGetSetterMethod(objc, name, argtype);
  	  } catch (NoSuchMethodException nsme3) {
  	    method = oldGetSetterMethod(objc, name, argtype);
  	  }
  	}
        }
      if (method == null)
	return null; 

      seen_classmethods.put(hash_str, method);
    }

    return method;
  }

  /**
   * Wrapper arround the java.lang.Class functionality to allow to 
   * get a method on an object that is called with a superclass of its
   * argument.  Assumes only one argument per method (good for setters).
   */
//    private static Method oldGetSetterMethod(Class objc, String name, Class argtype){
//        return getSetterMethod(objc, name, argtype);
//    }
  Hashtable seen_classes = new Hashtable();
  /**
   * Wrapper arround the java.lang.Class functionality to allow to 
   * get a method on an object that is called with a superclass of its
   * argument.  Assumes only one argument per method (good for setters).
   */
  private Method oldGetSetterMethod(Class objc, String name, Class argtype){
    Method[] methods  = null;
    Class[]  argclass = new Class[1];

    argclass[0] = argtype;
    if ((methods = (Method[])seen_classes.get(objc)) == null) {
      methods = objc.getMethods();
      seen_classes.put(objc, methods);
    }

    for(int i = 0; i < methods.length; i++){
      if(name.equals(methods[i].getName())){
        Class[] params = methods[i].getParameterTypes();
        //logger.debug(":XXX:: " + methods[i] + " (" + params[0] + ")");
        if(params[0].isAssignableFrom(argtype))
          return methods[i];
        
      }
    }
    return null;
  }

  /**
   * Calls a method inside of an object with the given
   * parameter of the given type.
   */
  private Object callMethod(Object object,
			    Method fieldSetter,
			    Object fieldValue,
			    String fieldType)
    throws IllegalAccessException, InvocationTargetException
  {
    
    Object[] buffer = new Object[1];
    Object retval = null;

    if(fieldType.equals("byte")){
      buffer[0] = Byte.valueOf((String)fieldValue);
      retval = fieldSetter.invoke(object, buffer);
    }
    else if(fieldType.equals("char")){
      buffer[0] = new Character(((String)fieldValue).charAt(0));
      retval = fieldSetter.invoke(object, buffer);
    }
    else if(fieldType.equals("boolean")){
      buffer[0] = Boolean.valueOf((String)fieldValue);
      retval = fieldSetter.invoke(object, buffer);
    }
    else if(fieldType.equals("short")){
      buffer[0] = Short.valueOf((String)fieldValue);
      retval = fieldSetter.invoke(object, buffer);
    }
    else if(fieldType.equals("int")){
      buffer[0] = Integer.valueOf((String)fieldValue);
      retval = fieldSetter.invoke(object, buffer);
    }
    else if(fieldType.equals("long")){
      buffer[0] = Long.valueOf((String)fieldValue);
      retval = fieldSetter.invoke(object, buffer);
    }
    else if(fieldType.equals("float")){
      buffer[0] = Float.valueOf((String)fieldValue);
      retval = fieldSetter.invoke(object, buffer);
    }
    else if(fieldType.equals("double")){
      buffer[0] = Double.valueOf((String)fieldValue);
      retval = fieldSetter.invoke(object, buffer);
    }
    else{
      buffer[0] = fieldValue;
      retval = fieldSetter.invoke(object, buffer);
    }
    return retval;
  }

  /**
   * This is a "loook ahead" function to get the object type
   * for a field that contains an object.
   */
  private String getObjectType(Node node){
    String    retval   = null;
    NodeList  nlist    = node.getChildNodes();      
    int       nlength  = nlist.getLength();
    
    for(int i = 0; i < nlength; i++){
      Node    child       = nlist.item(i);
      String  childname   = child.getNodeName();
      
      if(child.getNodeType() == Node.ELEMENT_NODE){
        if(childname.equals("value")){
          
          NodeList nnlist   = child.getChildNodes();
          
          for(int ii = 0; ii < nlength; ii++){
            Node    cchild       = nnlist.item(ii);
            String  cchildname   = cchild.getNodeName();
            
            if(cchild.getNodeType() == Node.ELEMENT_NODE){
              if(cchildname.equals("object")){
                
                retval = cchild.getAttributes().getNamedItem("class").getNodeValue();
              }
            }
          }
        }
      }
    }
    return retval;
  }

  /**
   * Get the value of a field given by Node.  Java primitives
   * are returned as strings, it is the callers responsibility
   * to do the conversion.
   */
  private Object getFieldValue(LDMServesPlugin ldm, Node node){
    Object     retval   = null;
    NodeList   nlist    = node.getChildNodes();      
    int        nlength  = nlist.getLength();
    
    for(int i = 0; i < nlength; i++){
      Node    child       = nlist.item(i);
      String  childname   = child.getNodeName();
      
      if(child.getNodeType() == Node.ELEMENT_NODE){
        if(childname.equals("value")){
          retval = valueParser.getValue(ldm, child);
        }
      }
    }
    return retval;
  }

  /**
   * Get the Collection of fields given by Node.  Java primitives
   * are returned as strings, it is the callers responsibility
   * to do the conversion.
   */
  private Collection getFieldCollection(
      LDMServesPlugin ldm, Node node, Class collectionClass){
    Collection retcoll;
    NodeList   nlist    = node.getChildNodes();      
    int        nlength  = nlist.getLength();
    
    try {
      retcoll = (Collection)collectionClass.newInstance();
    } catch (Exception noInstanceE) {
      logger.error("Unable to create instance of Collection Class "+collectionClass, 
		   noInstanceE);
      return null;
    }

    for(int i = 0; i < nlength; i++){
      Node    child       = nlist.item(i);
      String  childname   = child.getNodeName();
      
      if(child.getNodeType() == Node.ELEMENT_NODE){
        if(childname.equals("value")){
          Object collObj = valueParser.getValue(ldm, child);
          if(collObj != null) {
            retcoll.add(collObj);
          }
        }
      }
    }
    return retcoll;
  }

  private TypeIdentificationPG getTypeIdentificationPG(LDMServesPlugin ldm, 
                                                              Node node, Asset obj){
    NewTypeIdentificationPG ntip = (NewTypeIdentificationPG)obj.getTypeIdentificationPG();
    TypeIdentificationPG    tip  = null;

    NodeList  nlist    = node.getChildNodes();      
    int       nlength  = nlist.getLength();
    
    for(int i = 0; i < nlength; i++){
      Node    child       = nlist.item(i);
      String  childname   = child.getNodeName();
      
      if(child.getNodeType() == Node.ELEMENT_NODE){
        if(childname.equals("value")){
          tip = (TypeIdentificationPG)valueParser.getValue(ldm, child);
          // OK, this is not the most general way, I could dynamicallly
          // get all the methods that TypeIdentification has dynamically
          // and then call the setters, but hell...
          ntip.setTypeIdentification(tip.getTypeIdentification());
          ntip.setNomenclature(tip.getNomenclature());
          ntip.setAlternateTypeIdentification(tip.getAlternateTypeIdentification());
          tip = null;
        }
      }
    }
    return ntip;
  }

  /**
   * Get the Class of a field taking into account that it 
   * could be a Java primitive.
   */
  private Class getFieldClass(String fieldType){
    Class retval = null;

    if(fieldType.equals("byte")){
      retval = Byte.TYPE;
    }
    else if(fieldType.equals("char")){
      retval = Character.TYPE;
    }
    else if(fieldType.equals("boolean")){
      retval = Boolean.TYPE;
    }
    else if(fieldType.equals("short")){
      retval = Short.TYPE;
    }
    else if(fieldType.equals("int")){
      retval = Integer.TYPE;
    }
    else if(fieldType.equals("long")){
      retval = Long.TYPE;
    }
    else if(fieldType.equals("float")){
      retval = Float.TYPE;
    }
    else if(fieldType.equals("double")){
      retval = Double.TYPE;
    }
    else{
      try{
        retval = Class.forName(fieldType);
      }
      catch(ClassNotFoundException e){
        logger.error("XXXXXXX: could not find class: " + fieldType, e);
      }
    }
    return retval;
  }

  protected Logger logger;

  protected Hashtable seen_classmethods = new Hashtable();
  protected ValueParser valueParser;
}

