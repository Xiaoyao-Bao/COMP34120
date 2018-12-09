package MKAgent;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;


public class Agent
{
	Side side;

	Kalah kalah;

	int holes;

	int moveCount = 0;

	boolean first;	

	boolean maySwap;

	long startTime;

	boolean timeElapsed = false;

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
	private int[] minimax(int height, Board board, boolean maximizingPlayer, int alpha, int beta) {
		//Search to the given depth or leaf node reached
		if (height == 0 || kalah.gameOver(board)) {
			int v = evaluate(board);
		//	System.err.println("Evaluated value: " + v);
			return new int[] {v, alpha, beta};
		}

		if (maximizingPlayer) {

			int maxEval = Integer.MIN_VALUE;
			for(int i = 1; i <= holes; i++) {

				Move move = new Move(side,i);

				if (kalah.isLegalMove(board, move)) {
					//Board b = new Board(kalah.getBoard());
				//	System.err.println("Board before max move: " + board);
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
				//	System.err.println("Board after max move: " + board);
				//	System.err.println("Is it our turn now? " + maximizingPlayer);

					int eval = minimax(height-1, board, maximizingPlayer, alpha, beta)[0];
					//Undo the last move
					board = lastBoard;

					maxEval = Math.max(maxEval, eval);
					alpha = Math.max(alpha, eval);
				//	System.err.println("alpha: " + alpha);
					if (beta <= alpha) {
				//		System.err.println("Pruning, alpha: " + alpha + ", beta: " + beta);
						break;
					}
					
				}
				if((System.currentTimeMillis() - startTime > 5000) || (maySwap && System.currentTimeMillis() - startTime > 2500)){
						timeElapsed = true;
						break;
					}
			}
			return new int[] {maxEval, alpha, beta};
		}
		
		else {
			int minEval = Integer.MAX_VALUE;

			for(int i = 1; i <= holes; i++) {

				Move move = new Move(side.opposite(),i);

				if (kalah.isLegalMove(board, move)) {
				//	System.err.println("Board before min move: " + board);
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
				//	System.err.println("Board after min move: " + board);
				//	System.err.println("Is it our turn now? " + maximizingPlayer);
					
					int eval = minimax(height-1, board, maximizingPlayer, alpha, beta)[0];
					//Undo the last move
					board = lastBoard;
					minEval = Math.min(minEval, eval);

					beta = Math.min(beta, eval);
				//	System.err.println("beta: " + beta);
					if (beta <= alpha) {
				//		System.err.println("Pruning, alpha: " + alpha + ", beta: " + beta);
						break;
					}
				}
				if((System.currentTimeMillis() - startTime > 5000) || (maySwap && System.currentTimeMillis() - startTime > 2500)){
						timeElapsed = true;
						break;
					}
			}
			return new int[] {minEval, alpha, beta};
		}
	}

	private int IDDFS()
	{
		int bestMove = 0;
		int bestHeuristics = Integer.MIN_VALUE;
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		boolean ourTurn = false;
		//int depth = 0;
		timeElapsed = false;
		startTime = System.currentTimeMillis();
		while(!timeElapsed){
			for(int depth=0; ; depth++)
			{
				//Go through all the possible moves
				for (int i = 1; i <= holes; i++) {

					Move move = new Move(side,i);

					if (kalah.isLegalMove(move)) {

						Board board = new Board(kalah.getBoard());
						Side nextTurn = Kalah.makeMove(board, move);
						//Check whose turn it is next
						if(nextTurn == side)
							ourTurn = true;
						else
							ourTurn = false;
						if(first && moveCount ==1) {
							//System.err.println("First move so we wont get new turn");
							ourTurn = false;
						}
						//System.err.printf("Board after agent move %d: ",i);
						//System.err.println(board);
						//System.err.println("Is it our turn now? " + ourTurn);
						
						int[] results = minimax(depth, board,ourTurn, alpha, beta);
						int heuristics = results[0];
						alpha = results[1];
						alpha = Math.max(alpha, heuristics);

						//Check if this move is better than previous best one
						if (heuristics > bestHeuristics) {
							bestMove = i;
							bestHeuristics = heuristics;
						}
					}
					if(System.currentTimeMillis() - startTime > 5000){
						System.err.println("Five seconds elapse, depth reached: " + depth);
						timeElapsed = true;
						break;
					}
				}
				if(timeElapsed)
					break;
			}
		}
		return bestMove;
	}

