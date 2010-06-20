/*
 *  titl - Tools for iTunes Libraries
 *  Copyright (C) 2008 Joseph Walton
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

package org.kafsemo.titl.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.kafsemo.titl.ItlException;
import org.kafsemo.titl.ProcessLibrary;

/**
 * A command-line tool to process a music library and move all music files from one directory
 * to another. If you move the files on disk at the same time, processing with this tool
 * will make the app pick them up from the new location.
 *
 * @author Joseph
 */
public class MoveMusic implements ProcessLibrary.StringConverter
{
    private final String origDir, destDir;

    public MoveMusic(String string, String string2)
    {
        origDir = string;
        destDir = string2;
    }

    public static void main(String[] args) throws IOException, ItlException
    {
        if (args.length != 3) {
            System.err.println("Usage: MoveMusic <iTunes Library.itl> <source directory> <destination directory>");
            System.exit(5);
        }

        MoveMusic mm = new MoveMusic(args[1], args[2]);

        /* Warn if the paths are specified differently */
        if (hasSlash(mm.origDir) != hasSlash(mm.destDir)) {
            System.err.println("Inconsistent paths - one ends with a slash and one doesn't");
        }

        ProcessLibrary pl = new ProcessLibrary();
        pl.register(0x0d, mm);

        File f = new File(args[0]);

        File f2 = new File(f.getParentFile(), f.getName() + ".processed");
        if (f2.exists()) {
            System.err.println("Target file already exists - exiting");
            System.exit(10);
        }

        OutputStream out = new FileOutputStream(f2);
        try {
            pl.process(f, out);
        } finally {
            out.close();
        }
    }

    private static boolean hasSlash(String s)
    {
        return s.endsWith("/") || s.endsWith("\\");
    }

    public String convert(String s)
    {
        if (s.startsWith(origDir)) {
            String np = destDir + s.substring(origDir.length());
            System.out.println(s + " -> " + np);
            return np;
        } else {
            return s;
        }
    }
}
