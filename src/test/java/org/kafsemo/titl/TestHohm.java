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

import static junit.framework.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

import org.junit.Test;
import org.kafsemo.titl.ItlException;
import org.kafsemo.titl.ParseLibrary;
import org.kafsemo.titl.Util;

public class TestHohm
{
    private static byte[] sampleUtf16Chunk = {
        0x68, 0x6f, 0x68, 0x6d,
        0x00, 0x00, 0x00, 0x18,
        0x00, 0x00, 0x00, 0x3c,
        0x00, 0x00, 0x00, 0x64,
        0x00, 0x00, 0x00, 0x03,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x01,
        0x00, 0x00, 0x00, 0x14,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x39, 0x00, 0x30,
        0x20, 0x19, 0x00, 0x73,
        0x00, 0x20, 0x00, 0x4d,
        0x00, 0x75, 0x00, 0x73,
        0x00, 0x69, 0x00, 0x63
    };
    
    private static byte[] sampleLatin1Chunk = {
        0x68, 0x6f, 0x68, 0x6d,
        0x00, 0x00, 0x00, 0x18,
        0x00, 0x00, 0x00, 0x34,
        0x00, 0x00, 0x00, 0x64,
        0x00, 0x00, 0x00, 0x03,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x03,
        0x00, 0x00, 0x00, 0x0c,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
        0x4d, 0x79, 0x20, 0x54,
        0x6f, 0x70, 0x20, 0x52,
        0x61, 0x74, 0x65, 0x64
    };
    
    @Test
    public void testHohmUtf16PlaylistTitle() throws IOException, ItlException
    {
        DataInput di = new DataInputStream(new ByteArrayInputStream(sampleUtf16Chunk));
        
        String type = Util.toString(di.readInt());
        assertEquals("hohm", type);
        
        int length = di.readInt();
        assertEquals(24, length);

        assertEquals(60, di.readInt());
        assertEquals(0x64, di.readInt());
        
        String val = ParseLibrary.readGenericHohm(di);
        
        assertEquals("90\u2019s Music", val);
    }
    
    @Test
    public void testHohmLatin1PlaylistTitle() throws IOException, ItlException
    {
        DataInput di = new DataInputStream(new ByteArrayInputStream(sampleLatin1Chunk));
        
        String type = Util.toString(di.readInt());
        assertEquals("hohm", type);
        
        int length = di.readInt();
        assertEquals(24, length);

        assertEquals(52, di.readInt());
        assertEquals(0x64, di.readInt());
        
        String val = ParseLibrary.readGenericHohm(di);
        
        assertEquals("My Top Rated", val);
    }
}
