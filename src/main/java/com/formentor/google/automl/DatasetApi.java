package com.formentor.google.automl;

// Imports the Google Cloud client library

import com.google.cloud.automl.v1beta1.*;
import com.google.protobuf.Empty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class DatasetApi {

  // [START automl_translate_create_dataset]
  /**
   * Create dataset
   *
   * @param projectId the Google Cloud Project ID.
   * @param computeRegion the Region name. (e.g., "us-central1").
   * @param datasetName the name of the dataset to be created.
   * @param source the Source language
   * @param target the Target language
   * @throws IOException on Input/Output errors.
   */
  public static Dataset createDataset(
      String projectId, String computeRegion, String datasetName, String source, String target)
      throws IOException {
    // Instantiates a client
    AutoMlClient client = createAutoMlClient();

    // A resource that represents Google Cloud Platform location.
    LocationName projectLocation = LocationName.of(projectId, computeRegion);

    // Specify the source and target language.
    TranslationDatasetMetadata translationDatasetMetadata =
        TranslationDatasetMetadata.newBuilder()
            .setSourceLanguageCode(source)
            .setTargetLanguageCode(target)
            .build();

    // Set dataset name and dataset metadata.
    Dataset myDataset =
        Dataset.newBuilder()
            .setDisplayName(datasetName)
            .setTranslationDatasetMetadata(translationDatasetMetadata)
            .build();

    // Create a dataset with the dataset metadata in the region.
    Dataset dataset = client.createDataset(projectLocation, myDataset);

    return dataset;
  }

  /**
   * List AutoML datasets in a project
   * @param projectId the Google Cloud Project ID.
   * @param computeRegion the Region name. (e.g., "us-central1").
   * @param filter the Filter expression.
   * @throws Exception on AutoML Client errors
   */
  public static Iterable<Dataset> listDatasets(String projectId, String computeRegion, String filter)
          throws IOException {
    // Instantiates a client
    AutoMlClient client = createAutoMlClient();

    // A resource that represents Google Cloud Platform location.
    LocationName projectLocation = LocationName.of(projectId, computeRegion);

    ListDatasetsRequest request =
            ListDatasetsRequest.newBuilder()
                    .setParent(projectLocation.toString())
                    .setFilter(filter)
                    .build();

    return client.listDatasets(request).iterateAll();
  }

  /**
   * Return list of datasets with given displayname
   *
   * @param projectId the Google Cloud Project ID.
   * @param computeRegion the Region name. (e.g., "us-central1").
   * @param displayName the Filter expression.
   * @return
   * @throws IOException
   */
  public static List<Dataset> getDatasetByDisplayName(String projectId, String computeRegion, String displayName) throws IOException {
    List<Dataset> datasets = new ArrayList<>();

    for (Dataset dataset: listDatasets(projectId, computeRegion, "")) {
      if (dataset.getDisplayName().contains(displayName)) {
        datasets.add(dataset);
      }
    }

    return datasets;
  }

  /**
   * Get a dataset by ID.
   *
   * @param projectId the Google Cloud Project ID.
   * @param computeRegion the Region name. (e.g., "us-central1").
   * @param datasetId the Id of the dataset.
   * @throws Exception on AutoML Client errors
   */
  public static Dataset getDataset(String projectId, String computeRegion, String datasetId)
          throws Exception {
    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // Get the complete path of the dataset.
    DatasetName datasetFullId = DatasetName.of(projectId, computeRegion, datasetId);

    // Get all the information about a given dataset.
    return client.getDataset(datasetFullId);
  }

  /**
   * Return the DatasetId of Dataset
   * @param projectId
   * @param computeRegion
   * @param displayName
   * @return
   * @throws IOException
   */
  public static String getDatasetIdByName(String projectId, String computeRegion, String displayName) throws IOException {
    List<Dataset> datasets = new ArrayList<>();

    for (Dataset dataset: listDatasets(projectId, computeRegion, "")) {
      if (dataset.getDisplayName().contains(displayName)) {
        datasets.add(dataset);
      }
    }

    if (datasets != null && datasets.size() > 0) {
      return datasets.get(0).getName().split("/")[datasets.get(0).getName().split("/").length - 1];
    }

    return null;
  }
  /**
   * Import sentence pairs to the dataset.
   *
   * @param projectId the Google Cloud Project ID.
   * @param computeRegion the Region name. (e.g., "us-central1").
   * @param datasetId the Id of the dataset.
   * @param path the remote Path of the training data csv file.
   * @throws Exception on AutoML Client errors
   */
  public static Empty importData(
          String projectId, String computeRegion, String datasetId, String path) throws Exception {
    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // Get the complete path of the dataset.
    DatasetName datasetFullId = DatasetName.of(projectId, computeRegion, datasetId);

    GcsSource.Builder gcsSource = GcsSource.newBuilder();

    // Get multiple Google Cloud Storage URIs to import data from
    String[] inputUris = path.split(",");
    for (String inputUri : inputUris) {
      gcsSource.addInputUris(inputUri);
    }

    // Import data from the input URI
    InputConfig inputConfig = InputConfig.newBuilder().setGcsSource(gcsSource).build();

    Empty response = client.importDataAsync(datasetFullId, inputConfig).get();

    return response;
  }
  /**
   * Creates AutoMlClient
   * @return
   * @throws IOException
   */
  private static AutoMlClient createAutoMlClient() throws IOException {
    // Instantiates a client
    // java.io.IOException: The Application Default Credentials are not available. They are available if running in Google Compute Engine.
    // Otherwise, the environment variable GOOGLE_APPLICATION_CREDENTIALS must be defined pointing to a file defining the credentials (service-account.json).
    // See https://developers.google.com/accounts/docs/application-default-credentials for more information.
    return AutoMlClient.create();

    /**
     * Alternativa indicando el path del fichero de credenciales
     AutoMlSettings autoMlSettings =
     AutoMlSettings.newBuilder()
     .setCredentialsProvider(FixedCredentialsProvider.create(GoogleCredentials.fromStream(new FileInputStream(jsonPath)))
     .build();
     AutoMlClient autoMlClient = AutoMlClient.create(autoMlSettings);
     */

  }
}
