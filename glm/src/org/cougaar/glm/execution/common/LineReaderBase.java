/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
package org.cougaar.glm.execution.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.cougaar.core.util.UID;

/**
 * An implementation of the LineReader interface as an abstract base
 * class. the readLine method is abstract.
 **/
public abstract class LineReaderBase implements LineReader {
  public abstract String readLine() throws IOException;

  public EGObject readEGObject() throws IOException {
    try {
      int classIndex = readInt();
      EGObject o = (EGObject) EGObject.egObjectClasses[classIndex].newInstance();
      o.read(this);
      return o;
    } catch (IOException ioe) {
      throw ioe;
    } catch (Exception e) {
      e.printStackTrace();
      throw new IOException(e.toString());
    }
  }

  public String readUTF() throws IOException {
    return readLine();
  }

  public UID readUID() throws IOException {
    String uidOwner = readUTF();
    long uidId = readLong();
    return new UID(uidOwner, uidId);
  }

  public double readDouble() throws IOException {
    try {
      return Double.parseDouble(readLine());
    } catch (NumberFormatException nfe) {
      throw new RuntimeException(nfe.toString());
    }
  }

  public float readFloat() throws IOException {
    try {
      return Float.parseFloat(readLine());
    } catch (NumberFormatException nfe) {
      throw new RuntimeException(nfe.toString());
    }
  }

  public byte readByte() throws IOException {
    try {
      return Byte.parseByte(readLine());
    } catch (NumberFormatException nfe) {
      throw new RuntimeException(nfe.toString());
    }
  }

  public short readShort() throws IOException {
    try {
      return Short.parseShort(readLine());
    } catch (NumberFormatException nfe) {
      throw new RuntimeException(nfe.toString());
    }
  }

  public int readInt() throws IOException {
    try {
      return Integer.parseInt(readLine());
    } catch (NumberFormatException nfe) {
      throw new RuntimeException(nfe.toString());
    }
  }

  public long readLong() throws IOException {
    try {
      return Long.parseLong(readLine());
    } catch (NumberFormatException nfe) {
      throw new RuntimeException(nfe.toString());
    }
  }

  public boolean readBoolean() throws IOException {
    return readLine().equals("true");
  }

  public Object readObject() throws IOException {
    try {
      int nb = readInt();
      byte[] bytes = new byte[nb];
      int pb = 0;
      char[] line = new char[80];
      while (pb < nb) {
        String s = readLine();
        int nc = s.length();
        s.getChars(0, nc, line, 0);
        int pc = 0;
        while (pc < nc) {
          int threeBytes = ((base64Decoding[line[pc  ]] << 18) |
                            (base64Decoding[line[pc+1]] << 12) |
                            (base64Decoding[line[pc+2]] <<  6) |
                            (base64Decoding[line[pc+3]]      ));
          pc += 4;
          bytes[pb++] = (byte) (threeBytes >> 16);
          bytes[pb++] = (byte) (threeBytes >>  8);
          bytes[pb++] = (byte) (threeBytes      );
        }
      }
//        LineWriterBase.writeByteArray(bytes);
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      ObjectInputStream is = new ObjectInputStream(bais);
      return is.readObject();
    } catch (IOException ioe) {
      throw ioe;
    } catch (Exception e) {
      throw new IOException(e.getMessage());
    }
  }

  public static byte[] base64Decoding = new byte[128];

  static {
    for (byte i = 0; i < 64; i++) {
      char c = LineWriterBase.base64Encoding[i];
      base64Decoding[c] = i;
    }
  }
}
