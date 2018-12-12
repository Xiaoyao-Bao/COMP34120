package AgentWithIDDFSThreads;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

class SearchThread extends Thread {
	private SearchThread t;
	private String name;

	private int bestHeuristics;
	private int alpha;
	private int beta;
	private boolean ourTurn;
	private int[] temp;
	private boolean timeElapsed;
	private long startTime;
	private long time;
	private int firstMove;
	private Board board;
	private static Side side;
	private Kalah kalah;
	private int depth;
	private boolean gameOverReached;

	SearchThread(String n, int move, Board b, Side s, Kalah k) {
		name = n;
		firstMove = move;
		board = b;
		alpha = Integer.MIN_VALUE;
		beta = Integer.MAX_VALUE;
		bestHeuristics = Integer.MIN_VALUE;
		ourTurn = false;
		timeElapsed = false;
		side = s;
		kalah = k;
		depth = 0;
		gameOverReached = false;
		startTime = System.currentTimeMillis();
		t = this;
		this.start();
	}

	@Override
	public void run() {
		while(!timeElapsed) {
			for(depth=1; ; depth++) {
				//System.err.println(name+" board now: "+board.toString());
				Move move = new Move(side, firstMove);
				Board lastBoard = board;
				try  {
					lastBoard = board.clone();	
				}
				catch(CloneNotSupportedException e){
					System.err.println(e.getMessage());
					 }

				Side nextTurn = Kalah.makeMove(board, move);
				// if(name.equals("T7"))
				// 	System.err.println(name+" depth: "+depth+" board now: "+board.toString());
				//Check whose turn it is next
				if(nextTurn == side)
					ourTurn = true;
				else
					ourTurn = false;
				if(Agent.first && Agent.moveCount ==1) {
					ourTurn = false;
				}
				gameOverReached = false;
				int[] results = minimax(depth, board, ourTurn, alpha, beta, t);
				int heuristics = results[0];
				// if(name.equals("T7"))
				// 	System.err.println(name+" depth: "+depth+" board after minimax: "+board.toString());
				board = lastBoard;
				// if(name.equals("T7"))
				// 	System.err.println(name+" depth: "+depth+" board after lastBoard: "+board.toString());
				//Check if this move is better than previous best one
				if (heuristics > bestHeuristics) {
					bestHeuristics = heuristics;
				}
				
				if((System.currentTimeMillis() - startTime > (Agent.timeToSearch)) || (Agent.maySwap && System.currentTimeMillis() - startTime > (Agent.timeToSearch/2))){
					System.err.println(name+ " time elapsed, depth reached: " + depth);
					timeElapsed = true;
					break;
				}
				if(gameOverReached) {
					System.err.println(name+ " game over reached, depth reached: " + depth);
					timeElapsed = true;
					break;
				}
			}
		}
	}

	// private int evaluate(Board b)
	// {
	// 	int ourSeeds = b.getSeedsInStore(Agent.side);
	// 	int oppSeeds = b.getSeedsInStore(Agent.side.opposite());

	// 	for (int i = 1; i <= Agent.holes; i++)
	// 	{
	// 		ourSeeds += b.getSeeds(Agent.side, i);
	// 		oppSeeds += b.getSeeds(Agent.side.opposite(), i);
	// 	}

	// 	return ourSeeds - oppSeeds;
	// }

