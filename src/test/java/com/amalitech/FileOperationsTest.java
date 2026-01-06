
package com.amalitech;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class FileOperationsTest {

    @Test
    public void nio2_reading_various_sizes() throws IOException {
        Path tmp = Files.createTempFile("io_small", ".txt");
        Files.writeString(tmp, "Hello\nWorld\n", StandardCharsets.UTF_8);

        List<String> lines = Files.readAllLines(tmp, StandardCharsets.UTF_8);
        assertEquals(2, lines.size());

        Files.deleteIfExists(tmp);
    }

    @Test
    public void streaming_vs_loading_entire_file_time() throws IOException {
        Path tmp = Files.createTempFile("io_large", ".txt");
        String payload = IntStream.range(0, 100_000).mapToObj(i -> "line-" + i).collect(Collectors.joining("\n"));
        Files.writeString(tmp, payload, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);

        long t0 = System.nanoTime();
        try (java.util.stream.Stream<String> s = Files.lines(tmp, StandardCharsets.UTF_8)) {
            long count = s.count();
            assertTrue(count >= 100_000);
        }
        long streamNs = System.nanoTime() - t0;

        t0 = System.nanoTime();
        List<String> all = Files.readAllLines(tmp, StandardCharsets.UTF_8);
        long loadNs = System.nanoTime() - t0;
        assertEquals(100_000, all.size());

        // Streaming should be competitive; allow generous thresholds
        assertTrue("stream=" + streamNs + " load=" + loadNs, streamNs <= loadNs * 2);

        Files.deleteIfExists(tmp);
    }


    @Test
    public void utf8_encoding_write_read() throws IOException {
        Path tmp = Files.createTempFile("io_utf8", ".txt");
        String gh = "Akwaaba – Ɔdadeɛ – Kumasi – yɛn dɔ"; // UTF-8 content
        Files.writeString(tmp, gh, StandardCharsets.UTF_8);

        String readBack = Files.readString(tmp, StandardCharsets.UTF_8);
        assertEquals(gh, readBack);

        Files.deleteIfExists(tmp);
    }
}
