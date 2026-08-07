/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.util.impl;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.cumulus.datadrivenui.util.Observable;

/**
 * The default {@link Observable}.
 *
 * <p>A value is stored under the lock, and the listeners are called outside it. Holding the lock
 * across a listener would let two observables whose listeners feed each other take their locks in
 * opposite orders and deadlock, and would let a listener that sets a new value deliver it before
 * the value it is reacting to.
 *
 * <p>Every accepted value is queued in the order it was stored, and whichever thread finds the
 * queue idle delivers all of them. A listener that sets a new value therefore adds to the queue
 * being drained rather than nesting inside it, so listeners always see values in the order they
 * were set, and none of them sees an older value after a newer one.
 *
 * @param <T> the type of the value
 */
public final class ObservableImpl<T> implements Observable<T> {
  private final List<Subscriber<T>> subscribers = new CopyOnWriteArrayList<>();
  private final Queue<T> pending = new ArrayDeque<>();
  private final Object lock = new Object();
  private final AtomicBoolean delivering = new AtomicBoolean();
  private volatile T value;

  public ObservableImpl(@NonNull T value) {
    this.value = Objects.requireNonNull(value, "value");
  }

  @Override
  public @NonNull T get() {
    return value;
  }

  @Override
  public void set(@NonNull T value) {
    Objects.requireNonNull(value, "value");
    synchronized (lock) {
      if (this.value.equals(value)) {
        return;
      }
      this.value = value;
      pending.add(value);
    }
    deliver();
  }

  @Override
  public @NonNull Subscription subscribe(@NonNull Consumer<@NonNull T> listener) {
    Objects.requireNonNull(listener, "listener");
    Subscriber<T> subscriber = new Subscriber<>(subscribers, listener);
    subscribers.add(subscriber);
    return subscriber;
  }

  private void deliver() {
    while (true) {
      if (!delivering.compareAndSet(false, true)) {
        // Another thread is already delivering, and will pick up what was just queued.
        return;
      }
      try {
        while (true) {
          T next;
          synchronized (lock) {
            next = pending.poll();
          }
          if (next == null) {
            break;
          }
          for (Subscriber<T> subscriber : subscribers) {
            subscriber.accept(next);
          }
        }
      } finally {
        delivering.set(false);
      }
      synchronized (lock) {
        if (pending.isEmpty()) {
          return;
        }
      }
      // A value was queued between the drain emptying and the flag clearing; going around again
      // keeps it from sitting there until the next set.
    }
  }

  private static final class Subscriber<T> implements Subscription {
    private final List<Subscriber<T>> subscribers;
    private final Consumer<T> listener;
    private final AtomicBoolean closed = new AtomicBoolean();

    Subscriber(List<Subscriber<T>> subscribers, Consumer<T> listener) {
      this.subscribers = subscribers;
      this.listener = listener;
    }

    void accept(T value) {
      if (closed.get()) {
        return;
      }
      try {
        listener.accept(value);
      } catch (Throwable throwable) {
        // One misbehaving listener must not stop the others, and must not surface as a failure of
        // whatever set the value. A handler that itself fails must not either, so reporting is
        // guarded too; an Error still leaves by the shortest path, since swallowing one hides a
        // broken VM rather than a broken listener.
        if (throwable instanceof Error) {
          throw (Error) throwable;
        }
        Thread thread = Thread.currentThread();
        try {
          thread.getUncaughtExceptionHandler().uncaughtException(thread, throwable);
        } catch (RuntimeException ignored) {
          // Nowhere left to report it.
        }
      }
    }

    @Override
    public void close() {
      if (closed.compareAndSet(false, true)) {
        subscribers.remove(this);
      }
    }
  }
}
