package com.lescot.builders

import static org.junit.Assert.*

import groovy.util.GroovyTestCase
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.shared.PrefixMapping
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.rdf.model.Literal
import java.io.ByteArrayOutputStream



class RdfBuilderTest extends GroovyTestCase {

	Model testModel
	def builder
	def primedBuilder
	def prefixes = [:]
	def testResource
	def prefixedBuilder
	
	void setUp() {
		
		this.testModel = ModelFactory.createDefaultModel()
		this.prefixes = [foaf:"http://xmlns.com/foaf/0.1/", dc:"http://purl.org/elements/1.1/"]
		this.testModel.setNsPrefixes(this.prefixes)
		this.builder = new RdfBuilder()
		this.primedBuilder = new RdfBuilder(this.testModel, this.prefixes)
		this.prefixedBuilder = new RdfBuilder(this.prefixes)
	}
	
	
	void testCreatingANewModelWithAnEmptyModelandWithPrefixes(){
		assert this.testModel.getNsPrefixMap().size() == 2
		assert this.testModel.getNsPrefixMap().containsKey("foaf")
		assert this.testModel.getNsPrefixMap()['foaf'].equals("http://xmlns.com/foaf/0.1/")
		assert this.testModel.getNsPrefixMap().containsKey("dc")
		assert this.testModel.getNsPrefixMap()['dc'].equals("http://purl.org/elements/1.1/")
	}
	
	void testCreatingANewModelWithAnEmptyModelWithoutPrefixes() {
		this.testModel = ModelFactory.createDefaultModel()
		def builder = new RdfBuilder().Model(model:this.testModel)
		assert this.testModel.getNsPrefixMap().size() == 0
	}
	
	void testCreatingANewModelWithoutPrefixes () {
		def newModel = this.builder.Model()
		assert  newModel.getNsPrefixMap().size() == 0
	}
	void testCreatingANewModelWithPrefixes() {
		def newModel = this.builder.Model(prefixes:this.prefixes)
		assert this.testModel.getNsPrefixMap().size() == 2
	}
	
	void testCreatingANewPrefixedModel() {
		def newModel = this.prefixedBuilder.Model()
		assert this.testModel.getNsPrefixMap().size() == 2
	}
	
	
	
	void testCreatingANewModelWithAModelWithStatementsWithPrefixes() {
		this.testModel.add(this.testModel.createResource("http://subject/1"), this.testModel.createProperty("http://predicate/1"), this.testModel.createResource("http://object/1"))
		def builder = new RdfBuilder().Model(model:this.testModel)
		assert this.testModel.contains(this.testModel.createResource("http://subject/1"), this.testModel.createProperty("http://predicate/1"), this.testModel.createResource("http://object/1"))	
	}
	
	void testCreatingANewModelAndAddingAPrefixWithTheDsl() {
		this.builder.Model(model:this.testModel, prefixes:this.prefixes) {
			Prefix (test:"http://test.com/")
		}
		assert this.testModel.getNsPrefixMap()['test'].equals("http://test.com/")
		assert this.testModel.getNsPrefixMap().size() == 3
	}
	
	void testCreatingANewStatementWithALiteralAsObject () {
		this.builder.Model(model:this.testModel, prefixes:this.prefixes) {
			Resource("http://subject/1") {
				Predicate("http://predicate/1") {
					Literal("John Wayne")
				}
			}
		}
		assert this.testModel.contains(this.testModel.createResource("http://subject/1"),this.testModel.createProperty("http://predicate/1"), this.testModel.createLiteral("John Wayne"))
	}
	
	void testCreatingANewStatementWithAResourceAsObject () {
		this.builder.Model(model:this.testModel, prefixes:this.prefixes) {
			Resource("http://subject/1") {
				Predicate("http://predicate/1") {
					Resource("http://object/1")
				}
			}
		}
		assert this.testModel.contains(this.testModel.createResource("http://subject/1"),this.testModel.createProperty("http://predicate/1"), this.testModel.createResource("http://object/1"))
	}
	
	void testCreatingANewStatmentWithAPrefixedProperty () {
		this.builder.Model(model:this.testModel, prefixes:this.prefixes) {
			Resource("http://subject/1") {
				Predicate("foaf:knows") {
					Resource("http://object/1")
				}
			}
		}
		assert this.testModel.contains(this.testModel.createResource("http://subject/1"),this.testModel.createProperty(this.prefixes["foaf"].toString() + "knows"), this.testModel.createResource("http://object/1"))
	}
	
	void testCreatingANewStatmentWithAPrefixedResource () {
		this.builder.Model(model:this.testModel, prefixes:this.prefixes) {
			Resource("foaf:Will") {
				Predicate("http://predicate/1") {
					Resource("http://object/1")
				}
			}
		}
		assert this.testModel.contains(this.testModel.createResource(this.prefixes["foaf"].toString() + "Will"),this.testModel.createProperty("http://predicate/1"), this.testModel.createResource("http://object/1"))
	}
	
	void testCreatingANewStatementWithANonHTTPResource() {
		this.builder.Model(model:this.testModel, prefixes:this.prefixes) {
			Resource("uuid:1234") {
				Predicate("http://predicate/1") {
					Resource("http://object/1")
				}
			}
		}
		assert this.testModel.contains(this.testModel.createResource("uuid:1234"),this.testModel.createProperty("http://predicate/1"), this.testModel.createResource("http://object/1"))
	}
	
