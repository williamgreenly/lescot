package com.lescot
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.shared.PrefixMapping
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.rdf.model.Literal
import groovy.util.logging.*
import java.io.ByteArrayOutputStream


class LescotApp {

	static main(args) {
		GroovyRdfModel model = new GroovyRdfModel()
		model.findWithSparql()
	}

}


