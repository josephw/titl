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
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.kafsemo.titl.Artwork;
import org.kafsemo.titl.ItlException;
import org.kafsemo.titl.Library;
import org.kafsemo.titl.ParseLibrary;
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
        
        AlbumArtworkDirectory artDir = new AlbumArtworkDirectory(new File(libFile.getParentFile(), "Album Artwork"));
        
        Library l = ParseLibrary.parse(libFile);
        
        Collection<Artwork> artwork = l.getArtwork();

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outdir, "index.html")));
        
        try {
            bw.write("<!DOCTYPE html>");
            bw.newLine();
            
            int idx = 0;
            
            for (Artwork art : artwork) {
                File f = artDir.get(l, art);
                
                // XXX Should be escaped
                bw.write("<h1>" + art.title + "</h1>");
                bw.newLine();
                bw.write("<h2>" + art.artist + "</h2>");
                bw.newLine();
                
                if (f != null && f.isFile()) {
                    Collection<byte[]> artStreams = ExtractArt.extract(f);
    
                    for (byte[] ba : artStreams) {
                        String filename = "image-" + (idx++);
                        
                        filename += suffix(ba);
                        File output = new File(outdir, filename);
                        OutputStream out = new FileOutputStream(output);
                        try {
                            out.write(ba);
                        } finally {
                            out.close();
                        }
                        
                        // XXX Should be escaped
                        bw.write("<img src='" + filename + "'>");
                        bw.newLine();
                    }
                    
                } else {
                    bw.write("<p>(No art.)</p>");
                    bw.newLine();
                }
            }
        } finally {
            bw.close();
        }
    }
    
    static String suffix(byte[] ba) throws IOException
    {
        ByteArrayInputStream in = new ByteArrayInputStream(ba);
        
        Iterator<ImageReader> x = ImageIO.getImageReaders(ImageIO.createImageInputStream(in));
        if (x.hasNext()) {
            return "." + x.next().getFormatName();
        } else {
            return "";
        }
    }
}
