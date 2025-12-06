package com.june.config;

import com.june.service.PaywallEnterpriseApp;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "keys")
public class ApiKeysConfig {

    private GeminiConfig gemini = new GeminiConfig();
    private Google google = new Google();
    private Azure azure = new Azure();
    private Anthropic anthropic = new Anthropic();
    private OpenAi openAi = new OpenAi();
    private Aws aws = new Aws();
    private SwaggerHub swaggerHub = new SwaggerHub();
    private Stripe stripe = new Stripe();
    private Wordpress wordpress = new Wordpress();
    private SecureSwarm secureSwarm = new SecureSwarm();

    public GeminiConfig getGemini() { return gemini; }
    public void setGemini(GeminiConfig gemini) { this.gemini = gemini; }

    public Google getGoogle() { return google; }
    public void setGoogle(Google google) { this.google = google; }

    public Azure getAzure() { return azure; }
    public void setAzure(Azure azure) { this.azure = azure; }

    public Anthropic getAnthropic() { return anthropic; }
    public void setAnthropic(Anthropic anthropic) { this.anthropic = anthropic; }

    public OpenAi getOpenAi() { return openAi; }
    public void setOpenAi(OpenAi openAi) { this.openAi = openAi; }

    public Aws getAws() { return aws; }
    public void setAws(Aws aws) { this.aws = aws; }

    public SwaggerHub getSwaggerHub() { return swaggerHub; }
    public void setSwaggerHub(SwaggerHub swaggerHub) { this.swaggerHub = swaggerHub; }

    public Stripe getStripe() { return stripe; }
    public void setStripe(Stripe stripe) { this.stripe = stripe; }

    public Wordpress getWordpress() { return wordpress; }
    public void setWordpress(Wordpress wordpress) { this.wordpress = wordpress; }

    public SecureSwarm getSecureSwarm() { return secureSwarm; }
    public void setSecureSwarm(SecureSwarm secureSwarm) { this.secureSwarm = secureSwarm; }

    public static class GeminiConfig {
        private List<String> keys = new ArrayList<>();
        public List<String> getKeys() { return keys; }
        public void setKeys(List<String> keys) { this.keys = keys; }
    }

    public static class Google {
        private CloudProject cloudProject = new CloudProject();
        private GkeHub gkeHub = new GkeHub();
        private GeminiCloudAssist geminiCloudAssist = new GeminiCloudAssist();
        private Logging logging = new Logging();
        private Compute compute = new Compute();
        private KubernetesEngine kubernetesEngine = new KubernetesEngine();
        private VertexAi vertexAi = new VertexAi();
        private VertexAiGenerativeAi vertexAiGenerativeAi = new VertexAiGenerativeAi();
        private AutoMl autoMl = new AutoMl();
        private Pubsub pubsub = new Pubsub();
        private Chat chat = new Chat();
        private Dns dns = new Dns();
        private ServiceDirectory serviceDirectory = new ServiceDirectory();
        private ServiceHealth serviceHealth = new ServiceHealth();
        private ServiceNetworking serviceNetworking = new ServiceNetworking();
        private SecretManager secretManager = new SecretManager();
        private Checks checks = new Checks();
        private CloudTrace cloudTrace = new CloudTrace();
        private CloudSqlAdmin cloudSqlAdmin = new CloudSqlAdmin();
        private BigQuery bigQuery = new BigQuery();
        private BackupForGke backupForGke = new BackupForGke();
        private VpcAccess vpcAccess = new VpcAccess();
        private Optimization optimization = new Optimization();
        private OsConfig osConfig = new OsConfig();
        private Eventarc eventarc = new Eventarc();
        private Firebase firebase = new Firebase();
        private Dialogflow dialogflow = new Dialogflow();
        private Translation translation = new Translation();
        private Vision vision = new Vision();
        private SpeechToText speechToText = new SpeechToText();
        private TextToSpeech textToSpeech = new TextToSpeech();
        private VideoIntelligence videoIntelligence = new VideoIntelligence();
        private Calendar calendar = new Calendar();
        private Trends trends = new Trends();
        private ContainerFileSystem containerFileSystem = new ContainerFileSystem();
        private Adsense adsense = new Adsense();
        private Python python = new Python();
        private CloudRun cloudRun = new CloudRun();
        private AiPlatform aiPlatform = new AiPlatform();
        private NaturalLanguage naturalLanguage = new NaturalLanguage();
        private PlayDeveloperReporting playDeveloperReporting = new PlayDeveloperReporting();
        private Maps maps = new Maps();
        private Earth earth = new Earth();
        private String serviceAccountKey;
        private Monitoring monitoring = new Monitoring();
        private Maintenance maintenance = new Maintenance();

