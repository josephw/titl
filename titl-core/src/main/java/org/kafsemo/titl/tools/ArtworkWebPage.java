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

package org.kafsemo.titl.tools;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.kafsemo.titl.Artwork;
import org.kafsemo.titl.ItlException;
import org.kafsemo.titl.Library;
import org.kafsemo.titl.ParseLibrary;
import org.kafsemo.titl.Track;
import org.kafsemo.titl.art.AlbumArtworkDirectory;
import org.kafsemo.titl.art.ExtractArt;

/**
 * <p>Write as much artwork as possible as separate image files
 * and create a web page with album titles.</p>
 * <p>The artwork is currently from the iTunes cache, a
 * copy of the artwork embedded in the media files.</p>
 */
public class ArtworkWebPage
{
    public static void main(String[] args) throws IOException, ItlException
    {
        if (args.length != 2)
        {
            System.err.println("Usage: ArtworkWebPage <library.itl> <output-directory>");
            System.exit(5);
        }
        
        File libFile = new File(args[0]);
        File outdir = new File(args[1]);
        
        outdir.mkdir();
        
        AlbumArtworkDirectory artDir = new AlbumArtworkDirectory(artworkDirectoryFor(libFile));
        
        Library l = ParseLibrary.parse(libFile);
        
        Collection<Artwork> artwork = l.getArtwork();
        Collection<Track> tracks = l.getTracks();

        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(new File(outdir, "index.html")), "utf-8"));

        ArtworkWebPage awp = new ArtworkWebPage();
        
        Set<File> done = new HashSet<File>();
        
        try {
            bw.write("<!DOCTYPE html>\n");
            bw.write("<meta charset=utf-8>\n");
            bw.newLine();
            
            bw.write("<h1>Artwork</h1>\n");
            
            for (Track art : tracks) {
                File f = artDir.getDownload(l, art);
                
                // XXX Should be escaped
                bw.write("<h2>" + art.getName() + "</h1>");
                bw.newLine();
                bw.write("<h3>" + art.getArtist() + "</h2>");
                bw.newLine();

                if (done.contains(f)) {
                    bw.write("<p>(Done.)</p>");
                    bw.newLine();
                } else for (String filename : awp.writeAsGfxFiles(outdir, f)) {
                    // XXX Should be escaped
                    bw.write("<img src='" + filename + "'>");
                    bw.newLine();
                    
                    done.add(f);
                }
            }
            
            bw.write("<h1>Cached</h1>\n");
            
            for (Artwork art : artwork) {
                // XXX Should be escaped
                bw.write("<h2>" + art.title + "</h1>");
                bw.newLine();
                bw.write("<h3>" + art.artist + "</h2>");
                bw.newLine();
  
                File f = artDir.getCache(l, art);
                for (String filename : awp.writeAsGfxFiles(outdir, f)) {
                    // XXX Should be escaped
                    bw.write("<img src='" + filename + "'>");
                    bw.newLine();
                }
            }
            
            bw.write("<h1>Other</h1>");
            
            for (File f : artDir.getUnused()) {
                bw.write("<h3>" + f.getName() + "</h3>\n");
                
                for (String filename : awp.writeAsGfxFiles(outdir, f)) {
                    // XXX Should be escaped
                    bw.write("<img src='" + filename + "'>");
                    bw.newLine();
                }
            }
        } finally {
            bw.close();
        }
    }

    public static File artworkDirectoryFor(File libFile)
    {
        File artDir = new File(libFile.getParentFile(), "Album Artwork");
        return artDir;
    }
    
    Map<File, Iterable<String>> written = new HashMap<File, Iterable<String>>();
    int imageIndex;
    
    public Iterable<String> writeAsGfxFiles(File outdir, File f)
        throws IOException
    {
        if (f == null || !f.isFile()) {
            return Collections.emptyList();
        }

        Iterable<String> alreadyWritten = written.get(f);
        if (alreadyWritten != null) {
            return alreadyWritten;
        }
        
        Collection<byte[]> artStreams = ExtractArt.extract(f);

        Collection<String> names = new ArrayList<String>(artStreams.size());
        
        for (byte[] ba : artStreams) {
            String filename = "image-" + (imageIndex++);
            
            filename += suffix(ba);
            File output = new File(outdir, filename);
            OutputStream out = new FileOutputStream(output);
            try {
                out.write(ba);
                
                names.add(filename);
            } finally {
                out.close();
            }
        }

        written.put(f, names);
        
        return names;
    }
        
    static String suffix(byte[] ba) throws IOException
    {
        ByteArrayInputStream in = new ByteArrayInputStream(ba);
        
        Iterator<ImageReader> x = ImageIO.getImageReaders(ImageIO.createImageInputStream(in));
        if (x.hasNext()) {
            return "." + x.next().getFormatName().toLowerCase();
        } else {
            return "";
        }
    }
}
