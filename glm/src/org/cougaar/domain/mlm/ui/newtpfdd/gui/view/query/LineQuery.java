package org.cougaar.domain.mlm.ui.newtpfdd.gui.view.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.QueryData;

public class LineQuery extends SqlQuery {
    
    private List unitIDList = null;
    private List carrierPrototypeIDList = null;
    private List cargoPrototypeIDList = null;
    private List carrierInstanceIDList = null;
    private List cargoInstanceIDList = null;
    private boolean fullLines = true;

  public LineQuery (QueryData oldQuery) {
	setUnitIDList             (toList(oldQuery.getUnitNames ()));
	setCarrierPrototypeIDList (toList(oldQuery.getCarrierTypes ()));
	setCargoPrototypeIDList   (toList(oldQuery.getCargoTypes ()));
	setCarrierInstanceIDList  (toList(oldQuery.getCarrierTypes ()));
	setCargoInstanceIDList    (toList(oldQuery.getCargoTypes ()));
  }
  
  protected List toList (String [] strings) {
	List ret = new ArrayList ();
	for (int i = 0; i < strings.length; i++)
	  ret.add (strings[i]);
	return ret;
  }
  
    public List getUnitIDList() { return unitIDList; }
    public void setUnitIDList(List unitIDList) { this.unitIDList = unitIDList; }

    public List getCarrierPrototypeIDList() { return carrierPrototypeIDList; };
    public void setCarrierPrototypeIDList(List carrierPrototypeIDList) { this.carrierPrototypeIDList = carrierPrototypeIDList; };

    public List getCargoPrototypeIDList() { return cargoPrototypeIDList; };
    public void setCargoPrototypeIDList(List cargoPrototypeIDList) { this.cargoPrototypeIDList = cargoPrototypeIDList; };

    public List getCarrierInstanceIDList() { return carrierInstanceIDList; };
    public void setCarrierInstanceIDList(List carrierInstanceIDList) { this.carrierInstanceIDList = carrierInstanceIDList; };

    public List getCargoInstanceIDList() { return cargoInstanceIDList; };
    public void setCargoInstanceIDList(List cargoInstanceIDList) { this.cargoInstanceIDList = cargoInstanceIDList; };

    public boolean getFullLines() { return fullLines; }
    public void setFullLines(boolean fullLines) { this.fullLines = fullLines; }

  /**
   * some sql here, probably built using a string buffer. ... 
   * don't forget to handle dates and doubles in a DB safe way and to use ' quotes.  
   * See /tops/src/org/cougaar/domain/mlm/ui/grabber/config/DBConfig 
   * for examples of functions for doing oracle/my sql syntax)
   */	
  protected String getSqlQuery () {
	StringBuffer sb = new StringBuffer ();
	
	sb.append ("");
	return sb.toString ();
  }
  
  protected void handleResult (ResultSet rs, QueryResponse response) {
	try {
	while(rs.next()){
	  //        process a row as you see fit.  use rs.getXXXX()
	}
	} catch (SQLException e) {
	}
  }
}