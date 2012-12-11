/**
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tracknalysis.ecu.ms.log;

/**
 * @author David Smith
 * @author David Valeri
 */
public interface FileLog extends Log {

    /**
     * Returns the absolute path of the log file currently in use or last written to.  Returns
     * {@code null} if no log file has ever been written to.
     */
    String getLogFileAbsolutePath();
}
