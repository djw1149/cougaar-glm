/*
 * <copyright>
 *  Copyright 1997-2002 BBNT Solutions, LLC
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

package org.cougaar.lib.plugin;

import org.cougaar.util.ConfigFinder;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import java.net.URL;

import java.io.*;
import org.cougaar.util.log.*;

/**
 * <pre>
 * EntityResolver for use with all UTIL xml parsers.
 *
 * Wraps config file finder and uses it to resolve (find) xml entities.
 *
 * Need to provide an entity resolver to the parser.
 * The entity resolver is just a wrapper of the configFinder.
 * The resolver is expected to resolve a systemID, where:
 *
 * </pre>
 */
public class UTILEntityResolver implements EntityResolver {

  protected Logger logger;
  public UTILEntityResolver (Logger logger) { this.logger = logger; }

  /** 
   * Set org.cougaar.lib.plugin.UTILEntityResolver.debug to true to see debug info. <p>
   * Will say what file is getting read in...
   **/
  public InputSource resolveEntity (String publicId, String systemId)	{
    URL url = null;

    try {
      url = new URL(systemId);
    } catch(Exception e) {}

    String filename = url.getFile();

    filename = filename.replace('\\', '/');
    filename = filename.substring(filename.lastIndexOf("/")+1, filename.length());

    // return a special input source
    try {
      InputSource is = new InputSource(ConfigFinder.getInstance().open(filename));
      if (logger.isDebugEnabled())
	logger.debug ("UTILEntityResolver.resolveEntity - publicID " + 
		      publicId + " system ID " + 
		      systemId + " filename from systemID " + 
		      filename + " input source " + is);

      return is;
    } catch (Exception e) {
      logger.error ("UTILLdmXMLPlugin.getParsedDocument - Could not find on config path", e);
      return null;
    }
  }
}
