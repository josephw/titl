/*
 *  titl - Tools for iTunes Libraries
 *  Copyright (C) 2013 Lael Jones
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

import java.io.File;
import java.io.IOException;

import org.kafsemo.titl.ItlException;
import org.kafsemo.titl.RandomizePlaylist;


/**
 * A simple command-line tool to shuffle the items in a playlist.
 *
 * @author Lael Jones
 */
public class ShufflePlaylist
{
    /**
     *
     * @param args
     * @throws IOException
     * @throws ItlException
     */
    public static void main(String[] args) throws IOException, ItlException
    {
        if (args.length != 3) {
            System.err.println("Usage: Shuffle Playlist <library_in.itl> <library_out.itl> <playlist>");
            System.exit(5);
        }

        RandomizePlaylist.randomizePlaylist(new File(args[0]), new File(args[1]), args[2]);
    }
}
