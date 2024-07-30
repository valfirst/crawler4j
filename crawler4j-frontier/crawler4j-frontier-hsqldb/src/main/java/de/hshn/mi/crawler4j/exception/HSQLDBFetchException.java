/*
 * Copyright 2010-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.hshn.mi.crawler4j.exception;

public class HSQLDBFetchException extends RuntimeException{

    private static final long serialVersionUID = 5328538498739884171L;

    public HSQLDBFetchException(String message) {
        super(message);
    }

    public HSQLDBFetchException(Throwable cause) {
        super(cause);
    }

    public HSQLDBFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
