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
package org.cougaar.glm.execution.common;

import java.io.IOException;

/**
 * Report the Observable Aspects of a Task.
 * In particular: START_TIME, END_TIME, & QUANTITY
 **/
public class TaskEventReport extends Report implements java.io.Serializable {
  public static class Rescind extends TaskEventReport {
    public Rescind() {}
    public Rescind(TaskEventId aTaskEventId) {
      theTaskEventId = aTaskEventId;
    }
    public boolean isRescind() { return true; }
  }
  public boolean isRescind() { return false; }

  public TaskEventId theTaskEventId;
  public String theVerb = "";
  public double theAspectValue;
  public String theFullDescription = "";
  public String theShortDescription = "";

  public TaskEventReport(TaskEventId aTaskEventId,
                         String aVerb,
                         double anAspectValue,
                         long aReportDate,
                         long aReceivedDate,
                         String aFullDescription,
                         String aShortDescription)
  {
    super(aReportDate, aReceivedDate);
    theTaskEventId = aTaskEventId;
    theVerb = aVerb;
    theAspectValue = anAspectValue;
    theFullDescription = aFullDescription;
    theShortDescription = aShortDescription;
  }

  TaskEventReport() {}

  public TaskEventReport(TaskEventReport original) {
    this(original.theTaskEventId,
         original.theVerb,
         original.theAspectValue,
         original.theReportDate,
         original.theReceivedDate,
         original.theFullDescription,
         original.theShortDescription);
  }

  public void write(LineWriter writer) throws IOException {
    super.write(writer);
    writer.writeUID(theTaskEventId.theTaskUID);
    writer.writeInt(theTaskEventId.theAspectType);
    writer.writeUTF(theVerb);
    writer.writeDouble(theAspectValue);
    writer.writeUTF(theFullDescription);
    writer.writeUTF(theShortDescription);
  }

  public void read(LineReader reader) throws IOException {
    super.read(reader);
    theTaskEventId = new TaskEventId(reader.readUID(), reader.readInt());
    theVerb = reader.readUTF();
    theAspectValue = reader.readDouble();
    theFullDescription = reader.readUTF();
    theShortDescription = reader.readUTF();
  }
}