	public static int defendSeeds(Board board, Side ourSide)
  {

      int amountStealable = 0;
      int pos1 = 0, pos2 = 0, pos3 = 0;
      // find opponent pots where he has 0 seeds, but on the other side of the board we have seeds
      List<Integer> candidatePots = new LinkedList<Integer>();

      int pos = 0;
      for(int i=1; i<=board.getNoOfHoles(); i++)
      {
          // pos < 7 to ensure that we're not looking at a scoring well
          if (pos < board.getNoOfHoles() &&
                  board.getSeeds(ourSide.opposite(), i) == 0 && board.getSeeds(ourSide, 7-pos) != 0)
              candidatePots.add(pos);
          pos++;
      }

      // find how many can be stolen by sowing from a pot and dropping the last stone in the same pot
      pos = 0;
      for (int i=1; i<=board.getNoOfHoles(); i++)
          if(board.getSeeds(ourSide.opposite(), i) == 2 * board.getNoOfHoles() + 1)
          {   
            pos1 += board.getSeeds(ourSide, 7-pos++) + 1;
            break;
          }

      // find how many of those pots can be reached by the opponent on his next turn
      for(int index : candidatePots)
      {
          // by sowing from pot with index smaller than empty pot
          pos = 0;
          for(int i=1; i<=board.getNoOfHoles(); i++)
              if (/*pos < index &&*/ board.getSeeds(ourSide.opposite(), i) == index - pos++ && board.getSeeds(ourSide.opposite(), i) != 0)
                  if(pos2 < board.getSeeds(ourSide, 7-index))
                    pos2 = board.getSeeds(ourSide, 7-index);

          // by sowing from pot with index greater than empty pot
          pos = 0;
          for(int i=1; i<=board.getNoOfHoles(); i++)
              if(/*pos > index && */board.getSeeds(ourSide.opposite(), i) == 2 * board.getNoOfHoles() + 1 - (pos++ - index) )
              {
                if(pos3 <  board.getSeeds(ourSide, 7-index) + 1)
                  pos3 =  board.getSeeds(ourSide, 7-index) + 1;
              }
      }

      amountStealable = Math.max(pos1, Math.max(pos2, pos3));
      return amountStealable;
  }

	public static int scoringWellDiff(Board board, Side ourSide) {
    return board.getSeedsInStore(ourSide) - board.getSeedsInStore(ourSide.opposite());
  }

	public static int clusterTowardsScoringWell(Board board, Side side) {
        //Grid grid = board.getPlayersGrid(side);

        int n = 0;
        for (int i = 1; i <= board.getNoOfHoles(); ++i) {
            //Pot pot = grid.getPots()[i];
            if (board.getSeeds(side, i) == 0) continue;
            n += board.getSeeds(side, i) * i;
        }
        return n;
    }

	public static int evaluateLeafNodes(Board b) 
	{
		int w1 = 3;
  		int w2 = 5;
 		int w3 = 20;

		int evaluation =  scoringWellDiff(b, side) * w1
                          - defendSeeds(b, side)   / w2
                           + clusterTowardsScoringWell(b, side) / w3;
        return evaluation;
    }



	//Params: the depth of the search, the current board, whose turn it is, alpha, beta
	//Return: int array containing max/minEval, alpha, beta
	//TO DO: For alpha-beta pruning, add alpha and beta arguments of type int
	private int[] minimax(int depth, Board board, boolean maximizingPlayer, int alpha, int beta, SearchThread t) {
		//Search to the given depth or leaf node reached
		if (depth == 0 || kalah.gameOver(board)) {
			if(kalah.gameOver(board))
			{	
				// if(t.name.equals("T7")) {
				// 	System.err.println("depth: "+depth);
				// 	System.err.println(board.toString());
				// }
				t.gameOverReached = true;
			}
			int v = evaluateLeafNodes(board);
			return new int[] {v, alpha, beta};
		}

		if (maximizingPlayer) {

			int maxEval = Integer.MIN_VALUE;
			for(int i = 1; i <= board.getNoOfHoles(); i++) {

				Move move = new Move(side,i);

				if (kalah.isLegalMove(board, move)) {

					//Clone the current state of the board
					Board lastBoard = board;
					try  {
						lastBoard = board.clone();	
					}
					catch(CloneNotSupportedException e){
						System.err.println(e.getMessage());
						 }

					Side nextTurn = Kalah.makeMove(board, move);
					if(nextTurn == side)
						maximizingPlayer = true;
					else
						maximizingPlayer = false;

					int eval = minimax(depth-1, board, maximizingPlayer, alpha, beta, t)[0];
					//Undo the last move
					board = lastBoard;

					maxEval = Math.max(maxEval, eval);
					alpha = Math.max(alpha, eval);
					if (beta <= alpha) {
						break;
					}
					
				}
				if((System.currentTimeMillis() - startTime > (Agent.timeToSearch)) || (Agent.maySwap && System.currentTimeMillis() - startTime > (Agent.timeToSearch/2))){
						timeElapsed = true;
						break;
					}
			}
			return new int[] {maxEval, alpha, beta};
		}
		
		else {
			int minEval = Integer.MAX_VALUE;

			for(int i = 1; i <= board.getNoOfHoles(); i++) {

				Move move = new Move(side.opposite(),i);

				if (kalah.isLegalMove(board, move)) {
					//Clone the current state of the board
					Board lastBoard = board;
					try  {
						lastBoard = board.clone();	
					}
					catch(CloneNotSupportedException e){
						System.err.println(e.getMessage());
						 }
					Side nextTurn = Kalah.makeMove(board, move);
					if(nextTurn == side)
						maximizingPlayer = true;
					else
						maximizingPlayer = false;
					
					int eval = minimax(depth-1, board, maximizingPlayer, alpha, beta, t)[0];
					//Undo the last move
					board = lastBoard;
					minEval = Math.min(minEval, eval);
					beta = Math.min(beta, eval);

					if (beta <= alpha) {
						break;
					}
				}
				if((System.currentTimeMillis() - startTime > (Agent.timeToSearch)) || (Agent.maySwap && System.currentTimeMillis() - startTime > (Agent.timeToSearch/2))){
						timeElapsed = true;
						break;
					}
			}
			return new int[] {minEval, alpha, beta};
		}
}

	
	public String getThreadName() {
		return name;
	}

