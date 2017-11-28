package com.gmail.raducaz.arduinomate.model;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

import java.util.Date;

public interface Comment {
    int getId();
    int getProductId();
    String getText();
    String getLog();
    void setLog(String log);
    Date getPostedAt();
}
