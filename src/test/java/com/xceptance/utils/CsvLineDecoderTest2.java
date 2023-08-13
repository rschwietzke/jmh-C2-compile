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
package com.xceptance.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.common.lang.XltCharBuffer;
import com.xceptance.common.util.CsvLineDecoder;
import com.xceptance.common.util.CsvLineDecoder2;
import com.xceptance.common.util.CsvParserException;

public class CsvLineDecoderTest2
{
    void test(String s, String... expected)
    {
        final List<XltCharBuffer> result = CsvLineDecoder2.parse(s.replace("'", "\""));

        Assert.assertEquals(expected.length, result.size());
        for (int i = 0; i < expected.length; i++)
        {
            Assert.assertEquals(expected[i].replace("'", "\""), result.get(i).toString());
        }
    }

    void testException(String s, String expected)
    {
        try
        {
            final List<XltCharBuffer> result = CsvLineDecoder2.parse(s.replace("'", "\""));
        }
        catch(CsvParserException e)
        {
            var msg = e.getMessage();
            assertEquals(expected, msg);
            return;
        }
        fail("No exception was raised");
    }

    @Test
    public void empty()
    {
        var r = CsvLineDecoder.parse("");
        assertEquals(0, r.size());
    }

    @Test
    public void justChars()
    {
        test("a", "a");
        test("ab", "ab");
        test("abc", "abc");
    }

    @Test
    public void justDelimiters()
    {
        test(",", "", "");
        test(",,", "", "", "");
        test(",,,", "", "", "", "");
    }

    @Test
    public void delimiterAtTheStart()
    {
        test(",", "", "");
        test(",a", "", "a");
        test(",,a", "", "", "a");
    }

    @Test
    public void delimiterAtTheEnd()
    {
        test(",", "", "");
        test("a,", "a", "");
        test("aa,", "aa", "");
        test("abc,,", "abc", "", "");
        test("abc,cccc,", "abc", "cccc", "");
    }

    @Test
    public void delimiterInTheMiddle()
    {
        test(",", "", "");
        test(",,", "", "", "");
        test("a,b", "a", "b");
        test("a,,b", "a", "", "b");
        test("a123,,b123", "a123", "", "b123");
        test("a123,,b123,,c123", "a123", "", "b123", "", "c123");
    }

    @Test
    public void spaces()
    {
        test(" ,", " ", "");
        test(", ", "", " ");
        test(" , ", " ", " ");
        test("a, ,b", "a", " ", "b");
    }

    /**
     * All test cases use ' for definition but will run them with ", just
     * to aid the visuals here
     */
    @Test
    public void happyQuoteless()
    {
        test("a,b", "a", "b");
        test("a,b,c", "a", "b", "c");
        test("aa,bb,cc", "aa", "bb", "cc");
        test("aaa,bb,c", "aaa", "bb", "c");
        test("a,bb,ccc", "a", "bb", "ccc");
        test("a,bb,ccc,ddddd,ee,ffff", "a", "bb", "ccc", "ddddd", "ee", "ffff");
    }

    /*
     * The part with quotes
     */

    @Test
    public void minimalQuotes()
    {
        test("''", "");
        test("'',''", "", "");
        test("'','',''", "", "", "");
    }

    @Test
    public void quotesLast()
    {
        test(",''", "", "");
        test("a,''", "a", "");
        test("ab,cd,''", "ab", "cd", "");
        test("ab,cd,'e'", "ab", "cd", "e");
        test("ab,cd,'ef'", "ab", "cd", "ef");
        test("ab,cd,'ef1'", "ab", "cd", "ef1");
    }

    @Test
    public void quotesAndText()
    {
        test("'a','b'", "a", "b");

        test("'a'", "a");
        test("'aa'", "aa");
        test("'aaa'", "aaa");
        test("'aaaa'", "aaaa");
        test("'aa','bb'", "aa", "bb");
        test("'aaa','bbb'", "aaa", "bbb");
    }

    @Test
    public void quotesEverywhere()
    {
        test("'a'", "a");
        test("abc,'123'", "abc", "123");
        test("'abc',123", "abc", "123");
        test("'abc',123,'4445'", "abc", "123", "4445");
        test("abc1,'1234',45", "abc1", "1234", "45");
    }

