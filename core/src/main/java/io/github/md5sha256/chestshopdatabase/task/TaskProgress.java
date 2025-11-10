package io.github.md5sha256.chestshopdatabase.task;

import org.jetbrains.annotations.Nullable;

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

    public void setOnComplete(@Nullable Runnable onComplete) {
        this.onComplete.getAndSet(onComplete);
        if (isDone() && onComplete != null) {
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
