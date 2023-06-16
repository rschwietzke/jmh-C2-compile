# Hotspot Compile Issue for While-Loops
![FyWyf3JWIAAH0iq](https://github.com/rschwietzke/jmh-C2-compile/assets/1793856/6a9452e5-ee54-4155-b03f-d9ab80ddc4dc)

## The Problem

## Theory

## Summary

## Measurements and Results

### Short - 03a

```
# Benchmark: org.xceptance.B03a_ShortWarmupAndTest.parse

# Run progress: 0.00% complete, ETA 00:00:22
# Fork: 1 of 1
# Warmup Iteration   1: 72.308 ns/op
# Warmup Iteration   2: 71.459 ns/op
# Warmup Iteration   3: 70.666 ns/op
Iteration   1: 70.674 ns/op
Iteration   2: 70.716 ns/op
Iteration   3: 70.692 ns/op
Iteration   4: 70.748 ns/op
Iteration   5: 70.735 ns/op
Iteration   6: 70.626 ns/op
Iteration   7: 70.622 ns/op
Iteration   8: 70.682 ns/op
```

### Unquoted - 03b
```
# Benchmark: org.xceptance.B03b_UnquotedWarmupAndTest.parse

# Run progress: 0.00% complete, ETA 00:00:22
# Fork: 1 of 1
# Warmup Iteration   1: 437.093 ns/op
# Warmup Iteration   2: 451.833 ns/op
# Warmup Iteration   3: 448.237 ns/op
Iteration   1: 445.821 ns/op
Iteration   2: 445.459 ns/op
Iteration   3: 445.740 ns/op
Iteration   4: 445.826 ns/op
Iteration   5: 445.890 ns/op
Iteration   6: 445.433 ns/op
Iteration   7: 445.820 ns/op
Iteration   8: 446.090 ns/op
```

### Quoted - 03c
```
# Benchmark: org.xceptance.B03c_QuotedWarmupAndTest.parse

# Run progress: 0.00% complete, ETA 00:00:22
# Fork: 1 of 1
# Warmup Iteration   1: 624.110 ns/op
# Warmup Iteration   2: 715.606 ns/op
# Warmup Iteration   3: 708.387 ns/op
Iteration   1: 709.841 ns/op
Iteration   2: 707.189 ns/op
Iteration   3: 705.774 ns/op
Iteration   4: 705.159 ns/op
Iteration   5: 706.001 ns/op
Iteration   6: 705.795 ns/op
Iteration   7: 705.947 ns/op
Iteration   8: 705.302 ns/op
```

### Train Unquoted, Run Quoted - 05c
We would expect about 700 to 710 ns/op based on our single data benchmark from before. We got 685 ns/op, basically slightly better than the standalone run. When checking all 15 forks of the test, we get about 675 ns/op as best case, and 700 ns/op as worst-case.

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

