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

import java.io.DataInput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class HohmPodcast
{
    public final String url;
    public final String link;
    public final String aurl;

    public HohmPodcast(String url, String link, String aurl)
    {
        this.url = url;
        this.link = link;
        this.aurl = aurl;
    }

    public static String toString(byte[] ba) throws UnsupportedEncodingException
    {
        int lnz = 0;
        for (int i = 0; i < ba.length; i++) {
            if (ba[i] != 0) {
                lnz = i + 1;
            }
        }

        return new String(ba, 0, lnz, "utf-8");
    }

    public static HohmPodcast parse(DataInput di, int length) throws IOException
    {
        String url, link, aurl;

        di.skipBytes(23 * 4);
        length -= 23 * 4;

        if (length <= 0) {
            return null;
        }

        int len = di.readInt();
        String type = Util.toString(di.readInt());
        len -= 8;
        Util.assertEquals("url ", type);

        di.skipBytes(12);
        len -= 12;

        byte[] ba;

        ba = new byte[len];
        di.readFully(ba);

        url = toString(ba);

        di.skipBytes(15 * 4);

        len = di.readInt();
        type = Util.toString(di.readInt());
        len -= 8;

        Util.assertEquals("link", type);

        di.skipBytes(12);
        len -= 12;

        ba = new byte[len];
        di.readFully(ba);
        link = toString(ba);

        len = di.readInt();
        type = Util.toString(di.readInt());
        len -= 8;

        while(!"aurl".equals(type)) {
            System.out.println(type);

            if(type.equals("pech")) {
                System.out.println("Recurse");

                dump(di, len);
            } else {
                ba = new byte[len];
                di.readFully(ba);
                System.out.println(new String(ba));
            }

            len = di.readInt();
            type = Util.toString(di.readInt());
            len -= 8;
        }

        Util.assertEquals("aurl", type);

        di.skipBytes(12);
        len -= 12;

        ba = new byte[len];
        di.readFully(ba);

        aurl = toString(ba);

        return new HohmPodcast(url, link, aurl);
    }

    static void dump(DataInput di, int length) throws IOException
    {
        di.skipBytes(12);

        String type = Util.toString(di.readInt());
        System.out.println(type);

//        parse(di, length - 60 + 23);
//        di.skipBytes(24);
//        length -= 24;

//        ParseLibrary.hexDumpBytes(di, length);
        while(length > 0) {
            int len = di.readInt();
            type = Util.toString(di.readInt());

            System.out.println(type + ", " + len);

            if(type.equals("strt")) {
            } else if(type.equals("pech")) {
//                dump(di, len - 8);
                ParseLibrary.hexDumpBytes(di, length);
            } else {
                len -= 8;

    //            di.skipBytes(12);
    //            len -= 12;
                byte[] ba = new byte[len];
                di.readFully(ba);
                System.out.println(new String(ba));

                length -= len;
            }
        }
    }
}
