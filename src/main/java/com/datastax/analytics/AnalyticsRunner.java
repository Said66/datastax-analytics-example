package com.datastax.analytics;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.dao.TransactionsDao;
import com.datastax.demo.utils.PropertyHelper;
import com.datastax.demo.utils.Timer;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.sampledata.Transaction;

public class AnalyticsRunner{

	private static Logger logger = LoggerFactory.getLogger(AnalyticsRunner.class);	
	
	private BlockingQueue<Transaction> queue = new ArrayBlockingQueue<Transaction>(1000);
	private Session session;
	
	public AnalyticsRunner() {
		String contactPointsStr = PropertyHelper.getProperty("contactPoints", "localhost");
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		
		Top10TransactionsProcessor top10 = new Top10TransactionsProcessor(queue);
		executor.execute(top10);
		
		Cluster cluster = Cluster.builder().addContactPoints(contactPointsStr.split(",")).build();		
		this.session = cluster.connect();
		
		TransactionsDao dao = new TransactionsDao(session);
		
		Timer timer = new Timer();
		timer.start();
		
		dao.getAllProducts(queue);		
		Set<Transaction> results = top10.getResults();
		
		for (Transaction trans : results){
			logger.info(trans.toString());
		}
		
		timer.end();
		logger.info("Analytics Runner took : " + timer.getTimeTakenSeconds() + " secs");
		
		session.shutdown();
		cluster.shutdown();
		
		System.exit(0);
	}
	
	public static void main(String[] args) {
		new AnalyticsRunner();		
	}
}
