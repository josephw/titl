/*
 *  titl - Tools for iTunes Libraries
 *  Copyright (C) 2008 Joseph Walton
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kafsemo.titl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A processor that can be configured with callbacks to modify any particular string field.
 * 
 * @author Joseph
 */
public class ProcessLibrary
{
    private final Map<Integer, StringConverter> converters = new HashMap<Integer, StringConverter>();
    
    public void process(File inFile, OutputStream outStr) throws IOException, ItlException
    {
        /* Read the original library in */
        Hdfm hdfm;
        
        InputStream inStr = new FileInputStream(inFile);
        try {
            DataInput di = new DataInputStream(inStr);
            hdfm = Hdfm.read(di, inFile.length());
        } finally {
            inStr.close();
        }

        /* Modify... */
        ByteArrayOutputStream dto = new ByteArrayOutputStream();
        
        process(new DataInputStream(new ByteArrayInputStream(hdfm.fileData)), hdfm.fileData.length, new DataOutputStream(dto));
        
        /* ...and write out */
        DataOutput out = new DataOutputStream(outStr);
        
        hdfm.write(out, dto.toByteArray());
    }

    void process(DataInput di, int totalLength, DataOutput out) throws IOException, ItlException
    {
        int remaining = totalLength;
        
        boolean going = true;
        
        while(going)
        {
            int consumed = 0;
            String type = Util.toString(di.readInt());
            consumed += 4;
            
            int length = di.readInt();
            consumed += 4;

            if(type.equals("hohm")) {
                int recLength = di.readInt();
                consumed += 4;
                
                Integer hohmType = Integer.valueOf(di.readInt());
                consumed += 4;

                byte[] ba = new byte[recLength - consumed];
                di.readFully(ba);

                StringConverter sc = converters.get(hohmType);
                if (sc != null) {
                    ba = mapHohm(new DataInputStream(new ByteArrayInputStream(ba)), sc);
                }
                
                /* Write out again */
                out.writeInt(Util.fromString(type));
                out.writeInt(length);
                
                out.writeInt(ba.length + consumed);
                out.writeInt(hohmType);
                
                out.write(ba);
                
                remaining -= (recLength);
                
            } else {
                byte[] ba = new byte[length - consumed];
                
                di.readFully(ba);
                
                /* Did we hit the end? */
                if(type.equals("hdsm")) {
                    going = !ParseLibrary.readHdsm(new DataInputStream(new ByteArrayInputStream(ba)), ba.length);
                }
                
                remaining -= length;
                
                /* Write out again */
                out.writeInt(Util.fromString(type));
                out.writeInt(ba.length + 8);
                out.write(ba);
            }
        }
        
        byte[] footerBytes = new byte[remaining];
        di.readFully(footerBytes);
        
//        String footer = new String(footerBytes, "iso-8859-1");
//        System.out.println("Footer: " + footer);
        
        out.write(footerBytes);
    }

    static byte[] mapHohm(DataInput di, StringConverter sc) throws IOException, ItlException
    {
        /* Read in */
        byte[] unknown = new byte[12];
        di.readFully(unknown);
        
        int dataLength = di.readInt();
        byte[] alsoUnknown = new byte[8];
        di.readFully(alsoUnknown);
        
        byte[] data = new byte[dataLength];
        di.readFully(data);

        String s = ParseLibrary.toString(data, unknown[11]);

        s = sc.convert(s);

        /* Write out */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutput out = new DataOutputStream(baos);
        
        /* Choose the necessary character encoding */
        Encoding enc = chooseEncoding(s);
        
        unknown[11] = enc.code;
        byte[] newData = s.getBytes(enc.name);
        out.write(unknown);
        out.writeInt(newData.length);
        out.write(alsoUnknown);
        out.write(newData);
        
        return baos.toByteArray();
    }

    static Encoding chooseEncoding(String s)
    {
        for (char c : s.toCharArray()) {
            if (c > 0xff) {
                return Encoding.UTF16BE;
            }
        }
        
        return Encoding.ISO88591;
    }
    
    /**
     * Register a converter for a particular HOHM type.
     * 
     * <ul>
     * <li>0x02 - Track title
     * <li>0x0d - Location
     * </ul>
     */
    public void register(int hohmType, StringConverter cnvtr)
    {
        converters.put(Integer.valueOf(hohmType), cnvtr);
    }
    
    public enum Encoding
    {
        UNSPECIFIED((byte) 0, "iso-8859-1"),
        UTF16BE((byte) 1, "utf-16be"),
        ISO88591((byte) 3, "iso-8859-1");
        
        private final byte code;
        private final String name;
        
        Encoding(byte c, String n)
        {
            this.code = c;
            this.name = n;
        }
    }
    
    public interface StringConverter
    {
        String convert(String s);
    }
}
