/**
 * Copyright 2012 David Valeri.
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
 * Indicates that the signature received from the Megasquirt is not of a known format.
 *
 * @author David Valeri
 */
public class InvalidSignatureException extends SignatureException {

    private static final long serialVersionUID = 1L;

    public InvalidSignatureException() {
        super();
    }

    public InvalidSignatureException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public InvalidSignatureException(String detailMessage) {
        super(detailMessage);
    }

    public InvalidSignatureException(Throwable throwable) {
        super(throwable);
    }
}
