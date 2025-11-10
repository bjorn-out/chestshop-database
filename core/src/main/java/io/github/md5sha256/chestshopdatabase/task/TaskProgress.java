package io.github.md5sha256.chestshopdatabase.task;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TaskProgress {

    private final AtomicInteger completed;
    private final int total;
    private final AtomicReference<Runnable> onComplete = new AtomicReference<>();

    public TaskProgress(int total) {
        this.completed = new AtomicInteger(0);
        this.total = total;
    }

    public void chainOnComplete(@NotNull Runnable onComplete) {
        this.onComplete.getAndUpdate(existing -> {
            if (existing == null) {
                return onComplete;
            }
            return () -> {
                existing.run();
                onComplete.run();
            };
        });
        if (isDone()) {
            onComplete.run();
        }
    }

    public int total() {
        return this.total;
    }

    public boolean isDone() {
        return this.completed.get() == this.total;
    }

    public void markCompleted() {
        if (this.completed.incrementAndGet() == this.total) {
            triggerCompleted();
        }
    }

    public void markCompleted(int amount) {
        if (this.completed.addAndGet(amount) == this.total) {
            triggerCompleted();
        }
    }

    public int completed() {
        return this.completed.get();
    }

    public void triggerCompleted() {
        Runnable runnable = this.onComplete.get();
        if (runnable != null) {
            runnable.run();
        }
    }

}