	public int getBestHeuristics() {
		return bestHeuristics;
	}

	public int getFirstMove() {
		return firstMove;
	}
}


public class Agent
{
	Side side;

	Kalah kalah;

	int holes;

	static int moveCount = 0;

	static boolean first;

	static boolean maySwap;

	long startTime;

	boolean timeElapsed;

	static long timeToSearch;


	public Agent(int holes, int seeds, int time)
	{
		this.holes = holes;
		kalah = new Kalah(new Board(holes, seeds));
		timeToSearch = time;
	}

	public Agent(int holes, int seeds)
	{
		this.holes = holes;
		kalah = new Kalah(new Board(holes, seeds));
	}

	//EXAMPLE OF EVALUATION FUNCTION, COPIED FROM THE REFAGENT
	//TO DO: CREATE OUR OWN EVALUATION FUNCTION USING THE HEURISTICS
	private int evaluate(Board b)
	{
		int ourSeeds = b.getSeedsInStore(side);
		int oppSeeds = b.getSeedsInStore(side.opposite());

		for (int i = 1; i <= holes; i++)
		{
			ourSeeds += b.getSeeds(side, i);
			oppSeeds += b.getSeeds(side.opposite(), i);
		}

		return ourSeeds - oppSeeds;
	}

	//Params: the depth of the search, the current board, whose turn it is, alpha, beta
	//Return: int array containing max/minEval, alpha, beta
	//TO DO: For alpha-beta pruning, add alpha and beta arguments of type int
	//private int[] minimax(int height, Board board, boolean maximizingPlayer, int alpha, int beta) {
	// 	//Search to the given depth or leaf node reached
	// 	if (height == 0 || kalah.gameOver(board)) {
	// 		int v = evaluate(board);
	// 	//	System.err.println("Evaluated value: " + v);
	// 		return new int[] {v, alpha, beta};
	// 	}

	// 	if (maximizingPlayer) {

	// 		int maxEval = Integer.MIN_VALUE;
	// 		for(int i = 1; i <= holes; i++) {

	// 			Move move = new Move(side,i);

	// 			if (kalah.isLegalMove(board, move)) {
	// 				//Board b = new Board(kalah.getBoard());
	// 			//	System.err.println("Board before max move: " + board);
	// 				//Clone the current state of the board
	// 				Board lastBoard = board;
	// 				try  {
	// 					lastBoard = board.clone();
	// 				}
	// 				catch(CloneNotSupportedException e){
	// 					System.err.println(e.getMessage());
	// 					 }

	// 				Side nextTurn = Kalah.makeMove(board, move);
	// 				if(nextTurn == side)
	// 					maximizingPlayer = true;
	// 				else
	// 					maximizingPlayer = false;
	// 				//System.err.println("Board after max move: " + board);
	// 				//System.err.println("Is it our turn now? " + maximizingPlayer);

	// 				int[] tempEval = new int[3];

	// 				if(tTable.containsKey(board.hashCode())) {
	// 					TTableEntry entry = tTable.get(board.hashCode());

	// 					if(height < entry.getDepth())
	// 						tempEval = entry.getMinimaxResult();
	// 					else
	// 						tempEval = minimax(height-1, board, maximizingPlayer, alpha, beta);
	// 				}
	// 				else
	// 					tempEval = minimax(height-1, board, maximizingPlayer, alpha, beta);

