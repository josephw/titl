package org.kafsemo.titl;

import java.io.IOException;
import java.io.InputStream;

public class FlippedInputImpl extends InputImpl
{
    public FlippedInputImpl(InputStream in)
    {
        super(in);
    }

    @Override
    public int readInt() throws IOException
    {
        return Integer.reverseBytes(super.readInt());
    }
}
