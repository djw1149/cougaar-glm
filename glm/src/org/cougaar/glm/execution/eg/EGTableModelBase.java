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
package org.cougaar.glm.execution.eg;

import javax.swing.table.AbstractTableModel;
import javax.swing.JLabel;

/**
 * Augment the standard TableModel interface to include column layout info.
 **/
public abstract class EGTableModelBase extends AbstractTableModel {
  public static int SEND_WIDTH;
  public static int DATE_WIDTH;
  public static int STEP_WIDTH;
  public static int ITEM_WIDTH;
  public static int CONSUMER_WIDTH;
  public static int QUANTITY_WIDTH;
  public static int CLUSTER_WIDTH;

  static {
    SEND_WIDTH = getCellWidth("Send");
    DATE_WIDTH = getCellWidth("12/12/2000 23:59 (MMM)");
    STEP_WIDTH = getCellWidth("MONTHLY");
    CONSUMER_WIDTH = getCellWidth("M1A1 120 MM Tank Combat Full TrackXXX");
    ITEM_WIDTH = getCellWidth("NSN/XXXXXXXXXXXXX");
    QUANTITY_WIDTH = getNumericCellWidth("10,000,000.000");
    CLUSTER_WIDTH = getCellWidth("XXXXXXXXXXXXXXX");
    setWidths();
  }

  public static int getCellWidth(String s) {
    JLabel label = new JLabel(s);
    label.setFont(ReportManagerGUI.getFirstFont());
    return label.getPreferredSize().width;
  }

  public static int getNumericCellWidth(String s) {
    JLabel label = new JLabel(s);
    label.setFont(ReportManagerGUI.getFirstNumberFont());
    return label.getPreferredSize().width;
  }

  public static void setWidths() {
  }

  /**
   * Default implementation of compareRowsByColumn compares class
   * names if the objects have different classes. Otherwise uses
   * compareTo if both are Comparable. Otherwise, compares the
   * toString values of the objects.
   **/
  public int compareRowsByColumn(int row1, int row2, int column) {
    Object o1 = getValueAt(row1, column);
    Object o2 = getValueAt(row2, column);
    if (o1 == null) {
      if (o2 == null) return 0;
      return -1;
    } else if (o2 == null) {
      return 1;
    }
    if (o1.getClass() != o2.getClass()) {
      return o1.getClass().getName().compareTo(o2.getClass().getName());
    }
    if (o1 instanceof Comparable) {
      return ((Comparable) o1).compareTo(o2);
    }
    return o1.toString().compareTo(o2.toString());
  }

  public String getToolTipText(int row, int col) {
    return null;
  }
  public boolean cellIsMarked(int row, int col) {
    return false;
  }
  public void cellSelectionChanged(int row, int col) {
  }

  public boolean cellIsFirst(int row, int col) {
    if (row == 0) return true;
    Object thisValue = getValueAt(row, col);
    if (thisValue == null) return true;
    Object prevValue = getValueAt(row-1, col);
    if (prevValue == null) return true;
    return !thisValue.equals(prevValue);
  }
}
