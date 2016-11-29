package CC;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.zip.Checksum;

public final class ComputerPlayer {
	
	
	public static GameBoard mcts(GameBoard gameBoard,int maxNodes, int childType){
		long startTime = System.currentTimeMillis();
		GameBoard currentNode = null;
		int nodes=0;
		//(System.currentTimeMillis()-startTime)/1000<10
		while( nodes<maxNodes){
			currentNode = gameBoard;
			while(currentNode.children!=null){
				currentNode=Collections.max(currentNode.children);
			}
			if(currentNode.hasGameEnded()== -1){
				
				if(childType==1)
					currentNode.children= bestMoveGame(currentNode);
				else if(childType==2)
					currentNode.children= randomMoves1(currentNode);
				else if(childType==3)
					currentNode.children= randomMoves2(currentNode);
				else 
					currentNode.children= mixedMoves(currentNode);
				
				for(GameBoard child : currentNode.children){
					child.nextPlayer();
					simulation(child);
					nodes++;
				}
			}
			else{
				simulation(currentNode);
				nodes++;
			}
		}
		float bestValue=(float) -1.0;
		GameBoard bestChild=null;
		//System.out.println("current Player: " + (gameBoard.getCurrentPlayer().getNumber()-1));
		for(GameBoard child : gameBoard.children){
			//System.out.println("vitorias: "+child.utilityHeuristic[gameBoard.getCurrentPlayer().getNumber()-1]+" derrotas: "+child.defeatsHeuristic(gameBoard.getCurrentPlayer().getNumber()-1));
			float sum=0;
			for (int i =0; i< child.utilityHeuristic.length;i++){
				sum += child.utilityHeuristic[i];
			}
			float winrate= (float)child.utilityHeuristic[child.previusPlayer()]/(float)sum;
			if(winrate >bestValue){
				bestChild=child;
				bestValue=winrate;
			}
		}
		
		
//		System.out.println(bestValue);
//		for(int i =0; i<17; i++){
//			for(int j=0; j<17; j++){
//			if ((j < bestChild.rules[i].getStart())||(j >= bestChild.rules[i].getEnd())){
//			System.out.print(" ");
//		}
//			else System.out.print(bestChild.board[j][i]);
//		}
//		System.out.print("\n");
//	}
//		System.out.println("simulations: "+ nodes+" , depth: "+bestChild.depth+" Time: "+((System.currentTimeMillis()-startTime)/1000)+" ,memory used: "+(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()));
//		System.out.println("Best Child: vitorias: "+bestChild.utilityHeuristic[gameBoard.getCurrentPlayer().getNumber()-1]+" derrotas: "+bestChild.defeatsHeuristic(gameBoard.getCurrentPlayer().getNumber()-1));

		bestChild.parent.children=null;
		bestChild.children=null;
		Arrays.fill(bestChild.utilityHeuristic, 0);
		return bestChild;
		
		
	}
	

	
	public static void simulation(GameBoard gameBoard){
		GameBoard currentNode= gameBoard;
		LinkedList<GameBoard> children;
		int steps=0;
		while( currentNode.hasGameEnded()==-1){
			steps++;
			children =avaliableMoves(currentNode);
			for(GameBoard child : children){
				child.setSimulation();
			}
			//System.out.println(children.size());
			if(children.isEmpty()){
				gameBoard.updateUtilityHeuristic(gameBoard.previusPlayer());
				return;
			}
			
			Random random = new Random();
			int randNumber=random.nextInt(100);
			if(randNumber<5){
				randNumber=random.nextInt(children.size());
				currentNode= children.get(randNumber);
				currentNode.nextPlayer();
			}
			else{
				currentNode= Collections.min(children);
				currentNode.nextPlayer();
			}
			
//			System.out.println("number of children "+ children.size() +" deptth "+currentNode.depth);
//			for(int i =0; i<17; i++){
//			for(int j=0; j<17; j++){
//				if ((j < currentNode.rules[i].getStart())||(j >= currentNode.rules[i].getEnd())){
//					System.out.print(" ");
//				}
//				else System.out.print(currentNode.board[j][i]);
//			}
//			System.out.print("\n");
//		}
			
		}
		//System.out.println( steps );
		gameBoard.updateUtilityHeuristic(currentNode.hasGameEnded());
	}
	
