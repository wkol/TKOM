@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kover)
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
    mainClass.set("org.example.interpreter.ApplicationKt")
}

dependencies {
    implementation(project(":lexer"))
    implementation(project(":parser"))
    implementation(project(":interpreter"))
    implementation(project(":inputsource"))
    implementation(project(":errorhandler"))
    testImplementation(libs.kotlin.test)
}

tasks.test {
    finalizedBy(tasks.named<TestReport>("testAggregateTestReport"))
}
