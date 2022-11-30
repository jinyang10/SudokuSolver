package SudokuSolverDLX;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import SudokuSolverDLX.DancingLinks.*;
import SudokuSolverDLX.DLXSudoku.*;


public class ChessSudoku
{
	/* SIZE is the size parameter of the Sudoku puzzle, and N is the square of the size.  For 
	 * a standard Sudoku puzzle, SIZE is 3 and N is 9. 
	 */
	public int SIZE, N;

	/* The grid contains all the numbers in the Sudoku puzzle.  Numbers which have
	 * not yet been revealed are stored as 0. 
	 */
	public int grid[][];

	/* Booleans indicating whether of not one or more of the chess rules should be 
	 * applied to this Sudoku. 
	 */
	public boolean knightRule;
	public boolean kingRule;
	public boolean queenRule;

	
	// Field that stores the same Sudoku puzzle solved in all possible ways
	public HashSet<ChessSudoku> solutions = new HashSet<ChessSudoku>();

	//true if row contains the number, false otherwise
	private boolean checkRow(int row, int num) {
		for (int i=0; i<N; i++) {
			if (grid[row][i] == num) {
				return true;
			}
		}
		return false;
	}
	//true if column contains the number, false otherwise
	private boolean checkCol(int col, int num) {
		for (int i=0; i<N; i++) {
			if (grid[i][col] == num) {
				return true;
			}
		}
		return false;
	}
	//given a cell, checks whether the SIZExSIZE box that it's positioned in contains that number
	private boolean checkBox(int row, int col, int num) {
		int r = row - row % SIZE;
		int c = col - col % SIZE;
		
		for (int i=r; i<r+SIZE; i++) {
			for (int j=c; j<c+SIZE; j++) {
				if (grid[i][j] == num) {
					return true;
				}
			}
		}
		return false;
	}
	//no conflict 2 squares vertically and 1 horizontally, or 2 horizontally and 1 vertical
	//true if conflict, false if none
	private boolean checkKnight(int row, int col, int num) {

		int addRow = row + 1;
		int addtwoRow = row + 2;
		int subRow = row - 1;
		int subtwoRow = row - 2;
		int addCol = col + 1;
		int addtwoCol = col +2;
		int subCol = col - 1;
		int subtwoCol = col - 2;
		
		if (addtwoRow < N) {
			if (addCol < N) {
				if (grid[addtwoRow][addCol] == num) {
					return true;
				}
			}
			if (subCol >= 0) {
				if (grid[addtwoRow][subCol] == num) {
					return true;
				}
			}
			
		}
		if (addRow < N) {
			if (addtwoCol < N) {
				if (grid[addRow][addtwoCol] == num) {
					return true;
				}
			}
			if (subtwoCol >= 0) {
				if (grid[addRow][subtwoCol] == num) {
					return true;
				}
			}
		}
		if (subRow >= 0) {
			if (addtwoCol < N) {
				if (grid[subRow][addtwoCol] == num) {
					return true;
				}
			}
			if (subtwoCol >= 0) {
				if (grid[subRow][subtwoCol] == num) {
					return true;
				}
			}
		}
		if (subtwoRow >= 0) {
			if (addCol < N) {
				if (grid[subtwoRow][addCol] == num) {
					return true;
				}
			}
			if (subCol >= N) {
				if (grid[subtwoRow][subCol] == num) {
					return true;
				}
			}
		}
		return false;
	}

	//no conflict a signle diagonal away
	//true if conflict, false if none
	private boolean checkKing(int row, int col, int num) {
		int addRow = row + 1;
		int addCol = col + 1;
		int subRow = row - 1;
		int subCol = col - 1;
		
		if (addRow < N) {
			if (addCol < N) {
				if (grid[addRow][addCol] == num) {
					return true;
				}
			}
			if (subCol >= 0) {
				if (grid[addRow][subCol] == num) {
					return true;
				}
			}
		}
		if (subRow >= 0) {
			if (addCol < N) {
				if (grid[subRow][addCol] == num) {
					return true;
				}
			}
			if (subCol >= 0) {
				if (grid[subRow][subCol] == num) {
					return true;
				}
			}
		}
		return false;
	}

