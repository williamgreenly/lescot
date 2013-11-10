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
import com.hp.hpl.jena.rdf.model.impl.ModelCom

class GroovyRdfModelTest extends GroovyTestCase {
	
	private GroovyRdfModel model
	private String turtle = '''

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
	
	kotg: a owl:Ontology;
		owl:imports kotw: ;
		owl:imports scxml: ;
		owl:imports mmi: ;
		owl:imports provenance: .
	
	kotg:Experience a rdfs:Class.
	kotg:Room a rdfs:Class.
	
	kotg:Event a rdfs:Class;
		rdfs:label "An event";
		rdfs:subClassOf event:Event.
	
	kotg:ExperienceEvent a rdfs:Class;
		rdfs:label "An experience event";
		rdfs:subClassOf kotg:Event.
	
	kotg:TransitionTriggerEvent a rdfs:Class;
		rdfs:subClassOf kotg:Event.
	
	# rooms with more than 3 healthcare professionals are rooms with healthcare professionals
	kotg:RoomsWithHealthCareProfessionals a rdfs:Class;
		owl:equivalentClass [
			a owl:Restriction;
			owl:onProperty kotg:occupiedBy;
			owl:minQualifiedCardinality "3"^^xsd:nonNegativeInteger;
			owl:onClass kotw:HealthCareProfessional
		].
	
	kotg:VoiceSynthesiserComponent a rdfs:Class;
		rdfs:subClassOf mmi:ModalityComponent.
	
	kotg:VoiceInputComponent a rdfs:Class;
		rdfs:subClassOf mmi:ModalityComponent.
	
	kotg:TouchScreenComponent a rdfs:Class.
	
	
	kotg:screenWidth a owl:DatatypeProperty;
		rdfs:range xsd:integer.
	
	kotg:screenHeight a owl:DatatypeProperty;
		rdfs:range xsd:integer.
	
	kotg:occupies a owl:ObjectProperty.
	
	kotg:occupiedBy a owl:ObjectProperty;
		owl:inverseOf kotg:occupies.
	
	kotg:life-room a kotg:Room.
	
	kotg:life-room-projector a mmi:ModalityComponent;
		scxml:hasState mmi:idle;
		scxml:hasState mmi:paused;
		scxml:hasState mmi:running.
	
	kotg:life-room-voice-input a mmi:ModalityComponent;
		scxml:hasState mmi:idle;
		scxml:hasState mmi:paused;
		scxml:hasState mmi:running.
	
	kotg:life-experience a kotg:Experience;
		scxml:hasState kotg:life-experience-idle;
		scxml:hasState kotg:life-experience-start;
		scxml:hasState kotg:life-experience-has-just-asked-a-question.
	
	# this is crufty and might break DL constraints
	kotg:life-experience-idle a scxml:State;
		scxml:hasTransition [
			scxml:event kotg:LifeExperienceStartEvent;
			scxml:target kotg:life-experience-start
		].


	'''
	
	void setUp() {
		
		model = new GroovyRdfModel()
		model.read(new ByteArrayInputStream(turtle.getBytes()), null, "TTL")
		
	}
	
	void testConstruction() {
		assert model.getNsPrefixMap().size() == 11
	}
	
	void testAddingATriplesWithStrings() {
		model.add("kotg:Test a kotg:Event.")
		assert model.contains(model.createResource("http://www.telekom.com/ns/kotg/Test"), model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.createResource("http://www.telekom.com/ns/kotg/Event"))
		model.add("kotg:Test rdfs:label 'blahblahblah'.")
		assert model.contains(model.createResource("http://www.telekom.com/ns/kotg/Test"), model.createProperty("http://www.w3.org/2000/01/rdf-schema#label"), model.createLiteral("blahblahblah"))
	}
	
	void testTurtleUri () {
		assert GroovyRdfModel.isTurtleUri("<http://123>", model)
		assert GroovyRdfModel.isTurtleUri("kotg:Event", model)
		assert !GroovyRdfModel.isTurtleUri("hello world", model)
	}

