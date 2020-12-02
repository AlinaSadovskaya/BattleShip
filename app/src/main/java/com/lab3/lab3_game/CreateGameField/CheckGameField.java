package com.lab3.lab3_game.CreateGameField;

import com.lab3.lab3_game.Structures.GamePartition;
import com.lab3.lab3_game.Structures.GameField;


public class CheckGameField {

    private final GameField gameField;

    // final values for comparing and validating
    private final static int battleshipsCount = 4;
    private final static int cruisersCount = 3;
    private final static int destroyersCount = 2;
    private final static int torpedosCount = 1;

    private int currentBattleshipsCount = 0;
    private int currentCruisersCount = 0;
    private int currentDestroyersCount = 0;
    private int currentTorpedosCount = 0;
    private boolean [][] correctCells;

    public CheckGameField(GameField gameField)
    {
        this.gameField = gameField;
    }

    public boolean finalCheck(){
        if (checkField())
        {
            return battleshipsCount == currentBattleshipsCount && cruisersCount == currentCruisersCount &&
                    torpedosCount == currentTorpedosCount && destroyersCount == currentDestroyersCount;
        }
        else return false;
    }

    public boolean checkField()
    {
        correctCells = new boolean[10][10];
        currentBattleshipsCount = 0;
        currentCruisersCount = 0;
        currentDestroyersCount = 0;
        currentTorpedosCount = 0;

        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                if (!checkCell(i, j))
                    return false;
        return true;
    }

    private boolean checkCell(int i, int j)
    {
        if (!checkNeighbors(i, j)) {
            return false;
        } else
        {
            correctCells[i][j] = true;
            return true;
        }

    }

    private boolean checkNearbyCells(int i, int j, boolean checkLength)
    {
        int nearbyShipCellsHorizontal = 0, nearbyShipCellsVertical = 0;

        if (i > 0) {

            if (gameField.getCell(i - 1, j) == GamePartition.SHIP)
                nearbyShipCellsHorizontal++;
            if (j + 1 < 10)
                if (gameField.getCell(i - 1, j + 1) == GamePartition.SHIP)
                    return false;
            if (j > 0)
                if (gameField.getCell(i - 1, j - 1) == GamePartition.SHIP)
                    return false;
        }

        if (j + 1 < 10)
            if (gameField.getCell(i, j+1) == GamePartition.SHIP)
                nearbyShipCellsVertical++;
        if (j - 1 >= 0)
            if (gameField.getCell(i, j - 1) == GamePartition.SHIP)
                nearbyShipCellsVertical++;
        if (i + 1 < 10)
        {
            if (gameField.getCell(i + 1, j) == GamePartition.SHIP)
                nearbyShipCellsHorizontal++;
            if (j + 1 < 10)
                if (gameField.getCell(i + 1, j + 1) == GamePartition.SHIP)
                    return false;
            if (j - 1 >= 0)
                if (gameField.getCell(i + 1, j - 1) == GamePartition.SHIP)
                    return false;
        }

        if (checkLength) {
            if (nearbyShipCellsVertical == 0 && nearbyShipCellsHorizontal == 0) {
                currentBattleshipsCount++;
                return true;
            } else if (nearbyShipCellsVertical <= 2 && nearbyShipCellsHorizontal == 0 || nearbyShipCellsVertical == 0 && nearbyShipCellsHorizontal <= 2)
                return checkLength(i, j);
            else
                return false;
        }
        else
            return (nearbyShipCellsVertical <= 2 && nearbyShipCellsHorizontal == 0 || nearbyShipCellsVertical == 0 && nearbyShipCellsHorizontal <= 2);
    }

    private boolean checkNeighbors(int i, int j)
    {
        if (correctCells[i][j])
            return true;
        if (gameField.getCell(i, j) == GamePartition.EMPTY)
            return true;
        return checkNearbyCells(i, j, true);
    }

    private boolean checkLength(int i, int j)
    {
        int shipLength = 1;
        GamePartition leftCell = null, rightCell = null, upperCell = null, lowerCell = null;
        if (i + 1 < 10)
            rightCell = gameField.getCell(i+1, j);
        if (j - 1 >= 0)
            upperCell = gameField.getCell(i, j-1);
        if (j + 1 < 10)
            lowerCell = gameField.getCell(i, j+1);
        if (i - 1 >= 0)
            leftCell = gameField.getCell(i-1, j);


        if (leftCell == GamePartition.SHIP)
        {
            int iter = i - 1;
            while (iter >= 0 && gameField.getCell(iter, j) == GamePartition.SHIP)
            {
                if (checkNearbyCells(iter, j, false))
                    correctCells[iter][j] = true;
                else return false;
                shipLength++;
                iter--;
            }
        }

        else if (rightCell == GamePartition.SHIP)
        {
            int iter = i + 1;
            while (iter < 10 && gameField.getCell(iter, j) == GamePartition.SHIP)
            {
                if (checkNearbyCells(iter, j, false))
                    correctCells[iter][j] = true;
                else return false;
                shipLength++;
                iter++;
            }
        }

        else if (upperCell == GamePartition.SHIP)
        {
            int iter = j - 1;
            while (iter >= 0 && gameField.getCell(i, iter) == GamePartition.SHIP)
            {
                if (checkNearbyCells(i, iter, false))
                    correctCells[i][iter] = true;
                else return false;
                shipLength++;
                iter--;
            }
        }

        else if (lowerCell == GamePartition.SHIP)
        {
            int iter = j + 1;
            while (iter < 10 && gameField.getCell(i, iter) == GamePartition.SHIP)
            {
                if (checkNearbyCells(i, iter, false))
                    correctCells[i][iter] = true;
                else return false;
                shipLength++;
                iter++;
            }
        }

        if (shipLength > 4)
            return false;
        else if (shipLength == 2)
            currentCruisersCount++;
        else if (shipLength == 3)
            currentDestroyersCount++;
        else if (shipLength == 4)
            currentTorpedosCount++;
        return true;
    }

}
