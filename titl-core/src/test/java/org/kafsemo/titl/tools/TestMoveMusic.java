/*
 *  titl - Tools for iTunes Libraries
 *  Copyright (C) 2008-2011 Joseph Walton
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kafsemo.titl.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestMoveMusic
{
    @Test
    public void enoughArguments()
    {
        assertNull("At least three arguments must be provided", MoveMusic.fromArgs(new String[0]));
        assertNull("At least three arguments must be provided", MoveMusic.fromArgs(new String[1]));
        assertNull("At least three arguments must be provided", MoveMusic.fromArgs(new String[2]));
    }
    
    @Test
    public void minimalArguments()
    {
        MoveMusic mm;
        
        mm = MoveMusic.fromArgs(new String[]{"lib", "a", "b"});
        assertEquals("lib", mm.getLibraryFilename());
        assertEquals("a", mm.getOrigDir());
        assertEquals("b", mm.getDestDir());
        assertFalse("Default is not to use URLs", mm.isUseUrls());
    }
    
    @Test
    public void useUrlsFlag()
    {
        MoveMusic mm;
        
        mm = MoveMusic.fromArgs(new String[]{"--use-urls", "lib", "a", "b"});
        assertEquals("lib", mm.getLibraryFilename());
        assertEquals("a", mm.getOrigDir());
        assertEquals("b", mm.getDestDir());
        assertTrue("Use URLs when requested", mm.isUseUrls());
    }
    
    @Test
    public void badArguments()
    {
        assertNull("A switch is not an acceptable argument",
                MoveMusic.fromArgs(new String[]{"--use-urls", "a"}));
        
        assertNull("Too many arguments is a problem",
                MoveMusic.fromArgs(new String[]{"a", "b", "c","d"}));
        
        assertNull("Too many arguments is a problem",
                MoveMusic.fromArgs(new String[]{"--use-urls", "a", "b", "c", "d"}));
        
        assertNull("An unknown switch is a problem",
                MoveMusic.fromArgs(new String[]{"--unknown", "a", "b"}));
    }
    
    @Test
    public void testConversion()
    {
        MoveMusic mm = new MoveMusic(null, "before-", "after-");
        assertEquals("xxx", mm.convert("xxx"));
        assertEquals("after-xx", mm.convert("before-xx"));
        assertEquals("after-xx", mm.convert("after-xx"));
    }
}
