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
 * The {@link CsvLineDecoder_V1_FirstRewriteAttempt} class provides helper methods to encode and decode values to/from the CSV format.
 * This is the high performance and most efficient method. It will avoid copying data at all cost and move
 * through the cache very efficently.
 *
 * @author René Schwietzke
 *
 * @since 7.0.0
 */
public final class CsvLineDecoder_V1_FirstRewriteAttempt
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
    private CsvLineDecoder_V1_FirstRewriteAttempt()
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

    // our bit flags for the parser
    private static final int NONE = 0;
    private static final int COL = 1;
    private static final int STARTQUOTED = 2;
    private static final int ENDCOL = 4;
    private static final int QUOTED_COL = 8;
    private static final int QUOTED_COL_QUOTE = 16;
    private static final int QUOTED_COL_MOVE = 32;
    private static final int END_QUOTED_COL = 64;
    private static final int STARTCOL = 128;

    private static final int CHAR = 0;
    private static final int STATE = 1;
    private static final int START = 2;
    private static final int POS = 3;

    /**
     * Encodes the given fields to a CSV-encoded data record using the given field separator.
     *
     * @param list a list to append to, for memory efficiency, we hand one in instead of creating our own
     * @param src the buffer to read from
     * @param fieldSeparator the field separator to use
     * @return the decoded data record
     *
     * @throws ParseException
     */
    public static SimpleArrayList<XltCharBuffer> parse(final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src, final char fieldSeparator)
    {
        final int length = src.length();
        final int[] data = new int[4];
        // data[STATE] = NONE; // already set

        while (data[POS] < length)
        {
            data[CHAR] = src.charAt(data[POS]);

            if (data[CHAR]  == fieldSeparator)
            {
                endCol(result, src, fieldSeparator, data);
            }
            else if (data[CHAR]  == QUOTE_CHAR)
            {
                startQuoted(result, src, fieldSeparator, data);
            }
            else
            {
                col(result, src, fieldSeparator, data);
            }
        }

        // was the last char a delimiter?
        if (data[CHAR] == fieldSeparator && data[STATE] == STARTCOL)
        {
            result.add(XltCharBuffer.EMPTY);
        }

        return result;
    }

    private static void endCol(
                    final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src, final char fieldSeparator,
                    final int[] data)
    {
        result.add(src.substring(data[START], data[POS]));

        data[START] = ++data[POS];
        data[STATE] = STARTCOL;
    }

    private static void col(
                    final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src, final char fieldSeparator,
                    final int[] data)
    {
        data[STATE] = COL;
        int pos = data[POS] + 1;
        data[POS] = pos;
        int length = src.length();

        while (pos < length)
        {
            data[CHAR] = src.charAt(pos);

            if (data[CHAR] == fieldSeparator)
            {
                data[POS] = pos;
                endCol(result, src, fieldSeparator, data);
                pos = data[POS];
                break;
            }

            pos++;
        }

        data[POS] = pos;
        if (data[STATE] == COL)
        {
            endCol(result, src, fieldSeparator, data);
        }
    }

    private static void startQuoted(
                    final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src, final char fieldSeparator,
                    final int[] data)
    {
        data[STATE] = QUOTED_COL;
        data[START] = data[POS];
        int pos = data[POS] + 1;
        int length = src.length();

        while (pos < length)
        {
            char c = src.charAt(pos);

            if (c == QUOTE_CHAR)
            {
                // is the next a quote so we are quoted quote
                if (src.peakAhead(pos + 1) == QUOTE_CHAR)
                {
                    data[POS] = pos;
                    quotedQuoteStart(result, src, fieldSeparator, data);
                    break;
                }
                else
                {
                    data[POS] = pos;
                    endQuotedCol(result, src, fieldSeparator, data);
                    break;
                }
            }

            pos++;
        }

        // we have not finished and so we fail aka " are not matching
        if (data[STATE] == QUOTED_COL)
        {
            throw new CsvParserException("Quote expected but line does not end with it", src.toString());
        }
    }

    private static void quotedQuoteStart(
                    final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src, final char fieldSeparator,
                    final int[] data)
    {
        data[STATE] = QUOTED_COL_MOVE;
        // we got a start before, so nothing to set
        int pos = data[POS] + 2;
        int move = 1; // how far do we have to move
        int length = src.length();

        while (pos < length)
        {
            char c = src.charAt(pos);

            // compensate for the quote quote aka move by number of quoted quotes
            src.put(pos - move, c);

            if (c == QUOTE_CHAR)
            {
                // is the next a quote so we are quoted quote
                if (src.peakAhead(pos + 1) == QUOTE_CHAR)
                {
                    // one more to move
                    move++;
                }
                else
                {
                    result.add(src.substring(data[START] + 1, pos - move));

                    // next is either empty or a delimiter
                    final char nextChar = src.peakAhead(pos + 1);
                    if (nextChar == 0 || nextChar == fieldSeparator)
                    {
                        data[POS] = pos + 2;
                        data[START] = data[POS];
                        data[STATE] = STARTCOL;
                        data[CHAR] = nextChar;
                        break;
                    }
                    else
                    {
                        throw new CsvParserException(String.format("Delimiter or end of line expected at pos %d", pos + 1), src.toString());
                    }
                }
            }

            pos++;
        }

        if (data[STATE] != STARTCOL)
        {
            // we have not finished correctly
            throw new CsvParserException(String.format("Incorrectly quoted quotes, last pos: ", data[POS]), src.toString());
        }
    }

    private static void endQuotedCol(
                    final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src, final char fieldSeparator,
                    final int[] data)
    {
        // ok, we have to give up on the first and the last "
        result.add(src.substring(data[START] + 1, data[POS]));

        // next is either empty or a delimiter
        final char nextChar = src.peakAhead(data[POS] + 1);
        if (nextChar == 0 || nextChar == fieldSeparator)
        {
            data[POS] += 2;
            data[START] = data[POS];
            data[STATE] = STARTCOL;
            data[CHAR] = nextChar;
        }
        else
        {
            throw new CsvParserException(String.format("Delimiter or end of line expected at pos %d", data[POS] + 1), src.toString());
        }
    }
}
