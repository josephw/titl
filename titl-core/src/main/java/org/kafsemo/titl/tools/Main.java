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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Main
{
    public static void main(String... args)
        throws ClassNotFoundException, SecurityException,
            NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException
    {
        if (args.length < 1)
        {
            throw new IllegalArgumentException("Usage: Main <subcommand>");
        }
        
        Class<?> cls = Class.forName(Main.class.getPackage().getName() + "." + args[0]);
        
        Method mm = cls.getMethod("main", String[].class);

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        mm.invoke(null, (Object) subArgs);
    }
}
