package edu.usf.cas

import groovy.util.CliBuilder
import org.apache.commons.cli.Option

class JsonServiceRegistryTool {

	public static void main(String[] args) {

		def config = new ConfigObject()

		config.defaultTheme = "default"
		
		//All attributes that can be released by CAS
		config.releaseAttributes = []
		//Require these attributes in addition to those required by the ServiceRegistry Class
		config.requiredAttributes = []

		def configFileName = System.getProperty("user.home")+'/cas-json-tool-config.groovy'
	
		//Parse command-line options
		def cli = new CliBuilder(
						usage:"JsonServiceRegistryTool --input service_registry.json [options]",
						header:"\nAvailable options (use -h for help):\n",
						width:100)

		cli.with {
			h longOpt:'help', 'usage information', required: false 
			i longOpt:'input', args:1, argName:'inputFilename', 'JSON file to read.', required: false
			o longOpt:'output', args:1, argName:'outputFilename', 'write output to this file.  Prints to STDOUT if omitted', required: false
			f longOpt:'force', 'Overwrite output file', required: false
			n longOpt:'new', 'Add new service', required: false
			r longOpt:'remove', 'Remove service', required: false
			m longOpt:'modify', 'Modify service', required: false
			s longOpt:'search', 'Search for a service', required: false
			_ longOpt:'enable', 'Enable a disabled service', required: false
			_ longOpt:'disable', 'Disable a service', required: false
			_ longOpt:'enableSSO', 'Enable SSO access', required: false
			_ longOpt:'disableSSO', 'Disable Anonymous access', required: false
			_ longOpt:'enableAnonymous', 'Enable opaque user identifier instead of NetID', required: false
			_ longOpt:'disableAnonymous', 'Disable opaque identifier', required: false
			_ longOpt:'enableProxy', 'Allow service to request proxy tickets', required: false
			_ longOpt:'disableProxy', 'Do not allow proxy ticket requests', required: false
			_ longOpt:'defaults', args:1, argName:'configFileName', 'Groovy config file', required: false
			_ longOpt:'priority', args:1, argName:'number', 'Service priority - used when multiple patterns match a URL (higher wins)', required: false
			_ longOpt:'id', args:1, argName:'serviceID', 'Service ID number - valid with "--search", "--remove" or "--modify" ONLY', required: false
			_ longOpt:'name', args:1, argName:'serviceName', 'service name', required: false
			_ longOpt:'desc', args:1, argName:'description', 'description', required: false
			_ longOpt:'theme', args:1, argName:'theme', "CAS theme to use with this service.", required: false
			_ longOpt:'pattern', args:1, argName:'pattern', 'regular expression or ant pattern to match service', required: false
			_ longOpt:'url', args:1, argName:'url', 'sample URL to test the ant/regex pattern', required: false
			_ longOpt:'release', args:Option.UNLIMITED_VALUES, valueSeparator: ',' , argName:'attribute list', "add to attribute list (seperate multiple with commas)"
			_ longOpt:'contactName', args:Option.UNLIMITED_VALUES, valueSeparator: ',' , argName:'name(s)', "contact person(s) for this service (seperate multiple with commas)", required: false
			_ longOpt:'contactEmail', args:Option.UNLIMITED_VALUES, valueSeparator: ',' , argName:'email(s)', "contact email address(es) for this service (seperate multiple with commas)", required: false
			_ longOpt:'contactPhone', args:Option.UNLIMITED_VALUES, valueSeparator: ',' , argName:'phone number(s)', "contact phone number(s) for this service (seperate multiple with commas)", required: false
			_ longOpt:'contactDept', args:1, argName:'college/department', "College or department this service belongs to", required: false
		}

		def opt = cli.parse(args)

		//Exit if no options were given
		if(!opt)System.exit(1)

		//Display usage if --help is given
		if(opt.help) {
			cli.usage() 
			System.exit(0)
		}

		//Read the config file data
		if (opt.defaults) configFileName = opt.defaults

		def configFile = new File(configFileName)
		if ((! configFile.exists()) || (!configFile.canRead())) {
			println "${configFile} is not readable or does not exist!"
			System.exit(1)
		}

		def configFileData = new ConfigSlurper().parse(configFile.toURL())
		config = config.merge(configFileData)

		def jsonParser = new JsonServiceRegistryParser()
		
		//Set defaults
		jsonParser.setCasAttributes config.releaseAttributes
		jsonParser.setRequiredAttributes config.requiredAttributes
		jsonParser.setDefaultTheme config.defaultTheme

		if(opt.input) jsonParser.readInputFile opt.input
		if(opt.output) jsonParser.setOutput opt.output
		if(opt.force) jsonParser.setClobber true 

		//Add a new service
		if(opt.n){
			jsonParser.checkOptions('new',opt)

			def jsonService = jsonParser.createRegisteredService opt	
			jsonParser.addService jsonService
			jsonParser.printJSON()
		//Remove a service
		} else if (opt.r){
			jsonParser.checkOptions('remove',opt)
			
			jsonParser.removeService opt.id
			jsonParser.printJSON()
		//Search for a service
		} else if (opt.s){
			jsonParser.checkOptions('search',opt)
			
			jsonParser.printJSON(jsonParser.searchForService(opt))
		//Modify service
		} else if (opt.m){
			jsonParser.checkOptions('modify',opt)
			
			def origService = jsonParser.findById opt.id
			def newService = jsonParser.modifyService(origService,opt)
			jsonParser.removeService opt.id
			jsonParser.addService newService
			jsonParser.printJSON()
		//No options, just display JSON
		} else {
			jsonParser.printJSON()
		}
	}

}