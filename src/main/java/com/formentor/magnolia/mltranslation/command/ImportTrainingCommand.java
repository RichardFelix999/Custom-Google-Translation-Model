package com.formentor.magnolia.mltranslation.command;

import com.formentor.google.automl.DatasetApi;
import com.formentor.google.storage.StorageApi;
import com.formentor.magnolia.mltranslation.parser.HtmlToPlainText;
import com.google.cloud.automl.v1beta1.Dataset;
import com.google.cloud.storage.Bucket;
import com.google.protobuf.Empty;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Slf4j
@Getter
@Setter
public class ImportTrainingCommand extends MgnlCommand {

    /**
     * Workspace
     */
    private String workspace;

    /**
     * Root path to get translation content.
     */
    private String path;

    /**
     * Source lang
     */
    private String lang_source;

    /**
     * Target lang
     */
    private String lang_target;

    /**
     * Node type
     */
    private String nodeType;

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

    private final SiteManager siteManager;

    @Inject
    public ImportTrainingCommand(SiteManager siteManager) {
        this.siteManager = siteManager;
    }

    /**
     * Command params
     * workspace: workspace de los contenidos empleados para generar el dataset del modelo de traduccion
     *
     * path: root path de los contenidos empleados para generar el dataset
     *
     * lang_source: idioma origen de las traducciones
     * NOTA si lang_source vacio entonces lang_source sera fallbackLocale porque es el idioma base del CMS
     *
     * lang_target: idioma destino de las traducciones
     * NOTA si lang_target vacio entonces lang_target seran todos los locales del site y SERAN GENERADOS TANTOS DATASET COMO IDIOMAS
     *
     * project_id: Google project Id
     *
     * compute_region: Google compute region
     *
     * dataset_name: Dataset name
     *
     * @param context
     * @return
     * @throws Exception
     */
    @Override
    public boolean execute(Context context) throws Exception {
        workspace = getWorkspace();
        path = getPath();

        Session session = context.getJCRSession(workspace);
        final Node root = session.getNode(path);

        final Site site = siteManager.getAssignedSite(root);
        final I18nContentSupport i18nContentSupport = site.getI18n();
        final Collection<Locale> locales = i18nContentSupport.getLocales();

        if (lang_source != null && !isLangSupported(lang_source, locales)) {
            log.error("ImportTraining failure - source lang not supported {}", lang_source);
            return false;
        }
        if (lang_target != null && !isLangSupported(lang_target, locales)) {
            log.error("ImportTraining failure - target lang not supported {}", lang_target);
            return false;
        }
        if (lang_source != null && lang_target !=null && lang_source.equals(lang_target)) {
            log.error("ImportTraining failure - translate from {} to {}, really??", lang_source, lang_target);
            return false;
        }

        final Locale fallbackLocale = i18nContentSupport.getFallbackLocale();

        if (lang_source == null) {
            lang_source = fallbackLocale.toString();
        }

        /**
         * Build lang_source_suffix
         */
        String lang_source_suffix = "";
        if (lang_source != null && !fallbackLocale.toString().equals(lang_source)) {
            lang_source_suffix = lang_source;
        }

        /**
         * Build lang_target_suffixes
         */
        List<String> lang_target_suffixes = new ArrayList<>();
        if (lang_target != null) {
            lang_target_suffixes.add(lang_target);
        } else {
            locales.forEach((l)-> {
                if (!l.toString().equals(lang_source)) {
                    if (!fallbackLocale.equals(l)) {
                        lang_target_suffixes.add(l.toString());
                    } else {
                        lang_target_suffixes.add("");
                    }

                }
            });
        }

        /**
         * Generate list of sentences (txt source   txt target)
         */
        List<Sentence> sentences = new ArrayList<>();
        for (String lts: lang_target_suffixes) {
            List<Sentence> lang_sentences = getSentences(root, lang_source_suffix, lts, nodeType);
            if (lang_sentences != null) {
                sentences.addAll(lang_sentences);
            }
        }

        /**
         * Create file and import to dataset
         */
        String dataset_model_name = dataset_name;
        if (dataset_model_name == null) {
            dataset_model_name = "magnolia" + "_" + site.getName() + "_" + lang_source + "_" + lang_target;
        }

        return importDataset(project_id, compute_region, dataset_model_name, sentences);
    }

    /**
     * Return sentences for the tree node starting at root
     * @param root
     * @param lang_suffix_source
     * @param lang_suffix_target
     * @param nodeType
     * @return
     * @throws RepositoryException
     */
    private List<Sentence> getSentences(Node root, final String lang_suffix_source, final String lang_suffix_target, final String nodeType) throws RepositoryException {
        List<Sentence> sentences = new ArrayList<>();
        if (nodeType == null || root.getPrimaryNodeType().getName().equals(nodeType)) {
            List<Sentence> node_sentences = getSentencesForNode(root, lang_suffix_source, lang_suffix_target);
            if (node_sentences != null) {
                sentences.addAll(node_sentences);
            }
        }

        NodeIterator nodeIterator = root.getNodes();
        while (nodeIterator.hasNext()) {
            Node child = nodeIterator.nextNode();
            List<Sentence> node_sentences = getSentences(child, lang_suffix_source, lang_suffix_target, nodeType);
            if (node_sentences != null) {
                sentences.addAll(node_sentences);
            }
        }
        return sentences;
    }

