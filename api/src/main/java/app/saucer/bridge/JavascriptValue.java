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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Lets you expose a property/field to Javascript. You can optionally disable
 * get or sets, as well as register a watcher (via watchForMutate) which can be
 * used with the `__stores` feature. Note that the return value will always be a
 * Promise<T> to Javascript and WILL need to be awaited on.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface JavascriptValue {

    String value() default "";

    boolean allowGet() default true;

    boolean allowSet() default true;

    /**
     * @implNote watchForMutate will call your {@link JavascriptGetter} if you have
     *           it defined. It is recommended that you do not use
     *           {@link JavascriptGetter} with watchForMutate as this may cause
     *           unnecessary load.
     * 
     * @implSpec If the field being watched is a Collection it will traverse it's
     *           contents to determine whether or not the field has mutated.
     * 
     * @see      {@link Mutable}, which gives you more fine-grained control over
     *           when a mutation is determined to occur.
     */
    boolean watchForMutate() default false;

}
