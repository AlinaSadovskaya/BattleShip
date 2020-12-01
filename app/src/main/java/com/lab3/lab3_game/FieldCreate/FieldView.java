package com.lab3.lab3_game.FieldCreate;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.lab3.lab3_game.Structures.GamePartition;
import com.lab3.lab3_game.Structures.GameField;
import com.lab3.lab3_game.R;
import com.lab3.lab3_game.Structures.FieldMode;
import com.lab3.lab3_game.Structures.GameField;

import java.util.ArrayList;

public class FieldView extends View {
    private int cellWidth;
    private int cellHeight;

    private Paint PurplePaint;
    private Paint RedPaint;
    private Paint GreyishPaint;
    private Paint FieldGridPaint;

    private GameField field;
    private FieldMode fieldMode;
    private Context context;

    int battleshipsCount = 4;
    int cruisersCount = 3;
    int destroyersCount = 2;
    int torpedosCount = 1;
    int currentBattleshipsCount = 0;
    int currentCruisersCount = 0;
    int currentDestroyersCount = 0;
    int currentTorpedosCount = 0;
    boolean [][] correctCells;

    public FieldView(Context context)
    {
        super(context, null);
        this.context = context;

    }

    public FieldView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        this.context = context;
        PurplePaint = new Paint();
        PurplePaint.setColor(ContextCompat.getColor(context, R.color.ship_color));
        RedPaint = new Paint();
        RedPaint.setColor(ContextCompat.getColor(context,R.color.attacked_red));
        GreyishPaint = new Paint();
        GreyishPaint.setColor(ContextCompat.getColor(context,R.color.missed_greyish));
        FieldGridPaint = new Paint();
        FieldGridPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkest));
    }

    public void createField()
    {
        field = new GameField();
        this.fieldMode = FieldMode.CREATION;
    }

    public void setFieldMode(FieldMode mode)
    {   this.fieldMode = mode;  }

    public void updateField(GameField field)
    {
        this.field = field;
        invalidate();
    }

    public void setField(GameField field, FieldMode mode)
    {
        this.fieldMode = mode;
        this.field = field;
    }

    public GameField getField()
    {   return field;   }


    private void getSizes()
    {
        cellHeight = getHeight() / field.height;
        cellWidth = getWidth() / field.width;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int width, int height, int previous_height, int previous_width)
    {
        super.onSizeChanged(width, height, previous_height, previous_width);
        getSizes();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.drawColor(Color.WHITE);
        int width = getWidth();
        int height = getHeight();

        for (int i = 0; i < field.height; i++)
        {
            for (int j = 0; j < field.width; j++)
            {
                if (field.getPartition(i, j) == GamePartition.SHIP)
                {
                    if (fieldMode != FieldMode.OPPONENT)
                    {
                        canvas.drawRect(i*cellWidth, j * cellHeight, (i+1) * cellWidth, (j+1) * cellHeight, PurplePaint);
                    }
                }
                else if (field.getPartition(j, i) == GamePartition.MISS)
                {
                    canvas.drawRect(i*cellWidth, j * cellHeight, (i+1) * cellWidth, (j+1) * cellHeight, GreyishPaint);
                }
                else if (field.getPartition(j, i) == GamePartition.HURT)
                {
                    canvas.drawRect(i*cellWidth, j * cellHeight, (i+1) * cellWidth, (j+1) * cellHeight, RedPaint);
                }
            }
        }

        for (int i = 1; i < field.width; i++) {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, height, FieldGridPaint);
        }

        for (int i = 1; i < field.height; i++) {
            canvas.drawLine(0, i * cellHeight, width, i * cellHeight, FieldGridPaint);
        }

        canvas.drawLine(0, 0, 0, height, FieldGridPaint);
        canvas.drawLine(width, 0, width, height, FieldGridPaint);
        canvas.drawLine(0, 0, width, 0, FieldGridPaint);
        canvas.drawLine(0, height, width, height, FieldGridPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (fieldMode != FieldMode.READONLY && event.getAction() == MotionEvent.ACTION_DOWN) {

            int x = (int) event.getX() / cellWidth;
            int y = (int) event.getY() / cellHeight;
            if (x < field.width && y < field.height) {

                if (fieldMode == FieldMode.CREATION) {
                    if (field.getPartition(x, y) == GamePartition.EMPTY) {
                        field.setPartition(GamePartition.SHIP, x, y);
                    } else
                        field.setPartition(GamePartition.EMPTY, x, y);
                    if (!tryToPlay())
                        showError();

                } else {
                    if (field.getPartition(x, y) == GamePartition.EMPTY) {
                        field.setPartition(GamePartition.MISS, x, y);
                        //updategrid
                    } else if (field.getPartition(x, y) == GamePartition.SHIP) {
                        field.setPartition(GamePartition.HURT, x, y);
                        //updategrid
                    }

                }
            }
        }
        invalidate();
        return true;
    }

    public boolean endCreation(){
        if (tryToPlay())
        {
            return battleshipsCount == currentBattleshipsCount && cruisersCount == currentCruisersCount &&
                    torpedosCount == currentTorpedosCount && destroyersCount == currentDestroyersCount;
        }
        else return false;
    }

    public boolean tryToPlay()
    {
        correctCells = new boolean[field.height][field.width];
        currentBattleshipsCount = 0;
        currentCruisersCount = 0;
        currentDestroyersCount = 0;
        currentTorpedosCount = 0;
        for (int i = 0; i < field.height; i++)
        {
            for (int j = 0; j < field.width; j++)
                if (tryToPlayWithCell(i, j) == false)
                    return false;
        }
        return true;
        /*return battleshipsCount == currentBattleshipsCount && cruisersCount == currentCruisersCount &&
                torpedosCount == currentTorpedosCount && destroyersCount == currentDestroyersCount;*/
    }

    private boolean tryToPlayWithCell(int i, int j)
    {
        if (!checkNeighbors(i, j)) {
            return false;
        }
        else
        {
            correctCells[i][j] = true;
            return true;
        }

    }

    private boolean checkNeighbors(int i, int j)
    {
        if (correctCells[i][j])
            return true;
        if (field.getPartition(i, j) == GamePartition.EMPTY)
            return true;
        int nearbyShipCells = 0;
        ArrayList<GamePartition> neighborsCells = new ArrayList<>();
        ArrayList<GamePartition> cornerCells = new ArrayList<>();
        if (i > 0) {
            if (!correctCells[i-1][j])
                neighborsCells.add(field.getPartition(i - 1, j));
            if (j + 1 < field.height) {
                if (!correctCells[i-1][j+1])
                    cornerCells.add(field.getPartition(i - 1, j + 1));
            }
            if (j > 0) {
                if (!correctCells[i-1][j-1])
                    cornerCells.add(field.getPartition(i - 1, j - 1));
            }
        }
        if (j + 1 < field.height)
            if (!correctCells[i][j+1])
                neighborsCells.add(field.getPartition(i, j+1));
        if (j - 1 >= 0)
            if (!correctCells[i][j-1])
                neighborsCells.add(field.getPartition(i, j - 1));
        if (i + 1 < field.width)
        {
            if (!correctCells[i+1][j])
                neighborsCells.add(field.getPartition(i + 1, j));
            if (j + 1 < field.height) {
                if (!correctCells[i+1][j+1])
                    cornerCells.add(field.getPartition(i + 1, j + 1));
            }
            if (j - 1 >= 0) {
                if (!correctCells[i+1][j-1])
                    cornerCells.add(field.getPartition(i + 1, j - 1));
            }
        }
        for (GamePartition corner : cornerCells)
        {
            if (corner == GamePartition.SHIP) {
                return false;
            }
        }
        for (GamePartition neighbor : neighborsCells)
        {
            if (neighbor == GamePartition.SHIP)
                nearbyShipCells++;
        }
        if (nearbyShipCells == 0) {
            currentBattleshipsCount++;
            return true;
        }
        else if (nearbyShipCells < 2)
            return checkLength(i, j);
        else {
            return false;
        }
    }

    private boolean checkLength(int i, int j)
    {
        int shipLength = 1;
        GamePartition leftCell = null, rightCell = null, upperCell = null, lowerCell = null;
        if (i + 1 < field.width)
            rightCell = field.getPartition(i+1, j);
        if (j - 1 >= 0)
            upperCell = field.getPartition(i, j-1);
        if (j + 1 < field.height)
            lowerCell = field.getPartition(i, j+1);
        if (i - 1 >= 0)
            leftCell = field.getPartition(i-1, j);


        if (leftCell == GamePartition.SHIP)
        {
            int iter = i - 1;
            while (iter >= 0 && field.getPartition(iter, j) == GamePartition.SHIP)
            {
                if (correctCells[iter][j])
                    return false;
                shipLength++;
                correctCells[iter][j] = true;
                iter--;
            }
        }

        else if (rightCell == GamePartition.SHIP)
        {
            int iter = i + 1;
            while (iter < field.width && field.getPartition(iter, j) == GamePartition.SHIP)
            {
                if (correctCells[iter][j])
                    return false;
                shipLength++;
                correctCells[iter][j] = true;
                iter++;
            }
        }

        else if (upperCell == GamePartition.SHIP)
        {
            int iter = j - 1;
            while (iter >= 0 && field.getPartition(i, iter) == GamePartition.SHIP)
            {
                if (correctCells[i][iter])
                    return false;
                shipLength++;
                correctCells[i][iter] = true;
                iter--;
            }
        }
        else if (lowerCell == GamePartition.SHIP)
        {
            int iter = j + 1;
            while (iter < field.height && field.getPartition(i, iter) == GamePartition.SHIP)
            {
                if (correctCells[i][iter])
                    return false;
                shipLength++;
                correctCells[i][iter] = true;
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

    private void showError()
    {
        Toast toast = Toast.makeText(context,
                "Incorrect placement for ships.",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout toastContainer = (LinearLayout) toast.getView();
        ImageView catImageView = new ImageView(context);
        catImageView.setImageResource(R.drawable.kitty_wow);
        toastContainer.addView(catImageView, 0);
        toast.show();
    }


}
