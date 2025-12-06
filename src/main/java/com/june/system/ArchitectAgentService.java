package com.june.system;

import com.june.ai.service.AiCloudService;
import com.june.gcloud.service.*;
import com.june.geospatial.service.AgentCommunicationBus;
import com.june.geospatial.service.IAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Der selbsterkennende Architekt-Agent.
 * Dieser Meta-Agent analysiert die Struktur und den Zustand der Anwendung selbst.
 * In seiner ersten Form ist er ein reiner Beobachter, der sein Wissen im Log protokolliert.
 */
@Service
public class ArchitectAgentService implements IAgent {

    private static final Logger logger = LoggerFactory.getLogger(ArchitectAgentService.class);
    private static final String AGENT_NAME = "ArchitectAgent";

    private final ApplicationContext applicationContext;
    private final RestTemplate restTemplate;
    private final AgentCommunicationBus communicationBus;
    private final InfrastructureManager infrastructureManager;
    private final AppDeploymentService appDeploymentService;
    private final FirebaseAppHostingService firebaseAppHostingService;
    private final EventarcService eventarcService;
    private final DnsService dnsService;
    private final PubSubService pubSubService;
    private final ServiceDirectoryService serviceDirectoryService;
    private final CertificateManagerService certificateManagerService;
    private final CloudDomainsService cloudDomainsService;
    private final VpcAccessService vpcAccessService;
    private final ServiceNetworkingService serviceNetworkingService;
    private final DialogflowService dialogflowService;
    private final TranslationService translationService;
    private final MachineLearningOrchestrator mlOrchestrator;
    private final GeminiCloudAssistService geminiCloudAssistService;
    private final GeminiForGcpService geminiForGcpService;
    private final CloudOptimizationService cloudOptimizationService;
    private final CloudMonitoringService cloudMonitoringService;
    private final BigQueryService bigQueryService;
    private final ChecksService checksService;
    private final ServiceHealthService serviceHealthService;
    private final CloudTraceService cloudTraceService;
    private final ErrorReportingService errorReportingService;
    private final PlayDeveloperReportingService playDeveloperReportingService;
    private final CloudLoggingService cloudLoggingService;
    private final ContainerFileSystemService containerFileSystemService;
    private final KubernetesEngineService kubernetesEngineService;
    private final CloudAutoscalingService cloudAutoscalingService;
    private final OsConfigService osConfigService;
    private final BackupForGkeService backupForGkeService;
    private final SecretManagerService secretManagerService;
    private final GkeHubService gkeHubService;
    private final VertexAiGenerativeAiService vertexAiGenerativeAiService;
    private final CloudSqlAdminService cloudSqlAdminService;
    private final AlloyDbService alloyDbService;
    private final HauptService hauptService;
    private final AiCloudService aiCloudService;

    private final List<String> hiredAssistants = new ArrayList<>();

    @Value("${server.port}")
    private int serverPort;

    @Value("${api.google.cloud.project.id}")
    private String projectId;

    @Autowired
    public ArchitectAgentService(ApplicationContext applicationContext, RestTemplate restTemplate, AgentCommunicationBus communicationBus, InfrastructureManager infrastructureManager, AppDeploymentService appDeploymentService, FirebaseAppHostingService firebaseAppHostingService, EventarcService eventarcService, DnsService dnsService, PubSubService pubSubService, ServiceDirectoryService serviceDirectoryService, CertificateManagerService certificateManagerService, CloudDomainsService cloudDomainsService, VpcAccessService vpcAccessService, ServiceNetworkingService serviceNetworkingService, DialogflowService dialogflowService, TranslationService translationService, MachineLearningOrchestrator mlOrchestrator, GeminiCloudAssistService geminiCloudAssistService, GeminiForGcpService geminiForGcpService, CloudOptimizationService cloudOptimizationService, CloudMonitoringService cloudMonitoringService, BigQueryService bigQueryService, ChecksService checksService, ServiceHealthService serviceHealthService, CloudTraceService cloudTraceService, ErrorReportingService errorReportingService, PlayDeveloperReportingService playDeveloperReportingService, CloudLoggingService cloudLoggingService, ContainerFileSystemService containerFileSystemService, KubernetesEngineService kubernetesEngineService, CloudAutoscalingService cloudAutoscalingService, OsConfigService osConfigService, BackupForGkeService backupForGkeService, SecretManagerService secretManagerService, GkeHubService gkeHubService, VertexAiGenerativeAiService vertexAiGenerativeAiService, CloudSqlAdminService cloudSqlAdminService, AlloyDbService alloyDbService, HauptService hauptService, AiCloudService aiCloudService) {
        this.applicationContext = applicationContext;
        this.restTemplate = restTemplate;
        this.communicationBus = communicationBus;
        this.infrastructureManager = infrastructureManager;
        this.appDeploymentService = appDeploymentService;
        this.firebaseAppHostingService = firebaseAppHostingService;
        this.eventarcService = eventarcService;
        this.dnsService = dnsService;
        this.pubSubService = pubSubService;
        this.serviceDirectoryService = serviceDirectoryService;
        this.certificateManagerService = certificateManagerService;
        this.cloudDomainsService = cloudDomainsService;
        this.vpcAccessService = vpcAccessService;
        this.serviceNetworkingService = serviceNetworkingService;
        this.dialogflowService = dialogflowService;
        this.translationService = translationService;
        this.mlOrchestrator = mlOrchestrator;
        this.geminiCloudAssistService = geminiCloudAssistService;
        this.geminiForGcpService = geminiForGcpService;
        this.cloudOptimizationService = cloudOptimizationService;
        this.cloudMonitoringService = cloudMonitoringService;
        this.bigQueryService = bigQueryService;
        this.checksService = checksService;
        this.serviceHealthService = serviceHealthService;
        this.cloudTraceService = cloudTraceService;
        this.errorReportingService = errorReportingService;
        this.playDeveloperReportingService = playDeveloperReportingService;
        this.cloudLoggingService = cloudLoggingService;
        this.containerFileSystemService = containerFileSystemService;
        this.kubernetesEngineService = kubernetesEngineService;
        this.cloudAutoscalingService = cloudAutoscalingService;
        this.osConfigService = osConfigService;
        this.backupForGkeService = backupForGkeService;
        this.secretManagerService = secretManagerService;
        this.gkeHubService = gkeHubService;
        this.vertexAiGenerativeAiService = vertexAiGenerativeAiService;
        this.cloudSqlAdminService = cloudSqlAdminService;
        this.alloyDbService = alloyDbService;
        this.hauptService = hauptService;
        this.aiCloudService = aiCloudService;
    }

