package com.lab3.lab3_game.Structures;

import java.io.Serializable;

public class GameField  implements Serializable {

    public final int height;
    public final int width;
    private final GamePartition[][] cells;

    public GameField()
    {
        height = 10;
        width = 10;
        cells = new GamePartition[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                cells[i][j] = GamePartition.EMPTY;
    }

    public GamePartition getPartition(int x, int y)
    {
        return cells[x][y];
    }

    public void setPartition(GamePartition mode, int x, int y)
    {
        cells[x][y] = mode;
    }

}
