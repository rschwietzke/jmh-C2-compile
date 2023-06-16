# Hotspot Compile Issue for While-Loops

## The Problem

We updated our load test software and especially worked on the runtime of the report generator. While testing it before release, we noticed very unstable runtimes. A fixed data set sees runtimes between 5 to 14 min. The 5 min runtime is the expected one, while higher runtimes only occur occasionally. These range from 6 to 14 min in about 10 to 15% of the cases.

The report generator is multi-threaded and reads about 7 GB of compressed CSV data (56 GB uncompressed). This is a total of 722,176,188 lines. Because of the multi-threaded model, the order of things is always a slightly different when processing the data.

## First Diagnostics

### Async Profiler

When we caught a process in the act of being slow, we connected Async Profiler and got some traces. These are 60 sec captures and they show clearly a shift towards the CSV parser.

*Good*

```
         ns  percent  samples  top
  ----------  -------  -------  ---
 45612243950    9.55%     4559  com.xceptance.common.util.CsvUtilsDecode.parse
 31634884015    6.62%     3162  itable stub
 25989872875    5.44%     2589  /usr/lib/x86_64-linux-gnu/libz.so.1.2.11
 23561485986    4.93%     2355  java.util.regex.Pattern$Slice.match
 21140760305    4.42%     2113  vtable stub
 20569614899    4.30%     2056  java.util.regex.Pattern$BmpCharProperty.match
 19321839309    4.04%     1931  jdk.internal.util.ArraysSupport.mismatch
 18011700149    3.77%     1800  com.xceptance.xlt.api.util.XltCharBuffer.hashCode
```

*Bad*

```
       ns  percent  samples  top
  ----------  -------  -------  ---
384925806756   79.68%    38490  com.xceptance.common.util.CsvUtilsDecode.parse
  5582345351    1.16%      558  com.xceptance.xlt.api.util.XltCharBuffer.hashCode
  5393452865    1.12%      537  /usr/lib/x86_64-linux-gnu/libz.so.1.2.11
  5132247709    1.06%      513  itable stub
  4932415970    1.02%      493  java.util.regex.Pattern$Slice.match
  4400943650    0.91%      440  com.xceptance.xlt.api.util.XltCharBuffer.viewFromTo
  4381854811    0.91%      438  java.util.regex.Pattern$BmpCharProperty.match
  3881428022    0.80%      388  vtable stub
```

### PrintCompilation

Because it seems that the compiled code differs occasionally, we look at the compilation for that very method. This screenshot shows four different captures (displayed using JITWatch).

![JITWatch compile stages](/assets/jitwatch-compile.png)

## Theory

## Summary

Running this with GraalVM 22.3-19 produces even worse data. The runtime difference is huge now. We talk about 2600 ns/op now instead of 1000 ns/op for OpenJDK 17. JDK 21 EA 25 is also worse with 1950 ns/ops.

## Data

For this test, we use three lines of CSV data. The long versions are displayed in a shorted version here, to highlight the difference. You find the long version in the source code.

* SHORT: `T,TFlashCheckout,1666958662310,17729,false,,,,`
* LONG Unquoted: `R,CandleDaySalesPage.2,1666954266805,95,false,1349,429,200,https://<huge url here>,image/gif,0,0,95,0,95,95,,GET,,,0,,`
* LONG Quoted: `R,CandleDaySalesPage.2,1666954266805,95,false,1349,429,200,"https://<huge url here>",image/gif,0,0,95,0,95,95,,GET,,,0,,`

As you can see, the long version only differs in one spot - additional quotes around the URL, because it might contains commas. The parsing will change here and inline remove the quotes while parsing the data. The entire parsing is optimized towards low or no allocation, because we have to parse millions of these lines. This is also the reason while we are not running `String` parsing here, but access a custom char array which later returns views on that backing array instead of providing copies.

## Measurements and Results

All measurements have been taken on a Google Cloud c2-standard-8 instance. Similar data has been seen on a Lenovo T14s AMD.

### Short - 03a

This is the runtime of a short

```
# JMH version: 1.36
# VM version: JDK 17.0.7, OpenJDK 64-Bit Server VM, 17.0.7+7
# VM invoker: /home/r_schwietzke/.sdkman/candidates/java/17.0.7-tem/bin/java
# VM options: -Xms2g -Xmx2g -XX:+UseSerialGC -XX:+AlwaysPreTouch -XX:+UseSerialGC
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 8 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: org.xceptance.B03a_ShortWarmupAndTest.parse

# Run progress: 0.00% complete, ETA 00:00:22
# Fork: 1 of 1
# Warmup Iteration   1: 72.160 ns/op
# Warmup Iteration   2: 73.073 ns/op
# Warmup Iteration   3: 70.650 ns/op
Iteration   1: 70.518 ns/op
Iteration   2: 70.545 ns/op
Iteration   3: 70.526 ns/op
Iteration   4: 70.582 ns/op
Iteration   5: 70.555 ns/op
Iteration   6: 70.566 ns/op
Iteration   7: 70.534 ns/op
Iteration   8: 70.496 ns/op
```

