package io.github.md5sha256.chestshopdatabase.task;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class TaskProgress {

    private final AtomicInteger completed;
    private final int total;
    private final Runnable onComplete;

    public TaskProgress(int total, @Nullable Runnable onComplete) {
        this.completed = new AtomicInteger(0);
        this.total = total;
        this.onComplete = onComplete;
    }

    public boolean isDone() {
        return this.completed.get() == this.total;
    }

    public void markCompleted() {
        if (this.onComplete != null && this.completed.incrementAndGet() == this.total) {
            triggerCompleted();
        }
    }

    public int completed() {
        return this.completed.get();
    }

    public void triggerCompleted() {
        if (this.onComplete != null) {
            this.onComplete.run();
        }
    }

}
