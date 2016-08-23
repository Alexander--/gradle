/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.tooling.internal.provider.runner;

import org.gradle.api.internal.GradleInternal;
import org.gradle.tooling.internal.provider.BuildModelAction;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

public class BuildModelActionRunner extends AbstractBuildModelActionRunner {
    @Override
    protected boolean canHandle(BuildModelAction buildModelAction) {
        return !buildModelAction.isAllModels();
    }

    @Override
    protected Object getModelResult(GradleInternal gradle, String modelName) {
        return getModel(gradle, modelName);
    }

    private Object getModel(GradleInternal gradle, String modelName) {
        ToolingModelBuilder builder = getToolingModelBuilder(gradle, modelName);
        return builder.buildAll(modelName, gradle.getDefaultProject());
    }
}
