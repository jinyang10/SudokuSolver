package SudokuSolverDLX;

public class DLXSudoku {

    private ChessSudoku sudoku;

    public DLXSudoku(ChessSudoku sudoku) {
        this.sudoku = sudoku;
    }

    public int[][] sudokuToExactCover(ChessSudoku sudoku) {

        int numRows = sudoku.N * sudoku.N*sudoku.N;
        int numCols = sudoku.N * sudoku.N * 4;
        int sizeSquared = sudoku.N * sudoku.N;
        int[][] cover = new int[numRows][numCols];

        int[][] sud = sudoku.grid;

        for (int i=0; i<sud.length; i++) {
            for (int j=0; j<sud[0].length; j++) {
                int value = sud[i][j];
                int baseRow = i * sizeSquared + j * sudoku.N;
                int exactRow = value + baseRow - 1;
                // there's a value in the grid; zero out the other 8 unneeded rows in grid for the sudoku value
                if (value != 0) {

                    // need ones for only 1 row

                    //cell constraint
                    int col = getCellConstraintIdx(exactRow);
                    cover[exactRow][col] = 1;
                    int nthConstraint = sizeSquared;

                    //row constraint
                    col = getRowConstraintIdx(exactRow, nthConstraint);
                    cover[exactRow][col] = 1;
                    nthConstraint+=sizeSquared;

                    // col constraint
                    col = getColConstraintIdx(exactRow, nthConstraint);
                    cover[exactRow][col] = 1;
                    nthConstraint += sizeSquared;

                    // box constraint
                    col = getBoxConstraintIdx(exactRow, nthConstraint);
                    cover[exactRow][col] = 1;

                } else {
                    // the value in the sudoku grid is a 0; must put constraints

                    for (int r=baseRow; r<baseRow+sudoku.N; r++) {
                        // cell constraint
                        int col = getCellConstraintIdx(r);
                        cover[r][col] = 1;
                        int nthConstraint = sizeSquared;

                        // row constraint
                        col = getRowConstraintIdx(r, nthConstraint);
                        cover[r][col] = 1;
                        nthConstraint+=sizeSquared;

                        // col constraint
                        col = getColConstraintIdx(r, nthConstraint);
                        cover[r][col] = 1;
                        nthConstraint += sizeSquared;

                        // box constraint
                        col = getBoxConstraintIdx(r, nthConstraint);
                        cover[r][col] = 1;
                    }
                }

            }
        }

        return cover;
    }
    public int getCellConstraintIdx(int row) {
        return row / sudoku.N;
    }
    public int getRowConstraintIdx(int row, int nthConstraint) {
        int sizeSquared = sudoku.N * sudoku.N;
        int col = (row % sudoku.N) + nthConstraint;
        int checkOffset = row / sizeSquared;
        int offset = sudoku.N*checkOffset;
        col = col+offset;
        return col;
    }
    public int getColConstraintIdx(int row, int nthConstraint) {
        return row % (sudoku.N*sudoku.N) + nthConstraint;
    }
    public int getBoxConstraintIdx(int row, int nthConstraint) {
        int col = (row % sudoku.N) + nthConstraint;

        // every 3 columns in G share same column in M
        // every 81 rows returns multiplier to 0
        int multiplierCol = (row / (sudoku.N* sudoku.SIZE)) % sudoku.SIZE ;
        int offSet = sudoku.N*multiplierCol;

        // every 243 rows in M indicates a new box in G
        int offsetRow = row / (sudoku.N*sudoku.N* sudoku.SIZE) * (sudoku.SIZE*sudoku.N);
        offSet += offsetRow;
        col = col+offSet;
        return col;
    }

    private int[][] makeBaseCover() {
        // every 9 rows in Cover matrix M = 1 col in sudoku grid G
        int numRows = sudoku.N * sudoku.N*sudoku.N;
        int numCols = sudoku.N * sudoku.N * 4;
        int sizeSquared = sudoku.N * sudoku.N;

        int[][] cover = new int[numRows][numCols];
        int nthConstraint = sizeSquared;

        // cell constraint
        // every 9 rows has 1s for the same column, then increment column
        for (int row=0; row<numRows; row++) {
            int col = row / sudoku.N;
            cover[row][col] = 1;
        }

        // row constraint
        // every 9 rows has 1s in the next column, same pattern repeats every 81 rows
        for (int row=0; row<numRows; row++) {
            int col = (row % sudoku.N) + nthConstraint;
            int checkOffset = row / sizeSquared;
            int offset = sudoku.N*checkOffset;
            cover[row][col+offset] = 1;
        }
        nthConstraint+=sizeSquared;

        // column constraint
        // every row has 1st in the next col, goes for 81 rows, then repeat
        for (int row=0; row<numRows; row++) {
            int col = row % sizeSquared + nthConstraint;
            cover[row][col] = 1;
        }
        nthConstraint+=sizeSquared;
        // box constraint
        // every box in G shares the same columns in M
        // every 27 rows shares the same pattern of columns
        // every 81 rows, the above pattern repeats
        // every 81*3 rows, the column is incremented past the last column used above
        for (int row = 0; row < numRows; row++) {
            int col = (row % sudoku.N) + nthConstraint;

            // every 3 columns in G share same column in M
            // every 81 rows returns multiplier to 0
            int multiplierCol = (row / (sudoku.N* sudoku.SIZE)) % sudoku.SIZE ;
            int offSet = sudoku.N*multiplierCol;

            // every 243 rows in M indicates a new box in G
            int offsetRow = row / (sizeSquared* sudoku.SIZE) * (sudoku.SIZE*sudoku.N);
            offSet += offsetRow;

            cover[row][col+offSet] = 1;
        }
        return cover;
    }

    private static void printGrid(int[][] result){
        int N = result.length;
        for(int i = 0; i < N; i++){
            String ret = "";
            for(int j = 0; j < result[0].length; j++){
                ret += result[i][j] + " ";
            }
            if (i < 10) {
                System.out.println("row#" + i + ":   " + ret);
            }
            else {
                System.out.println("row#" + i + ": " + ret);
            }
        }
        System.out.println();
    }
    private static void printBoxConstraint(int[][] result) {
        int N = result.length;
        for(int i = 0; i < N; i++){
            String ret = "";
            for(int j = 243; j < result[0].length; j++){
                ret += result[i][j] + " ";
            }
            if (i < 10) {
                System.out.println("row#" + i + ":   " + ret);
            }
            else {
                System.out.println("row#" + i + ": " + ret);
            }

        }
        System.out.println();
    }





}
