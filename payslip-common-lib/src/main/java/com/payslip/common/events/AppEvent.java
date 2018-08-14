/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.common.events;

import com.google.gson.Gson;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 *
 * @author maliska
 */
public abstract class AppEvent implements Serializable {

    private String id;
    private Instant instant;
    private String subject;

    public AppEvent() {
        this.id = UUID.randomUUID().toString();
        instant = Instant.now();
    }

    public AppEvent(String subject) {
        this();
        this.subject = subject;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 53 * hash + (this.instant != null ? this.instant.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppEvent other = (AppEvent) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        if (this.instant != other.instant && (this.instant == null || !this.instant.equals(other.instant))) {
            return false;
        }
        return true;
    }

}
