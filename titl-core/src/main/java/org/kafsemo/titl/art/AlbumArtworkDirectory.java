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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.kafsemo.titl.Artwork;
import org.kafsemo.titl.Library;
import org.kafsemo.titl.Track;

/**
 * Discovers all the files in the artwork directory.
 */
public class AlbumArtworkDirectory implements Iterable<File>
{
    private final File dir;
    private Set<String> names;

    private final Set<String> used = new HashSet<String>();

    public AlbumArtworkDirectory(String dirName)
    {
        this(new File(dirName));
    }

    public AlbumArtworkDirectory(File d)
    {
        this.dir = d;
        names = null;
    }

    private void scan()
    {
        Set<String> names = new HashSet<String>();

        Collection<File> files = new ArrayList<File>();
        recurse(dir, files);

        URI base = dir.toURI();

        for (File f : files) {
            String relPath = base.relativize(f.toURI()).toString();
            names.add(relPath);
        }

        this.names = names;
    }

    private Set<String> getNames()
    {
        if (names == null) {
            scan();
        }

        return names;
    }

    private void recurse(File f, Collection<File> files)
    {
        if (f.isFile()) {
            files.add(f);
        } else if (f.isDirectory()) {
            File[] fa = f.listFiles();
            if (fa != null) {
                for (File sf : fa) {
                    recurse(sf, files);
                }
            }
        }
    }

    @Override
    public Iterator<File> iterator()
    {
        return asFiles(getNames()).iterator();
    }

    private Iterable<File> asFiles(Iterable<String> names)
    {
        Collection<File> files = new ArrayList<File>();
        for (String n : names) {
            files.add(new File(dir, n));
        }
        return Collections.unmodifiableCollection(files);
    }

    public Iterable<File> getUnused()
    {
        Collection<String> c = new HashSet<String>(getNames());
        c.removeAll(used);

        List<String> inOrder = new ArrayList<String>(c);
        Collections.sort(inOrder);
        return asFiles(inOrder);
    }

    public File getCache(Library l, Artwork art)
    {
        byte[] artId = art.getPersistentId();

        return get(l, ArtworkFile.Directory.Cache, artId);
    }

    public File getDownload(Library l, Track t)
    {
        byte[] artId = t.getAlbumPersistentId();

        get(l, ArtworkFile.Directory.Cache, artId);
        return get(l, ArtworkFile.Directory.Download, artId);
    }

    public File get(Library l, Track t)
    {
        byte[] artId = t.getAlbumPersistentId();

        File f = get(l, ArtworkFile.Directory.Download, artId);
        if (f != null && f.isFile()) {
            return f;
        } else {
            return get(l, ArtworkFile.Directory.Cache, artId);
        }
    }

    public File get(Library l, ArtworkFile.Directory type, byte[] artId)
    {
        if (artId == null) {
            return null;
        }

        ArtworkFile af = new ArtworkFile(type, artId, 2);

        String f = af.toString(l.getLibraryPersistentId());

//        ArtworkFile af1 = new ArtworkFile(type, artId, 1);
//
//        String f1 = af1.toString(l.getLibraryPersistentId());
//
//        if (getNames().contains(f1)) {
//            f = f1;
//        } else {
//            f = null;
//            return null;
//        }

        used.add(f);

        return new File(dir, f);
    }
}