	// 				if(!tTable.containsKey(board.hashCode())) {
	// 					tTable.put(board.hashCode(), new TTableEntry(tempEval, height));
	// 				}
	// 				else {
	// 					TTableEntry entry = tTable.get(board.hashCode());

	// 					if(height < entry.getDepth())
	// 						tTable.put(board.hashCode(), new TTableEntry(tempEval, height));
	// 				}

	// 				int eval = tempEval[0];
	// 				//Undo the last move
	// 				board = lastBoard;

	// 				maxEval = Math.max(maxEval, eval);
	// 				alpha = Math.max(alpha, eval);
	// 			//	System.err.println("alpha: " + alpha);
	// 				if (beta <= alpha) {
	// 			//		System.err.println("Pruning, alpha: " + alpha + ", beta: " + beta);
	// 					break;
	// 				}

	// 			}
	// 			if((System.currentTimeMillis() - startTime > 15000) || (maySwap && System.currentTimeMillis() - startTime > 2500)){
	// 					timeElapsed = true;
	// 					break;
	// 				}
	// 		}
	// 		return new int[] {maxEval, alpha, beta};
	// 	}

	// 	else {
	// 		int minEval = Integer.MAX_VALUE;

	// 		for(int i = 1; i <= holes; i++) {

	// 			Move move = new Move(side.opposite(),i);

	// 			if (kalah.isLegalMove(board, move)) {
	// 			//	System.err.println("Board before min move: " + board);
	// 				//Clone the current state of the board
	// 				Board lastBoard = board;
	// 				try  {
	// 					lastBoard = board.clone();
	// 				}
	// 				catch(CloneNotSupportedException e){
	// 					System.err.println(e.getMessage());
	// 					 }
	// 				Side nextTurn = Kalah.makeMove(board, move);
	// 				if(nextTurn == side)
	// 					maximizingPlayer = true;
	// 				else
	// 					maximizingPlayer = false;
	// 				//System.err.println("Board after min move: " + board);
	// 				//System.err.println("Is it our turn now? " + maximizingPlayer);


	// 				int[] tempEval = new int[4];

	// 				if(tTable.containsKey(board.hashCode())) {
	// 					TTableEntry entry = tTable.get(board.hashCode());

	// 					if(height < entry.getDepth())
	// 						tempEval = entry.getMinimaxResult();
	// 					else
	// 						tempEval = minimax(height-1, board, maximizingPlayer, alpha, beta);
	// 				}
	// 				else
	// 					tempEval = minimax(height-1, board, maximizingPlayer, alpha, beta);

	// 				if(!tTable.containsKey(board.hashCode())) {
	// 					tTable.put(board.hashCode(), new TTableEntry(tempEval, height));
	// 				}
	// 				else {
	// 					TTableEntry entry = tTable.get(board.hashCode());

	// 					if(height < entry.getDepth())
	// 						tTable.put(board.hashCode(), new TTableEntry(tempEval, height));
	// 				}

	// 				int eval = tempEval[0];

	// 				//Undo the last move
	// 				board = lastBoard;
	// 				minEval = Math.min(minEval, eval);

	// 				beta = Math.min(beta, eval);
	// 			//	System.err.println("beta: " + beta);
	// 				if (beta <= alpha) {
	// 			//		System.err.println("Pruning, alpha: " + alpha + ", beta: " + beta);
	// 					break;
	// 				}
	// 			}
	// 			if((System.currentTimeMillis() - startTime > 15000) || (maySwap && System.currentTimeMillis() - startTime > 2500)){
	// 					timeElapsed = true;
	// 					break;
	// 				}
	// 		}
	// 		return new int[] {minEval, alpha, beta};
	// 	}
	// }

