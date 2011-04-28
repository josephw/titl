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


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.kafsemo.titl.diag.InputRange;

/**
 * Development class to parse a library file. Fails as quickly as possibly
 * on unknown structure. Saves the unobfuscated contents in a new file.
 */
public class ParseLibrary
{
    private final Collection<Playlist> playlists = new ArrayList<Playlist>();

    private Playlist currentPlaylist;

    private Collection<Podcast> podcasts = new ArrayList<Podcast>();

    private Collection<Track> tracks = new ArrayList<Track>();

    private Track currentTrack;

    private List<InputRange> diagnostics = new ArrayList<InputRange>();
    
    private List<Artwork> resourcesWithArtwork = new ArrayList<Artwork>();
    private Artwork currentArtwork;
    
    public static void main(String[] args) throws Exception
    {
        if (args.length != 1) {
            System.err.println("Usage: ParseLibrary <iTunes Library.itl>");
            System.exit(5);
        }

        File f = new File(args[0]);

        Library lib = parse(f);

        OutputStream out = new FileOutputStream("decrypted-file");
        out.write(lib.hdr.fileData);
        out.close();
    }

    public static Library parse(File f) throws IOException, ItlException
    {
        long fileLength = f.length();

        InputStream in = new FileInputStream(f);
        try {
            return parse(in, fileLength);
        } finally {
            in.close();
        }
    }
    
    public static Library parse(InputStream in, long fileLength) throws IOException, ItlException
    {
        Input di = new InputImpl(in);

        Hdfm hdr = Hdfm.read(di, fileLength);

        ParseLibrary pl = new ParseLibrary();

        String path = pl.drain(new InputImpl(new ByteArrayInputStream(hdr.fileData)), hdr.fileData.length);

//        for (InputRange ir : pl.diagnostics) {
//            System.out.println(ir);
//        }
        
        Library library = new Library(hdr, path, pl.playlists, pl.podcasts, pl.tracks, pl.resourcesWithArtwork);
        return library;
    }

