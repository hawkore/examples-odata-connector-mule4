# Hawkore's OData connector for Mule 4

This connector allows you integrate your mule applications with Open Data services ([OData](https://www.odata.org/)). 

Supported OData protocols:

- [OData Version 2.0 protocol](https://www.odata.org/documentation/odata-version-2-0/)
- [OData Version 4.0 protocol](https://www.odata.org/documentation/)

Main features:
 
- Create, Read, Update, Delete and Query entities.
- Send a group of modifying requests (Create, Update, Delete) into a **single batch request**.
- **Manage relationship** between entities.

**DataSense** is available in all operations to easy data transformation when using Anypoint Studio.

_**DataSense** is a feature of Anypoint Studio that automatically infers input and output data structures to easy data transformations when designing your mule applications._

*All company names, logos, brand names and trademarks are property of their respective owners. All company, product and service names used in this product are for identification purposes only. Use of these names, logos, and brands does not imply endorsement.*

## Requirements

	-  Java >= 1.8.0_65 (OpenJDK and Sun have been tested)
	-  Maven >= 3.3.0
	-  Mule 4.2.1 (EE or community)k
	-  Anypoint Studio 7+ (Recomended version 7.3.4)

## Clone

	-  Clone this project: `git clone http://github.com/hawkore/examples-odata-connector-mule4.git`

## Content

1. `odata-server-for-test`: OData servers to test Hawkore's OData connector for Mule 4. See [README.md](odata-server-for-test/README.md) for more info.

2. `sample-odata-connector-mule4-app`: Mule 4 application to test [Hawkore's OData connector for Mule 4
](https://docs.hawkore.com/private/odata-connector-mule4/). See [README.md](sample-odata-connector-mule4-app/README.md) for more info.


# More resources

Sign up at [www.hawkore.com](https://www.hawkore.com) to access full documentation.