	public static GameBoard maxN(GameBoard gameBoard){

		LinkedList<GameBoard> children = avaliableMoves(gameBoard);
		Collections.shuffle(children);
		GameBoard bestChild=null;
		int currentPlayer= gameBoard.getCurrentPlayer().getNumber()-1;
		int[] value= new int[gameBoard.getNumberOfPlayers()];
		Arrays.fill(value, 1000);
		for( GameBoard child : children){
			int[] temp = maxN2(child,2);
			if(value[currentPlayer]> temp[currentPlayer]){
				value= temp.clone();
				bestChild=child;
			}
		}
		return bestChild;
	}
	
	public static int[] maxN2(GameBoard gameBoard,int maxDepth){
		int[] value= new int[gameBoard.getNumberOfPlayers()];
		Arrays.fill(value, 1000);
		if(gameBoard.hasGameEnded()!=-1){
			value[gameBoard.hasGameEnded()]=0;
		}
		if(maxDepth==0){
			return gameBoard.calculateHeuristic();
		}
		maxDepth--;
		gameBoard.nextPlayer();
		LinkedList<GameBoard> children = avaliableMoves(gameBoard);
		Collections.shuffle(children);
		int currentPlayer= gameBoard.getCurrentPlayer().getNumber()-1;
		for( GameBoard child : children){
			int[] temp = maxN2(child,maxDepth);
			if(value[currentPlayer]> temp[currentPlayer]){
				value= temp.clone();
			}
		}
		return value;
	}
	
	static LinkedList<GameBoard> bestMoveGame(GameBoard gameBoard){
		LinkedList<GameBoard> moveList = new LinkedList<GameBoard>();
		LinkedList<GameBoard> temp= new LinkedList<GameBoard>();
		Player currentPlayer= gameBoard.getCurrentPlayer();
		for(int i=0; i<10; i++){
			temp.addAll(simpleMoves(gameBoard,currentPlayer.getPieces()[i], currentPlayer.getPosition()));
			temp.addAll(complexMoves(gameBoard,currentPlayer.getPieces()[i], currentPlayer.getPosition()));
		}
		if(!temp.isEmpty()){
			Collections.shuffle(temp);
			for( GameBoard child : temp){
				child.setSimulation();
			}
			Collections.sort(temp);
			int max=10;
			if (temp.size()<10)
				max=temp.size();
			for(int i=0;i<max;i++){
				temp.get(i).isSimulation=false;
				moveList.add(temp.get(i));
			}
		}
		return moveList;
	}
	
	static LinkedList<GameBoard> mixedMoves(GameBoard gameBoard){
		LinkedList<GameBoard> moveList = new LinkedList<GameBoard>();
		LinkedList<GameBoard> temp= new LinkedList<GameBoard>();
		Player currentPlayer= gameBoard.getCurrentPlayer();
		for(int i=0; i<10; i++){
			temp.addAll(simpleMoves(gameBoard,currentPlayer.getPieces()[i], currentPlayer.getPosition()));
			temp.addAll(complexMoves(gameBoard,currentPlayer.getPieces()[i], currentPlayer.getPosition()));
		}
		if(!temp.isEmpty()){
			Collections.shuffle(temp);
			for( GameBoard child : temp){
				child.setSimulation();
			}
			Collections.sort(temp);
			int max=10;
			if (temp.size()<10)
				max=temp.size();
			for(int i=0;i<max/2;i++){
				temp.get(0).isSimulation=false;
				moveList.add(temp.remove(0));
			}
			Collections.shuffle(temp);
			for(int i=max/2;i<max;i++){
				temp.get(0).isSimulation=false;
				moveList.add(temp.remove(0));
			}
		}
		return moveList;
	}
	
	static LinkedList<GameBoard> randomMoves1(GameBoard gameBoard){
		LinkedList<GameBoard> moveList = new LinkedList<GameBoard>();
		LinkedList<GameBoard> temp= new LinkedList<GameBoard>();
		Player currentPlayer= gameBoard.getCurrentPlayer();
		for(int i=0; i<10; i++){
			temp.addAll(simpleMoves(gameBoard,currentPlayer.getPieces()[i], currentPlayer.getPosition()));
			temp.addAll(complexMoves(gameBoard,currentPlayer.getPieces()[i], currentPlayer.getPosition()));
		}
		if(!temp.isEmpty()){
			Collections.shuffle(temp);
			for( GameBoard child : temp){
				child.setSimulation();
			}
			Collections.sort(temp);
			int max=5;
			if (temp.size()<max)
				max=temp.size();
			for(int i=0;i<max;i++){
				temp.get(i).isSimulation=false;
				moveList.add(temp.get(i));
			}
		}
		return moveList;
	}
	
