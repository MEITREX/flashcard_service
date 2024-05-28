# syntax=docker/dockerfile:experimental
FROM gradle:8-jdk21 AS build
WORKDIR /workspace/app

COPY . /workspace/app
RUN gradle clean build -x test
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*-SNAPSHOT.jar)

FROM eclipse-temurin:21-jdk
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/build/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","de.unistuttgart.iste.meitrex.flashcard_service.FlashcardServiceApplication"]