        public CloudProject getCloudProject() { return cloudProject; }
        public void setCloudProject(CloudProject cloudProject) { this.cloudProject = cloudProject; }
        public GkeHub getGkeHub() { return gkeHub; }
        public void setGkeHub(GkeHub gkeHub) { this.gkeHub = gkeHub; }
        public GeminiCloudAssist getGeminiCloudAssist() { return geminiCloudAssist; }
        public void setGeminiCloudAssist(GeminiCloudAssist geminiCloudAssist) { this.geminiCloudAssist = geminiCloudAssist; }
        public Logging getLogging() { return logging; }
        public void setLogging(Logging logging) { this.logging = logging; }
        public Compute getCompute() { return compute; }
        public void setCompute(Compute compute) { this.compute = compute; }
        public KubernetesEngine getKubernetesEngine() { return kubernetesEngine; }
        public void setKubernetesEngine(KubernetesEngine kubernetesEngine) { this.kubernetesEngine = kubernetesEngine; }
        public VertexAi getVertexAi() { return vertexAi; }
        public void setVertexAi(VertexAi vertexAi) { this.vertexAi = vertexAi; }
        public VertexAiGenerativeAi getVertexAiGenerativeAi() { return vertexAiGenerativeAi; }
        public void setVertexAiGenerativeAi(VertexAiGenerativeAi vertexAiGenerativeAi) { this.vertexAiGenerativeAi = vertexAiGenerativeAi; }
        public AutoMl getAutoMl() { return autoMl; }
        public void setAutoMl(AutoMl autoMl) { this.autoMl = autoMl; }
        public Pubsub getPubsub() { return pubsub; }
        public void setPubsub(Pubsub pubsub) { this.pubsub = pubsub; }
        public Chat getChat() { return chat; }
        public void setChat(Chat chat) { this.chat = chat; }
        public Dns getDns() { return dns; }
        public void setDns(Dns dns) { this.dns = dns; }
        public ServiceDirectory getServiceDirectory() { return serviceDirectory; }
        public void setServiceDirectory(ServiceDirectory serviceDirectory) { this.serviceDirectory = serviceDirectory; }
        public ServiceHealth getServiceHealth() { return serviceHealth; }
        public void setServiceHealth(ServiceHealth serviceHealth) { this.serviceHealth = serviceHealth; }
        public ServiceNetworking getServiceNetworking() { return serviceNetworking; }
        public void setServiceNetworking(ServiceNetworking serviceNetworking) { this.serviceNetworking = serviceNetworking; }
        public SecretManager getSecretManager() { return secretManager; }
        public void setSecretManager(SecretManager secretManager) { this.secretManager = secretManager; }
        public Checks getChecks() { return checks; }
        public void setChecks(Checks checks) { this.checks = checks; }
        public CloudTrace getCloudTrace() { return cloudTrace; }
        public void setCloudTrace(CloudTrace cloudTrace) { this.cloudTrace = cloudTrace; }
        public CloudSqlAdmin getCloudSqlAdmin() { return cloudSqlAdmin; }
        public void setCloudSqlAdmin(CloudSqlAdmin cloudSqlAdmin) { this.cloudSqlAdmin = cloudSqlAdmin; }
        public BigQuery getBigQuery() { return bigQuery; }
        public void setBigQuery(BigQuery bigQuery) { this.bigQuery = bigQuery; }
        public BackupForGke getBackupForGke() { return backupForGke; }
        public void setBackupForGke(BackupForGke backupForGke) { this.backupForGke = backupForGke; }
        public VpcAccess getVpcAccess() { return vpcAccess; }
        public void setVpcAccess(VpcAccess vpcAccess) { this.vpcAccess = vpcAccess; }
        public Optimization getOptimization() { return optimization; }
        public void setOptimization(Optimization optimization) { this.optimization = optimization; }
        public OsConfig getOsConfig() { return osConfig; }
        public void setOsConfig(OsConfig osConfig) { this.osConfig = osConfig; }
        public Eventarc getEventarc() { return eventarc; }
        public void setEventarc(Eventarc eventarc) { this.eventarc = eventarc; }
        public Firebase getFirebase() { return firebase; }
        public void setFirebase(Firebase firebase) { this.firebase = firebase; }
        public Dialogflow getDialogflow() { return dialogflow; }
        public void setDialogflow(Dialogflow dialogflow) { this.dialogflow = dialogflow; }
        public Translation getTranslation() { return translation; }
        public void setTranslation(Translation translation) { this.translation = translation; }
        public Vision getVision() { return vision; }
        public void setVision(Vision vision) { this.vision = vision; }
        public SpeechToText getSpeechToText() { return speechToText; }
        public void setSpeechToText(SpeechToText speechToText) { this.speechToText = speechToText; }
        public TextToSpeech getTextToSpeech() { return textToSpeech; }
        public void setTextToSpeech(TextToSpeech textToSpeech) { this.textToSpeech = textToSpeech; }
        public VideoIntelligence getVideoIntelligence() { return videoIntelligence; }
        public void setVideoIntelligence(VideoIntelligence videoIntelligence) { this.videoIntelligence = videoIntelligence; }
        public Calendar getCalendar() { return calendar; }
        public void setCalendar(Calendar calendar) { this.calendar = calendar; }
        public Trends getTrends() { return trends; }
        public void setTrends(Trends trends) { this.trends = trends; }
        public ContainerFileSystem getContainerFileSystem() { return containerFileSystem; }
        public void setContainerFileSystem(ContainerFileSystem containerFileSystem) { this.containerFileSystem = containerFileSystem; }
        public Adsense getAdsense() { return adsense; }
        public void setAdsense(Adsense adsense) { this.adsense = adsense; }
        public Python getPython() { return python; }
        public void setPython(Python python) { this.python = python; }
        public CloudRun getCloudRun() { return cloudRun; }
        public void setCloudRun(CloudRun cloudRun) { this.cloudRun = cloudRun; }
        public AiPlatform getAiPlatform() { return aiPlatform; }
        public void setAiPlatform(AiPlatform aiPlatform) { this.aiPlatform = aiPlatform; }
        public NaturalLanguage getNaturalLanguage() { return naturalLanguage; }
        public void setNaturalLanguage(NaturalLanguage naturalLanguage) { this.naturalLanguage = naturalLanguage; }
        public PlayDeveloperReporting getPlayDeveloperReporting() { return playDeveloperReporting; }
        public void setPlayDeveloperReporting(PlayDeveloperReporting playDeveloperReporting) { this.playDeveloperReporting = playDeveloperReporting; }
        public Maps getMaps() { return maps; }
        public void setMaps(Maps maps) { this.maps = maps; }
        public Earth getEarth() { return earth; }
        public void setEarth(Earth earth) { this.earth = earth; }
        public String getServiceAccountKey() { return serviceAccountKey; }
        public void setServiceAccountKey(String serviceAccountKey) { this.serviceAccountKey = serviceAccountKey; }
        public Monitoring getMonitoring() { return monitoring; }
        public void setMonitoring(Monitoring monitoring) { this.monitoring = monitoring; }
        public Maintenance getMaintenance() { return maintenance; }
        public void setMaintenance(Maintenance maintenance) { this.maintenance = maintenance; }

