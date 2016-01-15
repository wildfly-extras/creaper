# Release Procedure

This text assumes that the version number for a release is X.Y.Z.

1. Make sure `CHANGELOG.md` is up to date. Cross-check with GitHub Issues
   for given milestone.

2. If needed: `git commit -m "add missing changelog entries"`.

3. Reorder the entries in `CHANGELOG.md` from most important to least.
   Up to this point, entries are typically added chronologically.
   This is not the best order for release.

4. Update the heading of the latest changelog section. It should look like
   `X.Y.Z` instead of `X.Y.Z (not yet released)`.

5. Update the latest version number in `README.md`.

6. `git commit -m "prepare for the X.Y.Z release"`.

7. Your workspace should now be pristine. Check with `git status`.

8. Make sure that your `JAVA_HOME` points to Java 6.
   Check with `$JAVA_HOME/bin/java -version`.

9. `mvn release:prepare -Pas7 -Dmaven.jboss.ga.repository.url=...`

10. Maven will ask 3 questions. You might want to manually enter X.Y.Z
    as an answer to the 1st question if the default answer is wrong.
    Default answers to the 2nd and 3rd questions are always OK.

11. `mvn release:perform -Pas7 -Dmaven.jboss.ga.repository.url=...`

12. Go to `https://repository.jboss.org/nexus/index.html#stagingRepositories`

13. Find the correct staging repository; it will be called
    `wildfly_extras-NNNN`. Verify the owner name; it should be your
    JBoss.org username. Also check the content of the repository.

14. Close the staging repository. Nexus will perform some validations.
    If successful, Nexus will send a confirmation e-mail.

15. Release the repository. Nexus will send another confirmation e-mail.

16. Pick a code name and create a GitHub Release.

17. Close the relevant GitHub Milestone.

18. Send a release announcement.

19. Add a new heading to the top of `CHANGELOG.md`.
    Its text should be: `X.Y.Z+1 (not yet released)`.

20. `git commit -m "add next version number to changelog"`
