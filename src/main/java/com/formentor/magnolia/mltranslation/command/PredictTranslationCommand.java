package com.formentor.magnolia.mltranslation.command;

import com.formentor.google.automl.PredictionApi;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class PredictTranslationCommand extends MgnlCommand {

    /**
     * Google project Id
     */
    private String project_id;

    /**
     * Google compute region
     */
    private String compute_region;

    /**
     * Model display name
     */
    private String model_display_name;

    /**
     * Model Id
     */
    private String model_id;

    /**
     * Text to be translated (translation predicted using a model)
     */
    private String text;

    /**
     * Output translation
     */
    private String translation;

    @Override
    public boolean execute(Context context) throws Exception {
        if (model_id == null && model_display_name == null) {
            log.error("PredictTranslationCommand failure - model_id and model_display_name must be provided");
            return false;
        }

        if (model_id != null && model_display_name != null) {
            log.error("PredictTranslationCommand failure - just model_id and model_display_name must be provided");
            return false;
        }

        // the target lang depends on the prediction model
        String translation = "";
        if (model_display_name != null) {
            translation = PredictionApi.predictByModelDisplayName(project_id, compute_region, model_display_name, text);
        } else if (model_id != null) {
            translation = PredictionApi.predict(project_id, compute_region, model_id, text);
        }

        log.warn("PredictTranslationCommand - translation of {} is {}", text, translation);
        setTranslation(translation);

        return true;
    }
}
