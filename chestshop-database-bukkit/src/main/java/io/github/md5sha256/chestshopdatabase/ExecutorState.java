package io.github.md5sha256.chestshopdatabase;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public record ExecutorState(@NotNull ExecutorService dbExec, @NotNull Executor mainThreadExec) {
}