	//every 9 can't be in the same row/col/3x3 box or diagonal of any other 9
	private boolean checkQueen(int row, int col) {
		int addRow = row + 1;
		int addCol = col + 1;
		int subRow = row - 1;
		int subCol = col - 1;

		//check diagonals going up to the right
		while (addRow < N && addCol < N) {
			if (grid[addRow][addCol] == 9) {
				return true;
			}
			addRow += 1;
			addCol += 1;
		}
		//check diagonals going down to the left
		while (subRow >= 0 && subCol >= 0) {
			if (grid[subRow][subCol] == 9) {
				return true;
			}
			subRow -= 1;
			subCol -= 1;
		}
		addRow = row + 1;
		addCol = col + 1;
		subRow = row - 1;
		subCol = col - 1;
		
		while (addRow < N && subCol >= 0) {
			if (grid[addRow][subCol] == 9) {
				return true;
			}
			addRow += 1;
			subRow -= 1;
		}
		while (subRow >= 0 && addCol < N) {
			if (grid[subRow][addCol] == 9) {
				return true;
			}
			subRow -= 1;
			addCol += 1;
		}
		
		return false;
	}
	
	private boolean oneSol() {
		
		for (int row = 0; row<N; row++) {
			
			for (int col = 0; col<N; col++) {
				
				// if grid is empty
				if (grid[row][col] == 0) {
					
					List<Integer> validNums = new LinkedList<>();
					
					for (int i=1; i<=N; i++) {
						validNums.add(i);
					}
					//check valid values for this row
					int c=0; 
					while (c<N) {
						for (int i=0; i<validNums.size(); i++) {
							
							int value = validNums.get(i);
							if (grid[row][c] == value) {
								validNums.remove(i);
								break;
							}
						}
						c++;
					}
					//check valid values for this column
					int r=0;
					while (r<N) {
						for (int i=0; i<validNums.size(); i++) {
							
							int value = validNums.get(i);
							if (grid[r][col] == value) {
								validNums.remove(i);
								break;
							}
						
						}
						r++;
					}
					//check valid values for this box
					
					int frow = row - row % SIZE;
					int fcol = col - col % SIZE;
					for (int rows = frow; rows<frow+SIZE; rows++) {
						for (int cols=fcol; cols<fcol+SIZE; cols++) {
							for (int i=0; i<validNums.size(); i++) {
								
								int value = validNums.get(i);
								if (grid[rows][cols] == value) {
									validNums.remove(i);
									break;
								}
							}
						}
					}
					//loop through each valid number
					for (int pos=0; pos<validNums.size(); pos++) {
						int value = validNums.get(pos);
						if (knightRule && kingRule && queenRule) {
							if (!checkKing(row, col, value) && !checkQueen(row, col) && !checkKnight(row, col, value)) {
								grid[row][col] = value;
								
								if (oneSol()) {
									ChessSudoku original = new ChessSudoku(SIZE);
									for (int R=0; R<N; R++) {
										for (int C=0; C<N; C++) {
											original.grid[R][C] = this.grid[R][C];
										}
									}
									solutions.add(original);
									return true;
									
								} else {
									grid[row][col] = 0;
								}
							}
							
						}
						if (knightRule && kingRule && !queenRule) {
							if (!checkKnight(row, col, value) && !checkKing(row, col, value)) {
								grid[row][col] = value;
								if (oneSol()) {
									ChessSudoku original = new ChessSudoku(SIZE);
									for (int R=0; R<N; R++) {
										for (int C=0; C<N; C++) {
											original.grid[R][C] = this.grid[R][C];
										}
									}
									solutions.add(original);
									return true;
								} else {
									grid[row][col] = 0;
								}
							}
						}
						if (knightRule && queenRule && !kingRule) {
							if(!checkKnight(row, col, value) && !checkQueen(row, col)) {
								grid[row][col] = value;
								if (oneSol()) {
									ChessSudoku original = new ChessSudoku(SIZE);
									for (int R=0; R<N; R++) {
										for (int C=0; C<N; C++) {
											original.grid[R][C] = this.grid[R][C];
										}
									}
									solutions.add(original);
									return true;
								} else {
									grid[row][col] = 0;
								}
							}
						}
						if (queenRule && kingRule && !knightRule) {
							if (!checkQueen(row, col) && !checkKing(row, col, value)) {
								grid[row][col] = value;
								if (oneSol()) {
									ChessSudoku original = new ChessSudoku(SIZE);
									for (int R=0; R<N; R++) {
										for (int C=0; C<N; C++) {
											original.grid[R][C] = this.grid[R][C];
										}
									}
									solutions.add(original);
									return true;
								} else {
									grid[row][col] = 0;
								}
							}
						}
						if (knightRule && !queenRule && !kingRule) {
							if (!checkKnight(row, col, value)) {
								grid[row][col] = value;
								if (oneSol()) {
									ChessSudoku original = new ChessSudoku(SIZE);
									for (int R=0; R<N; R++) {
										for (int C=0; C<N; C++) {
											original.grid[R][C] = this.grid[R][C];
										}
									}
									solutions.add(original);
									return true;
								} else {
									grid[row][col] = 0;
								}
							}
						}
						if (queenRule && !knightRule && !kingRule) {
							if (!checkQueen(row, col)) {
								grid[row][col] = value;
								if (oneSol()) {
									ChessSudoku original = new ChessSudoku(SIZE);
									for (int R=0; R<N; R++) {
										for (int C=0; C<N; C++) {
											original.grid[R][C] = this.grid[R][C];
										}
									}
									solutions.add(original);
									return true;
								} else {
									grid[row][col] = 0;
								}
							}
						}
						if (kingRule && !queenRule && !knightRule) {
							if (!checkKing(row, col, value)) {
								grid[row][col] = value;
								if (oneSol()) {
									ChessSudoku original = new ChessSudoku(SIZE);
									for (int R=0; R<N; R++) {
										for (int C=0; C<N; C++) {
											original.grid[R][C] = this.grid[R][C];
										}
									}
									solutions.add(original);
									return true;
								} else {
									grid[row][col] = 0;
								}
							} 
						}
						if (!kingRule && !queenRule && !knightRule) {
							grid[row][col] = value;
							if (oneSol()) {
								ChessSudoku original = new ChessSudoku(SIZE);
								for (int R=0; R<N; R++) {
									for (int C=0; C<N; C++) {
										original.grid[R][C] = this.grid[R][C];
									}
								}
								solutions.add(original);
								return true;
							} else {
								grid[row][col] = 0;
							}
						}
					}
					//if the none of the numbers lead to a solution
					//System.out.println("false");
					return false;
					
				}
			}
		}
		// if no empty spaces were found
		return true;
	}
	
	
	private boolean solveMult() {
		
		for (int row = 0; row<N; row++) {
			
			for (int col = 0; col<N; col++) {
				
				//if cell is empty
				if (grid[row][col] == 0) {
					
					List<Integer> validNums = new LinkedList<>();
					
					for (int i=1; i<=N; i++) {
						validNums.add(i);
					}
					//check valid values for this row
					int c=0; 
					while (c<N) {
						for (int i=0; i<validNums.size(); i++) {
							
							int value = validNums.get(i);
							if (grid[row][c] == value) {
								validNums.remove(i);
								break;
							}
						}
						c++;
					}
					//check valid values for this column
					int r=0;
					while (r<N) {
						for (int i=0; i<validNums.size(); i++) {
							
							int value = validNums.get(i);
							if (grid[r][col] == value) {
								validNums.remove(i);
								break;
							}
						
						}
						r++;
					}
					//check valid values for this box
					
					int frow = row - row % SIZE;
					int fcol = col - col % SIZE;
					for (int rows = frow; rows<frow+SIZE; rows++) {
						for (int cols=fcol; cols<fcol+SIZE; cols++) {
							for (int i=0; i<validNums.size(); i++) {
								
								int value = validNums.get(i);
								if (grid[rows][cols] == value) {
									validNums.remove(i);
									break;
								}
							}
						}
					}
					//loop through each valid number
					for (int pos=0; pos<validNums.size(); pos++) {
						
						int value = validNums.get(pos);

						if (knightRule && kingRule && queenRule) {
							if (!checkKing(row, col, value) && !checkQueen(row, col) && !checkKnight(row, col, value)) {
								grid[row][col] = value;
								
								//backtracking here 
								//if fully solved, add solution 
								if (solveMult()) {
									
									ChessSudoku original = new ChessSudoku(SIZE);
									for (int R=0; R<N; R++) {
										for (int C=0; C<N; C++) {
											original.grid[R][C] = this.grid[R][C];
										}
									}
									solutions.add(original);
									grid[row][col] = 0;
									
								} else {
									grid[row][col] = 0;
								}
							}
							
						}
						if (knightRule && kingRule && !queenRule) {
							if (!checkKnight(row, col, value) && !checkKing(row, col, value)) {
								grid[row][col] = value;
								if (solveMult()) {
									
									ChessSudoku original = new ChessSudoku(SIZE);
									for (int R=0; R<N; R++) {
										for (int C=0; C<N; C++) {
											original.grid[R][C] = this.grid[R][C];
										}
									}
									solutions.add(original);
									grid[row][col] = 0;
									
								} else {
									grid[row][col] = 0;
								}
							}
						}
						if (knightRule && queenRule && !kingRule) {
							if(!checkKnight(row, col, value) && !checkQueen(row, col)) {
								grid[row][col] = value;
								if (solveMult()) {
									
									ChessSudoku original = new ChessSudoku(SIZE);
									for (int R=0; R<N; R++) {
										for (int C=0; C<N; C++) {
											original.grid[R][C] = this.grid[R][C];
										}
									}
									solutions.add(original);
									grid[row][col] = 0;
									
								} else {
									grid[row][col] = 0;
								}
							}
						}
						if (queenRule && kingRule && !knightRule) {
							if (!checkQueen(row, col) && !checkKing(row, col, value)) {
								grid[row][col] = value;
								if (solveMult()) {
									
									ChessSudoku original = new ChessSudoku(SIZE);
									for (int R=0; R<N; R++) {
										for (int C=0; C<N; C++) {
											original.grid[R][C] = this.grid[R][C];
										}
									}
									solutions.add(original);
									grid[row][col] = 0;
									
								} else {
									grid[row][col] = 0;
								}
							}
						}
						if (knightRule && !queenRule && !kingRule) {
							if (!checkKnight(row, col, value)) {
								grid[row][col] = value;
								if (solveMult()) {
									
									ChessSudoku original = new ChessSudoku(SIZE);
									for (int R=0; R<N; R++) {
										for (int C=0; C<N; C++) {
											original.grid[R][C] = this.grid[R][C];
										}
									}
									solutions.add(original);
									grid[row][col] = 0;
									
								} else {
									grid[row][col] = 0;
								}
							}
						}
						if (queenRule && !knightRule && !kingRule) {
							if (!checkQueen(row, col)) {
								grid[row][col] = value;
								if (solveMult()) {
									
									ChessSudoku original = new ChessSudoku(SIZE);
									for (int R=0; R<N; R++) {
										for (int C=0; C<N; C++) {
											original.grid[R][C] = this.grid[R][C];
										}
									}
									solutions.add(original);
									grid[row][col] = 0;
									
								} else {
									grid[row][col] = 0;
								}
							}
						}
						if (kingRule && !queenRule && !knightRule) {
							if (!checkKing(row, col, value)) {
								grid[row][col] = value;
								if (solveMult()) {
									
									ChessSudoku original = new ChessSudoku(SIZE);
									for (int R=0; R<N; R++) {
										for (int C=0; C<N; C++) {
											original.grid[R][C] = this.grid[R][C];
										}
									}
									solutions.add(original);
									grid[row][col] = 0;
									
								} else {
									grid[row][col] = 0;
								}
							} 
						}
						if (!kingRule && !queenRule && !knightRule) {
							grid[row][col] = value;
							
							if (solveMult()) {
								
								ChessSudoku og = new ChessSudoku(SIZE);
								for (int R=0; R<N; R++) {
									for (int C=0; C<N; C++) {
										og.grid[R][C] = this.grid[R][C];
									}
								}
								solutions.add(og);
								grid[row][col] = 0;
								
							} else {
								grid[row][col] = 0;
							} 
						}
					}
					//if none of the numbers 1 to N work 
					return false;
				}
			}
		}
		// if no empty spaces were found - Base Case
		return true;
	}
	/* The solve() method should remove all the unknown characters ('x') in the grid
	 * and replace them with the numbers in the correct range that satisfy the constraints
	 * of the Sudoku puzzle. If true is provided as input, the method should find ALL 
	 * possible solutions and store them in the field named solutions. */
	public void solve(boolean allSolutions) {
		if (!allSolutions) {
			this.oneSol();
		} else {
			this.solveMult();

		}
		
	}


