### Pre-requisites
* Maven 3: http://maven.apache.org/
* Git client

### Clone and Build
1. git clone https://github.com/skubit/skubit-tools.git
2. cd skubit-tools
3. mvn install

### Create Application and Keys
* Log into production or test website (https://catalog.skubit.com or https://catalog.skubit.net)
* Go to Merchants tab
* Click 'Add Application' button
* Enter application name in field and click 'Add' button
* Click application row in table
* Click 'API' tab
* Click "Create Keys' button
* Click 'Show' button on right of keys row
* Copy API Keys and API Secret entries from popup

### Upload sample 

Use the API key ${apiKey} and API Secret ${apiSecretKey} from the website. ${packageName} is the application name you entered on the website. ${productId} is the product id you entered on the website.

${body} is the file name and path of the message body. For the sample below, a skudetails sample file is provided. 

To Test

`java -jar target/skubit-tools-0.0.1-SNAPSHOT-jar-with-dependencies.jar --method PUT --nonce 111 --apiSecretKey ${apiSecretKey} --apiKey ${apiKey} -body skudetails.json https://catalog.skubit.net/rest/v1/inventory/skus/${packageName}/{productId}`

To Production

`java -jar target/skubit-tools-0.0.1-SNAPSHOT-jar-with-dependencies.jar --method PUT --nonce 111 --apiSecretKey ${apiSecretKey} --apiKey ${apiKey} -body skudetails.json https://catalog.skubit.com/rest/v1/inventory/skus/${packageName}/{productId}`


### Links
To see the full API:
[https://catalog.skubit.net/#/apidocs](https://catalog.skubit.net/#/apidocs)
