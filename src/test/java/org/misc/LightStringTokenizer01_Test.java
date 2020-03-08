package org.misc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the correct function of the LightStringTokenizer class.
 *
 * @author Rene Schwietzke
 * @version 1.2.0.0.12 2002/07/06
 * 
 * @see com.xceptance.sitereports.util.LightStringTokenizer
 */
public class LightStringTokenizer01_Test
{

    /**
	 * Test 1
	 * 
	 * @see com.xceptance.sitereports.util.LightStringTokenizer#
	 */
    @Test 
	public void test_standardtokenize01()
	{
		String string    = "token1/token2/token3";
		String delimiter = "/";
		
		LightStringTokenizer lst = new LightStringTokenizer(string, delimiter);
		
		assertEquals("token1", lst.nextToken());
		assertEquals("token2", lst.nextToken());
		assertEquals("token3", lst.nextToken());

		assertTrue(!lst.hasMoreTokens());
	}

	/**
	 * Test 2
	 * 
	 * @see com.xceptance.sitereports.util.LightStringTokenizer#
	 */
    @Test 
	public void test_standardtokenize02()
	{
		String string    = "/token1/token2/token3";
		String delimiter = "/";
		
		LightStringTokenizer lst = new LightStringTokenizer(string, delimiter);
		
		assertEquals("", lst.nextToken());
		assertEquals("token1", lst.nextToken());
		assertEquals("token2", lst.nextToken());
		assertEquals("token3", lst.nextToken());

		assertTrue(!lst.hasMoreTokens());
	}

	/**
	 * Test 3
	 * 
	 * @see com.xceptance.sitereports.util.LightStringTokenizer#
	 */
    @Test 
	public void test_standardtokenize03()
	{
		String string    = "/token1/token2/token3/";
		String delimiter = "/";
		
		LightStringTokenizer lst = new LightStringTokenizer(string, delimiter);
		
		assertEquals("", lst.nextToken());
		assertEquals("token1", lst.nextToken());
		assertEquals("token2", lst.nextToken());
		assertEquals("token3", lst.nextToken());

		assertTrue(!lst.hasMoreTokens());
	}

	/**
	 * Test 4
	 * 
	 * @see com.xceptance.sitereports.util.LightStringTokenizer#
	 */
    @Test 
	public void test_standardtokenize04()
	{
		String string1    = "/token1/token2/token3/token4/token5/token6//";
		String delimiter1 = "/";
		
		LightStringTokenizer lst = new LightStringTokenizer(string1, delimiter1);
		
		assertEquals("", lst.nextToken());
		assertEquals("token1", lst.nextToken());
		assertEquals("token2", lst.nextToken());
		assertEquals("token3", lst.nextToken());
		assertEquals("token4", lst.nextToken());
		assertEquals("token5", lst.nextToken());
		assertEquals("token6", lst.nextToken());
		assertEquals("", lst.nextToken());

		assertTrue(!lst.hasMoreTokens());
	}

	/**
	 * Test 5
	 * 
	 * @see com.xceptance.sitereports.util.LightStringTokenizer#
	 */
    @Test 
	public void test_standardtokenize05()
	{
		String string1    = "token/token/token/test/test/foobar";
		String delimiter1 = "/";
		
		LightStringTokenizer lst = new LightStringTokenizer(string1, delimiter1);
		
		assertEquals("token", lst.nextToken());
		assertEquals("token", lst.nextToken());
		assertEquals("token", lst.nextToken());
		assertEquals("test", lst.nextToken());
		assertEquals("test", lst.nextToken());
		assertEquals("foobar", lst.nextToken());

		assertTrue(!lst.hasMoreTokens());

		String string2    = "token:token:token:test:test:foobar";
		String delimiter2 = ":";
	}
	
	
	/**
	 * Test 7
	 * 
	 * @see com.xceptance.sitereports.util.LightStringTokenizer#
	 */
    @Test 
	public void test_standardtokenize07()
	{
		String string1    = "/";
		String delimiter1 = "/";
		
		LightStringTokenizer lst = new LightStringTokenizer(string1, delimiter1);
		
		assertEquals("", lst.nextToken());
		assertNull(lst.nextToken());

		assertTrue(!lst.hasMoreTokens());
	}

	/**
	 * Test 8
	 * 
	 * @see com.xceptance.sitereports.util.LightStringTokenizer#
	 */
    @Test 
	public void test_standardtokenize08()
	{
		String string1    = "test";
		String delimiter1 = ":";
		
		LightStringTokenizer lst = new LightStringTokenizer(string1, delimiter1);
		
		assertEquals("test", lst.nextToken());
		assertNull(lst.nextToken());

		assertTrue(!lst.hasMoreTokens());
	}
//    public static void main(String[] args)
//	{
//		SortMap01_Test t = new SortMap01_Test("Test");
//		t.test_sortMap4();
//		
//		return;
//	}
}