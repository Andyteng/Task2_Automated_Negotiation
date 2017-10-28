package ai2017;

import java.util.Random;

import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.bidding.BidDetails;
import negotiator.utility.EvaluatorDiscrete;

public class Opponent {
	BidHistory bidHistory;
	
	//issue params
	Integer issue_num;
	int[] issue_ID;
	EvaluatorDiscrete[] issues;

	public void addBid(Bid bid) {
		this.bidHistory.add(new BidDetails(bid, 0));
	}
	
	public Opponent(Bid OneBid) {
		this.bidHistory = new BidHistory();
		this.issue_num = OneBid.getIssues().size();
		this.issue_ID = new int[this.issue_num];

		// Assign the issue IDs
		for (int i = 0; i < OneBid.getIssues().size(); i++) {
			this.issue_ID[i] = OneBid.getIssues().get(i).getNumber();
		}

		// Create the evaluators for each issue
		this.issues = new EvaluatorDiscrete[this.issue_num];
		for (int i = 0; i < this.issue_num; i++) {
			this.issues[i] = new EvaluatorDiscrete();
		}
	}
	
	//Compute opponent's estimated utility
	//TODO: Rand to Real
	public double computeUtility(Bid bid) {
		Random rand=new Random();
		double utility = rand.nextDouble();
		return  utility;
	} 
}
