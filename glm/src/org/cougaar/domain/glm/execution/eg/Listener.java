/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.execution.eg;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import org.cougaar.domain.glm.execution.common.*;
import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;

/**
 * Connects to and listens to the output of the PSP_EventWatcher.
 * Dispatches the received objects to the appropriate handler.
 **/
public class Listener extends Thread {
  public static final String PSP_package = "alpine/execution";

  private EGReceiver egReceiver;
  private URL url;
  private EGObject parameters;

  protected static Object[] emptyHandlers = new Object[] {};

  public Listener(String listenerName,
                  ClusterInfo clusterInfo,
                  String PSP_id,
                  EGObject parameters,
                  Object[] handlers)
    throws IOException
  {
    super(listenerName);
    try {
      url = new URL(clusterInfo.theClusterURL + PSP_package + "/" + PSP_id);
    } catch (MalformedURLException e) {
      e.printStackTrace();
      throw new IOException(e.toString());
    }
    this.parameters = parameters;
    egReceiver = new EGReceiver(listenerName, clusterInfo.theClusterName);
    addHandlers(handlers);
  }

  private static Object pauseLock = new Object();
  private static boolean busy = false;
  private static long nextOpen = System.currentTimeMillis();

  /**
   * Limit the rate of opening connections. Sleep enough so that a
   * connection cannot be opened sooner that 100 msec after a previous
   * connection is opened.
   **/
  private static void pause() {
    try {
      synchronized (pauseLock) {
        while (busy) {
          pauseLock.wait();
        }
        busy = true;
      }
      long delay = nextOpen - System.currentTimeMillis();
      if (delay > 0) Thread.sleep(delay);
      nextOpen = System.currentTimeMillis() + 100L;
      synchronized (pauseLock) {
        busy = false;
        pauseLock.notify();
      }
    } catch (InterruptedException ie) {
    }
  }
        
  public void run() {
    int nTries = 0;
    while (true) {
      try {
        nTries++;
        pause();
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        OutputStream os = connection.getOutputStream();
        OutputStreamLineWriter writer = new OutputStreamLineWriter(os);
        writer.writeEGObject(parameters);
        writer.flush();
        writer.close();
        LineReader reader = new InputStreamLineReader(connection.getInputStream());
        egReceiver.setReader(reader);
        egReceiver.run();
        return;
      } catch (ConnectException e) {
        if (nTries < 10) continue; // Retry 10 times to connect
        System.err.println(e + ": " + url);
        return;
      } catch (Exception e) {
        System.err.println(e + ": " + url);
        return;
      }
    }
  }

  public void addHandlers(Object[] handlers) {
    for (int i = 0; i < handlers.length; i++) {
      egReceiver.addHandler(handlers[i]);
    }
  }
}
