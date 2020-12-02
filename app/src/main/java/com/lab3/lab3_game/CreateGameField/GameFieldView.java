package com.lab3.lab3_game.CreateGameField;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.lab3.lab3_game.Activities.GameActivity;
import com.lab3.lab3_game.Structures.GameField;
import com.lab3.lab3_game.Structures.GamePartition;
import com.lab3.lab3_game.Structures.MoveResult;
import com.lab3.lab3_game.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class GameFieldView extends View {

    private Paint PurplePaint;
    private Paint RedPaint;
    private Paint GreyishPaint;
    private Paint FieldGridPaint;
    private Paint GreyPaint;
    private Paint borderPaint;

    private int cellWidth;
    private int cellHeight;

    private GameField gameField;
    private CurrentGameFieldMode fieldMode;
    private final Context context;

    public GameFieldView(Context context)
    {
        super(context, null);
        this.context = context;

    }

    public GameFieldView(Context context, AttributeSet attributeSet)
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
        GreyPaint = new Paint();
        GreyPaint.setColor(ContextCompat.getColor(context, R.color.colorGrey));
        borderPaint = new Paint();
        borderPaint.setStrokeWidth(7);
        borderPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkest));
    }

    public void createField()
    {
        gameField = new GameField();
        this.fieldMode = CurrentGameFieldMode.CREATION;
    }

    public void initializeField(CurrentGameFieldMode mode)
    {
        gameField = new GameField();
        this.fieldMode = mode;
    }

    public void setFieldMode(CurrentGameFieldMode mode)
    {   this.fieldMode = mode;  }


    public void updateField(GameField gameField)
    {
        this.gameField = gameField;
        invalidate();
    }

    public void setField(GameField gameField, CurrentGameFieldMode mode)
    {
        this.fieldMode = mode;
        this.gameField = gameField;
    }

    public GameField getGameField()
    {   return gameField;   }


    private void getSizes()
    {
        cellHeight = getHeight() / 10;
        cellWidth = getWidth() / 10;
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

        for (int i = 0; i < 10; i++)
        {
            for (int j = 0; j < 10; j++)
            {
                if (gameField.getCell(i, j) == GamePartition.SHIP)
                {
                    if (fieldMode != CurrentGameFieldMode.PLAYER2 && fieldMode != CurrentGameFieldMode.READONLY)
                    {
                        canvas.drawRect(i*cellWidth, j * cellHeight, (i+1) * cellWidth, (j+1) * cellHeight, PurplePaint);
                    }
                }
                else if (gameField.getCell(i, j) == GamePartition.MISS)
                {
                    canvas.drawRect(i*cellWidth, j * cellHeight, (i+1) * cellWidth, (j+1) * cellHeight, GreyishPaint);
                    canvas.drawCircle(i  * cellWidth + 0.5f * cellWidth, j * cellHeight + 0.5f * cellHeight, cellWidth / 7, GreyPaint);
                }
                else if (gameField.getCell(i , j) == GamePartition.HURT)
                {
                    canvas.drawRect(i*cellWidth, j * cellHeight, (i+1) * cellWidth, (j+1) * cellHeight, RedPaint);

                }
            }
        }

        for (int i = 1; i < 10; i++) {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, height, FieldGridPaint);
        }

        for (int i = 1; i < 10; i++) {
            canvas.drawLine(0, i * cellHeight, width, i * cellHeight, FieldGridPaint);
        }

        canvas.drawLine(0, 0, 0, height, borderPaint);
        canvas.drawLine(width, 0, width, height, borderPaint);
        canvas.drawLine(0, 0, width, 0, borderPaint);
        canvas.drawLine(0, height, width, height, borderPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (fieldMode != CurrentGameFieldMode.READONLY && event.getAction() == MotionEvent.ACTION_DOWN) {

            int x = (int) event.getX() / cellWidth;
            int y = (int) event.getY() / cellHeight;
            if (x < 10 && y < 10) {

                if (fieldMode == CurrentGameFieldMode.CREATION) {
                    if (gameField.getCell(x, y) == GamePartition.EMPTY) {
                        gameField.setCellMode(GamePartition.SHIP, x, y);
                    } else
                        gameField.setCellMode(GamePartition.EMPTY, x, y);
                    if (!new CheckGameField(gameField).checkField())
                        showError();

                } else if (fieldMode == CurrentGameFieldMode.PLAYER2){
                    if (gameField.getCell(x, y) == GamePartition.EMPTY) {
                        gameField.setCellMode(GamePartition.MISS, x, y);
                        ((GameActivity)this.context).updatingMove(MoveResult.MISS);
                    } else if (gameField.getCell(x, y) == GamePartition.SHIP) {
                        gameField.setCellMode(GamePartition.HURT, x, y);
                        ((GameActivity)this.context).updatingMove(MoveResult.HIT);
                    }

                }
            }
        }
        invalidate();
        return true;
    }

    public boolean finalCheck()
    {
        return new CheckGameField(gameField).finalCheck();
    }

    private void showError()
    {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.player_field), "Incorrect placement for ships.", BaseTransientBottomBar.LENGTH_SHORT);
        snackbar.show();
    }


}
