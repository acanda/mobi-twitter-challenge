package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.PropertyConfiguration;

public class SampleTwitterStatusListener {

	public static void main(String[] args) {
		StatusListener listener = new StatusListener() {
			public void onStatus(Status status) {
				System.out.println(status.getUser().getName() + " : " + status.getText());
			}

			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
			}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			}

			public void onScrubGeo(long userId, long upToStatusId) {
			}

			public void onStallWarning(StallWarning warning) {
			}

			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};

		// adapted from com.github.jcustenborder.kafka.connect.twitter.TwitterSourceTask
		// https://github.com/jcustenborder/kafka-connect-twitter/blob/master/src/main/java/com/github/jcustenborder/kafka/connect/twitter/TwitterSourceTask.java

		Configuration config = configuration();
		TwitterStreamFactory twitterStreamFactory = new TwitterStreamFactory(config);
		TwitterStream twitterStream = twitterStreamFactory.getInstance();

//		FilterQuery filterQuery = new FilterQuery();

//		String[] keywords = new String[] {};
//		filterQuery.track(keywords);

//	    long[] userIds =
//	    filterQuery.follow(userIds);

		twitterStream.addListener(listener);
//		twitterStream.filter(filterQuery);

		// sample() method internally creates a thread which manipulates TwitterStream
		// and calls these adequate listener methods continuously.
		twitterStream.sample();
	}

	private static Configuration configuration() {
		Properties properties = new Properties();

		String propertiesFile = "/twitter-auth.properties";
		try {
			InputStream is = SampleTwitterStatusListener.class.getResourceAsStream(propertiesFile);
			if (is != null) {
				properties.load(is);
			} else {
				String msg = String.format("%s not found", propertiesFile);
				throw new RuntimeException(msg);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		return new PropertyConfiguration(properties);
	}

}