        public Python.PythonPubSub getPythonPubSub() { return python.getPythonPubSub(); }

        public static class CloudProject { private String id = ""; public String getId(){return id;} public void setId(String id){this.id=id;} }
        public static class GkeHub { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class GeminiCloudAssist { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Logging { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Compute { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class KubernetesEngine { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class VertexAi { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class VertexAiGenerativeAi { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class AutoMl { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Pubsub { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Chat { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Dns { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class ServiceDirectory { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class ServiceHealth { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class ServiceNetworking { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class SecretManager { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Checks { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class CloudTrace { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class CloudSqlAdmin { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class BigQuery { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class BackupForGke { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class VpcAccess { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Optimization { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class OsConfig { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Eventarc { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Firebase { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Dialogflow { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Translation { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Vision { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class SpeechToText { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class TextToSpeech { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class VideoIntelligence { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Calendar { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Trends { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class ContainerFileSystem { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Adsense { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class CloudRun { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class AiPlatform { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class NaturalLanguage { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class PlayDeveloperReporting { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Maps { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Earth { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Monitoring { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }
        public static class Maintenance { private List<String> keys = new ArrayList<>(); public List<String> getKeys(){return keys;} public void setKeys(List<String> keys){this.keys=keys;} }

        public static class Python {
            private PythonPubSub pythonPubSub = new PythonPubSub();
            public PythonPubSub getPython() { return pythonPubSub; }
            public void setPython(PythonPubSub pythonPubSub) { this.pythonPubSub = pythonPubSub; }
            public PythonPubSub getPythonPubSub() { return pythonPubSub; }
            public static class PythonPubSub {
                private List<String> keys = new ArrayList<>();
                private String publisherEndpoint = "";
                public List<String> getKeys() { return keys; }
                public void setKeys(List<String> keys) { this.keys = keys; }
                public String getPublisherEndpoint() { return publisherEndpoint; }
                public void setPublisherEndpoint(String publisherEndpoint) { this.publisherEndpoint = publisherEndpoint; }
            }
        }
    }

