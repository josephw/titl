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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * A high-level view of a library, including all the data extracted.
 *
 * @author Joseph
 */
public class Library
{
    final Hdfm hdr;
    private final String path;
    private final Collection<Playlist> playlists;
    private final Collection<Podcast> podcasts;
    private final Collection<Track> tracks;
    private final Collection<Artwork> artwork;

    public Library(Hdfm header, String path, Collection<Playlist> playlists, Collection<Podcast> podcasts, Collection<Track> tracks,
            Collection<Artwork> artwork)
    {
        this.hdr = header;
        this.path = path;
        this.playlists = playlists;
        this.podcasts = podcasts;
        this.tracks = tracks;
        this.artwork = artwork;
    }

    public String getVersion()
    {
        return hdr.version;
    }

    public String getMusicFolder()
    {
        return path;
    }

    public byte[] getLibraryPersistentId()
    {
        int i = 0;
        
        while (hdr.headerRemainder[i] == 0x00) {
            i++;
        }
        
        /* The ID comes after the first non-zero byte */
        i++;
        
        byte[] lpid = new byte[8];
        System.arraycopy(hdr.headerRemainder, i, lpid, 0, lpid.length);
        return lpid;
    }

    public Collection<String> getPlaylistNames()
    {
        Collection<String> titles = new ArrayList<String>(playlists.size());

        for (Playlist pl : playlists)
        {
            String title = pl.getTitle();
            if(title != null) {
                titles.add(title);
            }
        }

        return titles;
    }

    private static <T> Collection<T> copy(Collection<T> coll)
    {
        return Collections.unmodifiableCollection(new ArrayList<T>(coll));
    }
    
    public Collection<Playlist> getPlaylists()
    {
        return copy(playlists);
    }

    public Collection<Track> getTracks()
    {
        return copy(tracks);
    }

    public Collection<Podcast> getPodcasts()
    {
        return copy(podcasts);
    }

    public Collection<Artwork> getArtwork()
    {
        return copy(artwork);
    }
}
