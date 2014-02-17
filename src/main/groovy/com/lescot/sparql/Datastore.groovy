package com.lescot.sparql

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
import org.apache.jena.web.DatasetGraphAccessorHTTP
import org.apache.jena.web.DatasetAdapter
import com.lescot.GroovyRdfModel

class Datastore {
	
	def uri
	def adapter
	def graphStoreUri
	def sparqlEndpoint

	public Datastore(uri) {
		this.uri = uri
		this.graphStoreUri = uri + "/data"
		this.sparqlEndpoint = uri + "/sparql"
		adapter = new DatasetAdapter(new DatasetGraphAccessorHTTP(graphStoreUri))
	}

	public void put (Model m, String uri) {
		adapter.putModel(uri, m)
	}

	public void delete(String uri) {
		adapter.deleteModel(uri)
	}

	public void post(Model m, String uri) {
		adapter.add(uri, m)
	}

	public Model get(String uri) {
		return adapter.getModel(uri)
	}

	public void put (Model m) {
		adapter.putDefault(m)
	}

	public void delete(Model m) {
		adapter.deleteDefault()
	}

	public void post(Model m) {
		adapter.add(m)
	}

	public Model get() {
		return adapter.getModel()
	}

	

}