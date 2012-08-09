# cas-json-tool
=============

JSON-based CAS ServiceRegistry configuration file editor.  This command creates/modifies JSON files that are compatible with [JsonServiceRegistryDao](https://github.com/Unicon/cas-addons/blob/master/src/main/java/net/unicon/cas/addons/serviceregistry/JsonServiceRegistryDao.java).

## Build
You can build the project from source using the following Maven command:

     mvn clean package
     
Once the build is complete, you will be left with two jar files in the `target` directory:

     cas-json-tool-0.1.1.jar
     cas-json-tool-0.1.1-jar-with-dependencies.jar

You can use either version, but the one including the dependencies has been slightly faster in my testing.
## Configuration
cas-json-tool will look for `cas-json-tool-config.groovy` in your home directory to read configuration options.  You can specify a different file with `--defaults`
### cas-json-tool-config.groovy
     //Default theme for CAS login page
     defaultTheme = "default"

     //All attributes that can be released by CAS
     releaseAttributes = ["uid", "eduPersonPrimaryAffiliation", "cn", "givenName",
      "sn", "mail", "Title", "Phone"]

     //Allow these extra attributes.  An empty list allows any attribute
     allowedExtraAttributes = []

    //Require these extra attributes for each service.  
    //Attributes in this list are automatically included in allowedExtraAttributes
    requiredExraAttributes = ["contactName","contactEmail","contactDept","contactPhone"]
    
    //Run this command BEFORE processing. the input file is passed as an argument.
    preCommand = ''
    //Run this command AFTER processing.  the output file is passed as an argument.
    postCommand = '' 
    
## Usage
To see a list of all options, use the `--help` argument:

    $ java -jar cas-json-tool-0.1.1.jar --help
    usage: cas-json-tool --input service_registry.json [options]
    Available options (use -h for help):
        --defaults <configFileName>          groovy config file
        --desc <description>                 description
        --disable                            disable a service
        --disableAnonymous                   disable opaque identifier
        --disableProxy                       do not allow proxy ticket requests
        --disableSSO                         disable Anonymous access
     -e,--extraAttribute <attribute=value>   add arbitrary extra attribute/value for this service (can be used multiple times)
        --enable                             enable a disabled service
        --enableAnonymous                    enable opaque user identifier instead of NetID
        --enableProxy                        allow service to request proxy tickets
        --enableSSO                          enable SSO access
        --evalOrder <number>                 evaluation order - used when multiple patterns match a URL. Lower wins. (default: 100)
     -f,--force                              overwrite output file
     -h,--help                               usage information
     -i,--input <inputFilename>              JSON file to read.
        --id <serviceID>                     service ID number - valid with "--search", "--remove" or "--modify" ONLY
     -m,--modify                             modify service
     -n,--new                                add new service
        --name <serviceName>                 service name
     -o,--output <outputFilename>            write output to this file.  Prints to STDOUT if omitted
        --pattern <pattern>                  regular expression or ant pattern to match service
     -r,--remove                             remove service
        --release <attribute list>           add to attribute list (separate multiple with commas)
     -s,--search                             search for a service
        --theme <theme>                      CAS theme to use with this service.
        --url <url>                          sample URL to test the ant/regex pattern

### Example: Display service registry contents
Read an existing JSON service-registry file (`cas_registry.json`)

     $ java -jar cas-json-tool-0.1.1.jar --input=cas_registry.json 
     {
         "services": [
             {
                 "enabled": true,
                 "ignoreAttributes": false,
                 "theme": "default",
                 "id": 1,
                 "extraAttributes": {
                     "contactPhone": [
                         "(813)974-8868"
                     ],
                     "contactEmail": [
                         "epierce@usf.edu"
                     ],
                     "contactName": [
                         "Eric Pierce"
                     ],
                     "contactDept": [
                         "Information Technology"
                     ],
                     "createdDate": "2012-08-04"
                 },
                 "allowedToProxy": false,
                 "serviceId": "^https://cas.example.org/services/.*",
                 "description": "CAS ServiceRegistry Manager",
                 "name": "Service Manager",
                 "ssoEnabled": true,
                 "anonymousAccess": false,
                 "evaluationOrder": 100,
                 "allowedAttributes": [
                
                 ]
             }
         ]
     }
     
### Example: Create a new service
Add a new  service then save the result as `/tmp/cas_service.json` 

    $ java -jar cas-json-tool-0.1.1.jar \
    --new \
    --input=cas_registry.json \
    --name="My Service" \
    --desc="This is a New Service" \
    --pattern="https://example.org/**" \
    --url="https://example.org/index.php" \
    --output=/tmp/cas_service.json \
    --extraAttribute contactName="Eric Pierce" \
    --extraAttribute contactEmail="epierce@usf.edu" \
    --extraAttribute contactPhone="(813)974-8868" \
    --extraAttribute contactDept="Information Technology"    
    
### Example: Search for a service by name
 
    $ java -jar cas-json-tool-0.1.jar \
    --input=/tmp/cas_service.json \
    --search \
    --name="^My Ser.*"
    
    [
         {
             "enabled": true,
             "ignoreAttributes": false,
             "theme": "default",
             "id": 2,
             "extraAttributes": {
                 "contactPhone": [
                     "(813)974-8868"
                 ],
                 "contactEmail": [
                     "epierce@usf.edu"
                 ],
                 "contactName": [
                     "Eric Pierce"
                 ],
                 "contactDept": [
                     "Information Technology"
                 ],
                 "createdDate": "2012-08-05"
             },
             "allowedToProxy": false,
             "serviceId": "https://example.org/**",
             "description": "This is a New Service",
             "name": "My Service",
             "ssoEnabled": true,
             "anonymousAccess": false,
             "evaluationOrder": 100,
             "allowedAttributes": [
            
             ]
         }
     ]
     
###Example: Modify an existing service, change the name and enable proxy ticket support.  Over-write the input file
    $ java -jar cas-json-tool-0.1.jar \
    --modify \
    --input=/tmp/cas_service.json \
    --id=2 \
    --name="Updated Service" \
    --enableProxy \
    --output=/tmp/cas_service.json \
    --force

    