	private int IDDFS()
	{
		int bestMove = 0;
		int bestHeuristics = Integer.MIN_VALUE;

		SearchThread[] threads = new SearchThread[7];
		int arrayIndex = 0;
		boolean threadsAlive = true;
		Board b = kalah.getBoard(); 
		System.err.println("Board in IDDFS: " + b.toString());
		// try  {
		// 	b = b.clone();
		// }
		// catch(CloneNotSupportedException e){
		// 	System.err.println(e.getMessage());
		//  }
		for(int i=1; i<=holes; i++) {

			Move move = new Move(side,i);

			if (kalah.isLegalMove(move)) {
				try {
					SearchThread t = new SearchThread("T"+i, i, b.clone(), side, kalah);
					threads[arrayIndex] = t;
					arrayIndex++;
				}
				catch(CloneNotSupportedException e) {
					System.err.println(e.getMessage());
				}
			}
		}
		try{
			for(SearchThread t : threads) {
				if(t != null)
					t.join();
			}	
		}
		catch(InterruptedException e) {
			System.err.println(e.getMessage());
		}
		

		// while(threadsAlive) {
		// 	for(int i=0; i<threads.length; i++) {
		// 		if(threads[i] != null) {
		// 			if(threads[i].isAlive()) {
		// 				threadsAlive = true;
		// 				break;
		// 			}
		// 			else
		// 				threadsAlive = false;
		// 		}
		// 	}
		// }

		for(int i=0; i<threads.length; i++) {
			if(threads[i] != null) {
				System.err.println("Thread name: " + threads[i].getThreadName());			
				System.err.println("Best heuristics: " +threads[i].getBestHeuristics());
				if(bestHeuristics < threads[i].getBestHeuristics()) {
					bestHeuristics = threads[i].getBestHeuristics();
					bestMove = threads[i].getFirstMove();
				}
			}
		}
		System.err.println("Best move to play: " + bestMove);

		return bestMove;
	}

	//TO DO: MODIFY THE METHOD SO THAT IT WOULD CHOOSE THE BEST NEW MOVE FIRST
	//BY CALLING EVALUATION FUNCTION ON ALL POSSIBLE MOVES FIRST AND THEN
	//SORT THEM AND CALL THE MINIMAX IN THE SORTED ORDER
	//Finds the next best move to make
	// private int nextMove()
	// {
	// 	int bestMove = 0;
	// 	int bestHeuristics = Integer.MIN_VALUE;
	// 	int alpha = Integer.MIN_VALUE;
	// 	int beta = Integer.MAX_VALUE;
	// 	boolean ourTurn = false;

	// 	//Go through all the possible moves
	// 	for (int i = 1; i <= holes; i++) {

	// 		Move move = new Move(side,i);

	// 		if (kalah.isLegalMove(move)) {

	// 			Board board = new Board(kalah.getBoard());
	// 			Side nextTurn = Kalah.makeMove(board, move);
	// 			//Check whose turn it is next
	// 			if(nextTurn == side)
	// 				ourTurn = true;
	// 			else
	// 				ourTurn = false;
	// 			//System.err.printf("Board after agent move %d: ",i);
	// 			System.err.println(board);
	// 			//System.err.println("Is it our turn now? " + ourTurn);

	// 			int[] results = minimax(3, board,ourTurn, alpha, beta);
	// 			int heuristics = results[0];
	// 			alpha = results[1];
	// 			alpha = Math.max(alpha, heuristics);

	// 			//Check if this move is better than previous best one
	// 			if (heuristics > bestHeuristics) {
	// 				bestMove = i;
	// 				bestHeuristics = heuristics;
	// 			}
	// 		}
	// 	}
	// 	return bestMove;
	// }