	/* Use read() function to load the Sudoku puzzle from a file or
	 * the standard input. */
	public ChessSudoku(int size ) {
		SIZE = size;
		N = size*size;

		grid = new int[N][N];
	}


	/* readInteger is a helper function for the reading of the input file.  It reads
	 * words until it finds one that represents an integer. For convenience, it will also
	 * recognize the string "x" as equivalent to "0". */
	static int readInteger( InputStream in ) throws Exception {

		int result = 0;
		boolean success = false;

		while( !success ) {
			String word = readWord( in );

			try {
				result = Integer.parseInt( word );
				success = true;
			} catch( Exception e ) {
				// Convert 'x' words into 0's
				if( word.compareTo("x") == 0 ) {
					result = 0;
					success = true;
				}
				// Ignore all other words that are not integers
			}
		}
		return result;
	}


	/* readWord is a helper function that reads a word separated by white space. */
	static String readWord( InputStream in ) throws Exception {
		StringBuffer result = new StringBuffer();
		int currentChar = in.read();
		String whiteSpace = " \t\r\n";
		// Ignore any leading white space
		while( whiteSpace.indexOf(currentChar) > -1 ) {
			currentChar = in.read();
		}

		// Read all characters until you reach white space
		while( whiteSpace.indexOf(currentChar) == -1 ) {
			result.append( (char) currentChar );
			currentChar = in.read();
		}
		return result.toString();
	}


