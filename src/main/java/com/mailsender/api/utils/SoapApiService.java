package com.mailsender.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.mailsender.api.dto.ExchangeRateFromSoap;
import com.mailsender.api.enumation.AccountType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class SoapApiService {
    @Value("${soap.service.url}")
    private String soapServiceUrl;

    @Value("${soap.service.username}")
    private String username;

    @Value("${soap.service.password}")
    private String password;

    private final RestTemplate restTemplate;

    public ExchangeRateFromSoap getAccountInfo(AccountType accountType) {
        log.info("Getting account info for accountType: {}", accountType);
        log.info("SOAP Service URL: {}", soapServiceUrl);
        log.info("Username: {}", username);

        String soapRequest = buildSoapRequest(accountType);
        log.info("Generated SOAP Request: {}", soapRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.TEXT_XML);
        HttpEntity<String> requestEntity = new HttpEntity<>(soapRequest, headers);

        try {
            log.info("Sending SOAP request...");

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(soapServiceUrl,
                    requestEntity, String.class);
            String xmlResponse = responseEntity.getBody();

            log.info("Received SOAP response with status: {}", responseEntity.getStatusCode());
            log.debug("XML Response: {}", xmlResponse);

            return convertXmlToJson(xmlResponse);
            // return xml;
        } catch (HttpClientErrorException e) {
            log.error("HTTP error while calling SOAP service: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error occurred: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    private ExchangeRateFromSoap convertXmlToJson(String xml) {
        try {
            log.info("Converting XML response to ExchangeRateFromSoap object...");
            // Create an XmlMapper to read the XML
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode jsonNode = xmlMapper.readTree(xml.getBytes());

            JsonNode detailNode = jsonNode.path("Body")
                    .path("ToGetExchangeRateResponse")
                    .path("CPBGETEXCHANGERATEType")
                    .path("gCPBGETEXCHANGERATEDetailType")
                    .path("mCPBGETEXCHANGERATEDetailType");

            if (detailNode.isMissingNode()) {
                log.warn("Exchange rate details not found in the response XML.");
                throw new RuntimeException("Invalid SOAP response format.");
            }

            ExchangeRateFromSoap exchangeRate = new ExchangeRateFromSoap();

            String buyRate = detailNode.path("BUYRATE").asText("0");
            String sellRate = detailNode.path("SELLRATE").asText("0");


            exchangeRate.setBuyRate(new BigDecimal(buyRate).abs());
            exchangeRate.setSellRate(new BigDecimal(sellRate).abs());
            log.info("Successfully parsed exchange rates: Buy Rate = {}, Sell Rate = {}", buyRate, sellRate);

            return exchangeRate;
        } catch (Exception e) {
            log.error("Error converting XML to ExchangeRateFromSoap: {}", e.getMessage(), e);
            throw new RuntimeException("Error converting XML to AccountInfoDTO: " + e.getMessage(), e);
        }
    }

    private String buildSoapRequest(AccountType currencyType) {
        log.info("Building SOAP request for currency: {}", currencyType);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:dev=\"http://temenos.com/DEVMB3\">"
                + "<soapenv:Header/>"
                + "<soapenv:Body>"
                + "<dev:ToGetExchangeRate>"
                + "<WebRequestCommon>"
                + "<company/>"
                + "<password>" + password + "</password>"
                + "<userName>" + username + "</userName>"
                + "</WebRequestCommon>"
                + "<CPBGETEXCHANGERATEType>"
                + "<enquiryInputCollection>"
                + "<columnName>@ID</columnName>"
                + "<criteriaValue>" + currencyType + "</criteriaValue>"
                + "<operand>EQ</operand>"
                + "</enquiryInputCollection>"
                + "</CPBGETEXCHANGERATEType>"
                + "</dev:ToGetExchangeRate>"
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";
    }

}
