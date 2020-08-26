package ibm.gse.eda.vaccine.coldchainagent.domain.interactivequery;

import java.util.Optional;
import java.util.OptionalInt;

import ibm.gse.eda.vaccine.coldchainagent.domain.ContainerTracker;

public class GetContainerTrackerDataResult {

    private static GetContainerTrackerDataResult NOT_FOUND = new GetContainerTrackerDataResult(null, null, null);

    private final ContainerTracker result;
    private final String host;
    private final Integer port;

    private GetContainerTrackerDataResult(ContainerTracker result, String host, Integer port) {
        this.result = result;
        this.host = host;
        this.port = port;
    }

    public static GetContainerTrackerDataResult found(ContainerTracker data) {
        return new GetContainerTrackerDataResult(data, null, null);
    }

    public static GetContainerTrackerDataResult foundRemotely(String host, int port) {
        return new GetContainerTrackerDataResult(null, host, port);
    }

    public static GetContainerTrackerDataResult notFound() {
        return NOT_FOUND;
    }

    public Optional<ContainerTracker> getResult() {
        return Optional.ofNullable(result);
    }

    public Optional<String> getHost() {
        return Optional.ofNullable(host);
    }

    public OptionalInt getPort() {
        return port != null ? OptionalInt.of(port) : OptionalInt.empty();
    }
}
