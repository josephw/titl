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

package org.kafsemo.titl;

import java.util.Arrays;

public class Artwork
{
    private final byte[] persistentId;
    
    public String title, artist, appTitle;
    
    public Artwork()
    {
        persistentId = null;
    }
    
    public Artwork(byte[] id)
    {
        persistentId = Arrays.copyOf(id, id.length);
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setArtist(String artist)
    {
        this.artist = artist;
    }

    public void setAppTitle(String appTitle)
    {
        this.appTitle = appTitle;
    }

    public byte[] getPersistentId()
    {
        return persistentId;
    }
}
