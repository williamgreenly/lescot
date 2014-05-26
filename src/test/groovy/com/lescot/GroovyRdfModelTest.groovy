package com.lescot;

import static org.junit.Assert.*
import java.util.UUID
import groovy.util.GroovyTestCase
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.shared.PrefixMapping
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.rdf.model.Literal
import java.io.ByteArrayOutputStream
import com.hp.hpl.jena.rdf.model.impl.ModelCom
import com.lescot.sparql.Datastore
import static java.util.UUID.randomUUID
import com.hp.hpl.jena.query.Query
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.query.QueryExecution
import com.hp.hpl.jena.query.QueryExecutionFactory
import com.github.jsonldjava.jena.*    


class GroovyRdfModelTest extends GroovyTestCase {
	
	static {
	    JenaJSONLD.init();       
	}

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
	@prefix spin: <http://spinrdf.org/spin#>.
	@prefix sp: <http://spinrdf.org/sp#>.
	@prefix spl: <http://spinrdf.org/spl#>.
	@prefix coo: <http://purl.org/coo/ns#>.
	@prefix gr: <http://purl.org/goodrelations/v1#>.

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

	kotg:inroom a owl:ObjectProperty.
	
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

	#spin
	kotg:Person a rdfs:Class;
		spin:rule
              [ a       sp:Construct ;
                sp:text """
                    CONSTRUCT {
					    ?this kotg:inroom ?something .
					}
					WHERE {
					    ?this kotg:occupies ?something .
					}"""
              ] .

	'''

	def load = {filename ->
		return this.getClass().getResource('/' + filename ).text
	}

	private fusekiUri = "http://localhost:3030/ds"
	
	void setUp() {
		
		model = new GroovyRdfModel()
		model.read(new ByteArrayInputStream(turtle.getBytes()), null, "TTL")
		
	}
	
	void testConstruction() {
		assert model.getNsPrefixMap().size() == 16
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

    void testJson () {
		
		Model nm = ModelFactory.createDefaultModel()
		nm.read (new ByteArrayInputStream(model.json().getBytes()), null, "JSON-LD")
    }

    void testXml () {
		
		Model nm = ModelFactory.createDefaultModel()
		nm.read (new ByteArrayInputStream(model.xml().getBytes()), null, "RDF/XML")
    }


    void testAsk() {
    	model.add("kotg:rt a kotg:ExperienceEvent")
    	assert model.ask("kotg:rt a kotg:ExperienceEvent")
    }
    
    void testReason () {
    	model.add("kotg:rt a kotg:ExperienceEvent")
    	assert model.reason().ask("kotg:rt a kotg:Event")
    }	

    
    void testReasonInNewModel() {
    	def px = '''
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
    	'''
    	def tboxterms = new GroovyRdfModel()
    	
    	tboxterms.add(px)
    	tboxterms.add("kotg:Test2 a rdfs:Class. kotg:Test1 a rdfs:Class. kotg:Test1 rdfs:subClassOf kotg:Test2")
    	tboxterms.add("kotg:x a kotg:Test1")
    	def res = model.reason(tboxterms)
    	assert (res.ask("kotg:x a kotg:Test2"))
    	assert (!model.ask("kotg:Test2 a rdfs:Class"))
    }
    

    void testSpin() {
    	model.add("kotg:testspin a kotg:Person. kotg:testspin kotg:occupies kotg:testroom.")
    	log.info(model.spin().turtle())
    	assert model.spin().ask("kotg:testspin kotg:inroom kotg:testroom")
    }

    void testPutPostGetAndDelete() {
    	def datastore = new Datastore(fusekiUri)
    	model.datastore = datastore
    	def uuid = randomUUID() as String
    	model.uri = "http://test/" + uuid
    	model.put()

    	def sparql = """
    	CONSTRUCT {?s ?p ?o}
    	WHERE {
    		GRAPH <${model.uri}>
    		{?s ?p ?o}.
    	}
    	"""
    	log.info sparql
    	def res = model.sparqlRemote(sparql)
    	assert res.ask("kotg:inroom a owl:ObjectProperty")
    	def m2 = new GroovyRdfModel(model.getNsPrefixMap(), model.uri, model.datastore)
    	m2.add("kotg:fu kotg:man kotg:chu")
    	m2.post()
    	model.get()
    	assert model.ask("kotg:fu kotg:man kotg:chu")
    	model.delete()
    	res = model.sparqlRemote(sparql)
    	assert !res.ask("kotg:inroom a owl:ObjectProperty")
    }

     void testAddRdfa() {
    
    	String html = load("rdfa.html")

    	String base = "http://www.telekom.com/ns/kotg/doc"
    	model.addRdfa(html, base)
    	
    	assert model.ask("?s gr:hasValue ?o")
    }

    void testAddRdfaLite() {
    	String html = """
    	<!DOCTYPE html>
		<html >
	    <head>
	                
	    </head>
	    <body>
			<p vocab='http://schema.org/' resource='#manu' typeof='Person'>
			   My name is
			   <span property='name'>Manu Sporny</span>
			   and you can give me a ring via
			   <span property='telephone'>1-800-555-0199</span>.
			   <img property='image' src='http://manu.sporny.org/images/manu.png' />
			</p>
		</body>
		</html>
    	"""
    	model.addRdfa(html, "http://something")

    	log.info model.turtle()
    }


/*
    void testDeleteWhere() {
    	model.add("kotg:rt a kotg:ExperienceEvent")
    	model.deleteWhere("kotg:rt a kotg:ExperienceEvent")
    	
    	assert !model.ask("kotg:rt a kotg:ExperienceEvent")
    }
*/


}
