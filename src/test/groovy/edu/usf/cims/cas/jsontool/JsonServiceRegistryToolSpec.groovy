package edu.usf.cims.cas.jsontool

import spock.lang.*
import groovy.json.*

class JsonServiceRegistryToolSpec extends spock.lang.Specification {

    def "JSON input file doesn't exist"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()

        when:
        jsonTool.readInputFile("file_does_not_exist.json")

        then:
        def e = thrown(java.io.FileNotFoundException)
    }

    def "input file with bad JSON data"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()

        when:
        jsonTool.readInputFile("src/test/resources/bad_format.json")

        then:
        thrown(groovy.json.JsonException)
    }

    def "Missing defaults file"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = ['--defaults=no_file.groovy','--input=src/test/resources/serviceRegistry.json']

        when:
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)

        then:
        thrown(java.io.FileNotFoundException)
    }

    def "Defaults file is not readable"() {
        given:
        def file = new File("no_file.groovy")
        file.createNewFile()
        file.setReadable(false)

        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--defaults=no_file.groovy',
                            '--input=src/test/resources/serviceRegistry.json']

        when:
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)

        then:
        thrown(java.io.FileNotFoundException)

        cleanup:
        file.delete()
    } 

    def "Defaults file"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--defaults=src/test/resources/defaults.groovy',
                            '--input=src/test/resources/serviceRegistry.json']

        when:
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)

        then:
        notThrown(java.io.FileNotFoundException)
        config.defaultTheme == "default"
        config.releaseAttributes == ["name", "email"]
        config.allowedExtraAttributes == ["ownerName", "organization"]
        config.preCommand == "/bin/true"
        config.postCommand == "/bin/false"
        config.requiredExtraAttributes == ["ownerName"]

    }   

    def "PreProcessor Failure"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = ['--input=src/test/resources/serviceRegistry.json']
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.preCommand = "false"

        when:
        jsonTool.runPreProcessor(config,opt)

        then:
        thrown(groovy.util.ScriptException)
    }

    def "PreProcessor success"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = ['--input=src/test/resources/serviceRegistry.json']
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.preCommand = "true"

        when:
        jsonTool.runPreProcessor(config,opt)

        then:
        notThrown(groovy.util.ScriptException)
    }

    def "PostProcessor Failure"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = ['--input=src/test/resources/serviceRegistry.json','--output=PostProcessor.test.json']
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.postCommand = "false"

        when:
        jsonTool.runPostProcessor(config,opt)

        then:
        thrown(groovy.util.ScriptException)

        cleanup:
        def file = new File("PostProcessor.test.json")
        file.delete()
    }

    def "PostProcessor success"() {
      given:
      def jsonTool = new JsonServiceRegistryTool()
      String[] args = ['--input=src/test/resources/serviceRegistry.json','--output=PostProcessor.test.json']
      def opt = jsonTool.getCommandLineOptions(args)
      def config = jsonTool.getConfigSettings(opt)
      config.postCommand = "true"
      def jsonParser = jsonTool.createJSONparser(config,opt)
      def result = jsonTool.runAction(jsonParser,opt)
      jsonTool.printJSON(result)

      when:
      jsonTool.runPostProcessor(config,opt)

      then:
      notThrown(groovy.util.ScriptException)

      cleanup:
      def file = new File("PostProcessor.test.json")
      file.delete()
    }

     def "Force option required to overwrite output JSON file"() {
      given:
      def file = new File("write_fail.test.json")
      file.createNewFile()

      def jsonTool = new JsonServiceRegistryTool()
      String[] args = ['--input=src/test/resources/serviceRegistry.json','--output=write_fail.test.json']
      def opt = jsonTool.getCommandLineOptions(args)
      def config = jsonTool.getConfigSettings(opt)

      when:
      def jsonParser = jsonTool.createJSONparser(config,opt)

      then:
      def e = thrown(java.io.FileNotFoundException)
      e.message == "write_fail.test.json already exists.  Use --force to overwrite."

      cleanup:
      file.delete()
    }

    def "Force option required to overwrite output CSV file"() {
      given:
      def file = new File("write_fail.test.csv")
      file.createNewFile()

      def jsonTool = new JsonServiceRegistryTool()
      String[] args = ['--input=src/test/resources/serviceRegistry.json','--csv=write_fail.test.csv']
      def opt = jsonTool.getCommandLineOptions(args)
      def config = jsonTool.getConfigSettings(opt)

      when:
      def jsonParser = jsonTool.createJSONparser(config,opt)

      then:
      def e = thrown(java.io.FileNotFoundException)
      e.message == "write_fail.test.csv already exists.  Use --force to overwrite."

      cleanup:
      file.delete()
    }

    def "Cannot write to output JSON file"() {
        given:
        def file = new File("write_fail.test.json")
        file.createNewFile()
        file.setWritable(false)

        def jsonTool = new JsonServiceRegistryTool()
        String[] args = ['--input=src/test/resources/serviceRegistry.json','--output=write_fail.test.json','--force']
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)

        when:
        def jsonParser = jsonTool.createJSONparser(config,opt)

        then:
        thrown(java.io.FileNotFoundException)

        cleanup:
        file.setWritable(true)
        file.delete()
    }

    def "Cannot write to output CSV file"() {
      given:
      def file = new File("write_fail.test.csv")
      file.createNewFile()
      file.setWritable(false)

      def jsonTool = new JsonServiceRegistryTool()
      String[] args = ['--input=src/test/resources/serviceRegistry.json','--csv=write_fail.test.csv','--force']
      def opt = jsonTool.getCommandLineOptions(args)
      def config = jsonTool.getConfigSettings(opt)

      when:
      def jsonParser = jsonTool.createJSONparser(config,opt)

      then:
      thrown(java.io.FileNotFoundException)

      cleanup:
      file.setWritable(true)
      file.delete()
    }

    def "--name is required"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "Service name required!"
    }

    def "--desc is required"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new', 
                            '--name=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "Service description required!"
    }

    def "--disable and --enable cannot be used together"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--enable',
                            '--disable'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "--disable and --enable cannot be used together"
    }

    def "--disableSSO and --enableSSO cannot be used together"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--disableSSO',
                            '--enableSSO'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "--disableSSO and --enableSSO cannot be used together"
    }

    def "--disableAnonymous and --enableAnonymous cannot be used together"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--disableAnonymous',
                            '--enableAnonymous'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "--disableAnonymous and --enableAnonymous cannot be used together"   
    }

    def "--disableProxy and --enableProxy cannot be used together"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--disableProxy',
                            '--enableProxy'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "--disableProxy and --enableProxy cannot be used together"
    }

    def "URL does not match the ServiceId Ant Pattern"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://notamatch.example.org/index.php'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "Test URL does not match pattern"
    }

    def "URL does not match the ServiceId RegEx Pattern"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=^https://example.org/.*', 
                            '--url=https://notamatch.example.org/index.php'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "Test URL does not match pattern"
    }

    def "URL matches the ServiceId RegEx Pattern"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=^https://example.org/.*', 
                            '--url=https://example.org/index.php'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(java.lang.IllegalArgumentException)
        result.services[0].name == "My Service"
        result.services[0].serviceId == "^https://example.org/.*"
        result.services[0].extraAttributes.size() == 1
    }

    def "Allow any extra attribute is allowedExtraAttributes is not set"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--extraAttribute=ownerName=Bruce Wayne'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(java.lang.IllegalArgumentException)
        result.services[0].name == "My Service"
        result.services[0].serviceId == "https://example.org/**"
        result.services[0].extraAttributes.ownerName == ["Bruce Wayne"]
    }

    def "Try to use attributes that have not been allowed"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--extraAttribute=ownerName=Bruce Wayne'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.allowedExtraAttributes = ['organization']
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "Attribute ownerName is not allowed!"
    }

    def "Release attributes to a new service"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--release=name,email'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.releaseAttributes = ['name','email']
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(java.lang.IllegalArgumentException)
        result.services[0].name == "My Service"
        result.services[0].serviceId == "https://example.org/**"
        result.services[0].allowedAttributes == ['name','email']
    }

    def "Release different username attribute to a service"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--userAttribute=email',
                            '--release=name,email'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.releaseAttributes = ['name','email']
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(java.lang.IllegalArgumentException)
        result.services[0].name == "My Service"
        result.services[0].serviceId == "https://example.org/**"
        result.services[0].allowedAttributes == ['name','email']
        result.services[0].usernameAttribute == "email"
    }

    def "Release attribute not in the allowed list"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--release=name,email'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.releaseAttributes = ['name',]
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == 'Attribute email can not be released'
    }

    def "Release usernameAttribute not in the allowed list"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--release=name',
                            '--userAttribute=email'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.releaseAttributes = ['name',]
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == 'Username Attribute email is not being released for this service'
    }

    def "Create new service with extra attributes"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--extraAttribute=ownerName=Bruce Wayne',
                            '--extraAttribute=ownerOrg=Wayne Enterprises'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.allowedExtraAttributes = ['ownerName','ownerOrg']
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(java.lang.IllegalArgumentException)
        result.services[0].name == "My Service"
        result.services[0].serviceId == "https://example.org/**"
        result.services[0].extraAttributes.ownerName == ["Bruce Wayne"]
        result.services[0].extraAttributes.ownerOrg == ["Wayne Enterprises"]

    }

    def "Create new service with role-based access controls"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--authzName=memberOf',
                            '--authzValue=group1,group2'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(java.lang.IllegalArgumentException)
        result.services[0].name == "My Service"
        result.services[0].serviceId == "https://example.org/**"
        result.services[0].extraAttributes.authzAttributes.memberOf == ["group1","group2"]
    }

    def "Require authzValue when authzName is given"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--authzName=memberOf'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "Authorization values are required when passing a authorization attribute name"
    }

    def "Require authzValue when authzName is given"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--authzValue=group1,group2'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "Authorization attribute name required when passing authorization values"
    }
    def "Create new service with multiple extra attributes"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--extraAttribute=member=Batman',
                            '--extraAttribute=member=Superman',
                            '--extraAttribute=organization=Justice League'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.allowedExtraAttributes = ['member','organization']
        config.requiredExtraAttributes = ['member', 'organization']
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(java.lang.IllegalArgumentException)
        result.services[0].name == "My Service"
        result.services[0].serviceId == "https://example.org/**"
        result.services[0].extraAttributes.member == ["Batman", "Superman"]
        result.services[0].extraAttributes.organization == ["Justice League"]

    }

    def "Create new service that requires extra attributes"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service', 
                            '--pattern=https://example.org/**', 
                            '--url=https://example.org/index.php',
                            '--extraAttribute=member=Batman',
                            '--extraAttribute=member=Superman'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.allowedExtraAttributes = ['member','organization']
        config.requiredExtraAttributes = ['member', 'organization']
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "Attribute organization is required!"

    }

    def "Create new service with none of the default values"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--new',
                            '--name=My Service', 
                            '--desc=Test Service',
                            '--theme=JusticeLeague',
                            '--pattern=https://example.org/**',
                            '--url=https://example.org/index.php',
                            '--evalOrder=50',
                            '--disable', 
                            '--enableAnonymous',
                            '--disableSSO',
                            '--enableProxy'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(java.lang.IllegalArgumentException)
        result.services[0].name == "My Service"
        result.services[0].enabled == false
        result.services[0].anonymousAccess == true
        result.services[0].ssoEnabled == false
        result.services[0].allowedToProxy == true
        result.services[0].evaluationOrder == 50

    }

    def "Service ID number required for modify"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--modify',
                            '--input=src/test/resources/serviceRegistry.json',
                            '--name=My Updated Service'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "Service ID number required!"
    }

    def "Disable existing service"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--modify',
                            '--id=1',
                            '--input=src/test/resources/serviceRegistry.json',
                            '--disable'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)
        assert jsonParser.jsonData.services[0].extraAttributes.modifiedDate == null

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(java.lang.IllegalArgumentException)
        result.services.last().name == "Example Application"
        result.services.last().enabled == false
        result.services.last().extraAttributes.modifiedDate != null
    }

    def "Update release attributes"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--modify',
                            '--id=1',
                            '--input=src/test/resources/serviceRegistry.json',
                            '--release=email'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.releaseAttributes = ["name","email"]
        def jsonParser = jsonTool.createJSONparser(config,opt)
        assert jsonParser.jsonData.services[0].allowedAttributes == ["name","email"]

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(java.lang.IllegalArgumentException)
        result.services.last().name == "Example Application"
        result.services.last().allowedAttributes == ["email"]
    }

    def "Update username attribute"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--modify',
                            '--id=1',
                            '--input=src/test/resources/serviceRegistry.json',
                            '--release=email',
                            '--userAttribute=email'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.releaseAttributes = ["name","email"]
        def jsonParser = jsonTool.createJSONparser(config,opt)
        assert jsonParser.jsonData.services[0].allowedAttributes == ["name","email"]
        assert jsonParser.jsonData.services[0].usernameAttribute == null

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(java.lang.IllegalArgumentException)
        result.services.last().name == "Example Application"
        result.services.last().allowedAttributes == ["email"]
        result.services.last().usernameAttribute == "email"
    }

       def "Update username attribute to one that is not released"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--modify',
                            '--id=1',
                            '--input=src/test/resources/serviceRegistry.json',
                            '--release=email',
                            '--userAttribute=name'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.releaseAttributes = ["name","email"]
        def jsonParser = jsonTool.createJSONparser(config,opt)
        assert jsonParser.jsonData.services[0].allowedAttributes == ["name","email"]
        assert jsonParser.jsonData.services[0].usernameAttribute == null

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "Username Attribute name is not being released for this service"
    }

    def "Remove release attributes"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--modify',
                            '--id=1',
                            '--input=src/test/resources/serviceRegistry.json',
                            '--release=REMOVE'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.releaseAttributes = ["name","email"]
        def jsonParser = jsonTool.createJSONparser(config,opt)
        assert jsonParser.jsonData.services[0].allowedAttributes == ["name","email"]

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(java.lang.IllegalArgumentException)
        result.services.last().name == "Example Application"
        result.services.last().allowedAttributes == []
        result.services.last().usernameAttribute == null
    }

    def "Remove extraAttribute from service"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--modify',
                            '--id=1',
                            '--input=src/test/resources/serviceRegistry.json',
                            '--extraAttribute=contactEmail=REMOVE'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.allowedExtraAttributes = ['contactEmail','contactName']
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(java.lang.IllegalArgumentException)
        result.services.last().extraAttributes.contactName == ["Admin User"]
        result.services.last().extraAttributes.contactEmail == null
    }

    def "Do not allow a required extraAttribute to be removed"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--modify',
                            '--id=1',
                            '--input=src/test/resources/serviceRegistry.json',
                            '--extraAttribute=contactEmail=REMOVE'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.allowedExtraAttributes = ['contactEmail','contactName']
        config.requiredExtraAttributes = ['contactEmail', 'contactName']
        def jsonParser = jsonTool.createJSONparser(config,opt)
        assert jsonParser.jsonData.services[0].extraAttributes.contactEmail == ["admin@example.edu"]

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "Attribute contactEmail is required!"
    }

    def "Require authzValue when authzName is given"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--modify',
                            '--id=1',
                            '--input=src/test/resources/serviceRegistry.json',
                            '--authzName=memberOf'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.allowedExtraAttributes = ['contactEmail','contactName']
        config.requiredExtraAttributes = ['contactEmail', 'contactName']
        def jsonParser = jsonTool.createJSONparser(config,opt)
        assert jsonParser.jsonData.services[0].extraAttributes.contactEmail == ["admin@example.edu"]

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "Authorization values are required when passing a authorization attribute name"
    }

    def "Require authzValue when authzName is given"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--modify',
                            '--id=1',
                            '--input=src/test/resources/serviceRegistry.json',
                            '--authzValue=group1,group2'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        config.allowedExtraAttributes = ['contactEmail','contactName']
        config.requiredExtraAttributes = ['contactEmail', 'contactName']
        def jsonParser = jsonTool.createJSONparser(config,opt)
        assert jsonParser.jsonData.services[0].extraAttributes.contactEmail == ["admin@example.edu"]

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "Authorization attribute name required when passing authorization values"
    }

    def "Service ID number required for remove"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--remove',
                            '--input=src/test/resources/serviceRegistry.json',
                            '--name=My Service'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "Service ID number required!"
    }

    def "Remove service"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--remove',
                            '--id=1',
                            '--input=src/test/resources/serviceRegistry.json'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)
        assert jsonParser.jsonData.services.size() == 2
        assert jsonParser.jsonData.services[0].name == "Example Application"
        assert jsonParser.jsonData.services[0].extraAttributes.contactEmail == ["admin@example.edu"]

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(java.lang.IllegalArgumentException)
        jsonParser.jsonData.services.size() == 1
    }


    def "Service ID, name or URL required for search"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--search',
                            '--input=src/test/resources/serviceRegistry.json'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        def e = thrown(java.lang.IllegalArgumentException)
        e.message == "URL, id, or name required!"
    }

    def "Search by id"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--search',
                            '--id=2',
                            '--input=src/test/resources/serviceRegistry.json'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)
        assert jsonParser.jsonData.services.size() == 2
        assert jsonParser.jsonData.services[0].name == "Example Application"
        assert jsonParser.jsonData.services[1].name == "Portal Server"

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(JsonServiceParserException)
        result.services.size() == 1
        result.services[0].name == "Portal Server"
        result.services[0].id == 2
    }

    def "Search by name"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--search',
                            '--name=Example.*',
                            '--input=src/test/resources/serviceRegistry.json'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)
        assert jsonParser.jsonData.services.size() == 2
        assert jsonParser.jsonData.services[0].name == "Example Application"
        assert jsonParser.jsonData.services[1].name == "Portal Server"

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(JsonServiceParserException)
        result.services.size() == 1
        result.services[0].name == "Example Application"
        result.services[0].id == 1
    }

    def "Search by URL"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--search',
                            '--url=https://portal.example.edu/index.jsp',
                            '--input=src/test/resources/serviceRegistry.json'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)
        assert jsonParser.jsonData.services.size() == 2
        assert jsonParser.jsonData.services[0].name == "Example Application"
        assert jsonParser.jsonData.services[1].name == "Portal Server"

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(JsonServiceParserException)
        result.services.size() == 1
        result.services[0].name == "Portal Server"
        result.services[0].id == 2
    }

    def "Return multiple services"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        String[] args = [   '--search',
                            '--name=.*',
                            '--input=src/test/resources/serviceRegistry.json'
                        ]
        def opt = jsonTool.getCommandLineOptions(args)
        def config = jsonTool.getConfigSettings(opt)
        def jsonParser = jsonTool.createJSONparser(config,opt)
        assert jsonParser.jsonData.services.size() == 2
        assert jsonParser.jsonData.services[0].name == "Example Application"
        assert jsonParser.jsonData.services[1].name == "Portal Server"

        when:
        def result = jsonTool.runAction(jsonParser,opt)

        then:
        notThrown(JsonServiceParserException)
        result.services.size() == 2
        result.services[0].name == "Example Application"
        result.services[1].name == "Portal Server"
    }
}