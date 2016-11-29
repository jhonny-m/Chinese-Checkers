package CC;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

class Piece implements Serializable{
	private int positionX, positionY; //Piece Position
	/**
	 * Node Constructor For player node
	 * @param x
	 * @param y
	 */
	public Piece (int x, int y){
		this.positionX = x;
		this.positionY = y;
	}
	public Piece( Piece piece){
		positionX= piece.positionX;
		positionY= piece.positionY;
	}
	/**
	 * Returns the node X coordinate
	 * @return 
	 */
	public int getPositionX(){
		return positionX;
	}
	/**
	 * Returns the node Y coordinate
	 * @return
	 */
	public int getPositionY(){
		return positionY;
	}
	
	/**
	 * Change the positions for the player pieces
	 * @param x
	 * @param y
	 */
	public void changePositions(int x, int y){
		this.positionX = x;
		this.positionY = y;
	}
	
}

class Player implements Serializable {
	private int number,position;
	private Piece[] playerPieces;
	/**
	 * Player constructor requires the position of the board where he will be playing
	 * (one the the 6 start corners), and the his number
	 * @param position
	 * @param number
	 */
	public Player(int position, int number){
		this.number= number;
		this.position=position;
		playerPieces=getStartPositions(position);
	}
	public Player( Player player){
		this.number= player.number;
		this.position= player.position;
		playerPieces=  new Piece[10];
		for(int i=0; i<10;i++){
			playerPieces[i]=new Piece(player.playerPieces[i]);
		}
		
	}
	
	public int getPosition(){
		return position;
	}
	public int getDistances(){
		int goalX,goalY;
		switch (position) {
		case 1:
			goalX=12;
			goalY=4;
			break;
		case 2:
			goalX=4;
			goalY=0;
			break;
		case 3:
			goalX=0;
			goalY=4;
			break;
		case 4:
			goalX=4;
			goalY=12;
			break;
		case 5:
			goalX=12;
			goalY=16;
			break;
		case 6:
			goalX=16;
			goalY=12;
			break;
		default:
			goalX=16;
			goalY=12;
			break;	
		}
		int sum=0;
		int max=0;
		int maxRepetitions=0;
		for(Piece piece :playerPieces){
			int temp;
			switch (position) {
			case 1:
				temp= piece.getPositionY()-goalY+goalX-piece.getPositionX() ;
				break;
			case 2:
				temp= piece.getPositionY()-goalY;
				break;
			case 3:
				temp= piece.getPositionX()-goalX;
				break;
			case 4:
				temp= piece.getPositionX()-goalX+goalY-piece.getPositionY();
				break;
			case 5:
				temp= goalY-piece.getPositionY();
				break;
			case 6:
				temp= goalX-piece.getPositionX();
				break;
			default:
				temp = (int)Math.sqrt( (piece.getPositionY()-goalY)*(piece.getPositionY()-goalY))+(int)Math.sqrt((piece.getPositionX()-goalX)*(piece.getPositionX()-goalX) );
				break;	
			}
			
			
			sum += temp;
			if( temp>max){
				max= temp;
				maxRepetitions=0;
			}
			if(max==temp){
				maxRepetitions++;
			}
			
		}
		return (max*(10)+maxRepetitions*3)+(sum);
	}
	
	private Piece[] getStartPositions(int position){
		int x = 0; 
		int y = 0; 
		char direction = 0;
		switch(position){
			case 1: x=4; y=12; direction='+'; break;
			case 2: x=12; y=13; direction='-'; break;
			case 3: x=13; y=12; direction='+'; break;
			case 4: x=12; y=4; direction='-'; break;
			case 5: x=4; y=3; direction='+'; break;
			case 6: x=3; y=4; direction='-'; break;
		}
		Piece[] pieces= new Piece[10];
		pieces[0]= new Piece(x,y);
		if( direction == '+'){
			pieces[1]= new Piece(x+1,y);
			pieces[2]= new Piece(x+2,y);
			pieces[3]= new Piece(x+3,y);
			pieces[4]= new Piece(x,y-1);
			pieces[5]= new Piece(x+1,y-1);
			pieces[6]= new Piece(x+2,y-1);
			pieces[7]= new Piece(x,y-2);
			pieces[8]= new Piece(x+1,y-2);
			pieces[9]= new Piece(x,y-3);
		}
		else{
			pieces[1]= new Piece(x-1,y);
			pieces[2]= new Piece(x-2,y);
			pieces[3]= new Piece(x-3,y);
			pieces[4]= new Piece(x,y+1);
			pieces[5]= new Piece(x-1,y+1);
			pieces[6]= new Piece(x-2,y+1);
			pieces[7]= new Piece(x,y+2);
			pieces[8]= new Piece(x-1,y+2);
			pieces[9]= new Piece(x,y+3);
		}
		return pieces;
	}
	/**
	 * return a vector with the pieces of the player
	 * @return
	 */
	public Piece[] getPieces(){
		return playerPieces;
	}
	
