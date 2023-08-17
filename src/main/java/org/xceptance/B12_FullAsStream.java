package org.xceptance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import com.xceptance.common.lang.XltCharBuffer;
import com.xceptance.common.util.CsvLineDecoder;
import com.xceptance.common.util.CsvLineDecoder2;
import com.xceptance.common.util.CsvLineDecoder3;
import com.xceptance.common.util.CsvUtilsDecode;
import com.xceptance.common.util.CsvUtilsDecodeV2;
import com.xceptance.common.util.CsvUtilsDecodeV3;
import com.xceptance.common.util.SimpleArrayList;

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
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Threads(4)
@Fork(10)
public class B12_FullAsStream
{
    List<XltCharBuffer> data = new ArrayList<>(1000);

    @Setup
    public void setup() throws IOException
    {
        var temp = new ArrayList<XltCharBuffer>(1000);

        try (var reader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("full-log.csv"))))
        {
            reader.lines().forEach(l -> {
                temp.add(XltCharBuffer.valueOf(l));
            });
        }

        for (int i = 0; i < 5; i++)
        {
            temp.forEach(l -> data.add(XltCharBuffer.valueOf(l.toCharArray())));
        }

        // break the cache patterns of ordered data in memory
        Collections.shuffle(data, new Random(42L));
    }

    @Benchmark
    public int parseV1()
    {
        var count = 0;
        var result = new SimpleArrayList<XltCharBuffer>(50);

        for (int i = 0; i < data.size(); i++)
        {
            result.clear();
            var r = CsvUtilsDecode.parse(result, data.get(i), ',');
            count += r.size();
        }

        return count;
    }

    @Benchmark
    public int parseV2()
    {
        var count = 0;
        var result = new SimpleArrayList<XltCharBuffer>(50);

        for (int i = 0; i < data.size(); i++)
        {
            result.clear();
            var r = CsvUtilsDecodeV2.parse(result, data.get(i), ',');
            count += r.size();
        }

        return count;
    }

    @Benchmark
    public int parseV3()
    {
        var count = 0;
        var result = new SimpleArrayList<XltCharBuffer>(50);

        for (int i = 0; i < data.size(); i++)
        {
            result.clear();
            var r = CsvUtilsDecodeV3.parse(result, data.get(i), ',');
            count += r.size();
        }

        return count;
    }

    @Benchmark
    public int parseV4()
    {
        var count = 0;
        var result = new SimpleArrayList<XltCharBuffer>(50);

        for (int i = 0; i < data.size(); i++)
        {
            result.clear();
            var r = CsvLineDecoder.parse(result, data.get(i), ',');
            count += r.size();
        }

        return count;
    }

    @Benchmark
    public int parseV5()
    {
        var count = 0;
        var result = new SimpleArrayList<XltCharBuffer>(50);

        for (int i = 0; i < data.size(); i++)
        {
            result.clear();
            var r = CsvLineDecoder2.parse(result, data.get(i), ',');
            count += r.size();
        }

        return count;
    }
    @Benchmark
    public int parseV6()
    {
        var count = 0;
        var result = new SimpleArrayList<XltCharBuffer>(50);

        for (int i = 0; i < data.size(); i++)
        {
            result.clear();
            var r = CsvLineDecoder3.parse(result, data.get(i));
            count += r.size();
        }

        return count;
    }
}
