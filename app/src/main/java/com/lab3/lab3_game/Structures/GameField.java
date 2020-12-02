package com.lab3.lab3_game.Structures;

import java.io.Serializable;

public class GameField implements Serializable {

    private final GamePartition[][] cells;

    public GameField()
    {
        cells = new GamePartition[10][10];
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                cells[i][j] = GamePartition.EMPTY;
    }

    public GamePartition getCell(int x, int y)
    {   return cells[x][y];     }

    public void setCellMode(GamePartition mode, int x, int y)
    {   cells[x][y] = mode;     }
}