    /*
     * Quoted quotes... things get interesting
     */
    @Test
    public void quotedQuotesSimple()
    {
        test("'''',''''", "'", "'");
        test("''''", "'");
        test(",''''", "", "'");
        test("'''',", "'", "");
    }

    @Test
    public void quotedQuotesComplex()
    {
        test("''''''", "''");
        test("'''a'''", "'a'");
        test("''''''''''''''''''", "''''''''");
    }

    @Test
    public void quotedQuotesAndText()
    {
        test("'a'''", "a'");
        test("'''a'", "'a");
        test("'a''b',cb", "a'b", "cb");
        test("'a''b',''''", "a'b", "'");
        test("'''',''''", "'", "'");
        test("'''',''''", "'", "'");
    }

    @Test
    public void delimiterInQuotes()
    {
        test("','", ",");
        test("',',','", ",", ",");
        test("''','", "',");
        test("','''", ",'");

        test("''' '''',''',''", "' '','", "");
    }

    @Test
    public void regularMixedLines()
    {
        test("a,b,c,'d,e',f,,", "a", "b", "c", "d,e", "f", "", "");
        test("abc,'123','456',,,,',,,','1012'", "abc", "123", "456", "", "", "", ",,,", "1012");
    }

    /*
     * All error cases
     */

    @Test
    public void noEndQuote()
    {
        testException("'", "Quoted col has not been properly closed");
        testException("'abcd,", "Quoted col has not been properly closed");
        testException("'','", "Quoted col has not been properly closed");
        testException("abc,'cdef", "Quoted col has not been properly closed");
    }

    @Test
    public void noDelimiterAfterClosingQuote()
    {
        testException("'' ,", "Delimiter or end of line expected at pos: 2");
        testException("'' ", "Delimiter or end of line expected at pos: 2");
    }

    @Test
    public void brokenQuotedQuotes()
    {
        testException("'''", "Quoted field with quotes was not ended properly at: 3");
        testException("'''''", "Quoted field with quotes was not ended properly at: 5");
        testException("'','''", "Quoted field with quotes was not ended properly at: 6");
        testException("'','''',''',''", "Quoted field with quotes was not ended properly at: 14");
    }

    /*
     * Extra concept tests for bit manipulation
     */
    final int BIT1 = 1;
    final int BIT2 = 2;
    final int BIT4 = 4;

    /**
     * Set a bit
     */
    @Test
    public void setBit()
    {
        // set when not set
        int empty = 0;
        empty |= BIT1;
        assertEquals(BIT1, empty);

        // set when set
        empty = 1;
        empty |= BIT1;
        assertEquals(BIT1, empty);

        // set when set and others
        empty = 3;
        empty |= BIT1;
        assertEquals(3, empty);

        // set when others are set too
        empty = 2;
        empty |= BIT1;
        assertEquals(3, empty);
    }

    /**
     * Check if set
     */
    @Test
    public void checkBit()
    {
        // check when set
        int empty = 1;
        boolean r = (empty & BIT1) != 0;
        assertTrue(r);

        // check when not set but others
        empty = 2;
        r = (empty & BIT1) != 0;
        assertFalse(r);

        // check when set and others
        empty = 3;
        r = (empty & BIT1) != 0;
        assertTrue(r);

        // check when nothhing set
        empty = 0;
        r = (empty & BIT1) != 0;
        assertFalse(r);
    }

    /**
     * Remove bit
     */
    @Test
    public void unsetBit()
    {
        // unset when set
        int empty = 1;
        empty &= ~BIT1;
        assertEquals(0, empty);

        // unset when not set
        empty = 0;
        empty &= ~BIT1;
        assertEquals(0, empty);

        // unset when set and others
        empty = 3;
        empty &= ~BIT1;
        assertEquals(2, empty);

        // unset when not set but others
        empty = 2;
        empty &= ~BIT1;
        assertEquals(2, empty);
    }


    /**
     * On/off a bit
     */
    @Test
    public void toggleBit()
    {
        // toggle when set
        int empty = 1;
        empty ^= BIT1;
        assertEquals(0, empty);

        // toggle when not set
        empty = 0;
        empty ^= BIT1;
        assertEquals(1, empty);

        // toggle when not set but others
        empty = 2;
        empty ^= BIT1;
        assertEquals(3, empty);

        // toggle when set and others
        empty = 3;
        empty ^= BIT1;
        assertEquals(2, empty);
    }

}
