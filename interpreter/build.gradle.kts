group = "org.example.interpreter"

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    testImplementation(libs.kotlin.test)
    implementation(project(":lexer"))
    implementation(project(":parser"))
}