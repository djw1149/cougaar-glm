/*
 * <copyright>
 *  
 *  Copyright 1997-2012 Raytheon BBN Technologies
 *  under partial sponsorship of the Defense Advanced Research Projects
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

/* @generated Wed Jun 06 08:28:58 EDT 2012 from alpprops.def - DO NOT HAND EDIT */
/** Implementation of OffRoadTransportationPG.
 *  @see OffRoadTransportationPG
 *  @see NewOffRoadTransportationPG
 **/

package org.cougaar.glm.ldm.asset;

import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;
import java.util.*;

import  org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.oplan.*;
import org.cougaar.glm.ldm.policy.*;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.execution.common.InventoryReport;


import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;

public class OffRoadTransportationPGImpl extends java.beans.SimpleBeanInfo
  implements NewOffRoadTransportationPG, Cloneable
{
  public OffRoadTransportationPGImpl() {
  }

  // Slots

  private long theContainerCount;
  public long getContainerCount(){ return theContainerCount; }
  public void setContainerCount(long container_count) {
    theContainerCount=container_count;
  }
  private Volume theNonContainerCapacity;
  public Volume getNonContainerCapacity(){ return theNonContainerCapacity; }
  public void setNonContainerCapacity(Volume non_container_capacity) {
    theNonContainerCapacity=non_container_capacity;
  }
  private Volume theWaterCapacity;
  public Volume getWaterCapacity(){ return theWaterCapacity; }
  public void setWaterCapacity(Volume water_capacity) {
    theWaterCapacity=water_capacity;
  }
  private Volume thePetroleumCapacity;
  public Volume getPetroleumCapacity(){ return thePetroleumCapacity; }
  public void setPetroleumCapacity(Volume petroleum_capacity) {
    thePetroleumCapacity=petroleum_capacity;
  }
  private Mass theAmmunitionCapacity;
  public Mass getAmmunitionCapacity(){ return theAmmunitionCapacity; }
  public void setAmmunitionCapacity(Mass ammunition_capacity) {
    theAmmunitionCapacity=ammunition_capacity;
  }
  private long thePassengerCapacity;
  public long getPassengerCapacity(){ return thePassengerCapacity; }
  public void setPassengerCapacity(long passenger_capacity) {
    thePassengerCapacity=passenger_capacity;
  }


  public OffRoadTransportationPGImpl(OffRoadTransportationPG original) {
    theContainerCount = original.getContainerCount();
    theNonContainerCapacity = original.getNonContainerCapacity();
    theWaterCapacity = original.getWaterCapacity();
    thePetroleumCapacity = original.getPetroleumCapacity();
    theAmmunitionCapacity = original.getAmmunitionCapacity();
    thePassengerCapacity = original.getPassengerCapacity();
  }

  public boolean equals(Object other) {

    if (!(other instanceof OffRoadTransportationPG)) {
      return false;
    }

    OffRoadTransportationPG otherOffRoadTransportationPG = (OffRoadTransportationPG) other;

    if (!(getContainerCount() == otherOffRoadTransportationPG.getContainerCount())) {
      return false;
    }

    if (getNonContainerCapacity() == null) {
      if (otherOffRoadTransportationPG.getNonContainerCapacity() != null) {
        return false;
      }
    } else if (!(getNonContainerCapacity().equals(otherOffRoadTransportationPG.getNonContainerCapacity()))) {
      return false;
    }

    if (getWaterCapacity() == null) {
      if (otherOffRoadTransportationPG.getWaterCapacity() != null) {
        return false;
      }
    } else if (!(getWaterCapacity().equals(otherOffRoadTransportationPG.getWaterCapacity()))) {
      return false;
    }

    if (getPetroleumCapacity() == null) {
      if (otherOffRoadTransportationPG.getPetroleumCapacity() != null) {
        return false;
      }
    } else if (!(getPetroleumCapacity().equals(otherOffRoadTransportationPG.getPetroleumCapacity()))) {
      return false;
    }

    if (getAmmunitionCapacity() == null) {
      if (otherOffRoadTransportationPG.getAmmunitionCapacity() != null) {
        return false;
      }
    } else if (!(getAmmunitionCapacity().equals(otherOffRoadTransportationPG.getAmmunitionCapacity()))) {
      return false;
    }

    if (!(getPassengerCapacity() == otherOffRoadTransportationPG.getPassengerCapacity())) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends OffRoadTransportationPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(OffRoadTransportationPG original) {
    super(original);
   }
   public Object clone() { return new DQ(this); }
   private transient org.cougaar.planning.ldm.dq.DataQuality _dq = null;
   public boolean hasDataQuality() { return (_dq!=null); }
   public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return _dq; }
   public void setDataQuality(org.cougaar.planning.ldm.dq.DataQuality dq) { _dq=dq; }
   private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    if (out instanceof org.cougaar.core.persist.PersistenceOutputStream) out.writeObject(_dq);
   }
   private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
    if (in instanceof org.cougaar.core.persist.PersistenceInputStream) _dq=(org.cougaar.planning.ldm.dq.DataQuality)in.readObject();
   }
    
    private final static PropertyDescriptor properties[]=new PropertyDescriptor[1];
    static {
      try {
        properties[0]= new PropertyDescriptor("dataQuality", DQ.class, "getDataQuality", null);
      } catch (Exception e) { e.printStackTrace(); }
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
      PropertyDescriptor[] pds = super.properties;
      PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+properties.length];
      System.arraycopy(pds, 0, ps, 0, pds.length);
      System.arraycopy(properties, 0, ps, pds.length, properties.length);
      return ps;
    }
  }


  private transient OffRoadTransportationPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new OffRoadTransportationPGImpl(OffRoadTransportationPGImpl.this);
  }

  public PropertyGroup copy() {
    try {
      return (PropertyGroup) clone();
    } catch (CloneNotSupportedException cnse) { return null;}
  }

  public Class getPrimaryClass() {
    return primaryClass;
  }
  public String getAssetGetMethod() {
    return assetGetter;
  }
  public String getAssetSetMethod() {
    return assetSetter;
  }

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[6];
  static {
    try {
      properties[0]= new PropertyDescriptor("container_count", OffRoadTransportationPG.class, "getContainerCount", null);
      properties[1]= new PropertyDescriptor("non_container_capacity", OffRoadTransportationPG.class, "getNonContainerCapacity", null);
      properties[2]= new PropertyDescriptor("water_capacity", OffRoadTransportationPG.class, "getWaterCapacity", null);
      properties[3]= new PropertyDescriptor("petroleum_capacity", OffRoadTransportationPG.class, "getPetroleumCapacity", null);
      properties[4]= new PropertyDescriptor("ammunition_capacity", OffRoadTransportationPG.class, "getAmmunitionCapacity", null);
      properties[5]= new PropertyDescriptor("passenger_capacity", OffRoadTransportationPG.class, "getPassengerCapacity", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(OffRoadTransportationPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements OffRoadTransportationPG, Cloneable, LockedPG
  {
    private transient Object theKey = null;
    _Locked(Object key) { 
      if (this.theKey == null) this.theKey = key;
    }  

    public _Locked() {}

    public PropertyGroup lock() { return this; }
    public PropertyGroup lock(Object o) { return this; }

    public NewPropertyGroup unlock(Object key) throws IllegalAccessException {
       if( theKey.equals(key) ) {
         return OffRoadTransportationPGImpl.this;
       } else {
         throw new IllegalAccessException("unlock: mismatched internal and provided keys!");
       }
    }

    public PropertyGroup copy() {
      try {
        return (PropertyGroup) clone();
      } catch (CloneNotSupportedException cnse) { return null;}
    }


    public Object clone() throws CloneNotSupportedException {
      return new OffRoadTransportationPGImpl(OffRoadTransportationPGImpl.this);
    }

    public boolean equals(Object object) { return OffRoadTransportationPGImpl.this.equals(object); }
    public long getContainerCount() { return OffRoadTransportationPGImpl.this.getContainerCount(); }
    public Volume getNonContainerCapacity() { return OffRoadTransportationPGImpl.this.getNonContainerCapacity(); }
    public Volume getWaterCapacity() { return OffRoadTransportationPGImpl.this.getWaterCapacity(); }
    public Volume getPetroleumCapacity() { return OffRoadTransportationPGImpl.this.getPetroleumCapacity(); }
    public Mass getAmmunitionCapacity() { return OffRoadTransportationPGImpl.this.getAmmunitionCapacity(); }
    public long getPassengerCapacity() { return OffRoadTransportationPGImpl.this.getPassengerCapacity(); }
  public final boolean hasDataQuality() { return OffRoadTransportationPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return OffRoadTransportationPGImpl.this.getDataQuality(); }
    public Class getPrimaryClass() {
      return primaryClass;
    }
    public String getAssetGetMethod() {
      return assetGetter;
    }
    public String getAssetSetMethod() {
      return assetSetter;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
      return properties;
    }

    public Class getIntrospectionClass() {
      return OffRoadTransportationPGImpl.class;
    }

  }

}
