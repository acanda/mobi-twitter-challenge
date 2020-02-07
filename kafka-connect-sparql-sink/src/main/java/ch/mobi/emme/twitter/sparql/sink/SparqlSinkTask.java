package ch.mobi.emme.twitter.sparql.sink;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

import ch.mobi.emme.twitter.sparql.util.VersionUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SparqlSinkTask extends SinkTask {

    private RepositoryConnection connection;
    private SPARQLRepository repo;

    @Override
    public void start(final Map<String, String> props) {
        final SparqlSinkConnectorConfig config = new SparqlSinkConnectorConfig(props);
        final String endpoint =
                String.format("%s/repositories/%s/statements", config.getRepositoryHost(), config.getRepositoryName());
        log.info("Repository endpoint: {}", endpoint);
        repo = new SPARQLRepository(endpoint);
        connection = repo.getConnection();
    }

    @Override
    public void put(final Collection<SinkRecord> records) {
        log.debug("Sinking {} twitter status records", records.size());
        try {
            connection.begin();
            records.stream().map(SparqlSinkTask::toStatements).forEach(connection::add);
            connection.commit();
        } catch (final RuntimeException e) {
            connection.rollback();
            throw e;
        }
    }

    @Override
    public void stop() {
        connection.close();
        repo.shutDown();
    }

    @Override
    public String version() {
        return VersionUtil.getVersion();
    }

    private static Iterable<? extends Statement> toStatements(final SinkRecord record) {
        if (record.valueSchema().type() != Schema.Type.STRUCT) {
            log.warn("Expected value schema type to be STRUCT but was {}", record.valueSchema().type());
            return Collections.emptyList();
        }

        final Struct status = (Struct) record.value();


        final ModelBuilder modelBuilder = new ModelBuilder();

        modelBuilder.setNamespace(RDF.NS);
        modelBuilder.setNamespace("ex", "http://example.org/");
        modelBuilder.setNamespace("user", "http://example.org/user/");
        modelBuilder.setNamespace("status", "http://example.org/status/");
        modelBuilder.setNamespace("hashtag", "http://example.org/hashtag/");

        final Struct user = status.getStruct("User");
        final String statusIri = "status:" + status.getInt64("Id");
        final String userIri = "user:" + user.getInt64("Id");

        // user info, user#status
        modelBuilder.subject(userIri)
                .add("rdf:type", "ex:User")
                .add("rdf:label", user.getString("Name"))
                .add("ex:name", user.getString("Name"))
                .add("ex:tweet", statusIri);

        // status
        modelBuilder.subject(statusIri).add("rdf:type", "ex:Tweet").add("ex:text", status.getString("Text"));

        return modelBuilder.build();
    }

}
