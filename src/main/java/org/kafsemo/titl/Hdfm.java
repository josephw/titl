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
import static org.kafsemo.titl.Util.assertEquals;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Hdfm
{
    public final String version;
    public final int unknown;
    final byte[] headerRemainder;
    public final byte[] fileData;
    
    private Hdfm(String version, int unknown, byte[] headerRemainder, byte[] fileData)
    {
        this.version = version;
        this.unknown = unknown;
        this.headerRemainder = headerRemainder;
        this.fileData = fileData;
    }
    
//    Byte   Length  Comment
//    -----------------------
//      0       4     'hdfm'
//      4       4     L = header length
//      8       4     file length ?
//     12       4     ?
//     13       1     N = length of version string
//     14       N     application version string
//     14+N   L-N-17  ?
    public static Hdfm read(DataInput di, long fileLength) throws IOException, ItlException
    {
        int hdr = di.readInt();
        assertEquals("hdfm", Util.toString(hdr));
        
        int hl = di.readInt();
        
        int fl = di.readInt();
        if (fileLength != fl)
        {
            throw new IOException("Disk file is " + fileLength + " but header claims " + fl);
        }
        
        int unknown = di.readInt();
        
        int vsl = di.readUnsignedByte();
        byte[] avs = new byte[vsl];
        di.readFully(avs);
        
        String version = new String(avs, "us-ascii");
        
        int consumed = vsl + 17;
        
        byte[] headerRemainder = new byte[hl - consumed];
        di.readFully(headerRemainder);
    
        consumed += headerRemainder.length;

        if (hl != consumed)
        {
            throw new IOException("Header claims to be " + hl + " bytes but read " + consumed);
        }

        byte[] restOfFile = new byte[(int)fileLength - consumed];
        
        di.readFully(restOfFile);

        byte[] decrypted = new byte[restOfFile.length];
        
        /* Decrypt */
        decrypted = crypt(restOfFile, Cipher.DECRYPT_MODE);
        
        return new Hdfm(version, unknown, headerRemainder, decrypted);
    }

    /**
     * Obfuscation description from
     * <a href="http://search.cpan.org/src/BDFOY/Mac-iTunes-0.90/examples/crypt-rijndael.pl">this sample</a>.
     * 
     * @param orig
     * @param mode
     * @return
     * @throws UnsupportedEncodingException
     * @throws ItlException
     */
    private static byte[] crypt(byte[] orig, int mode) throws UnsupportedEncodingException, ItlException
    {
        byte[] res = new byte[orig.length];
        
        /* Decrypt */
        try {
            byte[] rawKey = "BHUILuilfghuila3".getBytes("us-ascii");
    
            SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
            Cipher cip = Cipher.getInstance("AES/ECB/NoPadding");
            cip.init(mode, skeySpec);
    
            int x = orig.length % 16;
            
            byte[] result = cip.doFinal(orig, 0, orig.length - x);
            System.arraycopy(result, 0, res, 0, result.length);
            System.arraycopy(orig, result.length, res, result.length, x);
        } catch (GeneralSecurityException gse) {
            if (mode == Cipher.DECRYPT_MODE) {
                throw new ItlException("Unable to decrypt library", gse);
            } else if (mode == Cipher.ENCRYPT_MODE) {
                throw new ItlException("Unable to encrypt library", gse);
            } else {
                throw new ItlException("Unable to perform operation", gse);
            }
        }
        
        return res;
    }
    
    public void write(DataOutput o) throws IllegalArgumentException, IOException, ItlException
    {
        write(o, fileData);
    }
    
    public void write(DataOutput o, byte[] dat) throws IllegalArgumentException, IOException, ItlException
    {
        /* Write the header */
        byte[] ba = version.getBytes("us-ascii");
        
        assert ba.length < 256;
        
        o.writeInt(Util.fromString("hdfm"));
        
        int hl = 17 + headerRemainder.length + ba.length;
        o.writeInt(hl);
        
        int fileLength = hl + dat.length;
        o.writeInt(fileLength);
        
        o.writeInt(unknown);
        
        
        o.writeByte(ba.length);
        o.write(ba);
        
        o.write(headerRemainder);
        
        /* Encode and write the data */
        byte[] encrypted = crypt(dat, Cipher.ENCRYPT_MODE);
        
        o.write(encrypted);
    }
}
