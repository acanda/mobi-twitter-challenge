package org.example;

import java.util.Arrays;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;

public class Twitter2Sparql {

	public static void main(String[] args) {

		// inserting into local GraphDB, into repository 'twitter-challenge'
		String sparqlEndpoint = "http://localhost:7200/repositories/twitter-challenge/statements";
		Repository repo = new SPARQLRepository(sparqlEndpoint);

		Configuration config = SampleTwitterStatusListener.configuration();
		TwitterStreamFactory twitterStreamFactory = new TwitterStreamFactory(config);
		TwitterStream twitterStream = twitterStreamFactory.getInstance();

		StatusListener listener = createListener(repo);
		twitterStream.addListener(listener);

		// sample() method internally creates a thread which manipulates TwitterStream
		// and calls these adequate listener methods continuously.
		twitterStream.sample();
	}

	private static StatusListener createListener(Repository repo) {
		return new StatusAdapter() {
			public void onStatus(Status status) {
				System.out.println("--------------");
				System.out.println(status.getUser().getName());
				System.out.println(status.getText());
				System.out.println(Arrays.toString(status.getHashtagEntities()));
				System.out.println("--------------");

				Model triples = status2triples(status);
				try (RepositoryConnection con = repo.getConnection()) {
					con.add(triples);
				}
			}
		};
	}

	private static Model status2triples(Status status) {
		ModelBuilder modelBuilder = new ModelBuilder();

		modelBuilder.setNamespace(RDF.NS);
		modelBuilder.setNamespace("ex", "http://example.org/");
		modelBuilder.setNamespace("user", "http://example.org/user/");
		modelBuilder.setNamespace("status", "http://example.org/status/");
		modelBuilder.setNamespace("hashtag", "http://example.org/hashtag/");

		final String userIri = "user:" + status.getUser().getId();
		final String statusIri = "status:" + status.getId();

		// user info, user#status
		modelBuilder.subject(userIri)
				.add("rdf:type", "ex:User")
				.add("rdfs:label", status.getUser().getName())
				.add("ex:name", status.getUser().getName())
				.add("ex:tweet", statusIri);

		// status
		modelBuilder.subject(statusIri)
				.add("rdf:type", "ex:Tweet")
				.add("ex:text", status.getText());

		Model model = modelBuilder.build();
		return model;
	}

}
