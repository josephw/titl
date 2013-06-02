/*
 *  titl - Tools for iTunes Libraries
 *  Copyright (C) 2013 Lael Jones
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lael
 */
class PlaylistRawItems
{    
    public int expectedItemCount;
    public long itemStartOffset;
    private final List<Integer> itemIds = new ArrayList<Integer>();
    private final List<ByteArrayOutputStream> itemBytes = new ArrayList<ByteArrayOutputStream>();   
    
    /**
     *
     * @param expectedItemCount
     */
    public PlaylistRawItems(int expectedItemCount)
    {
        this.expectedItemCount = expectedItemCount;
    }

    public List<Integer> getItemIds()
    {
        return itemIds;
    }
    
    public List<ByteArrayOutputStream> getRawItems()
    {
        return itemBytes;
    }
    
    public void addItem(int key, ByteArrayOutputStream bo)
    {
        itemIds.add(Integer.valueOf(key));
        itemBytes.add(bo);
    }

    public int getExpectedItemCount()
    {
        return expectedItemCount;
    }
    
    public long getItemStartOffset()
    {
        return itemStartOffset;
    }    
}