	public Piece getPiece(int x, int y){
		for(Piece piece : playerPieces){
			if (piece.getPositionX()==x && piece.getPositionY()==y  )
				return piece;
		}
		return null;
	}
	/**
	 * returns the number of the player
	 * @return
	 */
	public int getNumber(){
		return number;
	}
	public Boolean hasPlayerWon(){
		switch (position) {
		case 1:
			return comparePieces(playerPieces, getStartPositions(4));
		case 2:
			return comparePieces(playerPieces, getStartPositions(5));
		case 3:
			return comparePieces(playerPieces, getStartPositions(6));
		case 4:
			return comparePieces(playerPieces, getStartPositions(1));
		case 5:
			return comparePieces(playerPieces, getStartPositions(2));
		default:
			return comparePieces(playerPieces, getStartPositions(3));
		}
	}
	private Boolean comparePieces(Piece[] playerPieces,Piece[] finishPieces){
		int counter=0;
		for (Piece piece : playerPieces) {
			for (Piece finish : finishPieces) {
				if(piece.getPositionX()== finish.getPositionX() && piece.getPositionY()== finish.getPositionY())
				{
					counter++;
					break;
				}
			}
		}
		return counter==10;
	}
}
class rule implements Serializable{
	private int startPosition, size;
	/**
	 * constructor to the rule
	 * @param startPosition
	 * @param size
	 */
	public rule(int startPosition, int size){
		this.startPosition= startPosition;
		this.size = size;
	}
	/**
	 * returns the first position of the board on a certain line
	 * @return
	 */
	public int getStart(){
		return startPosition;
	}
	/**
	 * returns the size of the board on a certain line
	 * @return
	 */
	public int getSize(){
		return size;
	}
	/**
	 * returns the last position of the board on a certain line
	 * @return
	 */
	public int getEnd(){
		return startPosition+size;
	}
}
public class GameBoard implements Comparable<GameBoard>, Serializable{
	
	public int[][] board;
	public int depth;
	public GameBoard parent;
	public rule[] rules;
	private int numberOfPlayers, currentPlayer;
	private Player[] players;
	public int[] utilityHeuristic;
	public LinkedList<GameBoard> children;
	public boolean isSimulation;
	/**
	 * Initialize the game board according to the number of players
	 * @param numberOfPlayers
	 */
	public GameBoard(int numberOfPlayers){
		depth=0;
		this.numberOfPlayers = numberOfPlayers;
		rules = new rule[17];
		board = new int[17][17];
		players = new Player[numberOfPlayers];
		utilityHeuristic = new int[numberOfPlayers];
		Arrays.fill(utilityHeuristic, 0);
		setUpRules();
		startBoard();
		addPlayers(numberOfPlayers);
		currentPlayer=0;
		parent = null;
		children = null;
		isSimulation=false;
	}
	
