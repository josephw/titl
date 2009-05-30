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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class Util
{
    public static void assertEquals(String expected, String actual)
        throws IOException
    {
        if(!expected.equals(actual))
        {
            throw new IOException("Expected: " + expected + " but was: " + actual);
        }
    }

    public static String toString(int tag)
    {
        char[] ba = new char[4];
        for (int i = 0; i < 4; i++) {
            ba[i] = (char) (tag >> ((3 - i) * 8) & 0xff);
        }

        return new String(ba);
    }

    public static int fromString(String s) throws IllegalArgumentException
    {
        char[] ca = s.toCharArray();
        if (ca.length != 4) {
            throw new IllegalArgumentException();
        }

        int res = 0;
        for (int i = 0; i < 4; i++) {
            int c = ca[i];
            if (c > 0xff) {
                throw new IllegalArgumentException();
            }

            res |= (c << ((3 - i) * 8));
        }

        return res;
    }

    public static String pidToString(byte[] libraryPersistentId)
    {
        StringBuffer sb = new StringBuffer(libraryPersistentId.length * 2);
        for (byte b : libraryPersistentId) {
            sb.append(String.format("%02X", b & 0xFF));
        }

        return sb.toString();
    }

    /**
     * Converts a Windows path into a file: URL, as used by iTunes in its XML.
     *
     * @param p
     * @return
     */
    public static String toUrl(String p)
    {
        try {
            URI uri;
            uri = new URI("file", "localhost", '/' + p.replace('\\', '/'), null);
            return uri.toASCIIString();
        } catch (URISyntaxException use) {
            throw new RuntimeException(use);
        }
    }
}
