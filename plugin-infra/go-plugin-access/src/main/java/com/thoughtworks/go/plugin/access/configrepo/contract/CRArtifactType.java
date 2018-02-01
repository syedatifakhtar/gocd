/*
 * Copyright 2017 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package com.thoughtworks.go.plugin.access.configrepo.contract;

import static com.thoughtworks.go.util.ExceptionUtils.bomb;

public enum CRArtifactType {
    build,
    test,
    external;

    public static CRArtifactType fromName(String artifactType) {
        try {
            return valueOf(artifactType);
        } catch (IllegalArgumentException e) {
            throw bomb("Illegal name in for the artifact type.[" + artifactType + "]", e);
        }
    }
}