	public GameBoard(GameBoard gameBoard){
		depth= gameBoard.depth+1;
		this.numberOfPlayers = gameBoard.numberOfPlayers;
		rules = gameBoard.rules;
		board = new int[17][17];
		utilityHeuristic = new int[numberOfPlayers];
		Arrays.fill(utilityHeuristic, 0);
		players = new Player[numberOfPlayers];
		for( int i =0; i< numberOfPlayers; i++){
		players[i] = new Player(gameBoard.players[i]) ;
		}
		for(int i=0;i<17;i++){
			board[i] = gameBoard.board[i].clone();
		}
		
		currentPlayer=gameBoard.currentPlayer;
		parent = gameBoard;
		children = null;
		isSimulation=false;
	}
	/**
	 * Method to create the rules
	 * these rules never change
	 */
	private void setUpRules(){
		rules[0]= new rule(4,1);
		rules[1]= new rule(4,2);
		rules[2]= new rule(4,3);
		rules[3]= new rule(4,4);		
		rules[4]= new rule(0,13);
		rules[5]= new rule(1,12);
		rules[6]= new rule(2,11);
		rules[7]= new rule(3,10);
		rules[8]= new rule(4,9);	
		rules[9]= new rule(4,10);
		rules[10]= new rule(4,11);
		rules[11]= new rule(4,12);
		rules[12]= new rule(4,13);
		rules[13]= new rule(9,4);
		rules[14]= new rule(10,3);
		rules[15]= new rule(11,2);
		rules[16]= new rule(12,1);
	}
	/**
	 * add the player pieces to the board
	 * @param player
	 */
	private void addPlayer(Player player){
		for(int i=0; i<10;i++){
			int x = player.getPieces()[i].getPositionX();
			int y = player.getPieces()[i].getPositionY();
			//System.out.print("x:"+x+" y:"+y+"\n");
			board[x][y]=player.getNumber();
			//System.out.print("x:"+x+" y:"+y+"\n");
		}
	}
	/**
	 * create and distribute the players 
	 * @param numberOfPlayers
	 */
	private void addPlayers(int numberOfPlayers){
		switch(numberOfPlayers){
		case 1: addPlayer(players[0] = new Player(1,1)); 
				break;
		case 2: addPlayer(players[0] = new Player(1,1)); 
				addPlayer(players[1] = new Player(4,2)); 
				break;
		case 3: addPlayer(players[0] = new Player(1,1)); 
				addPlayer(players[1] = new Player(3,2)); 
				addPlayer(players[2] = new Player(5,3));
				break;
		case 4: addPlayer(players[0] = new Player(1,1)); 
				addPlayer(players[1] = new Player(2,2)); 
				addPlayer(players[2] = new Player(4,3));
				addPlayer(players[3] = new Player(5,4));
				break;
		case 6: addPlayer(players[0] = new Player(1,1)); 
				addPlayer(players[1] = new Player(2,2)); 
				addPlayer(players[2] = new Player(3,3));
				addPlayer(players[3] = new Player(5,4)); 
				addPlayer(players[4] = new Player(5,5)); 
				addPlayer(players[5] = new Player(6,6));
				break;
		}
	}
	/**
	 * Creates an empty board
	 */
	private void startBoard(){
		for (int y=0; y<17; y++){
			for (int x= rules[y].getStart(); x< rules[y].getEnd(); x++ ){
				board[x][y] = 0;
			}
		}
	}
	public Player getCurrentPlayer(){
		return players[currentPlayer];
	}
	public int hasGameEnded(){
		if(ComputerPlayer.avaliableMoves(this).isEmpty()){
			if(this.calculateHeuristic()[0]>this.calculateHeuristic()[1]) return 1;
			if(this.calculateHeuristic()[0]<this.calculateHeuristic()[1]) return 0;
		}
		for(int i=0; i<numberOfPlayers;i++){
			if(players[i].hasPlayerWon()) return i;
		}
		return -1;
	}
	public void nextPlayer(){
		if(currentPlayer+1==numberOfPlayers){
			currentPlayer=0;
		}
		else{
			currentPlayer++;
		}
	}
	
	public boolean inBoard(int x , int y){
		if(x>=17 || y >=17 || y<0 || x<0 ) return false;
		if (rules[y].getStart() <= x && (rules[y].getSize()+rules[y].getStart())> x)
			return true;
		else return false;
	}
	public boolean isPositionEmpty(int x , int y){
		if(x<0 || x>16 ||y<0 || y>16 ) return false;
		if(board[x][y]==0)
			return true;
		else return false;
	}
	public void movePiece(Piece piece, int finalX, int finalY){
		int initialX, initialY;
		initialX= piece.getPositionX();
		initialY= piece.getPositionY();
		getPiece(initialX, initialY).changePositions(finalX, finalY);
		board[initialX][initialY]=0;
		board[finalX][finalY]=currentPlayer+1;
		
	}
	public int[] calculateHeuristic(){
		int[] values= new int[numberOfPlayers];
		for (int i=0; i<numberOfPlayers; i++){
			values[i]= players[i].getDistances();
		}
		return values;
	}
	public boolean uniqueInFamily(){
		GameBoard temp = this.parent;
		while( temp!=null){
			if(Arrays.deepEquals(this.board, temp.board)) return false;
			else temp= temp.parent;
		}
		return true;
	}
	
