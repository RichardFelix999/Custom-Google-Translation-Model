package com.formentor.magnolia.mltranslation.command;

import com.formentor.google.automl.DatasetApi;
import com.formentor.google.automl.ModelApi;
import com.google.cloud.automl.v1beta1.Dataset;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Getter
@Setter
public class TrainModelCommand extends MgnlCommand {

    /**
     * Google project Id
     */
    private String project_id;

    /**
     * Google compute region
     */
    private String compute_region;

    /**
     * Dataset name
     */
    private String dataset_name;

    /**
     * Model name
     */
    private String model_name;


    @Override
    public boolean execute(Context context) throws Exception {
        List<Dataset> datasets = DatasetApi.getDatasetByDisplayName(project_id, compute_region, dataset_name);
        if (datasets == null || datasets.isEmpty()) {
            log.error("TrainModel failure - dataset {} does not exist", dataset_name);
            return false;
        }
        final Dataset dataset = datasets.get(0);
        final String datasetId = dataset.getName().split("/")[dataset.getName().split("/").length - 1];

        String model_name_final = model_name;
        if (model_name_final == null) {
            final String datasetDisplayName = dataset.getDisplayName();
            model_name_final = buildModelName(datasetDisplayName);
        }

        // Launch training
        String response = ModelApi.createModel(project_id, compute_region, datasetId, model_name_final);

        return (response != null);
    }

    /**
     * Builds the name of the model
     * @param datasetName
     * @return
     */
    private String buildModelName(String datasetName) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return datasetName + "_v" + sdf.format(new Date());
    }

}