	//TO DO: MODIFY THE METHOD SO THAT IT WOULD CHOOSE THE BEST NEW MOVE FIRST
	//BY CALLING EVALUATION FUNCTION ON ALL POSSIBLE MOVES FIRST AND THEN
	//SORT THEM AND CALL THE MINIMAX IN THE SORTED ORDER
	//Finds the next best move to make
	private int nextMove()
	{
		int bestMove = 0;
		int bestHeuristics = Integer.MIN_VALUE;
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		boolean ourTurn = false;

		//Go through all the possible moves
		for (int i = 1; i <= holes; i++) {

			Move move = new Move(side,i);

			if (kalah.isLegalMove(move)) {

				Board board = new Board(kalah.getBoard());
				Side nextTurn = Kalah.makeMove(board, move);
				//Check whose turn it is next
				if(nextTurn == side)
					ourTurn = true;
				else
					ourTurn = false;
				//System.err.printf("Board after agent move %d: ",i);
				System.err.println(board);
				//System.err.println("Is it our turn now? " + ourTurn);
				
				int[] results = minimax(3, board,ourTurn, alpha, beta);
				int heuristics = results[0];
				alpha = results[1];
				alpha = Math.max(alpha, heuristics);

				//Check if this move is better than previous best one
				if (heuristics > bestHeuristics) {
					bestMove = i;
					bestHeuristics = heuristics;
				}
			}
		}
		return bestMove;
	}

	//Checks whether to perform a swap or a normal move
	//Returns -1 if swap, else number of the best move
	private int toSwap()
	{
		System.err.println("Checking whether to swap");
		int bestMove = 0;
		int swapEvaluation = Integer.MIN_VALUE;
		int noSwapEvaluation = Integer.MIN_VALUE;
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		boolean ourTurn = false;
		timeElapsed = false;
		startTime = System.currentTimeMillis();

		//Calculate evaluation for no swap
		//Go through all the possible moves
		System.err.println("Checking evaluation for no swapping " + kalah.getBoard());
		while(!timeElapsed)
		{
			for(int depth = 0; ; depth++)
			{
				for (int i = 1; i <= holes; i++) {

					Move move = new Move(side,i);

					if (kalah.isLegalMove(move)) {
						Board board = new Board(kalah.getBoard());
						Side nextTurn = Kalah.makeMove(board, move);
					//	System.err.println("nextTurn:" + nextTurn+ " side: "+side);
						//Check whose turn it is next
						if(nextTurn == side)
							ourTurn = true;
						else
							ourTurn = false;
					//	System.err.printf("Board after agent move %d: ",i);
					//	System.err.println(board);
					//	System.err.println("Is it our turn now? " + ourTurn);
						int[] results = minimax(3, board,ourTurn, alpha, beta);
						int heuristics = results[0];
						alpha = results[1];
						alpha = Math.max(alpha, heuristics);
					
						//Check if this move is better than previous best one
						if (heuristics > noSwapEvaluation) {
							bestMove = i;
							noSwapEvaluation = heuristics;
						}
					}
					if(System.currentTimeMillis() - startTime > 2500){
						System.err.println("2.5 seconds elapsed, depth reached: " + depth);
						timeElapsed = true;
						break;
					}
				}
				if(timeElapsed)
					break;
			}			
		}
		swap();
		alpha = Integer.MIN_VALUE;
		beta = Integer.MAX_VALUE;
		timeElapsed = false;
		startTime = System.currentTimeMillis();
		System.err.println("Checking evaluation for swapping " + kalah.getBoard());
		while(!timeElapsed) {
			for(int depth = 0; ; depth++)
			{
				//Calculate evaluation for swap
				//Go through all the possible moves
				for (int i = 1; i <= holes; i++) {

					Move move = new Move(side.opposite(),i);

					if (kalah.isLegalMove(move)) {
						Board board = new Board(kalah.getBoard());
						Side nextTurn = Kalah.makeMove(board, move);
						//Check whose turn it is next
						if(nextTurn == side)
							ourTurn = true;
						else
							ourTurn = false;
						//System.err.printf("Board after not agent move %d: ",i);
						//System.err.println(board);
						//System.err.println("Is it our turn now? " + ourTurn);
						int[] results = minimax(3, board, ourTurn, alpha, beta);
						int heuristics = results[0];
						beta = results[2];
						beta = Math.min(beta, heuristics);
						//Check if this move is better than previous best one
						if (heuristics > swapEvaluation) {
							swapEvaluation = heuristics;
						}
					}
					if(System.currentTimeMillis() - startTime > 2500){
						System.err.println("2.5 seconds elapsed, depth reached: " + depth);
						timeElapsed = true;
						break;
					}
				}
				if(timeElapsed)
					break;
			}		
		}
		swap();
		System.err.printf("Swap evaluation: %d, noSwapEvaluation: %d\n", swapEvaluation, noSwapEvaluation);
		if(swapEvaluation > noSwapEvaluation)
			return -1;
		else
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
			File file = new File("err.txt");
			FileOutputStream fos = new FileOutputStream(file);
			System.setErr(new PrintStream(fos));

			//To store messages from the engine
			String msg;

			//If the agent isn't the starting player it can swap sides
			maySwap = false;

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
