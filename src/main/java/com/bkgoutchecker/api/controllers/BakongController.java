package com.bkgoutchecker.api.controllers;

import com.bkgoutchecker.api.dto.BakongRequest;
import com.bkgoutchecker.api.dto.BakongResponse;
import com.bkgoutchecker.api.exceptions.ApiResponse;
import com.bkgoutchecker.api.service.BakongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/v1/transaction")
public class BakongController {
    @Autowired
    private BakongService bakongService;

    @PostMapping()
    public ApiResponse<BakongResponse> getTransaction(@RequestBody BakongRequest bakongRequest) throws SQLException {
        return new ApiResponse<>("success","Your request successfully",bakongService.getTransactionByExtRef(bakongRequest.getTrxref()));
    }
}
