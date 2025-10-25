package io.github.md5sha256.chestshopdatabase;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public record ExecutorState(@Nonnull ExecutorService dbExec, @Nonnull Executor mainThreadExec) {
}
