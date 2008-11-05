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

import java.util.Date;

import org.junit.Test;
import org.kafsemo.titl.Dates;

import junit.framework.TestCase;

public class TestDates extends TestCase
{
    @Test
    public void testToUnsigned()
    {
        int a = -992945054;
        
        long res = a;
        
        res = 0x100000000L + a;
        
        res = (long) a & 0xFFFFFFFFL;
        
        assertEquals(3302022242L, res);
    }

    @Test
    public void testToDate()
    {
        long a = 3302022242L;
        
        Date d = Dates.fromMac(a);
        
        assertEquals("2008-08-19T19:24:02Z",
                Dates.toString(d));
    }
    
    @Test
    public void testToDate2()
    {
        long a = 3277369669L;
        
        Date d = Dates.fromMac(a);
        
        assertEquals("2007-11-08T11:27:49Z",
                Dates.toString(d));
    }
    
    @Test
    public void testToDate3()
    {
        long a = 3300033502L;
        
        Date d = Dates.fromMac(a);
        
        assertEquals("2008-07-27T18:58:22Z",
                Dates.toString(d));
    }
    
    @Test
    public void testFromLibraryFile()
    {
        int a = -988797720;
        
        Date d = Dates.fromMac(a);
        
        assertEquals("2008-10-06T19:26:16Z",
                Dates.toString(d));
    }
    
    @Test
    public void zeroBecomesNullDate()
    {
        int a = 0;
        
        Date d = Dates.fromMac(a);
        assertNull(d);
    }
}
