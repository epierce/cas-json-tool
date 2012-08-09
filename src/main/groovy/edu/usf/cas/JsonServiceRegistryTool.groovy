package edu.usf.cas

import groovy.util.CliBuilder
import org.apache.commons.cli.Option

class JsonServiceRegistryTool {

	public static void main(String[] args) {

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
		if (defaultConfigFile.exists() && defaultConfigFile.canRead()) {
			config = config.merge(new ConfigSlurper().parse(defaultConfigFile.toURL()))
		}

		//Parse command-line options
		def cli = new CliBuilder(
						usage:"JsonServiceRegistryTool --input service_registry.json [options]",
						header:"\nAvailable options (use -h for help):\n",
						width:100)

		cli.with {
			h longOpt:'help', 'usage information', required: false 
			i longOpt:'input', args:1, argName:'inputFilename', 'JSON file to read.', required: false
			o longOpt:'output', args:1, argName:'outputFilename', 'write output to this file.  Prints to STDOUT if omitted', required: false
			f longOpt:'force', 'overwrite output file', required: false
			n longOpt:'new', 'add new service', required: false
			r longOpt:'remove', 'remove service', required: false
			m longOpt:'modify', 'modify service', required: false
			s longOpt:'search', 'search for a service', required: false
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
			_ longOpt:'desc', args:1, argName:'description', 'description', required: false
			_ longOpt:'theme', args:1, argName:'theme', "CAS theme to use with this service.", required: false
			_ longOpt:'pattern', args:1, argName:'pattern', 'regular expression or ant pattern to match service', required: false
			_ longOpt:'url', args:1, argName:'url', 'sample URL to test the ant/regex pattern', required: false
			_ longOpt:'release', args:Option.UNLIMITED_VALUES, valueSeparator: ',' , argName:'attribute list', "add to attribute list (separate multiple with commas)"
			e longOpt:'extraAttribute', args:2, valueSeparator:'=', argName:'attribute=value',"add arbitrary extra attribute/value for this service (can be used multiple time)", required: false
		}

		def opt = cli.parse(args)

		//Exit if no options were given
		if(!opt)System.exit(1)

		//Display usage if --help is given
		if(opt.help) {
			cli.usage() 
			System.exit(0)
		}

		//Read the config file passed on the commandline
		if (opt.defaults) {
			def newConfigFile = new File(opt.defaults)
			if (newConfigFile.exists() && newConfigFile.canRead()) {
				config = config.merge(new ConfigSlurper().parse(newConfigFile.toURL()))
			} else {
				println "Defaults file (${opt.defaults}) cannot be read or does not exist!"
				System.exit(1)
			}
		}

		if (config.preCommand && opt.input){
			println "${config.preCommand} ${opt.input}".execute().text
		}

		def jsonParser = new JsonServiceRegistryParser()
		
		//Set defaults
		jsonParser.setCasAttributes config.releaseAttributes
		jsonParser.setExtraServiceAttributes(config.allowedExtraAttributes,config.requiredExtraAttributes)
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

		if (config.postCommand){
			println "${config.postCommand} ${opt.output}".execute().text
		}
	}

}