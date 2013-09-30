# cas-json-tool
=============

JSON-based CAS ServiceRegistry configuration file editor.  This command creates/modifies JSON files that are compatible with [JsonServiceRegistryDao](https://github.com/Unicon/cas-addons/blob/master/src/main/java/net/unicon/cas/addons/serviceregistry/JsonServiceRegistryDao.java).

## Build
You can build the project from source using the following command:

     ./gradlew distZip  (gradlew.bat distZip on Windows)
     
Once the build is complete, you will be left with a zip file in the `build/distributions` directory:

     cas-json-tool-0.3.0.zip

## Install
Just extract the zip file where you want to install the application. Make sure to add the `cas-json-tool-0.3.0/bin` directory to your `$PATH` 

The install command creates a new directory (cas-json-tool-0.3.0) that contains all of the necessary jar files and some shell scripts:

* bin/cas-json-tool - Bash wrapper script for the cas-json-tool jar file
* bin/svnProcess and bin/gitProcess - Bash scripts that handle pre/post processing and workflow.

## Configuration
cas-json-tool will look for `cas-json-tool-config.groovy` in your home directory to read configuration options.  You can specify a different file with `--defaults`
### cas-json-tool-config.groovy
```groovy
//Default theme for CAS login page
defaultTheme = "default"

//All attributes that can be released by CAS
releaseAttributes = ["uid", "eduPersonPrimaryAffiliation", "cn", "givenName", "sn", "mail", "phone"]

//Allow these extra attributes.  An empty list allows any attribute
allowedExtraAttributes = []

//Require these extra attributes for each service.  
//Attributes in this list are automatically included in allowedExtraAttributes
requiredExtraAttributes = ["contactName","contactEmail","contactDept","contactPhone"]
    
//Run this command BEFORE processing. the input file is passed as an argument.
preCommand = ''
//Run this command AFTER processing.  the output file is passed as an argument.
postCommand = '' 
```
 
## Usage
To see a list of all options, use the `--help` argument:
```
$ ./cas-json-tool --help
usage: cas-json-tool --input service_registry.json [options]

Available options (use -h for help):
    --authzName <attributeName>              attribute that contains the authorization data for this
                                             service
    --authzValue <value list>                attribute values that users must have to access this
                                             service (separate multiple with commas)
    --csv <CSVfileName>                      Write data to a CSV file
    --defaults <configFileName>              groovy config file
    --desc <description>                     description
    --disable                                disable a service
    --disableAnonymous                       disable opaque identifier
    --disableProxy                           do not allow proxy ticket requests
    --disableSSO                             disable Anonymous access
 -e,--extraAttribute <attribute=value>       add arbitrary extra attribute/value for this service
                                             (can be used multiple times)
    --enable                                 enable a disabled service
    --enableAnonymous                        enable opaque user identifier instead of NetID
    --enableProxy                            allow service to request proxy tickets
    --enableSSO                              enable SSO access
    --evalOrder <number>                     evaluation order - used when multiple patterns match a
                                             URL. Lower wins. (default: 100)
 -f,--force                                  overwrite output file
 -h,--help                                   usage information
 -i,--input <inputFilename>                  JSON file to read.
    --id <serviceID>                         service ID number - valid with "--search", "--remove"
                                             or "--modify" ONLY
 -m,--modify                                 modify service
    --mfaAttr <MFA Attribue Name>            attribute that contains Multi-Factor Auth (MFA)
                                             Requirements for this service
    --mfaUser <MFA user list>                usernames that must use MFA (requires
                                             --mfaValue=CHECK_LIST) (separate multiple with commas)
    --mfaUserAttr <MFA User list Attribue>   attribute that contains the list of users required to
                                             use MFA (requires --mfaValue=CHECK_LIST)
    --mfaValue <MFA Attribue Value>          user group that will be required to use MFA (allowed:
                                             ALL, NONE or CHECK_LIST
 -n,--new                                    add new service
    --name <serviceName>                     service name
 -o,--output <outputFilename>                write output to this file.  Prints to STDOUT if omitted
    --pattern <pattern>                      regular expression or ant pattern to match service
 -r,--remove                                 remove service
    --release <attribute list>               add to attribute list (separate multiple with commas)
 -s,--search                                 search for a service
    --theme <theme>                          CAS theme to use with this service.
    --url <url>                              sample URL to test the ant/regex pattern
    --userAttribute <usernameAttribute>      Alternate Username attribute
 -v,--version                                version information
```

### Example: Display service registry contents
Read an existing JSON service-registry file (`example.json`)
```
$ cas-json-tool --input example.json 
{
    "services": [
        {
            "enabled": true,
            "ignoreAttributes": false,
            "theme": "default",
            "id": 1,
            "extraAttributes": {
                "createdDate": "2012-11-02 09:05:36 -0500"
            },
            "allowedToProxy": false,
            "serviceId": "https://example.org/**",
            "description": "This is a new service",
            "name": "My Service",
            "ssoEnabled": true,
            "anonymousAccess": false,
            "evaluationOrder": 100,
            "allowedAttributes": [
            
            ]
        }
    ]
}
```
     
### Example: Create a new service
Add a new service then save the result as `/tmp/cas_service.json` 
```
$ cas-json-tool --new \
 --name='My Service' \
 --desc='This is a New Service' \
 --pattern='https://example.org/**' \
 --url='https://example.org/index.php' \
 --output=/tmp/cas_service.json \
 --extraAttribute contactName='Eric Pierce' \
 --extraAttribute contactEmail='epierce@example.edu' \
 --extraAttribute contactPhone='555-555-5555'   
```

### Example: Search for a service by name
 ```
$ cas-json-tool \
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
                 "(555)555-5555"
             ],
             "contactEmail": [
                 "epierce@example.edu"
             ],
             "contactName": [
                 "Eric Pierce"
             ],
             "contactDept": [
                 "Information Technology"
             ],
             "createdDate": "2012-11-02 09:05:36 -0500"
         },
         "allowedToProxy": false,
         "serviceId": "https://example.org/**",
         "description": "This is a New Service",
         "name": "My Service",
         "ssoEnabled": true,
         "anonymousAccess": false,
         "evaluationOrder": 100,
         "allowedAttributes": [

         ],
         "usernameAttribute": null
     }
 ]
 ```
     
###Example: Modify an existing service, change the name and enable proxy ticket support.  Over-write the input file
```    
$ cas-json-tool \
  --modify \
  --input=/tmp/cas_service.json \
  --id=2 \
  --name="Updated Service" \
  --enableProxy \
  --output=/tmp/cas_service.json \
  --force
```

###Example: Create a new service with [Role-Based Access Controls](https://github.com/Unicon/cas-addons/wiki/Role-Based-Services-Authorization)
```
$ cas-json-tool --new \
 --name='My Service' \
 --desc='This is a New Service' \
 --pattern='https://example.org/**' \
 --url='https://example.org/index.php' \
 --output=/tmp/cas_service.json \
 --extraAttribute contactName='Eric Pierce' \
 --extraAttribute contactEmail='epierce@example.edu' \
 --extraAttribute contactPhone='555-555-5555' \
 --authzName=eduPersonPrimaryAffiliation \
 --authzValue=faculty,staff
```

###Example: Create a new service with [DuoSecurity MultiFactor Authentication](https://github.com/epierce/cas-server-extension-duo)
```
$ cas-json-tool --new \
 --name='My Service' \
 --desc='This is a New Service' \
 --pattern='https://example.org/**' \
 --url='https://example.org/index.php' \
 --output=/tmp/cas_service.json \
 --extraAttribute contactName='Eric Pierce' \
 --extraAttribute contactEmail='epierce@example.edu' \
 --extraAttribute contactPhone='555-555-5555' \
 --authzName=eduPersonPrimaryAffiliation \
 --authzValue=faculty,staff
