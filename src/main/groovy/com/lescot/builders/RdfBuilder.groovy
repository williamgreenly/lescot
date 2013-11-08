package com.lescot.builders
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.shared.PrefixMapping
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.rdf.model.Literal
import groovy.util.logging.*
import java.io.ByteArrayOutputStream

@Log
public class RdfBuilder extends BuilderSupport {

	public Model rdfmodel
	Resource subject
	Map defaultPrefixes
	com.hp.hpl.jena.rdf.model.Property predicate
	
	
	
	public RdfBuilder() {
		super()
		
	}
	
	public RdfBuilder(Model rdfmodel) {
		this()
		this.createModel(rdfmodel)
	}
	
	public RdfBuilder(Model rdfmodel, Map nsprefixes) {
		this(rdfmodel)
		this.rdfmodel.setNsPrefixes(nsprefixes)
	}
	
	public RdfBuilder(Map nsprefixes) {
		this.defaultPrefixes = nsprefixes
	}
	
	public void createModel(Model model) {
		this.rdfmodel = model
		if (this.defaultPrefixes != null) {
			this.rdfmodel.withDefaultMappings(ModelFactory.createDefaultModel().setNsPrefixes(this.defaultPrefixes))
		}
	}
	
	public void createModel() {
		this.createModel(ModelFactory.createDefaultModel())
	}
	
	public static String resolveUri(String uri, Model m) {
		if (!m.getNsPrefixMap().containsKey(uri.split(":")[0])) {
			return uri
		}
		else {
			return m.getNsPrefixURI(uri.split(":")[0]) +  uri.split(":")[1]
		}
	}
	
	public void createModel(Map attributes) {
		
		if (attributes.containsKey("model")) {
			this.createModel(attributes['model'])
		}
		else {
			this.createModel()
		}
		if (attributes.containsKey("prefixes")) {
			this.rdfmodel.withDefaultMappings(ModelFactory.createDefaultModel().setNsPrefixes(attributes['prefixes']))
		}
	} 
	
	public String uriResolver (String uri) throws UndeclaredRdfBuilderNamespaceException  {
		if (!this.rdfmodel.getNsPrefixMap().containsKey(uri.split(":")[0])) {
			return uri
		}
		else {
			return this.rdfmodel.getNsPrefixURI(uri.split(":")[0]) +  uri.split(":")[1]
		}
	}
		
	public Resource createResource(String uri) throws RdfBuilderException {
		try {
			return this.rdfmodel.createResource(uriResolver(uri))
		}
		catch (UndeclaredRdfBuilderNamespaceException e) {
			throw e
		}
		catch (Exception e) {
			log.severe("error")
			throw new RdfBuilderException(e)
		}
	} 
	
	
	
	public Resource createResource() {
		return this.rdfmodel.createResource()
	}
	
	public Literal createLiteral(Object literal) {
		return this.rdfmodel.createTypedLiteral(literal)
	}
	
	public com.hp.hpl.jena.rdf.model.Property createProperty(String uri) throws RdfBuilderException {
		try {
			return this.rdfmodel.createProperty(uriResolver(uri))
		}
		catch (UndeclaredRdfBuilderNamespaceException e) {
			log.info ("missing namespace")
			throw e
		}
		catch (Exception e) {
			
			log.info (e.getMessage())
			throw new RdfBuilderException(e)
		}
	}
	
	protected void setParent(Object parent, Object child) {
		
		switch (parent.getClass().getSimpleName()) {
			case null:
				log.info("syntax error with RdfBuilder causing null pointer exception")
				break
			case "ResourceImpl":
				switch (child.getClass().getSimpleName()) {
					case "PropertyImpl":
						this.subject = parent
						this.predicate = child
					break
					default:
						log.info("syntax error with RdfBuilder: " + parent.getClass().getSimpleName() + " -> " + child.getClass().getSimpleName())
					break
				}
			break
			case "LiteralImpl":
				log.info("cannot create literal as a subject" + parent.getClass().getSimpleName() + " -> " + child.getClass().getSimpleName())
				break
			case "PrefixMappingImpl":
			
			break	
			case "ModelCom":
				
			break
			case "PropertyImpl":
				
				switch (child.getClass().getSimpleName()) {
					case "ResourceImpl":
						this.rdfmodel.add(this.rdfmodel.createStatement(this.subject, this.predicate, child))
						break
					case "LiteralImpl":
						this.rdfmodel.add(this.rdfmodel.createStatement(this.subject, this.predicate, child))
					break
					case null:
						log.info("syntax error with RdfBuilder causing null pointer exception")
						break
					default:
						log.info("syntax error with RdfBuilder: " + parent.getClass().getSimpleName() + " -> " + child.getClass().getSimpleName())
						break
				}
				break
			
			default:
			    log.info("syntax error with RdfBuilder - " + parent.getClass().getSimpleName() + " -> " + child.getClass().getSimpleName())
				break
		}
	}
	
	protected Object createNode(Object name) {
		switch (name) {
			case ["Resource", "s","r"]:
				try {
					return this.createResource()
				}
				catch (Exception e) {
					
					log.info("${e.getMessage()}")
				}
			break
			case ["Predicate", "p"]:
				return null
				log.info("can't create anonymous predicates")
				break
			case ["Literal", "l"]:
				return null
				log.info("can't create anonymous literals")
				break
			break
			case "Model":
				if (this.rdfmodel == null) {
					this.createModel()
				}
				return this.rdfmodel
			break
			case "Prefix":
				return null
				log.info("no namespace defined for prefix")
			break
			default:
				log.info("invalid syntax with " + name)
				return null
			break
		}
	}
	
	protected Object createNode(Object name, Object value) {
		
		switch (name) {
			case "Resource":
				if (value != null) {
					try {
						return this.createResource(value)
					}
					catch (Exception e) {
						
						log.info("${e.getMessage()}")
					}
				}
				else {
					try {
						return this.createResource()
					}
					catch (Exception e) {
						
						log.info("${e.getMessage()}")
					}
				}
				break
			case "Predicate":
				return this.createProperty(value)
				break
			case "Literal":
				return this.createLiteral(value)
			break
			
			case "Statement":
			
				break
			
			case "Model":
		
				log.info("can't create a model from ${value.getClass()}")
				return null
				break
			default:
				return value
			break
		}
	}
	
	protected Object createNode(Object name, Map attributes) {
		switch (name) {
			case "Resource":
				log.info("cannot create a resource using a map")
				return null
				break
			case "Predicate":
				log.info("cannot create predicate using a map")
				return null
				break
			case "Model":
				this.createModel(attributes)
				return this.rdfmodel
				break
			case "Literal":
				log.info("cannot create literal using a map")
				return null
				break
			case "Prefix":
				attributes.each {
					return this.rdfmodel.setNsPrefix (it.key, it.value)
				}
				break
			default:
				log.info("${name} not valid RdfBuilder syntax")
				return null
			break
		}
	}
	
	protected Object createNode(Object name, Map attributes, Object value) {
		log.info("${name} with a map and ${value} not valid RdfBuilder syntax")
		return null
	}

}

