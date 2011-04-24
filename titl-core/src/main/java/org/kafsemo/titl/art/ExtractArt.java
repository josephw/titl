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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.kafsemo.titl.Input;
import org.kafsemo.titl.InputImpl;
import org.kafsemo.titl.Util;

/**
 * A class to extract image data from an .itc2 file.
 * Minimal implementation using notes from <a href="http://www.falsecognate.org/2007/01/deciphering_the_itunes_itc_fil/">this article</a>.
 *
 * @author Joseph
 */
public class ExtractArt
{
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        File f = new File("/tmp/sample.itc2");

        Collection<byte[]> streams = extract(f);

//        int i = 0;

        for (byte[] ba : streams) {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(ba));

            JFrame jf = new JFrame();
            jf.getContentPane().add(new JButton(new ImageIcon(img)));
            jf.pack();
            jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            jf.setVisible(true);
//            OutputStream out = new FileOutputStream("sample" + i + ".png");
//            out.write(ba);
//            out.close();
//            i++;
        }
    }

    public static Collection<byte[]> extract(File f) throws IOException
    {
        Collection<byte[]> streams = new ArrayList<byte[]>();

        int remaining = (int) f.length();

        InputStream in = new FileInputStream(f);
        try {
            Input di = new InputImpl(in);
            while (remaining > 0) {
                int bl = di.readInt();
                String type = Util.toString(di.readInt());

                if(type.equals("item")) {
                    int ltd = di.readInt();
                    di.skipBytes(ltd - 12);

                    byte[] ba = new byte[bl - ltd];
                    di.readFully(ba);

                    streams.add(ba);
                } else {
                    di.skipBytes(bl - 8);
                }

                remaining -= bl;
            }
        } finally {
            in.close();
        }

        return streams;
    }
}