	static LinkedList<GameBoard> randomMoves2(GameBoard gameBoard){
		LinkedList<GameBoard> moveList = new LinkedList<GameBoard>();
		LinkedList<GameBoard> temp= new LinkedList<GameBoard>();
		Player currentPlayer= gameBoard.getCurrentPlayer();
		for(int i=0; i<10; i++){
			temp.addAll(simpleMoves(gameBoard,currentPlayer.getPieces()[i], currentPlayer.getPosition()));
			temp.addAll(complexMoves(gameBoard,currentPlayer.getPieces()[i], currentPlayer.getPosition()));
		}
		if(!temp.isEmpty()){
			Collections.shuffle(temp);
			for( GameBoard child : temp){
				child.setSimulation();
			}
			Collections.sort(temp);
			int max=20;
			if (temp.size()<max)
				max=temp.size();
			for(int i=0;i<max;i++){
				temp.get(i).isSimulation=false;
				moveList.add(temp.get(i));
			}
		}
		return moveList;
	}
	
	static LinkedList<GameBoard> avaliableMoves(GameBoard gameBoard)
	{
		LinkedList<GameBoard> moveList = new LinkedList<GameBoard>();
		Player currentPlayer= gameBoard.getCurrentPlayer();
		for(int i=0; i<10; i++){
			moveList.addAll(simpleMoves(gameBoard,currentPlayer.getPieces()[i], currentPlayer.getPosition()));
			//System.out.println("x "+gameBoard.getCurrentPlayer().getPieces()[i].getPositionX()+" y "+gameBoard.getCurrentPlayer().getPieces()[i].getPositionY());
			moveList.addAll(complexMoves(gameBoard,currentPlayer.getPieces()[i], currentPlayer.getPosition()));

		}
		
		return moveList;
	}
	
