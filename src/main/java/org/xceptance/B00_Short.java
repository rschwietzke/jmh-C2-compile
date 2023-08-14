package org.xceptance;

import java.util.concurrent.TimeUnit;

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

import com.xceptance.common.lang.XltCharBuffer;
import com.xceptance.common.util.CsvLineDecoder;
import com.xceptance.common.util.CsvLineDecoder2;
import com.xceptance.common.util.CsvLineDecoder3;
import com.xceptance.common.util.CsvUtilsDecode;
import com.xceptance.common.util.CsvUtilsDecodeV2;
import com.xceptance.common.util.SimpleArrayList;

/**
 * Let's try to reproduce a C2 compile problem. This is version 1
 * where we keep things extremely simple in regards to data and usage.
 *
 * @author Rene Schwietzke <r.schwietzke@xceptance.com>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class B00_Short
{
    XltCharBuffer src;
    SimpleArrayList<XltCharBuffer> result;

    @Setup
    public void setup()
    {
        result = new SimpleArrayList<>(50);
        src = XltCharBuffer.valueOf("T,TFlashCheckout,1666958662310,17729,false,,,,");
    }

    @Benchmark
    public SimpleArrayList<XltCharBuffer> parse()
    {
        result.clear();
        var x = CsvUtilsDecode.parse(result, src, ',');

        return x;
    }

    @Benchmark
    public SimpleArrayList<XltCharBuffer> parseV2()
    {
        result.clear();
        var x = CsvUtilsDecodeV2.parse(result, src, ',');

        return x;
    }

    @Benchmark
    public SimpleArrayList<XltCharBuffer> parseV4()
    {
        result.clear();
        var x = CsvLineDecoder.parse(result, src, ',');

        return x;
    }

    @Benchmark
    public SimpleArrayList<XltCharBuffer> parseV5()
    {
        result.clear();
        var x = CsvLineDecoder2.parse(result, src, ',');

        return x;
    }

    @Benchmark
    public SimpleArrayList<XltCharBuffer> parseV6()
    {
        result.clear();
        var x = CsvLineDecoder3.parse(result, src);

        return x;
    }
}
