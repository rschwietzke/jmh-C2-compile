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
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.xceptance.common.lang.XltCharBuffer;
import com.xceptance.common.util.CsvUtilsDecode_V1_Original;
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
public class B02_LoadCPU
{
    XltCharBuffer src;
    SimpleArrayList<XltCharBuffer> result;

    int THREADS = 8;

    @Setup
    public void setup() throws InterruptedException
    {
        result = new SimpleArrayList<>(50);
        src = XltCharBuffer.valueOf("T,TFlashCheckout,1666958662310,17729,false,,,,");

        var t = new Thread(() -> {
            while (System.currentTimeMillis() > 10000)
            {
                Blackhole.consumeCPU(5120);
            }
        });
        t.setDaemon(true);
        t.start();

        // let them start
        Thread.sleep(1000);
    }

    @Benchmark
    @Threads(8)
    public SimpleArrayList<XltCharBuffer> parse()
    {
        result.clear();
        var x = CsvUtilsDecode_V1_Original.parse(result, src, ',');

        return x;
    }
}