    public static class Azure {
        private String region;
        private String subscriptionKey;
        private Endpoints endpoints = new Endpoints();

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getSubscriptionKey() { return subscriptionKey; }
        public void setSubscriptionKey(String subscriptionKey) { this.subscriptionKey = subscriptionKey; }
        public Endpoints getEndpoints() { return endpoints; }
        public void setEndpoints(Endpoints endpoints) { this.endpoints = endpoints; }

        public static class Endpoints {
            private String aidvision;
            private String gemini12;
            private String atomAuge;
            private String atomAugePrediction;
            private String fastvision;
            private String fastvisionPrediction;
            private String gemini1;
            private String gemini1Prediction;
            private String massData;
            private String massDataPrediction;
            private String app;

            public String getAidvision() { return aidvision; }
            public void setAidvision(String aidvision) { this.aidvision = aidvision; }
            public String getGemini12() { return gemini12; }
            public void setGemini12(String gemini12) { this.gemini12 = gemini12; }
            public String getAtomAuge() { return atomAuge; }
            public void setAtomAuge(String atomAuge) { this.atomAuge = atomAuge; }
            public String getAtomAugePrediction() { return atomAugePrediction; }
            public void setAtomAugePrediction(String atomAugePrediction) { this.atomAugePrediction = atomAugePrediction; }
            public String getFastvision() { return fastvision; }
            public void setFastvision(String fastvision) { this.fastvision = fastvision; }
            public String getFastvisionPrediction() { return fastvisionPrediction; }
            public void setFastvisionPrediction(String fastvisionPrediction) { this.fastvisionPrediction = fastvisionPrediction; }
            public String getGemini1() { return gemini1; }
            public void setGemini1(String gemini1) { this.gemini1 = gemini1; }
            public String getGemini1Prediction() { return gemini1Prediction; }
            public void setGemini1Prediction(String gemini1Prediction) { this.gemini1Prediction = gemini1Prediction; }
            public String getMassData() { return massData; }
            public void setMassData(String massData) { this.massData = massData; }
            public String getMassDataPrediction() { return massDataPrediction; }
            public void setMassDataPrediction(String massDataPrediction) { this.massDataPrediction = massDataPrediction; }
            public String getApp() { return app; }
            public void setApp(String app) { this.app = app; }
        }
    }

