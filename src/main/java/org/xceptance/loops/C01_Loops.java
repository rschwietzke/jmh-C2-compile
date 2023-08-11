package org.xceptance.loops;

import java.util.ArrayList;
import java.util.List;
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
import com.xceptance.common.util.SimpleArrayList;

/**
 * Trying different loops
 * @author Rene Schwietzke <r.schwietzke@xceptance.com>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class C01_Loops
{
    XltCharBuffer src;

    XltCharBuffer[] resultArray;

    SimpleArrayList<XltCharBuffer> resultSimpleListVeryShort;
    SimpleArrayList<XltCharBuffer> resultSimpleListShort;
    SimpleArrayList<XltCharBuffer> resultSimpleListLong;

    List<XltCharBuffer> resultListVeryShort;
    List<XltCharBuffer> resultListShort;
    List<XltCharBuffer> resultListLong;

    ArrayList<XltCharBuffer> resultArrayListVeryShort;
    ArrayList<XltCharBuffer> resultArrayListShort;
    ArrayList<XltCharBuffer> resultArrayListLong;

    final String SHORT = "T,TFlashCheckout,1666958662310,17729,false,,,,";
    final String LONG = "R,CandleDaySalesPage.2,1666954266805,95,false,1349,429,200,https://production-test.justacmecompany.com/on/dishwasher.store/Sites-justacmecompany-Site/en_US/__Analytics-Start?url=https%3A%2F%2Fproduction-test.justacmecompany.com%2Fs%2Fjustacmecompany%2Fc%2Fhome-smellstuff%2Fworkhelp4life&res=1600x1200&cookie=1&cmpn=&java=0&gears=0&fla=0&ag=0&dir=0&pct=0&pdf=0&qt=0&realp=0&tz=US%2FEastern&wma=1&pcat=new-arrivals&title=3-Wick+Scented+Candles+-+Swim+%26+Swamp+Tier&dwac=0.7629667259452815&r=2905563956785988054&ref=https%3A%2F%2Fproduction-test.justacmecompany.com%2F&data=givemesomedatathatjustfillshere,image/gif,0,0,95,0,95,95,,GET,,,0,,";

    @Setup(Level.Iteration)
    public void setup(BenchmarkParams params) throws InterruptedException
    {
        src = XltCharBuffer.valueOf(LONG);

        resultSimpleListVeryShort= new SimpleArrayList<XltCharBuffer>(1);
        resultSimpleListShort= new SimpleArrayList<XltCharBuffer>(10);
        resultSimpleListLong = new SimpleArrayList<XltCharBuffer>(40);

        resultListVeryShort = new ArrayList<XltCharBuffer>(1);
        resultListShort = new ArrayList<XltCharBuffer>(10);
        resultListLong = new ArrayList<XltCharBuffer>(40);

        resultArrayListVeryShort = new ArrayList<XltCharBuffer>(1);
        resultArrayListShort = new ArrayList<XltCharBuffer>(10);
        resultArrayListLong = new ArrayList<XltCharBuffer>(40);

        resultArray = new XltCharBuffer[40];
    }

    @Benchmark
    public int countXCB()
    {
        var x = Loops.l1_XCB_count(src, ',');
        return x;
    }

    @Benchmark
    public SimpleArrayList<XltCharBuffer> simpleListVeryShortXCB()
    {
        resultSimpleListVeryShort.clear();
        var x = Loops.l1_XCB_simpleList(resultSimpleListVeryShort, src, ',');

        return x;
    }

    @Benchmark
    public SimpleArrayList<XltCharBuffer> simpleListShortXCB()
    {
        resultSimpleListShort.clear();
        var x = Loops.l1_XCB_simpleList(resultSimpleListShort, src, ',');

        return x;
    }

    @Benchmark
    public SimpleArrayList<XltCharBuffer> simpleListLongXCB()
    {
        resultSimpleListLong.clear();
        var x = Loops.l1_XCB_simpleList(resultSimpleListLong, src, ',');

        return x;
    }

    @Benchmark
    public List<XltCharBuffer> listVeryShortXCB()
    {
        resultListVeryShort.clear();
        var x = Loops.l1_XCB_list(resultListVeryShort, src, ',');

        return x;
    }
    @Benchmark
    public List<XltCharBuffer> listShortXCB()
    {
        resultListShort.clear();
        var x = Loops.l1_XCB_list(resultListShort, src, ',');

        return x;
    }
    @Benchmark
    public List<XltCharBuffer> listLongXCB()
    {
        resultListLong.clear();
        var x = Loops.l1_XCB_list(resultListLong, src, ',');

        return x;
    }

    @Benchmark
    public ArrayList<XltCharBuffer> arrayListVeryShortXCB()
    {
        resultArrayListVeryShort.clear();
        var x = Loops.l1_XCB_arrayList(resultArrayListVeryShort, src, ',');

        return x;
    }
    @Benchmark
    public ArrayList<XltCharBuffer> arrayListShortXCB()
    {
        resultArrayListShort.clear();
        var x = Loops.l1_XCB_arrayList(resultArrayListShort, src, ',');

        return x;
    }
    @Benchmark
    public ArrayList<XltCharBuffer> arrayListLongXCB()
    {
        resultArrayListLong.clear();
        var x = Loops.l1_XCB_arrayList(resultArrayListLong, src, ',');

        return x;
    }

    @Benchmark
    public XltCharBuffer[] arrayXCB()
    {
        var x = Loops.l1_XCB_array(resultArray, src, ',');

        return x;
    }
}
