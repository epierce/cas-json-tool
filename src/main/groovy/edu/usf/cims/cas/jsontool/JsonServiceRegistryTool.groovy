package edu.usf.cims.cas.jsontool

import groovy.json.*
import groovy.util.CliBuilder
import org.apache.commons.cli.Option
import au.com.bytecode.opencsv.CSVWriter

class JsonServiceRegistryTool {

	static def version = "0.2.5"
  static def jsonOutputFile
  static def csvOutputFileName

	public static void main(String[] args) {

		try {
			def opt = getCommandLineOptions(args)
			def config = getConfigSettings(opt)

			runPreProcessor(config, opt)

			def jsonParser = createJSONparser(config,opt)

			def result = runAction(jsonParser,opt)

			printJSON(result)

      if(opt.csv) printCSV(result)

			runPostProcessor(config,opt)

		}catch(Exception e) {
			exitOnError e.message
		}
	}

	private static getCommandLineOptions(String[] args){
		//Parse command-line options
		def cli = new CliBuilder(
						usage:"cas-json-tool --input service_registry.json [options]",
						header:"\nAvailable options (use -h for help):\n",
						width:100)

		cli.with {
			h longOpt:'help', 'usage information', required: false
			v longOpt:'version', 'version information', required: false
			i longOpt:'input', args:1, argName:'inputFilename', 'JSON file to read.', required: false
			o longOpt:'output', args:1, argName:'outputFilename', 'write output to this file.  Prints to STDOUT if omitted', required: false
			f longOpt:'force', 'overwrite output file', required: false
			n longOpt:'new', 'add new service', required: false
			r longOpt:'remove', 'remove service', required: false
			m longOpt:'modify', 'modify service', required: false
			s longOpt:'search', 'search for a service', required: false
			e longOpt:'extraAttribute', args:2, valueSeparator:'=', argName:'attribute=value',"add arbitrary extra attribute/value for this service (can be used multiple times)", required: false
      _ longOpt:'authzName', args:1, argName:'attributeName', 'attribute that contains the authorization data for this service', required: false
      _ longOpt:'authzValue', args:Option.UNLIMITED_VALUES, valueSeparator: ',' , argName:'value list', "attribute values that users must have to access this service (separate multiple with commas)", required: false
			_ longOpt:'enable', 'enable a disabled service', required: false
			_ longOpt:'disable', 'disable a service', required: false
			_ longOpt:'enableSSO', 'enable SSO access', required: false
			_ longOpt:'disableSSO', 'disable Anonymous access', required: false
			_ longOpt:'enableAnonymous', 'enable opaque user identifier instead of NetID', required: false
			_ longOpt:'disableAnonymous', 'disable opaque identifier', required: false
			_ longOpt:'enableProxy', 'allow service to request proxy tickets', required: false
			_ longOpt:'disableProxy', 'do not allow proxy ticket requests', required: false
			_ longOpt:'defaults', args:1, argName:'configFileName', 'groovy config file', required: false
			_ longOpt:'evalOrder', args:1, argName:'number', 'evaluation order - used when multiple patterns match a URL. Lower wins. (default: 100)', required: false
			_ longOpt:'id', args:1, argName:'serviceID', 'service ID number - valid with "--search", "--remove" or "--modify" ONLY', required: false
			_ longOpt:'name', args:1, argName:'serviceName', 'service name', required: false
			_ longOpt:'userAttribute', args:1, argName:'usernameAttribute', 'Alternate Username attribute', required: false
			_ longOpt:'desc', args:1, argName:'description', 'description', required: false
			_ longOpt:'theme', args:1, argName:'theme', "CAS theme to use with this service.", required: false
			_ longOpt:'pattern', args:1, argName:'pattern', 'regular expression or ant pattern to match service', required: false
			_ longOpt:'url', args:1, argName:'url', 'sample URL to test the ant/regex pattern', required: false
			_ longOpt:'release', args:Option.UNLIMITED_VALUES, valueSeparator: ',' , argName:'attribute list', "add to attribute list (separate multiple with commas)"
      _ longOpt:'csv', args:1, argName:'CSVfileName', 'Write data to a CSV file', required: false
      _ longOpt:'mfaAttr', args: 1, argName:'MFA Attribue Name', 'attribute that contains Multi-Factor Auth (MFA) Requirements for this service', required: false
      _ longOpt:'mfaValue', args: 1, argName:'MFA Attribue Value', 'user group that will be required to use MFA (allowed: ALL, NONE or CHECK_LIST', required: false
			_ longOpt:'mfaUserAttr', args: 1, argName:'MFA User list Attribue', 'attribute that contains the list of users required to use MFA (requires --mfaValue=CHECK_LIST)', required: false
			_ longOpt:'mfaUser', args:Option.UNLIMITED_VALUES, valueSeparator: ',' , argName:'MFA user list', "usernames that must use MFA (requires --mfaValue=CHECK_LIST) (separate multiple with commas)", required: false
		}

		def options = cli.parse(args)

		//Display version info
		if(options.version){
			println "\nVersion: ${version}\n"
			System.exit(0)
		}

		//Display usage if --help is given OR no input file AND not creating a new file
		if( (options.help) || ((! options.i) && (! options.n)) ){
			cli.usage()
			System.exit(0)
		}

		return options
	}

