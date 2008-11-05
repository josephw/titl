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

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.kafsemo.titl.Base64;

public class TestBase64
{
    @Test
    public void testDecodeEmpty()
    {
        byte[] ba = Base64.decode("");
        assertEquals(0, ba.length);
    }
    
    @Test
    public void testDecode() throws UnsupportedEncodingException
    {
        byte[] ba = Base64.decode("VGVzdA==");
        assertEquals("Test", new String(ba, "US-ASCII"));
    }

    @Test
    public void testDecodeWhitespaceStripped() throws UnsupportedEncodingException
    {
        byte[] ba = Base64.decode("VGV\nzdA==");
        assertEquals("Test", new String(ba, "US-ASCII"));
    }
    
    @Test
    public void testDecodeNoPadding() throws UnsupportedEncodingException
    {
        byte[] ba = Base64.decode("QUJD");
        assertEquals("ABC", new String(ba, "US-ASCII"));
    }

    @Test
    public void testDecodeOnePaddingChar() throws UnsupportedEncodingException
    {
        byte[] ba = Base64.decode("ISE=");
        assertEquals("!!", new String(ba, "US-ASCII"));
    }

    /**
     * Strings must only contain the defined Base64 characters.0
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDecodeBadCharacter()
    {
        Base64.decode("!!!!");
    }
    
    /**
     * Strings must always be a multiple of four characters long.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDecodeBadStringLength()
    {
        Base64.decode("ISE");
    }
}
