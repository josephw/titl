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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class Dates
{
    static String toString(Date d)
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        return df.format(d);
    }

    public static Date fromMac(long seconds)
    {
        if (seconds == 0)
        {
            return null;
        }

        seconds &= 0xFFFFFFFFL;

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        cal.clear();
        cal.set(Calendar.YEAR, 1904);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DATE, 1);

        long epoch = cal.getTimeInMillis() - 60 * 60 * 1000L;

        return new Date((seconds * 1000L) + epoch);
    }
}
