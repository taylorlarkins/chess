package service;

import dataaccess.DataAccessObject;

public class ClearService {
    DataAccessObject dataAccess;

    public ClearService(DataAccessObject dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear() {
        dataAccess.clear();
    }
}
