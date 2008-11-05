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

import java.io.ByteArrayOutputStream;

public class Base64
{
    public static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    public static final char PAD = '=';

    private static final int val(char c)
    {
        int idx = CHARS.indexOf(c);
        if (idx < 0) {
            throw new IllegalArgumentException("Bad Base64 character: " + c);
        }
        return idx;
    }
    
    public static byte[] decode(String s)
    {
        s = s.replaceAll("\\s+", "");
        
        char[] ca = s.toCharArray();

        if (ca.length % 4 != 0)
        {
            throw new IllegalArgumentException();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream((ca.length * 3 + 3) / 4);
        
        for (int i = 0; i < ca.length; i += 4) {
            if (ca[i + 2] == PAD) {
                int v1 = val(ca[i]),
                    v2 = val(ca[i + 1]);
                
                byte b1 = (byte) ((v1 << 2) | ((v2 >> 4) & 0x03));
                
                baos.write(b1);
            } else if (ca[i + 3] == PAD) {
                int v1 = val(ca[i]),
                    v2 = val(ca[i + 1]),
                    v3 = val(ca[i + 2]);
                
                byte b1 = (byte) ((v1 << 2) | ((v2 >> 4) & 0x03));
                byte b2 = (byte) (((v2 << 4) & 0xF0) | ((v3 >> 2) & 0x0F));
                
                baos.write(b1);
                baos.write(b2);
            } else {
                int v1 = val(ca[i]),
                    v2 = val(ca[i + 1]),
                    v3 = val(ca[i + 2]),
                    v4 = val(ca[i + 3]);
                
                byte b1 = (byte) ((v1 << 2) | ((v2 >> 4) & 0x03));
                byte b2 = (byte) (((v2 << 4) & 0xF0) | ((v3 >> 2) & 0x0F));
                byte b3 = (byte) (((v3 << 6) & 0x40) | v4);
                
                baos.write(b1);
                baos.write(b2);
                baos.write(b3);
            }
        }
        
        return baos.toByteArray();
    }
}
