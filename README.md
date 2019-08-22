# magnolia-content-translation-ml

Usage of AutoML of Google to create a custom translation model from translations retrieved from a website in Magnolia CMS.  
The aim is to obtain a translation model specific to the contents of the websites to predict accurated translations.  

For a detailed explanation please visit [Using Google AutoML Translation with Magnolia CMS](https://medium.com/@joaquin.alfaro/using-google-automl-translation-with-magnolia-cms-540f6e1f0fa7)  
## Features
Integration with the service [Google AutoML Translation](https://cloud.google.com/translate/automl/docs/ "AutoML") to generate the translation model

Generation of a training dataset from translations in Magnolia.

Integration with the service [Google Cloud Storage](https://cloud.google.com/storage/ "Google Cloud Storage") to store the training datasets to be used to generate the model.
 
Magnolia utility to translate contents with the translation model created from contents of websites in Magnolia.


## Usage

### Set up
1- Create project in Google Cloud

2- Activar y configurar el servicio AutoML Translation en el proyecto creado en el punto anterior. Seguir el manual de Google https://cloud.google.com/translate/automl/docs/before-you-begin
Activate and setup the service AutoML Translation in the project. Use this manual from Google https://cloud.google.com/translate/automl/docs/before-you-begin  
>It is not required to create the bucket because it is created automatically by Magnolia

**UI for AutoML**
![Consola AutoML](_dev/consola-automl.png)

**Example of json of service account Key**
~~~~
{
  "type": "service_account",
  "project_id": "automl-translation",
  "private_key_id": "",
  "private_key": "-----BEGIN PRIVATE KEY----------END PRIVATE KEY-----\n",
  "client_email": "magnolia-challenge@automl-translation.iam.gserviceaccount.com",
  "client_id": "",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/magnolia-challenge%40automl-translation.iam.gserviceaccount.com"
}
~~~~

3- The client of AutoML used in Magnolia get credentials from the above json. The location of the json must be specified at the environment variable GOOGLE_APPLICATION_CREDENTIALS
~~~~
export GOOGLE_APPLICATION_CREDENTIALS="${SECURE_PATH}/automl-translation.json"
~~~~

#### Magnolia commands
The module provides a set of commands to create the training dataset, train the model and predict translation.

##### translationml-importtraining  
Generate and import the training dataset based in translations of Magnolia.  
  
Parameters:
>workspace: magnolia workspace of contents to be used to generate the training dataset.  

>path: Path of the root node of contents  

>lang_source: Source language (optional. Uses default language of Magnolia)  

>lang_target: Targe language.

>nodeType: Type of content whose translations will be used to generate the training dataset.  

>project_id: Identifier of the project in Google  

>compute_region: Google cloud Region  

>dataset_name: Name of the training dataset that will be created (optional. By default the name will be "magnolia_" + site +_" + lang_source + "_" + lang_target)

Example:  
~~~~
// Get command instance
cm = info.magnolia.commands.CommandsManager.getInstance()
command = cm.getCommand('translationml', 'importtraining')

// Execute import of "tours" repository
command.setWorkspace('tours')
command.setPath('/magnolia-travels')
command.setLang_target('de')
command.setProject_id('automl-translation')
command.setCompute_region('us-central1')
command.setDataset_name('magnolia_en_de')
command.execute(ctx)
~~~~

##### translationml-trainmodel  
Launch the training of a prediction model based in the training set generated with the command *translationml-importtraining* 
  
Parameters:  
>project_id: Identifier of the project in Google  

>compute_region: Google cloud Region  

>dataset_name: Name of the training dataset

>model_name: Name of the prediction model (option. By default dataset_name + "_" + "yyyyMMddHHmmss")

Example:  
~~~~
// Get command instance
cm = info.magnolia.commands.CommandsManager.getInstance()
command = cm.getCommand('translationml', 'trainmodel')

// Execute Model training
command.setProject_id('automl-translation')
command.setCompute_region('us-central1')
command.setDataset_name('magnolia_en_de')
command.execute(ctx)
~~~~  

##### translationml-predicttranslation
Predicts a translation using the model created with command *translationml-trainmodel*. The translations will be more accurate because they are based in contents of the website.
  
Parameters:  
>project_id: Identifier of the project in Google  

>compute_region: Google cloud Region  

>model_display_name: Name of the prediction model (incompatible with the param model_id)

>model_id: Identifier of the prediction model (incompatible with the param model_display_name)

~~~~
// Get command instance
cm = info.magnolia.commands.CommandsManager.getInstance()
command = cm.getCommand('translationml', 'predicttranslation')

// Execute prediction of translation
command.setProject_id('automl-translation')
command.setCompute_region('us-central1')
command.setModel_display_name('dataset_en_es_v20190515063014')
command.setText('Good luck to the participants of the Magnolia challenge 2019')
command.execute(ctx)
println command.getTranslation()
~~~~
 
#### Thoughts
Probabley the integration with AutoML should be done in a service out of Magnolia.    

## License

MIT

## Contributors

Formentor Studio, http://formentor-studio.com/

Joaquín Alfaro, @Joaquin_Alfaro