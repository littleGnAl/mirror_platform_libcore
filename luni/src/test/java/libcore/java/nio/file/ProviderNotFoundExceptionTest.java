/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.nio.file;

import junit.framework.TestCase;

import java.nio.file.ProviderNotFoundException;

public class ProviderNotFoundExceptionTest extends TestCase {

    public void test_constructor$() {
        ProviderNotFoundException exception = new ProviderNotFoundException();
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    public void test_constructor$String() {
        String message = "message";
        ProviderNotFoundException exception = new ProviderNotFoundException(message);
        assertEquals(message, exception.getMessage());

        message = null;
        exception = new ProviderNotFoundException(message);
        assertEquals(message, exception.getMessage());
    }
}
