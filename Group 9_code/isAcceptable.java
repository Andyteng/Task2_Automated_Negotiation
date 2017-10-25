private boolean isAcceptable(Bid myBid, double t, int acceptCase) {

		switch (acceptCase) {
		case 1: 
			//Case1: Accept when the opponent’s bid is better than our upcoming bid
			if (getUtility(this.lastPartnerBid)>= getUtility(myBid)) {
				System.out.println("Bid is acceptted as the opponent's bid is better than our upcoming bid.");
				return true;
			}
			break;
		case 2:
			//Case2: Accept when the opponent’s bid is better than alpha
			double alpha = Math.random();
			if(getUtility(this.lastPartnerBid)>=alpha){
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