package com.xceptance.common.util;
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


import java.text.ParseException;

import org.openjdk.jmh.annotations.CompilerControl;

import com.xceptance.common.lang.XltCharBuffer;

/**
 * The {@link CsvUtilsDecode_V3_Simple_NoQuoteSupport} class provides helper methods to encode and decode values to/from the CSV format.
 * This is the high performance and most efficient method. It will avoid copying data at all cost and move
 * through the cache very efficently.
 *
 * It does not know anything about quotes!
 *
 * @author René Schwietzke
 *
 * @since 7.1.0
 */
public final class CsvUtilsDecode_V3_Simple_NoQuoteSupport
{
    /**
     * Character constant representing a comma.
     */
    private static final char COMMA = ',';

    /**
     * Character constant representing a double quote.
     */
    private static final char QUOTE_CHAR = '"';

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private CsvUtilsDecode_V3_Simple_NoQuoteSupport()
    {
    }

    /**
     * Decodes the given CSV-encoded data record and returns the plain unquoted fields.
     * This method is for convenience. In most cases, you should have an XltCharBuffer at hand.
     *
     * @param s
     *            the CSV-encoded data record
     * @return the plain fields
     */
    public static SimpleArrayList<XltCharBuffer> parse(final String s)
    {
        return parse(new SimpleArrayList<>(32), XltCharBuffer.valueOf(s), COMMA);
    }

    /**
     * Encodes the given fields to a CSV-encoded data record using the given field separator.
     *
     * @param list a list to append to, for memory efficiency, we hand one in instead of creating our own
     * @param src the buffer to read from
     * @param fieldSeparator the field separator to use
     * @return the CSV-encoded data record
     * @throws ParseException
     */
    public static SimpleArrayList<XltCharBuffer> parse(final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src, final char fieldSeparator)
    {
        final int size = src.length();

        int pos = 0;
        int start = 0;

        while (pos < size)
        {
            final char c = src.charAt(pos);
            if (c == fieldSeparator)
            {
                if (start != pos)
                {
                    result.add(src.viewFromTo(start, pos));
                }
                else
                {
                    result.add(XltCharBuffer.EMPTY);
                }
                start = ++pos;
            }
            else
            {
                pos++;
            }
        }

        if (start < pos)
        {
            result.add(src.viewFromTo(start, pos));
        }
        else if (start == pos)
        {
            result.add(XltCharBuffer.EMPTY);
        }

        return result;
    }
}
