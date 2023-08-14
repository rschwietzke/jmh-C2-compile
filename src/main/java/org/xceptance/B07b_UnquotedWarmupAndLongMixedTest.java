package org.xceptance;

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
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.BenchmarkParams;

import com.xceptance.common.lang.XltCharBuffer;
import com.xceptance.common.util.CsvLineDecoder;
import com.xceptance.common.util.CsvLineDecoder2;
import com.xceptance.common.util.CsvLineDecoder3;
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
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class B07b_UnquotedWarmupAndLongMixedTest
{
    int iterationCount = 0;

    XltCharBuffer[] data;
    SimpleArrayList<XltCharBuffer> result;
    static FastRandom r = new FastRandom(345111L);

    final String SHORT = "T,TFlashCheckout,1666958662310,17729,false,,,,";
    final String LONG = "R,CandleDaySalesPage.2,1666954266805,95,false,1349,429,200,https://production-test.justacmecompany.com/on/dishwasher.store/Sites-justacmecompany-Site/en_US/__Analytics-Start?url=https%3A%2F%2Fproduction-test.justacmecompany.com%2Fs%2Fjustacmecompany%2Fc%2Fhome-smellstuff%2Fworkhelp4life&res=1600x1200&cookie=1&cmpn=&java=0&gears=0&fla=0&ag=0&dir=0&pct=0&pdf=0&qt=0&realp=0&tz=US%2FEastern&wma=1&pcat=new-arrivals&title=3-Wick+Scented+Candles+-+Swim+%26+Swamp+Tier&dwac=0.7629667259452815&r=2905563956785988054&ref=https%3A%2F%2Fproduction-test.justacmecompany.com%2F&data=givemesomedatathatjustfillshere,image/gif,0,0,95,0,95,95,,GET,,,0,,";
    final String LONGQUOTED  = "R,CandleDaySalesPage.2,1666954266805,95,false,1349,429,200,\"https://production-test.justacmecompany.com/on/dishwasher.store/Sites-justacmecompany-Site/en_US/__Analytics-Start?url=https%3A%2F%2Fproduction-test.justacmecompany.com%2Fs%2Fjustacmecompany%2Fc%2Fhome-smellstuff%2Fworkhelp4life&res=1600x1200&cookie=1&cmpn=&java=0&gears=0&fla=0&ag=0&dir=0&pct=0&pdf=0&qt=0&realp=0&tz=US%2FEastern&wma=1&pcat=new-arrivals&title=3-Wick+Scented+Candles+-+Swim+%26+Swamp+Tier&dwac=0.7629667259452815&r=2905563956785988054&ref=https%3A%2F%2Fproduction-test.justacmecompany.com%2F&data=givemesomedatathatjustfillshere\",image/gif,0,0,95,0,95,95,,GET,,,0,,";

    XltCharBuffer[] DATASHORT =
                {
                        XltCharBuffer.valueOf(LONG),
                };
    XltCharBuffer[] DATALONG =
                {
                        XltCharBuffer.valueOf(LONG),
                        XltCharBuffer.valueOf(LONGQUOTED)
                };

    @Setup(Level.Iteration)
    public void setup(BenchmarkParams params)
    {
        iterationCount++;

        result = new SimpleArrayList<>(50);

        if (iterationCount <= params.getWarmup().getCount())
        {
            data = DATASHORT;
        }
        else
        {
            data = DATALONG;
        }
    }

    @Benchmark
    public SimpleArrayList<XltCharBuffer> parse()
    {
        result.clear();
        var x = CsvUtilsDecode.parse(result, data[r.nextInt(data.length)], ',');

        return x;
    }

    @Benchmark
    public SimpleArrayList<XltCharBuffer> parseV2()
    {
        result.clear();
        var x = CsvUtilsDecodeV2.parse(result, data[r.nextInt(data.length)], ',');

        return x;
    }

    @Benchmark
    public SimpleArrayList<XltCharBuffer> parseV4()
    {
        result.clear();
        var x = CsvLineDecoder.parse(result, data[r.nextInt(data.length)], ',');

        return x;
    }

    @Benchmark
    public SimpleArrayList<XltCharBuffer> parseV5()
    {
        result.clear();
        var x = CsvLineDecoder2.parse(result, data[r.nextInt(data.length)], ',');

        return x;
    }

    @Benchmark
    public SimpleArrayList<XltCharBuffer> parseV6()
    {
        result.clear();
        var x = CsvLineDecoder3.parse(result, data[r.nextInt(data.length)]);

        return x;
    }
}
