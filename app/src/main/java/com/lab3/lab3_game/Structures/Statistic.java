package com.lab3.lab3_game.Structures;

public class Statistic {

    private String player_1;
    private String player_2;
    private int score_1;
    private int score_2;

    public Statistic()  { }

    public Statistic(String p1, String p2, int sc1, int sc2)
    {
        this.player_1 = p1;
        this.player_2 = p2;
        this.score_1 = sc1;
        this.score_2 = sc2;
    }

    public void setPlayer_1(String p1)
    {   this.player_1 = p1;     }

    public void setPlayer_2(String p2)
    {   this.player_2 = p2;     }

    public void setScore_1(int sc)
    {   this.score_1 = sc;     }

    public void setScore_2(int sc)
    {   this.score_2 = sc;     }

    public String getPlayer_1()
    {   return player_1;    }

    public String getPlayer_2()
    {   return  player_2;   }

    public int getScore_1()
    {   return score_1;     }

    public int getScore_2()
    {   return score_2;     }

}