    String drain(Input di, int totalLength) throws UnsupportedEncodingException, IOException, ItlException
    {
        int remaining = totalLength;

        boolean going = true;

        while(going)
        {
            InputRange thisChunk = new InputRange(di.getPosition());

            diagnostics.add(thisChunk);
            
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

//                System.out.printf("hohm type: 0x%02x - ", hohmType);

                thisChunk.more = hohmType;
                
                switch (hohmType)
                {
                    case 1:
                        throw new IOException("Looks complicated...");

                    case 0x02: // Track title
                        String trackTitle = readGenericHohm(di);
                        if (currentTrack == null) {
                            throw new ItlException("Title with no track defined");
                        }
                        currentTrack.setName(trackTitle);
                        consumed = recLength;
                        break;

                    case 0x03: // Album title
                        String albumTitle = readGenericHohm(di);
                        if (currentTrack == null) {
                            throw new ItlException("Album title with no track defined");
                        }
                        currentTrack.setAlbum(albumTitle);
                        consumed = recLength;
                        break;

                    case 0x04: // Artist
                        String artist = readGenericHohm(di);
                        if (currentTrack == null) {
                            throw new ItlException("Artist with no track defined");
                        }
                        currentTrack.setArtist(artist);
                        consumed = recLength;
                        break;

                    case 0x05: // Genre
                        String genre = readGenericHohm(di);
                        if (currentTrack == null) {
                            throw new ItlException("genre with no track defined");
                        }
                        currentTrack.setGenre(genre);
                        consumed = recLength;
                        break;

                    case 0x06: // Kind
                        String kind = readGenericHohm(di);
                        if (currentTrack == null) {
                            throw new ItlException("kind with no track defined");
                        }
                        currentTrack.setKind(kind);
                        consumed = recLength;
//                        System.out.println("Kind: " + kind);
                        break;

                    case 0x0b: // Local path as URL XXX
                        String url = readGenericHohm(di);

                        if (currentTrack == null) {
                            throw new ItlException("Podcast URL with no track defined");
                        }

                        currentTrack.setLocalUrl(url);
                        consumed = recLength;
                        break;

                    case 0x0d: // Location
                        String location = readGenericHohm(di);
                        if (currentTrack == null) {
                            throw new ItlException("kind with no track defined");
                        }
                        currentTrack.setLocation(location);
                        consumed = recLength;
                        break;

                    case 0x13: // Download URL for podcast item
                        di.readInt(); // Index?
                        expectZeroBytes(di, 4);
                        consumed += 8;

                        byte[] ba = new byte[recLength - consumed];
                        di.readFully(ba);

                        String trackUrl = toString(ba);
                        if (currentTrack == null) {
                            throw new ItlException("URL with no track defined");
                        }
                        currentTrack.setUrl(trackUrl);
                        consumed = recLength;
                        break;

                    case 0x25: // Podcast URL for item
                        String pcUrl = readGenericHohm(di);

                        if (currentTrack == null) {
                            throw new ItlException("Podcast URL with no track defined");
                        }

                        currentTrack.setPodcastUrl(pcUrl);
//                        System.out.println("Podcast URL for item");
                        consumed = recLength;
                        break;

                    case 0x64: // (Smart?) Playlist title
                        String title = readGenericHohm(di);
//                        if (!title.equals("####!####")) {
                            if (currentPlaylist != null) {
                                if (currentPlaylist.title != null) {
                                    throw new ItlException("Playlist title defined twice");
                                }
                                currentPlaylist.title = title;
                            } else {
                                throw new ItlException("Playlist title without defined playlist");
                            }
//                        }
//                        System.out.println("Playlist title: " + title);
                        consumed = recLength;
                        break;

                    case 0x131: // Podcast feed URL
                        String pcFeedUrl = readGenericHohm(di);
                        ((Podcast) currentTrack).setPodcastLocation(pcFeedUrl);
                        consumed = recLength;
                        break;

                    case 0x190: // Podcast author (multiple)
                        String pcAuthor = readGenericHohm(di);
                        ((Podcast) currentTrack).addPodcastAuthor(pcAuthor);
                        consumed = recLength;
                        break;

                    case 0x12C: // General title (XXX not just podcasts)
                        String pcTitle = readGenericHohm(di);
                        currentArtwork.setTitle(pcTitle);
//                        System.out.println(pcTitle);
                        ((Podcast) currentTrack).setPodcastTitle(pcTitle);
                        consumed = recLength;
                        thisChunk.details = pcTitle;
                        break;

                    case 0x12D: // Another artist
                        String pcArtist = readGenericHohm(di);
                        currentArtwork.setArtist(pcArtist);
                        consumed = recLength;
                        thisChunk.details = pcArtist;
                        break;
                        
                    case 0x12E: // Artist again? Album artist?
                        thisChunk.details = readGenericHohm(di);
                        consumed = recLength;
                        break;
                        
                    case 0x132: // App title
                        String appTitle = readGenericHohm(di);
                        currentArtwork.setAppTitle(appTitle);
                        consumed = recLength;
                        thisChunk.details = appTitle;
                        break;
                        
                    case 0x17: // iTunes podcast keywords
                        String keywords = readGenericHohm(di);
                        currentTrack.setItunesKeywords(keywords);
                        consumed = recLength;
                        break;

                    case 0x12: // Subtitle?
                        String subtitleOrFeedLink = readGenericHohm(di);
                        currentTrack.setItunesSubtitle(subtitleOrFeedLink);
//                        currentTrack.setFeedLink(subtitleOrFeedLink);
                        consumed = recLength;
                        break;

                    case 0x15: // (Only present in full DB)
                        hexDumpBytes(di, recLength - consumed);
//                        String v = readGenericHohm(di);
//                        System.out.println(v);
                        consumed = recLength;
                        break;

                    case 0x16: // iTunes summary?
                        String summary = readGenericHohm(di);
                        currentTrack.setItunesSummary(summary);
                        consumed = recLength;
                        break;

                    case 0x24: // (Only present in full DB)
                        hexDumpBytes(di, recLength - consumed);
//                        String v = readGenericHohm(di);
//                        System.out.println(v);
                        consumed = recLength;
                        break;

                    case 0x2B: // ISRC. Could also be a more generic recording ID?
                        String id = readGenericHohm(di);
                        consumed = recLength;
                        break;

                    case 0x30: // Data for apps
                    case 0x32: // DRM key files? for apps
                        expectZeroBytes(di, 8);
                        consumed += 8;
                        byte[] xmlBa = new byte[recLength - consumed];
                        di.readFully(xmlBa);
                        String plist = new String(xmlBa);
                        consumed = recLength;
                        break;
                        
                    case 0x09: // iTunes category?

                    case 0x08:
                    case 0x14:
                    case 0x0c:
                    case 0x0e:
                    case 0x1b:
                    case 0x1e:
                    case 0x1f:
                    case 0x20:
                    case 0x21:
                    case 0x22:
                    case 0x2D: // A version string?
                    case 0x2E: // Copyright notice?
                    case 0x2F:
                    case 0xc8: // Podcast episode list title
                    case 0xC9: // Podcast title
                    case 0x1F8: // A UUID. For?
                    case 0x1F9: // A UUID. For?
                    case 0x1FA: // An email address. For?
                    case 0x191: // Artist name without 'The'; sort artist
                        String val = readGenericHohm(di);
                        consumed = recLength;
                        thisChunk.more = hohmType + " [ignored] " + val;
                        break;

                    case 0x65: // Smart criteria
                        expectZeroBytes(di, 8);

                        byte[] smartCriteria = new byte[recLength - consumed - 8];
                        di.readFully(smartCriteria);
                        if (currentPlaylist.smartCriteria != null)
                        {
                            throw new ItlException("Unexpected duplicate smart criteria");
                        }
                        currentPlaylist.smartCriteria = smartCriteria;
                        consumed = recLength;
//                        System.out.println("Smart criteria");
                        break;

                    case 0x66: // Smart info
                        expectZeroBytes(di, 8);

                        byte[] smartInfo = new byte[recLength - consumed - 8];
                        di.readFully(smartInfo);
                        if (currentPlaylist.smartInfo != null)
                        {
                            throw new ItlException("Unexpected duplicate smart info");
                        }
                        currentPlaylist.smartInfo = smartInfo;
                        consumed = recLength;
//                        System.out.println("Smart info");
                        break;

                    case 0x67: // Podcast info?
                        byte[] pcInf = new byte[recLength - consumed];
//                        arrayDumpBytes(di, pcInf.length);
//                        System.exit(0);
                        di.readFully(pcInf);
//                        System.out.println(pcInf.length);
                        try {
                            currentPlaylist.setHohmPodcast(HohmPodcast.parse(
                                    new InputImpl(new ByteArrayInputStream(pcInf)),
                                    pcInf.length));
                        } catch (IOException ioe) {
                            // XXX Failed to parse podcast
                        }
                        consumed = recLength;
                        break;

                    /* Unknown, but seen */
                    case 0x68:
                    case 0x69:
                    case 0x6A: // A list of bands?
                    case 0x6b:
                    case 0x1f7:
                    case 0x1f4:
//                        int words = (recLength - consumed) / 4;
//                        hexDump(di, words);
//                        hexDumpBytes(di, (recLength - consumed) - words * 4);
                        di.skipBytes(recLength - consumed);
                        consumed = recLength;
                        thisChunk.more = hohmType + " [skipped]";
                        break;

                    //  GLH:    TV Show-related 'hohm's
                    //
                    //          Description                                 .XML Key?
                    case 0x18:  //  Show (on 'Video' tab)                   'Series'
                    case 0x19:  //  Episode ID (on 'Video' tab)             'Episode'
                    case 0x1a:  //  ?? Studio/Producer, e.g. "Fox"          --n/a--
                    case 0x1c:  //  mpaa Rating                             'Content Rating'
                    case 0x1d:  //  ?? DTD for Propertylist                 --n/a--
                    case 0x23:  //  Sort-order for show title               'Sort Series'
                    case 0x130: //  ??  Show/Series: I think it's used for 
                                //      building the 'TV Shows' menu, since
                                //      there's one entry for each 'Season'
                                //      within a given show.
                        String tvThing = readGenericHohm(di);
//                        System.out.println(String.format("0x%04x", hohmType) + ": " + tvThing);
                        consumed = recLength;
                        break;

                    default:
                        byte[] unknownHohmContents = new byte[recLength - consumed];
                        di.readFully(unknownHohmContents);
                        throw new UnknownHohmException(hohmType, unknownHohmContents);
                }
            }
            else if(type.equals("hdsm"))
            {
                going = !readHdsm(di, length);
                consumed = length;
            }
            else if (type.equals("hpim"))
            {
                readHpim(di, length);
                consumed = length;
            }
            else if (type.equals("hptm"))
            {
                readHptm(di, length);
                consumed = length;
            }
            else if (type.equals("htim"))
            {
                int extra = readHtim(di, length);
                consumed = length;
                consumed += extra;
            }
            else if (type.equals("haim"))
            {
                thisChunk.details = readHaim(di, length - consumed);
                consumed = length;
            }
            else if (type.equals("hdfm")) {
                Hdfm.readInline(di, length, consumed);
                consumed = length;
            }
            else if (type.equals("hghm") || type.equals("halm") || type.equals("hilm") || type.equals("htlm") || type.equals("hplm")
                    || type.equals("hiim"))
            {
                di.skipBytes(length - consumed);
                consumed = length;
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

        byte[] footerBytes = new byte[remaining];
        di.readFully(footerBytes);

        String footer = new String(footerBytes, "iso-8859-1");
//        System.out.println("Footer: " + footer);

        return footer;
    }

    static void hexDumpBytes(Input di, int count) throws IOException
    {
        for (int i = 0; i < count; i++) {
            int v = di.readUnsignedByte();
//            System.out.printf("%3d 0x%02x %4s\n", i, v, (v == 0 ? ' ' : (char) v));
        }
    }

//    Byte   Length  Comment
//    -----------------------
//      0'     12      ?
//     12       4      N = length of data
//     16       8      ?
//     24       N      data

    static String readGenericHohm(Input di) throws IOException, ItlException
    {
        byte[] unknown = new byte[12];
        di.readFully(unknown);

        int dataLength = di.readInt();
        expectZeroBytes(di, 8, " in HOHM block");

        byte[] data = new byte[dataLength];
        di.readFully(data);

        return toString(data, unknown[11]);
    }

    public static String toString(byte[] data) throws UnsupportedEncodingException
    {
        return new String(data, guessEncoding(data));
    }

    public static String toString(byte[] data, byte encodingFlag) throws ItlException, UnsupportedEncodingException
    {
        switch (encodingFlag) {
        case 0: // Seems only to be used for URLs
            return new String(data, "us-ascii");

        case 1:
            return new String(data, "utf-16be");

        case 2:
            return new String(data, "utf-8");

        case 3:
            return new String(data, "windows-1252");

        default:
            throw new ItlException("Unknown encoding type " + encodingFlag + " for string: " + new String(data));
        }
    }

    public static String guessEncoding(byte[] data) throws UnsupportedEncodingException
    {
        if(data.length > 1 && data.length % 2 == 0 && data[0] == 0)
        {
            return "utf-16be";
        }
        else
        {
            return "iso-8859-1";
        }
    }

//    Byte   Length  Comment
//    -----------------------
//      0       4     'hdsm'
//      4       4     L = header length
//      8       4     ?
//     12       4     block type ?
//     16      L-16   ?
    static boolean readHdsm(Input di, int length) throws IOException
    {
        // Assume header and length already read

        int unknown = di.readInt();
        int blockType = di.readInt();

        di.skipBytes(length - 16);

//        System.out.println("HDSM block type: " + blockType);
        return (blockType == 4);
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
//        System.out.printf("0x%04x%04x\n", unknownA, unknownB);

        byte[] remaining = new byte[length - 20];
        di.readFully(remaining);

        byte[] ppid = new byte[8];
        System.arraycopy(remaining, 420, ppid, 0, ppid.length);

        currentPlaylist = new Playlist();
        currentPlaylist.ppid = ppid;
        playlists.add(currentPlaylist);
    }

    private void readHptm(Input di, int length) throws IOException, ItlException
    {
        byte[] unknown = new byte[16];
        di.readFully(unknown);

        int key = di.readInt();

//        System.out.println(" Key: " + key);

        if (currentPlaylist == null) {
            throw new ItlException("Playlist item outside playlist content");
        }

        currentPlaylist.addItem(key);

        di.skipBytes(length - 28);
    }

//    Byte   Length  Comment
//    -----------------------
//      0       4     'htim'
//      4       4     L = header length (usually 156, or 0x9C)
//      8       4     R = total record length, including sub-blocks
//     12       4     N = number of hohm sub-blocks
//     16       4     song identifier
//     20       4     block type => (1, ?)
//     24       4     ?
//     28       4     Mac OS file type (e.g. MPG3)
//     32       4     modification date
//     36       4     file size, in bytes
//     40       4     playtime, millisecs
//     44       4     track number
//     48       4     total number of tracks
//     52       2     ?
//     54       2     year
//     56       2     ?
//     58       2     bit rate
//     60       2     sample rate
//     62       2     ?
//     64       4     volume adjustment (signed)
//     68       4     start time, milliseconds
//     72       4     end time, milliseconds
//     76       4     playcount
//     80       2     ?
//     82       2     compilation (1 = yes, 0 = no)
//     84      12     ?
//     96       4     playcount again?
//    100       4     last play date
//    104       2     disk number
//    106       2     total disks
//    108       1     rating ( 0 to 100 )
//    109      11     ?
//    120       4     add date
//    124      32     ?
    public int readHtim(Input di, int length) throws IOException
    {
//        8       4     R = total record length, including sub-blocks
//         12       4     N = number of hohm sub-blocks
//         16       4     song identifier
//         20       4     block type => (1, ?)
        int recordLength = di.readInt();
        int subblocks = di.readInt();
        int songId = di.readInt();
//        System.out.println("Song ID: " + songId);
        long blockType = di.readInt();
//        System.out.println("Block type: " + blockType);

        Track track = new Track();
        track.setTrackId(songId);

//         24       4     ?
//         28       4     Mac OS file type (e.g. MPG3)
        di.skipBytes(8);

//         32       4     modification date
        int modificationDate = di.readInt();
        track.setDateModified(Dates.fromMac(modificationDate));
//        System.out.println("Modification date: " + Dates.fromMac(modificationDate));

//         36       4     file size, in bytes
        int fileSize = di.readInt();
        track.setSize(fileSize);
//        System.out.println("File size: " + fileSize);


//         40       4     playtime, millisecs
        int playtimeMillis = di.readInt();
        track.setTotalTime(playtimeMillis);

//         44       4     track number
//         48       4     total number of tracks
//         52       2     ?
        di.skipBytes(10);

//         54       2     year
        int year = di.readShort();
        track.setYear(year);

//         56       2     ?
        di.skipBytes(2);

//         58       2     bit rate
        track.setBitRate(di.readShort());

//         60       2     sample rate
        track.setSampleRate(di.readShort());

//         62       2     ?
        int x = di.readShort();

//         64       4     volume adjustment (signed)

//         68       4     start time, milliseconds
//         72       4     end time, milliseconds
        di.skipBytes(12);

//         76       4     playcount
        int playcount = di.readInt();

//         80       2     ?
//         82       2     compilation (1 = yes, 0 = no)
//         84      12     ?
        di.skipBytes(16);

//         96       4     playcount again?
        int playcountAgain = di.readInt();
        if (playcount != playcountAgain && playcountAgain != 0 && playcountAgain != 1)
        {
//            throw new IOException(playcount + " != " + playcountAgain);
        }

//        System.out.println("Play count: " + playcount);

//        100       4     last play date
        int lastPlayDate = di.readInt();

//        104       2     disk number
//        106       2     total disks
        di.skipBytes(4);

//        108       1     rating ( 0 to 100 )
        int rating = di.readUnsignedByte();
        track.setRating(rating);

//        109      11     ?
        di.skipBytes(11);

//        120       4     add date
        int addDate = di.readInt();
        track.setDateAdded(Dates.fromMac(addDate));

//        124      32     ?
        di.skipBytes(4);
        byte[] persistentId = readPersistentId(di);
        track.setPersistentId(persistentId);
        
        di.skipBytes(20);

//        System.out.println("Last play date: " + Dates.fromMac(lastPlayDate));
//        System.out.println("Add date: " + Dates.fromMac(addDate));

        long remaining = length - 156;
        
        if (remaining > 152) {
            di.skipBytes(144);
            
            byte[] id = readPersistentId(di);
            remaining -= 152;

            track.setAlbumPersistentId(id);
        }
        
        di.skipBytes((int) remaining);

        tracks.add(track);
        currentTrack = track;

        if (false)
        {
//            System.out.println("Skipping remaining: " + (recordLength - length));
            di.skipBytes(recordLength - length);
            return (recordLength - length);
        }
        else
        {
            return 0;
        }
    }

    private static final byte[] BLANK_ID = new byte[8];
    
    /* A Podcast header? */
    String readHaim(Input di, int length) throws ItlException, IOException
    {
        if (length != 80) {
            throw new IOException("Unexpected HAIM length. Expected 80, was " + length);
        }
        
        Podcast p = new Podcast();
        podcasts.add(p);

        currentTrack = p;
        
        di.skipBytes(24);
        
        byte[] persistentId = readPersistentId(di);
        
        di.skipBytes(8);
        expectZeroBytes(di, 40);
        
        Artwork artwork;
        
        if (persistentId != null) {
            artwork = new Artwork(persistentId);
            resourcesWithArtwork.add(artwork);
            currentArtwork = artwork;
            // Which track to associate this ID with?
//            System.out.println("ID was: " + Util.pidToString(persistentId));
            return Util.pidToString(persistentId);
        } else {
            artwork = new Artwork(); 
            resourcesWithArtwork.add(artwork);
            currentArtwork = artwork;
            return null;
        }
    }

    static void expectZeroBytes(Input di, int count) throws IOException, ItlException
    {
        expectZeroBytes(di, count, "");
    }
    
    static void expectZeroBytes(Input di, int count, String where) throws IOException, ItlException
    {
        byte[] ba = new byte[count];
        di.readFully(ba);
        
        for (int i = 0; i < ba.length; i++) {
            byte b = ba[i];
            if (b != 0x00) {
                throw new ItlException("Expected " + count + " zero bytes" + where
                        + ". Was: 0x" + Integer.toHexString(b) + " at offset " + i);
            }
        }
    }
    
    static byte[] readPersistentId(Input di) throws IOException
    {
        byte[] id = new byte[8];
        di.readFully(id);
        
        if (!Arrays.equals(id, BLANK_ID)) {
            return id;
        } else {
            return null;
        }
    }
}