	/* This function reads a Sudoku puzzle from the input stream in.  The Sudoku
	 * grid is filled in one row at at time, from left to right.  All non-valid
	 * characters are ignored by this function and may be used in the Sudoku file
	 * to increase its legibility. */
	public void read( InputStream in ) throws Exception {

		for( int i = 0; i < N; i++ ) {
			for( int j = 0; j < N; j++ ) {
				grid[i][j] = readInteger( in );
			}
		}
	}


	/* Helper function for the printing of Sudoku puzzle.  This function will print
	 * out text, preceded by enough ' ' characters to make sure that the printint out
	 * takes at least width characters.  */
	void printFixedWidth( String text, int width ) {
		for( int i = 0; i < width - text.length(); i++ )
			System.out.print( " " );
		System.out.print( text );
	}


	/* The print() function outputs the Sudoku grid to the standard output, using
	 * a bit of extra formatting to make the result clearly readable. */
	public void print() {
		// Compute the number of digits necessary to print out each number in the Sudoku puzzle
		int digits = (int) Math.floor(Math.log(N) / Math.log(10)) + 1;

		// Create a dashed line to separate the boxes 
		int lineLength = (digits + 1) * N + 2 * SIZE - 3;
		StringBuffer line = new StringBuffer();
		for( int lineInit = 0; lineInit < lineLength; lineInit++ )
			line.append('-');

		// Go through the grid, printing out its values separated by spaces
		for( int i = 0; i < N; i++ ) {
			for( int j = 0; j < N; j++ ) {
				printFixedWidth( String.valueOf( grid[i][j] ), digits );
				// Print the vertical lines between boxes 
				if( (j < N-1) && ((j+1) % SIZE == 0) )
					System.out.print( " |" );
				System.out.print( " " );
			}
			System.out.println();

			// Print the horizontal line between boxes
			if( (i < N-1) && ((i+1) % SIZE == 0) )
				System.out.println( line.toString() );
		}
	}


