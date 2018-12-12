import subprocess
import sys
import random

def PlayKalah(player1, player2, name1, name2):
	cmd = 'java -jar ManKalah.jar "java -jar '+player1+'" "java -jar '+player2 +'"'
	outfile = open("results%sVS%s.txt" %(name1[:-4], name2[:-4]), "a+")
	out = subprocess.Popen(cmd, shell=True,  stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
	# while True:
	#  	line = out.stdout.readline()
	#  	print(str(line).split("'")[1].split("\\")[0])
	#  	if (str(line).split("'")[1].split("\\")[0].split(" ")[0] == "WINNER:"):
	#  		print("Winner found")
	#  	if out.poll() != None:
	#  		break
	while True:
	 	line = str(out.stdout.readline())
	 	first = line.split("'")[1].split("\\")[0].split(" ")[0]
	 	if(first == "WINNER:"):
	 		outfile.write(line.split("'")[1].split("\\")[0]+"\n")
	 	elif(first == "SCORE:"):
	 		outfile.write(line.split("'")[1].split("\\")[0]+"\n")
	 	elif(first == "Player"):
	 		outfile.write(line.split("'")[1].split("\\")[0]+"\n")

	 	if out.poll() != None:
	 		break
	outfile.close()
	print("Results written to: results%sVS%s.txt" %(name1[:-4], name2[:-4]))


player1 = sys.argv[2]
player2 = sys.argv[3]
name1 = player1
name2 = player2
p1 = player1
p2 = player2
timesToPlay = int(sys.argv[1])

for i in range(timesToPlay):
	depth = 0
	time = 0
	if(player1 == "AgentWithAlphaBeta.jar"):
		depth = random.randint(1, 11)
		p1 = player1 + " " + str(depth)
	if(player2 == "AgentWithAlphaBeta.jar"):
		depth = random.randint(1, 11)
		p2 = player2 + " " + str(depth)
	if("IDDFS" in player1): 
		time = random.randint(50, 2001)
		p1 = player1 + " " + str(time)
	if("IDDFS" in player2):
		time = random.randint(50, 2001)
		p2 = player2 + " " + str(time)

	print("Players: ", p1, p2)
	PlayKalah(p1, p2, name1, name2)