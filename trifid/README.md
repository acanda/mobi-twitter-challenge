# Trifid

[Trifid](https://github.com/zazuko/trifid) provides a SPARQL web frontend
and is used for Linked Data URI [dereferencing](http://en.wikipedia.org/wiki/Dereferenceable_Uniform_Resource_Identifier).


The pre-built docker image is used:

    PS docker pull zazuko/trifid


Run the image:

    PS docker run -ti -e DEBUG=trifid:* -v ${PWD}/config.json:/app/config.json -p 8080:8080 zazuko/trifid
    

Or, run the image with verbose output to see the actual config after expansion:
     
	PS docker run -ti -e DEBUG=trifid:* -v ${PWD}/config.json:/app/config.json -p 8080:8080 zazuko/trifid npm run start -- --verbose


Open the SPARQL web frontend at http://localhost:8080/sparql/