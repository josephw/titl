package org.kafsemo.titl;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

public class TestInputImpl
{
    @Test
    public void getPositionReflectsReading() throws IOException
    {
        Input in = new InputImpl(new ByteArrayInputStream(new byte[1024]));

        assertEquals(0, in.getPosition());
        in.readUnsignedByte();
        assertEquals(1, in.getPosition());
        in.readShort();
        assertEquals(3, in.getPosition());
        in.readInt();
        assertEquals(7, in.getPosition());
        in.readFully(new byte[1]);
        assertEquals(8, in.getPosition());
        in.skipBytes(1);
        assertEquals(9, in.getPosition());
    }
}
