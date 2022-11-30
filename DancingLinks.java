package SudokuSolverDLX;

import java.util.ArrayList;
import java.util.List;

public class DancingLinks {

    private ColumnNode header;
    private List<DancingNode> solution = new ArrayList<>();
    private ChessSudoku sudoku;

    class DancingNode {
        DancingNode L, R, U, D;
        ColumnNode C;
        int row;

        public DancingNode() {
            L = R = U = D = this;
        }
        public DancingNode(ColumnNode c) {
            this();
            C = c;
        }

        public void unlinkLR() {
            this.R.L = this.L;
            this.L.R = this.R;
        }
        public void unlinkUD() {
            this.U.D = this.D;
            this.D.U = this.U;
        }
        public void linkLR() {
            this.R.L = this;
            this.L.R = this;
        }
        public void linkUD() {
            this.U.D = this;
            this.D.U = this;
        }

        //add n1 to the right of this node
        public DancingNode addRight(DancingNode n1) {
            n1.R = this.R;
            n1.R.L = n1;
            n1.L = this;
            this.R = n1;
            return n1;
        }
        // adds n1 below this node
        public DancingNode addBelow(DancingNode n1) {
            assert (this.C == n1.C);

            n1.D = this.D;
            n1.D.U = n1;
            n1.U = this;
            this.D = n1;
            return n1;
        }

    }
    class ColumnNode extends DancingNode {
        int size;
        String name;

        public ColumnNode(String name) {
            super();
            size = 0;
            this.name = name;
            C = this;
        }

        // 'delete' all other columns with 1s in the same row as this column
        public void cover() {
            unlinkLR();
            DancingNode i = this.D;

            // i, at first, points to the 1st data object in this column
            // loop thru all data objects in the column
            while (i != this) {
                DancingNode j = i.R;

                // loop thru all the data objects in this row
                while (j != i) {
                    j.unlinkUD();
                    j.C.size--;
                    j = j.R;
                }
                i = i.D;
            }
            header.size--;
        }
        public void unCover() {
            DancingNode i = this.U;

            while (i != this) {
                DancingNode j = i.L;
                while (j != i) {
                    j.C.size++;
                    j.linkUD();
                    j = j.L;
                }
                i = i.U;
            }
            linkLR();
            header.size++;
        }
    }
    public ColumnNode selectSmallestColumn() {
        int min = Integer.MAX_VALUE;
        ColumnNode res = null;
        ColumnNode c = (ColumnNode) header.R;

        while (c != header) {
            int size = c.size;
            if (size < min) {
                min = size;
                res = c;
            }
            c = (ColumnNode) c.R;
        }
        return res;
    }

    // create dancing links from a cover grid
    public ColumnNode makeDLX(int[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;

        ColumnNode headerNode = new ColumnNode("header");

        List<ColumnNode> columnNodes = new ArrayList<>(cols);

        for (int i=0; i<cols; i++) {
            ColumnNode col = new ColumnNode(Integer.toString(i));
            columnNodes.add(col);
            headerNode = (ColumnNode) headerNode.addRight(col);
        }
        //return header pointer to header
        headerNode = headerNode.R.C;

        for (int i=0; i<rows; i++) {
            DancingNode prev = null;
            for (int j=0; j<cols; j++) {

                if (grid[i][j] == 1) {
                    ColumnNode col = columnNodes.get(j);
                    DancingNode newNode = new DancingNode(col);
                    newNode.row = i;
                    if (prev == null) {
                        prev = newNode;
                    }
                    col.U.addBelow(newNode);
                    prev = prev.addRight(newNode);
                    col.size++;
                }
            }
        }
        headerNode.size = cols;
        return headerNode;

    }

    public DancingLinks(int[][] coverGrid, ChessSudoku sudoku) {
        this.sudoku = sudoku;
        header = makeDLX(coverGrid);
    }

    private void search(int k) {
        if (header.R == header) {
            ChessSudoku sol = new ChessSudoku(sudoku.SIZE);
            sol.grid = deepCopyGrid(sudoku.grid);
            fillSudoku(sol);
            sudoku.solutions.add(sol);

        } else {
            ColumnNode c = selectSmallestColumn();
            if (c.size == 0) {
                return;
            }
            // unlink column c and 'delete' all other 1s in columns with 1s in the same rows
            c.cover();

            // loop thru each data object in column c, adding it to the list of solutions
            for(DancingNode r = c.D; r != c; r = r.D){
                solution.add(r);

                // remove the other 1s in the same row
                for(DancingNode j = r.R; j != r; j = j.R){
                    j.C.cover();
                }

                search(k + 1);

                r = solution.remove(solution.size() - 1);
                c = r.C;

                for(DancingNode j = r.L; j != r; j = j.L){
                    j.C.unCover();
                }
            }
            c.unCover();
        }
    }

    public void solve() {
        search(0);
    }

    public void fillSudoku(ChessSudoku sudoku) {

        for (DancingNode dNode : solution) {
            int squared = (sudoku.N * sudoku.N);

            int rowSudoku = dNode.row / squared;
            int colSudoku = (dNode.row - squared * (dNode.row / squared)) / sudoku.N;
            int val = dNode.row % sudoku.N + 1;

            sudoku.grid[rowSudoku][colSudoku] = val;

            DancingNode temp = dNode.R;

            while (temp != dNode) {
                rowSudoku = temp.row / squared;
                colSudoku = (temp.row - squared * (temp.row / squared)) / sudoku.N;
                val = temp.row % sudoku.N + 1;

                sudoku.grid[rowSudoku][colSudoku] = val;
                temp = temp.R;
            }
        }
    }
    public int[][] deepCopyGrid(int[][] grid) {
        int[][] copy = new int[grid.length][grid[0].length];
        for (int i=0; i<grid.length; i++) {
            for (int j=0; j<grid[0].length; j++) {
                copy[i][j] = grid[i][j];
            }
        }
        return copy;
    }
}
