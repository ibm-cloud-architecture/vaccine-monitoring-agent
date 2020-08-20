package ibm.gse.eda.vaccine.coldchainagent.domain;

public class Returntype {
    public String key;
    public ContainerTracker container;

	public Returntype(String key, ContainerTracker container) {
		this.key = key;
		this.container = container;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ContainerTracker getContainer() {
        return container;
    }

    public void setContainer(ContainerTracker container) {
        this.container = container;
    }

    @Override
    public String toString() {
        return "Returntype [container=" + container + ", key=" + key + "]";
    }
}