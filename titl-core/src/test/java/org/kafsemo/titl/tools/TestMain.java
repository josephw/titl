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

package org.kafsemo.titl.tools;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.junit.Test;

public class TestMain
{
    @Test(expected = IllegalArgumentException.class)
    public void exceptionWhenNoClassIsProvided() throws Exception
    {
        Main.main();
    }
    
    @Test(expected = ClassNotFoundException.class)
    public void unknownClassesAreNotFound() throws Exception
    {
        Main.main("UnknownClass");
    }
    
    @Test
    public void knownClassIsFound() throws Exception
    {
        Main.main("TestMain");
    }
    
    @Test(expected = IOException.class)
    public void correctArgumentsArePassedToMainMethod() throws Throwable
    {
        try {
            Main.main("TestMain", "arg1", "arg2");
            fail();
        } catch (InvocationTargetException ite) {
            throw ite.getCause();
        }
    }
    
    public static void main(String[] args) throws IOException
    {
        String[] expected = {"arg1", "arg2"};
        
        if (Arrays.equals(expected, args)) {
            throw new IOException("Expected args received");
        }
    }
}
