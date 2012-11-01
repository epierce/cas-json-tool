package edu.usf.cas

import spock.lang.*

class JsonServiceRegistryParserSpec extends spock.lang.Specification {

    def "Empty JSON Service Registry"() {
    
        when:
        def jsonParser = new JsonServiceRegistryParser()

        then:
        jsonParser.jsonData == [ services:[] ]
    }
    
    def "input file without valid JSON ServiceRegistry"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        def object = jsonTool.readInputFile("src/test/resources/bad_serviceRegistry.json")
        def jsonParser = new JsonServiceRegistryParser()

        when:
        jsonParser.setJsonData(object)

        then:
        thrown(edu.usf.cas.JsonServiceRegistryFileFormatException)
    }

    def "input file with valid JSON ServiceRegistry"() {
        given:
        def jsonTool = new JsonServiceRegistryTool()
        def object = jsonTool.readInputFile("src/test/resources/serviceRegistry.json")
        def jsonParser = new JsonServiceRegistryParser()

        when:
        jsonParser.setJsonData(object)

        then:
        jsonParser.jsonData.services[0].ssoEnabled == true
        jsonParser.jsonData.services[0].name == "Example Application"
        jsonParser.jsonData.services[0].description == "This is an example application"
        jsonParser.jsonData.services[0].ssoEnabled == true
    }    


}