# Contributing

Thank you so much for making the library better.
Please feel free to open bug reports to discuss new features; PRs are welcome as well :)

## Tests

Uses JUnit 5. Simply run `./gradlew test` to run all tests.

### Manual Tests

Make sure to test the Boot with all example apps, especially the Enter/CTRL+C handling:

1. Run `./gradlew` to clean up any Vaadin production build leftovers
2. Run the `Main` class as a traditional `main()` from Intellij and test that Enter shuts down the app correctly
3. Run `./gradlew clean build testapp:run -Pvaadin.productionMode` and test that CTRL+C kills the app
3. Run `./gradlew clean build testapp-kotlin:run -Pvaadin.productionMode` and test that CTRL+C kills the app
4. Unzip `testapp/build/distributions/testapp-*.zip`, then run it and test that both CTRL+C and Enter correctly shuts down the app.

# Releasing

To release the library to Maven Central:

1. Edit `build.gradle.kts` and remove `-SNAPSHOT` in the `version=` stanza
2. Commit with the commit message of simply being the version being released, e.g. "1.2.13"
3. git tag the commit with the same tag name as the commit message above, e.g. `1.2.13`
4. `git push`, `git push --tags`
5. Run `./gradlew clean build publish`
6. Continue to the [OSSRH Nexus](https://oss.sonatype.org/#stagingRepositories) and follow the [release procedure](https://central.sonatype.org/pages/releasing-the-deployment.html).
7. Add the `-SNAPSHOT` back to the `version=` while increasing the version to something which will be released in the future,
   e.g. 1.2.14-SNAPSHOT, then commit with the commit message "1.2.14-SNAPSHOT" and push.
