package ch.mobi.emme.twitter.sparql.sink;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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

    private static final String RDF_TYPE = "rdf:type";
    private static final String RDF_LABEL = "rdf:label";

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

        final Struct tweet = (Struct) record.value();


        final ModelBuilder modelBuilder = new ModelBuilder();

        modelBuilder.setNamespace(RDF.NS);
        final String namespaceBase = "http://emme.mobi.ch/twitter/";
        modelBuilder.setNamespace("tw", namespaceBase);
        modelBuilder.setNamespace("user", namespaceBase + "user/");
        modelBuilder.setNamespace("tweet", namespaceBase + "tweet/");
        modelBuilder.setNamespace("hashtag", namespaceBase + "hashtag/");


        // hashtags
        final List<Struct> hashtags = tweet.getArray("HashtagEntities");
        if (hashtags != null) {
            for (final Struct hashtag : hashtags) {
                final String hashtagIri = "hashtag:" + hashtag.getString("Text");
                modelBuilder.subject(hashtagIri);
                add(modelBuilder, RDF_TYPE, "tw:Hashtag");
                add(modelBuilder, RDF_LABEL, hashtag.getString("Text"));
            }
        }

        // tweet
        final String tweetIri = "tweet:" + tweet.getInt64("Id");
        modelBuilder.subject(tweetIri);
        add(modelBuilder, RDF_TYPE, "tw:Tweet");
        add(modelBuilder, "tw:text", tweet.getString("Text"));
        add(modelBuilder, "tw:timestamp", ((Date) tweet.get("CreatedAt")).getTime());
        add(modelBuilder, "tw:source", tweet.getString("Source"));
        final Struct geoLocation = tweet.getStruct("GeoLocation");
        if (geoLocation != null) {
            add(modelBuilder, "tw:latitude", geoLocation.getFloat64("Latitude"));
            add(modelBuilder, "tw:longitude", geoLocation.getFloat64("Longitude"));
        }
        final List<Struct> mentions = tweet.getArray("UserMentionEntities");
        if (mentions != null) {
            for (final Struct mention : mentions) {
                modelBuilder.add("tw:mention", "user:" + mention.getInt64("Id"));
            }
        }

        // user
        final Struct user = tweet.getStruct("User");
        final String userIri = "user:" + user.getInt64("Id");
        modelBuilder.subject(userIri);
        add(modelBuilder, RDF_TYPE, "tw:User");
        add(modelBuilder, RDF_LABEL, user.getString("Name"));
        add(modelBuilder, "tw:name", user.getString("Name"));
        add(modelBuilder, "tw:screenname", user.getString("ScreenName"));
        add(modelBuilder, "tw:followerscount", user.getInt32("FollowersCount"));
        add(modelBuilder, "tw:location", user.getString("Location"));
        add(modelBuilder, "tw:tweet", tweetIri);

        return modelBuilder.build();
    }

    private static void add(final ModelBuilder builder, final String predicate, final Object value) {
        if (value != null) {
            builder.add(predicate, value);
        }
    }

}