	private static getConfigSettings(options){
		def config = new ConfigObject()

		//Use this theme if one isn't specified with --theme
		config.defaultTheme = "default"
		//All attributes that can be released by CAS
		config.releaseAttributes = []
		//Allow these extra attributes to be saved for a service.  An empty list allows any attribute
		config.allowedExtraAttributes = []
		//Require these attributes in addition to those required by the ServiceRegistry Class
		config.requiredExtraAttributes = []
		//Run this command BEFORE processing. the input file is passed as an argument.
		config.preCommand = ''
		//Run this command AFTER processing.  the output file is passed as an argument.
		config.postCommand = ''

		/** Defaut configuration values can be set in $HOME/cas-json-tool-config.groovy **/
		def defaultConfigFile = new File(System.getProperty("user.home")+'/cas-json-tool-config.groovy')

		//The default file is not required, so if it doesn't exist don't throw an exception
		if (defaultConfigFile.exists() && defaultConfigFile.canRead()) {
			config = config.merge(new ConfigSlurper().parse(defaultConfigFile.toURL()))
		}

		//Merge the defaults file that was passed on the commandline
		if(options.defaults){
			def newConfigFile = new File(options.defaults)
			config = config.merge(new ConfigSlurper().parse(newConfigFile.toURL()))
		}

		return config
	}

	private static runPreProcessor(config,options){
		if (config.preCommand && options.input){
			def proc = "${config.preCommand} ${options.input}".execute()
			proc.waitFor()
			println "${proc.in.text}"
			if (proc.exitValue() != 0) throw new ScriptException("Preprocessor exited with an error!")
		}
	}

	private static runPostProcessor(config,options){
		if (config.postCommand && options.output){
			def proc = "${config.postCommand} ${options.output}".execute()
			proc.waitFor()
			println "${proc.in.text}"
			if (proc.exitValue() != 0) throw new ScriptException("Postprocessor exited with an error!")

			if(options.csv){
				proc = "${config.postCommand} ${csvOutputFileName}".execute()
				proc.waitFor()
				println "${proc.in.text}"
				if (proc.exitValue() != 0) throw new ScriptException("Postprocessor exited with an error!")
			}
		}
	}

	private static createJSONparser(config,options){
		def jsonParser = new JsonServiceRegistryParser()
		setDefaults(jsonParser,config)

		if(options.input) jsonParser.setJsonData(readInputFile(options.input))
		if(options.output) {
			jsonOutputFile = new File(options.output)
			if (! jsonOutputFile.exists()) {
				jsonOutputFile.createNewFile()
				jsonOutputFile.setWritable(true)
			}else if (! options.force) {
				throw new FileNotFoundException("${options.output} already exists.  Use --force to overwrite.")
			//Make sure the file is writeable now so an exception can be thrown before doing any work
			}else if (! jsonOutputFile.canWrite()) {
				throw new FileNotFoundException("${options.output} is not writeable.")
			}
		} else {
			jsonOutputFile = false
		}

    if(options.csv) {
      csvOutputFileName = options.csv
      def csvOutputFile = new File(options.csv)
      if (! csvOutputFile.exists()) {
        csvOutputFile.createNewFile()
        csvOutputFile.setWritable(true)
      } else if (! options.force) {
        throw new FileNotFoundException("${options.csv} already exists.  Use --force to overwrite.")
      //Make sure the file is writeable now so an exception can be thrown before doing any work
      } else if (! csvOutputFile.canWrite()) {
        throw new FileNotFoundException("${options.csv} is not writeable.")
      }
    }
		return  jsonParser
	}

	private static setDefaults(parser,config) {
		parser.setCasAttributes(config.releaseAttributes)
		parser.setExtraServiceAttributes(config.allowedExtraAttributes,config.requiredExtraAttributes)
		parser.setDefaultTheme(config.defaultTheme)
	}

