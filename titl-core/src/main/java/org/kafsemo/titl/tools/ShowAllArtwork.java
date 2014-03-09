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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Random;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.kafsemo.titl.art.AlbumArtworkDirectory;
import org.kafsemo.titl.art.ExtractArt;

/**
 * Simple hack to display all iTunes artwork in a rapid collage.
 *
 * @author Joseph
 */
public class ShowAllArtwork
{
    private static final Logger log = Logger.getLogger(ShowAllArtwork.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException
    {
        if (args.length != 1) {
            System.err.println("Usage: ShowAllArtwork <iTunes directory>");
            System.exit(5);
        }

        String iTunesDirectory = args[0];

        JFrame jf = new JFrame();
        JPanel jp = new JPanel();
        jp.setPreferredSize(new Dimension(500, 500));
        jf.getContentPane().add(jp);

        jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jf.pack();
        jf.setVisible(true);

        AlbumArtworkDirectory aad = new AlbumArtworkDirectory(new File(iTunesDirectory, "Album Artwork"));

        Random r = new Random();

        for (File f : aad) {
            if (!f.isFile()) {
                continue;
            }
            Collection<byte[]> streams = ExtractArt.extract(f);

            for (byte[] ba : streams) {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(ba));

                if(img == null) {
                    log.warning("Unable to load file: " + f);
                    continue;
                }

                Dimension d = jp.getSize();

                int x, y;

                if (img.getWidth() < d.width && img.getHeight() < d.height) {
                    x = r.nextInt(d.width - img.getWidth());
                    y = r.nextInt(d.height - img.getHeight());
                } else {
                    x = (d.width - img.getWidth()) / 2;
                    y = (d.height - img.getHeight()) / 2;
                }

                jp.getGraphics().drawImage(img, x, y, null);
                Thread.sleep(100);
            }
        }
    }
}