    /**
     * Return sentences for the properties of a node
     * @param node
     * @param lang_source
     * @param lang_target
     * @return
     * @throws RepositoryException
     */
    private List<Sentence> getSentencesForNode(Node node, String lang_source, String lang_target) throws RepositoryException {
        List<Sentence> sentences = new ArrayList<>();

        PropertyIterator propertyIterator = node.getProperties();
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.nextProperty();
            if (!isSystemProperty(property)) {
                Sentence sentence = buildSentenceForProperty(node, lang_source, lang_target, property);
                if (sentence != null) {
                    sentences.add(sentence);
                }
            }
        }

        return sentences;
    }

    /**
     * Build sentence for a property
     * @param node
     * @param lang_source
     * @param lang_target
     * @param property
     * @return
     * @throws RepositoryException
     */
    private Sentence buildSentenceForProperty(Node node, String lang_source, String lang_target, Property property) throws RepositoryException {
        String i18nPropertySource = buildI18nProperty(property.getName(), lang_source);
        String i18nPropertyTarget = buildI18nProperty(property.getName(), lang_target);
        if (node.hasProperty(i18nPropertySource) && node.hasProperty(i18nPropertyTarget)) {
            String source_sentence = HtmlToPlainText.getPlainText(node.getProperty(i18nPropertySource).getString());
            String target_sentence = HtmlToPlainText.getPlainText(node.getProperty(i18nPropertyTarget).getString());
            if (isTranslatable(source_sentence) && isTranslatable(target_sentence)) {
                return Sentence.builder()
                        .source(source_sentence)
                        .target(target_sentence)
                        .build();
            }
        }

        return null;
    }

    /**
     * Import translations to dataset
     *
     * @param project_id
     * @param compute_region
     * @param dataset_name
     * @param sentences
     * @return
     * @throws Exception
     */
    public boolean importDataset(String project_id, String compute_region, String dataset_name, List<Sentence> sentences) throws Exception {

        final String bucketName = project_id + "-vcm";
        /** Creating Bucket */
        Bucket bucket = StorageApi.getBucketByName(bucketName);
        if (bucket == null) {
            StorageApi.createBucket(bucketName);
        }
        /** Uploading training files */
        StringBuilder content_tsv = new StringBuilder();
        sentences.stream().forEach(sentence -> {
            content_tsv.append(sentence.toString()).append("\n");
        });

        // .tsv file with translations
        String blob_tsv = "csv/" + workspace + "-" + lang_source + "-" + lang_target + ".tsv";
        StorageApi.createBlob(bucketName, blob_tsv, content_tsv.toString());
        // .csv that references .tsv file translation
        String content_csv = "UNASSIGNED,gs://" + bucketName + "/" + blob_tsv;
        String blob_csv = "csv/" + workspace + "-" + lang_source + "-" + lang_target + ".csv";
        StorageApi.createBlob(bucketName, blob_csv, content_csv);

        /** Importing .csv */
        String gs_csv = "gs://" + bucketName + "/" + blob_csv;
        String datasetId = DatasetApi.getDatasetIdByName(project_id, compute_region, dataset_name);
        if (datasetId == null) {
            Dataset dataset = DatasetApi.createDataset(project_id, compute_region, dataset_name, lang_source, lang_target);
            datasetId = dataset.getName().split("/")[dataset.getName().split("/").length - 1];
        }
        Empty response = DatasetApi.importData(project_id, compute_region, datasetId, gs_csv);

        return true;
    }

    /**
     * Return true if lang is supported by the site
     * @param lang
     * @param supportedLocales
     * @return
     */
    private boolean isLangSupported(final String lang, Collection<Locale> supportedLocales) {
        return supportedLocales.stream().anyMatch((locale) -> locale.toString().equals(lang));
    }

    /**
     * Returns true for System properties
     * @param property
     * @return
     * @throws RepositoryException
     */
    private boolean isSystemProperty(Property property) throws RepositoryException {
        return (property.getName().indexOf("jcr:") > -1);
    }

    /**
     * Given a language, builds the i18n property name
     * @param property
     * @param lang_suffix
     * @return
     */
    private String buildI18nProperty(String property, String lang_suffix) {
        return (lang_suffix == null || "".equals(lang_suffix)) ? property : property+ "_" + lang_suffix;
    }

    /**
     * Returns true if the sentence is a good input for the dataset
     * Sentences with more than 15 words has no value
     * @param txt
     * @return
     */
    private boolean isTranslatable(String txt) {
        return (StringUtils.countMatches(txt, " ")<15);
    }

    @Data
    @Builder
    private static class Sentence {
        String source;
        String target;

        public String toString() {
            return source + "\t" + target;
        }
    }
}
