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
 * The {@link CsvLineDecoder_V2_Stateless} class provides helper methods to encode and decode values to/from the CSV format.
 * This is the high performance and most efficient method. It will avoid copying data at all cost and move
 * through the cache very efficently.
 *
 * @author René Schwietzke
 *
 * @since 7.0.0
 */
public final class CsvLineDecoder_V2_Stateless
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
    private CsvLineDecoder_V2_Stateless()
    {
    }

    /**
     * Decodes the given CSV-encoded data record and returns the plain unquoted fields.
     *
     * @param s
     *            the CSV-encoded data record
     * @return the plain fields
     * @throws ParseException
     */
    public static SimpleArrayList<XltCharBuffer> parse(final String s)
    {
        return parse(new SimpleArrayList<>(50), XltCharBuffer.valueOf(s), COMMA);
    }

    /**
     * Encodes the given fields to a CSV-encoded data record using the given field separator.
     *
     * @param list a list to append to, for memory efficiency, we hand one in instead of creating our own
     * @param src the buffer to read from
     * @param delimiter the field separator to use
     * @return the decoded data record
     *
     * @throws ParseException
     */
    public static SimpleArrayList<XltCharBuffer> parse(final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src, final char delimiter)
    {
        final int length = src.length();

        // empty case is handled here
        if (length == 0)
        {
            return result;
        }

        int pos = 0;
        while (pos < length)
        {
            char c = src.charAt(pos);

            if (c  == QUOTE_CHAR)
            {
                pos = startQuotedCol(result, src, delimiter, pos);
            }
            else
            {
                pos = startCol(result, src, delimiter, pos);
            }
        }

        // the special case when we end with , and that means there
        // is another empty col behind it
        // we are always length > 0!
        if (src.charAt(length - 1) == delimiter)
        {
            result.add(XltCharBuffer.EMPTY);
        }

        return result;
    }

    private static int startCol(
                    final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src, final char delimiter,
                    final int currentPos)
    {
        int length = src.length();
        int pos = currentPos;
        int start = currentPos;

        while (pos < length)
        {
            final char c = src.charAt(pos);

            // we now read everything, even a qzote, because that is legal when the field does not
            // start with a quote
            if (c == delimiter)
            {
                result.add(src.substring(start, pos));
                return pos + 1;
            }

            pos++;
        }

        // if we get to here, we read everything
        result.add(src.substring(start));

        // we don't have to offset the extra pos++ anymore, because we already are beyond the end
        return pos;
    }

    private static int startQuotedCol(
                    final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src, final char delimiter,
                    final int currentPos)
    {
        int length = src.length();
        int pos = currentPos + 1;
        int start = pos;

        while (pos < length)
        {
            final char c = src.charAt(pos);

            if (c == QUOTE_CHAR)
            {
                // if we have a quote after this, we have a quoted quote
                // we deal with that from here in its own realm to avoid copying and other
                // messy things for the main path
                if (src.peakAhead(pos + 1) == QUOTE_CHAR)
                {
                    pos = endQuotedQuotesCol(result, src, delimiter, start, pos + 1);

                    // we checked already that after our quoted col with quotes comes nothing
                    // or a delimiter
                    return pos + 1;
                }
                else
                {
                    // end it
                    result.add(src.substring(start, pos));
                    pos++;

                    // we must see a , next or nothing, otherwise something is wrong
                    final char nextChar = src.peakAhead(pos);
                    if (!(nextChar == 0 || nextChar == delimiter))
                    {
                        throw new CsvParserException("Delimiter or end of line expected at pos: " + pos, src.toString());
                    }

                    return pos + 1;
                }
            }
            pos++;
        }

        // ok, we got here, because we ran out of input, but we have not closed the field,
        // so raise a complaint
        throw new CsvParserException("Quoted col has not been properly closed", src.toString());
    }

    private static int endQuotedQuotesCol(
                    final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src, final char delimiter,
                    final int start, final int currentPos)
    {
        int length = src.length();
        int pos = currentPos;
        int move = 1;

        // move current ""
        src.put(pos - move, QUOTE_CHAR);
        pos++;

        while (pos < length)
        {
            final char c = src.charAt(pos);
            src.put(pos - move, c);

            if (c == QUOTE_CHAR)
            {
                // peak
                final int nextChar = src.peakAhead(pos + 1);
                if (nextChar == QUOTE_CHAR)
                {
                    // more quotes quotes
                    move++;
                    pos += 2;
                    continue;
                }
                else
                {
                    // this is the end
                    // we must see a , next or nothing, otherwise something is wrong
                    if (!(nextChar == 0 || nextChar == delimiter))
                    {
                        throw new CsvParserException("Delimiter or end of line expected at pos: " + pos, src.toString());
                    }

                    // get us our string and exclude the garbage at the end because we move a lot
                    // around
                    result.add(src.substring(start, pos - move));

                    // ensure we continue with the next fresh field, we checked the delimiter already
                    return pos + 1;
                }
            }
            else
            {
                pos++;
            }
        }

        // ok... we have not properly ended things
        throw new CsvParserException("Quoted field with quotes was not ended properly at: " + pos, src.toString());
    }
}
