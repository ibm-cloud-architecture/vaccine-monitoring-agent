package ibm.gse.eda.vaccine.coldchainagent.api.dto;

import ibm.gse.eda.vaccine.coldchainagent.domain.ReeferAggregate;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Returntype {
    public String key;
    public ReeferAggregate container;

	public Returntype(String key, ReeferAggregate container) {
		this.key = key;
		this.container = container;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ReeferAggregate getContainer() {
        return container;
    }

    public void setContainer(ReeferAggregate container) {
        this.container = container;
    }

    @Override
    public String toString() {
        return "Returntype [container=" + container + ", key=" + key + "]";
    }
}