	void testAddingATripleWithSPO() {
		model.add("kotg:Test", "rdf:type", "kotg:Event")
		assert model.contains(model.createResource("http://www.telekom.com/ns/kotg/Test"), model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.createResource("http://www.telekom.com/ns/kotg/Event"))
		model.add("kotg:Test", "rdfs:label", "blahblahblah")
		assert model.contains(model.createResource("http://www.telekom.com/ns/kotg/Test"), model.createProperty("http://www.w3.org/2000/01/rdf-schema#label"), model.createLiteral("blahblahblah"))
		model.add("kotg:Test", "rdf:type", "<http://www.test.com>")
		assert model.contains(model.createResource("http://www.telekom.com/ns/kotg/Test"), model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.createResource("http://www.test.com"))
	}
	
	
	void testFindWithSelect() {
		model.add("kotg:Test a kotg:Event.")
		model.add("kotg:Test rdfs:label 'blahblahblah'.")
		def res = model.sparql("SELECT ?o WHERE {kotg:Test rdfs:label ?o. }")
		assert res.size() > 0
		res.each {
			log.info it['o']
			assert it['o'] == "blahblahblah"
		}
		model.add("kotg:Test2 a kotg:Event.")
		
		res = model.sparql("SELECT ?o WHERE {kotg:Test2 a ?o. }")
		assert res.size() > 0
		res.each {
			log.info it['o']
			assert it['o'] == "kotg:Event"
		}
	}
	
	
	void testFindWithAsk() {
		model.add("kotg:Test a kotg:Event.")
		assert model.sparql("ASK {kotg:Test a kotg:Event}")
	}
	
	void testFindWithDescribe() {
		model.add("kotg:Test a kotg:Event.")
		model.add("kotg:Test rdfs:label 'blahblahblah'.")
		def res = model.sparql("DESCRIBE kotg:Test")
		res.setNsPrefixes model.getNsPrefixMap()
		assert res.sparql("ASK {kotg:Test a kotg:Event}")
	}
	
	void testFindWithConstruct() {
		model.add("kotg:Test a kotg:Event.")
		model.add("kotg:Test rdfs:label 'blahblahblah'.")
		def res = model.sparql("CONSTRUCT {kotg:Test ?p ?o} WHERE {kotg:Test ?p ?o.}")
		res.setNsPrefixes model.getNsPrefixMap()
		assert res.sparql("ASK {kotg:Test a kotg:Event}")
		assert res.sparql("ASK {kotg:Test rdfs:label 'blahblahblah'}")
	}
	
	void testConstructWhere() {
		model.add("kotg:Test a kotg:Event.")
		model.add("kotg:Test rdfs:label 'blahblahblah'.")
		def res = model.constructWhere("kotg:Test ?p ?o")
		res.setNsPrefixes model.getNsPrefixMap()
		assert res.sparql("ASK {kotg:Test a kotg:Event}")
		assert res.sparql("ASK {kotg:Test rdfs:label 'blahblahblah'}")
	}
	
	void testInsertWithSparql() {
		model.update("INSERT DATA {kotg:Test a kotg:Event.}")
		assert model.sparql("ASK {kotg:Test a kotg:Event}")
	}
	
	void testDeleteWithSparql() {
		model.add("kotg:Test a kotg:Event.")
		model.update("DELETE DATA {kotg:Test a kotg:Event.}")
		assert !model.sparql("ASK {kotg:Test a kotg:Event}")
	}

	void testEach() {
		String stmt = '''

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
		model = new GroovyRdfModel()
		model.read(new ByteArrayInputStream(stmt.getBytes()), null, "TTL")
		model.each {
			assert it.s == "kotg:Experience"
			assert it.p == "rdf:type"
			assert it.o == "rdfs:Class"
		}
	}

	void testTurtle () {
		
		Model nm = ModelFactory.createDefaultModel()
		nm.read (new ByteArrayInputStream(model.turtle().getBytes()), null, "TTL")
    }

    void testAsk() {
    	model.add("kotg:rt a kotg:ExperienceEvent")
    	assert model.ask("kotg:rt a kotg:ExperienceEvent")
    }
    
    void testReason () {
    	model.add("kotg:rt a kotg:ExperienceEvent")
    	assert model.reason().ask("kotg:rt a kotg:Event")
    }	

    void testDeleteWhere() {
    	model.add("kotg:rt a kotg:ExperienceEvent")
    	model.deleteWhere("?a a kotg:ExperienceEvent")
    	
    	assert !model.ask("kotg:rt a kotg:ExperienceEvent")
    }



}
