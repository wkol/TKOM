import io.gitlab.arturbosch.detekt.Detekt

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
    alias(libs.plugins.shadowJar)
    id(libs.plugins.test.aggregation.get().pluginId)
    java
    application
}
group = "org.example"

koverMerged {
    enable()
}

allprojects {
    apply(plugin = rootProject.libs.plugins.kover.get().pluginId)
    apply(plugin = rootProject.libs.plugins.kotlin.jvm.get().pluginId)
    apply(plugin = rootProject.libs.plugins.kotlin.kapt.get().pluginId)
    apply(plugin = rootProject.libs.plugins.test.aggregation.get().pluginId)
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)
    repositories {
        mavenCentral()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
    }

    tasks {
        compileKotlin {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
        test {
            useJUnitPlatform()
            reports.html.required.set(true)
        }
    }
}
application {
    mainClass.set("org.example.interpreter.InterpreterKt")
}

dependencies {
    implementation(project(":lexer"))
    implementation(project(":parser"))
    implementation(project(":interpreter"))
    detektPlugins(libs.detekt.formatting)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    finalizedBy(tasks.named<TestReport>("testAggregateTestReport"))
}

detekt {
    toolVersion = "1.22.0-RC3"
    buildUponDefaultConfig = true
}

tasks.withType(Detekt::class) {
    reports {
        html.required.set(true)
    }
}
