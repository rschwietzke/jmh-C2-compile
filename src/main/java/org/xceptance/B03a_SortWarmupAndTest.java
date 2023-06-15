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
import com.xceptance.common.util.CsvUtilsDecode;
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
@Measurement(iterations = 8, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class B03a_SortWarmupAndTest
{
    XltCharBuffer src;
    SimpleArrayList<XltCharBuffer> result;

    final String SHORT = "T,TFlashCheckout,1666958662310,17729,false,,,,";
    final String LONG = "R,CandleDaySalesPage.2,1666954266805,95,false,1349,429,200,https://production-test.bathandbodyworks.com/on/demandware.store/Sites-BathAndBodyWorks-Site/en_US/__Analytics-Start?url=https%3A%2F%2Fproduction-test.bathandbodyworks.com%2Fs%2FBathAndBodyWorks%2Fc%2Fhome-fragrance%2F3-wick-candles&res=1600x1200&cookie=1&cmpn=&java=0&gears=0&fla=0&ag=0&dir=0&pct=0&pdf=0&qt=0&realp=0&tz=US%2FEastern&wma=1&pcat=new-arrivals&title=3-Wick+Scented+Candles+-+Bath+%26+Body+Works&dwac=0.7629667259452815&r=2905563956785988054&ref=https%3A%2F%2Fproduction-test.bathandbodyworks.com%2F&email=xc6001a1077812-60%40varmail.de,image/gif,0,0,95,0,95,95,,GET,,,0,,";
    final String LONGQUOTED  = "R,CandleDaySalesPage.2,1666954266805,95,false,1349,429,200,\"https://production-test.bathandbodyworks.com/on/demandware.store/Sites-BathAndBodyWorks-Site/en_US/__Analytics-Start?url=https%3A%2F%2Fproduction-test.bathandbodyworks.com%2Fs%2FBathAndBodyWorks%2Fc%2Fhome-fragrance%2F3-wick-candles&res=1600x1200&cookie=1&cmpn=&java=0&gears=0&fla=0&ag=0&dir=0&pct=0&pdf=0&qt=0&realp=0&tz=US%2FEastern&wma=1&pcat=new-arrivals&title=3-Wick+Scented+Candles+-+Bath+%26+Body+Works&dwac=0.7629667259452815&r=2905563956785988054&ref=https%3A%2F%2Fproduction-test.bathandbodyworks.com%2F&email=xc6001a1077812-60%40varmail.de\",image/gif,0,0,95,0,95,95,,GET,,,0,,";

    @Setup(Level.Iteration)
    public void setup(BenchmarkParams params) throws InterruptedException
    {
        result = new SimpleArrayList<>(10);
        src = XltCharBuffer.valueOf(SHORT);
    }

    @Benchmark
    public SimpleArrayList<XltCharBuffer> parse()
    {
        result.clear();
        var x = CsvUtilsDecode.parse(result, src, ',');

        return x;
    }
}
