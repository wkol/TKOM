group = "org.example.lexer"

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(project(":inputsource"))
    implementation(project(":errorhandler"))
    testImplementation(libs.kotlin.test)
}