    public static class Anthropic {
        private List<String> keys = new ArrayList<>();
        public List<String> getKeys() { return keys; }
        public void setKeys(List<String> keys) { this.keys = keys; }
    }

    public static class OpenAi {
        private List<String> keys = new ArrayList<>();
        public List<String> getKeys() { return keys; }
        public void setKeys(List<String> keys) { this.keys = keys; }
    }

    public static class Aws {
        private List<String> apiGatewayKeys = new ArrayList<>();
        public List<String> getApiGatewayKeys() { return apiGatewayKeys; }
        public void setApiGatewayKeys(List<String> keys) { this.apiGatewayKeys = keys; }
    }

    public static class SwaggerHub {
        private String apiKey;
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    }

    public static class Stripe {
        private String restrictedKey1;
        private String restrictedKey2;
        private String secretKey;

        public String getRestrictedKey1() { return restrictedKey1; }
        public void setRestrictedKey1(String restrictedKey1) { this.restrictedKey1 = restrictedKey1; }
        public String getRestrictedKey2() { return restrictedKey2; }
        public void setRestrictedKey2(String restrictedKey2) { this.restrictedKey2 = restrictedKey2; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    }

    public static class Wordpress {
        private String url;
        private String username;
        private String password;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class SecureSwarm {
        private NurSystemGlobal nurSystemGlobal = new NurSystemGlobal();
        private GreenBootsBioModulesPro greenBootsBioModulesPro = new GreenBootsBioModulesPro();
        private SharoSecureOps2in1 sharoSecureOps2in1 = new SharoSecureOps2in1();

        public NurSystemGlobal getNurSystemGlobal() { return nurSystemGlobal; }
        public void setNurSystemGlobal(NurSystemGlobal nurSystemGlobal) { this.nurSystemGlobal = nurSystemGlobal; }
        public GreenBootsBioModulesPro getGreenBootsBioModulesPro() { return greenBootsBioModulesPro; }
        public void setGreenBootsBioModulesPro(GreenBootsBioModulesPro greenBootsBioModulesPro) { this.greenBootsBioModulesPro = greenBootsBioModulesPro; }
        public SharoSecureOps2in1 getSharoSecureOps2in1() { return sharoSecureOps2in1; }
        public void setSharoSecureOps2in1(SharoSecureOps2in1 sharoSecureOps2in1) { this.sharoSecureOps2in1 = sharoSecureOps2in1; }

        public static class NurSystemGlobal {
            private List<String> keys = new ArrayList<>();
            public List<String> getKeys() { return keys; }
            public void setKeys(List<String> keys) { this.keys = keys; }
        }

        public static class GreenBootsBioModulesPro {
            private List<String> keys = new ArrayList<>();
            public List<String> getKeys() { return keys; }
            public void setKeys(List<String> keys) { this.keys = keys; }
        }

        public static class SharoSecureOps2in1 {
            private List<String> keys = new ArrayList<>();
            public List<String> getKeys() { return keys; }
            public void setKeys(List<String> keys) { this.keys = keys; }
        }
    }
}
