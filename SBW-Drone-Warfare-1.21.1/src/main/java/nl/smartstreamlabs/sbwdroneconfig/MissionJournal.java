package nl.smartstreamlabs.sbwdroneconfig;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Arrays;

/** Immutable byte snapshots only. One in-flight write and one coalesced pending snapshot. */
final class MissionJournal implements AutoCloseable {
    @FunctionalInterface interface Writer { void write(byte[] snapshot) throws IOException; }
    private final Writer writer;
    private Thread worker;
    private byte[] latest, pending;
    private long version, pendingVersion, durableVersion;
    private IOException failure;
    private boolean closed;

    MissionJournal(Path path) { this(bytes -> writeAtomic(path, bytes)); }
    MissionJournal(Writer writer) { this.writer = writer; }
    private synchronized long submit(byte[] bytes) throws IOException {
        if (failure != null) throw failure;
        if (closed) throw new IOException("Mission journal is closed");
        if (Arrays.equals(latest, bytes)) return version;
        latest = bytes.clone(); pending = latest; pendingVersion = ++version;
        if (worker == null) {
            worker = new Thread(this::drain, "Drone Warfare mission journal");
            worker.setDaemon(true); worker.start();
        }
        notifyAll(); return version;
    }
    void saveAsync(byte[] bytes) throws IOException { submit(bytes); }
    void saveAndWait(byte[] bytes) throws IOException {
        long required = submit(bytes);
        synchronized (this) {
            while (durableVersion < required && failure == null) {
                try { wait(); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); throw new IOException("Interrupted awaiting mission durability", e);
                }
            }
            if (failure != null) throw failure;
        }
    }
    synchronized IOException failure() { return failure; }
    private void drain() {
        for (;;) {
            byte[] bytes; long writing;
            synchronized (this) {
                while (pending == null && !closed) {
                    try { wait(); } catch (InterruptedException e) {
                        failure = new IOException("Mission journal writer interrupted", e); closed = true; notifyAll(); return;
                    }
                }
                if (pending == null) return;
                bytes = pending; writing = pendingVersion; pending = null;
            }
            try { writer.write(bytes); }
            catch (Exception e) {
                synchronized (this) {
                    failure = e instanceof IOException io ? io : new IOException("Mission journal writer failed", e);
                    pending = null; closed = true; notifyAll();
                }
                return;
            }
            synchronized (this) { durableVersion = writing; notifyAll(); }
        }
    }
    @Override public void close() {
        Thread thread;
        synchronized (this) { closed = true; notifyAll(); thread = worker; }
        if (thread != null) {
            boolean interrupted = false;
            for (;;) { try { thread.join(); break; } catch (InterruptedException e) { interrupted = true; } }
            if (interrupted) Thread.currentThread().interrupt();
        }
    }
    private static void writeAtomic(Path path, byte[] bytes) throws IOException {
        Files.createDirectories(path.getParent());
        Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
        try (var channel = FileChannel.open(tmp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            var buffer = ByteBuffer.wrap(bytes);
            while (buffer.hasRemaining()) channel.write(buffer);
            channel.force(true);
        }
        try { Files.move(tmp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
        catch (AtomicMoveNotSupportedException e) { Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING); }
    }
}
