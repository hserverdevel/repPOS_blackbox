package com.example.blackbox.model;

/**
 * Created by tiziano on 12/14/17.
 */

public class ButtonPermission {

    private boolean invoice;
    private boolean print;
    private boolean homage;
    private boolean dicount;
    private boolean round;
    private boolean email;
    private boolean peramount;
    private boolean perperson;
    private boolean peritem;
    private boolean pernumber;

    public ButtonPermission(){
        this.invoice = true;
        this.print = true;
        this.homage = true;
        this.dicount = true;
        this.round = true;
        this.email = true;
        this.peramount = true;
        this.perperson = true;
        this.peritem = true;
        this.pernumber = true;
    }

    public void setPeramount(Boolean b){ this.peramount=b;}
    public void setPerperson(Boolean b){ this.perperson=b;}
    public void setPeritem(Boolean b){ this.peritem=b;}
    public void setPernumber(Boolean b){ this.pernumber=b;}


    public boolean getPeramount(){return this.peramount;}
    public boolean getPerperson(){ return this.perperson;}
    public boolean getPeritem(){ return this.peritem;}
    public boolean getPernumber(){ return this.pernumber;}

}
