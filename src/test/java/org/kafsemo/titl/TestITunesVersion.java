/*
 *  titl - Tools for iTunes Libraries
 *  Copyright (C) 2008-2011 Joseph Walton
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestITunesVersion
{
    @Test
    public void versionsAreAtLeastTen()
    {
        assertTrue(ITunesVersion.isAtLeast("10", 10));
        assertTrue(ITunesVersion.isAtLeast("10.1", 10));
        assertTrue(ITunesVersion.isAtLeast("11", 10));
    }

    @Test
    public void earlierVersionsAreNotAtLeastTen()
    {
        assertFalse(ITunesVersion.isAtLeast("9", 10));
        assertFalse(ITunesVersion.isAtLeast("8.0", 10));
        assertFalse(ITunesVersion.isAtLeast("8.0.1", 10));
    }
}
