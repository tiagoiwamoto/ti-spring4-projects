https://www.graalvm.org/latest/reference-manual/native-image/guides/build-spring-boot-app-into-native-executable/



./mvnw -Pnative spring-boot:build-image

docker run --rm --network host -p 8081:8081 docker.io/library/02-spring-jpa:0.0.1-SNAPSHOT