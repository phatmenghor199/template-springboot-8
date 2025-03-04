package com.mailsender.api.mapper;

import com.mailsender.api.dto.*;
import com.mailsender.api.models.Customer;
import com.mailsender.api.models.ExchangeDashboard;
import com.mailsender.api.models.ExchangeRate;
import com.mailsender.api.request.ExchangeRateRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExchangeRateMapper {
    ExchangeRateMapper INSTANCE = Mappers.getMapper(ExchangeRateMapper.class);

    ExchangeRate toEntity(ExchangeRateRequestDto dto);
    ExchangeRateResponseDto toDto(ExchangeRate entity);
    // ✅ Add this method to map a List of ExchangeDashboard entities
    List<ExchangeDashboardResponseDto> toDtoDashboard(List<ExchangeDashboard> entities);

    default AllExchangeRateResponseDto mapToListDto(List<ExchangeRateResponseDto> content, Page<ExchangeRate> exchangeRates) {
        AllExchangeRateResponseDto exchangeRateResponseDto = new AllExchangeRateResponseDto();
        exchangeRateResponseDto.setContent(content);
        exchangeRateResponseDto.setPageNo(exchangeRates.getNumber() + 1); // Convert 0-indexed to 1-indexed page number
        exchangeRateResponseDto.setPageSize(exchangeRates.getSize());
        exchangeRateResponseDto.setTotalElements(exchangeRates.getTotalElements());
        exchangeRateResponseDto.setTotalPages(exchangeRates.getTotalPages());
        exchangeRateResponseDto.setLast(exchangeRates.isLast());
        return exchangeRateResponseDto;
    }
}
