package org.xceptance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import com.xceptance.common.lang.XltCharBuffer;
import com.xceptance.common.util.CsvUtilsDecode;
import com.xceptance.common.util.CsvUtilsDecodeV2;
import com.xceptance.common.util.SimpleArrayList;
import com.xceptance.misc.FastRandom;

/**
 * Let's try to reproduce a C2 compile problem. This is version 1
 * where we keep things extremely simple in regards to data and usage.
 *
 * @author Rene Schwietzke <r.schwietzke@xceptance.com>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class B11_FullFileTest
{
    static FastRandom r = new FastRandom();
    BufferedReader reader;

    @Setup(Level.Invocation)
    public void setup()
    {
        reader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("full-log.csv")));
    }

    @TearDown(Level.Invocation)
    public void teardon() throws IOException
    {
        reader.close();
    }

    @Benchmark
    public int parse() throws IOException
    {
        int count = 0;

        var result = new SimpleArrayList<XltCharBuffer>(10);
        String line = null;
        while ((line = reader.readLine()) != null)
        {
            result.clear();
            var r = CsvUtilsDecode.parse(result, XltCharBuffer.valueOf(line), ',');
            count += r.size();
        }

        return count;
    }

    @Benchmark
    public int parseV2() throws IOException
    {
        int count = 0;

        var result = new SimpleArrayList<XltCharBuffer>(10);
        String line = null;
        while ((line = reader.readLine()) != null)
        {
            result.clear();
            var r = CsvUtilsDecodeV2.parse(result, XltCharBuffer.valueOf(line), ',');
            count += r.size();
        }

        return count;
    }
}
