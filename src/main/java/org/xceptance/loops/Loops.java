package org.xceptance.loops;
/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.util.ArrayList;
import java.util.List;

import com.xceptance.common.lang.XltCharBuffer;
import com.xceptance.common.util.SimpleArrayList;

/**
 * The {@link Loops} class provides helper methods to encode and decode values to/from the CSV format.
 * This is the high performance and most efficient method. It will avoid copying data at all cost and move
 * through the cache very efficently.
 *
 * @author René Schwietzke
 *
 * @since 7.0.0
 */
public final class Loops
{
    public static int l1_XCB_count(final XltCharBuffer src, final char fieldSeparator)
    {
        final int size = src.length();
        int count = 0;

        for (int pos = 0; pos < size; pos++)
        {
            char c = src.charAt(pos);

            if (c == fieldSeparator)
            {
                count++;
            }
        }

        return count;
    }

    public static SimpleArrayList<XltCharBuffer> l1_XCB_simpleList(final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src, final char fieldSeparator)
    {
        final int size = src.length();

        for (int pos = 0; pos < size; pos++)
        {
            char c = src.charAt(pos);

            if (c == fieldSeparator)
            {
                result.add(src);
            }
        }

        return result;
    }

    public static ArrayList<XltCharBuffer> l1_XCB_arrayList(final ArrayList<XltCharBuffer> result, final XltCharBuffer src, final char fieldSeparator)
    {
        final int size = src.length();

        for (int pos = 0; pos < size; pos++)
        {
            char c = src.charAt(pos);

            if (c == fieldSeparator)
            {
                result.add(src);
            }
        }

        return result;
    }

    public static List<XltCharBuffer> l1_XCB_list(final List<XltCharBuffer> result, final XltCharBuffer src, final char fieldSeparator)
    {
        final int size = src.length();

        for (int pos = 0; pos < size; pos++)
        {
            char c = src.charAt(pos);

            if (c == fieldSeparator)
            {
                result.add(src);
            }
        }

        return result;
    }

    public static XltCharBuffer[] l1_XCB_array(final XltCharBuffer[] result, final XltCharBuffer src, final char fieldSeparator)
    {
        final int size = src.length();
        int ai = 0;

        for (int pos = 0; pos < size; pos++)
        {
            char c = src.charAt(pos);

            if (c == fieldSeparator)
            {
                result[ai++] = src;
            }
        }

        return result;
    }
}