### Unquoted - 03b
```
# JMH version: 1.36
# VM version: JDK 17.0.7, OpenJDK 64-Bit Server VM, 17.0.7+7
# VM invoker: /home/r_schwietzke/.sdkman/candidates/java/17.0.7-tem/bin/java
# VM options: -Xms2g -Xmx2g -XX:+UseSerialGC -XX:+AlwaysPreTouch -XX:+UseSerialGC
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 8 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: org.xceptance.B03b_UnquotedWarmupAndTest.parse

# Run progress: 0.00% complete, ETA 00:00:22
# Fork: 1 of 1
# Warmup Iteration   1: 437.233 ns/op
# Warmup Iteration   2: 453.090 ns/op
# Warmup Iteration   3: 447.492 ns/op
Iteration   1: 445.609 ns/op
Iteration   2: 446.047 ns/op
Iteration   3: 446.409 ns/op
Iteration   4: 448.784 ns/op
Iteration   5: 446.039 ns/op
Iteration   6: 445.666 ns/op
Iteration   7: 446.513 ns/op
Iteration   8: 446.051 ns/op
```

### Quoted - 03c
```
# JMH version: 1.36
# VM version: JDK 17.0.7, OpenJDK 64-Bit Server VM, 17.0.7+7
# VM invoker: /home/r_schwietzke/.sdkman/candidates/java/17.0.7-tem/bin/java
# VM options: -Xms2g -Xmx2g -XX:+UseSerialGC -XX:+AlwaysPreTouch -XX:+UseSerialGC
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 8 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: org.xceptance.B03c_QuotedWarmupAndTest.parse

# Run progress: 0.00% complete, ETA 00:00:22
# Fork: 1 of 1
# Warmup Iteration   1: 679.505 ns/op
# Warmup Iteration   2: 720.437 ns/op
# Warmup Iteration   3: 709.460 ns/op
Iteration   1: 709.459 ns/op
Iteration   2: 708.525 ns/op
Iteration   3: 709.155 ns/op
Iteration   4: 708.071 ns/op
Iteration   5: 706.513 ns/op
Iteration   6: 706.533 ns/op
Iteration   7: 707.932 ns/op
Iteration   8: 706.479 ns/op
```

### Train Unquoted, Run Quoted - 05c
We would expect about 700 to 710 ns/op based on our single data benchmark from before. We got 685 ns/op, basically slightly better than the standalone run. When checking all 15 forks of the test, we get about 675 ns/op best case, and 700 ns/op as worst-case.

```
# Benchmark: org.xceptance.B05c_UnquotedWarmupAndQuotedTest.parse

# Run progress: 0.00% complete, ETA 00:06:30
# Fork: 1 of 15
# Warmup Iteration   1: 508.500 ns/op
# Warmup Iteration   2: 451.894 ns/op
# Warmup Iteration   3: 447.117 ns/op
Iteration   1: 696.897 ns/op
Iteration   2: 686.440 ns/op
Iteration   3: 686.527 ns/op
Iteration   4: 686.171 ns/op
Iteration   5: 687.529 ns/op
Iteration   6: 686.502 ns/op
Iteration   7: 685.432 ns/op
Iteration   8: 685.129 ns/op
Iteration   9: 685.846 ns/op
Iteration  10: 687.054 ns/op
```

### Train Quoted, Run Unquoted - 06b
We would expect about 445 ns/op based on our single data benchmark from before. We got abut 1,000 ns/op and hence we are twice as slow. The compiler seem to sit on an incorrectly trained plan and does not give it up.

```
# Benchmark: org.xceptance.B06b_QuotedWarmupAndUnquotedTest.parse

# Run progress: 0.00% complete, ETA 00:06:30
# Fork: 1 of 15
# Warmup Iteration   1: 676.655 ns/op
# Warmup Iteration   2: 717.067 ns/op
# Warmup Iteration   3: 709.536 ns/op
Iteration   1: 1004.059 ns/op
Iteration   2: 1003.092 ns/op
Iteration   3: 1001.875 ns/op
Iteration   4: 1001.421 ns/op
Iteration   5: 1000.465 ns/op
Iteration   6: 1000.704 ns/op
Iteration   7: 1006.352 ns/op
Iteration   8: 999.966 ns/op
Iteration   9: 999.657 ns/op
Iteration  10: 1000.144 ns/op
```

### Train and Run with All Data - 08

```
# Benchmark: org.xceptance.B08_MixedWarmupAndMixedTest.parse

# Run progress: 0.00% complete, ETA 00:00:22
# Fork: 1 of 1
# Warmup Iteration   1: 568.374 ns/op
# Warmup Iteration   2: 582.044 ns/op
# Warmup Iteration   3: 571.633 ns/op
Iteration   1: 570.451 ns/op
Iteration   2: 571.065 ns/op
Iteration   3: 569.132 ns/op
Iteration   4: 572.941 ns/op
Iteration   5: 568.703 ns/op
Iteration   6: 569.357 ns/op
Iteration   7: 569.318 ns/op
Iteration   8: 569.239 ns/op
```

