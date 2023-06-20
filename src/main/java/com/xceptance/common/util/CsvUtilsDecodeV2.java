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

import com.xceptance.common.lang.XltCharBuffer;

/**
 * The {@link CsvUtilsDecode} class provides helper methods to encode and decode
 * values to/from the CSV format.
 * This is the high performance and most efficient method. It will avoid copying
 * data at all cost and move
 * through the cache very efficently.
 * 
 * @author René Schwietzke
 * 
 * @since 7.0.0
 */
public final class CsvUtilsDecodeV2 
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
    private CsvUtilsDecodeV2() 
    {
    }

    /**
     * Decodes the given CSV-encoded data record and returns the plain unquoted
     * fields.
     * 
     * @param s
     *          the CSV-encoded data record
     * @return the plain fields
     */
    public static SimpleArrayList<XltCharBuffer> parse(final String s) 
    {
        return parse(new SimpleArrayList<>(32), XltCharBuffer.valueOf(s), COMMA);
    }

    @SuppressWarnings("unused")
    private static final int COLLECT = 0;
    private static final int COLLECT_QUOTED = 1;
    private static final int WRITE = 2;

    /**
     * Encodes the given fields to a CSV-encoded data record using the given field
     * separator.
     * 
     * @param list           a list to append to, for memory efficiency, we hand one
     *                       in instead of creating our own
     * @param src            the buffer to read from
     * @param fieldSeparator the field separator to use
     * @return the CSV-encoded data record
     * @throws ParseException
     */
    public static SimpleArrayList<XltCharBuffer> parse(final SimpleArrayList<XltCharBuffer> result,
            final XltCharBuffer src, final char fieldSeparator) 
    {
        int pos = 0;
        int to = pos;
        int from = to;

        while (pos < src.length())
        {
            switch (getState(pos, src)) 
            {
                case COLLECT:
                    to = stateCollect(pos, src);
                    pos = to;
                    break;

                case COLLECT_QUOTED:
                    to = stateCollectQuoted(pos, src);
                    from++;
                    pos = to + 1;
                    break;

                case WRITE:
                    result.add(src.viewFromTo(from, to));
                    pos++;
                    to = pos;
                    from = to;
                    break;

                default:
                    break;
            }
        }

        result.add(src.viewFromTo(from, pos));

        return result;
    }

    private static int getState(final int pos, final XltCharBuffer src) 
    {
        if (src.charAt(pos) == '"') 
        {
            return COLLECT_QUOTED;
        } 
        else if (src.charAt(pos) == ',') 
        {
            return WRITE;
        } 
        else 
        {
            return COLLECT;
        }
    }

    private static int stateCollect(int pos, final XltCharBuffer src) 
    {
        while (pos < src.length()) 
        {
            if (src.charAt(pos) == ',' || src.charAt(pos) == '"') 
            {
                return pos;
            }
            pos++;
        }

        return pos;
    }

    private static int stateCollectQuoted(int pos, final XltCharBuffer src) 
    {
        pos++;

        while (pos < src.length()) 
        {
            if (src.charAt(pos) == '"') 
            {
                return pos;
            }
            pos++;
        }

        return pos;
    }
}