	static LinkedList<GameBoard> complexMoves(GameBoard gameBoard, Piece piece, int position){
		
		int positionX= piece.getPositionX();
		int positionY= piece.getPositionY();
		Piece movingPiece = piece;
		LinkedList<GameBoard> moves= new LinkedList<GameBoard>();
		switch (position) {
		case 1:
				//++
				if( gameBoard.inBoard(positionX+1, positionY+1) && !gameBoard.isPositionEmpty(positionX+1, positionY+1)){
					if( gameBoard.inBoard(positionX+2, positionY+2) && gameBoard.isPositionEmpty(positionX+2, positionY+2)&& gameBoard.avoidBadPosition(positionX+2, positionY+2,position)){
						moves.addLast(new GameBoard(gameBoard));
						moves.getLast().movePiece(piece, positionX+2, positionY+2);
						if(moves.getLast().uniqueInFamily()){
							movingPiece = moves.getLast().getPiece(positionX+2, positionY+2);
							moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
						}
						else moves.removeLast();
					}
				}
				
				
				//--
				if( gameBoard.inBoard(positionX-1, positionY-1) && !gameBoard.isPositionEmpty(positionX-1, positionY-1)){
					if( gameBoard.inBoard(positionX-2, positionY-2) && gameBoard.isPositionEmpty(positionX-2, positionY-2)&& gameBoard.avoidBadPosition(positionX-2, positionY-2,position)){
						moves.addLast(new GameBoard(gameBoard));
						moves.getLast().movePiece(piece, positionX-2, positionY-2);
						if(moves.getLast().uniqueInFamily()){
							movingPiece = moves.getLast().getPiece(positionX-2, positionY-2);
							moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
						}
						else moves.removeLast();
					}
				}
				//0-
				if( gameBoard.inBoard(positionX, positionY-1) && !gameBoard.isPositionEmpty(positionX, positionY-1)){
					if( gameBoard.inBoard(positionX, positionY-2) && gameBoard.isPositionEmpty(positionX, positionY-2)&& gameBoard.avoidBadPosition(positionX, positionY-2,position)){
						moves.addLast(new GameBoard(gameBoard));
						moves.getLast().movePiece(piece, positionX, positionY-2);
						if(moves.getLast().uniqueInFamily()){
							movingPiece = moves.getLast().getPiece(positionX, positionY-2);
							moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
						}
						else moves.removeLast();
					}
				}
				//+0
				if( gameBoard.inBoard(positionX+1, positionY) && !gameBoard.isPositionEmpty(positionX+1, positionY)){
					if( gameBoard.inBoard(positionX+2, positionY) && gameBoard.isPositionEmpty(positionX+2, positionY)&& gameBoard.avoidBadPosition(positionX+2, positionY,position)){
						moves.addLast(new GameBoard(gameBoard));
						moves.getLast().movePiece(piece, positionX+2, positionY);
						if(moves.getLast().uniqueInFamily()){
							movingPiece = moves.getLast().getPiece(positionX+2, positionY);
							moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
						}
						else moves.removeLast();
					}
				}				

			break;
		
		case 2:
				//--
			if( gameBoard.inBoard(positionX-1, positionY-1) && !gameBoard.isPositionEmpty(positionX-1, positionY-1)){
				if( gameBoard.inBoard(positionX-2, positionY-2) && gameBoard.isPositionEmpty(positionX-2, positionY-2)&& gameBoard.avoidBadPosition(positionX-2, positionY-2,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX-2, positionY-2);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX-2, positionY-2);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
			
				
				//-0
			if( gameBoard.inBoard(positionX-1, positionY) && !gameBoard.isPositionEmpty(positionX-1, positionY)){
				if( gameBoard.inBoard(positionX-2, positionY) && gameBoard.isPositionEmpty(positionX-2, positionY)&& gameBoard.avoidBadPosition(positionX-2, positionY,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX-2, positionY);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX-2, positionY);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
				//0-
			if( gameBoard.inBoard(positionX, positionY-1) && !gameBoard.isPositionEmpty(positionX, positionY-1)){
				if( gameBoard.inBoard(positionX, positionY-2) && gameBoard.isPositionEmpty(positionX, positionY-2)&& gameBoard.avoidBadPosition(positionX, positionY-2,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX, positionY-2);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX, positionY-2);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
				
				//+0
			if( gameBoard.inBoard(positionX+1, positionY) && !gameBoard.isPositionEmpty(positionX+1, positionY)){
				if( gameBoard.inBoard(positionX+2, positionY) && gameBoard.isPositionEmpty(positionX+2, positionY)&& gameBoard.avoidBadPosition(positionX+2, positionY,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX+2, positionY);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX+2, positionY);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}	
				
			
			break;
		
		case 3:
			
			
				//--
			if( gameBoard.inBoard(positionX-1, positionY-1) && !gameBoard.isPositionEmpty(positionX-1, positionY-1)){
				if( gameBoard.inBoard(positionX-2, positionY-2) && gameBoard.isPositionEmpty(positionX-2, positionY-2)&& gameBoard.avoidBadPosition(positionX-2, positionY-2,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX-2, positionY-2);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX-2, positionY-2);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
				
				//-0
			if( gameBoard.inBoard(positionX-1, positionY) && !gameBoard.isPositionEmpty(positionX-1, positionY)){
				if( gameBoard.inBoard(positionX-2, positionY) && gameBoard.isPositionEmpty(positionX-2, positionY)&& gameBoard.avoidBadPosition(positionX-2, positionY,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX-2, positionY);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX-2, positionY);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
				
				
				//0-
			if( gameBoard.inBoard(positionX, positionY-1) && !gameBoard.isPositionEmpty(positionX, positionY-1)){
				if( gameBoard.inBoard(positionX, positionY-2) && gameBoard.isPositionEmpty(positionX, positionY-2)&& gameBoard.avoidBadPosition(positionX, positionY-2,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX, positionY-2);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX, positionY-2);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
				
				//0+
			if( gameBoard.inBoard(positionX, positionY+1) && !gameBoard.isPositionEmpty(positionX, positionY+1)){
				if( gameBoard.inBoard(positionX, positionY+2) && gameBoard.isPositionEmpty(positionX, positionY+2)&& gameBoard.avoidBadPosition(positionX, positionY+2,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX, positionY+2);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX, positionY+2);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
			
			break;
		
		case 4:
			//++
			if( gameBoard.inBoard(positionX+1, positionY+1) && !gameBoard.isPositionEmpty(positionX+1, positionY+1)){
				if( gameBoard.inBoard(positionX+2, positionY+2) && gameBoard.isPositionEmpty(positionX+2, positionY+2)&& gameBoard.avoidBadPosition(positionX+2, positionY+2,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX+2, positionY+2);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX+2, positionY+2);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
			
			
			//--
			if( gameBoard.inBoard(positionX-1, positionY-1) && !gameBoard.isPositionEmpty(positionX-1, positionY-1)){
				if( gameBoard.inBoard(positionX-2, positionY-2) && gameBoard.isPositionEmpty(positionX-2, positionY-2)&& gameBoard.avoidBadPosition(positionX-2, positionY-2,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX-2, positionY-2);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX-2, positionY-2);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
				//-0
			if( gameBoard.inBoard(positionX-1, positionY) && !gameBoard.isPositionEmpty(positionX-1, positionY)){
				if( gameBoard.inBoard(positionX-2, positionY) && gameBoard.isPositionEmpty(positionX-2, positionY)&& gameBoard.avoidBadPosition(positionX-2, positionY,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX-2, positionY);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX-2, positionY);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
				//0+
			if( gameBoard.inBoard(positionX, positionY+1) && !gameBoard.isPositionEmpty(positionX, positionY+1)){
				if( gameBoard.inBoard(positionX, positionY+2) && gameBoard.isPositionEmpty(positionX, positionY+2)&& gameBoard.avoidBadPosition(positionX, positionY+2,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX, positionY+2);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX, positionY+2);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
				
			
			break;
		
		case 5:
				//++
			if( gameBoard.inBoard(positionX+1, positionY+1) && !gameBoard.isPositionEmpty(positionX+1, positionY+1)){
				if( gameBoard.inBoard(positionX+2, positionY+2) && gameBoard.isPositionEmpty(positionX+2, positionY+2)&& gameBoard.avoidBadPosition(positionX+2, positionY+2,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX+2, positionY+2);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX+2, positionY+2);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
				//+0
			if( gameBoard.inBoard(positionX+1, positionY) && !gameBoard.isPositionEmpty(positionX+1, positionY)){
				if( gameBoard.inBoard(positionX+2, positionY) && gameBoard.isPositionEmpty(positionX+2, positionY)&& gameBoard.avoidBadPosition(positionX+2, positionY,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX+2, positionY);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX+2, positionY);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}				
				//0+
			if( gameBoard.inBoard(positionX, positionY+1) && !gameBoard.isPositionEmpty(positionX, positionY+1)){
				if( gameBoard.inBoard(positionX, positionY+2) && gameBoard.isPositionEmpty(positionX, positionY+2)&& gameBoard.avoidBadPosition(positionX, positionY+2,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX, positionY+2);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX, positionY+2);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
				
				//-0
			if( gameBoard.inBoard(positionX-1, positionY) && !gameBoard.isPositionEmpty(positionX-1, positionY)){
				if( gameBoard.inBoard(positionX-2, positionY) && gameBoard.isPositionEmpty(positionX-2, positionY)&& gameBoard.avoidBadPosition(positionX-2, positionY,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX-2, positionY);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX-2, positionY);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
				
			
			break;
		
		case 6:
				//++
			if( gameBoard.inBoard(positionX+1, positionY+1) && !gameBoard.isPositionEmpty(positionX+1, positionY+1)){
				if( gameBoard.inBoard(positionX+2, positionY+2) && gameBoard.isPositionEmpty(positionX+2, positionY+2)&& gameBoard.avoidBadPosition(positionX+2, positionY+2,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX+2, positionY+2);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX+2, positionY+2);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
				//+0
			if( gameBoard.inBoard(positionX+1, positionY) && !gameBoard.isPositionEmpty(positionX+1, positionY)){
				if( gameBoard.inBoard(positionX+2, positionY) && gameBoard.isPositionEmpty(positionX+2, positionY)&& gameBoard.avoidBadPosition(positionX+2, positionY,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX+2, positionY);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX+2, positionY);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}			
				//0+
			if( gameBoard.inBoard(positionX, positionY+1) && !gameBoard.isPositionEmpty(positionX, positionY+1)){
				if( gameBoard.inBoard(positionX, positionY+2) && gameBoard.isPositionEmpty(positionX, positionY+2)&& gameBoard.avoidBadPosition(positionX, positionY+2,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX, positionY+2);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX, positionY+2);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
				
				//0-
			if( gameBoard.inBoard(positionX, positionY-1) && !gameBoard.isPositionEmpty(positionX, positionY-1)){
				if( gameBoard.inBoard(positionX, positionY-2) && gameBoard.isPositionEmpty(positionX, positionY-2)&& gameBoard.avoidBadPosition(positionX, positionY-2,position)){
					moves.addLast(new GameBoard(gameBoard));
					moves.getLast().movePiece(piece, positionX, positionY-2);
					if(moves.getLast().uniqueInFamily()){
						movingPiece = moves.getLast().getPiece(positionX, positionY-2);
						moves.addAll(complexMoves(moves.getLast(), movingPiece,position));
					}
					else moves.removeLast();
				}
			}
				
			
			
			break;

		default:
			break;
		}
		return moves;
		
	}
		
	static LinkedList<GameBoard> simpleMoves(GameBoard gameBoard, Piece piece, int position){
		int positionX= piece.getPositionX();
		int positionY= piece.getPositionY();
		LinkedList<GameBoard> moves= new LinkedList<GameBoard>();
		switch (position) {
		case 1:
			// x,-y
			if( gameBoard.inBoard(positionX, positionY-1) && gameBoard.isPositionEmpty(positionX, positionY-1)&& gameBoard.avoidBadPosition(positionX, positionY-1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX, positionY-1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// +x,y
			if( gameBoard.inBoard(positionX+1, positionY) && gameBoard.isPositionEmpty(positionX+1, positionY)&& gameBoard.avoidBadPosition(positionX+1, positionY,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX+1, positionY);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// -x,-y
			if( gameBoard.inBoard(positionX-1, positionY-1) && gameBoard.isPositionEmpty(positionX-1, positionY-1) && gameBoard.avoidBadPosition(positionX-1, positionY-1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX-1, positionY-1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// +x,+y
			if( gameBoard.inBoard(positionX+1, positionY+1) && gameBoard.isPositionEmpty(positionX+1, positionY+1) && gameBoard.avoidBadPosition(positionX+1, positionY+1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX+1, positionY+1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			break;
			

		case 2:
			// -x,-y
			if( gameBoard.inBoard(positionX-1, positionY-1) && gameBoard.isPositionEmpty(positionX-1, positionY-1)&& gameBoard.avoidBadPosition(positionX-1, positionY-1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX-1, positionY-1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// +x,y
			if( gameBoard.inBoard(positionX+1, positionY) && gameBoard.isPositionEmpty(positionX+1, positionY)&& gameBoard.avoidBadPosition(positionX+1, positionY,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX+1, positionY);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// x,-y
			if( gameBoard.inBoard(positionX, positionY-1) && gameBoard.isPositionEmpty(positionX, positionY-1)&& gameBoard.avoidBadPosition(positionX, positionY-1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX, positionY-1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// -x,y
			if( gameBoard.inBoard(positionX-1, positionY) && gameBoard.isPositionEmpty(positionX-1, positionY)&& gameBoard.avoidBadPosition(positionX-1, positionY,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX-1, positionY);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			break;
		case 3:
			// -x,-y
			if( gameBoard.inBoard(positionX-1, positionY-1) && gameBoard.isPositionEmpty(positionX-1, positionY-1)&& gameBoard.avoidBadPosition(positionX-1, positionY-1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX-1, positionY-1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// -x,y
			if( gameBoard.inBoard(positionX-1, positionY) && gameBoard.isPositionEmpty(positionX-1, positionY)&& gameBoard.avoidBadPosition(positionX-1, positionY,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX-1, positionY);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// x,+y
			if( gameBoard.inBoard(positionX, positionY+1) && gameBoard.isPositionEmpty(positionX, positionY+1)&& gameBoard.avoidBadPosition(positionX, positionY+1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX, positionY+1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// x,-y
			if( gameBoard.inBoard(positionX, positionY-1) && gameBoard.isPositionEmpty(positionX, positionY-1)&& gameBoard.avoidBadPosition(positionX, positionY-1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX, positionY-1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			break;
		case 4:
			// -x,-y
			if( gameBoard.inBoard(positionX-1, positionY-1) && gameBoard.isPositionEmpty(positionX-1, positionY-1)&& gameBoard.avoidBadPosition(positionX-1, positionY-1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX-1, positionY-1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// +x,+y
			if( gameBoard.inBoard(positionX+1, positionY+1) && gameBoard.isPositionEmpty(positionX+1, positionY+1)&& gameBoard.avoidBadPosition(positionX+1, positionY+1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX+1, positionY+1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// x,+y
			if( gameBoard.inBoard(positionX, positionY+1) && gameBoard.isPositionEmpty(positionX, positionY+1)&& gameBoard.avoidBadPosition(positionX, positionY+1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX, positionY+1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// -x,y
			if( gameBoard.inBoard(positionX-1, positionY) && gameBoard.isPositionEmpty(positionX-1, positionY)&& gameBoard.avoidBadPosition(positionX-1, positionY,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX-1, positionY);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			break;
		case 5:
			// +x,+y
			if( gameBoard.inBoard(positionX+1, positionY+1) && gameBoard.isPositionEmpty(positionX+1, positionY+1)&& gameBoard.avoidBadPosition(positionX+1, positionY+1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX+1, positionY+1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// x,+y
			if( gameBoard.inBoard(positionX, positionY+1) && gameBoard.isPositionEmpty(positionX, positionY+1)&& gameBoard.avoidBadPosition(positionX, positionY+1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX, positionY+1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// +x,y
			if( gameBoard.inBoard(positionX+1, positionY) && gameBoard.isPositionEmpty(positionX+1, positionY)&& gameBoard.avoidBadPosition(positionX+1, positionY,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX+1, positionY);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// -x,y
			if( gameBoard.inBoard(positionX-1, positionY) && gameBoard.isPositionEmpty(positionX-1, positionY)&& gameBoard.avoidBadPosition(positionX-1, positionY,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX-1, positionY);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			break;
		case 6:
			// +x,+y
			if( gameBoard.inBoard(positionX+1, positionY+1) && gameBoard.isPositionEmpty(positionX+1, positionY+1)&& gameBoard.avoidBadPosition(positionX+1, positionY+1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX+1, positionY+1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// x,+y
			if( gameBoard.inBoard(positionX, positionY+1) && gameBoard.isPositionEmpty(positionX, positionY+1)&& gameBoard.avoidBadPosition(positionX, positionY+1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX, positionY+1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// x,-y
			if( gameBoard.inBoard(positionX, positionY-1) && gameBoard.isPositionEmpty(positionX, positionY-1)&& gameBoard.avoidBadPosition(positionX, positionY-1,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX, positionY-1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// +x,y
			if( gameBoard.inBoard(positionX+1, positionY) && gameBoard.isPositionEmpty(positionX+1, positionY)&& gameBoard.avoidBadPosition(positionX+1, positionY,position)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX+1, positionY);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			
			break;
		default:
			// -x,-y
			if( gameBoard.inBoard(positionX-1, positionY-1) && gameBoard.isPositionEmpty(positionX-1, positionY-1)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX-1, positionY-1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// +x,+y
			if( gameBoard.inBoard(positionX+1, positionY+1) && gameBoard.isPositionEmpty(positionX+1, positionY+1)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX+1, positionY+1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// x,+y
			if( gameBoard.inBoard(positionX, positionY+1) && gameBoard.isPositionEmpty(positionX, positionY+1)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX, positionY+1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// x,-y
			if( gameBoard.inBoard(positionX, positionY-1) && gameBoard.isPositionEmpty(positionX, positionY-1)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX, positionY-1);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// +x,y
			if( gameBoard.inBoard(positionX+1, positionY) && gameBoard.isPositionEmpty(positionX+1, positionY)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX+1, positionY);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			// -x,y
			if( gameBoard.inBoard(positionX-1, positionY) && gameBoard.isPositionEmpty(positionX-1, positionY)){
				moves.addLast(new GameBoard(gameBoard));
				moves.getLast().movePiece(piece, positionX-1, positionY);
				if(!moves.getLast().uniqueInFamily()){
					moves.removeLast();
				}
			}
			
			break;
		}
		return moves;
		
	}
	
}