	//Checks whether to perform a swap or a normal move
	//Returns -1 if swap, else number of the best move
	private int toSwap()
	{
		System.err.println("Checking whether to swap");
		int bestMove = 0;
		int swapEvaluation = Integer.MIN_VALUE;
		int noSwapEvaluation = Integer.MIN_VALUE;

		SearchThread[] threads = new SearchThread[7];
		int arrayIndex = 0;
		boolean threadsAlive = true;
		Board b = kalah.getBoard(); 
		
		System.err.println("Checking evaluation for no swapping " + b.toString());
		try  {
			b = b.clone();
		}
		catch(CloneNotSupportedException e){
			System.err.println(e.getMessage());
		 }
		
		for (int i = 1; i <= holes; i++) {

			Move move = new Move(side,i);

			if (kalah.isLegalMove(move)) {
				try {
					SearchThread t = new SearchThread("T"+i, i, b.clone(), side, kalah);
					threads[arrayIndex] = t;
					arrayIndex++;
				}
				catch(CloneNotSupportedException e) {
					System.err.println(e.getMessage());
				}
			}
		}

		try{
			for(SearchThread t : threads) {
				if(t != null)
					t.join();
			}	
		}
		catch(InterruptedException e) {
			System.err.println(e.getMessage());
		}

		for(int i=0; i<threads.length; i++) {
			if(threads[i] != null) {
				System.err.println("Thread name: " + threads[i].getThreadName());			
				System.err.println("Best heuristics: " +threads[i].getBestHeuristics());
				if(noSwapEvaluation < threads[i].getBestHeuristics()) {
					noSwapEvaluation = threads[i].getBestHeuristics();
					bestMove = threads[i].getFirstMove();
				}
			}
		}
		System.err.println("Best move to play: " + bestMove);

		swap();
		arrayIndex = 0;
		threadsAlive = true;
		System.err.println("Checking evaluation for swapping " + kalah.getBoard());
		try  {
			b = b.clone();
		}
		catch(CloneNotSupportedException e){
			System.err.println(e.getMessage());
		 }
		
		for (int i = 1; i <= holes; i++) {

			Move move = new Move(side,i);

			if (kalah.isLegalMove(move)) {
				try {
					SearchThread t = new SearchThread("T"+i, i, b.clone(), side, kalah);
					threads[arrayIndex] = t;
					arrayIndex++;
				}
				catch(CloneNotSupportedException e) {
					System.err.println(e.getMessage());
				}
			}
		}
		try{
			for(SearchThread t : threads) {
				if(t != null)
					t.join();
			}	
		}
		catch(InterruptedException e) {
			System.err.println(e.getMessage());
		}

		for(int i=0; i<threads.length; i++) {
			if(threads[i] != null) {
				System.err.println("Thread name: " + threads[i].getThreadName());			
				System.err.println("Best heuristics: " +threads[i].getBestHeuristics());
				if(noSwapEvaluation < threads[i].getBestHeuristics()) {
					bestMove = -1;
					break;
				}
			}
		}


		swap();
		return bestMove;
	}

	//Swap sides if either player chooses to play swap
	private void swap()
	{
		side = side.opposite();
	}

	public void start()
	{
		try {
			//Puts the System.err prints to a err.txt file for debugging
			File file = new File("errThreads.txt");
			FileOutputStream fos = new FileOutputStream(file);
			System.setErr(new PrintStream(fos));

			//To store messages from the engine
			String msg;
			
			//If the agent isn't the starting player it can swap sides
			maySwap = false;
			if(timeToSearch == 0)
				timeToSearch = 500;

			System.err.println("Time to Search: "+timeToSearch);
			//Make a move
			while(true) {
				//Get message from the engine
				msg = Main.recvMsg();
				try {
					MsgType mt = Protocol.getMessageType(msg);
					switch(mt) {

						case START:
						System.err.println("A start");
						//Check which side the agent is playing
						first = Protocol.interpretStartMsg(msg);
						//Make the first move, now just plays hole 1
						if (first) {
							side = Side.SOUTH;
							moveCount++;
							int move = IDDFS();
							Main.sendMsg(Protocol.createMoveMsg(move));
						}
						else {
							side = Side.NORTH;
							maySwap = true;
						}
						System.err.println("Starting player? " + first);
						break;

						case STATE:
						System.err.println("A state");
						Protocol.MoveTurn moveTurn = Protocol.interpretStateMsg(msg, kalah.getBoard());
						System.err.println("This was the move: " + moveTurn.move);
						System.err.println("Is the game over? " + moveTurn.end);
						System.err.println("Is it our turn again? " + moveTurn.again);
						//If opponent swapped sides
						if (moveTurn.move == -1) {
							swap();
						}
						//If it's our turn again and the game hasn't ended
						if ((moveTurn.again) && (!moveTurn.end)) {

							msg = null;
							int move = 0;

							//Check whether to swap or not
							if (maySwap) {
								moveCount++;
								int swapMove = toSwap();

								if (swapMove == -1) {
									System.err.println("Swapping");
									swap();
									msg = Protocol.createSwapMsg();
								}

								else
									move = swapMove;
							}
							else
							{
								moveCount++;
								move = IDDFS();
							}
							maySwap = false;

							if (msg == null)
								msg = Protocol.createMoveMsg(move);
							Main.sendMsg(msg);
						}

						System.err.print("The board:\n" + kalah.getBoard());
						break;

						case END:
						System.err.println("An end");
						return;

					}
				}
				catch (InvalidMessageException e) {
					System.err.println(e.getMessage());
				}
			}
		}
		catch (IOException e) {
			System.err.println("This shouldn't happen " + e.getMessage());
		}
	}
}
