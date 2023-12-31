/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tests.java.lang;

import junit.framework.TestCase;

public class IndexOutOfBoundsExceptionTest extends TestCase {

    /**
     * java.lang.IndexOutOfBoundsException#IndexOutOfBoundsException()
     */
    public void test_Constructor() {
        IndexOutOfBoundsException e = new IndexOutOfBoundsException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }

    /**
     * java.lang.IndexOutOfBoundsException#IndexOutOfBoundsException(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        IndexOutOfBoundsException e = new IndexOutOfBoundsException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }

    public void test_ConstructorI() {
        IndexOutOfBoundsException e = new IndexOutOfBoundsException(Integer.MAX_VALUE);
        assertEquals("Index out of range: " + Integer.MAX_VALUE, e.getMessage());
        assertNull(e.getCause());
    }

    public void test_ConstructorJ() {
        IndexOutOfBoundsException e = new IndexOutOfBoundsException(Long.MAX_VALUE);
        assertEquals("Index out of range: " + Long.MAX_VALUE, e.getMessage());
        assertNull(e.getCause());
    }
}