    @PostConstruct
    public void registerAgent() {
        communicationBus.register(this);
    }

    @Override
    public String getAgentName() {
        return AGENT_NAME;
    }

    @Override
    public void receiveMessage(String sender, String message) {
        logger.info("Nachricht von '{}' erhalten: {}", sender, message);
        // Hier könnte der Agent auf Nachrichten von anderen Agenten reagieren
        if (message.startsWith("Deploy app:")) {
            String imageName = message.substring("Deploy app:".length()).trim();
            deployApp(imageName);
        } else if (message.startsWith("Train model:")) {
            String dataset = message.substring("Train model:".length()).trim();
            trainVertexAiModel("MyCustomModel", dataset);
        } else if (message.startsWith("Optimize infrastructure:")) {
            String prompt = message.substring("Optimize infrastructure:".length()).trim();
            optimizeInfrastructure(prompt);
        } else if (message.startsWith("Get code suggestions:")) {
            String codeSnippet = message.substring("Get code suggestions:".length()).trim();
            getCodeSuggestions(codeSnippet);
        } else if (message.startsWith("Solve route:")) {
            String routeRequest = message.substring("Solve route:".length()).trim();
            solveOptimizationProblem(routeRequest);
        } else if (message.startsWith("Train AutoML model:")) {
            String[] parts = message.substring("Train AutoML model:".length()).trim().split(",");
            if (parts.length == 2) {
                trainAutoMLModel(parts[0].trim(), parts[1].trim());
            }
        } else if (message.startsWith("Analyze sentiment:")) {
            String text = message.substring("Analyze sentiment:".length()).trim();
            analyzeSentiment(text);
        } else if (message.startsWith("Analyze entities:")) {
            String text = message.substring("Analyze entities:".length()).trim();
            analyzeEntities(text);
        } else if (message.startsWith("Get metric data:")) {
            String[] parts = message.substring("Get metric data:".length()).trim().split(",");
            if (parts.length == 2 && !parts[0].trim().isEmpty()) {
                getMetricData(parts[0].trim(), parts[1].trim());
            } else {
                logger.warn("Ungültiger Befehl 'Get metric data:'. Erwartet: 'Get metric data: <metricType>,<filter>'");
            }
        } else if (message.startsWith("Execute BigQuery query:")) {
            String query = message.substring("Execute BigQuery query:".length()).trim();
            executeBigQueryQuery(query);
        } else if (message.startsWith("List checks:")) {
            listChecks();
        } else if (message.startsWith("List service health events:")) {
            listServiceHealthEvents();
        } else if (message.startsWith("List traces:")) {
            String filter = message.substring("List traces:".length()).trim();
            listTraces(filter);
        } else if (message.startsWith("List error groups:")) {
            String projectId = message.substring("List error groups:".length()).trim();
            listErrorGroups(projectId);
        } else if (message.startsWith("Get app reports:")) {
            String packageName = message.substring("Get app reports:".length()).trim();
            getAppReports(packageName);
        } else if (message.startsWith("Write log entry:")) {
            String[] parts = message.substring("Write log entry:".length()).trim().split(",");
            if (parts.length == 2) {
                writeLogEntry(parts[0].trim(), parts[1].trim());
            }
        } else if (message.startsWith("List log entries:")) {
            String filter = message.substring("List log entries:".length()).trim();
            listLogEntries(filter);
        } else if (message.startsWith("Get prefetch image report:")) {
            String image = message.substring("Get prefetch image report:".length()).trim();
            getPrefetchImageReport(image);
        } else if (message.startsWith("List Kubernetes clusters:")) {
            String[] parts = message.substring("List Kubernetes clusters:".length()).trim().split(",");
            if (parts.length == 2) {
                listKubernetesClusters(parts[0].trim(), parts[1].trim());
            }
        } else if (message.startsWith("Get Kubernetes cluster:")) {
            String[] parts = message.substring("Get Kubernetes cluster:".length()).trim().split(",");
            if (parts.length == 3) {
                getKubernetesCluster(parts[0].trim(), parts[1].trim(), parts[2].trim());
            }
        } else if (message.startsWith("Diagnose DNS error:")) {
            String zone = message.substring("Diagnose DNS error:".length()).trim();
            diagnoseDnsError(zone);
        } else if (message.startsWith("Update autoscaling metrics:")) {
            String metricsData = message.substring("Update autoscaling metrics:".length()).trim();
            updateAutoscalingMetrics(metricsData);
        } else if (message.startsWith("List OS policies:")) {
            String instanceId = message.substring("List OS policies:".length()).trim();
            listOsPolicies(instanceId);
        } else if (message.startsWith("List backup plans:")) {
            String location = message.substring("List backup plans:".length()).trim();
            listBackupPlans(location);
        } else if (message.startsWith("Access secret:")) {
            String secretName = message.substring("Access secret:".length()).trim();
            accessSecret(secretName);
        } else if (message.startsWith("List GKE Hub memberships:")) {
            String[] parts = message.substring("List GKE Hub memberships:".length()).trim().split(",");
            if (parts.length == 2) {
                listGkeHubMemberships(parts[0].trim(), parts[1].trim());
            }
        } else if (message.startsWith("Get GKE Hub feature:")) {
            String[] parts = message.substring("Get GKE Hub feature:".length()).trim().split(",");
            if (parts.length == 3) {
                getGkeHubFeature(parts[0].trim(), parts[1].trim(), parts[2].trim());
            }
        } else if (message.startsWith("Generate chat:")) {
            String prompt = message.substring("Generate chat:".length()).trim();
            generateChat(prompt);
        } else if (message.startsWith("Generate code:")) {
            String prompt = message.substring("Generate code:".length()).trim();
            generateCode(prompt);
        } else if (message.startsWith("List Cloud SQL instances:")) {
            String projectId = message.substring("List Cloud SQL instances:".length()).trim();
            listCloudSqlInstances(projectId);
        } else if (message.startsWith("List AlloyDB clusters:")) {
            String[] parts = message.substring("List AlloyDB clusters:".length()).trim().split(",");
            if (parts.length == 2) {
                listAlloyDbClusters(parts[0].trim(), parts[1].trim());
            }
        } else if (message.startsWith("Run KI analysis:")) {
            // Hier würden wir die Parameter für die KI-Analyse übergeben
            Map<String, Object> cloudDaten = new HashMap<>();
            cloudDaten.put("cpuUsage", 0.95);
            cloudDaten.put("errorCount", 10);
            runKiAnalysis(cloudDaten);
        } else if (message.startsWith("Predict with DJL:")) {
            String[] parts = message.substring("Predict with DJL:".length()).trim().split(",");
            if (parts.length == 2) {
                predictWithDjl(parts[0].trim(), parts[1].trim());
            }
        }
    }

