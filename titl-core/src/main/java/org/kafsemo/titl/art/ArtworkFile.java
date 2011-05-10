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

import java.io.File;

import org.kafsemo.titl.Util;

public class ArtworkFile
{
    enum Directory {
        Cache,
        Download
    }

    final Directory dir;
    final byte[] id;
    final int version;
    
    public ArtworkFile(Directory dir, byte[] id, int version)
    {
        this.dir = dir;
        this.id = id;
        this.version = version;
    }

    public File getFile(File artworkDirectory, byte[] libraryPersistentId)
    {
        return new File(artworkDirectory, toString(libraryPersistentId));
    }
    
    public String toString(byte[] libraryPersistentId)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append(dir.name());
        sb.append('/');
        
        String libDir = Util.pidToString(libraryPersistentId);

        sb.append(libDir);
        sb.append('/');
        sb.append(String.format("%02d", id[7] & 0x0F));
        sb.append('/');
        sb.append(String.format("%02d", (id[7] >> 4) & 0x0F));
        sb.append('/');
        sb.append(String.format("%02d", id[6] & 0x0F));
        
        sb.append('/' + libDir + "-" + Util.pidToString(id));

        sb.append(".itc");
        
        if (version != 1) {
            sb.append(version);
        }
        
        return sb.toString();
    }
}
