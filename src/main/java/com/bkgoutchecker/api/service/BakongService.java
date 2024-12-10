package com.bkgoutchecker.api.service;

import com.bkgoutchecker.api.dto.BakongResponse;

import java.sql.SQLException;

public interface BakongService {
    BakongResponse getTransactionByExtRef(String extRef) throws SQLException;
}
