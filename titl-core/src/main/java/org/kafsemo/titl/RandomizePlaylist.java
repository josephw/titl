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
package org.kafsemo.titl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import static org.kafsemo.titl.ParseLibrary.readGenericHohm;
import static org.kafsemo.titl.ParseLibrary.readHdsm;
import org.kafsemo.titl.diag.InputRange;

/**
 *
 * @author Lael
 */
public class RandomizePlaylist {

    private PlaylistRawItems currentPlaylistItems; 
    private String currentPlaylistTitle;
    
    public static void randomizePlaylist(File i, File o, String s) throws IOException, ItlException
    {
        long fileLength = i.length();

//        System.out.println("Playlist : " + s);
        InputStream in = new FileInputStream(i);
        OutputStream out = new FileOutputStream(o);        
        try {
            randomizePlaylist(in, fileLength, out, s);
        } finally {
            in.close();
            out.close();
        }
    }
    
    public static void randomizePlaylist(InputStream in, long fileLength, OutputStream out, String s) throws IOException, ItlException
    {
        Input di = new InputImpl(in);

        Hdfm hdr = Hdfm.read(di, fileLength);

        RandomizePlaylist pl = new RandomizePlaylist();

        PlaylistRawItems playlist;        
        playlist = pl.drainPlaylist(new InputImpl(new ByteArrayInputStream(hdr.fileData)), hdr.fileData.length, s);
 
        if (playlist == null)
        {
            System.out.printf("Unable to locate playlist %s\n", s);
            return;
        }
        
        // Get the item ids (song ids) from the playlist
        List<Integer> pi = playlist.getItemIds();
        Collections.shuffle(pi);
        
        // Starting byte offset within the data for the playlist items
        int offset = (int) playlist.getItemStartOffset();  
        int index = 0;
       
        List<ByteArrayOutputStream> pbi = playlist.getRawItems();
        
        // Byte array output stream that will contain the library file data
        // with the shuffled playlist
        ByteArrayOutputStream bo = new ByteArrayOutputStream(hdr.fileData.length);
        
        // Copy the library input to the output up to the place where the playlist
        // items start
        bo.write(hdr.fileData, 0, (int) playlist.getItemStartOffset());
        
        // Loop through all raw playlist items
	for (ByteArrayOutputStream temp : pbi) 
        {
            // Get the raw data in byte array form and convert the new song id
            // to a byte array suitable for writing out
            byte [] data = temp.toByteArray();
            byte [] idArr = ByteBuffer.allocate(4).putInt(pi.get(index).intValue()).array();
            
            // Write the new playlist item to a byte array
            ByteArrayOutputStream renum = new ByteArrayOutputStream(data.length);
            renum.write(data, 0, 24);
            renum.write(idArr, 0, 4);
            renum.write(data, 28, data.length - 28);      
            
            // Write the new playlist item to the library output
            bo.write(renum.toByteArray());
            
            offset += data.length;
            index++;
	}
        
        // Write the remainder of the library
        bo.write(hdr.fileData, offset, hdr.fileData.length - offset);
        
        // Write the new library
        DataOutputStream dos = new DataOutputStream(out);
        byte [] outputData = bo.toByteArray();
        hdr.write(dos, outputData);
    }    

    static void PrintData(byte [] data)
    {
        int idx = 0;
        while (idx < data.length)
        {
            if ((idx % 16) == 0)
            {
                System.out.printf("\n\t0x%04x : ", idx);
            }
            System.out.printf("0x%02x ", data[idx]);
            idx++;
        }
        System.out.printf("\n\n");
    }

