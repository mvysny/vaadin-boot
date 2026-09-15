# Decisions

Why this project is the way it is and not otherwise — FAQ-shaped: each entry is a question and
its current answer. Rewrite the answer when it changes; delete the entry when nobody asks any
more. An entry is earned by what it would cost to reverse — half the code base — or by research
the next person would otherwise redo (cited as its `R_`). Not an entry: windows → panels
"because that's the trend", this red over that red, `get_foo` over `is_foo?`, the testing library,
the CI host, a version bump — a comment at the site of the choice, or nothing; nothing about
`design/` itself. Cite by slug, `D_<slug>`, never by position; `grep '^## D_' design/decisions.md`
is the index. The first entry is the ruler: every later one trims to its length — which is how
long this file gets, so keep it short. When you have written an entry, re-read it against the one
above, check it says nothing the javadoc already says, and cut what is left over.

---

## D_two_container_artifacts — Why two published artifacts shipping the same `VaadinBoot` class rather than one jar supporting both containers?

`vaadin-boot` carries Jetty 12 (ee10), `vaadin-boot-tomcat` carries Tomcat 11, and both publish
`com.github.mvysny.vaadinboot.VaadinBoot`, so an app's `main()` — imports included — is identical
on either container and switching is one line in the build file. Why not one artifact holding
both: every app would then resolve a whole second container it never starts, against **You only
use what you need**, and unpicking that back down to one would be the user's problem, written in
exclusion rules. Why not one artifact with two entry points (`JettyVaadinBoot` /
`TomcatVaadinBoot`): the container would be named in the app's own source, so switching becomes a
code change instead of a build-file line, and every snippet in the README forks in two. The cost
we carry: the two `VaadinBoot` classes are kept in API sync by hand, and they are not identical —
the Jetty one has three extra knobs, listed under *API differences vs. Jetty* in the README.
