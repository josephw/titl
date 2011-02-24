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
import static org.kafsemo.titl.Util.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Hdfm
{
    public final String version;
    public final int unknown;
    final byte[] headerRemainder;
    public final byte[] fileData;
    public Boolean compressContents;

    private Hdfm(String version, int unknown, byte[] headerRemainder, byte[] fileData, Boolean compressContents)
    {
        this.version = version;
        this.unknown = unknown;
        this.headerRemainder = headerRemainder;
        this.fileData = fileData;
        this.compressContents = compressContents;
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
        decrypted = crypt(version, restOfFile, Cipher.DECRYPT_MODE);
        
        /* Unzip (aka inflate, decompress...) */
        byte[] inflated = inflate(decrypted);
        
        /* If inflate() returned the exact same array, that means the unzip failed, so we should assume
           that the compression shouldn't be used for this ITL file. */
        boolean useCompression = !Arrays.equals(decrypted, inflated);

        return new Hdfm(version, unknown, headerRemainder, inflated, useCompression);
    }

    /**
     * hdfm chunks occur inline in 10.0, for some reason.
     * 
     * @param di
     * @param length
     * @param consumed
     * @return
     * @throws IOException
     * @throws ItlException
     */
    public static Hdfm readInline(DataInput di, int length, int consumed) throws IOException, ItlException
    {
        int hl = di.readInt();

        if (hl != 0) {
            throw new IOException("Expected zero for inline HDFM length (was " + hl + ")");
        }
        
        int fl = di.readInt();

        int unknown = di.readInt();

        int vsl = di.readUnsignedByte();
        byte[] avs = new byte[vsl];
        di.readFully(avs);

        String version = new String(avs, "us-ascii");

        consumed += vsl + 13;

        byte[] headerRemainder = new byte[length - consumed];
        di.readFully(headerRemainder);

        consumed += headerRemainder.length;

        if (consumed != length) {
            throw new IOException("Expected to read " + length + " bytes but read " + consumed);
        }
        
        return new Hdfm(version, unknown, headerRemainder, null, false);
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
    private static byte[] crypt(String version, byte[] orig, int mode) throws UnsupportedEncodingException, ItlException
    {
        byte[] res = new byte[orig.length];

        /* Decrypt */
        try {
            byte[] rawKey = "BHUILuilfghuila3".getBytes("us-ascii");

            SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
            Cipher cip = Cipher.getInstance("AES/ECB/NoPadding");
            cip.init(mode, skeySpec);

            int encryptedLength = orig.length;

            if (ITunesVersion.isAtLeast(version, 10)) {
                encryptedLength = Math.min(encryptedLength, 102400);
            }

            encryptedLength -= encryptedLength % 16;
            
            int x = orig.length - encryptedLength;
            
            byte[] result = cip.doFinal(orig, 0, encryptedLength);
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

    static byte[] inflate(byte[] orig) throws ItlException, ZipException
    {
        /* Check for a zlib flag byte; 0x78 => 32k window, deflate */
        boolean probablyCompressed = (orig.length >= 1 && orig[0] == 0x78);
        
    	byte[] inflated = null;

    	try
    	{
	        InflaterInputStream isInflater = new InflaterInputStream(new ByteArrayInputStream(orig), new Inflater());
	        ByteArrayOutputStream osDecompressed = new ByteArrayOutputStream(orig.length);
	        inflated = new byte[orig.length];
	        int iDecompressed;
	        while(true)
	        {
	        	iDecompressed = isInflater.read(inflated, 0, orig.length);
	            if (iDecompressed == -1)
	                break;
	            osDecompressed.write(inflated, 0, iDecompressed);
	        }
	        inflated = osDecompressed.toByteArray();
	        osDecompressed.close();
	        isInflater.close();
    	}
    	catch (ZipException ze)
    	{
            if (probablyCompressed)
            {
                throw ze;
            }
    		// If a ZipException occurs, it's probably because "orig" isn't actually compressed data,
    		// because it's from an earlier version of iTunes.
    		// So since there's nothing to decompress, just return the array that was passed in, unchanged.
    		return orig;
    	}
    	catch (IOException ioe)
    	{
    		throw new ItlException("Error when unzipping the file contents", ioe);
    	}

        return inflated;
    }
    
    private static byte[] deflate(byte[] orig) throws ItlException
    {
    	try
    	{
    		DeflaterInputStream isDeflater = new DeflaterInputStream(new ByteArrayInputStream(orig), new Deflater());
    		ByteArrayOutputStream osCompressed = new ByteArrayOutputStream(orig.length);
    		byte[] deflated = new byte[orig.length];
    		int iCompressed;

    		while(true)
    		{
    			iCompressed = isDeflater.read(deflated, 0, orig.length);
    		    if (iCompressed == -1)
    		        break;
    		    osCompressed.write(deflated, 0, iCompressed);
    		}
    		
    		deflated = osCompressed.toByteArray();
    		osCompressed.close();
    		isDeflater.close();

    		return deflated;
    	}
    	catch (IOException ioe)
    	{
    		throw new ItlException("Error when zipping the file contents", ioe);
    	}
    }

    public void write(DataOutput o) throws IllegalArgumentException, IOException, ItlException
    {
        write(o, fileData);
    }

    public void write(DataOutput o, byte[] dat) throws IllegalArgumentException, IOException, ItlException
    {
    	if (this.compressContents)
    	{
    		/* If the contents were zipped before, we should zip them now, before encrypting and then writing to the file */
    		dat = deflate(dat);
    	}

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
        byte[] encrypted = crypt(version, dat, Cipher.ENCRYPT_MODE);

        o.write(encrypted);
    }
}
