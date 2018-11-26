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

	//Params: the depth of the search, the current board, whose turn it is
	//TO DO: For alpha-beta pruning, add alpha and beta arguments of type int
	private int minimax(int height, Board board, boolean maximizingPlayer, int alpha, int beta)
	{
		//Search to the given depth or leaf node reached
		if (height == 0 || kalah.gameOver(board)) {
			int v = evaluate(board);
			System.err.println("Evaluated value: " + v);
			return v;
		}
		//Go through all the moves and check if they're valid
		for(int i = 1; i <= holes; i++) {

			Side currentSide;
			if(maximizingPlayer)
				currentSide = side;
			else
				currentSide = side.opposite();

			Move move = new Move(currentSide,i);

			if (kalah.isLegalMove(board, move)) {

				if (maximizingPlayer) {

					//Board b = new Board(kalah.getBoard());
					System.err.println("Board before max move: " + board);
					Kalah.makeMove(board, move);
					System.err.println("Board after max move: " + board);
					int maxEval = Integer.MIN_VALUE;

					//Add aplha and beta as parameters
					int eval = minimax(height-1, board, false, alpha, beta);
					maxEval = Math.max(maxEval, eval);
					//Alpha-beta here
				  alpha = Math.max(alpha, maxEval);
					System.err.println("Max player Alpha: " + alpha + ", beta: " + beta);
					if(beta <= alpha) {
						System.err.println("pruning");
						break;
					}

					return maxEval;
				}

				else {

					//Board b = new Board(kalah.getBoard());
					System.err.println("Board before min move: " + board);
					Kalah.makeMove(board, move);
					System.err.println("Board after min move: " + board);
					int minEval = Integer.MAX_VALUE;

					//Add aplha and beta as parameters
					int eval = minimax(height-1, board, true, alpha, beta);
					minEval = Math.min(minEval, eval);
					//Alpha-beta here
					beta = Math.min(beta, minEval);
					System.err.println("Min plaAlpha: " + alpha + ", beta: " + beta);
					if(beta <= alpha) {
						System.err.println("pruning");
						break;
					}


					return minEval;
				}
			}

		}

		return Integer.MIN_VALUE;
	}

	//TO DO: MODIFY THE METHOD SO THAT IT WOULD CHOOSE THE BEST NEW MOVE FIRST
	//BY CALLING EVALUATION FUNCTION ON ALL POSSIBLE MOVES FIRST AND THEN
	//SORT THEM AND CALL THE MINIMAX IN THE SORTED ORDER
	//Finds the next best move to make
	private int nextMove()
	{
		int bestMove = 0;
		int bestHeuristics = Integer.MIN_VALUE;

		//Go through all the possible moves
		for (int i = 1; i <= holes; i++) {

			Move move = new Move(side,i);

			if (kalah.isLegalMove(move)) {

				Board board = new Board(kalah.getBoard());
				Kalah.makeMove(board, move);
				int heuristics = minimax(3, board,false, Integer.MIN_VALUE, Integer.MAX_VALUE);

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

		//Calculate evaluation for no swap
		//Go through all the possible moves
		System.err.println("Checking evaluation for no swapping");
		for (int i = 1; i <= holes; i++) {

			Move move = new Move(side,i);

			if (kalah.isLegalMove(move)) {
				Board board = new Board(kalah.getBoard());
				Kalah.makeMove(board, move);
				System.err.printf("Board after agent move %d: ",i);
				System.err.println(board);
				int heuristics = minimax(5, board,false, Integer.MIN_VALUE, Integer.MAX_VALUE);

				//Check if this move is better than previous best one
				if (heuristics > noSwapEvaluation) {
					bestMove = i;
					noSwapEvaluation = heuristics;
				}
			}
		}
		swap();
		System.err.println("Checking evaluation for swapping");
		//Calculate evaluation for swap
		//Go through all the possible moves
		for (int i = 1; i <= holes; i++) {

			Move move = new Move(side.opposite(),i);

			if (kalah.isLegalMove(move)) {
				Board board = new Board(kalah.getBoard());
				Kalah.makeMove(board, move);
				System.err.printf("Board after not agent move %d: ",i);
				System.err.println(board);
				int heuristics = minimax(5, board, true, Integer.MIN_VALUE, Integer.MAX_VALUE);

				//Check if this move is better than previous best one
				if (heuristics > swapEvaluation) {
					swapEvaluation = heuristics;
				}
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
			boolean maySwap = false;

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
						boolean first = Protocol.interpretStartMsg(msg);
						//Make the first move, now just plays hole 1
						if (first) {
							side = Side.SOUTH;
							int move = nextMove();
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
								move = nextMove();
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
