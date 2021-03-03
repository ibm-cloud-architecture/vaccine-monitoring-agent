package ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring;

import java.io.StringReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Singleton
public class WMLScoringClient {
    protected static Logger LOG = Logger.getLogger(WMLScoringClient.class);
    private Client client;
    
    @ConfigProperty(name="anomalydetection.scoring.url",defaultValue = "")
    private String predictionURL;
    @ConfigProperty(name = "cp4d.auth.url",defaultValue = "")
    private String endPointURL;
    @ConfigProperty(name = "cp4d.user",defaultValue = "")
    private String username;
    @ConfigProperty(name = "cp4d.api.key",defaultValue = "")
    private String api_key;
    public String wml_token;

    Jsonb jsonb = JsonbBuilder.create();

    // connection is over TLS
    private SSLContext sslcontext;

    public WMLScoringClient() {
        initSSL();
    }

    
    private void initSSL() {
        try {
            if (this.endPointURL == null)
                this.endPointURL = ConfigProvider.getConfig().getValue("cp4d.auth.url",String.class);
            if (this.username == null)
                this.username = ConfigProvider.getConfig().getValue("cp4d.user",String.class);
            if (this.api_key == null)
                this.api_key = ConfigProvider.getConfig().getValue("cp4d.api.key",String.class);
            if (this.predictionURL == null)
                this.predictionURL = ConfigProvider.getConfig().getValue("anomalydetection.scoring.url",String.class);
               
            sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }}, new java.security.SecureRandom());
           
            client = ClientBuilder.newBuilder()
            		.sslContext(this.sslcontext)
            		.hostnameVerifier((s1,s2) -> true)
                .build();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close(){
        this.client.close();
    }

    public String getIAMToken() {
        String token = null;
        Response resAuth = null;
        try {
            final WebTarget targetAuth = client.target(endPointURL);
            
            System.out.println("Getting Authentication token for the prediction service..."); 
            LOG.info(endPointURL + " username= " + this.username + " " + api_key);
            JsonObject payload = Json.createObjectBuilder()
            .add("username", this.username)
            .add("api_key", this.api_key)
            .build();
            resAuth = targetAuth.request().post(Entity.json(payload));
    
            if (resAuth.getStatus() == 200){
                JsonReader reader = Json.createReader(new StringReader(resAuth.readEntity(String.class)));
                JsonObject ibmAMResponse = reader.readObject();
                token = ibmAMResponse.getString("token");
            } else {
                System.out.println("[ERROR] - The IAM service returned: " + resAuth.getStatus());
            }
            resAuth.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (resAuth != null) {
                resAuth.close();
            }
        }
        LOG.info("Token = " + token);
        return token;
    }


    public ScoringResult assessTelemetry(ScoringTelemetryWrapper telemetry) {
        if (this.wml_token == null) {
            synchronized (this) { 
                wml_token = getIAMToken();
            }
        }
        WebTarget targetResource = client.target(this.predictionURL);
        String telemetryAsString = jsonb.toJson(telemetry);
        LOG.error(telemetryAsString);
        Entity<String> entity = Entity.json(telemetryAsString);

        Response  rep = targetResource.request().header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + wml_token)
            .accept(MediaType.APPLICATION_JSON)
            .post(entity);
        
        String repAsString = rep.readEntity(String.class);
        LOG.error(repAsString);
        try {
            ScoringResult result = jsonb.fromJson(repAsString,ScoringResult.class);
            return result;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        
    
    }
}
