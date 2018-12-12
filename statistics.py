import sys
player1Wins = 0
player1Score = 0
player1Time = 0
player2Wins = 0
player2Score = 0
player2Time = 0
noOfDraws = 0
noOfGames = 0
player1SearchDepth = 0
player2SearchDepth = 0
player1SearchTime = 0
player2SearchTime = 0

fileToOpen = sys.argv[1]
print("File to Open: ", fileToOpen)
with open(fileToOpen) as file:
	for line in file:
		if(line.split(" ")[0] == "WINNER:" and line.split(" ")[2] == "1"):
			player1Wins+=1
			noOfGames+=1
			nextLine = file.readline()
			if(nextLine.split(" ")[0] == "SCORE:"):
				player1Score += int(nextLine.split(" ")[1])
			if("MKRef" in line or "Group2" in line or "Jimmy" in line):
				continue
			if("AlphaBeta" in line.split(" ")[5]):
				player1SearchDepth += int(line.split(" ")[6].split(")")[0])
			if("IDDFS" in line.split(" ")[5]):
				player1SearchTime += int(line.split(" ")[6].split(")")[0])


		if(line.split(" ")[0] == "WINNER:" and line.split(" ")[2] == "2"):
			player2Wins+=1
			noOfGames+=1
			nextLine = file.readline()
			if(nextLine.split(" ")[0] == "SCORE:"):
				player2Score += int(nextLine.split(" ")[1])
			if("MKRef" in line or "Group2" in line or "Jimmy" in line):
				continue
			if("AlphaBeta" in line.split(" ")[5]):
				player2SearchDepth += int(line.split(" ")[6].split(")")[0])
			if("IDDFS" in line.split(" ")[5]):
				player2SearchTime += int(line.split(" ")[6].split(")")[0])
		
		if(line.split(" ")[0] == "Player"):
			if(line.split(" ")[1] == "1"):
				if("MKRef" in line or "Group2" in line or "Jimmy" in line):
					player1Time += int(line.split(" ")[7])
				else:
					player1Time += int(line.split(" ")[8])
					
			if(line.split(" ")[1] == "2"):
				if("MKRef" in line or "Group2" in line or "Jimmy" in line):
					player2Time += int(line.split(" ")[7])
				else:
					player2Time += int(line.split(" ")[8])			

		if(line.split(" ")[0] == "SCORE:" and line.split(" ")[1] == "0\n"):
			noOfDraws+=1
			noOfGames+=1

print("Statistics:\n")
print("Total number of Games: ", noOfGames)
print("\nPlayer 1 Wins: ", player1Wins)
print("Player 1 Total Winning Scores: ", player1Score)
if player1Wins != 0:
	print("Player 1 Score/Win: ", float(player1Score)/player1Wins)
else:
	print("Player 1 Score/Win: 0")
#print("Player 1 Total Time/Move: ", player1Time)
print("Player 1 Actual Time/Move/Game", float(player1Time)/noOfGames)
if player1SearchDepth != 0:
	#print("Player 1 Search Depth: ", player1SearchDepth)
	print("Player 1 Average Winning Search Depth: ", float(player1SearchDepth/player1Wins))
if player1SearchTime != 0:
	#print("Player 1 Search Time Given as Params: ", player1SearchTime)
	print("Player 1 Average Winning Search Time Given as Params: ", float(player1SearchTime/player1Wins))

print("\nPlayer 2 Wins: ", player2Wins)
print("Player 2 Total Winning Scores: ", player2Score)
if player2Wins != 0:
	print("Player 2 Score/Win: ", float(player2Score)/player2Wins)
else:
	print("Player 2 Score/Win: 0")
#print("Player 2 Total Time/Move: ", player2Time)
print("Player 2 Actual Time/Move/Game", float(player2Time)/noOfGames)
if player2SearchDepth != 0:
	#print("Player 2 Search Depth Given as Params: ", player2SearchDepth)
	print("Player 2 Average winning Search Depth Given as Params: ", float(player2SearchDepth/player2Wins))
if player2SearchTime != 0:
	#print("Player 2 Total Search Time Given as Params: ", player2SearchTime)
	print("Player 2 Average Winning Search Time Given as Params: ", float(player2SearchTime/player2Wins))

print("\nNumber of Draws: ", noOfDraws)

