/*
 * Copyright 2010 the original author or authors.
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

package org.gradle.api.internal;

import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.tasks.TaskOutputFilePropertySpec;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskOutputFilePropertyBuilder;
import org.gradle.api.tasks.TaskOutputs;

import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.Callable;

public interface TaskOutputsInternal extends TaskOutputs {

    /**
     * Register some named outputs for this task.
     *
     * @param paths A {@link Callable} returning the actual output files. The keys of the returned map should not
     * be {@code null}, and they must be
     * <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.8">valid Java identifiers</a>}.
     * The values will be evaluated to individual files as per {@link org.gradle.api.Project#file(Object)}.
     */
    TaskOutputFilePropertyBuilder namedFiles(Callable<Map<?, ?>> paths);

    /**
     * Register some named outputs for this task.
     *
     * @param paths The output files. The keys of the map should not be {@code null}, and they must be
     * <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.8">valid Java identifiers</a>}.
     * The values will be evaluated to individual files as per {@link org.gradle.api.Project#file(Object)}.
     */
    TaskOutputFilePropertyBuilder namedFiles(Map<?, ?> paths);

    Spec<? super TaskInternal> getUpToDateSpec();

    SortedSet<TaskOutputFilePropertySpec> getFileProperties();

    /**
     * Returns the output files recorded during the previous execution of the task.
     */
    FileCollection getPreviousOutputFiles();

    void setHistory(TaskExecutionHistory history);

    /**
     * Check if caching is explicitly enabled for the task outputs.
     */
    boolean isCacheEnabled();

    /**
     * Checks if caching is allowed based on the output properties.
     */
    boolean isCacheAllowed();
}
