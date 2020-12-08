package com.lab3.lab3_game.CreateGameField;

import com.lab3.lab3_game.Structures.GamePartition;
import com.lab3.lab3_game.Structures.GameField;


public class CheckGameField {

    private final GameField gameField;

    private final static int one_sell = 4;
    private final static int two_sells = 3;
    private final static int three_sells = 2;
    private final static int for_sells = 1;

    private int currentOneSellCount = 0;
    private int currentTwoSellsCount = 0;
    private int currentThreeSellsCount = 0;
    private int currentForSellsCount = 0;
    private boolean [][] correctCells;

    public CheckGameField(GameField gameField)
    {
        this.gameField = gameField;
    }

    public boolean finalCheck(){
        if (checkField())
        {
            return one_sell == currentOneSellCount && two_sells == currentTwoSellsCount &&
                    for_sells == currentForSellsCount && three_sells == currentThreeSellsCount;
        }
        else return false;
    }

    public boolean checkField()
    {
        correctCells = new boolean[10][10];
        currentOneSellCount = 0;
        currentTwoSellsCount = 0;
        currentThreeSellsCount = 0;
        currentForSellsCount = 0;

        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                if (!checkCell(i, j))
                    return false;
        return true;
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
            currentTwoSellsCount++;
        else if (shipLength == 3)
            currentThreeSellsCount++;
        else if (shipLength == 4)
            currentForSellsCount++;
        return true;
    }

    private boolean checkCell(int i, int j)
    {
        if (!checkShip(i, j)) {
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
                currentOneSellCount++;
                return true;
            } else if (nearbyShipCellsVertical <= 2 && nearbyShipCellsHorizontal == 0 || nearbyShipCellsVertical == 0 && nearbyShipCellsHorizontal <= 2)
                return checkLength(i, j);
            else
                return false;
        }
        else
            return (nearbyShipCellsVertical <= 2 && nearbyShipCellsHorizontal == 0 || nearbyShipCellsVertical == 0 && nearbyShipCellsHorizontal <= 2);
    }

    private boolean checkShip(int i, int j)
    {
        if (correctCells[i][j])
            return true;
        if (gameField.getCell(i, j) == GamePartition.EMPTY)
            return true;
        return checkNearbyCells(i, j, true);
    }


}
