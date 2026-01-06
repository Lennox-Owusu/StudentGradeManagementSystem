
package com.amalitech;

import org.junit.Assume;
import org.junit.Test;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class StreamProcessingTest {

    @Test
    public void filter_map_reduce_operations() {
        List<Integer> data = IntStream.rangeClosed(1, 1000).boxed().toList();

        // keep evens, square them, sum
        int sumSquares = data.stream()
                .filter(x -> x % 2 == 0)
                .map(x -> x * x)
                .reduce(0, Integer::sum);

        // sanity check using math formula: 2^2 + 4^2 + ... + 1000^2
        int expected = IntStream.rangeClosed(1, 500).map(i -> (2 * i) * (2 * i)).sum();
        assertEquals(expected, sumSquares);
    }

    @Test
    public void parallel_stream_correctness() {
        List<Integer> data = IntStream.rangeClosed(1, 200_000).boxed().toList();
        long seqSum = data.stream().mapToLong(Integer::longValue).sum();
        long parSum = data.parallelStream().mapToLong(Integer::longValue).sum();
        assertEquals(seqSum, parSum);
    }

    @Test
    public void shortCircuit_findFirst() {
        List<String> words = List.of("alpha","beta","gamma","delta","epsilon");
        String firstStartingWithG = words.stream().filter(w -> w.startsWith("g")).findFirst().orElse(null);
        assertEquals("gamma", firstStartingWithG);
    }


}
