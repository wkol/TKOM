group = "org.example.errorhandler"

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(project(":inputsource"))
    implementation(libs.kotlin.reflect)
    testImplementation(libs.kotlin.test)
}
