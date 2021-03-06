/*
 * Copyright 2020 ThoughtWorks, Inc.
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
package com.thoughtworks.go.server.service;

import com.thoughtworks.go.config.BasicPipelineConfigs;
import com.thoughtworks.go.config.PipelineConfig;
import com.thoughtworks.go.config.PipelineConfigs;
import com.thoughtworks.go.config.materials.MaterialConfigs;
import com.thoughtworks.go.config.materials.git.GitMaterialConfig;
import com.thoughtworks.go.domain.PipelineGroups;
import com.thoughtworks.go.domain.materials.MaterialConfig;
import com.thoughtworks.go.server.service.result.HttpOperationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static com.thoughtworks.go.helper.MaterialConfigsMother.git;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class MaterialConfigServiceTest {
    @Mock
    private GoConfigService goConfigService;

    @Mock
    private SecurityService securityService;

    private String user;
    private MaterialConfigService materialConfigService;

    @BeforeEach
    void setup() throws Exception {
        initMocks(this);

        user = "looser";
        when(securityService.hasViewPermissionForGroup(user, "group1")).thenReturn(true);
        when(securityService.hasViewPermissionForGroup(user, "group2")).thenReturn(false);
        when(securityService.hasViewPermissionForGroup(user, "group3")).thenReturn(true);

        PipelineConfigs pipelineGroup1 = new BasicPipelineConfigs();
        pipelineGroup1.setGroup("group1");
        PipelineConfig pipelineConfig1 = new PipelineConfig();
        pipelineConfig1.setName("pipeline1");
        pipelineConfig1.addMaterialConfig(git("http://test.com"));
        pipelineConfig1.addMaterialConfig(git("http://crap.com"));
        pipelineGroup1.add(pipelineConfig1);

        PipelineConfigs pipelineGroup2 = new BasicPipelineConfigs();
        pipelineGroup2.setGroup("group2");
        PipelineConfig pipelineConfig2 = new PipelineConfig();
        pipelineConfig2.setName("pipeline2");
        pipelineConfig2.addMaterialConfig(git("http://another.com"));
        pipelineGroup2.add(pipelineConfig2);

        PipelineConfigs pipelineGroup3 = new BasicPipelineConfigs();
        pipelineGroup3.setGroup("group3");
        PipelineConfig pipelineConfig3 = new PipelineConfig();
        pipelineConfig3.setName("pipeline3");
        pipelineConfig3.addMaterialConfig(git("http://test.com"));
        pipelineGroup3.add(pipelineConfig3);

        PipelineGroups pipelineGroups = new PipelineGroups(pipelineGroup1, pipelineGroup2, pipelineGroup3);
        when(goConfigService.groups()).thenReturn(pipelineGroups);

        materialConfigService = new MaterialConfigService(goConfigService, securityService);
    }

    @Test
    void shouldGetUniqueMaterialConfigsToWhichUserHasViewPermission() {
        MaterialConfigs materialConfigs = materialConfigService.getMaterialConfigs(user);

        assertThat(materialConfigs.size()).isEqualTo(2);
        assertThat(materialConfigs.get(0)).isEqualTo(git("http://test.com"));
        assertThat(materialConfigs.get(1)).isEqualTo(git("http://crap.com"));
    }

    @Test
    void shouldGetMaterialConfigByFingerprint() {
        HttpOperationResult result = new HttpOperationResult();
        GitMaterialConfig gitMaterialConfig = git("http://crap.com");
        MaterialConfig materialConfig = materialConfigService.getMaterialConfig(user, gitMaterialConfig.getFingerprint(), result);

        assertThat(materialConfig).isEqualTo(gitMaterialConfig);
        assertThat(result.canContinue()).isTrue();
    }

    @Test
    void shouldPopulateErrorCorrectlyWhenMaterialNotFound_getMaterialConfigByFingerprint() {
        HttpOperationResult result = new HttpOperationResult();
        MaterialConfig materialConfig = materialConfigService.getMaterialConfig(user, "unknown-fingerprint", result);

        assertThat(materialConfig).isNull();
        assertThat(result.httpCode()).isEqualTo(404);
    }

    @Test
    void shouldPopulateErrorCorrectlyWhenUnauthorizedToViewMaterial_getMaterialConfigByFingerprint() {
        HttpOperationResult result = new HttpOperationResult();
        GitMaterialConfig gitMaterialConfig = git("http://another.com");
        MaterialConfig materialConfig = materialConfigService.getMaterialConfig(user, gitMaterialConfig.getFingerprint(), result);

        assertThat(materialConfig).isNull();
        assertThat(result.httpCode()).isEqualTo(403);
    }

    @Test
    void shouldReturnUsagesOfAGivenMaterial() {
        GitMaterialConfig material = git("http://test.com");

        List<String> usages = materialConfigService.getUsagesForMaterial(user, material.getFingerprint());

        assertThat(usages.size()).isEqualTo(2);
        assertThat(usages).containsExactly("pipeline1", "pipeline3");
    }

    @Test
    void shouldReturnEmptyMapIfNotUsagesFound() {
        GitMaterialConfig material = git("http://example.com");

        List<String> usages = materialConfigService.getUsagesForMaterial(user, material.getFingerprint());

        assertThat(usages).isEmpty();
    }
}
