/*
 * Copyright 2016 the original author or authors.
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

import com.google.common.collect.Maps;
import org.gradle.api.BuildCancelledException;
import org.gradle.api.initialization.IncludedBuild;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.composite.CompositeBuildContext;
import org.gradle.composite.internal.IncludedBuildInternal;
import org.gradle.initialization.ReportedException;
import org.gradle.tooling.internal.protocol.BuildExceptionVersion1;
import org.gradle.tooling.internal.protocol.InternalBuildCancelledException;
import org.gradle.tooling.internal.protocol.InternalModelResults;
import org.gradle.tooling.internal.provider.BuildModelAction;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.internal.ProjectToolingModelBuilder;

import java.util.Map;
import java.util.Set;

public class BuildModelsActionRunner extends AbstractBuildModelActionRunner {

    @Override
    protected boolean canHandle(BuildModelAction buildModelAction) {
        return buildModelAction.isAllModels();
    }

    @Override
    protected Object getModelResult(GradleInternal gradle, String modelName) {
        return getAllModels(gradle, modelName);
    }

    private InternalModelResults<Object> getAllModels(GradleInternal gradle, String modelName) {
        InternalModelResults<Object> compositeResults = new InternalModelResults<Object>();
        try {
            collectModels(gradle, modelName, compositeResults);
        } catch (RuntimeException e) {
            compositeResults.addBuildFailure(gradle.getRootProject().getProjectDir(), transformFailure(e));
        }

        if (!getBuildTypeAttributes(gradle).isNestedBuild()) {
            CompositeBuildContext compositeBuildContext = gradle.getServices().get(CompositeBuildContext.class);
            Set<? extends IncludedBuild> includedBuilds = compositeBuildContext.getIncludedBuilds();


            for (IncludedBuild includedBuild : includedBuilds) {
                IncludedBuildInternal includedBuildInternal = (IncludedBuildInternal) includedBuild;
                GradleInternal includedGradle = includedBuildInternal.configure();
                try {
                    forceFullConfiguration(includedGradle);
                    collectModels(includedGradle, modelName, compositeResults);
                } catch (RuntimeException e) {
                    compositeResults.addBuildFailure(includedBuild.getProjectDir(), transformFailure(e));
                }
            }
        }
        return compositeResults;
    }

    //TODO let ToolingModelBuilder register results/failures instead of giving it a Map
    private void collectModels(GradleInternal gradle, String modelName, InternalModelResults<Object> models) {
        ToolingModelBuilder builder = getToolingModelBuilder(gradle, modelName);
        if (builder instanceof ProjectToolingModelBuilder) {
            Map<String, Object> modelsByPath = Maps.newLinkedHashMap();
            ((ProjectToolingModelBuilder) builder).addModels(modelName, gradle.getDefaultProject(), modelsByPath);
            for (Map.Entry<String, Object> entry : modelsByPath.entrySet()) {
                models.addProjectModel(gradle.getRootProject().getProjectDir(), entry.getKey(), entry.getValue());
            }
        } else {
            Object buildScopedModel = builder.buildAll(modelName, gradle.getDefaultProject());
            models.addBuildModel(gradle.getRootProject().getProjectDir(), buildScopedModel);
        }
    }

    //TODO get rid of duplication between this and DaemonBuildActionExecuter
    private RuntimeException transformFailure(RuntimeException e) {
        if (e instanceof BuildCancelledException) {
            return new InternalBuildCancelledException(e.getCause());
        }
        if (e instanceof ReportedException) {
            return unwrap((ReportedException) e);
        }
        return e;
    }

    private RuntimeException unwrap(ReportedException e) {
        Throwable t = e.getCause();
        while (t != null) {
            if (t instanceof BuildCancelledException) {
                return new InternalBuildCancelledException(e.getCause());
            }
            t = t.getCause();
        }
        return new BuildExceptionVersion1(e.getCause());
    }
}
