package org.xceptance.concurrent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import com.xceptance.common.lang.XltBufferedLineReader;
import com.xceptance.common.lang.XltCharBuffer;
import com.xceptance.common.util.CsvLineDecoder_V3_FixedDelimiter;
import com.xceptance.common.util.CsvParserException;
import com.xceptance.common.util.SimpleArrayList;

public class B99_FullFileTest
{
    public static final int BUFFER_SIZE = 10000;

    public static void main(String[] args) throws InterruptedException
    {
        if (args.length == 0)
        {
            System.err.println("<dir> <readerThread> <parserThreads> <randomSeed>");
            return;
        }
        System.out.printf("#####################################################%n");

        final int readerThreadCount = Integer.parseInt(args[1]);
        final int parserThreadCount = Integer.parseInt(args[2]);
        final int randomSeed = Integer.parseInt(args[3]);

        final int totalThreads = readerThreadCount + parserThreadCount;

        // get us all files
        final List<Path> files = getFiles(args[0]);
        System.out.printf("Shuffling input data with seed: %d%n", randomSeed);
        Collections.shuffle(files, new Random(randomSeed));

        final Dispatcher dispatcher = new Dispatcher(totalThreads);

        final List<Thread> parserThreads = new ArrayList<>();
        for (int i = 0; i < parserThreadCount; i++)
        {
            var t = new Thread(new Parser(dispatcher, i));
            parserThreads.add(t);
            t.start();
        }

        final int chunckSize = files.size() / readerThreadCount;
        System.out.printf("Found %d files for processsing%n", files.size());

        final List<Thread> readerThreads = new ArrayList<>();
        for (int i = 0; i < readerThreadCount; i++)
        {
            final int from = i * chunckSize;
            final int _to = i * chunckSize + chunckSize;
            int to = _to >= files.size() ? files.size() : _to;

            //System.out.printf("Reader %d with %d to %d%n", i, from, to);

            var t = new Thread(new Reader(dispatcher, i, files.subList(from, to)));
            readerThreads.add(t);
            t.start();
        }

        var start = System.currentTimeMillis();
        for (int i = 0; i < totalThreads; i++)
        {
            dispatcher.syncLatch.countDown();
        }

        // wait for reader
        for (var thread : readerThreads)
        {
            thread.join();
        }

        dispatcher.finish();

        // wait till threads finished
        for (var thread : parserThreads)
        {
            thread.join();
        }

        var end = System.currentTimeMillis();

        final long count = dispatcher.totalLines.get();

        var runtime = end - start;
        var linesPerMS = Math.round((double)count / (double)runtime);
        long linesPerS = Math.round( (double)count / ((double)runtime / 1000.0));

        System.out.printf("# Read %d lines in %d ms, seed %d%n", count, runtime, randomSeed);
        System.out.printf("# Average %d lines/s%n", linesPerS);
    }

    public static List<Path> getFiles(final String dir)
    {
        try (Stream<Path> stream = Files.walk(Paths.get(dir)))
        {
            return stream
              .filter(f -> !Files.isDirectory(f))
              .filter(f -> f.getFileName().endsWith("timers.csv.gz"))
              .collect(Collectors.toList());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    static class Reader implements Runnable
    {
        final List<Path> files;
        final Dispatcher dispatcher;

        final int number;

        public Reader(final Dispatcher dispatcher, final int number, final List<Path> files)
        {
            this.dispatcher = dispatcher;
            this.files = files;
            this.number = number;
        }

        @Override
        public void run()
        {
            try
            {
                dispatcher.syncLatch.await();
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // System.out.printf("Reader %d started%n", number);

            long count = 0;
            long last = 0;
            for (var file : files)
            {
                //System.out.print("# File: " + file.toString());

                XltCharBuffer line = null;
                XltBufferedLineReader reader = null;
                try
                {
                    if (file.getFileName().toString().endsWith(".gz"))
                    {
                        reader = new XltBufferedLineReader(
                                        new InputStreamReader(
                                                        new GZIPInputStream(Files.newInputStream(file))));
                    }
                    else
                    {
                        reader = new XltBufferedLineReader(new InputStreamReader(Files.newInputStream(file)));
                    }

                    int localLines = 0;
                    var lines = new ArrayList<XltCharBuffer>(BUFFER_SIZE);

                    while ((line = reader.readLine()) != null)
                    {
                        lines.add(line);
                        count++;
                        localLines++;

                        if (lines.size() >= BUFFER_SIZE)
                        {
                            dispatcher.add(lines);
                            lines = new ArrayList<XltCharBuffer>(BUFFER_SIZE);
                        }
                    }
                    // add remaining
                    dispatcher.add(lines);
                    //System.out.println("Read" + count);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if (reader != null)
                    {
                        try
                        {
                            reader.close();
                        }
                        catch (IOException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }

                long next = count / 10_000_000;
                if (last != next)
                {
                    System.out.printf("Reader %d read %d lines%n", number, count);
                    last = next;
                }
            }
        }
    }

    static class Parser implements Runnable
    {
        final Dispatcher dispatcher;
        final int number;

        public Parser(final Dispatcher dispatcher, final int number)
        {
            this.dispatcher = dispatcher;
            this.number = number;
        }

        public void run()
        {
            try
            {
                dispatcher.syncLatch.await();
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // System.out.printf("Parser %d started%n", number);

            int totalLines = 0;
            while (!dispatcher.hasFinished())
            {
                final var _lines = dispatcher.get();
                if (_lines.isEmpty())
                {
                    break;
                }

                // System.out.printf("[%d] Batch %d%n", number, ++batch);

                final var lines = _lines.get();

                var totalColumns = 0L;
                totalLines += lines.size();

                var result = new SimpleArrayList<XltCharBuffer>(50);

                for (int i = 0; i < lines.size(); i++)
                {
                    var line = lines.get(i);

                    result.clear();
                    try
                    {
                        var r = CsvLineDecoder_V3_FixedDelimiter.parse(result, line);
                        totalColumns += r.size();
                    }
                    catch (CsvParserException pe)
                    {
                        System.err.println(pe.getMessage());
                        System.err.println(pe.getIncorrectLine());
                        System.err.println();
                    }
                }

                dispatcher.update(lines.size(), totalColumns);
            }

            System.out.printf("Finished Parser %d with %d lines%n", number, totalLines);
        }
    }

    static class Dispatcher
    {
        final LinkedBlockingQueue<List<XltCharBuffer>> queue =  new LinkedBlockingQueue<>(100);
        final AtomicBoolean finished = new AtomicBoolean(false);

        public final AtomicLong totalLines = new AtomicLong();
        public final AtomicLong totalColumns = new AtomicLong();

        public final CountDownLatch syncLatch;

        public Dispatcher(final int threadCount)
        {
            syncLatch = new CountDownLatch(threadCount);
        }

        public void add(List<XltCharBuffer> data)
        {
            try
            {
                queue.put(data);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public Optional<List<XltCharBuffer>> get()
        {
            List<XltCharBuffer> list = null;
            do
            {
                try
                {
                    list = queue.poll(250, TimeUnit.MILLISECONDS);
                    if (list != null)
                    {
                        return Optional.of(list);
                    }
                }
                catch (InterruptedException e)
                {
                }
            }
            while (!hasFinished());

            return Optional.empty();
        }

        public boolean hasFinished()
        {
            return finished.get();
        }

        public void finish()
        {
            finished.set(true);
        }

        public void update(long lines, long columns)
        {
            totalLines.addAndGet(lines);
            totalColumns.addAndGet(columns);
        }
    }
}
