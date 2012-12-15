/**
 * Copyright 2012 the original author or authors.
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
package net.tracknalysis.ecu.ms.ecu.factory;

/**
 * @author David Valeri
 */
class UnsupportedFirmwareException extends MsEcuInterfaceFactoryException {

    private static final long serialVersionUID = 1L;

    public UnsupportedFirmwareException() {
        super();
    }

    public UnsupportedFirmwareException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UnsupportedFirmwareException(String detailMessage) {
        super(detailMessage);
    }

    public UnsupportedFirmwareException(Throwable throwable) {
        super(throwable);
    }
}