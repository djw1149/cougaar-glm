package org.cougaar.domain.mlm.ui.newtpfdd.transit;
import java.util.*;
import java.io.Serializable;

import org.cougaar.domain.mlm.ui.newtpfdd.transit.TagChronicle.TagTransitData;
import org.cougaar.domain.mlm.ui.newtpfdd.transit.TagChronicle.TagTally;

/**
 * Holds a Chronicle of data about the location of a Asset Classes
 *
 * @author Benjamin Lubin; last modified by $Author: wseitz $
 * @version $Revision: 1.2 $; Last modified on $Date: 2001-02-23 01:02:20 $
 * @since 11/15/00
 */
public class AssetClassChronicle extends TagChronicle{

  //Variables:
  ////////////

  public static String[] assetClassNames = {"Unknown",
					    "Subsistence",
					    "Clothing & Equipment",
					    "POL",
					    "Construction Material",
					    "Ammunition",
					    "Personal Demand Item",
					    "Major End Item",
					    "Medical",
					    "Repair Part",
					    "Non-Military Item",
					    "Person",
					    "Container",
					    "Package"};

  protected static Set assetClassSet;

  //Static Initializer:
  /////////////////////
  static {
    assetClassSet = new HashSet(assetClassNames.length);
    for(int i=0;i<assetClassNames.length;i++){
      assetClassSet.add(assetClassNames[i]);
    }
    assetClassSet = Collections.unmodifiableSet(assetClassSet);
  }

  //Constructors:
  ///////////////

  public AssetClassChronicle(){
    super();
    tags=null;
  }

  public AssetClassChronicle(int binSize){
    super(binSize);
    tags=null;
  }
  
  //Functions:
  ////////////

  public static int getAssetClassNumber(String assetClass){
    for(int i=0;i<assetClassNames.length;i++)
      if(assetClass.equals(assetClassNames[i]))
	return i;
    return 0; //unknown
  }

  public Set getTagSet(){
    return assetClassSet;
  }

  /**
   * This function returns a new Tallier object that records the low-level
   * data.
   **/
  protected Tallier getNewTallier(){
    return new AssetClassTally();
  }

  //Inner Classes:
  ////////////////

  /** Incoming data for each asset class transport task.**/
  public static class AssetClassTransitData extends TagTransitData{
    protected int assetClass;

    public AssetClassTransitData(Position start,
				 Position end,
				 long startDate,
				 long endDate,
				 int assetClass,
				 int count){
      super(start,end,startDate,endDate,null,count);
      this.assetClass=assetClass;
    }

    public void setTag(String tag){assetClass=getAssetClassNumber(tag);}
    public String getTag(){return assetClassNames[assetClass];}
    
    public int getAssetClass(){return assetClass;}

    /**
     * Used to clone a copy of a TransitData, but set new values for its
     * positions and dates
     **/
    public TransitData cloneNewDatePos(Position startP,
				       Position endP,
				       long startD,
				       long endD){
      return new AssetClassTransitData(startP,endP,startD,endD,
				       assetClass,count);
    }
  }

  /**
   * Actual data for a time-loc bin.  In this case an array of integers, one
   * per assetClass.
   **/
  public class AssetClassTally implements TagTallier{

    protected int[] assetClassCounts=new int[assetClassNames.length];
    
    public AssetClassTally(){
      for(int i=0;i<assetClassCounts.length;i++)
	assetClassCounts[i]=0;
    }

    public Map getTagToCountMap(){
      HashMap map = new HashMap(assetClassCounts.length);
      for(int i=0;i<assetClassCounts.length;i++)
	if(assetClassCounts[i]>0)
	  map.put(assetClassNames[i],new Integer(assetClassCounts[i]));
      return map;
    }

    /** Return the count for a given asset class**/
    public int getCount(String assetClass){
      int ac = getAssetClassNumber(assetClass);
      return assetClassCounts[ac];
    }

    /** 
     * Called for each piece of transit data that is recieved 
     **/
    public boolean increment(TransitData td){
      AssetClassTransitData actd = (AssetClassTransitData)td;
      return updateAssetClass(actd.getAssetClass(),actd.getCount());
    }

    /** 
     * Called for each piece of transit data that is recieved 
     **/
    public boolean decrement(TransitData td){
      AssetClassTransitData actd = (AssetClassTransitData)td;
      return updateAssetClass(actd.getAssetClass(),-actd.getCount());
    }

    protected boolean updateAssetClass(int assetClass, int amount){
      assetClassCounts[assetClass]+=amount;
      if(assetClassCounts[assetClass] < 0){
	logMessage(MIDDLING,"Unexpected negative count in '"
			     + assetClass + "': " + amount);
	  return false;
      }
      return true;
    }

    /** This should return a deep copy of this instance **/
    public Tallier deepClone(){
      AssetClassTally ret=new AssetClassTally();
      for(int i=0;i<assetClassCounts.length;i++)
	ret.assetClassCounts[i]=assetClassCounts[i];
      return ret;
    }

    public String toString(){
      StringBuffer sb = new StringBuffer();
      sb.append("(");
      for(int i=0;i<assetClassCounts.length;i++){
	sb.append(i);
	sb.append(" = ");
	sb.append(assetClassCounts[i]);
	sb.append(",");
      }
      sb.append(")");
      return sb.toString();
    }
  }
}