	public void setSimulation(){
		isSimulation=true;
	}
	
	public boolean avoidBadPosition(int x, int y, int position){
		switch (position) {
		case 1:
			if(x>12|| y<4 || y>12 || x<4) return false;
			break;
		case 2:
			if(x>12|| x<4) return false;
			if (((12 - 9)*(y -4) - (7 - 4)*(x - 9)) <= 0) return false;
			if(((7 - 4)*(y - 9) - (12 - 9)*(x - 4)) >= 0) return false;
			break;
		case 3:
			if( y<4 || y>12 ) return false;
			if (((12 - 9)*(y -4) - (7 - 4)*(x - 9)) <= 0) return false;
			if(((7 - 4)*(y - 9) - (12 - 9)*(x - 4)) >= 0) return false;
			break;
		case 4:
			if(x>12|| y<4 || y>12 || x<4) return false;
			break;
		case 5:
			if(x>12|| x<4) return false;
			if (((12 - 9)*(y -4) - (7 - 4)*(x - 9)) <= 0) return false;
			if(((7 - 4)*(y - 9) - (12 - 9)*(x - 4)) >= 0) return false;
			break;
		case 6:
			if( y<4 || y>12 ) return false;
			if (((12 - 9)*(y -4) - (7 - 4)*(x - 9)) <= 0) return false;
			if(((7 - 4)*(y - 9) - (12 - 9)*(x - 4)) >= 0) return false;
			break;
		default:
			return true;
		}
		return true;
	}
	@SuppressWarnings("unused")
	private boolean uniqueInFamily(GameBoard child, GameBoard parent){
		if( parent == null){
			return true;
		}
		if(Arrays.deepEquals(child.board, parent.board)) return false;
		else return uniqueInFamily(child, parent.parent);
	}
	public int 	getNumberOfPlayers(){
		return numberOfPlayers;
	}
	public Piece getPiece(int x, int y){
		return players[(board[x][y])-1].getPiece(x, y);
	}

	public void updateUtilityHeuristic(int winner){
		this.utilityHeuristic[winner]++;
		GameBoard currentNode = this;
		while(currentNode.parent!=null){
			currentNode.parent.utilityHeuristic[winner]++;
			currentNode= currentNode.parent;
		}
	}


	public int previusPlayer(){
		if(currentPlayer==0)
			return numberOfPlayers-1;
		else
			return currentPlayer-1;
	}

	public int defeatsHeuristic(int player){
		int thisDefeats=0;
		for(int i=0; i<numberOfPlayers; i++){
			
			if(i!= player){
				thisDefeats+=this.utilityHeuristic[i];
			}
		}
		return thisDefeats;
	}
	
