package org.example;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

public class SampleWrite2SparqlEndpoint {

	public static void main(String[] args) {

		// inserting into local GraphDB, into repository 'twitter-challenge'
		String sparqlEndpoint = "http://localhost:7200/repositories/twitter-challenge/statements";
		Repository repo = new SPARQLRepository(sparqlEndpoint);

		ModelBuilder modelBuilder = new ModelBuilder();

		modelBuilder.setNamespace(RDF.NS);
		modelBuilder.setNamespace("ex", "http://example.org/");

		// a dummy triple
		modelBuilder.subject("ex:Foo")
				.add("ex:test", "blabla");

		Model model = modelBuilder.build();

		try (RepositoryConnection con = repo.getConnection()) {
			con.add(model);
		}
	}

}
