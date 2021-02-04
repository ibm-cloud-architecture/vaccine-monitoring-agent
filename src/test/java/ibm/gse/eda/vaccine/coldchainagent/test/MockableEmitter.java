package ibm.gse.eda.vaccine.coldchainagent.test;

import java.util.concurrent.CompletionStage;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.resteasy.plugins.providers.CompletionStageProvider;

import ibm.gse.eda.vaccine.coldchainagent.infrastructure.ReeferEvent;

public class MockableEmitter implements Emitter<ReeferEvent> {
    Jsonb parser = JsonbBuilder.create();

    @Override
    public CompletionStage<Void> send(ReeferEvent msg) {
        System.out.println(parser.toJson(msg));
        return null;
    }

    @Override
    public <M extends Message<? extends ReeferEvent>> void send(M msg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void complete() {
        // TODO Auto-generated method stub

    }

    @Override
    public void error(Exception e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isCancelled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasRequests() {
        // TODO Auto-generated method stub
        return false;
    }
    
}