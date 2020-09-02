package ibm.gse.eda.vaccine.coldchainagent.domain.interactivequery;

import java.util.Optional;
import java.util.OptionalInt;

import ibm.gse.eda.vaccine.coldchainagent.domain.ReeferAggregate;

public class GetContainerTrackerDataResult {

    private static GetContainerTrackerDataResult NOT_FOUND = new GetContainerTrackerDataResult(null, null, null);

    private final ReeferAggregate result;
    private final String host;
    private final Integer port;

    private GetContainerTrackerDataResult(ReeferAggregate result, String host, Integer port) {
        this.result = result;
        this.host = host;
        this.port = port;
    }

    public static GetContainerTrackerDataResult found(ReeferAggregate data) {
        return new GetContainerTrackerDataResult(data, null, null);
    }

    public static GetContainerTrackerDataResult foundRemotely(String host, int port) {
        return new GetContainerTrackerDataResult(null, host, port);
    }

    public static GetContainerTrackerDataResult notFound() {
        return NOT_FOUND;
    }

    public Optional<ReeferAggregate> getResult() {
        return Optional.ofNullable(result);
    }

    public Optional<String> getHost() {
        return Optional.ofNullable(host);
    }

    public OptionalInt getPort() {
        return port != null ? OptionalInt.of(port) : OptionalInt.empty();
    }
}
