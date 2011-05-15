package org.kafsemo.titl.rdf;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.kafsemo.titl.ItlException;
import org.kafsemo.titl.Library;
import org.kafsemo.titl.ParseLibrary;
import org.kafsemo.titl.Track;
import org.kafsemo.titl.Util;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.turtle.TurtleWriterFactory;

public class LibraryAsRdf
{
    static URI nid3(String s)
    {
        return new URIImpl("http://www.semanticdesktop.org/ontologies/2007/05/10/nid3/#" + s);
    }
    
    private static final URI TYPE = new URIImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    private static final URI AUDIO = new URIImpl("http://www.semanticdesktop.org/ontologies/nfo/#Audio");
    
    private static final URI NID3_TITLE = nid3("title"),
        NID3_ALBUM_TITLE = nid3("albumTitle"),
        NID3_LEAD_ARTIST = nid3("leadArtist"),
        NID3_ATTACHED_PICTURE = nid3("attachedPicture");

    static URI titl(String s)
    {
        return new URIImpl("tag:kafsemo.org,2011:titl-rdf/" + s);
    }
    
    private static final URI ITUNES_PLAY_COUNT = titl("Play Count");
    private static final URI ITUNES_PLAY_DATE_UTC = titl("Play Date UTC");
    private static final URI ITUNES_PERSISTENT_ID = titl("Persistent ID");
    
    
    public static void main(String[] args) throws RDFHandlerException, IOException, ItlException
    {
        if (args.length != 1) {
            System.err.println("Usage: LibraryAsRdf <iTunes Library.itl>");
            System.exit(5);
        }
        
        String filename = args[0];
        
        Library lib = ParseLibrary.parse(new File(filename));
        
        RDFWriter rw = new TurtleWriterFactory().getWriter(System.out);

        rw.handleNamespace("nfo", "http://www.semanticdesktop.org/ontologies/nfo/#");
        rw.handleNamespace("nid3", nid3("").toString());
        
        rw.startRDF();


//        int count = 0;

        DateFormat df = utcDateFormat();
        
        for (Track t : lib.getTracks()) {
//            if (count++ > 10)
//                break;
            
            Resource res = new URIImpl(t.getLocalUrl());

            rw.handleStatement(new StatementImpl(res, TYPE, AUDIO));
            
            String artist = t.getArtist();
            String album = t.getAlbum();
            // Track index?
            String title = t.getName();
            
            if (artist != null) {
                rw.handleStatement(new StatementImpl(res, NID3_LEAD_ARTIST, new LiteralImpl(artist)));
            }
            
            if (album != null) {
                rw.handleStatement(new StatementImpl(res, NID3_ALBUM_TITLE, new LiteralImpl(album)));
            }
            
            if (title != null) {
                rw.handleStatement(new StatementImpl(res, NID3_TITLE, new LiteralImpl(title)));
            }
            
            int playCount = t.getPlayCount();
            Date lastPlayDate = t.getLastPlayDate();
            byte[] persistentId = t.getPersistentId();

            rw.handleStatement(new StatementImpl(res, ITUNES_PLAY_COUNT, new LiteralImpl(Integer.toString(playCount))));
            
            if (lastPlayDate != null) {
                rw.handleStatement(new StatementImpl(res, ITUNES_PLAY_DATE_UTC, new LiteralImpl(df.format(lastPlayDate))));
            }
            
            if (persistentId != null) {
                rw.handleStatement(new StatementImpl(res, ITUNES_PERSISTENT_ID, new LiteralImpl(Util.pidToString(persistentId))));
            }
        }
        rw.endRDF();
    }
    
    private static DateFormat utcDateFormat()
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df;
    }
}
