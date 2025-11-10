# Allows you to run the "testapp" app easily as a docker container.
# See README.md for more details. Requires the docker buildkit/buildx extension.
#
# 1. Build the image with: docker build -t test/vaadin-boot-testapp:latest .
# 2. Run the image with: docker run --rm -ti -p8080:8080 test/vaadin-boot-testapp
#
# Uses Docker Multi-stage builds: https://docs.docker.com/build/building/multi-stage/

# The "Build" stage. Copies the entire project into the container, into the /app/ folder, and builds it.
FROM --platform=$BUILDPLATFORM eclipse-temurin:21 AS builder
COPY . /app/
WORKDIR /app/
RUN --mount=type=cache,target=/root/.gradle --mount=type=cache,target=/root/.vaadin ./gradlew clean testapp:build -Pvaadin.productionMode --no-daemon --info --stacktrace -x test
WORKDIR /app/testapp/build/distributions/
RUN tar xvf testapp-*.tar && rm -rf testapp-*.tar testapp-*.zip

# The "Run" stage. Start with a clean image, and copy over just the app itself, omitting gradle, npm and any intermediate build files.
FROM eclipse-temurin:21
COPY --from=builder /app/testapp/build/distributions/testapp-*/ /app/
WORKDIR /app/bin
RUN ls -la
EXPOSE 8080
ENTRYPOINT ["./testapp"]

