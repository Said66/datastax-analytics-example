package com.datastax.analytics;

import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.solr.util.BoundedTreeSet;

import com.datastax.sampledata.Transaction;

public class Top10TransactionsProcessor implements Runnable{
	
	private BlockingQueue<Transaction> queue;
	private Set<Transaction> orderedSet = new BoundedTreeSet<Transaction>(10, new TransactionAmountComparator());
	
	public Top10TransactionsProcessor(BlockingQueue<Transaction> queue) {
		this.queue = queue;
	}

	@Override
	public void run() {
		while(true){				
			Transaction transaction = queue.poll();
			
			if (transaction!=null){
				orderedSet.add(transaction);				
			}				
		}						
	}

	public Set<Transaction> getResults() {
		return orderedSet;
	}

}
