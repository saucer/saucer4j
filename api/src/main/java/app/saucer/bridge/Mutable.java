/**
 * MIT LICENSE
 *
 * Copyright (c) 2024 Alex Bowles @ Casterlabs
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package app.saucer.bridge;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.TypeToken;
import lombok.NonNull;

/**
 * This is to aid in controlling how {@link JavascriptValue} determines
 * mutations.
 * 
 * @implSpec This contains an internal counter for determining set()s, and the
 *           hashCode returned is just the current iteration.
 */
public class Mutable<T> {
    public final TypeToken<T> type;

    private volatile T value = null;
    private volatile int mutationIter = 0; // 0 = null.

    /**
     * Initializes with a null value.
     */
    public Mutable(@NonNull Class<T> type) {
        this(TypeToken.of(type));
    }

    /**
     * Initializes with a null value.
     */
    public Mutable(@NonNull TypeToken<T> type) {
        this.type = type;
    }

    public @Nullable T get() {
        return this.value;
    }

    /**
     * @return this instance, for chaining.
     */
    public Mutable<T> set(@Nullable T newValue) {
        this.value = newValue;
        return this.notifyMutate();
    }

    /**
     * @return this instance, for chaining.
     */
    public Mutable<T> notifyMutate() {
        this.mutationIter++;
        return this;
    }

    @Override
    public int hashCode() {
        return this.mutationIter;
    }

}