	void testCreatingANewResourceWithMultiplePredicates() {
		this.builder.Model(model:this.testModel, prefixes:this.prefixes) {
			Resource("uuid:1234") {
				Predicate("http://predicate/1") {
					Resource("uuid:3456")
				}
				Predicate("http://predicate/2") {
					Resource("uuid:7890")
				}
				Predicate("http://predicate/3") {
					Literal("william")
				}
				Predicate("http://predicate/4") {
					Literal("mr")
				}
			}
		}
		assert this.testModel.contains(this.testModel.createResource("uuid:1234"),this.testModel.createProperty("http://predicate/1"), this.testModel.createResource("uuid:3456"))
		assert this.testModel.contains(this.testModel.createResource("uuid:1234"),this.testModel.createProperty("http://predicate/2"), this.testModel.createResource("uuid:7890"))
		assert this.testModel.contains(this.testModel.createResource("uuid:1234"),this.testModel.createProperty("http://predicate/3"), this.testModel.createLiteral("william"))
		assert this.testModel.contains(this.testModel.createResource("uuid:1234"),this.testModel.createProperty("http://predicate/4"), this.testModel.createLiteral("mr"))
		
	}
	
	void testCreatingANewResourceWithMultipleObjects() {
		this.builder.Model(model:this.testModel, prefixes:this.prefixes) {
			Resource("uuid:1234") {
				Predicate("http://predicate/1") {
					Resource("uuid:3456")
					Resource("uuid:7890")
				}
			}
		}
		assert this.testModel.contains(this.testModel.createResource("uuid:1234"),this.testModel.createProperty("http://predicate/1"), this.testModel.createResource("uuid:3456"))
		assert this.testModel.contains(this.testModel.createResource("uuid:1234"),this.testModel.createProperty("http://predicate/1"), this.testModel.createResource("uuid:7890"))
	}
	
	void testCreatingAModelWithDeepNesting() {
		this.builder.Model(model:this.testModel, prefixes:this.prefixes) {
			Resource("uuid:1234") {
				Predicate("http://predicate/1") {
					Resource("uuid:3456") {
						Predicate("http://predicate/3") {
							Resource("uuid:1111")
						}
					}
				}
				Predicate("http://predicate/2") {
					Resource("uuid:7890") {
						Predicate("http://predicate/4") {
							Resource("uuid:2222")
						}
					}
				}
			}
		}
		assert this.testModel.contains(this.testModel.createResource("uuid:1234"),this.testModel.createProperty("http://predicate/1"), this.testModel.createResource("uuid:3456"))
		assert this.testModel.contains(this.testModel.createResource("uuid:1234"),this.testModel.createProperty("http://predicate/2"), this.testModel.createResource("uuid:7890"))
		assert this.testModel.contains(this.testModel.createResource("uuid:3456"),this.testModel.createProperty("http://predicate/3"), this.testModel.createResource("uuid:1111"))
		assert this.testModel.contains(this.testModel.createResource("uuid:7890"),this.testModel.createProperty("http://predicate/4"), this.testModel.createResource("uuid:2222"))
	}
	
	void testCreatingAModelWithDeepNestingWithPrimedBuilder(){
		this.primedBuilder.Model() {
			Resource("uuid:1234") {
				Predicate("http://predicate/1") {
					Resource("uuid:3456") {
						Predicate("http://predicate/3") {
							Resource("uuid:1111")
						}
					}
				}
				Predicate("http://predicate/2") {
					Resource("uuid:7890") {
						Predicate("http://predicate/4") {
							Resource("uuid:2222")
						}
					}
				}
			}
		}
		assert this.testModel.contains(this.testModel.createResource("uuid:1234"),this.testModel.createProperty("http://predicate/1"), this.testModel.createResource("uuid:3456"))
		assert this.testModel.contains(this.testModel.createResource("uuid:1234"),this.testModel.createProperty("http://predicate/2"), this.testModel.createResource("uuid:7890"))
		assert this.testModel.contains(this.testModel.createResource("uuid:3456"),this.testModel.createProperty("http://predicate/3"), this.testModel.createResource("uuid:1111"))
		assert this.testModel.contains(this.testModel.createResource("uuid:7890"),this.testModel.createProperty("http://predicate/4"), this.testModel.createResource("uuid:2222"))
	}
	
	void testCreatingANewPrefixedModelWithDeepNesting() {
		def newModel = this.prefixedBuilder.Model() {
			Resource("uuid:1234") {
				Predicate("http://predicate/1") {
					Resource("uuid:3456") {
						Predicate("http://predicate/3") {
							Resource("uuid:1111")
						}
					}
				}
				Predicate("http://predicate/2") {
					Resource("uuid:7890") {
						Predicate("http://predicate/4") {
							Resource("uuid:2222")
						}
					}
				}
			}
		}
		assert newModel.contains(newModel.createResource("uuid:1234"),newModel.createProperty("http://predicate/1"), newModel.createResource("uuid:3456"))
		assert newModel.contains(newModel.createResource("uuid:1234"),newModel.createProperty("http://predicate/2"), newModel.createResource("uuid:7890"))
		assert newModel.contains(newModel.createResource("uuid:3456"),newModel.createProperty("http://predicate/3"), newModel.createResource("uuid:1111"))
		assert newModel.contains(newModel.createResource("uuid:7890"),newModel.createProperty("http://predicate/4"), newModel.createResource("uuid:2222"))
	
	}
	
}