	private static checkOptions(opt){
		if((opt.enable)&&(opt.disable)) {
			throw new IllegalArgumentException('--disable and --enable cannot be used together')
		}
		if((opt.enableSSO)&&(opt.disableSSO)) {
			throw new IllegalArgumentException('--disableSSO and --enableSSO cannot be used together')
		}
		if((opt.enableAnonymous)&&(opt.disableAnonymous)) {
			throw new IllegalArgumentException('--disableAnonymous and --enableAnonymous cannot be used together')
		}
		if((opt.enableProxy)&&(opt.disableProxy)) {
			throw new IllegalArgumentException('--disableProxy and --enableProxy cannot be used together')
		}
		if((opt.id)&&(! opt.id.isInteger())) {
			throw new IllegalArgumentException('ServiceID must be an Integer')
		}

		if(opt.new){
    	if((opt.authzName)&&(! opt.authzValue)) {
    		throw new IllegalArgumentException('Authorization values are required when passing a authorization attribute name')
    	}
    	if((! opt.authzName)&&(opt.authzValue)) {
    		throw new IllegalArgumentException('Authorization attribute name required when passing authorization values')
    	}
    	if((! opt.mfaAttr)&&(opt.mfaValue)) {
    		throw new IllegalArgumentException('MFA attribute name is required when passing a MFA attribute value')
   		}
   		if((opt.mfaAttr)&&(!opt.mfaValue)) {
   			throw new IllegalArgumentException('MFA value is required when passing a MFA attribute name')
   		}
    	if((opt.mfaUser)&&((!opt.mfaAttr)||(opt.mfaValue != 'CHECK_LIST')||(! opt.mfaUserAttr))) {
    		throw new IllegalArgumentException('This option requires --mfaValue=CHECK_LIST and values for --mfaAttr and --mfaUserAttr')
    	}
    	if((opt.mfaValue)&&
    			((opt.mfaValue !='ALL')&&(opt.mfaValue !='NONE')&&(opt.mfaValue !='CHECK_LIST'))) {
				throw new IllegalArgumentException('mfaValue must be "ALL", "NONE" or "CHECK_LIST"')
			}
		}
		//Search and modify require --id
		if((!opt.id) && ((opt.remove) || (opt.modify))) {
			throw new IllegalArgumentException('Service ID number required!')
		}
	}

	/**
	* Read JSON data from file.  JSON data must contain a list with 0 or more elements named "services"
	* @param jsonFileName	The file to read from.
	* @return Object 	JSON Object
	* @throws IOException 	If an input exception occurred
	* @throws JsonException If the included JSON data is malformed
	*/
	private static readInputFile(jsonFileName) {
		def text = new File(jsonFileName).text
		def slurper = new JsonSlurper()
		slurper.parseText(text)
	}

	private static runAction(jsonParser,options) {
		checkOptions(options)

		//Add a new service
		if(options.n){
			jsonParser.createService options

		//Remove a service
		} else if (options.r){

			jsonParser.removeService options.id

		//Search for a service
		} else if (options.s){
			def service = jsonParser.searchForService options

			return service
		//Modify service
		} else if (options.m){

			def origService = jsonParser.findById options.id
			def newService = jsonParser.modifyService(origService[0],options)

			jsonParser.removeService options.id
			jsonParser.addService newService

		}

		return jsonParser.jsonData

	}

	private static printJSON(data) {
		def jsonOut = new JsonOutput()

		//Write to a file
		if(jsonOutputFile) {
			jsonOutputFile.write("${jsonOut.prettyPrint(jsonOut.toJson(data))}\n")

		//output to screen
		} else {
			println "${jsonOut.prettyPrint(jsonOut.toJson(data))}\n"
		}
	}

  private static printCSV(data) {
    def writer = new CSVWriter(new FileWriter(csvOutputFileName))

    def fieldNames = ['createdDate','modifiedDate','id','name','description','serviceId','enabled','allowedAttributes','contactEmail','contactDept','contactName','contactPhone','ssoEnabled','allowedToProxy','anonymousAccess','evaluationOrder','theme','ignoreAttributes'] as String[]

    writer.writeNext(fieldNames)

    data.services.each { service ->
      def csv_line = []
      fieldNames.each { field ->
				if(service["${field}"]) {
				  csv_line.add(service["${field}"] as String)
				} else if (service.extraAttributes["${field}"] as String) {
				  csv_line.add(service.extraAttributes["${field}"] as String)
				} else {
				  csv_line.add('')
				}
      }
      writer.writeNext(csv_line as String[])
    }
    writer.close()
  }

	private static exitOnError(errorString){
		println("\nERROR: ${errorString}\n")
		System.exit(1)
	}
}