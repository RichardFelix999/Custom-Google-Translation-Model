package com.formentor.google.automl;

import com.google.cloud.automl.v1beta1.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PredictionApi {

    public static String predict(
            String projectId, String computeRegion, String modelId, String content) throws IOException {

        // Instantiate client for prediction service.
        PredictionServiceClient predictionClient = PredictionServiceClient.create();

        // Get the full path of the model.
        ModelName name = ModelName.of(projectId, computeRegion, modelId);

        TextSnippet textSnippet = TextSnippet.newBuilder().setContent(content).build();

        // Set the payload by giving the content of the file.
        ExamplePayload payload = ExamplePayload.newBuilder().setTextSnippet(textSnippet).build();

        // Additional parameters that can be provided for prediction
        Map<String, String> params = new HashMap<>();

        PredictResponse response = predictionClient.predict(name, payload, params);
        TextSnippet translatedContent = response.getPayload(0).getTranslation().getTranslatedContent();

        return translatedContent.getContent();

    }

    public static String predictByModelDisplayName(
            String projectId, String computeRegion, String modelDisplayName, String content) throws IOException {
        // Get model by displayName
        final Model model = ModelApi.getModelByDisplayName(projectId, computeRegion, modelDisplayName);

        if (model == null) {
            return null;
        }

        return predict(projectId, computeRegion, model.getName().split("/")[model.getName().split("/").length - 1], content);
    }
}