./mvnw clean package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t quay.io/ibmcase/reefer-monitoring-agent .
docker push quay.io/ibmcase/reefer-monitoring-agent
