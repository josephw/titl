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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.kafsemo.titl.Artwork;
import org.kafsemo.titl.Library;
import org.kafsemo.titl.Track;
import org.kafsemo.titl.Util;

/**
 * Discovers all the files in the artwork directory.
 *
 * @author Joseph
 */
public class AlbumArtworkDirectory implements Iterable<File>
{
    private final File dir;
    private final List<File> files;

    public AlbumArtworkDirectory(String dirName)
    {
        this(new File(dirName));
    }

    public AlbumArtworkDirectory(File d)
    {
        this.dir = d;
        files = new ArrayList<File>();
    }

    public void scan()
    {
        files.clear();
        recurse(dir);
        Collections.shuffle(files);
    }

    private void recurse(File f)
    {
        if (f.isFile()) {
            files.add(f);
        } else if (f.isDirectory()) {
            File[] fa = f.listFiles();
            if (fa != null) {
                for (File sf : fa) {
                    recurse(sf);
                }
            }
        }
    }

    @Override
    public Iterator<File> iterator()
    {
        return Collections.unmodifiableCollection(files).iterator();
    }
    
    public File getCache(Library l, Artwork art)
    {
        byte[] artId = art.getPersistentId();
        
        return get(l, "Cache", artId);
    }
    
    public File getDownload(Library l, Track t)
    {
        byte[] artId = t.getAlbumPersistentId();
        
        return get(l, "Download", artId);
    }
    
    public File get(Library l, String type, byte[] artId)
    {
        if (artId == null) {
            return null;
        }

        if (!type.equals("Cache") && !type.equals("Download")) {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
        
        File d = new File(this.dir, type);
      
        String libDir = Util.pidToString(l.getLibraryPersistentId());

        d = new File(d, libDir);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02d", artId[7] & 0x0F));
        sb.append(File.separator);
        sb.append(String.format("%02d", (artId[7] >> 4) & 0x0F));
        sb.append(File.separator);
        sb.append(String.format("%02d", artId[6] & 0x0F));
        
        d = new File(d, sb.toString());
        
        File f = new File(d,
                libDir + "-" + Util.pidToString(artId) + ".itc2");

        return f;
    }
}
