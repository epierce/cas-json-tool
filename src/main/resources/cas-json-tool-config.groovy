//Default theme for CAS login page
defaultTheme = "default"

//All attributes that can be released by CAS
releaseAttributes = ["uid",
					"eduPersonPrimaryAffiliation",
					"cn",
					"givenName",
					"sn",
					"mail",
					"Title",
					"Phone"]

//Allow these extra attributes.  An empty list allows any attribute
allowedExtraAttributes = []

//Require these extra attributes for each service.  
//Attributes in this list are automatically included in allowedExtraAttributes
requiredExraAttributes = ["contactName","contactEmail","contactDept","contactPhone"]

//Run this command BEFORE processing. the input file is passed as an argument.
preCommand = ''
//Run this command AFTER processing.  the output file is passed as an argument.
postCommand = ''
