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

public class Podcast extends Track
{
    private String podcastUrl;
    private String podcastTitle;
    private Collection<String> podcastAuthors = new ArrayList<String>();

    public String getPodcastLocation()
    {
        return podcastUrl;
    }

    public void setPodcastLocation(String l)
    {
        this.podcastUrl = l;
    }

    public String getPodcastTitle()
    {
        return podcastTitle;
    }

    public void setPodcastTitle(String t)
    {
        this.podcastTitle = t;
    }

    public Collection<String> getPodcastAuthors()
    {
        return Collections.unmodifiableCollection(podcastAuthors);
    }

    public void addPodcastAuthor(String author)
    {
        this.podcastAuthors.add(author);
    }
}
