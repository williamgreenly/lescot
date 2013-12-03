Lescot
======

This project is named after Pierre Lescot. Not too sure why. Its a Groovy library for working with the Semantic Web.

Anyway, take a look at the examples below to get an idea of what this library can do, *but ideally, look at the tests because they are a bit more comprehensive*.

**Create a Model**

`def prefixes = [ex:"http://example.com/"]`

`def model = new GroovyRdfModel(prefixes)`

**Add some statements**

`model.add("ex:Subject ex:predicate ex:Object")`

`model.add("ex:Subject", "ex:predicate", "ex:Object")`

**Ask some questions, find some answers**

`def res = model.sparql("SELECT ?sub WHERE {?sub ex:predicate ?o}")`

`res.each {`

>`    println it.sub`

`}`

`def construct = model.sparql("CONSTRUCT {ex:Subject ?p ?o} WHERE {?s ?p ?o}")`

`construct.each {`

>`    println it.s`

>`    println it.p`

>`    println it.o`

`}`

`assert model.ask("ex:Subject ex:predicate ex:Object")`

**Insert and delete**

`model.update("INSERT DATA {kotg:Test a kotg:Event.}")`

`model.update("DELETE DATA {kotg:Test a kotg:Event.}")`

**Reason and SPIN**

`def inferredModelByOWLReasoning = model.reason()`

`def inferredModelBySPINRules = model.spin()`

**Turtle is the way forward**

`String turtle = model.turtle()`

**Build a RDF Graph**

`def builder = new RdfBuilder(prefixes)`

`builder.Model() {`

> `Resource("ex:Subject") {`

>> `Predicate("ex:predicate") {`

>>>  `Resource("ex:Object")`
  
>>  `}`
				
>> `Predicate("ex:literal") {`
 
>>> `Literal("Lescot")`
 
>> `}`

> `}`

`}`
