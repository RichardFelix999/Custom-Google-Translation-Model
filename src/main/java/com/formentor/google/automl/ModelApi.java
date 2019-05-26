package com.formentor.google.automl;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.automl.v1beta1.*;

import java.io.IOException;

public class ModelApi {

    /**
     * Given a dataset create a model
     *
     * @param projectId the Id of the project.
     * @param computeRegion the Region name.
     * @param dataSetId the Id of the dataset to which model is created.
     * @param modelName the Name of the model.
     * @throws Exception on AutoML Client errors
     */
    public static String createModel(
            String projectId, String computeRegion, String dataSetId, String modelName) throws Exception {
        // Instantiates a client
        AutoMlClient client = AutoMlClient.create();

        // A resource that represents Google Cloud Platform location.
        LocationName projectLocation = LocationName.of(projectId, computeRegion);

        // Set model metadata.
        /**
         * BaseModel
         * " The resource name of the model to use as a baseline to train the custom model.
         * " If unset, we use the default base model provided by Google Translate.
         * " Format: projects/{project_id}/locations/{location_id}/models/{model_id}
         */
        TranslationModelMetadata translationModelMetadata =
                TranslationModelMetadata.newBuilder().setBaseModel("").build();

        // Set model name, dataset and metadata.
        Model myModel =
                Model.newBuilder()
                        .setDisplayName(modelName)
                        .setDatasetId(dataSetId)
                        .setTranslationModelMetadata(translationModelMetadata)
                        .build();

        // Create a model with the model metadata in the region.
        OperationFuture<Model, OperationMetadata> response =
                client.createModelAsync(projectLocation, myModel);

        return response.getInitialFuture().get().getName();
    }

    /**
     * Lists models in project
     *
     * @param projectId the Id of the project.
     * @param computeRegion the Region name.
     * @param filter the filter expression.
     * @throws IOException on Input/Output errors.
     */
    public static Iterable<Model> listModels(String projectId, String computeRegion, String filter)
            throws IOException {
        // Instantiates a client
        AutoMlClient client = AutoMlClient.create();

        // A resource that represents Google Cloud Platform location.
        LocationName projectLocation = LocationName.of(projectId, computeRegion);

        // Create list models request.
        ListModelsRequest listModlesRequest =
                ListModelsRequest.newBuilder()
                        .setParent(projectLocation.toString())
                        .setFilter(filter)
                        .build();

        // List all the models available in the region by applying filter.
        return client.listModels(listModlesRequest).iterateAll();
    }

    public static Model getModelByDisplayName(String projectId, String computeRegion, String displayName) throws IOException {
        Iterable<Model> models = ModelApi.listModels(projectId, computeRegion, "");

        for (Model model : models) {
            if (displayName.equals(model.getDisplayName())) {
                return model;
            }
        }

        return null;
    }
    /**
     * List model evaluations
     *
     * @param projectId the Id of the project.
     * @param computeRegion the Region name.
     * @param modelId the Id of the model.
     * @param filter the filter expression.
     * @throws IOException on Input/Output errors.
     */
    public static Iterable<ModelEvaluation> listModelEvaluations(
            String projectId, String computeRegion, String modelId, String filter) throws IOException {
        // Instantiates a client
        AutoMlClient client = AutoMlClient.create();

        // Get the full path of the model.
        ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

        // Create list model evaluations request
        ListModelEvaluationsRequest modelEvaluationsrequest =
                ListModelEvaluationsRequest.newBuilder()
                        .setParent(modelFullId.toString())
                        .setFilter(filter)
                        .build();

        // List all the model evaluations in the model by applying filter.
        return client.listModelEvaluations(modelEvaluationsrequest).iterateAll();
    }

}
