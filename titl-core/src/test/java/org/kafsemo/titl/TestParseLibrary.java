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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kafsemo.titl.Base64;
import org.kafsemo.titl.Dates;
import org.kafsemo.titl.HohmPodcast;
import org.kafsemo.titl.ItlException;
import org.kafsemo.titl.Library;
import org.kafsemo.titl.ParseLibrary;
import org.kafsemo.titl.Playlist;
import org.kafsemo.titl.Podcast;
import org.kafsemo.titl.Track;
import org.kafsemo.titl.Util;

import junit.framework.TestCase;

public class TestParseLibrary extends TestCase
{
    public void testParseEmptyItunes80Library() throws IOException, ItlException
    {
        File f = new File("src/test/resources/Empty iTunes 8.0 Library.itl");
        
        Library lib = ParseLibrary.parse(f);
        assertNotNull(lib);
        assertEquals("8.0", lib.getVersion());
        assertEquals("file://localhost/C:/Users/Joseph/Music/iTunes/iTunes%20Music/", lib.getMusicFolder());
    }

    public void testParseEmptyItunes801Library() throws IOException, ItlException
    {
        File f = new File("src/test/resources/Empty iTunes 8.0.1 Library.itl");
        
        Library lib = ParseLibrary.parse(f);
        assertNotNull(lib);
        assertEquals("8.0.1", lib.getVersion());
        assertEquals("file://localhost/C:/Users/Joseph/Music/iTunes/iTunes%20Music/", lib.getMusicFolder());

        assertEquals("301496C163C0DF40", Util.pidToString(lib.getLibraryPersistentId()));
        
        String[][] expectedPlaylists = {
                {"Music", "38", "301496C163C0DF4B"},
                {"Movies", "83", "301496C163C0DF4C"},
                {"TV Shows", "86", "301496C163C0DF4D"},
                {"Podcasts", "89", "301496C163C0DF4A"},
                {"Audiobooks", "80", "301496C163C0DF4E"},
                {"Party Shuffle", "92", "301496C163C0DF48"},
                {"Genius", "73", "301496C163C0DF51"},
                {"90’s Music", "41", "301496C163C0DF42"},
                {"Music Videos", "56", "301496C163C0DF47"},
                {"My Top Rated", "44", "301496C163C0DF43"},
                {"Recently Added", "53", "301496C163C0DF46"},
                {"Recently Played", "50", "301496C163C0DF45"},
                {"Top 25 Most Played", "47", "301496C163C0DF44"},
                
                /* Expected, but not present in XML */
                {"Rented Movies"},
                {"Ringtones"},
                
                /* In XML, but not a playlist in the binary */
//                "Library",
                
                {"####!####", null, "301496C163C0DF41"}
        };
        
        List<String> expectedPlaylistNames = new ArrayList<String>(expectedPlaylists.length);
        for (String[] pla : expectedPlaylists) {
            expectedPlaylistNames.add(pla[0]);
        }
        Collections.sort(expectedPlaylistNames);
        
        List<String> playlistNames = new ArrayList<String>(lib.getPlaylistNames());
        Collections.sort(playlistNames);
        
        assertEquals(expectedPlaylistNames.toString(), playlistNames.toString());
        
        /* Check IDs */
//        Map<String, Integer> expectedIds = new HashMap<String, Integer>();
//        for (String[] pla : expectedPlaylists) {
//            if(pla.length > 1) {
//                expectedIds.put(pla[0], Integer.parseInt(pla[1]));
//            }
//        }
//        
//        for (Playlist pl : lib.getPlaylists()) {
//            if(expectedIds.containsKey(pl.getTitle())) {
//                assertEquals(expectedIds.get(pl.getTitle()).intValue(), pl.getId());
//            }
//        }
        
        /* Check PPIDs */
        Map<String, String> expectedPpids = new HashMap<String, String>();
        for (String[] pla : expectedPlaylists) {
            if(pla.length > 1) {
                expectedPpids.put(pla[0], pla[2]);
            }
        }
        
        for (Playlist pl : lib.getPlaylists()) {
            if(expectedPpids.containsKey(pl.getTitle())) {
                assertEquals("Checking PID for playlist: " + pl.getTitle(),
                        expectedPpids.get(pl.getTitle()), Util.pidToString(pl.getPpid()));
            }
        }
        
        /* Check a single smart criteria */
        Playlist pl90sMusic = null;
        for (Playlist pl : lib.getPlaylists()) {
            if (pl.getTitle().equals("90’s Music")) {
                pl90sMusic = pl;
            }
        }
        assertNotNull(pl90sMusic);
        
        byte[] expectedSmartInfo = Base64.decode(
                  "AQEAAwAAAAIAAAAZAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                + "AAAAAA==");
        
        assertEquals(Util.pidToString(expectedSmartInfo), Util.pidToString(pl90sMusic.smartInfo));
        assertTrue(Arrays.equals(expectedSmartInfo, pl90sMusic.smartInfo));
        
        byte[] expectedSmartCriteria = Base64.decode("U0xzdAABAAEAAAACAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAcAAAEAAAAAAAAAAAAAAAAAAAAAAAAA"
                + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABEAAAAAAAAB8YAAAAAAAAAAAAAAAAAAAAB"
                + "AAAAAAAAB88AAAAAAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA5AgAAAQAA"
                + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARAAAAAAAAAAB"
                + "AAAAAAAAAAAAAAAAAAAAAQAAAAAAAAABAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAA"
                + "AAAAAAAA");
        
        assertEquals(Util.pidToString(expectedSmartCriteria), Util.pidToString(pl90sMusic.smartCriteria));
        assertTrue(Arrays.equals(expectedSmartCriteria, pl90sMusic.smartCriteria));
    }
    
    public void testMinimalLibraryTracksLoaded() throws IOException, ItlException
    {
        File f = new File("src/test/resources/Minimal iTunes 8.0.1 Library.itl");
        
        Library lib = ParseLibrary.parse(f);
        
        // Song
        Collection<Track> tracks = lib.getTracks();
        assertNotNull(tracks);
        
        assertEquals("The library should contain all tracks from the podcast",
                13, tracks.size());
    }
    
    private static Track getTrack(Collection<Track> tracks, int trackId)
    {
        for (Track tt : tracks) {
            if (tt.getTrackId() == trackId) {
                return tt;
            }
        }

        fail("Track ID " + trackId + " not found");
        return null;
    }
    
    private static Playlist getPlaylist(Collection<Playlist> pls, String name)
    {
        for (Playlist pl : pls) {
            if (pl.getTitle().equals(name)) {
                return pl;
            }
        }
        
        fail("Playlist " + name + " not found");
        return null;
    }
    public void testMinimalLibraryLocalTrack() throws IOException, ItlException
    {
        File f = new File("src/test/resources/Minimal iTunes 8.0.1 Library.itl");
        
        Library lib = ParseLibrary.parse(f);
        
        Track t = getTrack(lib.getTracks(), 246);

        assertNotNull("The track list should contain track ID 246", t);

        /* Tracks exported in iTunes XML */
        assertEquals(246, t.getTrackId());
        assertEquals("Fortune", t.getName());
        assertEquals("Kristin Hersh", t.getArtist());
        assertEquals("CASH Music: Kristin Hersh", t.getAlbum());
        assertEquals("Podcast", t.getGenre());
        assertEquals("MPEG audio file", t.getKind());
        assertEquals(8611627, t.getSize());
        assertEquals(256574, t.getTotalTime());
        assertEquals(2008, t.getYear());
        assertEquals("2008-10-06T19:26:16Z", Dates.toString(t.getDateModified()));
        assertEquals("2008-10-06T19:26:16Z", Dates.toString(t.getDateAdded()));
        assertEquals(256, t.getBitRate());
//        assertEquals(44100, t.getSampleRate());
//        assertEquals("2008-09-28T16:00:00Z", Dates.toString(t.getReleaseDate()));
//        assertEquals(1, t.getArtworkCount());
//        assertEquals("6738F834EEB48618", Util.pidToString(t.getPersistentId()));
//        assertEquals("File", t.getTrackType());
//        assertTrue(t.isPodcast());
//        assertTrue(t.isUnplayed());
        
        assertEquals("C:\\Users\\Joseph\\Music\\iTunes\\iTunes Music\\Podcasts\\CASH Music_ Kristin Hersh\\Fortune.mp3",
                t.getLocation());
//        assertEquals(4, t.getFileFolderCount());
//        assertEquals(1, t.getLibraryFolderCount());
        
        /* Extra fields */
        assertEquals(0, t.getRating());
        assertEquals("http://s3.amazonaws.com/cash_users/kristinhersh/Speedbath/Fortune/Fortune_256.mp3", t.getUrl());
        
        assertEquals("kristin hersh, fortune, cash music", t.getItunesKeywords());
        assertEquals("http://kristinhersh.cashmusic.org", t.getItunesSubtitle());
//        assertEquals("Kristin Hersh", t.getAuthor());
        
        assertEquals("file://localhost/C:/Users/Joseph/Music/iTunes/iTunes%20Music/Podcasts/CASH%20Music_%20Kristin%20Hersh/Fortune.mp3",
                t.getLocalUrl());
    }
    
    public void testMinimalLibraryUndownloadedTrack() throws IOException, ItlException
    {
        File f = new File("src/test/resources/Minimal iTunes 8.0.1 Library.itl");
        
        Library lib = ParseLibrary.parse(f);
        
        Track t = getTrack(lib.getTracks(), 170);

        assertNotNull("The track list should contain track called Mississippi Kite", t);

        assertEquals(170, t.getTrackId()); // Implementation details
        assertEquals("Mississippi Kite", t.getName());
        assertEquals("Kristin Hersh", t.getArtist());
        assertEquals("CASH Music: Kristin Hersh", t.getAlbum());
        assertEquals("Podcast", t.getGenre());
        assertNull(t.getKind());
        assertEquals(9243257, t.getSize());
        assertEquals(288000, t.getTotalTime());
        assertEquals(0, t.getYear());
        assertNull(t.getDateModified());
        assertEquals("2008-10-06T19:23:46Z", Dates.toString(t.getDateAdded()));
        assertEquals(0, t.getBitRate());
//        assertEquals("2008-08-30T16:00:00Z", Dates.toString(t.getReleaseDate()));
//        assertEquals("File", t.getTrackType());
//        assertTrue(t.isPodcast());
//        assertTrue(t.isUnplayed());
        
        assertNull(t.getLocation());
//        assertEquals(4, t.getFileFolderCount());
//        assertEquals(1, t.getLibraryFolderCount());
        
        /* Extra fields */
        assertEquals(0, t.getRating());
        assertEquals("http://s3.amazonaws.com/cash_users/kristinhersh/Speedbath/MississippiKite/MississippiKite_256.mp3", t.getUrl());
        
        assertEquals("kristin hersh, mississippi kite, cash music", t.getItunesKeywords());
        assertEquals("http://kristinhersh.cashmusic.org", t.getItunesSubtitle());
//        assertEquals("Kristin Hersh", t.getAuthor());
        
        assertEquals("http://s3.amazonaws.com/cash_users/kristinhersh/Speedbath/MississippiKite/MississippiKite_256.mp3",
                t.getLocalUrl());
    }
    
    public void testMinimalLibraryPodcast() throws Exception
    {
        File f = new File("src/test/resources/Minimal iTunes 8.0.1 Library.itl");
        
        Library lib = ParseLibrary.parse(f);
    
        // Podcast
        Collection<Podcast> podcasts = lib.getPodcasts();
        assertNotNull(podcasts);
        assertEquals(1, podcasts.size());
        
        Podcast p = podcasts.iterator().next();
        assertEquals("http://feeds.cashmusic.org/kristinhersh/", p.getPodcastLocation());
        assertEquals("CASH Music: Kristin Hersh", p.getPodcastTitle());
        
        Set<String> expectedAuthors = new HashSet<String>(Arrays.asList("Kristin Hersh", "Xiu Xiu + Kristin Hersh"));
        
        assertEquals(expectedAuthors, new HashSet<String>(p.getPodcastAuthors()));
        
        HohmPodcast hp = getPlaylist(lib.getPlaylists(), "Podcasts").getHohmPodcast();
        
        assertEquals("http://kristinhersh.cashmusic.org", hp.link);
        assertEquals("http://feeds.cashmusic.org/kristinhersh/kristinhersh.jpg", hp.aurl);
        assertEquals("http://feeds.cashmusic.org/kristinhersh/", hp.url);

        Track podcastTrack = getTrack(lib.getTracks(), 152);
        
        assertEquals("Kristin Hersh is releasing a new album serially, one track per month, via her project at CASH Music. This podcast delivers each month's track, along with any other music she releases through CASH.",
                podcastTrack.getItunesSummary());
        
        assertEquals("The music from Kristin Hersh's CASH Music project", podcastTrack.getItunesSubtitle());
    }
    
    public void testMinimalLibraryPlaylist() throws Exception
    {
        File f = new File("src/test/resources/Minimal iTunes 8.0.1 Library.itl");
        
        Library lib = ParseLibrary.parse(f);
    
        // Playlist
        Collection<Playlist> playlists = lib.getPlaylists();
        assertFalse(playlists.isEmpty());

        Playlist library = null;
        
        for (Playlist pl : playlists) {
            if (pl.getTitle().equals("####!####")) {
                library = pl;
            }
        }
        
        assertNotNull("The Library playlist should be found", library);
        
        List<Integer> items = library.getItems();
        assertEquals(Collections.singletonList(246), items);
    }
    
    public void testMinimalLibraryTracksLoaded2() throws IOException, ItlException
    {
        File f = new File("src/test/resources/iTunes 8.0.1 Library TMBG.itl");
        
        Library lib = ParseLibrary.parse(f);
        
        // Song
        Collection<Track> tracks = lib.getTracks();
        assertNotNull(tracks);
        
        assertEquals("The library should contain all tracks from the podcast",
                4, tracks.size());
    }
    
    public void testMinimalLibraryLocalTrackTMBG() throws IOException, ItlException
    {
        File f = new File("src/test/resources/iTunes 8.0.1 Library TMBG.itl");
        
        Library lib = ParseLibrary.parse(f);
        
        Track t = getTrack(lib.getTracks(), 194);

        assertNotNull("The track list should contain track ID 194", t);

        /* Fields exported in iTunes XML */
        assertEquals(194, t.getTrackId());
        assertEquals("TMBG Podcast 36B", t.getName());
        assertEquals("They Might Be Giants", t.getArtist());
        assertEquals("They Might Be Giants Podcast", t.getAlbum());
        assertEquals("Podcast", t.getGenre());
        assertEquals("MPEG audio file", t.getKind());
        assertEquals(35062766, t.getSize());
        assertEquals(2114351, t.getTotalTime());
        assertEquals(2008, t.getYear());
        assertEquals("2008-10-12T23:52:43Z", Dates.toString(t.getDateModified()));
        assertEquals("2008-10-12T23:52:42Z", Dates.toString(t.getDateAdded()));
        assertEquals(128, t.getBitRate());
//        assertEquals(44100, t.getSampleRate());
//        assertEquals("2008-09-24T17:22:42Z", Dates.toString(t.getReleaseDate()));
//        assertEquals(1, t.getArtworkCount());
//        assertEquals("D3160BB5065977B1", Util.pidToString(t.getPersistentId()));
//        assertEquals("File", t.getTrackType());
//        assertTrue(t.isPodcast());
//        assertTrue(t.isUnplayed());
        
        assertEquals("C:\\Users\\Joseph\\Music\\iTunes\\iTunes Music\\Podcasts\\They Might Be Giants Podcast\\TMBG Podcast 36B.mp3",
                t.getLocation());
//        assertEquals(4, t.getFileFolderCount());
//        assertEquals(1, t.getLibraryFolderCount());
        
        /* Extra fields */
        assertEquals(0, t.getRating());
        assertEquals("http://www.tmbg.com/_media/_pod/TMBGPodcast36B.mp3", t.getUrl());
        
        assertEquals("They Might Be Giants, TMBG, John Flansburgh, John Linnell, Birdhouse in Your Soul, Istanbul, Homestar ", t.getItunesKeywords());
        assertEquals("Hosted by public radio's Duke of Dead Air-Cecil Portesque-broadcasting from a secret, very futuristic location. Includes exclusive They Might Be Giants recordings, previews and rarities.", t.getItunesSubtitle());
//        assertEquals("Kristin Hersh", t.getAuthor());
        
        assertEquals("file://localhost/C:/Users/Joseph/Music/iTunes/iTunes%20Music/Podcasts/They%20Might%20Be%20Giants%20Podcast/TMBG%20Podcast%2036B.mp3",
                t.getLocalUrl());
    }
    
    public void testMinimalLibraryPodcast2() throws Exception
    {
        File f = new File("src/test/resources/iTunes 8.0.1 Library TMBG.itl");
        
        Library lib = ParseLibrary.parse(f);
    
        // Podcast
        Collection<Podcast> podcasts = lib.getPodcasts();
        assertNotNull(podcasts);
        assertEquals(1, podcasts.size());
        
        Podcast p = podcasts.iterator().next();
        assertEquals("http://www.tmbg.com/_media/_pod/podcast.xml", p.getPodcastLocation());
        assertEquals("They Might Be Giants Podcast", p.getPodcastTitle());
        
        Set<String> expectedAuthors = new HashSet<String>(Arrays.asList("They Might Be Giants"));
        assertEquals(expectedAuthors, new HashSet<String>(p.getPodcastAuthors()));

        
        HohmPodcast hp = getPlaylist(lib.getPlaylists(), "Podcasts").getHohmPodcast();
        
        assertEquals("http://www.tmbg.com", hp.link);
        assertEquals("http://www.tmbg.com/_media/_pod/TMBGPODCAST.jpg", hp.aurl);
        assertEquals("http://www.tmbg.com/_media/_pod/podcast.xml", hp.url);
        
        Track podcastTrack = getTrack(lib.getTracks(), 156);
        
        assertEquals("Direct from Brooklyn's own They Might Be Giants, this is the only authorized and official podcast from the band. Often imitated, never remunerated. Specializing in original, live and/or rare material from the band, please enjoy TMBG's finest podcast!",
                podcastTrack.getItunesSummary());
        
        assertEquals("The official podcast of They Might Be Giants.", podcastTrack.getItunesSubtitle());
        
        assertEquals("http://www.tmbg.com/_media/_pod/podcast.xml", podcastTrack.getLocalUrl());
    }

    /**
     * Libraries in this release are compressed. This test confirms that decompression
     * works.
     * 
     * @throws IOException
     * @throws ItlException
     */
    public void testParseEmptyItunes9Library() throws IOException, ItlException
    {
        File f = new File("src/test/resources/Empty iTunes 9.0.3 Library.itl");

        Library lib = ParseLibrary.parse(f);
        assertNotNull(lib);
        assertEquals("9.0.3", lib.getVersion());
        assertEquals("file://localhost/C:/Documents%20and%20Settings/joe/My%20Documents/My%20Music/iTunes/iTunes%20Media/", lib.getMusicFolder());
    }

    /**
     * Another test for a new release.
     * 
     * @throws IOException
     * @throws ItlException
     */
    public void testParseEmptyItunes9_2Library() throws IOException, ItlException
    {
        File f = new File("src/test/resources/Empty iTunes 9.2.0 Library.itl");

        Library lib = ParseLibrary.parse(f);
        assertNotNull(lib);
        assertEquals("9.2", lib.getVersion());
        assertEquals("file://localhost/C:/Documents%20and%20Settings/joe/My%20Documents/My%20Music/iTunes/iTunes%20Media/", lib.getMusicFolder());
    }
    
    /**
     * A new release with a new obfuscation method.
     * 
     * @throws IOException
     * @throws ItlException
     */
    public void testParseEmptyItunes10_0Library() throws IOException, ItlException
    {
        File f = new File("src/test/resources/Empty iTunes 10.0 Library.itl");

        Library lib = ParseLibrary.parse(f);
        assertNotNull(lib);
        assertEquals("10.0", lib.getVersion());
        assertEquals("file://localhost/C:/Documents%20and%20Settings/joe/My%20Documents/My%20Music/iTunes/iTunes%20Media/", lib.getMusicFolder());
    }
    
    /**
     * A new point release.
     * 
     * @throws IOException
     * @throws ItlException
     */
    public void testParseEmptyItunes10_0_1Library() throws IOException, ItlException
    {
        File f = new File("src/test/resources/Empty iTunes 10.0.1 Library.itl");

        Library lib = ParseLibrary.parse(f);
        assertNotNull(lib);
        assertEquals("10.0.1", lib.getVersion());
        assertEquals("file://localhost/C:/Documents%20and%20Settings/joe/My%20Documents/My%20Music/iTunes/iTunes%20Media/", lib.getMusicFolder());
    }
    
    public void testParseEmptyItunes10_1Library() throws IOException, ItlException
    {
        File f = new File("src/test/resources/Empty iTunes 10.1 Library.itl");

        Library lib = ParseLibrary.parse(f);
        assertNotNull(lib);
        assertEquals("10.1", lib.getVersion());
        assertEquals("file://localhost/C:/Documents%20and%20Settings/joe/My%20Documents/My%20Music/iTunes/iTunes%20Media/", lib.getMusicFolder());
        assertEquals("44328F10E636D81E", Util.pidToString(lib.getLibraryPersistentId()));
    }
    
    public void testParseEmptyItunes10_2Library() throws IOException, ItlException
    {
        File f = new File("src/test/resources/Empty iTunes 10.2 Library.itl");

        Library lib = ParseLibrary.parse(f);
        assertNotNull(lib);
        assertEquals("10.2.2", lib.getVersion());
        assertEquals("file://localhost/C:/Documents%20and%20Settings/joe/My%20Documents/My%20Music/iTunes/iTunes%20Media/", lib.getMusicFolder());
        assertEquals("3A587ACD4CC64C31", Util.pidToString(lib.getLibraryPersistentId()));
    }
    
    public void testParseItunes10_2LibraryWithSingleTrack() throws IOException, ItlException
    {
        File f = new File("src/test/resources/iTunes 10.2.2 Library with single track.itl");

        Library lib = ParseLibrary.parse(f);
        assertNotNull(lib);
        assertEquals("10.2.2", lib.getVersion());
        assertEquals("file://localhost/C:/Documents%20and%20Settings/joe/My%20Documents/My%20Music/iTunes/iTunes%20Media/", lib.getMusicFolder());
        assertEquals("35F8CA737C067601", Util.pidToString(lib.getLibraryPersistentId()));
        
        Collection<Track> tracks = lib.getTracks();
        assertEquals(1, tracks.size());
        
        Track t = getTrack(tracks, 221);
        
        assertEquals("Here Sometimes", t.getName());
        assertNull(t.getAlbumPersistentId());
        
        assertEquals("C14C9C03E7DBB0E7", Util.pidToString(t.getPersistentId()));
    }
    
    public void testParseItunes10_2LibraryWithTrackWithArtwork() throws IOException, ItlException
    {
        File f = new File("src/test/resources/iTunes 10.2.2 Library with single track with artwork.itl");

        Library lib = ParseLibrary.parse(f);
        assertNotNull(lib);
        assertEquals("10.2.2", lib.getVersion());
        assertEquals("file://localhost/C:/Documents%20and%20Settings/joe/My%20Documents/My%20Music/iTunes/iTunes%20Media/", lib.getMusicFolder());
        assertEquals("35F8CA737C067601", Util.pidToString(lib.getLibraryPersistentId()));
        
        Collection<Track> tracks = lib.getTracks();
        assertEquals(1, tracks.size());
        
        // XXX Why is this 64 now, and still 221 in the XML?
        Track t = getTrack(tracks, 64);
        
        assertEquals("Here Sometimes", t.getName());
        assertEquals("00D1246314F75A0C", Util.pidToString(t.getAlbumPersistentId()));
        assertEquals("C14C9C03E7DBB0E7", Util.pidToString(t.getPersistentId()));
    }
}
