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
 
package org.cougaar.mlm.ui.planviewer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

import org.cougaar.util.OptionPane;

/**
 * Creates connection between client and XML Plan Server.
 */

public class ConnectionHelper {
  private URL url;
  private URLConnection connection;
  static final String DebugPSP_package = "alpine/demo";
  static final String DebugPSP_id = "DEBUG.PSP";
  String clusterURL;
  boolean isDebugPSP = false;

  /**
    * If you create a ConnectionHelper instance with 0 params.  must
    * call setConnection()... to intialize connection
    **/
    public ConnectionHelper() {
    }

  /**
    Connects to the debug PSP at the specified cluster,
    where cluster is specified by URL (host and port).
    */

  public ConnectionHelper(String clusterURL) throws MalformedURLException, IOException {
    this(clusterURL, DebugPSP_package, DebugPSP_id);
    isDebugPSP = true;
  }


  public ConnectionHelper(String clusterURL, String path) throws MalformedURLException, IOException {
    this.clusterURL = clusterURL;
    url = new URL(clusterURL + path);
    connection = url.openConnection();
    connection.setDoInput(true);
    connection.setDoOutput(true);
  }

  /**
    Connects to the specified PSP at the specified cluster,
    where cluster is specified by URL (host and port).
    */

  public ConnectionHelper(String clusterURL, String PSP_package, String PSP_id) throws MalformedURLException, IOException {
    this(clusterURL, PSP_package+"/"+PSP_id);
  }

  public void setConnection(String completeURLString) throws Exception {
      if( connection != null ) {
          new RuntimeException("ConnectionHelper.connection already set!");
      }
      clusterURL = completeURLString; // messy -- but should be okay for now
      url = new URL(completeURLString);
      connection = url.openConnection();
      connection.setDoInput(true);
      connection.setDoOutput(true);
  }

  /**
   * getURL - returns URL for the connection
   *
   * @return URL for the connection
   */
  public URL getURL() {
    return url;
  }

  /**
    Sends data on the connection.
   */

  public void sendData(String s) throws IOException {
    ((HttpURLConnection) connection).setRequestMethod("PUT");
    PrintWriter pw = new PrintWriter(connection.getOutputStream());
    pw.println(s);
    pw.flush();
  }

  /**
    Returns input stream for the connection.
    */

  public InputStream getInputStream() throws IOException {
    return connection.getInputStream();
  }

    public URLConnection getConnection() {
        return connection;
    }

  /**
    Sends data and returns response in buffer.
    */

  public byte[] getResponse() throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    InputStream is = getInputStream();
    byte b[] = new byte[512];
    int len;
    while( (len = is.read(b,0,512)) > -1 )
      os.write(b,0,len);
    return os.toByteArray();
  }

  /**
    Returns a list of cluster identifiers from the debug PSP
    located at the cluster specified in this class's constructor.
    */

  public Hashtable getClusterIdsAndURLs() throws MalformedURLException, ClassNotFoundException, IOException {
    ConnectionHelper debugConnection;
    Hashtable results = new Hashtable();
    Hashtable results2 = new Hashtable();

    try {
      /*
get host:port from clusterURL
open url connection to host:port/agents?all&text
get input stream from connection
wrap in BufferedReader
read lines of response into temporary ArrayLIst (get agent-names)
fill "results" with (agent-name, http://host:port/$ + agent-name + /)
return results

       */
      
      int p = clusterURL.lastIndexOf(":");
      URL url = new URL(clusterURL + "agents?scope=all&format=text");
      //System.out.println(url.toString());
      URLConnection urlconn = null; 
      InputStream in;
      try {
	urlconn = url.openConnection();
	in = urlconn.getInputStream();
	BufferedReader input = new BufferedReader(new InputStreamReader(in));
	
	try { 
	  while(input !=null) {
	    String n = input.readLine();
	    if( n==null )
	      break;
	    String u = ((clusterURL.substring(0,p+1)) + "8800/$" + n + "/");
	    //System.out.println(u);
	    results.put(n, u);
	  }
	} catch(IOException e) {
	  System.out.println("Error reading agent list: " + e.getMessage());
	}
      } catch(IOException e) {
	System.out.println("Error reaching agents list: " + e.getMessage());
      }
      
      
      
// we don't want this code:
      /*
      if (!isDebugPSP) {
        int i = clusterURL.lastIndexOf(":");
        if (i == -1)
          throw new MalformedURLException("Expected host:port");
        debugConnection = new ConnectionHelper(clusterURL.substring(0,i+1) + "5555/");
      } else {
        debugConnection = this;
      }
      debugConnection.sendData("LIST_CLUSTERS");
      ObjectInputStream ois = 
        new ObjectInputStream(debugConnection.getInputStream());
      while (true) {
        String nextName = (String)ois.readObject();
        String nextURL = (String)ois.readObject();
        results.put(nextName, nextURL);
      }
//
      
    } catch (java.io.EOFException e) {
      // ignore this one
    }
      */
    } catch (Exception e) {
      // ignore
    }

    return results;
  }
  
  

  /**
   * getClusterInfo - get the location of the cluster
   * Only used when running in an application
   */
  public static String getClusterHostPort(java.awt.Component parent) {
      return getClusterHostPort(parent, "Enter location of a cluster.");
  }

  public static String getClusterHostPort(java.awt.Component parent, Object msg) {
      return (String)
          OptionPane.showInputDialog(parent,
                                     msg,
                                     "Cluster Location",
                                     OptionPane.QUESTION_MESSAGE,
                                     //null, null, "localhost:5555");
                                     null, null, "localhost:8800");

  }

  public static Hashtable getClusterInfo(java.awt.Component parent) {
      return getClusterInfo(parent, "Enter location of a cluster.");
  }

  public static Hashtable getClusterInfo(java.awt.Component parent, Object msg) {
    String host = getClusterHostPort(parent, msg);
    if (host == null) {
      return null;
    }
    try {
      host = host.trim();
      if (host.length() == 0) {
        //host = "localhost:5555";
        host = "localhost:8800";
      } else {
        if (host.indexOf(':') < 0) {
          //host += ":5555";
          host += ":8800";
        }
      }
      ConnectionHelper connection = new ConnectionHelper("http://" + host + "/");
      return connection.getClusterIdsAndURLs();
    } catch (Exception e) {
      return null;
    }
  }
}

