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
package net.tracknalysis.ecu.ms;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author David Valeri
 */
public class DefaultMegasquirtConfiguration implements MegasquirtConfiguration {
    
    private Set<String> props;
    
    public DefaultMegasquirtConfiguration(Set<String> flags) {
        if (flags == null) {
            this.props = Collections.emptySet();
        } else {
            this.props = new HashSet<String>(flags);
        }
    }

    @Override
    public boolean isSet(String property) {
        return props.contains(property);
    }
}
