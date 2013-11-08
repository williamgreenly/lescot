package com.lescot

import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.shared.PrefixMapping
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.rdf.model.Literal
import groovy.util.logging.*

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
import org.mindswap.pellet.jena.PelletReasonerFactory
import com.hp.hpl.jena.reasoner.ReasonerRegistry
import com.hp.hpl.jena.rdf.model.InfModel

import groovy.util.logging.*

@Log
class GroovyRdfModel extends ModelCom implements Model {


	
	public GroovyRdfModel(Map prefixes) {
		super(Factory.createDefaultGraph())
		this.setNsPrefixes(prefixes)
	}
	
	public GroovyRdfModel(Model m, Map prefixes) {
		super(Factory.createDefaultGraph())
		this.setNsPrefixes(prefixes)
		this.add(m)
	}
	
	
	public GroovyRdfModel() {
		super(Factory.createDefaultGraph())
	}
	
	public GroovyRdfModel(Model m) {
		super(Factory.createDefaultGraph())
		this.add(m)
		this.setNsPrefixes(m.getNsPrefixMap())
	}
	
	public static sparql (String sparql, Model m) {
		String q = serialisePrefixes(m) + sparql
		log.info q
		Query query = QueryFactory.create(q)
		QueryExecution qx = QueryExecutionFactory.create(query, m)
		
		return checkAndExecuteQuery(qx, query, m)
	}

	public static serialisePrefixes (Model m) {
		String q = ""
		m.getNsPrefixMap().each {
			q += "PREFIX ${it.key}: <${it.value}> \n"
		}
		return q
	}
	
	public static getFullUri(String node, Model rdfmodel) {
		
		return rdfmodel.expandPrefix(node)
		
	}
		
	public static isTurtleUri (String val, Model m) {
		String str = val.trim()
		if ( (str[0] == "<" && str[val.length()-1] == ">"  && str.contains(":")) || isPrefixedUri(val, m) ) {
			return true
		}
		else {
			return false
		}
	}	

	public static isPrefixedUri(String node, Model rdfmodel) {
		if (rdfmodel.getNsPrefixMap().containsKey(node.split(":")[0]) && node.split(":").length == 2) {
			return true
		}
		else {
			return false
		}
	}

	public static getShortForm(RDFNode node, Model rdfModel) {
		return rdfModel.shortForm(node.toString())
	}
	
	public static constructWhere (String pattern, Model m) {
		String spl = "CONSTRUCT {" + pattern + "} \n" + "WHERE {" + pattern + "} \n"
		return sparql(spl, m)
	}
		
	public static add (GroovyRdfStatement stmt, Model m) {
		add(stmt.s, stmt.p, stmt.o, m)
	}
	
	public static add (String s, String p, String o, Model m) {
		if (isTurtleUri(o, m)) {
			m.add(s + " " + p + " " + o + ".", m)
		}
		else {
			m.add(s + " " + p + " \"" + o + "\".", m)
		}
	}

	public static delete(String pattern, Model m) {
		String spl = "DELETE DATA {" + pattern + "}"
		sparql(spl,m)
	}

	public static ask(String pattern, Model m) {
		String spl = "ASK {" + pattern + "}"
		sparql(spl, m)
	}
	
	
	public static add (String turtle, Model m) {
		String pf = ""
		m.getNsPrefixMap().each {
			pf += "@prefix ${it.key}: <${it.value}>. \n"
		}
		pf += turtle
		m.read(new ByteArrayInputStream(pf.getBytes()), null, "TTL")
	}
	
	public static  remove (GroovyRdfStatement stmt, Model m) {
		remove(stmt.s, stmt.p, stmt.o, m)
	}
	
	public static  remove (List stmt, Model m) {
		remove(stmt[0], stmt[1], stmt[2], m)
	}
	
	public static  remove (String s, String p, String o, Model m) {
		//m.remove(createStatement(s, p, o ,m))
	}
	
	public static update (String spl, Model m) {
		UpdateAction.parseExecute(serialisePrefixes(m) + spl, m)
	}
		
	private static checkAndExecuteQuery (QueryExecution qx, Query qy, Model mdl) {
		
		
		if (qy.isConstructType()) {
			log.info "executing construct query"
			Model m = qx.execConstruct()
			m.setNsPrefixes(mdl.getNsPrefixMap())
			return new GroovyRdfModel(m)
		}
		else if (qy.isDescribeType()) {
			log.info "executing describe query"
			Model m =  qx.execDescribe()
			m.setNsPrefixes(mdl.getNsPrefixMap())
			return new GroovyRdfModel(m)
		}
		else if (qy.isAskType()) {
			log.info "executing ask query"
			return qx.execAsk()
		}
		else if (qy.isSelectType()) {
			log.info "executing select query"
			def results = []
			ResultSet r =  qx.execSelect()
			while (r.hasNext()) {
				log.info "creating resultset"
				QuerySolution qs = r.next()
				def row = [:]
				qs.varNames().each {
					def val = qs.get(it)
					if (val.isLiteral()) {
						row[it] = val.asLiteral().getValue()
					}
					if (val.isResource()) {
						
						row[it] = mdl.shortForm(val.asResource().toString())
					}
				}
				results << row
			}
			return results
		}
		else {
			updateWithSparql(qy.serialise(),mdl)
		}
		
	} 
	
	def iterator() {
		def stmts = []
		this.listStatements().each () {
			stmts << new GroovyRdfStatement(it, this)
		}
    	return stmts.iterator()
  	}
	
	void add (GroovyRdfStatement stmt) {
		add (stmt, this)
	}
		
	void add (String turtle) {
		add (turtle, this)
	}
	
	void add (String s, String p, String o) {
		add (s, p, o, this)
	}

	def reason = {it ->
		InfModel inferredModel = ModelFactory.createInfModel(PelletReasonerFactory.theInstance().create(), it)
		return new GroovyRdfModel(inferredModel,this.getNsPrefixMap())
	}
	
	def update (String spl) {
		update (spl, this)
	}

	def sparql (String spl) {
		return sparql (spl, this)
	}
	
	def constructWhere (String pattern) {
		return constructWhere(pattern, this)
	}
	
	void remove (GroovyRdfStatement stmt) {
		remove (stmt, this)
	}
	
	
	void remove (String s, String p, String o) {
		remove(s, p, o, this)
	}

	def turtle () {
		ByteArrayOutputStream b = new ByteArrayOutputStream()
		this.write(b, "TURTLE", null)
		return b.toString()
	}



}