    PlaylistRawItems drainPlaylist(Input di, int totalLength, String playlistTitle) throws UnsupportedEncodingException, IOException, ItlException
    {
        int remaining = totalLength;

        boolean going = true;
        boolean foundPlaylist = false;
        boolean firstItemFound = false;

        currentPlaylistTitle = null;
        
        while(going)
        {
            InputRange thisChunk = new InputRange(di.getPosition());
            
            int consumed = 0;
            String type = Util.toString(di.readInt());
            consumed += 4;

            int length = di.readInt();
            consumed += 4;
//            System.out.println(di.getPosition() + ": " + type + ": " + length);
            
            thisChunk.length = length;
            thisChunk.type = type;

            int recLength;

            if(type.equals("hohm"))
            {
                recLength = di.readInt();
                consumed += 4;

//                System.out.println("HOHM length: " + recLength);

                int hohmType = di.readInt();
                consumed += 4;

//                System.out.printf("hohm type: 0x%02x\n", hohmType);

                thisChunk.more = hohmType;                            
                switch (hohmType)
                {
                    case 1:
                        throw new IOException("Looks complicated...");

                    case 0x64: // (Smart?) Playlist title
                        String title = readGenericHohm(di);
//                        if (!title.equals("####!####")) {
                            if (currentPlaylistItems != null) {
                                if (currentPlaylistTitle != null) {
                                    throw new ItlException("Playlist title defined twice");
                                }  
                                if (firstItemFound &&
                                    ((currentPlaylistItems.getExpectedItemCount() != currentPlaylistItems.getItemIds().size()) ||
                                     (!currentPlaylistItems.getItemIds().isEmpty())))
                                {
                                    throw new ItlException("Playlist items not continuous");
                                }
                                currentPlaylistTitle = title;
                                if (title.equals(playlistTitle))
                                    foundPlaylist = true;
                            } else {
                                throw new ItlException("Playlist title without defined playlist");
                            }
//                        }
//                        System.out.println("Playlist title: " + title + " " + foundPlaylist);
                        consumed = recLength;
                        break;
                        
                    // Types that can occur inside a playlist that we dont care about
                    case 0x65: // Smart criteria
                    case 0x66: // Smart info
                    case 0x67: // Podcast info?
                    case 0x69:
                    case 0x6c:
                        di.skipBytes(recLength - consumed);                        
                        consumed = recLength;
                        thisChunk.more = hohmType + " [ignored] ";
                        if (currentPlaylistItems != null) 
                        {
                            if (firstItemFound &&
                                ((currentPlaylistItems.getExpectedItemCount() != currentPlaylistItems.getItemIds().size()) ||
                                 (!currentPlaylistItems.getItemIds().isEmpty())))
                            {
                                throw new ItlException("Playlist items not continuous");
                            }
                        }
                        else {
                            throw new ItlException("Playlist info without defined playlist");
                        } 
                        break;
                        
                    // no other hohm types should occur inside a playlist. If
                    // they do then assume that the playlist has ended
                    default: 
                        if (EndCurrentPlaylist(foundPlaylist))
                            return currentPlaylistItems;
                        di.skipBytes(recLength - consumed);
                        consumed = recLength;
                        thisChunk.more = hohmType + " [ignored] ";
                        break;
                }
            }
            else if (type.equals("hpim"))
            {
                // Starting a new playlist ends the current playlist
                if (EndCurrentPlaylist(foundPlaylist))
                    return currentPlaylistItems;              
                readHpim(di, length);
                firstItemFound = false;
                consumed = length;
            }
            else if (type.equals("hptm"))
            {
                readHptm(di, length);
                if (!firstItemFound)
                {
                    firstItemFound = true;
                    currentPlaylistItems.itemStartOffset = thisChunk.origin;
                }
                consumed = length;
            }
            else if(type.equals("hdsm"))
            {    
                // End the current playlist on hdsm as well
                if (EndCurrentPlaylist(foundPlaylist))
                    return currentPlaylistItems;                                  
                going = !readHdsm(di, length);
                consumed = length;
            }
            // Ignored types (not useful for playlist randomization)  any of these
            // types also ends parsing of the current playlist
            else if (type.equals("hghm") || type.equals("halm") || type.equals("hilm") || type.equals("htlm") || type.equals("hplm")
                    || type.equals("hiim") || type.equals("hqlm") || type.equals("hqim") || type.equals("htim") || type.equals("haim") 
                    || type.equals("hdfm"))
            {                
                di.skipBytes(length - consumed);
                consumed = length;                
                if (EndCurrentPlaylist(foundPlaylist))
                    return currentPlaylistItems;                                   
            }
            else
            {
//                hexDumpBytes(di, length - consumed);
//                consumed = length;
                if (Util.isIdentifier(type)) {
                    throw new ItlException("Unhandled type: " + type);
                } else {
                    throw new ItlException("Library format not understood; bad decryption (unhandled type: "
                            + type + ")");
                }
            }

            remaining -= consumed;
        }

        return null;
    }

//    Byte   Length  Comment
//    -----------------------
//      0       4      hpim
//      4       4      N = length of data
//      8       4      ?
//     12       4      ?
//     16       4      number of items (hptm) in playlist
    private void readHpim(Input di, int length) throws IOException, ItlException
    {
        int unknownA = di.readInt();
        int unknownB = di.readInt();

        int itemCount = di.readInt();

//        System.out.println("HPIM items: " + itemCount);
//        System.out.printf("0x%04x%04x", unknownA, unknownB);
//        System.out.println("");

        byte[] remaining = new byte[length - 20];
        di.readFully(remaining);

        byte[] ppid = new byte[8];
        System.arraycopy(remaining, 420, ppid, 0, ppid.length);

        currentPlaylistItems = new PlaylistRawItems(itemCount);
    }

    private void readHptm(Input di, int length) throws IOException, ItlException
    {
        // type/len byte arrays for recreating the entire hptm byte entries
        byte[] type = ByteBuffer.allocate(4).putInt(Util.fromString("hptm")).array();
        byte[] len = ByteBuffer.allocate(4).putInt(length).array();
        
        byte[] unknown1 = new byte[16];
        di.readFully(unknown1);

        int key = di.readInt();
        byte[] keyArr = ByteBuffer.allocate(4).putInt(key).array();
//        System.out.println(" Key: " + key);

        if (currentPlaylistItems == null) {
            throw new ItlException("Playlist item outside playlist content");
        }

        byte[] unknown2 = new byte[length - 28];
        di.readFully(unknown2);

        // Reconstruct the entire hptm entry and store it in the playlist
        ByteArrayOutputStream bo = new ByteArrayOutputStream(length);
        bo.write(type, 0, type.length);
        bo.write(len, 0, len.length);
        bo.write(unknown1, 0, unknown1.length);
        bo.write(keyArr, 0, keyArr.length);
        bo.write(unknown2, 0, unknown2.length);
        
        currentPlaylistItems.addItem(key, bo); 
    }
   
    boolean EndCurrentPlaylist(boolean foundPlaylist) throws ItlException
    {
        // Ensure that when a playlist ends that the correct number of playlist
        // items were found and if the current playlist is not the desired
        // playlist null out the current playlist/title so it gets ignored
        if (currentPlaylistItems != null)
        {
            List<Integer> items = currentPlaylistItems.getItemIds();
            int expectedCount = currentPlaylistItems.getExpectedItemCount();
            if (expectedCount != items.size())
            {
                throw new ItlException("Expected " + expectedCount + " playlist items, got " + items.size());
            }
            if (!foundPlaylist)
            {
                currentPlaylistItems = null;
                currentPlaylistTitle = null;                
            }
        }
        
        return foundPlaylist;
    } 
}