	/* The main function reads in a Sudoku puzzle from the standard input, 
	 * unless a file name is provided as a run-time argument, in which case the
	 * Sudoku puzzle is loaded from that file.  It then solves the puzzle, and
	 * outputs the completed puzzle to the standard output. */
	public static void main( String args[] ) throws Exception {
		Scanner input = new Scanner(System.in);
		System.out.println("Enter path to sudoku file: ");
		String userInput = input.nextLine();

		//"src/SudokuSolver/SudokuPuzzles/2veryHard3x3.txt"
		InputStream in = new FileInputStream(userInput);

		// The first number in all Sudoku files must represent the size of the puzzle.  See
		// the example files for the file format.
		int puzzleSize = readInteger( in );
		if( puzzleSize > 100 || puzzleSize < 1 ) {
			System.out.println("Error: The Sudoku puzzle size must be between 1 and 100.");
			System.exit(-1);
		}

		ChessSudoku s = new ChessSudoku( puzzleSize );
		
		// modify these to add rules to your sudoku
		s.knightRule = false;
		s.kingRule = false;
		s.queenRule = false;

		// read the rest of the Sudoku puzzle
		s.read( in );
		System.out.println("Before the solve:");
		s.print();
		System.out.println();

		// Solve the puzzle by finding all solutions.
		double starttime;

		if (s.knightRule || s.kingRule || s.queenRule) {
			starttime = System.nanoTime();
			s.solve(true);

		} else {
			starttime = System.nanoTime();
			DLXSudoku sud = new DLXSudoku(s);
			int[][] exactCover = sud.sudokuToExactCover(s);
			DancingLinks dlx = new DancingLinks(exactCover, s);
			dlx.solve();
		}

		double stoptime = System.nanoTime();
		double time = (stoptime - starttime) / 1000000;
		System.out.println("the solver took "+ time + " ms");
		System.out.println("");


		// Print out the completed puzzle
		System.out.println("After the solve:");
		System.out.println("there are" + " " + s.solutions.size() + " solutions!");
		Iterator<ChessSudoku> it = s.solutions.iterator();
		while (it.hasNext()) {
			it.next().print();
			System.out.println("");
		}


	}
}

