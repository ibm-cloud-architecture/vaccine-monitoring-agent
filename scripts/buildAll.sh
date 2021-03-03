./mvnw clean package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t ibmcase/reefer-monitoring-agent .
docker push ibmcase/reefer-monitoring-agent
