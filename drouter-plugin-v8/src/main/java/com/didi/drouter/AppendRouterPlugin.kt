package com.didi.drouter

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.didi.drouter.plugin.RouterSetting
import com.didi.drouter.utils.SystemUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized

class AppendRouterPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        SystemUtil.confirm(project)
        project.extensions.create("drouter", RouterSetting::class.java)

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val taskProvider = project.tasks.register(
                "${variant.name.capitalized()}DRouterTask", AppendRouterTransform::class.java, androidComponents,variant)
            project.afterEvaluate {
                taskProvider.configure {
                    it.dependsOn(project.tasks.named("compile${variant.name.capitalized()}JavaWithJavac"))
                }
            }
            variant.artifacts
                .forScope(ScopedArtifacts.Scope.ALL)
                .use(taskProvider)
                .toAppend(
                    ScopedArtifact.CLASSES,
                    AppendRouterTransform::outputJar
                )
        }
    }
}