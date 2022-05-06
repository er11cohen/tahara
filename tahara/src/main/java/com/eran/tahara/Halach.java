package com.eran.tahara;

public class Halach {
    private int halachIndex;
    private String halachHe;
    private String href;
    private int htmlPageIndex;

    private String time;
    private int scrollY;

    //for keyHalachotActivity
    public Halach(int halachIndex, String halachHe, String href, int htmlPageIndex) {
        this.halachIndex = halachIndex;
        this.halachHe = halachHe;
        this.href = href;
        this.htmlPageIndex = htmlPageIndex;
    }

    //for locations
    public Halach(String time, int scrollY, String halachHe, int htmlPageIndex) {
        this.time = time;
        this.scrollY = scrollY;
        this.halachHe = halachHe;
        this.htmlPageIndex = htmlPageIndex;
    }

    @Override
    public String toString() {
        return getHalachHe(); //what you want displayed for each row in the listview
    }

    public int getHalachIndex() {
        return halachIndex;
    }

    public void setHalachIndex(int halachIndex) {
        this.halachIndex = halachIndex;
    }

    public String getHalachHe() {
        return halachHe;
    }

    public void setHalachHe(String halachHe) {
        this.halachHe = halachHe;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public int getHtmlPageIndex() {
        return htmlPageIndex;
    }

    public void setHtmlPageIndex(int htmlPageIndex) {
        this.htmlPageIndex = htmlPageIndex;
    }


    public String getTime() {
        return time;
    }

    public void setYime(String time) {
        this.time = time;
    }

    public int getScrollY() {
        return scrollY;
    }

    public void setScrollY(int scrollY) {
        this.scrollY = scrollY;
    }


}
