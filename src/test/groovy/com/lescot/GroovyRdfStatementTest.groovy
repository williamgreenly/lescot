package com.lescot;

import static org.junit.Assert.*

import groovy.util.GroovyTestCase
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.shared.PrefixMapping
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.rdf.model.Literal
import java.io.ByteArrayOutputStream

class GroovyRdfStatementTest extends GroovyTestCase {

	private String statementWithResourceAsObject = '''

	@prefix owl: <http://www.w3.org/2002/07/owl#>.
	@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.
	@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.
	@prefix kotg: <http://www.telekom.com/ns/kotg/>.
	@prefix kotw: <http://www.telekom.com/ns/kotw/>.
	@prefix scxml: <http://www.w3.org/2005/07/scxml/>.
	@prefix mmi: <http://www.w3.org/2008/04/mmi-arch/>.
	@prefix event: <http://purl.org/NET/c4dm/event.owl#>.
	@prefix time: <http://www.w3.org/2006/time#>.
	@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.
	@prefix provenance: <http://www.w3.org/ns/prov#>.
	
	
	kotg:Experience a rdfs:Class.
	
	'''

	private String statementWithLiteralAsObject = '''

	@prefix owl: <http://www.w3.org/2002/07/owl#>.
	@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.
	@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.
	@prefix kotg: <http://www.telekom.com/ns/kotg/>.
	@prefix kotw: <http://www.telekom.com/ns/kotw/>.
	@prefix scxml: <http://www.w3.org/2005/07/scxml/>.
	@prefix mmi: <http://www.w3.org/2008/04/mmi-arch/>.
	@prefix event: <http://purl.org/NET/c4dm/event.owl#>.
	@prefix time: <http://www.w3.org/2006/time#>.
	@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.
	@prefix provenance: <http://www.w3.org/ns/prov#>.
	
	
	kotg:Experience rdfs:label "lahlah".
	
	'''

	private GroovyRdfStatement stmtRes
	private GroovyRdfStatement stmtLit

	void setUp() {
		def modelRes = ModelFactory.createDefaultModel()
		modelRes.read(new ByteArrayInputStream(statementWithResourceAsObject.getBytes()), null, "TTL")
		modelRes.listStatements().each {
			stmtRes = new GroovyRdfStatement(it, modelRes)
		}

		def modelLit = ModelFactory.createDefaultModel()
		modelLit.read(new ByteArrayInputStream(statementWithLiteralAsObject.getBytes()), null, "TTL")
		modelLit.listStatements().each {
			stmtLit = new GroovyRdfStatement(it, modelLit)
		}
	}

	void testSPO () {
		assert stmtRes.s == "kotg:Experience"
		assert stmtRes.p == "rdf:type"
		assert stmtRes.o == "rdfs:Class"
		assert stmtLit.o == "lahlah"
	}

}
