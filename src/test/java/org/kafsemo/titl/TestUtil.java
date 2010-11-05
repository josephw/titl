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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.kafsemo.titl.Util;

public class TestUtil
{
    @Test
    public void testToString() throws UnsupportedEncodingException
    {
        int hdfmInt = 1751410285;
        assertEquals("hdfm", Util.toString(hdfmInt));
    }
    
    @Test
    public void testFromString() throws UnsupportedEncodingException
    {
        String s = "hdfm";
        
        assertEquals(1751410285, Util.fromString(s));
    }
    
    @Test
    public void isPlausibleIdentifier() throws UnsupportedEncodingException
    {
        assertTrue("Letters form a valid identifier", Util.isIdentifier("test"));
        assertTrue("Letters form a valid identifier", Util.isIdentifier("0123"));
        assertFalse("Non-alphanumeric characters are not a valid identifer",
                Util.isIdentifier("xÄ½"));
        
        assertFalse("The empty string is not a valid identifier", Util.isIdentifier(""));
    }
    
    @Test
    public void testPidToString()
    {
        byte[] libraryPersistentId = {
                0x30, 0x14, (byte) 0x96, (byte) 0xC1, 0x63, (byte) 0xC0, (byte) 0xDF, 0x40
        };

        assertEquals("301496C163C0DF40", Util.pidToString(libraryPersistentId));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void failsWithBadIdStringLength()
    {
        Util.fromString("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsWithBadIdStringCharacters()
    {
        Util.fromString("€€€€");
    }
    
    @Test
    public void pathToUrl()
    {
        String p = "C:\\Users\\Joseph\\Music\\iTunes\\iTunes Music\\Podcasts\\CASH Music_ Kristin Hersh\\Fortune.mp3";
        
        String url = Util.toUrl(p);
        
        assertEquals("A Windows file path should be converted to a URL",
                "file://localhost/C:/Users/Joseph/Music/iTunes/iTunes%20Music/Podcasts/CASH%20Music_%20Kristin%20Hersh/Fortune.mp3",
                url);
    }
}
