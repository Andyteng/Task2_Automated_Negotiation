package ai2017;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;


/**
 * This is your negotiation party.
 */
public class Group9 extends AbstractNegotiationParty {
	private Action opponentAction = null;
	private Bid lastOpponentBid = null;
	private SortedOutcomeSpace outcome;
	private double minUtility = 0.2;
	private double maxTime = 0.2D;
	private Random randomGenerator;
	private List<BidDetails> generatedBestBids = new ArrayList<BidDetails>();
	private int maxAmountSavedBids = 50;
	private HashMap<AgentID, Opponent> opponentHistory;
	@Override
	public void init(NegotiationInfo info) {

		super.init(info);
		this.outcome = new SortedOutcomeSpace(this.utilitySpace);
		System.out.println("Discount Factor is " + info.getUtilitySpace().getDiscountFactor());
		System.out.println("Reservation Value is " + info.getUtilitySpace().getReservationValueUndiscounted());

		// if you need to initialize some variables, please initialize them
		// below

	}

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		if(getTimeLine().getTime()<maxTime){
			Bid maxBid = null;
			try{
				maxBid = this.utilitySpace.getMaxUtilityBid();
			} catch(Exception e) {
				System.out.println("Cannot generate max utility bid");
			}
			System.out.print("provide maximum Utility bid");
			return new Offer(getPartyId(), maxBid);
		}
		
		System.out.format("Last received opponent bid had utility of [%f] for me%n", getUtility(this.lastOpponentBid));
		Bid thisBid=null;
		try {
			thisBid = generateBid();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(isAcceptable(thisBid, getTimeLine().getTime(),1)){
			System.out.print("accept the offer");
			return new Accept(getPartyId(),lastOpponentBid);
		}
		System.out.format("offer my own generated bid with Utility [%f]%n", getUtility(thisBid));
		return new Offer(getPartyId(),thisBid);
	}
	
	private Bid generateBid() throws Exception{
		double nashProduct,bestNash = 0;
		Bid bestBid=null;
		for(int i = 0; i < maxAmountSavedBids; i++){
			Bid randomBid = generateRandomBid(minUtility);
			nashProduct = getNashProduct(randomBid);
			if(nashProduct > bestNash){
				bestNash = nashProduct;
				bestBid = randomBid;
			}
		}
		if (this.generatedBestBids.size() < maxAmountSavedBids){
			this.generatedBestBids.add(new BidDetails(bestBid, bestNash));
			// If the list gets full sort it
			if (this.generatedBestBids.size() == this.maxAmountSavedBids){
				this.sortBids();
			}
		}
		else {
			// Get the worst bid saved, replace it by the new best bid
			double worstBidsUtility = this.generatedBestBids.get(this.maxAmountSavedBids -1).getMyUndiscountedUtil();
			if (bestNash > worstBidsUtility){
				this.generatedBestBids.remove(0);
				this.generatedBestBids.add(new BidDetails(bestBid, bestNash));
				this.sortBids();
			}
		}
		
		// When reach maximum size, offer one of the best bids
		if (this.generatedBestBids.size() >= this.maxAmountSavedBids){
			int index =  this.maxAmountSavedBids - randomGenerator.nextInt(3) - 1;
			bestBid = this.generatedBestBids.get(index).getBid();		
		}
		return bestBid;
	}
	
	private Bid generateRandomBid(double minUtility) {
		Bid bid=null;	
	
		do {
			// Generate random double in range 0:1
		    double randomNumber = this.randomGenerator.nextDouble();
		    // Map randomNumber in range (minimum acceptable utility : 1)
		    double utility = minUtility + randomNumber * (1.0 - minUtility);
		    // Get a bid closest to $utility
		    bid = outcome.getBidNearUtility(utility).getBid();
		} while (getUtility(bid) <= minUtility);
		return bid;
	}
	
	private double getNashProduct(Bid bid)
	{
		double nash = this.getUtility(bid);
		for (AgentID agent : this.opponentHistory.keySet()) {
			nash *= this.opponentHistory.get(agent).computeUtility(bid);
		}
		return nash;
	}
	
	private void sortBids(){
		Collections.sort(this.generatedBestBids, new Comparator<BidDetails>() {
		    @Override
		    public int compare(BidDetails bid1, BidDetails bid2) {
		    	if (bid1.getMyUndiscountedUtil() < bid2.getMyUndiscountedUtil()) return -1;
		    	else if (bid1.getMyUndiscountedUtil() == bid2.getMyUndiscountedUtil()) return 0;
		    	else return 1;
		    }
		});
	}
	
	private boolean isAcceptable(Bid myBid, double t, int acceptCase) {

		switch (acceptCase) {
		case 1: 
			//Case1: Accept when the opponent¡¯s bid is better than our upcoming bid
			if (getUtility(this.lastOpponentBid)>= getUtility(myBid)) {
				System.out.println("Bid is acceptted as the opponent's bid is better than our upcoming bid.");
				return true;
			}
			break;
		case 2:
			//Case2: Accept when the opponent¡¯s bid is better than alpha
			double alpha = Math.random();
			if(getUtility(this.lastOpponentBid)>=alpha){
				System.out.println("Bid is accepted as the opponent's bid is better than alpha");
				return true;
			}
			break;
		case 3:
			//case3: Accept when time period T belongs [0,1] has passed
			if (t>1) {
				System.out.println("Bid is accepted as the time is up.");
				return true;
			}
		}
		System.out.println("The bid is not accepted");
		return false;
	}
	
	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict
	 * their utility.
	 *
	 * @param sender
	 *            The party that did the action. Can be null.
	 * @param action
	 *            The action that party did.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		opponentAction = action;
		
		this.lastOpponentBid = ((Offer) action).getBid();
		
		if (sender != null && action instanceof Offer) {
			// Store the bid as the latest received bid
			this.lastOpponentBid = ((Offer) action).getBid();

			// Store the bid in the opponent's history
			if (opponentHistory.containsKey(sender)) {
				opponentHistory.get(sender).addBid(this.lastOpponentBid);
			} else {
				// If it's the first time we see this opponent, create a new
				// entry in the opponent map
				try {
					Opponent opponentBid = new Opponent(generateRandomBid(minUtility));
					opponentBid.addBid(this.lastOpponentBid);
					opponentHistory.put(sender, opponentBid);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "example party group 9";
	}

}



