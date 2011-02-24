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
    public void parseArgs()
    {
        assertNull("At least two arguments must be provided", MoveMusic.fromArgs(new String[0]));
        assertNull("At least two arguments must be provided", MoveMusic.fromArgs(new String[1]));

        MoveMusic mm;
        
        mm = MoveMusic.fromArgs(new String[]{"a", "b"});
        assertEquals("a", mm.getOrigDir());
        assertEquals("b", mm.getDestDir());
        assertFalse("Default is not to use URLs", mm.isUseUrls());
        
        mm = MoveMusic.fromArgs(new String[]{"--use-urls", "a", "b"});
        assertEquals("a", mm.getOrigDir());
        assertEquals("b", mm.getDestDir());
        assertTrue("Use URLs when requested", mm.isUseUrls());
        
        assertNull("A switch is not an acceptable argument",
                MoveMusic.fromArgs(new String[]{"--use-urls", "a"}));
        
        assertNull("Too many arguments is a problem",
                MoveMusic.fromArgs(new String[]{"a", "b", "c"}));
        
        assertNull("Too many arguments is a problem",
                MoveMusic.fromArgs(new String[]{"--use-urls", "a", "b", "c"}));
        
        assertNull("An unknown switch is a problem",
                MoveMusic.fromArgs(new String[]{"--unknown", "a", "b"}));
    }
}
