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


package org.cougaar.mlm.debug.ui;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FileDialog;
import java.io.File;

import org.cougaar.core.mts.MessageAddress;

/** Displays information in a tree.
  This supports dynamically expanded nodes (expanded when the
  user clicks on a tree node to display more information) and dynamically
  updated nodes (updated when listeners on the underlying information
  are notified of changes).
  */

public class UITreeDisplay implements Runnable, ActionListener {
  private UIPlugin uiPlugin;
  private String planName;
  private MessageAddress clusterId;
  private String command;
  private UITreeNode root;
  private JFrame uiFrame;
  private DefaultTreeModel treeModel;
  private JTree tree;

  /** Display the specified data in a tree.
    All the work is done in the run method so
    that the main user interface thread which creates this,
    isn't waiting to fetch the information needed for the tree.
    @param uiPlugin this user interface plugin
    @param planName the name of the plan for which to display information
    @param clusterId the cluster for which to display information
    @param command UIDisplay.PLAN_COMMAND, PLAN_DETAILS_COMMAND, TASKS_COMMAND, 
    WORKFLOWS_COMMAND, CLUSTER_ASSETS_COMMAND, ALL_ASSETS_COMMAND
   */

  public UITreeDisplay(UIPlugin uiPlugin, String planName, 
		       MessageAddress clusterId, String command) {
    this.uiPlugin = uiPlugin;
    this.planName = planName;
    this.clusterId = clusterId;
    this.command = command;
  }

  /** Create the root node of the tree and the window to display it in.
    Fill in the tree here so that delays in accessing information
    from clusters don't affect the main user interface thread.
    */

  public void run() {
    String title = "";

    try {
      if (command.equals(UIDisplay.PLAN_COMMAND)) {
	root = new UIPlanNode(uiPlugin, planName, clusterId);
	title = "LogPlan in " + clusterId.getAddress();
      }
      else if (command.equals(UIDisplay.PLAN_DETAILS_COMMAND)) {
	root = new UIPlanDetailsNode(uiPlugin, planName, clusterId);
	title = "LogPlan Details in " + clusterId.getAddress();
      }
      else if (command.equals(UIDisplay.TASKS_COMMAND)) {
	root = new UITaskCollectionNode(uiPlugin, planName, clusterId);
	title = "Expandable Tasks in " + 
	  clusterId.getAddress();
      }
      else if (command.equals(UIDisplay.WORKFLOWS_COMMAND)) {
	root = new UIWorkflowCollectionNode(uiPlugin, planName, clusterId);
	title = "Allocatable Workflows in " + 
	  clusterId.getAddress();
      } else if (command.equals(UIDisplay.CLUSTER_ASSETS_COMMAND)) {
	root = new UIClusterAssetsNode(uiPlugin, planName, clusterId);
	title = "Cluster Assets in " +
	  clusterId.getAddress();
      } else if (command.equals(UIDisplay.ALL_ASSETS_COMMAND)) {
	root = new UIAllAssetsNode(uiPlugin, planName, clusterId);
	title = "All Assets in " +
	  clusterId.getAddress();
      }
      else {
	new UIDisplayError("This request is not supported.");
	return;
      }
    } catch (UINoPlanException e) {
      new UIDisplayError(e.getMessage());
      return; // nothing to display
    }
    treeModel = new DefaultTreeModel(root);
    root.setTreeModel(treeModel);
    tree = new JTree(treeModel);

    // for doing custom tree cell rendering, such as using color
    //    tree.setCellRenderer(new UITreeCellRenderer());
    // create frame with update function if not displaying local cluster
    // and with save (to file) function
    // don't provide expand button for log plan details, as expansion
    // is circular due to cluster object class definitions
    if (command.equals(UIDisplay.PLAN_DETAILS_COMMAND)) 
      uiFrame = new UIFrame(title, tree, this, false, true, false);
    else
      uiFrame = new UIFrame(title, tree, this, false, true, true);
  }

  /** Called when:
    the user invokes the save function; saves the tree to a file.
    the user invokes the update function; fetches new data and reloads the tree.
    the user invokes the expand action; expands the tree
    @param e the action event for the save menu
    */

  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    synchronized (root) {
    if (command.equals(UIFrame.SAVE_COMMAND)) {
      FileDialog fileDialog = new FileDialog(uiFrame, "Save", FileDialog.SAVE);
      fileDialog.show();
      String dirname = fileDialog.getDirectory();
      String filename = fileDialog.getFile();
      if ((dirname != null) && (filename != null))
	root.saveTree(dirname + filename);
    }
    else if (command.equals(UIFrame.UPDATE_COMMAND)) {
      root.invalidateChildren(); // children cache is out of date, reload them
      treeModel.reload(root); // reload this root
    }
    else if (command.equals(UIFrame.EXPAND_COMMAND)) {
      expandTree(root);
    } else
      System.out.println("Received unknown action event: " + command);
    }
  }


  private void expandTree(TreeNode node) {
    if (node.isLeaf())
      return;
    int n = node.getChildCount();
    for (int i = 0; i < n; i ++) {
      TreeNode childNode = node.getChildAt(i);
      TreePath path = new TreePath(treeModel.getPathToRoot(childNode));
      tree.expandPath(path);
      tree.fireTreeExpanded(path);
      expandTree(childNode);
    }
  }
}
 
