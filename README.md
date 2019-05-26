# Magnolia AutoML 

Integración de Magnolia con el servicio AutoML Translation de Google para adaptar traducciones a contenidos propios (turismo, finanzas etc.)

## Features
Integración con el servicio [Google AutoML Translation](https://cloud.google.com/translate/automl/docs/ "AutoML") para generar modelo predictivo de traducción

Generación de dataset a partir de la traducción de contenidos almacenados en Magnolia.

Integración con el servicio [Google Cloud Storage](https://cloud.google.com/storage/ "Google Cloud Storage") para almacenar datasets

Comando magnolia para traducir texto empleando modelo de predicción específico


## Usage

### Set up
1- Crear proyecto en Google Cloud

2- Activar y configurar el servicio AutoML Translation en el proyecto creado en el punto anterior. Seguir el manual de Google https://cloud.google.com/translate/automl/docs/before-you-begin
>No es obligatorio crear el bucket porque el módulo lo crea en caso de no existir

Como resultado, podremos acceder a la consola de AutoML y dispondremos del service account Key con las credenciales de acceso

**UI de AutoML**
![Consola AutoML](_dev/consola-automl.png)

**Ejemplo de json de service account Key**
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

3- El cliente de AutoML recupera las credenciales del json del service account. La ubicación del fichero se indica en la variable de entorno GOOGLE_APPLICATION_CREDENTIALS

~~~~
export GOOGLE_APPLICATION_CREDENTIALS="${SECURE_PATH}/automl-translation.json"
~~~~

> ¿Por qué no empleo Api Key o similar? el Api no dispone de Api key. Es posible obtener token por oauth pero es temporal y exige más control. 

#### Comandos
El úso del módulo está limitado a comandos que pueden ser asociados a acciones etc.

##### translationml-importtraining  
Objetivo: Importar dataset con la traducción de contenidos en magnolia.  
  
Parámetros:
>workspace: workspace de los contenidos empleados para generar el dataset  

>path: Path del nodo raíz de los contenidos para generar dataset  

>lang_source: Idioma origen (opcional. Por defecto local fallback)  

>lang_target: Idioma destino

>nodeType: Tipo de nodo de los contenidos a emplear para generar el dataset  

>project_id: Identificador del proyecto Google  

>compute_region: Región de computación  

>dataset_name: Nombre del dataset (opcional. Por defecto "magnolia_" + site +_" + lang_source + "_" + lang_target)

Ejemplo:  
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

**translationml-trainmodel**  
Objetivo: Entreno y generación de modelo.  
  
Parámetros:  
>project_id: Identificador del proyecto Google  

>compute_region: Región de computación  

>dataset_name: Nombre del dataset

>model_name: Nombre del modelo (opcional. Por defecto dataset_name + "_" + "yyyyMMddHHmmss")

Ejemplo:  
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

**translationml-predicttranslation**  
Objetivo: Entreno y generación de modelo.  
  
Parámetros:  
>project_id: Identificador del proyecto Google  

>compute_region: Región de computación  

>dataset_name: Nombre del dataset

>model_display_name: Nombre del modelo de predicción (incompatible con model_id)

>model_id: Identificador del modelo de predicción (incompatible con model_id)

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
 
#### Reflexión
>La integración con AutoML quizás debería ser un servicio independiente empleado por magnolia para trabajar con el API de AutomML   

## Contribute to the Magnolia component ecosystem
It's easy to create components for Magnolia and share them on github and npm. I invite you to do so and join the community. Let's stop wasting time by developing the same thing again and again, rather let's help each other out by sharing our work and create a rich library of components.

Just add `magnolia-light-module` as a keyword to npm's package.json to make them easy to find and use on npm.

## License

MIT

## Contributors

Formentor Studio, http://formentor-studio.com/

Joaquín Alfaro, @Joaquin_Alfaro