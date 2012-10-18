package edu.usf.cas

import spock.lang.*


class JsonServiceRegistryParserSpec extends spock.lang.Specification {

    def "Empty JSON Service Registry"() {
    
        when:
        def jsonParser = new JsonServiceRegistryParser()

        then:
        jsonParser.jsonObject == [ services:[] ]
    }

    def "JSON input file doesn't exist"() {
    	given:
    	def jsonParser = new JsonServiceRegistryParser()

        when:
        jsonParser.readInputFile("file_does_not_exist.json")

        then:
   		thrown(java.io.FileNotFoundException)
    }

    def "input file with bad JSON data"() {
        given:
        def jsonParser = new JsonServiceRegistryParser()

        when:
        jsonParser.readInputFile("src/test/resources/bad_format.json")

        then:
        thrown(groovy.json.JsonException)
    }
    
    def "input file without valid JSON ServiceRegistry"() {
        given:
        def jsonParser = new JsonServiceRegistryParser()

        when:
        jsonParser.readInputFile("src/test/resources/bad_serviceRegistry.json")

        then:
        thrown(edu.usf.cas.JsonServiceRegistryFileFormatException)
    }
}