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

package org.kafsemo.titl;

import java.util.Date;

public class Track
{
    private int trackId;
    private String name;
    private String album;
    private String artist;
    private String genre;
    private String kind;
    private Date dateModified;
    private int size;
    private int totalTime;
    private int year;
    private Date dateAdded;
    private int bitRate;
    private int sampleRate;
    private String location;
    private int rating;
    private String url;

    public int getTrackId()
    {
        return trackId;
    }

    public String getName()
    {
        return name;
    }

    public String getArtist()
    {
        return artist;
    }

    public String getAlbum()
    {
        return album;
    }

    public String getGenre()
    {
        return genre;
    }

    public String getKind()
    {
        return kind;
    }

    public long getSize()
    {
        return size;
    }

    public int getTotalTime()
    {
        return totalTime;
    }

    public int getYear()
    {
        return year;
    }

    public int getBitRate()
    {
        return bitRate;
    }

    public int getSampleRate()
    {
        return sampleRate;
    }

    public Date getDateModified()
    {
        return dateModified;
    }

    public Date getDateAdded()
    {
        return dateAdded;
    }

    public Date getReleaseDate() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getArtworkCount() {
        // TODO Auto-generated method stub
        return -1;
    }

    public byte[] getPersistentId() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTrackType() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isPodcast() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isUnplayed() {
        // TODO Auto-generated method stub
        return false;
    }

    public String getLocation()
    {
        return location;
    }

    public int getFileFolderCount() {
        // TODO Auto-generated method stub
        return -1;
    }

    public int getLibraryFolderCount() {
        // TODO Auto-generated method stub
        return -1;
    }

    public void setTrackId(int trackId)
    {
        this.trackId = trackId;
    }

    public void setName(String n)
    {
        this.name = n;
    }

    public void setAlbum(String a)
    {
        this.album = a;
    }

    public void setArtist(String artist)
    {
        this.artist = artist;
    }

    public void setGenre(String genre)
    {
        this.genre = genre;
    }

    public void setKind(String kind)
    {
        this.kind = kind;
    }

    public void setDateModified(Date date)
    {
        this.dateModified = date;
    }

    public void setSize(int fileSize)
    {
        this.size = fileSize;
    }

    public void setTotalTime(int playtimeMillis)
    {
        this.totalTime = playtimeMillis;
    }

    public void setYear(int year)
    {
        this.year = year;
    }

    public void setDateAdded(Date date)
    {
        this.dateAdded = date;
    }

    public void setBitRate(int bitRate)
    {
        this.bitRate = bitRate;
    }

    public void setSampleRate(int rate)
    {
        this.sampleRate = rate;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public void setRating(int rating)
    {
        this.rating = rating;
    }

    public int getRating()
    {
        return rating;
    }

    /**
     * @return the download URL for a podcast item
     */
    public String getUrl()
    {
        return url;
    }

    public void setUrl(String u)
    {
        this.url = u;
    }

    String podcastUrl;
    private String itunesKeywords;
    private String subtitle;
    private String feedLink;
    private String localUrl;
    private String summary;

    public void setPodcastUrl(String url)
    {
        podcastUrl = url;
    }

    public String getItunesKeywords()
    {
        return itunesKeywords;
    }

    public void setItunesKeywords(String kw)
    {
        this.itunesKeywords = kw;
    }

    public String getItunesSubtitle()
    {
        return subtitle;
    }

    public void setItunesSubtitle(String st)
    {
        this.subtitle = st;
    }

//    public String getAuthor()
//    {
//        return author;
//    }
//
//    public void setAuthor(String a)
//    {
//        this.author = a;
//    }

    public void setFeedLink(String subtitleOrFeedLink)
    {
        this.feedLink = subtitleOrFeedLink;
    }

    public String getFeedLink()
    {
        return feedLink;
    }

    public String getLocalUrl()
    {
        return localUrl;
    }

    public void setLocalUrl(String url)
    {
        this.localUrl = url;
    }

    public String getItunesSummary()
    {
        return summary;
    }

    public void setItunesSummary(String summary)
    {
        this.summary = summary;
    }
}
