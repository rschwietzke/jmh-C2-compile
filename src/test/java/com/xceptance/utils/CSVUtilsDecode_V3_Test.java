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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.common.lang.XltCharBuffer;
import com.xceptance.common.util.CsvUtilsDecode_V3_Simple_NoQuoteSupport;

public class CSVUtilsDecode_V3_Test
{
    void test_noQuoteConversion(String s, String... expected)
    {
        final List<XltCharBuffer> result = CsvUtilsDecode_V3_Simple_NoQuoteSupport.parse(s);

        Assert.assertEquals(expected.length, result.size());
        for (int i = 0; i < expected.length; i++)
        {
            Assert.assertEquals(expected[i], result.get(i).toString());
        }
    }

    void test(String s, String... expected)
    {
        final String _s = s.replace("'", "\"");
        test_noQuoteConversion(_s, expected);
    }

    /**
     * All test cases use ' for definition but will run them with ", just
     * to aid the visuals here
     */
    @Test
    public void happy()
    {
        test("a,foo,bar,123,1232,7,true", "a", "foo", "bar", "123", "1232", "7", "true");
    }

    @Test
    public void happyAndEmpty()
    {
        test("a,foo,bar,123,1232,,,7,true,,", "a", "foo", "bar", "123", "1232", "", "", "7", "true", "", "");
    }

    @Test
    public void empty()
    {
        test("", "");
        test(" ", " ");
    }

    @Test
    public void col1()
    {
        test("a", "a");
        test("ab", "ab");
        test("abc", "abc");
        test("abc def", "abc def");
        test("foobar", "foobar");
    }

    @Test
    public void emptyStart()
    {
        test(",a", "", "a");
        test(",a,b", "", "a", "b");
    }

    @Test
    public void emptyEnd()
    {
        test("a,", "a", "");

        test("a,b,", "a", "b", "");
        test("aaa,bbb,", "aaa", "bbb", "");
    }

    @Test
    public void col2()
    {
        test("a,b", "a", "b");
        test("aa,bb", "aa", "bb");
    }

    @Test
    public void emptyCols()
    {
        test("a,,,b,", "a", "", "", "b", "");
        test("a,,b", "a", "", "b");
        test("a,,,b", "a", "", "", "b");
        test(",", "", "");
    }

}
