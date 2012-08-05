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

