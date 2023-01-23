group = "org.example.parser"

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    testImplementation(libs.kotlin.test)
    implementation(project(":lexer"))
    implementation(project(":errorhandler"))
    implementation(project(":inputsource"))
}