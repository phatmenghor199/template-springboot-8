package com.mailsender.api.utils.filter;

import com.mailsender.api.enumation.Status;
import com.mailsender.api.enumation.StatusData;
import com.mailsender.api.models.Customer;
import com.mailsender.api.models.EmailSendHistory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;

public class EmailSchedulerSpecifications {

    public static Specification<EmailSendHistory> hasStatus(Status status) {
        return (root, query, criteriaBuilder) ->
                status == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<EmailSendHistory> createdOn(LocalDate date) {
        return (root, query, criteriaBuilder) ->
                date == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.between(
                        root.get("createdAt"),
                        date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay());
    }

    public static Specification<EmailSendHistory> customerUsernameOrEmailContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return null;
            }
            Join<EmailSendHistory, Customer> customerJoin = root.join("customer");
            Predicate usernamePredicate = cb.like(cb.lower(customerJoin.get("username")), "%" + search.toLowerCase() + "%");
            Predicate emailPredicate = cb.like(cb.lower(customerJoin.get("email")), "%" + search.toLowerCase() + "%");
            return cb.or(usernamePredicate, emailPredicate);
        };
    }
}
