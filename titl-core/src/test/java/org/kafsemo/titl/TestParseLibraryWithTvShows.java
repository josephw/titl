package org.kafsemo.titl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TestParseLibraryWithTvShows
{
	@Test
    public void testParseEmptyItunes80Library() throws IOException, ItlException
    {
        File f = new File("src/test/resources/iTunes 8.0.1 Library with TV show.itl");
        
        Library lib = ParseLibrary.parse(f);
        assertNotNull(lib);
        assertEquals("8.0.2", lib.getVersion());
        assertEquals("file://localhost/C:/Users/Joseph/Music/iTunes/iTunes%20Music/", lib.getMusicFolder());
    }
}
