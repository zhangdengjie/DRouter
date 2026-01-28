package com.didi.drouter

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import com.didi.drouter.plugin.RouterSetting
import com.didi.drouter.utils.SystemUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized

class AppendRouterPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        SystemUtil.confirm(project)
        project.extensions.create("drouter", RouterSetting::class.java)

        project.plugins.withType(AppPlugin::class.java) {
            val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                val taskProvider = project.tasks.register(
                    "${variant.name}DRouterTask", AppendRouterTransform::class.java, androidComponents,variant)
                taskProvider.configure {
                    it.dependsOn(project.tasks.named("compile${variant.name.capitalized()}JavaWithJavac"))
                }
                // 新版本8.0.0以后,目前使用的是8.12.3 ScopedArtifacts.Scope.PROJECT 会自动将AppendRouterTransform添加到任务图中
                // 编译依赖可以通过api获取,具体见AppendRouterTransform实现(可实现所有模块的路由表收集)[重要]
                // ScopedArtifacts.Scope.ALL 必须得有任务消费输出才可以触发任务的执行
                variant.artifacts
                    .forScope(ScopedArtifacts.Scope.PROJECT)
                    .use(taskProvider)
                    .toAppend(
                        ScopedArtifact.CLASSES,
                        AppendRouterTransform::outputJar
                    )
            }
        }
    }
}