	public static void main(String args[]){
		
		
		Scanner in = new Scanner(System.in);
		System.out.println("Número de Jogadores (2 ou 3)");
		int nOfPlayers= in.nextInt();
		
		if (nOfPlayers == 2){
			System.out.println("Primeiro Jogador: número de nós explorados");
			int sims1= in.nextInt();
			System.out.println("Primeiro Jogador: política de seleção e fator de ramificação\n1-Seleção Greedy, 10 nós\n2-Seleção Greedy, 5 nós\n3-Seleção Greedy, 20 nós\n4-Seleção Epsilon-Greedy, 10 nós");
			int sons1= in.nextInt();
			
			System.out.println("Segundo Jogador: número de nós explorados");
			int sims2= in.nextInt();
			System.out.println("Segundo Jogador: política de seleção e fator de ramificação\n1-Seleção Greedy, 10 nós\n2-Seleção Greedy, 5 nós\n3-Seleção Greedy, 20 nós\n4-Seleção Epsilon-Greedy, 10 nós");
			int sons2= in.nextInt();
			
			GameBoard game = new GameBoard(2);
			long clock= System.currentTimeMillis();
			int rounds=0;
			System.out.println("Iniciou o jogo");
			while(game.hasGameEnded()==-1){
			rounds++;
			game = ComputerPlayer.mcts(game,sims1,sons1);
			if(game.hasGameEnded()!=-1) break;
			game= ComputerPlayer.mcts(game,sims2,sons2);
			}
			
			System.out.println("Player "+(game.hasGameEnded()+1)+" won in "+rounds+" plays. Time : "+ (System.currentTimeMillis()-clock)/60000 +" minutes");

		}
		
		if(nOfPlayers==3){
			System.out.println("Primeiro Jogador: número de nós explorados");
			int sims1= in.nextInt();
			System.out.println("Primeiro Jogador: política de seleção e fator de ramificação\n1-Seleção Greedy, 10 nós\n2-Seleção Greedy, 5 nós\n3-Seleção Greedy, 20 nós\n4-Seleção Epsilon-Greedy, 10 nós");
			int sons1= in.nextInt();
			System.out.println("Segundo Jogador: número de nós explorados");
			int sims2= in.nextInt();
			System.out.println("Segundo Jogador: política de seleção e fator de ramificação\n1-Seleção Greedy, 10 nós\n2-Seleção Greedy, 5 nós\n3-Seleção Greedy, 20 nós\n4-Seleção Epsilon-Greedy, 10 nós");
			int sons2= in.nextInt();
			System.out.println("Terceiro Jogador: número de nós explorados");
			int sims3= in.nextInt();
			System.out.println("Terceiro Jogador: política de seleção e fator de ramificação\n1-Seleção Greedy, 10 nós\n2-Seleção Greedy, 5 nós\n3-Seleção Greedy, 20 nós\n4-Seleção Epsilon-Greedy, 10 nós");
			int sons3= in.nextInt();
			
			long clock= System.currentTimeMillis();
			GameBoard game = new GameBoard(3);
			int rounds=0;
			System.out.println("Iniciou o jogo");
			while(game.hasGameEnded()==-1){
			rounds++;
			game = ComputerPlayer.mcts(game,sims1,sons1);
			if(game.hasGameEnded()!=-1) break;
			game= ComputerPlayer.mcts(game,sims2,sons2);
			if(game.hasGameEnded()!=-1) break;
			game= ComputerPlayer.mcts(game,sims3,sons3);
			}
			System.out.println("Player "+(game.hasGameEnded()+1)+" won in "+rounds+" plays. Time : "+ (System.currentTimeMillis()-clock)/60000 +" minutes");
			
		}
		
		//testes
//		GameBoard game1 = new GameBoard(2);
//		ComputerPlayer.simulation(game1);
//
//		if (game1.hasGameEnded()==-1)
//		return;

//		int sims1=2000;
//		int sims2=2000;
//		int sons1=1;
//		int sons2=4;
		
		//Two Players code

//		System.out.println("2 Players");
//		System.out.println("10vs20 2000");
//		System.out.println("Fase 1");
//		
//		int x=0;
//		int rounds;
//		while(x++<15){
//		GameBoard game = new GameBoard(2);
//		long clock= System.currentTimeMillis();
//		rounds=0;
//		while(game.hasGameEnded()==-1){
//		rounds++;
//		game = ComputerPlayer.mcts(game,sims1,sons1);
//		if(game.hasGameEnded()!=-1) break;
//		game= ComputerPlayer.mcts(game,sims2,sons2);
//		}
//		
//		System.out.println("Player "+game.hasGameEnded()+" won in "+rounds+" plays. Time : "+ (System.currentTimeMillis()-clock)/60000 +" minutes");
//		}
//		
//		System.out.println("Fase 2");
//		x=0;
//		while(x++<15){
//		long clock= System.currentTimeMillis();
//		GameBoard game = new GameBoard(2);
//		rounds=0;
//		while(game.hasGameEnded()==-1){
//		rounds++;
//		game = ComputerPlayer.mcts(game,sims2,sons2);
//		if(game.hasGameEnded()!=-1) break;
//		game= ComputerPlayer.mcts(game,sims1,sons1);
//		}
//		System.out.println("Player "+game.hasGameEnded()+" won in "+rounds+" plays. Time : "+ (System.currentTimeMillis()-clock)/60000 +" minutes");
//		}
//
//		return;
		//3 players code
		
//		int sims1=4000;
//		int sims2=4000;
//		int sons1=1;
//		int sons2=2;
//		
//		System.out.println("3 Players");
//		System.out.println("10vs5 4000");
//		System.out.println("Fase 1");
//		int x=0;
//		int rounds;
//		while(x++<5){
//
//			long clock= System.currentTimeMillis();
//			GameBoard game = new GameBoard(3);
//			rounds=0;
//			while(game.hasGameEnded()==-1){
//			rounds++;
//			game = ComputerPlayer.mcts(game,sims1,sons1);
//			if(game.hasGameEnded()!=-1) break;
//			game= ComputerPlayer.mcts(game,sims2,sons2);
//			if(game.hasGameEnded()!=-1) break;
//			game= ComputerPlayer.mcts(game,sims1,sons1);
//			}
//			System.out.println("Player "+game.hasGameEnded()+" won in "+rounds+" plays. Time : "+ (System.currentTimeMillis()-clock)/60000 +" minutes");
//		}	
//		
//		System.out.println("Fase 2");
//		x=0;
//		while(x++<5){
//		long clock= System.currentTimeMillis();
//		GameBoard game = new GameBoard(3);
//		rounds=0;
//		while(game.hasGameEnded()==-1){
//		rounds++;
//		game = ComputerPlayer.mcts(game,sims1,sons1);
//		if(game.hasGameEnded()!=-1) break;
//		game= ComputerPlayer.mcts(game,sims2,sons2);
//		if(game.hasGameEnded()!=-1) break;
//		game= ComputerPlayer.mcts(game,sims2,sons2);
//		}
//		System.out.println("Player "+game.hasGameEnded()+" won in "+rounds+" plays. Time : "+ (System.currentTimeMillis()-clock)/60000 +" minutes");
//		}
//		
//		System.out.println("Fase 3");
//		x=0;
//		while(x++<5){
//		long clock= System.currentTimeMillis();
//		GameBoard game = new GameBoard(3);
//		rounds=0;
//		while(game.hasGameEnded()==-1){
//		rounds++;
//		game = ComputerPlayer.mcts(game,sims1,sons1);
//		if(game.hasGameEnded()!=-1) break;
//		game= ComputerPlayer.mcts(game,sims1,sons1);
//		if(game.hasGameEnded()!=-1) break;
//		game= ComputerPlayer.mcts(game,sims2,sons2);
//		}
//		System.out.println("Player "+game.hasGameEnded()+" won in "+rounds+" plays. Time : "+ (System.currentTimeMillis()-clock)/60000 +" minutes");
//		}
//		
//		System.out.println("Fase 4");
//		x=0;
//		while(x++<5){
//		long clock= System.currentTimeMillis();
//		GameBoard game = new GameBoard(3);
//		rounds=0;
//		while(game.hasGameEnded()==-1){
//		rounds++;
//		game = ComputerPlayer.mcts(game,sims2,sons2);
//		if(game.hasGameEnded()!=-1) break;
//		game= ComputerPlayer.mcts(game,sims1,sons1);
//		if(game.hasGameEnded()!=-1) break;
//		game= ComputerPlayer.mcts(game,sims2,sons2);
//		}
//		System.out.println("Player "+game.hasGameEnded()+" won in "+rounds+" plays. Time : "+ (System.currentTimeMillis()-clock)/60000 +" minutes");
//		}
//		
//		System.out.println("Fase 5");
//		x=0;
//		while(x++<5){
//		long clock= System.currentTimeMillis();
//		GameBoard game = new GameBoard(3);
//		rounds=0;
//		while(game.hasGameEnded()==-1){
//		rounds++;
//		game = ComputerPlayer.mcts(game,sims2,sons2);
//		if(game.hasGameEnded()!=-1) break;
//		game= ComputerPlayer.mcts(game,sims2,sons2);
//		if(game.hasGameEnded()!=-1) break;
//		game= ComputerPlayer.mcts(game,sims1,sons1);
//		}
//		System.out.println("Player "+game.hasGameEnded()+" won in "+rounds+" plays. Time : "+ (System.currentTimeMillis()-clock)/60000 +" minutes");
//		}
//		
//		System.out.println("Fase 6");
//		x=0;
//		while(x++<5){
//		long clock= System.currentTimeMillis();
//		GameBoard game = new GameBoard(3);
//		rounds=0;
//		while(game.hasGameEnded()==-1){
//		rounds++;
//		game = ComputerPlayer.mcts(game,sims2,sons2);
//		if(game.hasGameEnded()!=-1) break;
//		game= ComputerPlayer.mcts(game,sims1,sons1);
//		if(game.hasGameEnded()!=-1) break;
//		game= ComputerPlayer.mcts(game,sims1,sons1);
//		}
//		System.out.println("Player "+game.hasGameEnded()+" won in "+rounds+" plays. Time : "+ (System.currentTimeMillis()-clock)/60000 +" minutes");		
//		}
		
		
		
		//end 3 players code
		
//		while(rootGame.parent!=null){
//			rootGame=rootGame.parent;
//		}
//		saveGameTree(rootGame,"TwoPlayers.bin");
//		System.out.println("Tree Saved");
		
		
		
		
		
//		game.movePiece(game.getPiece(4, 12), 12, 4);
//		game.movePiece(game.getPiece(5, 12), 12, 5);
//		game.movePiece(game.getPiece(6, 12), 12, 6);
//		game.movePiece(game.getPiece(7, 12), 12, 7);
//		game.movePiece(game.getPiece(4, 11), 11, 4);
//		game.movePiece(game.getPiece(5, 11), 11, 5);
//		game.movePiece(game.getPiece(6, 11), 11, 6);
//		game.movePiece(game.getPiece(4, 10), 10, 4);
//		game.movePiece(game.getPiece(5, 10), 10, 5);
//		game.movePiece(game.getPiece(4, 9), 9, 4);
//		System.out.println(game.hasGameEnded());
		//GameBoard game2 = new GameBoard(game);
		//System.out.println(game2.uniqueInFamily()+" "+ Arrays.deepEquals(game2.board, game.board));
		//System.out.print(game.depth+"\n");
//		for(int i =0; i<17; i++){
//			for(int j=0; j<17; j++){
//				if ((j < game.rules[i].getStart())||(j >= game.rules[i].getEnd())){
//					System.out.print(" ");
//				}
//				else System.out.print(game.board[j][i]);
//			}
//			System.out.print("\n");
//		}
//		System.out.print(game.depth+"\n");
//		}
	}

	
	@Override
	public int compareTo(GameBoard o) {
		if(isSimulation){
			if(o.calculateHeuristic()[this.currentPlayer]>this.calculateHeuristic()[this.currentPlayer])
				return -1;
			if ( o.calculateHeuristic()[this.currentPlayer]== this.calculateHeuristic()[this.currentPlayer])
				return 0;
			else return 1;
		}
		else{
			float otherValue=(float)0.0 , thisValue =(float)0.0;
			float parentN= this.parent.defeatsHeuristic(this.previusPlayer())+parent.utilityHeuristic[this.previusPlayer()];
			float otherN=o.defeatsHeuristic(this.previusPlayer())+o.utilityHeuristic[this.previusPlayer()], thisN=this.defeatsHeuristic(this.previusPlayer())+this.utilityHeuristic[this.previusPlayer()];

			if (otherN==0)
			{
				otherValue=(float) (0.0+(1/ Math.sqrt(2))*Math.sqrt(2*Math.log(parentN)/1));
			}
			else
			{
				otherValue=(float) (o.utilityHeuristic[this.previusPlayer()]/otherN+(1/ Math.sqrt(2))*Math.sqrt(2*Math.log(parentN)/otherN));
			}
			if (thisN==0)
			{
				thisValue=(float) (0.0+(1/ Math.sqrt(2))*Math.sqrt(2*Math.log(parentN)/1));
			}
			else
			{
				thisValue=(float) (this.utilityHeuristic[this.previusPlayer()]/thisN+(1/ Math.sqrt(2))*Math.sqrt(2*Math.log(parentN)/thisN));
			}
			
			
			if(otherValue>thisValue)
				return -1;
			if(otherValue==thisValue)
				return 0;
			else return 1;
		}
	}
	
	public static void saveGameTree(GameBoard gameBoard, String file) {

		try {
			String filename= file;
			ObjectOutputStream os=	new ObjectOutputStream(new FileOutputStream(filename));
			os.writeObject(gameBoard);
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	public static GameBoard readGameTree(String file) {
		String filename= file;
		
		try {
			ObjectInputStream is=	new ObjectInputStream(new FileInputStream(filename));
			GameBoard gameBoard = (GameBoard) is.readObject();
			is.close();
			return gameBoard;
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

}
