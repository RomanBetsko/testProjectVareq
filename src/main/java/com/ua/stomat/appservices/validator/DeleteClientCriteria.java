package com.ua.stomat.appservices.validator;

import java.io.Serializable;

public class DeleteClientCriteria implements Serializable {

    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
