package com.bkgoutchecker.api.repository;

import com.bkgoutchecker.api.dto.BakongResponse;
import com.bkgoutchecker.api.exceptions.NotFoundException;
import com.bkgoutchecker.api.helper.JdbcInternalConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Repository
public class BakongRepository {
    @Autowired
    private JdbcInternalConnection jdbcInternalConnection;

    public BakongResponse getTransactionByExtRef(String extRef) throws SQLException {
        // SQL query to fetch trx_hash and amount based on ext_ref
        String queryStr = "SELECT trx_hash, amount, asset_id, dst_bin , create_time , ext_ref " +
                "FROM fst_iroha_trx " +
                "WHERE src_account_id = 'cpbpkhppxxx@cpbp' AND ext_ref = ?";
        // Get the data from the secondary database using JdbcInternalConnection
        ResultSet result = jdbcInternalConnection.getData(queryStr, extRef);
        if (result != null && result.next()) {
            // Return the transaction response with trx_hash and amount
            return new BakongResponse(
                    result.getString("ext_ref"),
                    result.getString("trx_hash"),
                    result.getDouble("amount"),
                    result.getString("asset_id").split("#")[0],
                    "Cambodia Post bank Plc",
                    result.getString("dst_bin"),
                    convertToLocalDateTime(result.getString("create_time"))
            );
        } else {
            // Handle the case where no data is found (optional)
            throw new NotFoundException("Transaction with ext_ref '" + extRef + "' not found");
        }

    }

    private String convertToLocalDateTime(String utcTime) {
        // Define the formatter for the input format
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        // Parse the input string as a LocalDateTime
        LocalDateTime utcDateTime = LocalDateTime.parse(utcTime, inputFormatter);

        // Convert to ZonedDateTime in UTC
        ZonedDateTime zonedUtcTime = utcDateTime.atZone(ZoneId.of("UTC"));

        // Convert to local timezone
        LocalDateTime localDateTime = zonedUtcTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

        // Format the result to a string
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(outputFormatter);
    }

}
