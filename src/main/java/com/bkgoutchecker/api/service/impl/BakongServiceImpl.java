package com.bkgoutchecker.api.service.impl;

import com.bkgoutchecker.api.dto.BakongResponse;
import com.bkgoutchecker.api.repository.BakongRepository;
import com.bkgoutchecker.api.service.BakongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class BakongServiceImpl implements BakongService {
    @Autowired
    private BakongRepository bakongRepository;

    @Override
    public BakongResponse getTransactionByExtRef(String extRef) throws SQLException {
        return bakongRepository.getTransactionByExtRef(extRef);
    }
}
