package com.xceptance.common.util;
/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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

import com.xceptance.common.lang.XltCharBuffer;

/**
 * The {@link CsvLineDecoder_V3_FixedDelimiter} class provides helper methods to encode and decode values to/from the CSV format.
 * This is the high performance and most efficient method. It will avoid copying data at all cost and move
 * through the cache very efficiently.
 *
 * This is a very limited version that has only COMMA support. If you need more, use the old version.
 *
 * @author René Schwietzke
 *
 * @since 7.2.0
 */
public final class CsvLineDecoder_V3_FixedDelimiter
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
    private CsvLineDecoder_V3_FixedDelimiter()
    {
    }

    /**
     * Decodes the given CSV-encoded data record and returns the plain unquoted fields.
     *
     * @param s
     *            the CSV-encoded data record
     * @return the plain fields
     * @throws CsvParserException
     */
    public static SimpleArrayList<XltCharBuffer> parse(final String s)
    {
        return parse(new SimpleArrayList<>(50), XltCharBuffer.valueOf(s));
    }

    /**
     * Encodes the given fields to a CSV-encoded data record using the given field separator.
     *
     * @param list a list to append to, for memory efficiency, we hand one in instead of creating our own
     * @param src the buffer to read from
     * @param delimiter the field separator to use
     * @return the decoded data record
     *
     * @throws CsvParserException in case delimiters are incorrect or mostly due to incorrect quotes or quoted quotes
     */
    public static SimpleArrayList<XltCharBuffer> parse(final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src)
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
                pos = startQuotedCol(result, src, pos);
            }
            else
            {
                pos = startCol(result, src, pos);
            }
        }

        // the special case when we end with , and that means there
        // is another empty col behind it
        // we are always length > 0!
        if (src.charAt(length - 1) == COMMA)
        {
            result.add(XltCharBuffer.EMPTY);
        }

        return result;
    }

    /**
     * Reads a full column of the line without support for quotes, because the started with unquoted.
     * We end the field at a COMMA.
     *
     * @param result the final result array (holds the columns)
     * @param src the source line to be parsed
     * @param currentPos the position from where to read
     *
     * @return the next position to read
     *
     * @throws CsvParserException in case delimiters are incorrect or mostly due to incorrect quotes or quoted quotes
     */
    private static int startCol(final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src, final int currentPos)
    {
        int length = src.length();
        int pos = currentPos;
        int start = currentPos;

        while (pos < length)
        {
            final char c = src.charAt(pos);

            // we now read everything, even a qzote, because that is legal when the field does not
            // start with a quote
            if (c == COMMA)
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

    /**
     * Reads a full column of the line with support for quoted quotes and such that a COMMA as delimiter will
     * be ignored when in quotes. If a quote is not followed by another quote, the column is ended and
     * we expect a delimiter aka COMMA.
     *
     * @param result the final result array (holds the columns)
     * @param src the source line to be parsed
     * @param currentPos the position from where to read
     *
     * @return the next position to read
     *
     * @throws CsvParserException in case delimiters are incorrect or mostly due to incorrect quotes or quoted quotes
     */
    private static int startQuotedCol(final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src, final int currentPos)
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
                    pos = endQuotedQuotesCol(result, src, start, pos + 1);

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
                    if (!(nextChar == 0 || nextChar == COMMA))
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

    /**
     * If we encountered a quoted quote, we finish the rest of the data with this routine.
     *
     * @param result the final result array (holds the columns)
     * @param src the source line to be parsed
     * @param currentPos the position from where to read
     *
     * @return the next position to read

     * @throws CsvParserException in case delimiters are incorrect or mostly due to incorrect quotes or quoted quotes
     */
    private static int endQuotedQuotesCol(
                    final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src,
                    final int start, final int currentPos)
    {
        int length = src.length();
        int pos = currentPos + 1;
        int offset = 1;

        while (pos < length)
        {
            final char c = src.charAt(pos);
            src.put(pos - offset, c);

            if (c == QUOTE_CHAR)
            {
                // peak
                final int nextChar = src.peakAhead(pos + 1);
                if (nextChar == QUOTE_CHAR)
                {
                    // quoted quote
                    // increase offset distance
                    offset++;

                    // next new position plus skipping one quote
                    pos += 2;
                }
                else
                {
                    // this is the end
                    // we must see a , next or nothing, otherwise something is wrong
                    if (!(nextChar == 0 || nextChar == COMMA))
                    {
                        throw new CsvParserException("Delimiter or end of line expected at pos: " + pos, src.toString());
                    }

                    // get us our string and exclude the garbage at the end because we moved a lot
                    // around
                    result.add(src.substring(start, pos - offset));

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
