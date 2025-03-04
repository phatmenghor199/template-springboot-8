package com.mailsender.api.mapper;

import com.mailsender.api.dto.AllCustomerResponseDto;
import com.mailsender.api.dto.AllEmailSchedulerResponseDto;
import com.mailsender.api.dto.CustomerResponseDto;
import com.mailsender.api.dto.EmailSchedulerResponseDto;
import com.mailsender.api.models.Customer;
import com.mailsender.api.models.EmailSendHistory;
import com.mailsender.api.request.CustomerRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmailSendHistoryMapper {
    EmailSendHistoryMapper INSTANCE = Mappers.getMapper(EmailSendHistoryMapper.class);


    //    @Mapping(target = "company", source = "company")
//    @Mapping(target = "branch", source = "branch")
//    @Mapping(target = "schedules", source = "schedules")
    EmailSchedulerResponseDto toDto(EmailSendHistory entity);

    List<EmailSchedulerResponseDto> toDtoList(List<EmailSendHistory> emailSendHistories);

    default AllEmailSchedulerResponseDto mapToListDto(List<EmailSchedulerResponseDto> content, Page<EmailSendHistory> customerPage) {
        AllEmailSchedulerResponseDto historyResponseDto = new AllEmailSchedulerResponseDto();
        historyResponseDto.setContent(content);
        historyResponseDto.setPageNo(customerPage.getNumber() + 1); // Convert 0-indexed to 1-indexed page number
        historyResponseDto.setPageSize(customerPage.getSize());
        historyResponseDto.setTotalElements(customerPage.getTotalElements());
        historyResponseDto.setTotalPages(customerPage.getTotalPages());
        historyResponseDto.setLast(customerPage.isLast());
        return historyResponseDto;
    }
}
