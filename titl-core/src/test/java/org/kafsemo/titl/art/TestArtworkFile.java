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

package org.kafsemo.titl.art;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestArtworkFile
{
    @Test
    public void fileFromDefinition()
    {
        ArtworkFile af = new ArtworkFile(
                ArtworkFile.Directory.Cache,
                new byte[8],
                2);
        
        String f = af.toString(new byte[8]);
        
        assertEquals(
                "Cache/0000000000000000/00/00/00/0000000000000000-0000000000000000.itc2",
                f);
    }
    
    @Test
    public void fileFromDefinitionWithNumbers()
    {
        ArtworkFile af = new ArtworkFile(
                ArtworkFile.Directory.Cache,
                new byte[]{0, 1, 2, 3, 4, 5, 6, 7},
                1);
        
        String f = af.toString(
                new byte[]{8, 9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF});
        
        assertEquals(
                "Cache/08090A0B0C0D0E0F/07/00/06/08090A0B0C0D0E0F-0001020304050607.itc",
                f);
    }
}
