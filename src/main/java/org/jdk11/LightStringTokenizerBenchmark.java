package org.jdk11;

import java.text.MessageFormat;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.misc.LightStringTokenizer;
import org.misc.LightStringTokenizerCharArray;
import org.misc.LightStringTokenizerCharDelimiter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * This code is meant to discuss the performance change between JDK 8 and 11.
 * It uses a copy of the JDK 11 StringTokenizer to ensure that this is not the
 * cause.
 * 
 * The LightStringTokenizer is a legacy class written years ago (2002).
 * It was written because it was faster back in the days and also reports
 * empty tokens between consecutive delimiters. But for our demo, this is not 
 * the important feature.
 * 
 * @author Rene Schwietzke <r.schwietzke@xceptance.com>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class LightStringTokenizerBenchmark
{
    // the problem does not depend on the size of the test data, so
    // 10 or 100 is fine
    final int SIZE = 10;
    String[] src;
    
    @Setup
    public void setup()
    {
        // just create fancy IPs for later conversion to an int
        src = new String[SIZE];

        final Random r = new Random(1L);
        for (int i = 0; i < SIZE; i++)
        {
            src[i] = MessageFormat.format("{0}.{1}.{2}.{3}", 
                            r.nextInt(256), 
                            r.nextInt(256),
                            r.nextInt(256),
                            r.nextInt(256)); 
        }
    }
    
    private static int OCTET1 = 256 * 256 * 256;
    private static int OCTET2 = 256 * 256;
    private static int OCTET3 = 256;
    
    /**
     * The code using the JDK11 StringTokenizer.
     * 
     * @param ipv4
     * @return
     */
    private int convertIPV4_02a_StringTokenizer(final String ipv4)
    {
        final JDK11StringTokenizer groups = new JDK11StringTokenizer(ipv4, ".");
        final int[] octets = new int[4];

        int count = 0;
        while (groups.hasMoreTokens())
        {
            final String token = groups.nextToken();
            octets[count] = Integer.valueOf(token);
            count++;
        }
        
        int sum = OCTET1 * octets[0];
        sum += OCTET2 * octets[1];
        sum += OCTET3 * octets[2];
        sum += octets[3];
        
        return sum;
    }

    /**
     * Ok, we use our legacy tokenizer here that was once faster or at least at par
     * with the StringTokenizer.
     * 
     * @param ipv4
     * @return
     */
    private int convertIPV4_03a_LightTokenizer(final String ipv4)
    {
        final LightStringTokenizer groups = new LightStringTokenizer(ipv4, ".");
        final int[] octets = new int[4];
        
        int count = 0;
        while (groups.hasMoreTokens())
        {
            final String token = groups.nextToken();
            octets[count] = Integer.valueOf(token);
            count++;
        }
        
        int sum = OCTET1 * octets[0];
        sum += OCTET2 * octets[1];
        sum += OCTET3 * octets[2];
        sum += octets[3];
        
        return sum;
    }

    /**
     * Just heavily optimized the implementation by avoid String::indexOf
     * 
     * @param ipv4
     * @return
     */
    private int convertIPV4_03b_LightTokenizerArray(final String ipv4)
    {
        final LightStringTokenizerCharArray groups = new LightStringTokenizerCharArray(ipv4, '.');
        final int[] octets = new int[4];
        
        int count = 0;
        while (groups.hasMoreTokens())
        {
            final String token = groups.nextToken();
            octets[count] = Integer.valueOf(token);
            count++;
        }
        
        int sum = OCTET1 * octets[0];
        sum += OCTET2 * octets[1];
        sum += OCTET3 * octets[2];
        sum += octets[3];
        
        return sum;
    }

    /**
     * This uses an optimized tokenizer that is similar to the legacy version but
     * uses a char instead of String as delimiter.
     * 
     * @param ipv4
     * @return
     */
    private int convertIPV4_03c_LightTokenizerCharDelimiter(final String ipv4)
    {
        final LightStringTokenizerCharDelimiter groups = new LightStringTokenizerCharDelimiter(ipv4, '.');
        final int[] octets = new int[4];
        
        int count = 0;
        while (groups.hasMoreTokens())
        {
            final String token = groups.nextToken();
            octets[count] = Integer.valueOf(token);
            count++;
        }
        
        int sum = OCTET1 * octets[0];
        sum += OCTET2 * octets[1];
        sum += OCTET3 * octets[2];
        sum += octets[3];
        
        return sum;
    }
    
    @Benchmark
    public int cpu_02a_StringTokenizer()
    {
        int sum = 0;

        for (int i = 0; i < SIZE; i++)
        {
            sum += convertIPV4_02a_StringTokenizer(src[i]);
        }

        return sum;
    }

    @Benchmark
    public int cpu_03a_LightTokenizer()
    {
        int sum = 0;
        
        for (int i = 0; i < SIZE; i++)
        {
            sum += convertIPV4_03a_LightTokenizer(src[i]);
        }

        return sum;
    }

    @Benchmark
    public int cpu_03b_LightTokenizerArray()
    {
        int sum = 0;
        
        for (int i = 0; i < SIZE; i++)
        {
            sum += convertIPV4_03b_LightTokenizerArray(src[i]);
        }

        return sum;
    }    

    @Benchmark
    public int cpu_03c_LightTokenizerCharDelimiter()
    {
        int sum = 0;
        
        for (int i = 0; i < SIZE; i++)
        {
            sum += convertIPV4_03c_LightTokenizerCharDelimiter(src[i]);
        }

        return sum;
    }  
    
    /**
     * Just a litte test code to ensure we have not messed up the method under test
     */
    @Test
    public void test()
    {
        Assert.assertEquals(783771400, convertIPV4_02a_StringTokenizer("46.183.103.8"));
        Assert.assertEquals(783771400, convertIPV4_03a_LightTokenizer("46.183.103.8"));
        Assert.assertEquals(783771400, convertIPV4_03b_LightTokenizerArray("46.183.103.8"));
        Assert.assertEquals(783771400, convertIPV4_03c_LightTokenizerCharDelimiter("46.183.103.8"));
    }
}
