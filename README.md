# cas-json-tool
=============

JSON-based CAS ServiceRegistry configuration file editor.  This command creates/modifies JSON files that are compatible with [JsonServiceRegistryDao](https://github.com/Unicon/cas-addons/blob/master/src/main/java/net/unicon/cas/addons/serviceregistry/JsonServiceRegistryDao.java).

## Build
You can build the project from source using the following Maven command:

     mvn clean package
     
Once the build is complete, you will be left with two jar files in the `target` directory:

     cas-json-tool-0.2.0.jar
     cas-json-tool-0.2.0-jar-with-dependencies.jar

You can use either version, but the one including the dependencies has been slightly faster in my testing.

## Install
I've included an installer script `install_tool.sh` that takes a single parameter: the directory to install cas-json-tool in:

    sudo ./install_tool.sh /usr/local/apps

    Creating directory structure

    cas-json-tool installation complete!

    Please add the following lines to your .bashrc or .bash_profile
    export CASTOOL_HOME=/usr/local/apps/cas-json-tool
    export PATH="$PATH:$CASTOOL_HOME/bin"

The install command creates a new directory (cas-json-tool-0.2.0) that contains all of the necessary jar files and some shell/groovy scripts:

* bin/cas-json-tool - Bash wrapper script for the cas-json-tool jar file
* bin/cas-json-to-csv - Groovy script that converts the JSON file to a CSV - we use this because it's easier for out business analysts to open the CSV in Excel when getting a current list of CAS services than trying to parse the JSON file.
* bin/svnProcess - Bash script that handles our pre/post processing and workflow: 

>* Checks the latest JSON file out of SVN
>* Ensures it matches the version in use
>* Converts the newly modified JSON file to CSV
>* Checks the JSON and CSV files into SVN

## Configuration
cas-json-tool will look for `cas-json-tool-config.groovy` in your home directory to read configuration options.  You can specify a different file with `--defaults`
### cas-json-tool-config.groovy
     //Default theme for CAS login page
     defaultTheme = "default"

     //All attributes that can be released by CAS
     releaseAttributes = ["uid", "eduPersonPrimaryAffiliation", "cn", "givenName",
      "sn", "mail", "Phone"]

     //Allow these extra attributes.  An empty list allows any attribute
     allowedExtraAttributes = []

    //Require these extra attributes for each service.  
    //Attributes in this list are automatically included in allowedExtraAttributes
    requiredExtraAttributes = ["contactName","contactEmail","contactDept","contactPhone"]
    
    //Run this command BEFORE processing. the input file is passed as an argument.
    preCommand = ''
    //Run this command AFTER processing.  the output file is passed as an argument.
    postCommand = '' 
    
## Usage
To see a list of all options, use the `--help` argument:

    $ cas-json-tool --help
    usage: cas-json-tool --input service_registry.json [options]

    Available options (use -h for help):
        --defaults <configFileName>           groovy config file
        --desc <description>                  description
        --disable                             disable a service
        --disableAnonymous                    disable opaque identifier
        --disableProxy                        do not allow proxy ticket requests
        --disableSSO                          disable Anonymous access
     -e,--extraAttribute <attribute=value>    add arbitrary extra attribute/value for this service (can
                                          be used multiple times)
        --enable                              enable a disabled service
        --enableAnonymous                     enable opaque user identifier instead of NetID
        --enableProxy                         allow service to request proxy tickets
        --enableSSO                           enable SSO access
        --evalOrder <number>                  evaluation order - used when multiple patterns match a
                                              URL. Lower wins. (default: 100)
     -f,--force                               overwrite output file
     -h,--help                                usage information
     -i,--input <inputFilename>               JSON file to read.
        --id <serviceID>                      service ID number - valid with "--search", "--remove" or
                                              "--modify" ONLY
     -m,--modify                              modify service
     -n,--new                                 add new service
        --name <serviceName>                  service name
     -o,--output <outputFilename>             write output to this file.  Prints to STDOUT if omitted
        --pattern <pattern>                   regular expression or ant pattern to match service
     -r,--remove                              remove service
        --release <attribute list>            add to attribute list (separate multiple with commas)
     -s,--search                              search for a service
        --theme <theme>                       CAS theme to use with this service.
        --url <url>                           sample URL to test the ant/regex pattern
        --userAttribute <usernameAttribute>   Alternate Username attribute
     -v,--version                             version information

### Example: Display service registry contents
Read an existing JSON service-registry file (`example.json`)

     $ cas-json-tool --input=example.json 
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
                    
                ],
                "usernameAttribute": null
            }
        ]
}
     
### Example: Create a new service
Add a new  service then save the result as `/tmp/cas_service.json` 

    $ cas-json-tool \
    --new \
    --input=cas_registry.json \
    --name='My Service' \
    --desc='This is a New Service' \
    --pattern='https://example.org/** \
    --url='https://example.org/index.php" \
    --output=/tmp/cas_service.json \
    --extraAttribute contactName='Eric Pierce' \
    --extraAttribute contactEmail='epierce@example.edu' \
    --extraAttribute contactPhone='(555)555-5555' \
    --extraAttribute contactDept='Information Technology'    
    
### Example: Search for a service by name
 
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
     
###Example: Modify an existing service, change the name and enable proxy ticket support.  Over-write the input file
    $ cas-json-tool \
    --modify \
    --input=/tmp/cas_service.json \
    --id=2 \
    --name="Updated Service" \
    --enableProxy \
    --output=/tmp/cas_service.json \
    --force
