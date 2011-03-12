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
}
