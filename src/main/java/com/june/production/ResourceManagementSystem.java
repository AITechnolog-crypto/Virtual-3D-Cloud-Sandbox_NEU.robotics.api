package com.june.production;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ressourcenverwaltung: Extraktion → ML-Training → Vorhersage → Transaktion.
 */
public class ResourceManagementSystem {
    private static final Logger logger = Logger.getLogger(ResourceManagementSystem.class.getName());

    private final IResourceExtractor extractor;
    private final IMachineLearningModel mlModel;
    private final IBlockchainWallet wallet;
    private final IErrorHandler errorHandler;

    public ResourceManagementSystem(IResourceExtractor extractor,
                                    IMachineLearningModel mlModel,
                                    IBlockchainWallet wallet,
                                    IErrorHandler errorHandler) {
        this.extractor = extractor;
        this.mlModel = mlModel;
        this.wallet = wallet;
        this.errorHandler = errorHandler;
    }

    public void manageResources() {
        try {
            List<Resource> resources = extractor.extractResources();
            List<ResourceData> data = convertToData(resources);
            mlModel.train(data);
            ResourcePrediction prediction = mlModel.predict(new ResourceData("NEU", 1234.0));
            wallet.processTransaction(new Transaction(prediction));
            logger.info("Ressourcenverteilung erfolgreich durchgeführt.");
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.handleException(e, "Fehler im Ressourcenverwaltungssystem.");
            } else {
                logger.log(Level.SEVERE, "Fehler im Ressourcenverwaltungssystem.", e);
            }
        }
    }

    private static List<ResourceData> convertToData(List<Resource> resources) {
        List<ResourceData> out = new ArrayList<>();
        if (resources == null) return out;
        for (Resource r : resources) {
            if (r == null) continue;
            out.add(new ResourceData(r.getType(), r.getAmount()));
        }
        return out;
    }

    // Demo main
    public static void main(String[] args) {
        ResourceManagementSystem rms = new ResourceManagementSystem(
                new AdvancedResourceExtractor(),
                new ResourceOptimizationModel(),
                new SecureBlockchainWallet(5000.0),
                new ErrorHandler()
        );
        rms.manageResources();
    }
}