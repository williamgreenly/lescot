package com.lescot.transformers

import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.shared.PrefixMapping
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.rdf.model.Literal

import java.io.ByteArrayOutputStream
import java.lang.reflect.Method
import com.hp.hpl.jena.query.Query
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.query.QueryExecution
import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.update.GraphStoreFactory
import com.hp.hpl.jena.update.Update
import com.hp.hpl.jena.update.UpdateExecutionFactory
import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.update.GraphStore
import com.hp.hpl.jena.update.UpdateRequest
import com.hp.hpl.jena.update.UpdateAction
import com.hp.hpl.jena.rdf.model.impl.ModelCom
import com.hp.hpl.jena.graph.Factory
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.reasoner.ReasonerRegistry
import com.hp.hpl.jena.rdf.model.InfModel
import org.topbraid.spin.inference.SPINInferences
import com.lescot.sparql.Datastore
import com.github.jsonldjava.jena.*    
import org.ccil.cowan.tagsoup.Parser
import groovy.util.XmlSlurper
import net.rootdev.javardfa.jena.RDFaReader
import groovy.xml.MarkupBuilder

import groovy.util.logging.*

public class HtmlTransformer {
	
	def input
	def output

	def htmlbuilder

	public OwlToHtmlTransformer {
		htmlbuilder =  new MarkupBuilder()
	}

	void transform() {

	}

	public transformHeader() {
		htmlbuilder.html {
			

		}

	}

}