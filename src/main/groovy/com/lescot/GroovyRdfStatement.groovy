package com.lescot

import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.shared.PrefixMapping
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.rdf.model.Literal
import com.hp.hpl.jena.rdf.model.Statement
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.rdf.model.impl.StatementImpl
import groovy.util.logging.*
import com.hp.hpl.jena.rdf.model.impl.ModelCom

import java.io.ByteArrayOutputStream
import java.lang.reflect.Method
import groovy.util.logging.Log

@Log
class GroovyRdfStatement extends StatementImpl {

	def s
	def p
	def o

	GroovyRdfStatement (Resource subject, Property predicate, RDFNode object, ModelCom model) {
		super(subject, predicate, object, model)
		
	}

	GroovyRdfStatement (Statement s, ModelCom model) {
		this(s.getSubject(), s.getPredicate(), s.getObject(), model)
	}
		
	public static String nodeToString(RDFNode node, Model m) {
		if (node.isURIResource()) {
			return m.shortForm(node.toString())
		}
		else if (node.isLiteral()) {
			return node.asLiteral().getValue()
		}
		else {
			return node.toString()
		}
	} 
	
	public getS() {
		return nodeToString(this.getSubject(), this.getModel())
	}
	
	public getP() {
		return nodeToString(this.getPredicate(), this.getModel())
	}
	
	public getO() {
		return nodeToString(this.getObject(), this.getModel())
	}
	
	
	
}
