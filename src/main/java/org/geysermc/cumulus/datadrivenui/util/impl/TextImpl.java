/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.util.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.cumulus.datadrivenui.util.Text;

/**
 * Implementations of {@link Text}.
 *
 * <p>Text is built up by nesting, and the result is sent to a client that has to walk it. The
 * limits below are checked while building so that an over-large or deeply nested value is rejected
 * by the code that created it, rather than much later while a form is being sent.
 */
public final class TextImpl {
  /** How deeply text may be nested inside other text. */
  public static final int MAX_DEPTH = 16;

  /** How many pieces of text a single value may be made of, counting all nesting. */
  public static final int MAX_NODES = 128;

  /** How long one literal or translation key may be; the client refuses longer strings. */
  public static final int MAX_LENGTH = 5000;

  private TextImpl() {}

  public static @NonNull Text literal(@NonNull String text) {
    Objects.requireNonNull(text, "text");
    if (text.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "text is " + text.length() + " characters, at most " + MAX_LENGTH);
    }
    return new LiteralImpl(text);
  }

  public static @NonNull Text translatable(@NonNull String key, @NonNull Text... args) {
    Objects.requireNonNull(key, "key");
    if (key.isEmpty()) {
      throw new IllegalArgumentException("key cannot be empty");
    }
    if (key.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "key is " + key.length() + " characters, at most " + MAX_LENGTH);
    }
    return new TranslatableImpl(key, copyAndCheck(args, "args"));
  }

  public static @NonNull Text sequence(@NonNull Text... parts) {
    return new SequenceImpl(copyAndCheck(parts, "parts"));
  }

  private static List<Text> copyAndCheck(@Nullable Text[] values, String name) {
    Objects.requireNonNull(values, name);
    List<Text> copy = new ArrayList<>(values.length);
    for (int i = 0; i < values.length; i++) {
      Text value = values[i];
      if (value == null) {
        throw new NullPointerException(name + "[" + i + "]");
      }
      copy.add(value);
    }
    List<Text> result = Collections.unmodifiableList(copy);
    // The whole subtree is walked again on every construction: the constituents were legal alone,
    // but together they may exceed what one value may hold.
    checkDepth(result, 1);
    int nodes = countNodes(result, 1);
    if (nodes > MAX_NODES) {
      throw new IllegalArgumentException("text has " + nodes + " parts, at most " + MAX_NODES);
    }
    return result;
  }

  private static void checkDepth(List<Text> values, int depth) {
    if (depth > MAX_DEPTH) {
      throw new IllegalArgumentException("text is nested deeper than " + MAX_DEPTH);
    }
    for (Text value : values) {
      List<Text> children = childrenOf(value);
      if (!children.isEmpty()) {
        checkDepth(children, depth + 1);
      }
    }
  }

  private static int countNodes(List<Text> values, int counted) {
    int total = counted + values.size();
    for (Text value : values) {
      List<Text> children = childrenOf(value);
      if (!children.isEmpty()) {
        total += countNodes(children, 0);
      }
      if (total > MAX_NODES) {
        return total;
      }
    }
    return total;
  }

  private static List<Text> childrenOf(Text value) {
    if (value instanceof Text.Translatable) {
      return ((Text.Translatable) value).args();
    }
    if (value instanceof Text.Sequence) {
      return ((Text.Sequence) value).parts();
    }
    return Collections.emptyList();
  }

  static final class LiteralImpl implements Text.Literal {
    private final String text;

    LiteralImpl(String text) {
      this.text = text;
    }

    @Override
    public @NonNull String text() {
      return text;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Text.Literal)) {
        return false;
      }
      return text.equals(((Text.Literal) o).text());
    }

    @Override
    public int hashCode() {
      return text.hashCode();
    }

    @Override
    public String toString() {
      return "Text.Literal(" + text + ")";
    }
  }

  static final class TranslatableImpl implements Text.Translatable {
    private final String key;
    private final List<Text> args;

    TranslatableImpl(String key, List<Text> args) {
      this.key = key;
      this.args = args;
    }

    @Override
    public @NonNull String key() {
      return key;
    }

    @Override
    public @NonNull List<@NonNull Text> args() {
      return args;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Text.Translatable)) {
        return false;
      }
      Text.Translatable other = (Text.Translatable) o;
      return key.equals(other.key()) && args.equals(other.args());
    }

    @Override
    public int hashCode() {
      return 31 * key.hashCode() + args.hashCode();
    }

    @Override
    public String toString() {
      return "Text.Translatable(" + key + ", " + args + ")";
    }
  }

  static final class SequenceImpl implements Text.Sequence {
    private final List<Text> parts;

    SequenceImpl(List<Text> parts) {
      this.parts = parts;
    }

    @Override
    public @NonNull List<@NonNull Text> parts() {
      return parts;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Text.Sequence)) {
        return false;
      }
      return parts.equals(((Text.Sequence) o).parts());
    }

    @Override
    public int hashCode() {
      return parts.hashCode();
    }

    @Override
    public String toString() {
      return "Text.Sequence(" + parts + ")";
    }
  }
}