    /**
     * Führt eine KI-Analyse durch.
     */
    public void runKiAnalysis(Map<String, Object> cloudDaten) {
        logger.info("🧠 [{}] Mission: KI-Analyse. Führe Analyse durch...", AGENT_NAME);
        try {
            hauptService.performAnalysis(cloudDaten);
            logger.info("   -> KI-Analyse erfolgreich abgeschlossen.");
        } catch (Exception e) {
            logger.error("   -> ❌ KI-Analyse fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Macht eine Vorhersage mit einem DJL-Modell.
     */
    public void predictWithDjl(String modelName, String input) {
        logger.info("🧠 [{}] Mission: DJL Prediction. Mache Vorhersage mit Modell '{}' für Input '{}'...", AGENT_NAME, modelName, input);
        try {
            AiCloudService.PredictionResult result = aiCloudService.predict(modelName, input);
            if (result.isSuccess()) {
                logger.info("   -> DJL Vorhersage erfolgreich. Ergebnis: {}", result.getOutput());
            } else {
                logger.error("   -> ❌ DJL Vorhersage fehlgeschlagen: {}", result.getError());
            }
        } catch (Exception e) {
            logger.error("   -> ❌ DJL Vorhersage fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Stellt eine neue Anwendung in der Cloud bereit.
     */
    public void deployApp(String imageName) {
        logger.info("🚀 [{}] Mission: Deployment. Stelle neue Anwendung bereit...", AGENT_NAME);
        try {
            String response = appDeploymentService.deployToCloudRun(imageName);
            logger.info("   -> Deployment erfolgreich. Antwort: {}", response);
            communicationBus.sendMessage(AGENT_NAME, "GoogleChatService", "Neue Anwendung wurde bereitgestellt: " + response);
        } catch (Exception e) {
            logger.error("   -> ❌ Deployment fehlgeschlagen: {}", e.getMessage(), e);
            communicationBus.sendMessage(AGENT_NAME, "GoogleChatService", "Deployment fehlgeschlagen: " + e.getMessage());
        }
    }

    /**
     * Stellt eine neue Web-Anwendung auf Firebase bereit.
     */
    public void deployWebApp(String siteId, String version) {
        logger.info("🚀 [{}] Mission: Web-Deployment. Stelle neue Web-Anwendung auf Firebase bereit...", AGENT_NAME);
        try {
            String response = firebaseAppHostingService.deployToFirebase(siteId, version);
            logger.info("   -> Web-Deployment erfolgreich. Antwort: {}", response);
            communicationBus.sendMessage(AGENT_NAME, "GoogleChatService", "Neue Web-Anwendung wurde bereitgestellt: " + response);
        } catch (Exception e) {
            logger.error("   -> ❌ Web-Deployment fehlgeschlagen: {}", e.getMessage(), e);
            communicationBus.sendMessage(AGENT_NAME, "GoogleChatService", "Web-Deployment fehlgeschlagen: " + e.getMessage());
        }
    }

    /**
     * Erstellt einen neuen Eventarc-Trigger.
     */
    public void createEventarcTrigger(String eventType, String targetService) {
        logger.info("⚡️ [{}] Mission: Eventing. Erstelle neuen Eventarc-Trigger...", AGENT_NAME);
        try {
            String response = eventarcService.createTrigger(eventType, targetService);
            logger.info("   -> Eventarc-Trigger erfolgreich erstellt. Antwort: {}", response);
            communicationBus.sendMessage(AGENT_NAME, "GoogleChatService", "Neuer Eventarc-Trigger wurde erstellt: " + response);
        } catch (Exception e) {
            logger.error("   -> ❌ Erstellung des Eventarc-Triggers fehlgeschlagen: {}", e.getMessage(), e);
            communicationBus.sendMessage(AGENT_NAME, "GoogleChatService", "Erstellung des Eventarc-Triggers fehlgeschlagen: " + e.getMessage());
        }
    }

    /**
     * Listet DNS-Einträge für eine Zone auf.
     */
    public void listDnsRecords(String zone) {
        logger.info("🌐 [{}] Mission: DNS. Liste DNS-Einträge für Zone '{}'...", AGENT_NAME, zone);
        try {
            String response = dnsService.listDnsRecords(zone);
            logger.info("   -> DNS-Einträge erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der DNS-Einträge fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Veröffentlicht eine Nachricht in einem Pub/Sub-Thema.
     */
    public void publishMessage(String topic, String message) {
        logger.info("✉️ [{}] Mission: Messaging. Veröffentliche Nachricht in Thema '{}'...", AGENT_NAME, topic);
        try {
            String response = pubSubService.publishMessage(topic, message);
            logger.info("   -> Nachricht erfolgreich veröffentlicht. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Veröffentlichung der Nachricht fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Listet Dienste in einem Namespace auf.
     */
    public void listServices(String namespace) {
        logger.info("🗺️ [{}] Mission: Service Discovery. Liste Dienste in Namespace '{}'...", AGENT_NAME, namespace);
        try {
            String response = serviceDirectoryService.listServices(namespace);
            logger.info("   -> Dienste erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der Dienste fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Listet SSL-Zertifikate auf.
     */
    public void listCertificates() {
        logger.info("🔒 [{}] Mission: Security. Liste SSL-Zertifikate...", AGENT_NAME);
        try {
            String response = certificateManagerService.listCertificates();
            logger.info("   -> SSL-Zertifikate erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der SSL-Zertifikate fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Listet Domains auf.
     */
    public void listDomains() {
        logger.info("🌍 [{}] Mission: Domains. Liste Domains...", AGENT_NAME);
        try {
            String response = cloudDomainsService.listDomains();
            logger.info("   -> Domains erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der Domains fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Listet VPC-Konnektoren auf.
     */
    public void listVpcConnectors() {
        logger.info("🔌 [{}] Mission: VPC Access. Liste VPC-Konnektoren...", AGENT_NAME);
        try {
            String response = vpcAccessService.listConnectors();
            logger.info("   -> VPC-Konnektoren erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der VPC-Konnektoren fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Listet Netzwerkverbindungen auf.
     */
    public void listNetworkConnections() {
        logger.info("🔗 [{}] Mission: Service Networking. Liste Netzwerkverbindungen...", AGENT_NAME);
        try {
            String response = serviceNetworkingService.listConnections();
            logger.info("   -> Netzwerkverbindungen erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der Netzwerkverbindungen fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Übersetzt einen Text in eine Zielsprache.
     */
    public void translateText(String text, String targetLanguage) {
        logger.info("🗣️ [{}] Mission: Translation. Übersetze Text '{}' nach '{}'...", AGENT_NAME, text, targetLanguage);
        try {
            String translatedText = translationService.translateText(text, targetLanguage);
            logger.info("   -> Text erfolgreich übersetzt. Ergebnis: {}", translatedText);
        } catch (Exception e) {
            logger.error("   -> ❌ Übersetzung fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Analysiert die Stimmung eines Textes.
     */
    public void analyzeSentiment(String text) {
        logger.info("💬 [{}] Mission: Natural Language. Analysiere Stimmung von Text '{}'...", AGENT_NAME, text);
        try {
            String sentiment = naturalLanguageService.analyzeSentiment(text);
            logger.info("   -> Stimmung erfolgreich analysiert. Ergebnis: {}", sentiment);
        } catch (Exception e) {
            logger.error("   -> ❌ Stimmungsanalyse fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Analysiert Entitäten in einem Text.
     */
    public void analyzeEntities(String text) {
        logger.info("🔍 [{}] Mission: Natural Language. Analysiere Entitäten in Text '{}'...", AGENT_NAME, text);
        try {
            String entities = naturalLanguageService.analyzeEntities(text);
            logger.info("   -> Entitäten erfolgreich analysiert. Ergebnis: {}", entities);
        } catch (Exception e) {
            logger.error("   -> ❌ Entitätenanalyse fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Trainiert ein Vertex AI Modell.
     */
    public void trainVertexAiModel(String modelDisplayName, String trainingPipelineDefinition) {
        logger.info("🧠 [{}] Mission: ML Training. Trainiere Vertex AI Modell '{}'...", AGENT_NAME, modelDisplayName);
        try {
            String response = mlOrchestrator.trainVertexAiModel(modelDisplayName, trainingPipelineDefinition);
            logger.info("   -> Vertex AI Modelltraining erfolgreich gestartet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Vertex AI Modelltraining fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Trainiert ein AutoML Modell.
     */
    public void trainAutoMLModel(String datasetId, String modelDisplayName) {
        logger.info("🧠 [{}] Mission: AutoML Training. Trainiere AutoML Modell '{}' mit Dataset '{}'...", AGENT_NAME, modelDisplayName, datasetId);
        try {
            String response = mlOrchestrator.trainAutoMLModel(datasetId, modelDisplayName);
            logger.info("   -> AutoML Modelltraining erfolgreich gestartet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ AutoML Modelltraining fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Stellt ein Vertex AI Modell bereit.
     */
    public void deployVertexAiModel(String modelId, String endpointDisplayName) {
        logger.info("🧠 [{}] Mission: ML Deployment. Stelle Vertex AI Modell '{}' bereit...", AGENT_NAME, modelId);
        try {
            String response = mlOrchestrator.deployVertexAiModel(modelId, endpointDisplayName);
            logger.info("   -> Vertex AI Modellbereitstellung erfolgreich. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Vertex AI Modellbereitstellung fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Macht eine Vorhersage mit einem Vertex AI Modell.
     */
    public void predictVertexAiModel(String endpointId, String instance) {
        logger.info("🧠 [{}] Mission: ML Prediction. Mache Vorhersage mit Vertex AI Modell an Endpunkt '{}'...", AGENT_NAME, endpointId);
        try {
            String response = mlOrchestrator.predictVertexAiModel(endpointId, instance);
            logger.info("   -> Vertex AI Vorhersage erfolgreich. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Vertex AI Vorhersage fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Optimiert die Infrastruktur mit Gemini Cloud Assist.
     */
    public void optimizeInfrastructure(String prompt) {
        logger.info("✨ [{}] Mission: Cloud Optimization. Optimiere Infrastruktur mit Gemini Cloud Assist...", AGENT_NAME);
        try {
            String response = geminiCloudAssistService.optimizeDeployment(prompt);
            logger.info("   -> Infrastruktur-Optimierung erfolgreich. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Infrastruktur-Optimierung fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Holt Code-Vorschläge von Gemini for Google Cloud.
     */
    public void getCodeSuggestions(String codeSnippet) {
        logger.info("💡 [{}] Mission: Code Assistance. Hole Code-Vorschläge von Gemini for Google Cloud...", AGENT_NAME);
        try {
            String response = geminiForGcpService.getCodeSuggestions(codeSnippet);
            logger.info("   -> Code-Vorschläge erfolgreich erhalten. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Code-Vorschläge fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Löst ein Optimierungsproblem mit der Cloud Optimization API.
     */
    public void solveOptimizationProblem(String routeRequest) {
        logger.info("📊 [{}] Mission: Optimization. Löse Optimierungsproblem mit Cloud Optimization API...", AGENT_NAME);
        try {
            String response = cloudOptimizationService.solveRoute(routeRequest);
            logger.info("   -> Optimierungsproblem erfolgreich gelöst. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Optimierungsproblem fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Ruft Metrikdaten von Cloud Monitoring ab.
     */
    public void getMetricData(String metricType, String filter) {
        logger.info("📈 [{}] Mission: Monitoring. Rufe Metrikdaten für Typ '{}' und Filter '{}' ab...", AGENT_NAME, metricType, filter);
        try {
            String response = cloudMonitoringService.getMetricData(metricType, filter);
            logger.info("   -> Metrikdaten erfolgreich abgerufen. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Abruf der Metrikdaten fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Listet Compliance-Checks auf.
     */
    public void listChecks() {
        logger.info("✅ [{}] Mission: Compliance. Liste Compliance-Checks...", AGENT_NAME);
        try {
            String response = checksService.listChecks();
            logger.info("   -> Compliance-Checks erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der Compliance-Checks fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Listet Service Health Events auf.
     */
    public void listServiceHealthEvents() {
        logger.info("🚨 [{}] Mission: Service Health. Liste Service Health Events...", AGENT_NAME);
        try {
            String response = serviceHealthService.listEvents();
            logger.info("   -> Service Health Events erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der Service Health Events fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Listet Traces auf.
     */
    public void listTraces(String filter) {
        logger.info("🔍 [{}] Mission: Tracing. Liste Traces mit Filter '{}'...", AGENT_NAME, filter);
        try {
            String response = cloudTraceService.listTraces(filter);
            logger.info("   -> Traces erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der Traces fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Listet Fehlergruppen auf.
     */
    public void listErrorGroups(String projectId) {
        logger.info("🐞 [{}] Mission: Error Reporting. Liste Fehlergruppen für Projekt '{}'...", AGENT_NAME, projectId);
        try {
            String response = errorReportingService.listGroupStats(projectId);
            logger.info("   -> Fehlergruppen erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der Fehlergruppen fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Ruft App-Berichte ab.
     */
    public void getAppReports(String packageName) {
        logger.info("📱 [{}] Mission: App Reporting. Rufe App-Berichte für Paket '{}'...", AGENT_NAME, packageName);
        try {
            String response = playDeveloperReportingService.getAppReports(packageName);
            logger.info("   -> App-Berichte erfolgreich abgerufen. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Abruf der App-Berichte fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Schreibt einen Log-Eintrag in Cloud Logging.
     */
    public void writeLogEntry(String logName, String message) {
        logger.info("📝 [{}] Mission: Logging. Schreibe Log-Eintrag in Log '{}'...", AGENT_NAME, logName);
        try {
            String response = cloudLoggingService.writeLogEntry(logName, message);
            logger.info("   -> Log-Eintrag erfolgreich geschrieben. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Schreiben des Log-Eintrags fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Listet Log-Einträge aus Cloud Logging auf.
     */
    public void listLogEntries(String filter) {
        logger.info("📜 [{}] Mission: Logging. Liste Log-Einträge mit Filter '{}'...", AGENT_NAME, filter);
        try {
            String response = cloudLoggingService.listLogEntries(filter);
            logger.info("   -> Log-Einträge erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der Log-Einträge fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Holt einen Prefetch Image Report vom Container File System.
     */
    public void getPrefetchImageReport(String image) {
        logger.info("📦 [{}] Mission: Container. Hole Prefetch Image Report für Image '{}'...", AGENT_NAME, image);
        try {
            String response = containerFileSystemService.getPrefetchImageReport(image);
            logger.info("   -> Prefetch Image Report erfolgreich abgerufen. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Abruf des Prefetch Image Reports fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Listet Kubernetes Cluster auf.
     */
    public void listKubernetesClusters(String projectId, String zone) {
        logger.info("☸️ [{}] Mission: Kubernetes. Liste Cluster in Projekt '{}' Zone '{}'...", AGENT_NAME, projectId, zone);
        try {
            String response = kubernetesEngineService.listClusters(projectId, zone);
            logger.info("   -> Kubernetes Cluster erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der Kubernetes Cluster fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Holt einen Kubernetes Cluster.
     */
    public void getKubernetesCluster(String projectId, String zone, String clusterId) {
        logger.info("☸️ [{}] Mission: Kubernetes. Hole Cluster '{}' in Projekt '{}' Zone '{}'...", AGENT_NAME, clusterId, projectId, zone);
        try {
            String response = kubernetesEngineService.getCluster(projectId, zone, clusterId);
            logger.info("   -> Kubernetes Cluster erfolgreich abgerufen. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Abruf des Kubernetes Clusters fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Aktualisiert Autoscaling-Metriken.
     */
    public void updateAutoscalingMetrics(String metricsData) {
        logger.info("📈 [{}] Mission: Autoscaling. Aktualisiere Autoscaling-Metriken...", AGENT_NAME);
        try {
            String response = cloudAutoscalingService.updateMetricsValues(metricsData);
            logger.info("   -> Autoscaling-Metriken erfolgreich aktualisiert. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Aktualisierung der Autoscaling-Metriken fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Listet OS-Richtlinien auf.
     */
    public void listOsPolicies(String instanceId) {
        logger.info("💻 [{}] Mission: OS Config. Liste OS-Richtlinien für Instanz '{}'...", AGENT_NAME, instanceId);
        try {
            String response = osConfigService.listOsPolicies(instanceId);
            logger.info("   -> OS-Richtlinien erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der OS-Richtlinien fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Listet Backup-Pläne für GKE auf.
     */
    public void listBackupPlans(String location) {
        logger.info("💾 [{}] Mission: Backup. Liste Backup-Pläne für GKE in '{}'...", AGENT_NAME, location);
        try {
            String response = backupForGkeService.listBackupPlans(location);
            logger.info("   -> Backup-Pläne erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der Backup-Pläne fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Greift auf ein Secret im Secret Manager zu.
     */
    public void accessSecret(String secretName) {
        logger.info("🔑 [{}] Mission: Security. Greife auf Secret '{}' zu...", AGENT_NAME, secretName);
        try {
            String secretValue = secretManagerService.accessSecret(secretName);
            logger.info("   -> Secret '{}' erfolgreich abgerufen. Wert: {}", secretName, secretValue);
        } catch (Exception e) {
            logger.error("   -> ❌ Zugriff auf Secret '{}' fehlgeschlagen: {}", secretName, e.getMessage(), e);
        }
    }

    /**
     * Listet GKE Hub Memberships auf.
     */
    public void listGkeHubMemberships(String project, String location) {
        logger.info("☸️ [{}] Mission: GKE Hub. Liste Memberships in Projekt '{}' Location '{}'...", AGENT_NAME, project, location);
        try {
            String response = gkeHubService.listMemberships(project, location);
            logger.info("   -> GKE Hub Memberships erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der GKE Hub Memberships fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Holt ein GKE Hub Feature.
     */
    public void getGkeHubFeature(String project, String location, String featureId) {
        logger.info("☸️ [{}] Mission: GKE Hub. Hole Feature '{}' in Projekt '{}' Zone '{}'...", AGENT_NAME, featureId, project, location);
        try {
            String response = gkeHubService.getFeature(project, location, featureId);
            logger.info("   -> GKE Hub Feature erfolgreich abgerufen. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Abruf des GKE Hub Features fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Generiert Chat-Antworten mit Vertex AI Generative AI.
     */
    public void generateChat(String prompt) {
        logger.info("💬 [{}] Mission: Generative AI. Generiere Chat-Antwort mit Vertex AI: '{}'...", AGENT_NAME, prompt);
        try {
            String response = vertexAiGenerativeAiService.generateChat(prompt);
            logger.info("   -> Chat-Antwort erfolgreich generiert. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Generierung der Chat-Antwort fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Generiert Code-Vorschläge mit Vertex AI Generative AI.
     */
    public void generateCode(String prompt) {
        logger.info("💡 [{}] Mission: Generative AI. Generiere Code-Vorschläge mit Vertex AI: '{}'...", AGENT_NAME, prompt);
        try {
            String response = vertexAiGenerativeAiService.generateCode(prompt);
            logger.info("   -> Code-Vorschläge erfolgreich generiert. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Generierung der Code-Vorschläge fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Listet Cloud SQL Instanzen auf.
     */
    public void listCloudSqlInstances(String projectId) {
        logger.info("🗄️ [{}] Mission: Cloud SQL. Liste Instanzen in Projekt '{}'...", AGENT_NAME, projectId);
        try {
            String response = cloudSqlAdminService.listInstances(projectId);
            logger.info("   -> Cloud SQL Instanzen erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der Cloud SQL Instanzen fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Listet AlloyDB Cluster auf.
     */
    public void listAlloyDbClusters(String projectId, String location) {
        logger.info("🗄️ [{}] Mission: AlloyDB. Liste Cluster in Projekt '{}' Location '{}'...", AGENT_NAME, projectId, location);
        try {
            String response = alloyDbService.listClusters(projectId, location);
            logger.info("   -> AlloyDB Cluster erfolgreich aufgelistet. Antwort: {}", response);
        } catch (Exception e) {
            logger.error("   -> ❌ Auflistung der AlloyDB Cluster fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Führt eine KI-Analyse durch.
     */
    public void runKiAnalysis2(Map<String, Object> cloudDaten) {
        logger.info("🧠 [{}] Mission: KI-Analyse. Führe Analyse durch...", AGENT_NAME);
        try {
            hauptService.performAnalysis(cloudDaten);
            logger.info("   -> KI-Analyse erfolgreich abgeschlossen.");
        } catch (Exception e) {
            logger.error("   -> ❌ KI-Analyse fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * Macht eine Vorhersage mit einem DJL-Modell.
     */
    public void predictWithDjl2(String modelName, String input) {
        logger.info("🧠 [{}] Mission: DJL Prediction. Mache Vorhersage mit Modell '{}' für Input '{}'...", AGENT_NAME, modelName, input);
        try {
            AiCloudService.PredictionResult result = aiCloudService.predict(modelName, input);
            if (result.isSuccess()) {
                logger.info("   -> DJL Vorhersage erfolgreich. Ergebnis: {}", result.getOutput());
            } else {
                logger.error("   -> ❌ DJL Vorhersage fehlgeschlagen: {}", result.getError());
            }
        } catch (Exception e) {
            logger.error("   -> ❌ DJL Vorhersage fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * MISSION 1: Die Inspektion (Architektur-Scan)
     * Lernt die Architektur, indem es alle aktiven Komponenten (Zahnräder) im Stammhirn findet.
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 30000) // Läuft alle 60 Minuten
    @Async
    public void inspectArchitecture() {
        logger.info("🏛️ [{}] Mission: Inspektion. Scanne Anwendungs-Architektur...", AGENT_NAME);
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        long juneBeans = Arrays.stream(beanNames).filter(name -> name.startsWith("com.june")).count();
        logger.info("   -> Inspektion abgeschlossen. {} aktive Komponenten (Zahnräder) im Stammhirn gefunden.", juneBeans);
    }

    /**
     * MISSION 2: Die Diagnose (Endpunkt-Check)
     * Überprüft die Gesundheit der externen "Arme" des Systems.
     */
    @Scheduled(fixedRate = 600000, initialDelay = 60000) // Läuft alle 10 Minuten
    @Async
    public void diagnoseEndpoints() {
        logger.info("🩺 [{}] Mission: Diagnose. Pinge kritische Endpunkte an...", AGENT_NAME);

        String baseUrl = "http://localhost:" + serverPort;

        // Diagnose GCP-Status
        try {
            String gcloudStatus = restTemplate.getForObject(baseUrl + "/api/gcloud/status", String.class);
            logger.info("   -> Diagnose GCP-Status: OK. Antwort erhalten.");
        } catch (ResourceAccessException e) {
            logger.error("   -> ❌ Diagnose GCP-Status: FEHLER. Endpunkt nicht erreichbar (Netzwerk/Verbindung): {}", e.getMessage());
        } catch (HttpClientErrorException e) {
            logger.error("   -> ❌ Diagnose GCP-Status: FEHLER. HTTP-Fehler ({}): {}", e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("   -> ❌ Diagnose GCP-Status: FEHLER. Unerwarteter Fehler: {}", e.getMessage(), e);
        }

        // Diagnose Geospatial-Status
        try {
            String geoStats = restTemplate.getForObject(baseUrl + "/api/geospatial/stats", String.class);
            logger.info("   -> Diagnose Geospatial-Status: OK. Antwort erhalten.");
        } catch (ResourceAccessException e) {
            logger.error("   -> ❌ Diagnose Geospatial-Status: FEHLER. Endpunkt nicht erreichbar (Netzwerk/Verbindung): {}", e.getMessage());
        } catch (HttpClientErrorException e) {
            logger.error("   -> ❌ Diagnose Geospatial-Status: FEHLER. HTTP-Fehler ({}): {}", e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("   -> ❌ Diagnose Geospatial-Status: FEHLER. Unerwarteter Fehler: {}", e.getMessage(), e);
        }

        // Diagnose Infrastruktur-Status
        try {
            String infraStatus = infrastructureManager.listInstances("your-zone");
            logger.info("   -> Diagnose Infrastruktur-Status: OK. Antwort erhalten: {}", infraStatus);
        } catch (Exception e) {
            logger.error("   -> ❌ Diagnose Infrastruktur-Status: FEHLER. Unerwarteter Fehler: {}", e.getMessage(), e);
        }

        // Analyse Monitoring-Daten
        analyzeMonitoringData();
    }

    /**
     * Analysiert Monitoring-Daten und schlägt bei Problemen Alarm.
     */
    private void analyzeMonitoringData() {
        logger.info("📈 [{}] Mission: Monitoring-Analyse. Analysiere Metriken...", AGENT_NAME);
        try {
            // Beispiel: Abrufen der Fehlerrate für CreateServiceTimeSeries
            String metricType = "logging.googleapis.com/log_entry_count"; // Beispielmetrik
            String filter = "metric.type=\"logging.googleapis.com/log_entry_count\" AND resource.type=\"gce_instance\""; // Beispielfilter
            String metricData = cloudMonitoringService.getMetricData(metricType, filter);
            logger.info("   -> Metrikdaten für '{}' erhalten: {}", metricType, metricData);

            // Hier würde eine komplexere Logik zur Analyse der Fehlerrate stehen.
            // Fürs Erste simulieren wir eine Erkennung basierend auf den von Ihnen bereitgestellten Daten.
            double errorRateCreateServiceTimeSeries = 5.47; // Aus Ihren Daten

            if (errorRateCreateServiceTimeSeries > 5.0) {
                String warningMessage = String.format("Hohe Fehlerrate (%.2f%%) bei CreateServiceTimeSeries erkannt! Ursache und Lösung mit Gemini Cloud Assist analysieren.", errorRateCreateServiceTimeSeries);
                logger.warn("   -> ⚠️ {}", warningMessage);
                communicationBus.sendMessage(AGENT_NAME, "GoogleChatService", warningMessage);

                // Gemini Cloud Assist um Hilfe bitten
                String geminiAssistPrompt = String.format("Analysiere die hohe Fehlerrate (%.2f%%) bei der Google Monitoring API Methode 'CreateServiceTimeSeries'. Was sind mögliche Ursachen und wie kann ich das beheben?", errorRateCreateServiceTimeSeries);
                optimizeInfrastructure(geminiAssistPrompt);
            }

            // NEU: Überprüfung der DNS-Fehler
            double errorRateDnsResponsePolicyRulesGet = 66.67; // Aus Ihren Daten
            if (errorRateDnsResponsePolicyRulesGet > 5.0) {
                String warningMessage = String.format("Kritische Fehlerrate (%.2f%%) bei DNS ResponsePolicyRules.Get erkannt! Ursache und Lösung mit Gemini Cloud Assist analysieren.", errorRateDnsResponsePolicyRulesGet);
                logger.error("   -> ❌ {}", warningMessage);
                communicationBus.sendMessage(AGENT_NAME, "GoogleChatService", warningMessage);

                // Gemini Cloud Assist um Hilfe bitten
                String geminiAssistPrompt = String.format("Analysiere die kritische Fehlerrate (%.2f%%) bei der Google Cloud DNS API Methode 'ResponsePolicyRulesService.Get'. Was sind mögliche Ursachen und wie kann ich das beheben?", errorRateDnsResponsePolicyRulesGet);
                optimizeInfrastructure(geminiAssistPrompt);
            }

            // NEU: Überprüfung der Autoscaling-Fehler
            double errorRateAutoscalingMetricsValues = 21.39; // Aus Ihren Daten
            if (errorRateAutoscalingMetricsValues > 5.0) {
                String warningMessage = String.format("Kritische Fehlerrate (%.2f%%) bei Autoscaling Metrics Values erkannt! Ursache und Lösung mit Gemini Cloud Assist analysieren.", errorRateAutoscalingMetricsValues);
                logger.error("   -> ❌ {}", warningMessage);
                communicationBus.sendMessage(AGENT_NAME, "GoogleChatService", warningMessage);

                // Gemini Cloud Assist um Hilfe bitten
                String geminiAssistPrompt = String.format("Analysiere die kritische Fehlerrate (%.2f%%) bei der Google Cloud Autoscaling API Methode 'StreamingUpdateMetricsValues'. Was sind mögliche Ursachen und wie kann ich das beheben?", errorRateAutoscalingMetricsValues);
                optimizeInfrastructure(geminiAssistPrompt);
            }

            // NEU: Überprüfung der GKE Hub GetFeature Fehler
            double errorRateGkeHubGetFeature = 100.0; // Aus Ihren Daten
            if (errorRateGkeHubGetFeature > 5.0) {
                String warningMessage = String.format("Kritische Fehlerrate (%.2f%%) bei GKE Hub GetFeature erkannt! Ursache und Lösung mit Gemini Cloud Assist analysieren.", errorRateGkeHubGetFeature);
                logger.error("   -> ❌ {}", warningMessage);
                communicationBus.sendMessage(AGENT_NAME, "GoogleChatService", warningMessage);

                // Gemini Cloud Assist um Hilfe bitten
                String geminiAssistPrompt = String.format("Analysiere die kritische Fehlerrate (%.2f%%) bei der Google GKE Hub API Methode 'GetFeature'. Was sind mögliche Ursachen und wie kann ich das beheben?", errorRateGkeHubGetFeature);
                optimizeInfrastructure(geminiAssistPrompt);
            }

            // NEU: Überprüfung der Container File System GetPrefetchImageReport Fehler
            double errorRateContainerFileSystemGetPrefetchImageReport = 100.0; // Aus Ihren Daten
            if (errorRateContainerFileSystemGetPrefetchImageReport > 5.0) {
                String warningMessage = String.format("Kritische Fehlerrate (%.2f%%) bei Container File System GetPrefetchImageReport erkannt! Ursache und Lösung mit Gemini Cloud Assist analysieren.", errorRateContainerFileSystemGetPrefetchImageReport);
                logger.error("   -> ❌ {}", warningMessage);
                communicationBus.sendMessage(AGENT_NAME, "GoogleChatService", warningMessage);

                // Gemini Cloud Assist um Hilfe bitten
                String geminiAssistPrompt = String.format("Analysiere die kritische Fehlerrate (%.2f%%) bei der Google Container File System API Methode 'GetPrefetchImageReport'. Was sind mögliche Ursachen und wie kann ich das beheben?", errorRateContainerFileSystemGetPrefetchImageReport);
                optimizeInfrastructure(geminiAssistPrompt);
            }

            // NEU: Überprüfung der GKE BackendServicesService.Get Fehler
            double errorRateGkeBackendServicesGet = 100.0; // Aus Ihren Daten
            if (errorRateGkeBackendServicesGet > 5.0) {
                String warningMessage = String.format("Kritische Fehlerrate (%.2f%%) bei GKE BackendServicesService.Get erkannt! Ursache und Lösung mit Gemini Cloud Assist analysieren.", errorRateGkeBackendServicesGet);
                logger.error("   -> ❌ {}", warningMessage);
                communicationBus.sendMessage(AGENT_NAME, "GoogleChatService", warningMessage);

                // Gemini Cloud Assist um Hilfe bitten
                String geminiAssistPrompt = String.format("Analysiere die kritische Fehlerrate (%.2f%%) bei der Google Kubernetes Engine API Methode 'BackendServicesService.Get'. Was sind mögliche Ursachen und wie kann ich das beheben?", errorRateGkeBackendServicesGet);
                optimizeInfrastructure(geminiAssistPrompt);
            }

            // NEU: Überprüfung der Cloud Logging WriteLogEntries Fehler
            double errorRateCloudLoggingWriteLogEntries = 0.168; // Aus Ihren Daten
            if (errorRateCloudLoggingWriteLogEntries > 0.1) { // Schwellenwert anpassen
                String warningMessage = String.format("Erhöhte Fehlerrate (%.3f%%) bei Cloud Logging WriteLogEntries erkannt! Ursache und Lösung mit Gemini Cloud Assist analysieren.", errorRateCloudLoggingWriteLogEntries);
                logger.warn("   -> ⚠️ {}", warningMessage);
                communicationBus.sendMessage(AGENT_NAME, "GoogleChatService", warningMessage);

                // Gemini Cloud Assist um Hilfe bitten
                String geminiAssistPrompt = String.format("Analysiere die erhöhte Fehlerrate (%.3f%%) bei der Google Cloud Logging API Methode 'WriteLogEntries'. Was sind mögliche Ursachen und wie kann ich das beheben?", errorRateCloudLoggingWriteLogEntries);
                optimizeInfrastructure(geminiAssistPrompt);
            }

            // Weitere Monitoring-Aufgaben
            listChecks();
            listServiceHealthEvents();
            listTraces("status=ERROR");
            listErrorGroups(projectId);
            getAppReports("your.package.name"); // Ersetzen Sie dies durch den tatsächlichen Paketnamen Ihrer App
            listLogEntries("severity=ERROR"); // Liste Fehler-Logs

            // Container File System API - GetPrefetchImageReport
            getPrefetchImageReport("your-image-name"); // Ersetzen Sie dies durch den tatsächlichen Image-Namen

            // Kubernetes Engine API - BackendServicesService.Get
            getKubernetesCluster(projectId, "your-zone", "your-cluster-id"); // Ersetzen Sie dies durch die tatsächlichen Werte

            // NEU: OS Config API - listOsPolicies
            listOsPolicies("your-instance-id"); // Ersetzen Sie dies durch die tatsächliche Instanz-ID

            // NEU: Backup for GKE API - listBackupPlans
            listBackupPlans("your-location"); // Ersetzen Sie dies durch den tatsächlichen Standort

            // NEU: Secret Manager API - accessSecret
            accessSecret("your-secret-name"); // Ersetzen Sie dies durch den tatsächlichen Secret-Namen

            // NEU: GKE Hub API - GetFeature
            getGkeHubFeature(projectId, "your-location", "your-feature-id"); // Ersetzen Sie dies durch die tatsächlichen Werte

            // NEU: Cloud SQL Admin API - listInstances
            listCloudSqlInstances(projectId);

            // NEU: AlloyDB API - listClusters
            listAlloyDbClusters(projectId, "your-location"); // Ersetzen Sie dies durch den tatsächlichen Standort

        } catch (Exception e) {
            logger.error("   -> ❌ Analyse der Monitoring-Daten fehlgeschlagen: {}", e.getMessage(), e);
        }
    }

    /**
     * MISSION 3: Die Selbstheilung (Integritäts-Check)
     * Prüft, ob die Verbindungen zwischen den Zahnrädern gültig sind.
     */
    @Scheduled(fixedRate = 1800000, initialDelay = 90000) // Läuft alle 30 Minuten
    @Async
    public void checkBeanIntegrity() {
        logger.info("⚙️ [{}] Mission: Integritäts-Check. Prüfe Verbindungen der Zahnräder...", AGENT_NAME);
        int warnings = 0;
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            if (beanName.startsWith("com.june")) { // Wir prüfen nur unsere eigenen Komponenten
                Object bean = applicationContext.getBean(beanName);
                for (Field field : bean.getClass().getDeclaredFields()) {
                    if (field.isAnnotationPresent(Autowired.class)) {
                        try {
                            // Prüfen, ob eine Bean dieses Typs im Context existiert
                            applicationContext.getBean(field.getType());
                        } catch (Exception e) {
                            logger.warn("   -> ❌ Integritäts-Warnung bei '{}': Benötigtes Zahnrad '{}' konnte nicht gefunden werden!", beanName, field.getType().getSimpleName());
                            warnings++;
                        }
                    }
                }
            }
        }

        if (warnings == 0) {
            logger.info("   -> Integritäts-Check abgeschlossen. Alle Verbindungen sind stabil.");
        } else {
            logger.warn("   -> Integritäts-Check abgeschlossen mit {} Warnungen. Überprüfung empfohlen.", warnings);
        }
    }
}
